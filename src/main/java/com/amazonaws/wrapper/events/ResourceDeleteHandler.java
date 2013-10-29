package com.amazonaws.wrapper.events;

/**
 * Handler for resource deleting
 * 
 * @param <T>
 */
public interface ResourceDeleteHandler<T> {

    /**
     * Calling before resource will be deleted
     * 
     * @param resource
     *            - that will be deleted
     * @param sendDataToAmazon
     *            - determine if need send data to Amazon
     * @return true - if resource must be deleted, false - otherwise
     */
    boolean beforeDelete(T resource, boolean sendDataToAmazon);

    /**
     * Calling after resource was actual deleted from amazon
     * 
     * @param resource
     *            mockup of resource that was deleted
     */
    void afterDelete(T resource);

    /**
     * Call back function that calling if deleting of resource raise exception
     * 
     * @param resource
     *            resource that was deleted
     * @param exception
     *            exception
     */
    void afterThrow(T resource, Exception exception);

}
