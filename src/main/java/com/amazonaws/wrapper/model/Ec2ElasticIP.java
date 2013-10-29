package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

/**
 * Represents amazon elastic IP
 */
public class Ec2ElasticIP extends Ec2Resource<Address, Ec2ElasticIP> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ec2ElasticIP.class);

    public final static String ALLOCATE_IN_VPC = "vpc";

    /**
     * Constructor
     * 
     * @param ip
     *            - IP (only for getting info)
     * @throws ResourceDoesNotExistException
     */
    public Ec2ElasticIP(String ip) throws ResourceDoesNotExistException {
        super(ip);
        refresh();
    }

    /**
     * 
     * @param address
     */
    public Ec2ElasticIP(Address address) {
        super(address);
    }

    public Ec2ElasticIP() {
    }

    // allocation id
    private String allocationId;

    @Override
    protected void doDeleteRequest() {
        // we cant release address if it allocate to any instance
        if (getResource().getInstanceId() != null) {
            getEc2().releaseAddress(new ReleaseAddressRequest().withAllocationId(allocationId));
        } else {
            throw new RuntimeException("Address allocated to instance");
        }
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String description, Properties props, AdapterSettings settings) {
        AllocateAddressRequest request = new AllocateAddressRequest();
        if (props.containsKey(ALLOCATE_IN_VPC)) {
            request.withDomain(ALLOCATE_IN_VPC);
        }
        AllocateAddressResult result = ec2.allocateAddress(request);
        this.allocationId = result.getAllocationId();
        Address address = new Address();
        address.setPublicIp(result.getPublicIp());
        doUpdate(address);

    }

    @Override
    protected String getResourceId() {
        return getResource().getPublicIp();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeAddressesRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2ElasticIP> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2ElasticIP> acmElasticIPs = new ArrayList<Ec2ElasticIP>();
        DescribeAddressesResult result = amazonEC2.describeAddresses(((DescribeAddressesRequest) request));
        for (Address address : result.getAddresses()) {
            acmElasticIPs.add(new Ec2ElasticIP(address));
        }
        return acmElasticIPs;
    }

    @Override
    protected Address processDescribe() throws ResourceDoesNotExistException {
        try {
            DescribeAddressesResult result = getEc2().describeAddresses(new DescribeAddressesRequest().withPublicIps(getId()));
            return result.getAddresses().get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public boolean isAssociated() {
        String instanceId = getResource().getInstanceId();
        return !(instanceId == null || instanceId.equals(""));
    }

    public String getIp() {
        return getResource().getPublicIp();
    }

    public void associate(Ec2Instance acmInstance) {
        if (getResource().getDomain().equals("vpc")) {
            getEc2().associateAddress(new AssociateAddressRequest().withInstanceId(acmInstance.getId()).withAllocationId(getResource().getAllocationId()));
        } else {
            getEc2().associateAddress(new AssociateAddressRequest().withInstanceId(acmInstance.getId()).withPublicIp(getIp()));
        }
    }

    /**
     * Associate this address to given machine, if this address is already in use this method dissociates it, and associate to new instance
     * 
     * @param acmInstance
     *            Instance to associate IP address to
     */
    public void forceAssociate(Ec2Instance acmInstance) {
        if (isAssociated()) {
            disassociateAddress();
        }
        associate(acmInstance);
        try {
            //need to update DNS address
            acmInstance.refresh();
        } catch (ResourceDoesNotExistException e) {
            LOGGER.error("Unable to refresh Instance after attaching Elastic IP. Nested exception: ", e);
        }
    }

    /**
     * The DisassociateAddress operation disassociates the specified elastic IP address from the instance to which it is assigned. This is an idempotent
     * operation. If you enter it more than once, Amazon EC2 does not return an error.
     * 
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while attempting to make the request or handle the response. For example if a
     *             network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating either a problem with the data in the request, or a server side issue.
     */
    public void disassociateAddress() {
        if (getResource().getDomain().equals("vpc")) {
            getEc2().disassociateAddress(new DisassociateAddressRequest().withAssociationId(getResource().getAssociationId()));
        } else {
            getEc2().disassociateAddress(new DisassociateAddressRequest().withPublicIp(getResource().getPublicIp()));
        }
    }

    public static List<Ec2ElasticIP> getAllAddresses() {
        return new Ec2ElasticIP().getAll();
    }

    public static List<Ec2ElasticIP> getAllVPCAddresses() {
        return new Ec2ElasticIP().getFiltered("domain", "vpc");
    }

    public static List<Ec2ElasticIP> getAllEC2Addresses() {
        return new Ec2ElasticIP().getFiltered("domain", "standard");
    }

    public static List<Ec2ElasticIP> getNotAssociatedVPC() {
        List<Ec2ElasticIP> notAssociated = new ArrayList<Ec2ElasticIP>();
        List<Ec2ElasticIP> acmElasticIPs = getAllVPCAddresses();
        for (Ec2ElasticIP acmElasticIP : acmElasticIPs) {
            if (!acmElasticIP.isAssociated()) {
                notAssociated.add(acmElasticIP);
            }
        }
        return notAssociated;
    }

    public static List<Ec2ElasticIP> getNotAssociatedEC2() {
        List<Ec2ElasticIP> notAssociated = new ArrayList<Ec2ElasticIP>();
        List<Ec2ElasticIP> acmElasticIPs = getAllEC2Addresses();
        for (Ec2ElasticIP acmElasticIP : acmElasticIPs) {
            if (!acmElasticIP.isAssociated()) {
                notAssociated.add(acmElasticIP);
            }
        }
        return notAssociated;
    }

    public static List<Ec2ElasticIP> getElasticIPByInstanceID(String id) {
        return new Ec2ElasticIP().getFiltered("instance-id", id);
    }
}
