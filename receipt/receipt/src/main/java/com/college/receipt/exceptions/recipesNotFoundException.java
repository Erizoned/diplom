package com.college.receipt.exceptions;

public class recipesNotFoundException extends RuntimeException{
    public recipesNotFoundException(String message){
        super(message);
    }
}
