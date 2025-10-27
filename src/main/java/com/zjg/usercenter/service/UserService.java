package com.zjg.usercenter.service;

import com.zjg.usercenter.model.domain.Tag;
import com.zjg.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【user(用户中心用户)】的数据库操作Service
* @createDate 2025-10-17 20:46:16
*/
public interface UserService extends IService<User> {


    //String USER_LOGIN_STATE = "userLoginState";

    /**
     *
     * @param userAccount  用户账户
     * @param userPassword  用户密码
     * @param checkPassword   校验密码
     * @return  返回新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String centerCode);


    /**
     *
     * @param userAccount 用户账户
     * @param userPassword   用户密码
     * @return   返回一个用户对象
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser   包含敏感信息用户
     * @return  去掉敏感信息用户
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request  请求
     * @return  返回一个整数
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签名查询用户
     * @param tagNameList  标签名列表
     * @return 查询到的用户列表
     */
    List<User> searchUserByTags(List<String> tagNameList);
}
