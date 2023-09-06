package com.havd.cloudsearch.dao.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
public class Project {
    @Id
    private String canonicalName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_channels", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private Set<Channel> channels;
}
