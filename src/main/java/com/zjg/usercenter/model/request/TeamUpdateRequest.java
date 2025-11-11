package com.zjg.usercenter.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TeamUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1757900172318733174L;
    /**
     * 队伍id
     */
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
     * 小队最大人数
     */
    private int maxNum;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 状态位，0表示公开队伍，1是私有队伍，2是队伍加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

    /**
     * 过期时间
     */
    private Date expireTime;
}
