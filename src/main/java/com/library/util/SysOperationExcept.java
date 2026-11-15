package com.library.util;

/**
 * 图书馆系统操作异常类：注册、登录等操作失败时抛出，携带提示信息。
 */
public class SysOperationExcept extends Exception {

    public SysOperationExcept() {
        super();
    }

    public SysOperationExcept(String message) {
        super(message);
    }
}
