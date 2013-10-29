package com.amazonaws.wrapper.exception;

public class ResourceDoesNotExistException extends Exception {

    private static final long serialVersionUID = -8955617760746593629L;

    public ResourceDoesNotExistException(String resourceId) {
        super("Resource doesn't exist on Amazon Cloud [" + resourceId + "]");
    }

}
