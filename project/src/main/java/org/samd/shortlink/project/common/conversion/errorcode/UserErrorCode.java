package org.samd.shortlink.project.common.conversion.errorcode;

public enum UserErrorCode implements IErrorCode {
    USERNAME_NULL("A100", "用户名不能为空"),

    EMAIL_INVALID("A101", "邮箱格式不正确"),

    PHONE_INVALID("A102", "手机号格式不正确"),

    PASSWORD_NULL("A103", "密码不能为空"),

    USER_EXIST("B101", "用户已存在"),

    USER_SAVE_ERROR("B102", "用户保存失败"),;

    private final String code;

    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
