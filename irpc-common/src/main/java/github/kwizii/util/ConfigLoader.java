package github.kwizii.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;


@Slf4j
public class ConfigLoader {

    private final Properties properties;

    public ConfigLoader(Properties properties) {
        this.properties = properties;
    }

    // 利用反射将配置文件内容载入类字段
    @SuppressWarnings("unchecked")
    public void loadConfig(AbsSettings config) {
        Class<?> clazz = config.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Map<String, String> keys = config.getKeys();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                String propertyKey = keys.get(fieldName);
                if (propertyKey != null) {
                    String propertyValue = properties.getProperty(propertyKey);
                    if (propertyValue != null) {
                        if (fieldType.isEnum()) {
                            field.set(config, Enum.valueOf((Class<Enum>) field.getType(), propertyValue.toUpperCase()));
                        } else if (fieldType.equals(int.class)) {
                            field.set(config, Integer.parseInt(propertyValue));
                        } else if (fieldType.equals(String.class)) {
                            field.set(config, propertyValue);
                        } else {
                            log.warn("Unsupported property type: [{}]", propertyKey);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log.error("Unsupported property enum type");
            throw new RuntimeException(e);
        }
    }
}
