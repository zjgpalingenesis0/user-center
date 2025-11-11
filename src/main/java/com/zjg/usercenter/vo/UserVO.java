package com.zjg.usercenter.vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5651582368503727326L;

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
