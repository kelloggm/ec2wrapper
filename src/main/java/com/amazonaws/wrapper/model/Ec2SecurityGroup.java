package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

public class Ec2SecurityGroup extends Ec2Resource<SecurityGroup, Ec2SecurityGroup> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ec2SecurityGroup.class);

    public final static String CREATE_PARAM_VPC_ID = "vpc_id";
    public final static String CREATE_PARAM_DESCRIPTION = "description";

    public Ec2SecurityGroup(SecurityGroup group) {
        super(group);
    }

    public Ec2SecurityGroup(String id) throws ResourceDoesNotExistException {
        super(id);
        refresh();
    }

    public Ec2SecurityGroup() {
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String groupName, Properties props, AdapterSettings settings) {
        CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
        //TODO handle whitespaces for group name for VPC
        request.withGroupName(groupName);
        request.withDescription(props.getProperty(CREATE_PARAM_DESCRIPTION));
        request.withVpcId(props.getProperty(CREATE_PARAM_VPC_ID));
        CreateSecurityGroupResult result = ec2.createSecurityGroup(request);
        setId(result.getGroupId());
        try {
            refresh();
        } catch (ResourceDoesNotExistException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doDeleteRequest() {
        getEc2().deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(getId()));
    }

    @Override
    protected String getResourceId() {
        return getResource().getGroupId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeSecurityGroupsRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2SecurityGroup> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2SecurityGroup> securityGroups = new ArrayList<Ec2SecurityGroup>();

        DescribeSecurityGroupsResult result = amazonEC2.describeSecurityGroups(((DescribeSecurityGroupsRequest) request));
        for (SecurityGroup group : result.getSecurityGroups()) {
            securityGroups.add(new Ec2SecurityGroup(group));
        }
        return securityGroups;
    }

    @Override
    protected SecurityGroup processDescribe() throws ResourceDoesNotExistException {
        try {
            List<SecurityGroup> securityGroups = getEc2().describeSecurityGroups(new DescribeSecurityGroupsRequest().withGroupIds(getId())).getSecurityGroups();
            return securityGroups.get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public static List<Ec2SecurityGroup> getAllSecurityGroups() {
        return new Ec2SecurityGroup().getAll();
    }

    public List<IpPermission> getIpPermissions() {
        return getResource().getIpPermissions();
    }

    public String getVpcId() {
        return getResource().getVpcId();
    }
}
