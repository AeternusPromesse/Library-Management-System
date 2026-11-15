package com.library.model;

import java.time.LocalDateTime;

/**
 * 用户实体类：管理员 Admin、学生 Student、教师 Teacher、操作员 Operator 的父类。
 */
public class User extends Base {

    /** 角色常量 */
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_OPERATOR = "operator";

    private String username;         // 用户名（姓名）
    private String password;          // 密码
    private String phone;            // 手机号
    private String major;            // 专业
    private String role;             // 角色：admin / student / teacher / operator
    private LocalDateTime loginTime; // 最近登录时间

    public User() {
    }

    public User(int id, String username, String password, String phone, String major, String role) {
        setId(id);
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.major = major;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    @Override
    public String toString() {
        return String.format("ID:%d | 用户名:%s | 角色:%s | 专业:%s | 手机:%s",
                getId(), username, role,
                (major == null || major.isEmpty()) ? "-" : major,
                (phone == null || phone.isEmpty()) ? "-" : phone);
    }
}
