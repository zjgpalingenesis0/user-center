package com.zjg.usercenter.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 8296060561159462050L;

    private String userAccount;
    private String userPassword;
}
