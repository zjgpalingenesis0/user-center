package com.zjg.usercenter.once;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 导入用户到数据库
 */
public class ImportUser {
    public static void main(String[] args) {
        String fileName = "F:\\learnforwork\\project\\user-center\\user-center-backend\\src\\main\\resources\\static\\testExcel.xlsx";

        List<TableUserInfo> userInfoList =
                EasyExcel.read(fileName).head(TableUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());
        //按用户名分组，相同用户名的一组
        Map<String, List<TableUserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(TableUserInfo::getUsername));
        System.out.println("不重复昵称数 = " + listMap.keySet().size());
        for (Map.Entry<String, List<TableUserInfo>> entry : listMap.entrySet()) {
            //listMap  [key1:value1, key2:value2,...]
            //entry   key1:value1
            //list   [value1]
            //listKey  key1
            List<TableUserInfo> list = entry.getValue();
            String listKey = entry.getKey();
            System.out.println(list);
            System.out.println(listKey);
        }
    }

}
