package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.wrapper.events.ResourceDeleteHandler;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

/**
 * Wrapper for amazon ec2 Instance
 */
public class Ec2Instance extends Ec2Resource<Instance, Ec2Instance> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ec2Instance.class);

    private AdapterSettings settings;

    /*
     * Instance types
     */
    /**
     * <li>613 MB memory <li>Up to 2 EC2 Compute Units (for short periodic bursts) <li>EBS storage only <li>32-bit or 64-bit platform <li>I/O Performance: Low
     * 
     */
    public static final String INSTANCE_TYPE_MICRO = "t1.micro";
    public static final String INSTANCE_TYPE_MICRO_STRING = "2 EC2 CPU and 613 MB Memory";

    /**
     * <li>1.7 GB memory <li>1 EC2 Compute Unit (1 virtual core with 1 EC2 Compute Unit) <li>160 GB instance storage <li>32-bit platform <li>I/O Performance:
     * Moderate <li>API name: m1.small
     */
    public static final String INSTANCE_TYPE_SMALL = "m1.small";
    public static final String INSTANCE_TYPE_SMALL_STRING = "1 EC2 CPU and 1.7 GB Memory";

    /**
     * <li>7.5 GB memory <li>4 EC2 Compute Units (2 virtual cores with 2 EC2 Compute Units each) <li>850 GB instance storage <li>64-bit platform <li>I/O
     * Performance: High <li>API name: m1.large
     */
    public static final String INSTANCE_TYPE_LARGE = "m1.large";
    public static final String INSTANCE_TYPE_LARGE_STRING = "4 EC2 CPU and 7.5 GB Memory";

    /**
     * <li>15 GB memory <li>8 EC2 Compute Units (4 virtual cores with 2 EC2 Compute Units each) <li>1,690 GB instance storage <li>64-bit platform <li>I/O
     * Performance: High <li>API name: m1.xlarge
     */
    public static final String INSTANCE_TYPE_EXTRA_LARGE = "m1.xlarge";
    public static final String INSTANCE_TYPE_EXTRA_LARGE_STRING = "8 EC2 CPU and 15 GB Memory";

    /**
     * <li>17.1 GB of memory <li>6.5 EC2 Compute Units (2 virtual cores with 3.25 EC2 Compute Units each) <li>420 GB of instance storage <li>64-bit platform <li>
     * I/O Performance: Moderate <li>API name: m2.xlarge
     */
    public static final String INSTANCE_TYPE_HIGHT_MEMORY_EXTRA_LARGE = "m2.xlarge";
    public static final String INSTANCE_TYPE_HIGHT_MEMORY_EXTRA_LARGE_STRING = "6.5 EC2 CPU and 17.1 GB Memory";

    /**
     * <li>34.2 GB of memory <li>13 EC2 Compute Units (4 virtual cores with 3.25 EC2 Compute Units each) <li>850 GB of instance storage <li>64-bit platform <li>
     * I/O Performance: High <li>API name: m2.2xlarge
     */
    public static final String INSTANCE_TYPE_HIGHT_MEMORY_DOUBLE_EXTRA_LARGE = "m2.2xlarge";
    public static final String INSTANCE_TYPE_HIGHT_MEMORY_DOUBLE_EXTRA_LARGE_STRING = "13 EC2 CPU and 34.2 GB Memory";

    /**
     * <li>68.4 GB of memory <li>26 EC2 Compute Units (8 virtual cores with 3.25 EC2 Compute Units each) <li>1690 GB of instance storage <li>64-bit platform <li>
     * I/O Performance: High <li>API name: m2.4xlarge
     */
    public static final String INSTANCE_TYPE_HIGHT_MEMORY_QUADRUPLE_EXTRA_LARGE = "m2.4xlarge";
    public static final String INSTANCE_TYPE_HIGHT_MEMORY_QUADRUPLE_EXTRA_LARGE_STRING = "26 EC2 CPU and 68.4 GB Memory";

    /**
     * <li>1.7 GB of memory <li>5 EC2 Compute Units (2 virtual cores with 2.5 EC2 Compute Units each) <li>350 GB of instance storage <li>32-bit platform <li>I/O
     * Performance: Moderate <li>API name: c1.medium
     */
    public static final String INSTANCE_TYPE_HIGHT_CPU_MEDIUM = "c1.medium";
    public static final String INSTANCE_TYPE_HIGHT_CPU_MEDIUM_STRING = "5 EC2 CPU and 1.7 GB Memory";

    /**
     * <li>7 GB of memory <li>20 EC2 Compute Units (8 virtual cores with 2.5 EC2 Compute Units each) <li>1690 GB of instance storage <li>64-bit platform <li>I/O
     * Performance: High <li>API name: c1.xlarge
     */
    public static final String INSTANCE_TYPE_HIGHT_CPU_EXTRA_LARGE = "c1.xlarge";
    public static final String INSTANCE_TYPE_HIGHT_CPU_EXTRA_LARGE_STRING = "20 EC2 CPU and 7 GB Memory";

    /**
     * <li>23 GB of memory <li>33.5 EC2 Compute Units (2 x Intel Xeon X5570, quad-core â€œNehalemâ€� architecture) <li>1690 GB of instance storage <li>64-bit
     * platform <li>I/O Performance: Very High (10 Gigabit Ethernet) <li>API name: cc1.4xlarge
     */
    public static final String INSTANCE_TYPE_CLUSTER_QUADRUPLE_EXTRA_LARGE = "cc1.4xlarge";
    public static final String INSTANCE_TYPE_CLUSTER_QUADRUPLE_EXTRA_LARGE_STRING = "33.5 EC2 CPU and 23 GB Memory";

    /**
     * <li>22 GB of memory <li>33.5 EC2 Compute Units (2 x Intel Xeon X5570, quad-core â€œNehalemâ€� architecture) <li>2 x NVIDIA Tesla â€œFermiâ€� M2050 GPUs
     * <li>1690 GB of instance storage <li>64-bit platform <li>I/O Performance: Very High (10 Gigabit Ethernet) <li>API name: cg1.4xlarge
     */
    public static final String INSTANCE_TYPE_CLUSTER_GPU_QUADRUPLE_EXTRA_LARGE = "cg1.4xlarge";
    public static final String INSTANCE_TYPE_CLUSTER_GPU_QUADRUPLE_EXTRA_LARGE_STRING = "33.5 EC2 CPU and 22 GB Memory";

    public static final String HARDWARE_TAG = "hardware";

    /*
     * EC2 machine states
     */
    public static final String STATE_PENDING = "pending";
    public static final String STATE_RUNNING = "running";
    public static final String STATE_STOPPING = "stopping";
    public static final String STATE_STOPPED = "stopped";
    public static final String STATE_TERMINATED = "terminated";
    public static final String STATE_SHUTDOWN = "shutting-down";

    private RunInstancesResult createResult;

    /*
     * Volumes attach points and snapshots id for it
     */
    private Map<String, String> attachVolume = new HashMap<String, String>();

    public Ec2Instance(Instance instance) {
        super(instance);
    }

    public Ec2Instance() {
    }

    public Ec2Instance(String id, boolean isAMI) throws ResourceDoesNotExistException {
        super(id);
        if (!isAMI) {
            refresh();
        }
    }

    @Override
    protected void doDeleteRequest() {
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(getId());
        getEc2().terminateInstances(request);
        //result.getTerminatingInstances().get(0).

    }

    /*
     * props for create instance: 
     */
    public static final String INIT_INSTANCE_TYPE = "instance-type";
    public static final String INIT_SEC_GROUP = "sec-group";
    public static final String INIT_KEY_NAME = "key-name";
    public static final String INIT_SUB_NET_ID = "sub-net-id";
    public static final String INIT_BLOCK_DEVICE_MAP = "block-device-mapping";
    public static final String INIT_PRIVATE_IP = "private-ip";

    public static String getServiceStringByServiceType(String type) {
        if (!"".equals(type)) {
            if (Ec2Instance.INSTANCE_TYPE_MICRO.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_MICRO_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_SMALL.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_SMALL_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_EXTRA_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_HIGHT_MEMORY_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_HIGHT_MEMORY_EXTRA_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_HIGHT_MEMORY_DOUBLE_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_HIGHT_MEMORY_DOUBLE_EXTRA_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_HIGHT_MEMORY_QUADRUPLE_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_HIGHT_MEMORY_QUADRUPLE_EXTRA_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_HIGHT_CPU_MEDIUM.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_HIGHT_CPU_MEDIUM_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_HIGHT_CPU_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_HIGHT_CPU_EXTRA_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_CLUSTER_QUADRUPLE_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_CLUSTER_QUADRUPLE_EXTRA_LARGE_STRING;
            } else if (Ec2Instance.INSTANCE_TYPE_CLUSTER_GPU_QUADRUPLE_EXTRA_LARGE.equalsIgnoreCase(type)) {
                return Ec2Instance.INSTANCE_TYPE_CLUSTER_GPU_QUADRUPLE_EXTRA_LARGE_STRING;
            }
        }
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doCreateRequest(AmazonEC2 ec2, String description, Properties props, AdapterSettings settings) {
        this.settings = settings;
        if (getId().contains("ami-")) {
            RunInstancesRequest request = new RunInstancesRequest().withInstanceType(props.getProperty(INIT_INSTANCE_TYPE, INSTANCE_TYPE_EXTRA_LARGE))
                    .withSecurityGroupIds(props.getProperty(INIT_SEC_GROUP, "")).withImageId(getId());
            if (!props.getProperty(INIT_PRIVATE_IP, "").trim().equals("")) {
                request.withPrivateIpAddress(props.getProperty(INIT_PRIVATE_IP, ""));
            }
            request.withKeyName(props.getProperty(INIT_KEY_NAME, "common")).withSubnetId(props.getProperty(INIT_SUB_NET_ID, "")).withMaxCount(1)
                    .withMinCount(1);
            if (props.containsKey(INIT_BLOCK_DEVICE_MAP)) {
                request.setBlockDeviceMappings((Collection<BlockDeviceMapping>) props.get("block-device-mapping"));
            }
            if (attachVolume.size() > 0) {
                Collection<BlockDeviceMapping> blockDeviceMappings = new ArrayList<BlockDeviceMapping>();
                for (String attachPoint : attachVolume.keySet()) {
                    BlockDeviceMapping blockDeviceMapping = new BlockDeviceMapping();
                    EbsBlockDevice blockDevice = new EbsBlockDevice();
                    blockDevice.setDeleteOnTermination(true);
                    blockDevice.setSnapshotId(attachVolume.get(attachPoint));
                    blockDeviceMapping.setEbs(blockDevice);
                    blockDeviceMapping.setDeviceName(attachPoint);
                    blockDeviceMappings.add(blockDeviceMapping);
                }
                request.withBlockDeviceMappings(blockDeviceMappings);
            }
            RunInstancesResult result = ec2.runInstances(request);
            this.createResult = result;
            doUpdate(result.getReservation().getInstances().get(0));
            try {
                Thread.sleep(5000);
                //will mark all started jobs with prefix
                addTag("Name", getInstancePrefix() + description);
            } catch (Exception e) {
                // TODO: handle exception
            }
            // clean resources:
            attachVolume.clear();
        }
    }

    private String getInstancePrefix() {
        String username = System.getenv("USERNAME") == null ? "" : System.getenv("USERNAME") + "_";
        return settings.getInstancePrefix().concat(username);
    }

    @Override
    protected String getResourceId() {
        return getResource().getInstanceId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeInstancesRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2Instance> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2Instance> instances = new ArrayList<Ec2Instance>();
        DescribeInstancesResult result = amazonEC2.describeInstances((DescribeInstancesRequest) request);
        for (Reservation reservation : result.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                instances.add(new Ec2Instance(instance));
            }
        }
        return instances;
    }

    @Override
    protected Instance processDescribe() throws ResourceDoesNotExistException {
        try {
            DescribeInstancesResult result = getEc2().describeInstances(new DescribeInstancesRequest().withInstanceIds(getId()));
            List<Reservation> reservations = result.getReservations();
            return reservations.get(0).getInstances().get(0);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            throw new ResourceDoesNotExistException(getId());
        }

    }

    /**
     * Return all instances running in provided sub-network
     * 
     * @param subNetwork
     *            - subnetwork amazon id
     * @return
     */
    public static List<Ec2Instance> fromSubNetwork(String subNetwork) {
        return new Ec2Instance().getFiltered("subnet-id", subNetwork);
    }

    /**
     * Deletes all provided instances
     * 
     * @param instances
     * @param handler
     */
    public static void deleteAll(List<Ec2Instance> instances, ResourceDeleteHandler<Ec2Instance> handler) {
        new Ec2Instance().delete(instances, handler);
    }

    /**
     * Return instance with provided private ip adress
     * 
     * @param ip
     *            - private ip of machine
     * @return ACMInstance or null if there is no machine with private ip
     */
    public static Ec2Instance getWithPrivateIp(String ip) {
        List<Ec2Instance> instances = new Ec2Instance().getFiltered("private-ip-address", ip);
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    public static Ec2Instance getWithPublicDNS(String dns) {
        List<Ec2Instance> instances = new Ec2Instance().getFiltered("dns-name", dns);
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    /**
     * Return volumes witch use this machine
     * 
     * @return
     */
    public List<Ec2Volume> getVolumes() {
        return Ec2Volume.getFromInstanceId(getResourceId());
    }

    /**
     * Return private ip of this machine
     * 
     * @return
     */
    public String getPrivateIp() {
        return getResource().getPrivateIpAddress();
    }

    /**
     * Return public DNS
     */
    public String getPublicDNS() {
        return getResource().getPublicDnsName();
    }

    /**
     * Get machine host can be public DNS or Private IP (if machine running VPC sub-network
     */
    public String getHost() {
        if (getPublicDNS() != null && !getPublicDNS().equals("")) {
            return getPublicDNS();
        }
        return getPrivateIp();
    }

    /**
     * This will open input ports for whole security group
     * 
     * @param ports
     */
    //TODO implementation
    public void openPorts(Integer... ports) {
        throw new RuntimeException("This method not implemented yet!");
    }

    /**
     * This will close input ports for whole security group
     * 
     * @param ports
     */
    //TODO implementation
    public void closePorts(Integer... ports) {
        throw new RuntimeException("This method not implemented yet!");
    }

    /**
     * Get machine state: pending, running, shutting-down, terminated always refresh state from amazon
     * 
     * @return
     */
    public String getState() throws ResourceDoesNotExistException {
        refresh();
        return getResource().getState().getName();
    }

    /**
     * Return fake string like ec2-run-instances return
     * 
     * @return
     */
    public String ec2ApiToolsFakeString() {
        Reservation reservation = createResult.getReservation();
        StringBuilder builder = new StringBuilder("RESERVATION     ").append(reservation.getReservationId()).append("    ").append(reservation.getOwnerId());
        for (String group : reservation.getGroupNames()) {
            builder.append("    ").append(group);
        }
        builder.append("\n");

        // INSTANCE i-5bca5a30 ami-b232d0db pending 0 m1.small
        // 2010-04-07T12:25:47+0000 us-east-1a aki-94c527fd ari-96c527ff
        // monitoring-disabled ebs paravirtual xen
        for (Instance instance : reservation.getInstances()) {
            builder.append("INSTANCE  ").append(instance.getInstanceId()).append("    ").append(instance.getImageId()).append("    ")
                    .append(instance.getState().getName()).append("    ").append(instance.getAmiLaunchIndex()).append("    ")
                    .append(instance.getInstanceType()).append(" ").append(instance.getLaunchTime()).append("\n");
        }
        return builder.toString();
    }

    public Instance getAmazonInstance() {
        return getResource();
    }

    public void stop() throws ResourceDoesNotExistException {
        if (getState().equals(STATE_RUNNING)) {
            getEc2().stopInstances(new StopInstancesRequest().withInstanceIds(getResourceId()));
        }
    }

    public void start() throws ResourceDoesNotExistException {
        if (getState().equals(STATE_STOPPED)) {
            getEc2().startInstances(new StartInstancesRequest().withInstanceIds(getResourceId()));
        }
    }

    public void reboot() throws ResourceDoesNotExistException {
        if (getState().equals(STATE_RUNNING)) {
            getEc2().rebootInstances(new RebootInstancesRequest().withInstanceIds(getResourceId()));
        }
    }

    //TODO Stopped machines also can capture
    //TODO NAme for ami
    public String capture() throws ResourceDoesNotExistException {
        if (getState().equals(STATE_RUNNING)) {
            CreateImageResult result = getEc2().createImage(new CreateImageRequest().withInstanceId(getResourceId()).withName("CAPTURE"));
            return result.getImageId();
        }
        return null;
    }

    public String capture(String name, String description) throws ResourceDoesNotExistException {
        if (getState().equals(STATE_RUNNING)) {
            CreateImageResult result = getEc2().createImage(
                    new CreateImageRequest().withInstanceId(getResourceId()).withName(name).withDescription(description));
            return result.getImageId();
        }
        return null;
    }

    private boolean portPresent(IpPermission ipPermission, int port) {
        int startPort = ipPermission.getFromPort();
        int endPort = ipPermission.getToPort();
        return (startPort <= port && port <= endPort);
    }

    private boolean portPresent(List<IpPermission> ipPermissions, int port) {
        for (IpPermission ipPermission : ipPermissions) {
            if (portPresent(ipPermission, port)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This will allow TCP ports ingress for CIDR 0.0.0.0/0 for security group this machine running with.
     * 
     * @param ports
     */
    public void allowPorts(int... ports) {

        LOGGER.debug("About to allow ports: " + Arrays.toString(ports));
        String groupId = this.getResource().getSecurityGroups().get(0).getGroupId();
        LOGGER.debug("Security GroupId for this machine: " + groupId);

        //		//Getting list of already allowed permissions:
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.withGroupIds(groupId);
        List<IpPermission> ipPermissions = getEc2().describeSecurityGroups(describeSecurityGroupsRequest).getSecurityGroups().get(0).getIpPermissions();
        LOGGER.debug("Already opened ports: " + ipPermissions);
        List<IpPermission> toSend = new ArrayList<IpPermission>();
        for (int port : ports) {
            if (!portPresent(ipPermissions, port)) {
                LOGGER.debug("This port is not present, add to send list: " + port);
                IpPermission allowPort = new IpPermission();
                allowPort.withIpProtocol("tcp").withFromPort(port).withToPort(port).withIpRanges("0.0.0.0/0");
                toSend.add(allowPort);
            } else {
                LOGGER.debug("This port present in Security Group, skiping:" + port);
            }
        }

        AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest();
        request.setGroupId(groupId);
        request.withIpPermissions(toSend);
        LOGGER.debug("Sending authorizeSecurityGroupIngress to Amazon: " + request.toString());
        getEc2().authorizeSecurityGroupIngress(request);

    }

    /**
     * This will disallow TCP ports ingress for CIDR 0.0.0.0/0 for security group this machine running with. <div style="color: red">Warning! if affected port
     * is in <b>range</b> of allowed ports (i.e port to disable - 22, opened ports 10-100), this method will not disable it!</div>
     * 
     * @param ports
     * 
     */
    public void disallowPorts(int... ports) {

        LOGGER.debug("About to disallow ports: " + Arrays.toString(ports));
        String groupId = this.getResource().getSecurityGroups().get(0).getGroupId();
        LOGGER.debug("Security GroupId for this machine: " + groupId);

        //Getting list of permissions for current security group
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.withGroupIds(groupId);
        List<IpPermission> ipPermissions = getEc2().describeSecurityGroups(describeSecurityGroupsRequest).getSecurityGroups().get(0).getIpPermissions();
        LOGGER.debug("Open ports for this machine: " + ipPermissions);
        List<IpPermission> toSend = new ArrayList<IpPermission>();
        for (int port : ports) {
            LOGGER.debug("Dissable port: " + port);
            IpPermission disablePort = new IpPermission();
            disablePort.withIpProtocol("tcp").withFromPort(port).withToPort(port).withIpRanges("0.0.0.0/0");
            toSend.add(disablePort);
        }

        RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest();
        request.setGroupId(groupId);
        request.withIpPermissions(toSend);
        LOGGER.debug("Sending revokeSecurityGroupIngress to Amazon: " + request.toString());
        getEc2().revokeSecurityGroupIngress(request);
    }

    /**
     * Suspend execution until server boot
     */
    public void waitForServerBoot() {
        while (getResource().getState().getName().equals(Ec2Instance.STATE_PENDING)) {
            try {
                Thread.sleep(5000);
                refresh();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                break;
            }
        }
        if (!getResource().getState().getName().equals(Ec2Instance.STATE_RUNNING)) {
            throw new RuntimeException("Machine is in incorrect state: " + getResource().getState());
        }
    }

    /**
     * Return name of this instance
     * 
     * @return name of this instance, "" if this instance doesn't have name
     */
    public String getName() {
        for (Tag tag : getResource().getTags()) {
            if ("Name".equals(tag.getKey()))
                return tag.getValue();
        }
        return "";
    }

    /**
     * Attach snapshot volume to this instance. Only works if all instance not created on Amazon
     */
    public String atttachSnapshot(String attachPoint, String snapshotId) {
        return attachVolume.put(attachPoint, snapshotId);
    }

    /**
     * Remove attach (only works before instance create on Amazon
     */
    public String removeAttach(Object attachPoint) {
        return attachVolume.remove(attachPoint);
    }

    /**
     * Whatever or not this instance exists and running on Amazon AWS
     */
    public boolean isRunning() {
        try {
            return STATE_RUNNING.equals(getState());
        } catch (ResourceDoesNotExistException e) {
            return false;
        }
    }

    /**
     * Whatever or not this instance exists and stopped on Amazon AWS
     */
    public boolean isStopped() {
        try {
            return STATE_STOPPED.equals(getState());
        } catch (ResourceDoesNotExistException e) {
            return false;
        }
    }

    /**
     * Whatever or not this instance running in VPC
     */
    public boolean isVPCInstance() {
        try {
            return !getResource().getVpcId().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Instance Type of this machine
     * 
     * @return m1.large, etc...
     */
    public String getType() {
        return getResource() == null ? "" : getResource().getInstanceType();
    }

    public static boolean isRunning(String instanceId) {
        Validate.notEmpty(instanceId);
        try {
            return new Ec2Instance(instanceId, false).isRunning();
        } catch (ResourceDoesNotExistException e) {
            return false;
        }
    }

    /**
     * Whatever instance is stopped
     * 
     * @param instanceId
     * @return
     */
    public static boolean isStopped(String instanceId) {
        Validate.notEmpty(instanceId);
        try {
            return new Ec2Instance(instanceId, false).isStopped();
        } catch (ResourceDoesNotExistException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "CMInstance: {id: " + getId() + ", host: " + getHost() + "}";
    }

}
