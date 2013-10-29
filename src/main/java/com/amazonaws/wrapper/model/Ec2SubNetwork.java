package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateSubnetResult;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

public class Ec2SubNetwork extends Ec2Resource<Subnet, Ec2SubNetwork> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ec2SubNetwork.class);

    public final static String CREATE_PARAM_VPC_ID = "vpc_id";
    public final static String CREATE_PARAM_DESCRIPTION = "description";

    public Ec2SubNetwork(Subnet subnetwork) {
        super(subnetwork);
    }

    public Ec2SubNetwork(String id) throws ResourceDoesNotExistException {
        super(id);
        refresh();
    }

    public Ec2SubNetwork() {
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String vpcId, Properties props, AdapterSettings settings) {
        CreateSubnetRequest request = new CreateSubnetRequest();
        request.withVpcId(vpcId);
        CreateSubnetResult result = ec2.createSubnet(request);
        setId(result.getSubnet().getSubnetId());
        try {
            refresh();
        } catch (ResourceDoesNotExistException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doDeleteRequest() {
        getEc2().deleteVpc(new DeleteVpcRequest().withVpcId(getId()));
    }

    @Override
    protected String getResourceId() {
        return getResource().getSubnetId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeSubnetsRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2SubNetwork> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2SubNetwork> subNets = new ArrayList<Ec2SubNetwork>();

        DescribeSubnetsResult result = amazonEC2.describeSubnets(((DescribeSubnetsRequest) request));
        for (Subnet subnet : result.getSubnets()) {
            subNets.add(new Ec2SubNetwork(subnet));
        }
        return subNets;
    }

    @Override
    protected Subnet processDescribe() throws ResourceDoesNotExistException {
        try {
            List<Subnet> subnetworks = getEc2().describeSubnets(new DescribeSubnetsRequest().withSubnetIds(getId())).getSubnets();
            return subnetworks.get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public static List<Ec2SubNetwork> getAllSubnets() {
        return new Ec2SubNetwork().getAll();
    }

    public String getVpcId() {
        return getResource().getVpcId();
    }
}
