package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

import org.checkerframework.checker.objectconstruction.qual.CalledMethodsPredicate;

public class Ec2Image extends Ec2Resource<Image, Ec2Image> {

    public Ec2Image(Image image) {
        super(image);
    }

    public Ec2Image() {
    }

    public Ec2Image(String imageId) throws ResourceDoesNotExistException {
        super(imageId);
        refresh();
    }

    @Override
    protected void doDeleteRequest() {
        DeregisterImageRequest request = new DeregisterImageRequest().withImageId(getId());
        getEc2().deregisterImage(request);
    }

    @Override
    protected void doCreateRequest(AmazonEC2 ec2, String description, Properties props, AdapterSettings constants) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getResourceId() {
        return getResource().getImageId();
    }

    @Override
    protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters) {
        return new DescribeImagesRequest().withFilters(filters);
    }

    @Override
    protected List<Ec2Image> processDescribe(AmazonEC2 amazonEC2, @CalledMethodsPredicate("(withOwners || setOwners) || (withImageIds || setImageIds) || (withExecutableUsers || setExecutableUsers)") AmazonWebServiceRequest request) {
        List<Ec2Image> images = new ArrayList<Ec2Image>();
        DescribeImagesResult result = amazonEC2.describeImages(((DescribeImagesRequest) request));
        for (Image image : result.getImages()) {
            images.add(new Ec2Image(image));
        }
        return images;
    }

    @Override
    protected Image processDescribe() throws ResourceDoesNotExistException {
        try {
            DescribeImagesResult result = getEc2().describeImages(new DescribeImagesRequest().withImageIds(getId()));
            return result.getImages().get(0);
        } catch (Exception ex) {
            throw new ResourceDoesNotExistException(getId());
        }
    }

    public String getState() throws ResourceDoesNotExistException {
        refresh();
        return getResource().getState();
    }

    public static List<Ec2Image> getAllImages() {
        return new Ec2Image().getAll();
    }

    /**
     * Get name of this image
     */
    public String getName() {
        return getResource().getName();
    }

    private List<Ec2Image> getOwnedImagesP() {
        return getFiltered("owner-id", getUserId());
    }

    private List<Ec2Image> getPrivateImagesP() {
        return getFiltered("is-public", "false");
    }

    /**
     * Return list of all owned AMIs
     * 
     * @return
     */
    public static List<Ec2Image> getOwnedImages() {
        return new Ec2Image().getOwnedImagesP();
    }

    /**
     * Return list of all available private images
     */
    public static List<Ec2Image> getAllPrivateImages() {
        return new Ec2Image().getPrivateImagesP();
    }

}
