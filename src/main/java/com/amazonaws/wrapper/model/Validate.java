package com.amazonaws.wrapper.model;

class Validate {

    public static void notEmpty(String string) {
        if(string == null || string.trim().isEmpty()){
            throw new IllegalArgumentException("Validated string is empty");
        }
    }

}
