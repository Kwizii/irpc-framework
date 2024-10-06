package github.kwizii.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IRpcErrorEnum {
    SERVICE_NOT_FOUND("service not found"),
    SERVICE_INVOCATION_FAILURE("service remote invocation failure"),
    MESSAGE_NOT_MATCH("rpc request id does not match the rpc response id");

    private final String message;
}
