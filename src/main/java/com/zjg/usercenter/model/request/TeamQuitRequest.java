package com.zjg.usercenter.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7467472524465671391L;
    /**
     * 队伍id
     */
    private Long teamId;

}
