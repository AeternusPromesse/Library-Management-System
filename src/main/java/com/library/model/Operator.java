package com.library.model;

/**
 * 操作员：继承自用户，额外拥有负责区域与职务。
 */
public class Operator extends User {

    private String area;     // 负责区域
    private String position; // 职务

    public Operator() {
    }

    public Operator(int id, String username, String password, String phone, String major,
                    String area, String position) {
        super(id, username, password, phone, major, User.ROLE_OPERATOR);
        this.area = area;
        this.position = position;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return super.toString() + " | 负责区域:" + area + " | 职务:" + position;
    }
}
