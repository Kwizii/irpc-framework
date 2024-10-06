package github.kwizii.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceRegistryEnum {
    ZK("zk");

    private final String name;
}
