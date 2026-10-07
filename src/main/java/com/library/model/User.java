package com.library.model;

/**
 * 用户实体类。
 */
public class User {

    /** 用户角色 */
    public enum Role {
        ADMIN("管理员"),
        BORROWER("借阅用户");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;          // 用户ID
    private String username; // 用户名
    private String password; // 密码
    private Role role;       // 角色
    private String major;    // 专业（用于按专业统计）

    public User() {
    }

    public User(int id, String username, String password, Role role, String major) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.major = major;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    @Override
    public String toString() {
        return String.format("ID:%d | 用户名:%s | 角色:%s | 专业:%s",
                id, username, role.getLabel(), (major == null || major.isEmpty()) ? "-" : major);
    }
}