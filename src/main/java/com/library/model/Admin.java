package com.library.model;

import java.time.LocalDate;

/**
 * 管理员：继承自用户，额外拥有权限等级与入职时间。
 */
public class Admin extends User {

    private int permissionLevel; // 权限等级
    private LocalDate hireDate;  // 入职时间

    public Admin() {
    }

    public Admin(int id, String username, String password, String phone, String major,
                 int permissionLevel, LocalDate hireDate) {
        super(id, username, password, phone, major, User.ROLE_ADMIN);
        this.permissionLevel = permissionLevel;
        this.hireDate = hireDate;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    @Override
    public String toString() {
        return super.toString() + " | 权限等级:" + permissionLevel + " | 入职时间:" + hireDate;
    }
}
