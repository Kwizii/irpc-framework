package github.kwizii.remoting.dto;

import github.kwizii.enums.IRpcResponseCodeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class IRpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;

    private Integer code;

    private String message;

    private T data;

    public IRpcResponse(String requestId, IRpcResponseCodeEnum codeEnum, T data) {
        this.requestId = requestId;
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        this.data = data;
    }

    public static <T> IRpcResponse<T> success(String requestId) {
        return IRpcResponse.success(requestId, null);
    }

    public static <T> IRpcResponse<T> success(String requestId, T data) {
        return new IRpcResponse<>(requestId, IRpcResponseCodeEnum.SUCCESS, data);
    }

    public static <T> IRpcResponse<T> error(String requestId) {
        return IRpcResponse.error(requestId, IRpcResponseCodeEnum.ERROR);
    }

    public static <T> IRpcResponse<T> error(String requestId, IRpcResponseCodeEnum codeEnum) {
        return new IRpcResponse<>(requestId, codeEnum, null);
    }

    public boolean isSuccess() {
        return IRpcResponseCodeEnum.SUCCESS.getCode().equals(this.code);
    }
}
