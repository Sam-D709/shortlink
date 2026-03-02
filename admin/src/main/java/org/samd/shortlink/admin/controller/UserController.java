package org.samd.shortlink.admin.controller;


import lombok.RequiredArgsConstructor;
import org.samd.shortlink.admin.common.conversion.result.Result;
import org.samd.shortlink.admin.common.conversion.result.Results;
import org.samd.shortlink.admin.dto.req.UserLoginReqDTO;
import org.samd.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.samd.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.samd.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.samd.shortlink.admin.dto.resp.UserRespDTO;
import org.samd.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 通过用户名获取用户信息
     * @return 用户信息实体
     */
    @GetMapping("/api/shortlink/admin/user/username")
    public Result<UserRespDTO> getUserByUsername() {
        return Results.success(userService.getUserByUsername());
    }

    /**
     * 验证用户名是否存在
     * @param username 用户名
     * @return false:不存在 true:存在
     */
    @GetMapping("/api/shortlink/admin/user/hasUsername")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 用户注册
     * @param dto 注册实体
     * @return false:注册失败 true:注册成功
     */
    @PostMapping("/api/shortlink/admin/user/register")
    public Result<Boolean> registerUser(@RequestBody UserRegisterReqDTO dto) {
        return Results.success(userService.registerUser(dto));
    }

    /**
     * 更新用户信息
     * @param dto 更新实体
     * @return false:更新失败 true:更新成功
     */
    @PutMapping("/api/shortlink/admin/user/update")
    public Result<Boolean> updateUser(@RequestBody UserUpdateReqDTO dto) {
        return Results.success(userService.updateUser(dto));
    }

    /**
     * 用户登录
     * @param requestParam 登录请求参数
     * @return 登录响应结果
     */
    @PostMapping("/api/shortlink/admin/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam){
        return Results.success(userService.login(requestParam));
    }

    /**
     * 用户登出
     * @return false:登出失败 true:登出成功
     */
    @PostMapping("/api/shortlink/admin/user/logout")
    public Result<Boolean> logout(){
        return Results.success(userService.logout());
    }
}