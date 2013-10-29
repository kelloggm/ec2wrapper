package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

public class Ec2Vpc extends Ec2Resource<Vpc, Ec2Vpc> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ec2Vpc.class);

    public Ec2Vpc(Vpc vpc) {
        super(vpc);
    }

    public Ec2Vpc(String id) throws ResourceDoesNotExistException {
        super(id);
        refresh();
    }

    public Ec2Vpc() {
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String groupName, Properties props, AdapterSettings settings) {
        CreateVpcRequest request = new CreateVpcRequest();
        CreateVpcResult result = ec2.createVpc(request);
        setId(result.getVpc().getVpcId());
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
        return getResource().getVpcId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeVpcsRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2Vpc> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2Vpc> vpcs = new ArrayList<Ec2Vpc>();

        DescribeVpcsResult result = amazonEC2.describeVpcs(((DescribeVpcsRequest) request));
        for (Vpc vpc : result.getVpcs()) {
            vpcs.add(new Ec2Vpc(vpc));
        }
        return vpcs;
    }

    @Override
    protected Vpc processDescribe() throws ResourceDoesNotExistException {
        try {
            List<Vpc> vpcs = getEc2().describeVpcs(new DescribeVpcsRequest().withVpcIds(getId())).getVpcs();
            return vpcs.get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public static List<Ec2Vpc> getAllVpcs() {
        return new Ec2Vpc().getAll();
    }
}
