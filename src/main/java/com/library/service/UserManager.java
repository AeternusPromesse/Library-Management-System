package com.library.service;

import com.google.gson.reflect.TypeToken;
import com.library.model.Admin;
import com.library.model.Operator;
import com.library.model.Student;
import com.library.model.Teacher;
import com.library.model.User;
import com.library.util.JsonUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息数据管理类：用户的增删改查、注册验证、登录验证，并持久化到 JSON 文件。
 */
public class UserManager {

    private final LogManager logManager;
    private final Path usersFile;

    private List<User> users = new ArrayList<>();
    private int nextUserId = 1;
    private User currentUser; // 当前登录用户

    public UserManager(LogManager logManager) {
        this.logManager = logManager;
        usersFile = Paths.get(System.getProperty("user.dir"), "data", "users.json");
        users = JsonUtil.loadList(usersFile, new TypeToken<List<User>>() {}.getType());
        if (users.isEmpty()) {
            users = initUsers();
            save();
        }
        nextUserId = users.stream().mapToInt(User::getId).max().orElse(0) + 1;
    }

    /**
     * 新增用户（注册）：进行用户名、手机号重复验证，返回 {code, message}。
     * code=200 成功；code=500 失败。
     */
    public Map<String, Object> addUser(User user) {
        Map<String, Object> result = new HashMap<>();
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            result.put("code", 500);
            result.put("message", "注册失败：用户名已存在");
            return result;
        }
        if (user.getPhone() != null
                && users.stream().anyMatch(u -> user.getPhone().equals(u.getPhone()))) {
            result.put("code", 500);
            result.put("message", "注册失败：手机号已被注册");
            return result;
        }
        user.setId(nextUserId++);
        users.add(user);
        save();
        result.put("code", 200);
        result.put("message", "注册成功");
        return result;
    }

    /**
     * 登录验证：用户名和密码匹配则返回 {code:200, message:"登录成功", user:用户对象}，
     * 同时设置登录时间与当前登录用户；失败返回 code=500。
     */
    public Map<String, Object> loginCheck(String name, String password) {
        Map<String, Object> result = new HashMap<>();
        User user = users.stream()
                .filter(u -> u.getUsername().equals(name) && u.getPassword().equals(password))
                .findFirst().orElse(null);
        if (user == null) {
            result.put("code", 500);
            result.put("message", "登录失败：用户名或密码错误");
            return result;
        }
        user.setLoginTime(LocalDateTime.now());
        user.update();
        currentUser = user;
        save();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("user", user);
        return result;
    }

    /** 退出登录：清除当前登录用户 */
    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User findUser(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public User findUserByName(String name) {
        return users.stream().filter(u -> u.getUsername().equals(name)).findFirst().orElse(null);
    }

    /**
     * 用户信息查询：关键字模糊匹配用户名与手机号；关键字为空返回全部用户。
     */
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(users);
        }
        String k = keyword.toLowerCase();
        return users.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(k)
                        || (u.getPhone() != null && u.getPhone().toLowerCase().contains(k)))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public void save() {
        JsonUtil.save(usersFile, users);
    }

    /**
     * 初始化用户：一个管理员、一个操作人员、一个教师、2个学生。
     */
    private List<User> initUsers() {
        List<User> list = new ArrayList<>();
        list.add(new Admin(1, "admin", "admin123", "13800000001", "图书馆学",
                1, LocalDate.of(2020, 9, 1)));
        list.add(new Operator(2, "operator", "operator123", "13800000002", "图书馆学",
                "一楼借阅区", "图书管理员"));
        list.add(new Teacher(3, "teacher", "teacher123", "13800000003", "哲学",
                "文学院", "副教授"));
        list.add(new Student(4, "student1", "student123", "13800000004", "计算机科学与技术",
                "大二", "信息学院"));
        list.add(new Student(5, "student2", "student123", "13800000005", "软件工程",
                "研一", "软件学院"));
        return list;
    }
}
