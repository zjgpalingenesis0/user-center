package com.zjg.usercenter.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 2399966111984196677L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
