package com.zjg.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjg.usercenter.common.BaseResponse;
import com.zjg.usercenter.common.ErrorCode;
import com.zjg.usercenter.dto.TeamQuery;
import com.zjg.usercenter.exception.BusinessException;
import com.zjg.usercenter.model.domain.Team;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.model.domain.UserTeam;
import com.zjg.usercenter.model.request.*;
import com.zjg.usercenter.service.TeamService;
import com.zjg.usercenter.service.UserService;
import com.zjg.usercenter.service.UserTeamService;
import com.zjg.usercenter.utils.ResultUtils;
import com.zjg.usercenter.vo.TeamUserVO;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId, "成功创建队伍");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest request) {
        if (teamDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数有误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(teamDeleteRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
        }
        return ResultUtils.success(result, "成功解散队伍");
    }

    @PostMapping("update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数为空");
        }
        User loginUser = userService.getLoginUser(request);
//        boolean result = teamService.updateById(team);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改队伍信息失败");
        }
        return ResultUtils.success(result, "成功修改队伍信息");
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数有误");
        }
        Team result = teamService.getById(id);
        if (result == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "查询返回为空");
        }
        return ResultUtils.success(result, "成功找到队伍");
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数为空");
        }

        Boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 优化：判断当前用户是否加入队伍
        // 1. 查询队伍id列表
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).toList();
        // 2. 判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        //希望用户不登陆也能使用，但是getLoginUser方法中封装了一个报异常，为了有一场也能正常运行，用try-catch
        try {
            User loginUser = userService.getLoginUser(request);
            queryWrapper.eq("user_id", loginUser.getId());
            queryWrapper.in("team_id", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            //已加入的队伍id集合，这里用set因为去重（一个用户可能多次加入同一个队伍），查找高效
            Set<Long> hasJoinTeamIdSet = userTeamList.stream()
                    .map(UserTeam::getTeamId)
                    .collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        // 3. 查询加入队伍的用户信息（人数）
        queryWrapper = new  QueryWrapper<>();
        queryWrapper.in("team_id", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //每个队伍id 映射对应到 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
        return ResultUtils.success(teamList, "成功查询队伍列表");
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),  teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage, "分页查询成功");
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result, "用户成功加入队伍");
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);

        return ResultUtils.success(result, "成功退出队伍");
    }

    @GetMapping("/list/my")
    public BaseResponse<List<TeamUserVO>> listMyTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> list = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(list, "成功查询自己创建队伍列表");
    }

    @GetMapping("/list/myJoin")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入参数为空");
        }

        User loginUser = userService.getLoginUser(request);
        //获取到用户加入的队伍id列表
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);

        List<TeamUserVO> list = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(list, "成功查询队伍列表");
    }

}
