package com.zjg.usercenter.mapper;

import com.zjg.usercenter.dto.TeamQuery;
import com.zjg.usercenter.model.domain.Team;
import com.zjg.usercenter.vo.TeamUserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2025-11-06 20:59:47
* @Entity com.zjg.usercenter.model.domain.Team
*/
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * SQL关联查询队伍和创建人信息
     * @param teamQuery 查询条件
     * @return 队伍用户信息列表
     */


}




