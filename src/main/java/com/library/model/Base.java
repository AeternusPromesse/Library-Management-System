package com.library.model;

import java.time.LocalDateTime;

/**
 * 实体基类：提供 id、创建时间、更新时间，并在对象被修改时刷新更新时间。
 */
public class Base {

    private int id;                     // 唯一标识
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间

    public Base() {
        this.id = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /** 对象被修改时调用，更新 updateTime 为当前时间。 */
    public void update() {
        this.updateTime = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}