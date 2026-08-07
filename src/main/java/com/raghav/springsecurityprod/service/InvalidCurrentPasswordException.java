package com.raghav.springsecurityprod.service;

public class InvalidCurrentPasswordException extends Throwable {
    public InvalidCurrentPasswordException(String currentPasswordIsIncorrect) {
    }
}
