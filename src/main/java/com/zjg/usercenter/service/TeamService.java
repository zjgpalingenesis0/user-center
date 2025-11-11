package com.zjg.usercenter.service;

import com.zjg.usercenter.dto.TeamQuery;
import com.zjg.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.model.request.TeamDeleteRequest;
import com.zjg.usercenter.model.request.TeamJoinRequest;
import com.zjg.usercenter.model.request.TeamQuitRequest;
import com.zjg.usercenter.model.request.TeamUpdateRequest;
import com.zjg.usercenter.vo.TeamUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-11-06 20:59:47
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team  队伍信息
     * @param loginUser   登录用户
     * @return  队伍id
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍列表
     * @param teamQuery  队伍信息
     * @param isAdmin  是否是管理员
     * @return  队伍及关联用户信息
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin);

    /**
     * 更新用户信息
     * @param teamUpdateRequest   可以更新的信息
     * @param loginUser  当前登录用户
     * @return  是否可以更新
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest   前端输入的想要加入队伍的信息
     * @param loginUser   登录用户信息
     * @return  是否加入队伍
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest  想要退出的队伍信息
     * @param loginUser 登录用户信息
     * @return 是否退出
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param teamDeleteRequest 输入参数信息
     * @param loginUser  登录用户信息
     * @return  是否解散队伍
     */
    boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User loginUser);
}
