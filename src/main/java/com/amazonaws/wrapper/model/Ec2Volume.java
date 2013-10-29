package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

public class Ec2Volume extends Ec2Resource<Volume, Ec2Volume> {

    public Ec2Volume() {
    }

    public Ec2Volume(Volume volume) {
        super(volume);
    }

    public Ec2Volume(String volumeId) throws ResourceDoesNotExistException {
        super(volumeId);
        refresh();
    }

    @Override
    protected void doDeleteRequest() {
        getEc2().deleteVolume(new DeleteVolumeRequest(getId()));
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String description, Properties props, AdapterSettings settings) {

    }

    @Override
    protected String getResourceId() {
        return getResource().getVolumeId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeVolumesRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2Volume> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2Volume> volumes = new ArrayList<Ec2Volume>();
        DescribeVolumesResult result = amazonEC2.describeVolumes((DescribeVolumesRequest) request);
        for (Volume volume : result.getVolumes()) {
            volumes.add(new Ec2Volume(volume));
        }
        return volumes;
    }

    public Ec2Snapshot takeSnapshot(String description) {
        return new Ec2Snapshot(getEc2().createSnapshot(new CreateSnapshotRequest().withVolumeId(getId()).withDescription(description)).getSnapshot());
    }

    public String attachedTo() {
        return getResource().getAttachments().get(0).getDevice();
    }

    @Override
    protected Volume processDescribe() throws ResourceDoesNotExistException {
        try {
            DescribeVolumesResult result = getEc2().describeVolumes(new DescribeVolumesRequest().withVolumeIds(getId()));
            return result.getVolumes().get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public static List<Ec2Volume> getAllAvailable() {
        return new Ec2Volume().getFiltered("status", "available");
    }

    public static List<Ec2Volume> getAttachedTo(String attachPoint) {
        return new Ec2Volume().getFiltered("attachment.device", attachPoint);
    }

    public static List<Ec2Volume> getFromInstanceId(String instanceId) {
        return new Ec2Volume().getFiltered("attachment.instance-id", instanceId);
    }
}
