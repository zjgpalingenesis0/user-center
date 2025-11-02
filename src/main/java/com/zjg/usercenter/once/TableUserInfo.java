package com.zjg.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TableUserInfo {
    /**
     * 中心编号
     */
    @ExcelProperty("中心编号")
    private String centerCode;
    /**
     * 用户名
     */
    @ExcelProperty("用户昵称")
    private String username;

}