package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.wrapper.events.ResourceDeleteHandler;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

/**
 * Simple wrap com.amazonaws.services.ec2.model.Snapshot and override equals & hashcode;
 * 
 * @author qatest
 * 
 */
public class Ec2Snapshot extends Ec2Resource<Snapshot, Ec2Snapshot> {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_COMPLETED = "completed";

    public Ec2Snapshot() {
    }

    public Ec2Snapshot(Snapshot snapshot) {
        super(snapshot);
    }

    public Ec2Snapshot(String snapshotID, boolean isSnapshotId) throws ResourceDoesNotExistException {
        super(snapshotID);
        if (isSnapshotId) {
            refresh();
        }
    }

    @Override
    protected void doDeleteRequest() {
        getEc2().deleteSnapshot(new DeleteSnapshotRequest(getId()));
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String description, Properties props, AdapterSettings settings) {
        if (getId().contains("vol-")) {
            CreateSnapshotResult result = ec2.createSnapshot(new CreateSnapshotRequest(getId(), description));
            doUpdate(result.getSnapshot());
        }

    }

    @Override
    protected String getResourceId() {
        return getResource().getSnapshotId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeSnapshotsRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2Snapshot> processDescribe(AmazonEC2 amazonEC2, AmazonWebServiceRequest request) {
        List<Ec2Snapshot> snapshots = new ArrayList<Ec2Snapshot>();
        DescribeSnapshotsResult result = amazonEC2.describeSnapshots(((DescribeSnapshotsRequest) request).withOwnerIds(getUserId()));
        for (Snapshot snapshot : result.getSnapshots()) {
            snapshots.add(new Ec2Snapshot(snapshot));
        }
        return snapshots;
    }

    @Override
    protected Snapshot processDescribe() throws ResourceDoesNotExistException {
        try {
            DescribeSnapshotsResult result = getEc2().describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(getId()));
            return result.getSnapshots().get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public Integer getSize() {
        return getResource().getVolumeSize();
    }

    public String getDescription() {
        return getResource().getDescription();
    }

    public String getVolumeId() {
        return getResource().getVolumeId();
    }

    public String getState() throws ResourceDoesNotExistException {
        refresh();
        return getResource().getState();
    }

    public Calendar getCreatedAt() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(getResource().getStartTime());
        return calendar;
    }

    public static void deleteAll(List<Ec2Snapshot> acmSnapshots, ResourceDeleteHandler<Ec2Snapshot> handler) {
        new Ec2Snapshot().delete(acmSnapshots, handler);
    }

    public static List<Ec2Snapshot> getAllSnapshots() {
        return new Ec2Snapshot().getAll();
    }

    public static boolean exists(String snapshotID) {
        try {
            new Ec2Snapshot(snapshotID, true);
            return true;
        } catch (ResourceDoesNotExistException e) {
            return false;
        }
    }

}
