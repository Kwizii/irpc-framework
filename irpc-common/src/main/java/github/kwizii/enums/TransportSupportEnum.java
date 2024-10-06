package github.kwizii.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransportSupportEnum {
    NETTY("netty");

    private final String name;
}
