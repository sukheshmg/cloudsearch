package com.havd.cloudsearch.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Channel {
    @Id
    @NonNull
    private String canonicalName;

    @NonNull
    private String accessToken;
    @NonNull
    private String refreshToken;

    @ManyToMany
    private Set<Project> projects;
}
