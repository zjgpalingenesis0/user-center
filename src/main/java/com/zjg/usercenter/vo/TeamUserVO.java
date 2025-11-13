package com.zjg.usercenter.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TeamUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 115254570951127142L;

    private Long id;

    /**
     * 队伍名
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 队伍最大人数
     */
    private Integer maxNum;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 状态位，0表示公开队伍，1是私有队伍，2是队伍加密
     */
    private Integer status;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建人用户信息
     */
    UserVO createUser;
    /**
     * 用户是否已加入
     */
    private Boolean hasJoin;
    /**
     * 加入用户数量
     */
    private Integer hasJoinNum;

}
