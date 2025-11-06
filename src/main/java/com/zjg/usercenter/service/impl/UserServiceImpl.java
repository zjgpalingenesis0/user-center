package com.zjg.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zjg.usercenter.common.ErrorCode;
import com.zjg.usercenter.exception.BusinessException;
import com.zjg.usercenter.model.domain.Tag;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.service.UserService;
import com.zjg.usercenter.mapper.UserMapper;
import com.zjg.usercenter.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zjg.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.zjg.usercenter.constant.UserConstant.USER_LOGIN_STATE;
import static java.time.LocalDateTime.now;

/**
* @author Lenovo
* @description 针对表【user(用户中心用户)】的数据库操作Service实现
* @createDate 2025-10-17 20:46:16
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    private final static String SALT = "love";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;



    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String centerCode) {

        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword, centerCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入信息为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户太短");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码太短");
        }
        if (centerCode.length() > 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号太长");
        }
        //账户不能有特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户含特殊字符");
        }
        //密码和校验码相同
        if(!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入密码和校验码不同");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();  //创建查询包装器
        queryWrapper.eq("user_account",userAccount);  //设置查询条件：user_account字段等于指定的userAccount值
        long count = this.count(queryWrapper);  //如果这个输入的userAccount的名字已经有>0个，就不能注册
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入账户已存在");
        }

        //用户编号不能重复
        queryWrapper = new QueryWrapper<>();  //创建查询包装器
        queryWrapper.eq("center_code",centerCode);  //设置查询条件：user_account字段等于指定的userAccount值
        count = this.count(queryWrapper);  //如果这个输入的编号已经有>0个，就不能注册
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入编号已存在");
        }

        //2.加密
//        final String SALT = "love";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setCenterCode(centerCode);
        boolean saveResult = this.save(user);
        if(!saveResult) {
            throw new BusinessException(ErrorCode.SAVE_ERROR, "插入用户时出错");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入信息为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户太短");
        }
        if(userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码太短");
        }

        //账户不能有特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户含特殊字符");
        }

        //2.加密 为了之后验证密码，需要都变成md5加密后的形式
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //校验密码是否输入正确
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();  //创建查询包装器
        queryWrapper.eq("user_account",userAccount);  //设置查询条件：user_account字段等于指定的userAccount值
        queryWrapper.eq("user_password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper); // 从数据库中查询一条记录，将查询结果映射到java对象。 this中没有
        //用户不存在
        if (user == null) {
            //log.info("user login failed...");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或者密码错误");
        }

        //3. 用户信息脱敏
        User safetyUser = getSafetyUser(user);

        //4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }


    /**
     * 用户信息脱敏
     * @param originUser  用户包括敏感信息
     * @return 返回安全信息用户
     */
    @Override
    public User getSafetyUser(User originUser) {
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setCenterCode(originUser.getCenterCode());
        safetyUser.setTag(originUser.getTag());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户（内存查询）
     * @param tagNameList  标签名列表
     * @return 查到的用户列表
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {

        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入标签列表为空");
        }
        //内存查询
        //先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        //在内存中判断是否包含要求的标签
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagStr = user.getTag();
            if (StringUtils.isAnyBlank(tagStr)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());   //如果集合为空处理一下，防止空指针异常
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入为空");
        }
        Object objUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        User loginUser = (User) objUser;
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        return loginUser;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long id = user.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不符合规范");
        }
        if (!isAdmin(loginUser) && id != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "权限不够");
        }
        User oldUser = userMapper.selectById(id);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "查询为空");
        }

        return userMapper.updateById(user);  //这里是要前端输入的user信息内容，可能有改变的
    }

    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        User user =  getLoginUser(request);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public Boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public Page<User> getData(User loginUser, long pageNum, long pageSize) {
        //设置缓存Key
        String redisKey = String.format("pika.:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return userPage;
        }
        //没有缓存，从数据库中读
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userList = userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        //要把数据库中读出的数据放入缓存
        try {
            valueOperations.set(redisKey, userList, 10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }

        return userList;
    }

    /**
     * 根据标签搜索用户（sql查询）  @Deprecated 表示过时，不调用
     * @param tagNameList  标签名列表
     * @return 查到的用户列表
     */
    @Deprecated
    private List<User> searchUserByTagsBySQL(List<String> tagNameList) {

        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入标签列表为空");
        }
        //SQL方式
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tag",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);

        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

    }
}




