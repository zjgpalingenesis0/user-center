package com.zjg.usercenter.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前第几页
     */
    protected int pageNum;
}
