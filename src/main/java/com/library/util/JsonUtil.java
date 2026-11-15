package com.library.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.library.model.Admin;
import com.library.model.Operator;
import com.library.model.Student;
import com.library.model.Teacher;
import com.library.model.User;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 文件读写工具，使用 Gson 完成对象与 JSON 之间的转换。
 */
public final class JsonUtil {

    /** 反序列化具体子类时使用的普通 Gson（无 User 多态适配器，避免递归） */
    private static final Gson PLAIN = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /** 对外使用的 Gson：支持 User 子类（Admin/Student/Teacher/Operator）的多态反序列化 */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeHierarchyAdapter(User.class, new UserDeserializer())
            .create();

    private JsonUtil() {
    }

    /**
     * 从文件读取 JSON 数组并解析为对象列表，文件不存在时返回空列表。
     */
    public static <T> List<T> loadList(Path file, Type type) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<T> list = GSON.fromJson(reader, type);
            return list == null ? new ArrayList<>() : list;
        } catch (IOException e) {
            System.err.println("读取数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 将对象序列化为 JSON 写入文件。
     */
    public static void save(Path file, Object obj) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
        } catch (IOException ignored) {
        }
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(obj, writer);
        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    /**
     * LocalDate 与 JSON 字符串（yyyy-MM-dd）之间的转换适配器。
     */
    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString());
        }
    }

    /**
     * LocalDateTime 与 JSON 字符串（ISO 格式）之间的转换适配器。
     */
    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString());
        }
    }

    /**
     * User 多态反序列化：根据 JSON 中的 role 字段选择具体的子类。
     */
    private static class UserDeserializer implements JsonDeserializer<User> {

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            String role = obj.has("role") && !obj.get("role").isJsonNull()
                    ? obj.get("role").getAsString() : "";
            Class<? extends User> clazz = switch (role) {
                case User.ROLE_ADMIN -> Admin.class;
                case User.ROLE_STUDENT -> Student.class;
                case User.ROLE_TEACHER -> Teacher.class;
                case User.ROLE_OPERATOR -> Operator.class;
                default -> User.class;
            };
            return PLAIN.fromJson(json, clazz);
        }
    }
}
