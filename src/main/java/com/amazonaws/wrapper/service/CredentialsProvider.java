package com.amazonaws.wrapper.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public abstract class CredentialsProvider {

    private AWSCredentials awsCredentials = null;

    public abstract String getAWSKey();

    public abstract String getAWSSecretKey();

    public AWSCredentials getAwsCredentials() {
        if (awsCredentials == null) {
            awsCredentials = new BasicAWSCredentials(getAWSKey(), getAWSSecretKey());
        }
        return awsCredentials;
    }

}
