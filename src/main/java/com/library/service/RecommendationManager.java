package com.library.service;

import com.google.gson.reflect.TypeToken;
import com.library.model.Recommendation;
import com.library.model.User;
import com.library.util.JsonUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书推荐数据管理类：教师推荐图书、管理员审阅推荐，并持久化到 JSON 文件。
 */
public class RecommendationManager {

    private final Path file;

    private List<Recommendation> recommendations = new ArrayList<>();
    private int nextId = 1;

    public RecommendationManager() {
        file = Paths.get(System.getProperty("user.dir"), "data", "recommendations.json");
        recommendations = JsonUtil.loadList(file, new TypeToken<List<Recommendation>>() {}.getType());
        nextId = recommendations.stream().mapToInt(Recommendation::getId).max().orElse(0) + 1;
    }

    /** 新增推荐 */
    public Recommendation add(String title, String author, User user) {
        Recommendation r = new Recommendation(nextId++, title, author, user.getId());
        recommendations.add(r);
        save();
        return r;
    }

    public List<Recommendation> getAll() {
        return new ArrayList<>(recommendations);
    }

    /** 审阅推荐：通过或驳回 */
    public String review(int id, boolean approve) {
        Recommendation r = recommendations.stream()
                .filter(x -> x.getId() == id).findFirst().orElse(null);
        if (r == null) {
            return "审阅失败：推荐记录不存在";
        }
        if (!Recommendation.STATUS_PENDING.equals(r.getStatus())) {
            return "审阅失败：该推荐已审阅过";
        }
        r.setStatus(approve ? Recommendation.STATUS_APPROVED : Recommendation.STATUS_REJECTED);
        r.update();
        save();
        return "审阅完成：《" + r.getTitle() + "》" + r.getStatus();
    }

    public void save() {
        JsonUtil.save(file, recommendations);
    }
}
