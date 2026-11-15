package com.library.model;

/**
 * 教师：继承自用户，额外拥有所属学院与职称。
 */
public class Teacher extends User {

    private String college; // 所属学院
    private String title;   // 职称

    public Teacher() {
    }

    public Teacher(int id, String username, String password, String phone, String major,
                   String college, String title) {
        super(id, username, password, phone, major, User.ROLE_TEACHER);
        this.college = college;
        this.title = title;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return super.toString() + " | 所属学院:" + college + " | 职称:" + title;
    }
}
