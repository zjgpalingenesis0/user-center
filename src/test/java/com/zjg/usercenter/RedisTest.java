package com.zjg.usercenter;
import java.util.Date;

import com.zjg.usercenter.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void test(){
        //操作字符串类型
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增  操作
        valueOperations.set("wanfengString", "dog");
        valueOperations.set("wanfengInt", 1);
        valueOperations.set("wanfengDouble", 2.5);
        User user = new User();
        user.setId(48L);
        user.setUsername("wanfeng");
        valueOperations.set("wanfengUser", user);

        //查  操作
        Object wanfeng = valueOperations.get("wanfengString");
        Assertions.assertEquals("dog", (String) wanfeng);
        wanfeng = valueOperations.get("wanfengInt");
        Assertions.assertEquals(1, (Integer) wanfeng);
        wanfeng = valueOperations.get("wanfengDouble");
        Assertions.assertEquals(2.5, (Double) wanfeng);
        System.out.println(valueOperations.get("wanfengUser"));

        //改  .set

        //删  .delete
        redisTemplate.delete("wanfengString");
        //操作list类型
        //ListOperations listOperations = redisTemplate.opsForList();

    }
}
