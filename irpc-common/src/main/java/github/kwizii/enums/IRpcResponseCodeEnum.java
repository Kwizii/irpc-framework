package github.kwizii.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum IRpcResponseCodeEnum {
    SUCCESS(200, ""),
    ERROR(500, "");

    private final Integer code;
    private final String message;
}
