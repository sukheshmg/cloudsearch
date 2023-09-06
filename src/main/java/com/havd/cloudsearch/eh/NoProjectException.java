package com.havd.cloudsearch.eh;

public class NoProjectException extends Exception {
    public NoProjectException(String project) {
        super("project " + project + " doesn't exist");
    }
}
