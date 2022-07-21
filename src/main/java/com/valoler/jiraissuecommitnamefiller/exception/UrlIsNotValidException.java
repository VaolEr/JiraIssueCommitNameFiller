package com.valoler.jiraissuecommitnamefiller.exception;

import java.util.concurrent.ExecutionException;

public class UrlIsNotValidException extends ExecutionException {

    public UrlIsNotValidException(String message) {
        super(message);
    }
}
