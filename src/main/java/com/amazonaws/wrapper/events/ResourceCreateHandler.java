package com.amazonaws.wrapper.events;

import com.amazonaws.wrapper.model.Ec2Resource;

public interface ResourceCreateHandler<T extends Ec2Resource<?, ?>> {

    boolean beforeCreate(T resource, boolean sendDataToAmazon);

    void afterCreate(T resource);

    void afterThrow(T resource, Exception exception);

}
