package github.kwizii.exception;


import github.kwizii.enums.IRpcErrorEnum;

public class IRpcException extends RuntimeException {
    public IRpcException(IRpcErrorEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public IRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public IRpcException(IRpcErrorEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
