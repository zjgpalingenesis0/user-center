package com.zjg.usercenter.service;

import com.zjg.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lenovo
* @description 针对表【user(用户中心用户)】的数据库操作Service
* @createDate 2025-10-17 20:46:16
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param userAccount  用户账户
     * @param userPassword  用户密码
     * @param checkPassword   校验密码
     * @return  返回新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);
}
