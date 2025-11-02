package com.zjg.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
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

    /**
     * 用户标签列表
     */
    private String tag;
}