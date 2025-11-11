package com.zjg.usercenter.model.request;

import com.zjg.usercenter.common.BaseResponse;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TeamJoinRequest implements Serializable{
    @Serial
    private static final long serialVersionUID = 91598487102098846L;
    /**
     * 队伍id
     */
    private Long teamId;


    /**
     * 密码
     */
    private String password;

}
