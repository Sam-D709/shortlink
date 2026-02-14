package org.samd.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginRespDTO {
    /**
     * 登录token
     */
    private String token;
}
