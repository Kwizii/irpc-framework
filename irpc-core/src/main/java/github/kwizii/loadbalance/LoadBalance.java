package github.kwizii.loadbalance;


import github.kwizii.extension.SPI;
import github.kwizii.remoting.dto.IRpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {

    String selectServiceAddress(List<String> serviceUrlList, IRpcRequest rpcRequest);
}
