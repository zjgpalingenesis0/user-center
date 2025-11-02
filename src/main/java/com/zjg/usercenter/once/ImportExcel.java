package com.zjg.usercenter.once;

import com.alibaba.excel.EasyExcel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导入 Excel
 */
@Slf4j
public class ImportExcel {

    /**
     * 读取数据
     */
    public static void main(String[] args) {

        String fileName = "F:\\learnforwork\\project\\user-center\\user-center-backend\\src\\main\\resources\\static\\testExcel.xlsx";
        System.out.println("监听器读");
        readByListener(fileName);
        System.out.println("===========================");
        System.out.println("同步读");
        synchronousRead(fileName);
    }

    /**
     * 方法一 用监听器读
     * @param fileName   表格文件路径
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, TableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 方法二：同步去读
     * @param fileName   表格文件路径
     */
    public static void synchronousRead(String fileName) {
        List<TableUserInfo> totalDataList = EasyExcel.read(fileName).head(TableUserInfo.class).sheet().doReadSync();
        for (TableUserInfo tableUserInfo : totalDataList) {
            System.out.println(tableUserInfo);
        }

    }

}
