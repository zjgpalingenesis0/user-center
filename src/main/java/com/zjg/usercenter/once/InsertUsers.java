package com.zjg.usercenter.once;

import com.zjg.usercenter.mapper.UserMapper;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //定义数据量1000 -> 100000
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i ++) {
            User user = new User();   //下面的set可以在这行alt+enter，用快捷键
            user.setUsername("菊草叶");
            user.setUserAccount("jucao");
            user.setAvatarUrl("https://img.3dmgame.com/uploads/images/news/20191123/1574477007_162444.jpg");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("159");
            user.setEmail("shenqi@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setCenterCode("1313");
            user.setTag("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    public void doInsertUsersMultiThread() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //定义数据量1000 -> 100000
        final int INSERT_NUM = 100000;

        //多线程
        int j = 0;
        final int BATCH_SIZE = 5000;
        int group = INSERT_NUM / BATCH_SIZE;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < group; i ++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();   //下面的set可以在这行alt+enter，用快捷键
                user.setUsername("卡比兽");
                user.setUserAccount("卡比");
                user.setAvatarUrl("https://img.3dmgame.com/uploads/images/news/20191123/1574477007_162444.jpg");
                user.setGender(0);
                user.setUserPassword("123456789");
                user.setPhone("159");
                user.setEmail("shenqi@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setCenterCode("1313");
                user.setTag("[]");
                userList.add(user);
                if (j % 10000 == 0) {
                    break;
                }
                //异步执行
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    System.out.println("thredName: " + Thread.currentThread().getName());
                    userService.saveBatch(userList, 10000);
                });
                futureList.add(future);
            }
        }
        //回到单线程来
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
