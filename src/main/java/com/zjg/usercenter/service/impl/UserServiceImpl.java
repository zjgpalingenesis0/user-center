package com.zjg.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjg.usercenter.common.ErrorCode;
import com.zjg.usercenter.exception.BusinessException;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.service.UserService;
import com.zjg.usercenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zjg.usercenter.constant.UserConstant.USER_LOGIN_STATE;

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
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




