package github.kwizii.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IRpcServiceConfig {

    private String version = "";

    private String group = "";

    private Object service;

    public String getRpcServiceName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getServiceName());
        if (StringUtils.hasText(group)) {
            sb.append(".");
            sb.append(group);
        }
        if (StringUtils.hasText(version)) {
            sb.append(".");
            sb.append(version);
        }
        return sb.toString();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
