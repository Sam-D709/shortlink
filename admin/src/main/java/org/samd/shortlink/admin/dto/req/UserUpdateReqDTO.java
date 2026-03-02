package org.samd.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class UserUpdateReqDTO {

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
}
