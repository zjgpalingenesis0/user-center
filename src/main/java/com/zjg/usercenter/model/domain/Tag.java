package com.zjg.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签
 * @TableName tag
 */
@TableName(value ="tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tagname;

    /**
     * 上传标签的用户id
     */
    private Long userId;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 是否为父标签,0不是，1是
     */
    private Integer isParent;

    /**
     * 标签创建时间
     */
    private Date createTime;

    /**
     * 标签更新时间
     */
    private Date updateTime;

    /**
     * 是否删除,逻辑删除
     */
    @TableLogic
    private Integer isDelete;
}