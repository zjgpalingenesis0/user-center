package com.zjg.usercenter.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.zjg.usercenter.model.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * ID
     */
    private Long id;

    /**
     * ID列表   支持根据id列表来查询
     */
    private List<Long> idList;

    /**
     * 队伍名
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 搜索关键词，同时对名称和描述搜索
     */
    private String searchText;

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
}
