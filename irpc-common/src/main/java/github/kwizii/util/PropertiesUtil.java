package github.kwizii.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public final class PropertiesUtil {

    public static Properties get(String fileName) {
        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            log.error("Properties file not found", e);
        }
        return properties;
    }
}
