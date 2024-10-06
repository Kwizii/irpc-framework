package github.kwizii.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;

    private String interfaceName;

    private String methodName;

    private Class<?>[] paramTypes;

    private Object[] paramValues;

    private String version;

    private String group;

    public String getRpcServiceName() {
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceName);
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
}
