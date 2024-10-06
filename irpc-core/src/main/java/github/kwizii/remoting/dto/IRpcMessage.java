package github.kwizii.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IRpcMessage {
    private byte messageType;

    private byte codec;

    private byte compress;

    private int requestId;

    private Object data;
}
