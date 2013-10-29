package com.amazonaws.wrapper.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.wrapper.model.Ec2Connector;

public class AmazonServices implements Ec2Connector {

    private final static Logger LOGGER = LoggerFactory.getLogger(AmazonServices.class);

    private CredentialsProvider credentialsProvider;

    private AmazonEC2 amazonEC2;

    private AmazonIdentityManagement amazonIdentityManagementClient;

    private AmazonCloudWatch amazonCloudWatch;

    private User user;

    @Override
    public AmazonEC2 getAmazonEC2() {
        if (amazonEC2 == null) {
            amazonEC2 = createAmazonEC2Client();
        }
        return amazonEC2;
    }

    public AmazonIdentityManagement getAmazonIdentityManagement() {
        if (amazonIdentityManagementClient == null) {
            amazonIdentityManagementClient = createAmazonIdentityMamagementClient();
        }
        return amazonIdentityManagementClient;
    }

    public AmazonCloudWatch getAmazonCloudWatchService() {
        if (amazonCloudWatch == null) {
            amazonCloudWatch = createAmazonCloudWatchClient();
        }
        return amazonCloudWatch;
    }

    private AmazonCloudWatchClient createAmazonCloudWatchClient() {
        LOGGER.info("======> Creating Amazon Cloud Watch Client....");
        return new AmazonCloudWatchClient(getAWSCredentials());
    }

    private AmazonIdentityManagementClient createAmazonIdentityMamagementClient() {
        LOGGER.info("======> Creating Amazon Identity Management Client....");
        return new AmazonIdentityManagementClient(getAWSCredentials());
    }

    private AmazonEC2Client createAmazonEC2Client() {
        LOGGER.info("======> Creating Amazon EC2 Client....");
        return new AmazonEC2Client(getAWSCredentials());
    }

    private AWSCredentials getAWSCredentials() {
        return credentialsProvider.getAwsCredentials();
    }

    @Override
    public String getUserId() {
        return getUser().getUserId();
    }

    private User getUser() {
        if (user == null) {
            LOGGER.debug("Fetching User from Amazon");
            user = getAmazonIdentityManagement().getUser().getUser();
        }
        return user;
    }

}
