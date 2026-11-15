package com.library.model;

/**
 * 学生：继承自用户，额外拥有年级与所属学院。
 */
public class Student extends User {

    private String grade;   // 年级
    private String college; // 所属学院

    public Student() {
    }

    public Student(int id, String username, String password, String phone, String major,
                   String grade, String college) {
        super(id, username, password, phone, major, User.ROLE_STUDENT);
        this.grade = grade;
        this.college = college;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    @Override
    public String toString() {
        return super.toString() + " | 年级:" + grade + " | 所属学院:" + college;
    }
}
