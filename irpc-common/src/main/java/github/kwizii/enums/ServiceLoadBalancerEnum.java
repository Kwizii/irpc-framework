package github.kwizii.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceLoadBalancerEnum {
    RANDOM("random"),
    HASH("hash");

    private final String name;
}
