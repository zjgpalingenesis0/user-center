package com.zjg.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjg.usercenter.common.BaseResponse;
import com.zjg.usercenter.common.ErrorCode;
import com.zjg.usercenter.exception.BusinessException;
import com.zjg.usercenter.model.domain.User;
import com.zjg.usercenter.model.request.UserLoginRequest;
import com.zjg.usercenter.model.request.UserRegisterRequest;
import com.zjg.usercenter.service.UserService;
import com.zjg.usercenter.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zjg.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.zjg.usercenter.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String centerCode = userRegisterRequest.getCenterCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, centerCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入信息为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, centerCode);
        return ResultUtils.success(result, "用户已成功注册");
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入信息为空");
        }
        User result = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(result, "用户已成功登录");
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求为空");
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result, "用户已成功注销");
    }

    /**
     * 获取当前用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "目前未登录");
        }
        long userId = currentUser.getId();
        // TODO 检验用户是否合法
        User user = userService.getById(userId);
        User safeUser = userService.getSafetyUser(user);
        return ResultUtils.success(safeUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request) {
        //管理员权限校验
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_AUTH, "非管理员权限");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());

        return ResultUtils.success(result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入标签为空");
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        if(!CollectionUtils.isEmpty(userList)){
            return ResultUtils.success(userList, "成功根据标签找到用户");
        }
        else {
            return ResultUtils.success(userList, "没找到有这种标签的用户");
        }

    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "需要修改的用户为空");
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result, "更新成功");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,  HttpServletRequest request) {
        //管理员权限校验
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_AUTH, "非管理员权限");
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入id不符合规范");
        }
        Boolean result = userService.removeById(id);
        return ResultUtils.success(result, "用户已成功删除");
    }


//    public Boolean isAdmin(HttpServletRequest request) {
//        //管理员权限校验
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User user = (User) userObj;
//        return user != null && user.getUserRole() == ADMIN_ROLE;
//
//    }
}
