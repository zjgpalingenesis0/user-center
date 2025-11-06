package com.zjg.usercenter.once;

import com.zjg.usercenter.mapper.UserMapper;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.service.UserService;
import jakarta.annotation.Resource;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class InsertUsersTest {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    //用自己的线程池，之前用的都是默认的
    private ExecutorService executorService = new ThreadPoolExecutor(20, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));


    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //定义数据量1000 -> 100000
        final int INSERT_NUM = 100000;

        //优化
        List<User> userList = new ArrayList<>();

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
            //按理说，是最慢的，我这里1000个数据9.042s
//            userMapper.insert(user);
            userList.add(user);
        }
        //优化加速了，批量插入方法
        // 1000条数据，bs100，2.4s
        //100000条数据  bs1000   39.986s
        //100000条数据   bs10000   40.908s，按理说应该更快，第二次37.357s
        //100000条数据   bs50000   39.281s
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
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
                if (j % BATCH_SIZE == 0) {
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, BATCH_SIZE);
            }, executorService);
            futureList.add(future);
        }
        //回到单线程来
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());   //20.442s 最快
    }
}