package com.zjg.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import lombok.Data;

/**
 * 用户中心用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户nicheng
     */
    private String username;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    //@JsonIgnore
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态位，0表示正常，1是封号
     */
    private Integer userStatus;

    /**
     * 数据创建时间
     */
    private Date createTime;

    /**
     * 数据更新时间
     */
    private Date updateTime;

    /**
     * 是否删除,逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户权限  0是用户   1是管理员
     */
    private Integer userRole;

    /**
     * 编号
     */
    private String centerCode;
}