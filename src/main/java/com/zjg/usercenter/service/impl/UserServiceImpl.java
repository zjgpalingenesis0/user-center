package com.zjg.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.service.UserService;
import com.zjg.usercenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author Lenovo
* @description 针对表【user(用户中心用户)】的数据库操作Service实现
* @createDate 2025-10-17 20:46:16
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return -1;
        }
        if(userAccount.length() < 4) {
            return -1;
        }
        if(userPassword.length() < 8) {
            return -1;
        }
        if(checkPassword.length() < 8) {
            return -1;
        }
        //账户不能有特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            return -1;
        }
        //密码和校验码相同
        if(!userPassword.equals(checkPassword)) {
            return -1;
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();  //创建查询包装器
        queryWrapper.eq("user_account",userAccount);  //设置查询条件：user_account字段等于指定的userAccount值
        long count = this.count(queryWrapper);  //如果这个输入的userAccount的名字已经有>0个，就不能注册
        if(count > 0) {
            return -1;
        }

        //2.加密
        final String SALT = "love";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult) {
            return -1;
        }

        return user.getId();
    }
}




