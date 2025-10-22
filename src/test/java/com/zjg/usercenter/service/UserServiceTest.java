package com.zjg.usercenter.service;
import java.util.Date;

import com.zjg.usercenter.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.util.DateUtil.now;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 * @author zjg
 */

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();

        user.setUsername("wanfeng");
        user.setUserAccount("123");
        user.setAvatarUrl("https://cn.bing.com/images/search?q=%e8%87%ad%e8%87%ad%e6%b3%a5&id=161EC173562251F7ECF2DB2ED9D3AB79C7AFE47F&FORM=IQFRBA");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        user.setUserStatus(0);
        user.setCreateTime(now());
        user.setUpdateTime(now());

        Boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);

    }

    @Test
    void userRegister() {
        String userAccount = "zjgang";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        String centerCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, centerCode);
//        Assertions.assertEquals(-1, result);
//        userPassword = "123455";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "ch i";
//        userPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "chibao";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "zjgfeng";
//        userPassword = "12345678";
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "chibao";
//        userPassword = "123456789";
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertTrue(result > 0);



    }
}