package com.library.service;

import com.google.gson.reflect.TypeToken;
import com.library.util.JsonUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志管理器：记录登录及各类非查询操作日志（含操作人信息），并持久化到 JSON 文件。
 */
public class LogManager {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;
    private List<String> logs = new ArrayList<>();

    public LogManager() {
        logFile = Paths.get(System.getProperty("user.dir"), "data", "logs.json");
        logs = JsonUtil.loadList(logFile, new TypeToken<List<String>>() {}.getType());
    }

    /** 写入一条日志：时间 + 内容（内容中应包含当前操作人信息） */
    public void log(String content) {
        String entry = LocalDateTime.now().format(FORMATTER) + " " + content;
        logs.add(entry);
        JsonUtil.save(logFile, logs);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }
}
