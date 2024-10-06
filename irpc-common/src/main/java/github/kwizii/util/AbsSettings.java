package github.kwizii.util;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Getter
@Setter
public abstract class AbsSettings {

    protected final Map<String, String> keys = new HashMap<>();

    private Properties properties;

    public void load(Properties properties) {
        this.properties = properties;
        ConfigLoader configLoader = new ConfigLoader(properties);
        configLoader.loadConfig(this);
    }
}
