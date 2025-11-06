package com.zjg.usercenter;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        //list
        //数据存在JVM内存中
        List<String> list = new ArrayList<>();
        list.add("king");
        System.out.println("list = " + list.get(0));
        list.remove(0);
        //数据存在redis的内存中.  test-list是这个数据的key
        RList<String> rList = redissonClient.getList("test-list");
        //rList.add("king");
        System.out.println("rList = " + rList.get(0));
        rList.remove(0);


        //map
        Map<String, Integer> map = new HashMap<>();
        map.put("kun", 23);
        System.out.println("map = " + map.get("kun"));

        RMap<Object, Object> rMap = redissonClient.getMap("test-map");
        rMap.put("kun", 34);
        System.out.println("rMap = " + rMap.get("kun"));

        //set

        //stack
    }
}
