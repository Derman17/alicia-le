package me.yurboirene.alicia_le;

public class InsufficientPermissionsException extends Exception {
    public InsufficientPermissionsException() {}

    public InsufficientPermissionsException(String message) {
        super(message);
    }
}
