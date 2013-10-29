package com.amazonaws.wrapper.model;

import com.amazonaws.services.ec2.AmazonEC2;

public interface Ec2Connector {

    public AmazonEC2 getAmazonEC2();

    public String getUserId();

}
