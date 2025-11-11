package com.zjg.usercenter.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TeamDeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5560883815617793325L;
    /**
     * 队伍id
     */
    private Long teamId;
}
