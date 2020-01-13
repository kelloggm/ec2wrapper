package com.amazonaws.wrapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteTagsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.wrapper.events.ResourceCreateHandler;
import com.amazonaws.wrapper.events.ResourceDeleteHandler;
import com.amazonaws.wrapper.exception.ResourceDoesNotExistException;

import org.checkerframework.checker.objectconstruction.qual.CalledMethodsPredicate;

/**
 * Base class for all amazon related resources This class provide simple methods for creating, searching, deleting any resource from amazon aws
 * 
 * @param <T>
 *            - Amazon java api model
 * @param <O>
 *            - Name of actual class that extends this class
 */
public abstract class Ec2Resource<T, O extends Ec2Resource<T, O>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ec2Resource.class);

    /*
     * Amazon ec2 connector - holds AWS related objects 
     */
    private Ec2Connector ec2Connector;

    /*
     * This handler call automatically before, after deleting resource
     *  or if it throws any error
     */
    private ResourceDeleteHandler<O> deleteHandler;

    /*
     * This handler call automatically before, after create resource
     *  or if it throws any error on creating operation
     */
    private ResourceCreateHandler<O> createHandler;

    /*
     * Amazon model
     */
    private T resource;

    /**
     * Set amazon conector
     * 
     * @param ec2connector
     */

    Ec2Resource(T resource) {
        doUpdate(resource);
    }

    //Constructor
    Ec2Resource() {
    }

    //Constructor
    Ec2Resource(String id) {
        this.uniqueId = id;
    }

    /*
     * In most case this value holds
     * unique id of amazon resource, but
     * it can hold id of resource from with we can create actual
     * For example:
     * ACMSnapshot snapshot = new ACMSnapshot("volume-id").create; 
     */
    private String uniqueId;

    private AdapterSettings settings;

    //Constructor
    public void setId(String id) {
        this.uniqueId = id;
    }

    /**
     * Return unique id of this resource (in most cases this value equal to amazon ec2 unique id), but it can hold id of resource from with we can create actual
     * For example: ACMSnapshot snapshot = new ACMSnapshot("volume-id").create; after refresh it become actual one
     * 
     * @return
     */
    public String getId() {
        return this.uniqueId;
    }

    /**
     * Return amazon ec2 connector
     * 
     * @return
     * @throws RuntimeException
     *             if this resource doesn't have unique id.
     */
    protected AmazonEC2 getEc2() {
        if (this.uniqueId != null) {
            return getEc2Connector().getAmazonEC2();
        } else {
            LOGGER.error("You cant use amazonEC2 requests unless you specify id for resource");
            throw new RuntimeException("You cant use amazonEC2 requests unless you specify id for resource");
        }
    }

    /**
     * Add tag to amazon resource
     * 
     * @param name
     * @param value
     */
    public void addTag(String name, String value) {
        CreateTagsRequest request = new CreateTagsRequest().withResources(uniqueId).withTags(new Tag(name, value));
        getEc2().createTags(request);
    }

    /**
     * Delete amazon tag from this resource
     * 
     * @param name
     * @param value
     */
    public void deleteTag(String name, String value) {
        DeleteTagsRequest request = new DeleteTagsRequest().withResources(uniqueId).withTags(new Tag(name, value));
        getEc2().deleteTags(request);
    }

    /**
     * Delete tag from all resources of this type from amazon
     */
    public void deleteTagFromAll(String name, String value) {
        DeleteTagsRequest request = new DeleteTagsRequest().withTags(new Tag(name, value)).withResources(allIds());
        getEc2Connector().getAmazonEC2().deleteTags(request);
    }

    /**
     * Updates uniq id, & resource
     * 
     * @param resource
     */
    protected void doUpdate(T resource) {
        this.resource = resource;
        this.uniqueId = getResourceId();
    }

    /**
     * Gets amazon model of this resource
     * 
     * @return actual amazon model of this resource
     * @throws RuntimeException
     *             if this resource = null if you have this error check if it has uniq id and than call refresh method
     */
    protected T getResource() {
        if (resource != null) {
            return resource;
        }
        LOGGER.error("You cant use amazonEC2 requests unless you specify id for resource");
        throw new RuntimeException("Cant reach resource, try refresh() first");
    }

    /**
     * Helper method for getting current user id of logged amazon user
     * 
     * @return
     */
    String getUserId() {
        return getEc2Connector().getUserId();
    }

    /**
     * Override this method with delete logic of concrete amazon resource for getting ec2 service use getEc2() method
     */
    abstract protected void doDeleteRequest();

    /**
     * Override this method for implementation of creating amazon resource
     * 
     * @param ec2
     *            - amazon aws service
     * @param description
     *            -description of created resource
     * @param props
     *            - additional properties for creating resource
     * @param settings
     *            - settings object
     */
    abstract protected void doCreateRequest(AmazonEC2 ec2, String description, Properties props, AdapterSettings settings);

    /**
     * This method must return id of underground amazon model
     * 
     * @return id of amazon model
     */
    abstract protected String getResourceId();

    /**
     * Override this method to implement creating of concrete "Describe" request with filters
     * 
     * @param filters
     *            - filters to be used with request
     * @return prepared Amazon "Describe" request
     */
    abstract protected AmazonWebServiceRequest applyFiltersForRequest(Filter... filters);

    /**
     * Must implement logic of sending and parsing results from amazon
     * 
     * @param amazonEC2
     *            - amazon AWS service
     * @param request
     *            - request to send to amazon
     * @return list of actual objects
     */
    abstract protected List<O> processDescribe(AmazonEC2 amazonEC2, @CalledMethodsPredicate("(withOwners || setOwners) || (withImageIds || setImageIds) || (withExecutableUsers || setExecutableUsers)") AmazonWebServiceRequest request);

    /**
     * Override this method for getting info of this object from amazon use getEC2() and getId() to implement this behavior
     * 
     * @return
     */
    abstract protected T processDescribe() throws ResourceDoesNotExistException;

    /**
     * Return all objects of this type from amazon
     * 
     * @return
     */
    public List<O> getAll() {
        // TRUE POSITIVE: gets everything w/o regard for AMI sniping
        return processDescribe(getEc2Connector().getAmazonEC2(), applyFiltersForRequest());
    }

    /**
     * Return all objects of this type witch filtered with "key=value"
     * 
     * @param key
     *            - amazon filer key
     * @param values
     *            - key value
     * @return All founded objects
     */
    public List<O> getFiltered(String key, String... values) {
        // ESH POSITIVE: doesn't guarantee that the right filters are passed, wraps the API
        return processDescribe(getEc2Connector().getAmazonEC2(), applyFiltersForRequest(new Filter().withName(key).withValues(values)));
    }

    /**
     * Return all objects of this type witch filtered with "key=value"
     * 
     * @param filters
     *            array of amazon fiters to apply
     * @return all founded objects
     */
    public List<O> getFiltered(Filter... filters) {
        // ESH POSITIVE: doesn't guarantee that the right filters are passed, wraps the API
        return processDescribe(getEc2Connector().getAmazonEC2(), applyFiltersForRequest(filters));
    }

    /**
     * Return all resources of this type witch have tag provided tag
     * 
     * @param name
     *            - tag name
     * @param value
     *            - tag value
     * @return All founded objects
     */
    public List<O> getTagged(String name, String value) {
        return getFiltered("tag:" + name, value);
    }

    /**
     * Return list of all amazon ids of this type
     * 
     * @return
     */
    public List<String> allIds() {
        List<O> list = getAll();
        List<String> ids = new ArrayList<String>();
        for (O o : list) {
            ids.add(o.getId());
        }
        return ids;
    }

    /**
     * Returns all resources that is not tagged with provided name, value
     * 
     * @param name
     *            - tag name
     * @param value
     *            - tag value
     * @return all objects that is not tagged with provided values
     */
    public List<O> getNotTagged(String name, String value) {
        List<O> tagged = getTagged(name, value);
        List<O> all = getAll();
        all.removeAll(tagged);
        return all;
    }

    /**
     * Synchronize resource with amazon
     * 
     * @throws RuntimeException
     *             if this resources doesn't have unique id
     */
    public void refresh() throws ResourceDoesNotExistException {
        if (this.uniqueId != null) {
            doUpdate(processDescribe());
        } else {
            throw new RuntimeException("You cant use amazonEC2 requests unless you specify id resource");
        }
    }

    /**
     * Delete this resource from amazon If it was set delete handler (by setDeleteHandler) it will call beforeDelete(resource) method for check if this objects
     * need to be deleted
     */
    @SuppressWarnings("unchecked")
    public void delete() throws Exception {
        try {
            if ((getDeleteHandler() == null) || getDeleteHandler().beforeDelete((O) this, settings.isEmulation())) {
                doDeleteRequest();
                //	this.uniqueId = null;
                if (getDeleteHandler() != null) {
                    getDeleteHandler().afterDelete((O) this);
                }
            }
        } catch (Exception exception) {
            if (getDeleteHandler() != null) {
                getDeleteHandler().afterThrow((O) this, exception);
            } else
                LOGGER.error(exception.getMessage());
            throw exception;
        }
    }

    /**
     * Deletes all objects from list It simple call delete method for all objects
     * 
     * @param resources
     */
    public void delete(List<O> resources) throws Exception {
        for (O acmResource : resources) {
            acmResource.delete();
        }
    }

    /**
     * Delete list of objects with given delete handler
     * 
     * @param resources
     *            - list of resources to be deleted
     * @param event
     *            - handler that will automatically calls for all objects
     */
    public void delete(List<O> resources, ResourceDeleteHandler<O> event) {
        applyDeleteHandlerForAll(event, resources);
        for (O acmResource : resources) {
            try {
                acmResource.delete();
            } catch (Exception e) {
                //Actually this case is unreachable)
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates this resource in amazon aws
     * 
     * @param properties
     *            - properties to be used for creating
     * @param description
     *            - description of created objects if it was set creation handler it will be called it methods in workflow order
     */
    @SuppressWarnings("unchecked")
    public void create(Properties properties, String description) {
        try {
            if ((getCreateHandler() == null) || getCreateHandler().beforeCreate((O) this, settings.isEmulation())) {
                doCreateRequest(getEc2Connector().getAmazonEC2(), description, properties, settings);
                refresh();
                if (getCreateHandler() != null) {
                    getCreateHandler().afterCreate((O) this);
                }
            }
        } catch (RuntimeException exception) {
            if (getCreateHandler() != null) {
                getCreateHandler().afterThrow((O) this, exception);
            } else
                LOGGER.error(exception.getMessage(), exception);
            throw exception;
        } catch (ResourceDoesNotExistException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    //TODO AFTER DELETE LOGIC BROKEN!!!
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof Ec2Resource)) {
            return false;
        }
        Ec2Resource<?, ?> other = (Ec2Resource<?, ?>) object;
        if (other.getId() != null && this.getId() != null) {
            return (other.getId().equals(getId()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() != null) {
            return getId().hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return getResourceId();
    }

    /**
     * Sets the delete handler for calling workflow methods if this objects wil be deleted
     * 
     * @param handler
     */
    public void setDeleteHandler(ResourceDeleteHandler<O> handler) {
        this.deleteHandler = handler;
    }

    /**
     * Returns this objects delete handler
     * 
     * @return
     */
    public ResourceDeleteHandler<O> getDeleteHandler() {
        return deleteHandler;
    }

    /**
     * Sets create handler for this objec
     * 
     * @param createHandler
     */
    public void setCreateHandler(ResourceCreateHandler<O> createHandler) {
        this.createHandler = createHandler;
    }

    /**
     * Return this object create handler
     * 
     * @return
     */
    public ResourceCreateHandler<O> getCreateHandler() {
        return createHandler;
    }

    //Static helper methods
    protected static <T extends Ec2Resource<?, T>> void applyDeleteHandlerForAll(ResourceDeleteHandler<T> event, List<T> resources) {
        for (T t : resources) {
            t.setDeleteHandler(event);
        }
    }

    protected static <T extends Ec2Resource<?, T>> void forAll(ResourceCreateHandler<T> event, List<T> resources) {
        for (T t : resources) {
            t.setCreateHandler(event);
        }
    }

    private Ec2Connector getEc2Connector() {
        return ec2Connector;
    }

}
