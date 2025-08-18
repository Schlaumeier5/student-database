package de.igslandstuhl.database.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gson.Gson;

import de.igslandstuhl.database.api.APIObject;

public class JSONUtils {
    public static class JSONBuilder {
        private final Map<String, String> map = new HashMap<>();
        private final Gson gson = new Gson();

        public JSONBuilder addProperty(String property, Object o) {
            map.put(property, gson.toJson(o));
            return this;
        }
        public JSONBuilder addProperty(String property, Number n) {
            map.put(property, n.toString());
            return this;
        }
        public JSONBuilder addProperty(String property, String s) {
            map.put(property, '"' + s + '"');
            return this;
        }
        public JSONBuilder addProperty(String property, APIObject o) {
            map.put(property, o.toJSON());
            return this;
        }
        public JSONBuilder addProperty(String property, List<? extends APIObject> l) {
            map.put(property, toJSON(l));
            return this;
        }
        public JSONBuilder addProperty(String property, JSONBuilder builder) {
            map.put(property, builder.toString());
            return this;
        }
        @Override
        public String toString() {
            return "{" + map.entrySet().stream()
                .map((e) -> '"' + e.getKey() + "\": " + e.getValue())
                .reduce("", (s1, s2) -> s1 + ",\n    " + s2)
                .replaceFirst(",", "") + "\n}";
        }
    }
    public static String toJSON(List<? extends APIObject> list) {
        return "[" + list.stream()
            .map(APIObject::toJSON)
            .reduce("", (s1, s2) -> s1 + ",\n    " + s2)
            .replaceFirst(",", "") + "\n]";
    }
    public static <T> String toJSON(List<T> list, BiConsumer<T, JSONBuilder> jsonHandler) {
        return "[" + list.stream()
            .map((t) -> {
                JSONBuilder builder = new JSONBuilder();
                jsonHandler.accept(t, builder);
                return builder.toString();
            })
            .reduce("", (s1, s2) -> s1 + ",\n    " + s2)
            .replaceFirst(",", "") + "\n]";
    }
    public static <T> String toJON(List<T> list, Function<T, String> jsonHandler) {
        return "[" + list.stream()
            .map(jsonHandler)
            .reduce("", (s1, s2) -> s1 + ",\n    " + s2)
            .replaceFirst(",", "") + "\n]";
    }
    public static String toJSON(int[] arr) {
        return Arrays.toString(arr);
    }
}
