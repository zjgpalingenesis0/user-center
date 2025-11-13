package com.zjg.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjg.usercenter.enum_.TeamStatusEnum;
import com.zjg.usercenter.common.ErrorCode;
import com.zjg.usercenter.dto.TeamQuery;
import com.zjg.usercenter.exception.BusinessException;
import com.zjg.usercenter.model.domain.Team;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.model.domain.UserTeam;
import com.zjg.usercenter.model.request.TeamDeleteRequest;
import com.zjg.usercenter.model.request.TeamJoinRequest;
import com.zjg.usercenter.model.request.TeamQuitRequest;
import com.zjg.usercenter.model.request.TeamUpdateRequest;
import com.zjg.usercenter.service.TeamService;
import com.zjg.usercenter.mapper.TeamMapper;
import com.zjg.usercenter.service.UserService;
import com.zjg.usercenter.service.UserTeamService;
import com.zjg.usercenter.vo.TeamUserVO;
import com.zjg.usercenter.vo.UserVO;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author Lenovo
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-11-06 20:59:47
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {

//        1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
//        2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        final long userId = loginUser.getId();
//        3. 校验信息
    //        1. 队伍人数>1 && <=20(Optional.ofNullable防止为空)
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
    //        2. 队伍名 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名不满足要求");
        }
    //        3. 描述 <= 500
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述不满足要求");
        }
    //        4. status是否公开（int），不传默认为0，公开
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态码不满足要求");
        }

    //        5. 如果status是加密状态，一定要有密码，且 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不满足要求");
            }
        }
    //        6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前时间 < 超时时间");
        }

    //        7. 校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户创建队伍太多");
        }
//        4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        //测试事务是否生效的
//        if (true) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "插入用户关系失败");
//        }
        if(!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
//        5. 插入用户队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");

        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin) {

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            //根据队伍id查询
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            //根据队伍id列表查询
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            //根据队伍名称查询
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            //根据队伍描述查询
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            //同时对名称和描述搜索
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(
                        qw -> qw.like("name", searchText)
                                .or()
                                .like("description", searchText)
                );
            }
            //根据队伍最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("max_num", maxNum);
            }
            //根据队伍创建人id查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("user_id", userId);
            }
            //根据队伍状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            //这里非管理员只能查询到公开的队伍，不管登录的用户是谁
            //TODO:如果一个非管理员登录，且队伍信息输入时不设置status，那么不管这个队伍什么状态都不会返回异常，
            // 默认只查询status=0的队伍
            if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NOT_AUTH, "没有权限");
            }
            // 管理员可以查询所有状态的队伍.这一步很重要！！！困扰我很久的bug加个判断就解决了
            if (!isAdmin) {
                queryWrapper.eq("status", statusEnum.getValue());
            }
            //TODO: 创建了自己队伍的用户应该能看到非公开的队伍（在controller中实现了）
//
        }

        //不展示已过期的队伍
        queryWrapper.and(qw -> qw.gt("expire_time", new Date())
                .or()
                .isNull("expire_time"));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人用户信息
        // TODO:后续可以改成用SQL的，尤其是关联多个表的时候
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            UserVO userVO = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        //TODO:入队用户列表

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {

//        1. 判断请求参数是否为空
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR ,"请求参数为空");
        }
        Long id = teamUpdateRequest.getId();
        if (id == null ||  id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR ,"请求参数为空");
        }
//        2. 查询队伍是否存在
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
//        3. 只有管理员或者队伍的创建者可以修改
        if (!userService.isAdmin(loginUser) && oldTeam.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "权限不够");
        }
//        4. TODO:自己实现：如果用户传入的新值和老值一致，就不用update了。可以降低数据库使用次数
        //5. 如果队伍状态改为加密，必须要有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间要设置密码");
            }
        }
//        6. 更新成功
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求为空");
        }
        // 队伍必须存在，只能加入未过期的队伍
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);

        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已过期");
        }
        // 禁止加入私有的队伍
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }
        // 如果加入的队伍是加密的，必须密码匹配才可以
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (password == null || team.getPassword() == null || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "加密队伍，输入密码不正确");
            }
        }
        // 不能加入自己的队伍
//        Long userId = loginUser.getId();
//        if (team.getUserId() == userId) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入自己的队伍");
//        }

        Long userId = loginUser.getId();
        // 都放到锁里。意思是如果要加入同一个队伍，很多个线程一个一个进入其中的逻辑执行，按顺序来。
        // 但是不同用户加入不同队伍就可以同时进行
        // 只有单个服务器可以用这个synchronized(this){业务逻辑}，如果有多个服务器，可以用分布式锁
        //分布式锁，只有一个线程能抢到锁
        RLock lock = redissonClient.getLock("pika.join.team:" + teamId);
        try {
            // 抢锁并执行  加while要让每个线程都抢到一次,没抢到锁就循环，直到所有线程执行成功或者异常
            // (也就是登录用户所有想加入的队伍都是一次，满足条件能加入，不满足下面条件就不让加，但必须给机会)
            // 如果怕思索，就在循环外设置一个计数器count=0，每抢到一次锁就+1，直到max次数退出循环
            while(true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock" + Thread.currentThread().threadId());
                    // 用户最多可以加入5个队伍
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId);
                    long hasJoinTeam = userTeamService.count(queryWrapper);   // 用户已加入队伍数量
                    if (hasJoinTeam > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户加入太多队伍");
                    }
                    // 不能重复加入已加入的队伍
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId);
                    queryWrapper.eq("team_id", teamId);
                    long hasUserJoinTeam = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入队伍");
                    }

                    // 不能加入已满队伍
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("team_id", teamId);
//        long teamHasJoinNum = userTeamService.count(queryWrapper);   // 队伍中已加入人数
                    long teamHasJoinNum = getTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }

                    // 新增队伍-用户关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());

                    return userTeamService.save(userTeam);

                }
            }

        } catch (InterruptedException e) {
            log.error("joinTeam error", e);
            return false;
        } finally {
            //释放锁，只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock" +  Thread.currentThread().threadId());
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);

        Long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long hasTeamUserNum = userTeamService.count(queryWrapper);  //某用户与某队伍之间的关联关系
        if (hasTeamUserNum == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未加入这个队伍");
        }
        long teamHasUserNum = getTeamUserByTeamId(teamId);
        //如果队伍人数只有1人,直接解散
        if (teamHasUserNum == 1) {
            this.removeById(teamId);
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("team_id", teamId);

            return userTeamService.remove(queryWrapper);
        }
        //如果队伍还有其他人
        else {
            // 如果是队长
            // Java 中 Long 类型比较的常见问题，当 Long 值在一定范围内（-128 到 127）时，== 可以正常工作，但超出这个范围时就需要使用 equals() 方法。
            if (userId.equals(team.getUserId())) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("team_id", teamId);
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍里没人");
                }
                UserTeam nextUserTeam = userTeamList.get(1);//取出id第二小的用户队伍关系
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
                }
            }
            //移除关系
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("team_id", teamId);
            return userTeamService.remove(queryWrapper);
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User loginUser) {
//        1. 校验请求参数
        if (teamDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
//        2. 校验队伍是否存在
        Long teamId = teamDeleteRequest.getTeamId();
        Team team = getTeamById(teamId);
//        3. 校验你是不是队长
        Long userId = loginUser.getId();
        if (!userId.equals(team.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "用户不是队长，无权解散队伍");
        }
//        4. 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散失败");
        }
//        5. 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 获取某队伍当前人数
     * @param teamId  队伍id
     * @return 队伍当前人数
     */
    private long getTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        return userTeamService.count(queryWrapper);
    }

    /**
     * 根据队伍id获取队伍信息
     * @param teamId  队伍id
     * @return  队伍信息
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id为空");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }
}




