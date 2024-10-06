package github.kwizii.loadbalance;

import github.kwizii.remoting.dto.IRpcRequest;

import java.util.List;

public abstract class AbsLoadBalancer implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, IRpcRequest rpcRequest) {
        if (serviceUrlList == null || serviceUrlList.isEmpty()) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceUrlList, IRpcRequest rpcRequest);
}
