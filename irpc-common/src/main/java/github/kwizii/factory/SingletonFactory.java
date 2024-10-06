package github.kwizii.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return c.cast(OBJECT_MAP.get(key));
        } else {
            synchronized (c) {
                if (!OBJECT_MAP.containsKey(key)) {
                    try {
                        T instance = c.getDeclaredConstructor().newInstance();
                        OBJECT_MAP.put(key, instance);
                        return instance;
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return c.cast(OBJECT_MAP.get(key));
                }
            }
        }
    }
}
