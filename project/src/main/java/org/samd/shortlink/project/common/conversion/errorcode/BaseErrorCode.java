package org.samd.shortlink.project.common.conversion.errorcode;

public enum BaseErrorCode implements IErrorCode {
    SERVICE_ERROR("500", "服务器内部错误，无法完成请求"),

    OBJECT_NOT_FOUND("404", "请求的资源不存在"),;

    private final String code;

    private final String message;

    BaseErrorCode(String code, String message) {
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
