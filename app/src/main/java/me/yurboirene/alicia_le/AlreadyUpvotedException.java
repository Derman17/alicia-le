package me.yurboirene.alicia_le;

public class AlreadyUpvotedException extends Exception {

    public AlreadyUpvotedException() {}

    public AlreadyUpvotedException(String message) {
        super(message);
    }
}
