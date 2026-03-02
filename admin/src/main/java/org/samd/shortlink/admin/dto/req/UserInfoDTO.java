package org.samd.shortlink.admin.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息传输对象
 * 用于在网关和下游服务之间传递用户信息
 */
@Data
public class UserInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}