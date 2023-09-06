package com.havd.cloudsearch.ws.model.req;

import lombok.Data;

import java.util.Set;

@Data
public class AddProjectRequest {
    private Set<String> projects;
}
