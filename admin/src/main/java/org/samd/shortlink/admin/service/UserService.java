package org.samd.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.samd.shortlink.admin.dao.entity.UserDO;
import org.samd.shortlink.admin.dto.req.UserLoginReqDTO;
import org.samd.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.samd.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.samd.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.samd.shortlink.admin.dto.resp.UserRespDTO;


/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {
    /**
     * 通过用户名获取用户信息
     * @param username 用户名
     * @return 用户信息实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 验证用户名是否存在
     * @param username 用户名
     * @return false:不存在 true:存在
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param requestParam 注册实体
     * @return false:注册失败 true:注册成功
     */
    Boolean registerUser(UserRegisterReqDTO requestParam);

    /**
     * 更新用户信息
     * @param requestParam 更新实体
     * @return false:更新失败 true:更新成功
     */
    Boolean updateUser(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 登录实体
     * @return 登录响应实体,包含token等信息
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查登录状态
     * @param username 用户名
     * @param token 登录token
     * @return false:未登录 true:已登录
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户登出
     * @param username 用户名
     * @param token 登录token
     * @return false:登出失败 true:登出成功
     */
    Boolean logout(String username, String token);
}
