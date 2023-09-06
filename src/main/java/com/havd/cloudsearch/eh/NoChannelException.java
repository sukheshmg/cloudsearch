package com.havd.cloudsearch.eh;

public class NoChannelException extends Exception {
    public NoChannelException(String channel) {
        super("channel " + channel + " doesn't exist");
    }
}
