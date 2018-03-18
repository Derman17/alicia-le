package me.yurboirene.alicia_le;

public class InsufficientPremissionsException extends Exception {
    public InsufficientPremissionsException() {}

    public InsufficientPremissionsException(String message) {
        super(message);
    }
}
