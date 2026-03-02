package org.samd.shortlink.admin.common.util;

import org.apache.commons.validator.routines.EmailValidator;
import org.samd.shortlink.admin.common.conversion.errorcode.BaseErrorCode;
import org.samd.shortlink.admin.common.conversion.errorcode.IErrorCode;
import org.samd.shortlink.admin.common.conversion.errorcode.UserErrorCode;
import org.samd.shortlink.admin.common.conversion.exception.ClientException;
import org.samd.shortlink.admin.common.conversion.exception.ServiceException;
import org.samd.shortlink.admin.dto.req.UserRegisterReqDTO;

public final class ParamValidator {
    private ParamValidator() {}

    /**
     * 验证字符串参数非空
     * @param value 字符串参数
     */
    public static void NameNotNull(String value) throws ClientException {
        NameNotNull(value, UserErrorCode.USERNAME_NULL);
    }

    /**
     * 验证字符串参数非空（可传入自定义错误码）
     * @param value 字符串参数
     * @param errorCode 发生错误时抛出的错误码，必须实现 BaseErrorCode
     */
    public static void NameNotNull(String value, IErrorCode errorCode) throws ClientException {
        if (value == null || value.trim().isEmpty()) {
            throw new ClientException(errorCode);
        }
    }

    /**
     * 验证邮箱格式
     * @param email 邮箱
     */
    public static void emailIsValuable(String email) throws ClientException {
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (email == null || !emailValidator.isValid(email)) {
            throw new ClientException(UserErrorCode.EMAIL_INVALID);
        }
    }

    /**
     * 验证手机号格式
     * @param phone 手机号
     */
    public static void phoneIsValuable(String phone) throws ClientException {
        String phoneRegex = "^\\+?[1-9]\\d{1,14}$"; // E.164 国际电话号码格式
        if (phone == null || !phone.matches(phoneRegex)) {
            throw new ClientException(UserErrorCode.PHONE_INVALID);
        }
    }

    /**
     * 验证密码非空
     * @param password 密码
     */
    public static void passwordNotNull(String password) throws ClientException {
        if (password == null || password.trim().isEmpty()) {
            throw new ClientException(UserErrorCode.PASSWORD_NULL);
        }
    }

    /**
     * 验证对象参数非空
     * @param obj 对象参数
     */
    public static void objNonNull(Object obj) throws ClientException {
        if (obj == null) {
            throw new ServiceException(BaseErrorCode.OBJECT_NOT_FOUND);
        }
    }


    public static void checkUserRegisterReqDTOValuable(UserRegisterReqDTO dto) throws ClientException {
        passwordNotNull(dto.getPassword());
        if(dto.getEmail() != null && !dto.getEmail().isEmpty()){emailIsValuable(dto.getEmail());}
        if(dto.getPhone() != null && !dto.getPhone().isEmpty()){phoneIsValuable(dto.getPhone());}
    }
}