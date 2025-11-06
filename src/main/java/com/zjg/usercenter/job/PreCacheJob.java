package com.zjg.usercenter.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjg.usercenter.mapper.UserMapper;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    //重要用户列表
    private List<Long> mainUserList = Arrays.asList(1L);

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 14 21 * * *")
    public void doCacheRecommendUsers() {
        RLock lock = redissonClient.getLock("pika.precachejob.docache.lock");
        //只有一个能获取数据
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock" + Thread.currentThread().threadId());
                for (Long userId : mainUserList) {
                    //从数据库中取出数据
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page(1, 20), queryWrapper);
                    //创建rediskey
                    String redisKey = String.format("pika.user.recommend.%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写入缓存
                    try {
                        valueOperations.set(redisKey, userPage, 25, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUsers error", e);
        } finally {
            //释放锁，只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock" +  Thread.currentThread().threadId());
                lock.unlock();
            }
        }


    }

}
