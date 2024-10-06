package github.kwizii.loadbalance.impl;

import github.kwizii.loadbalance.AbsLoadBalancer;
import github.kwizii.remoting.dto.IRpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbsLoadBalancer {

    private final Random random;

    public RandomLoadBalancer() {
        this.random = new Random(System.currentTimeMillis());
    }

    @Override
    protected String doSelect(List<String> serviceUrlList, IRpcRequest rpcRequest) {
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
