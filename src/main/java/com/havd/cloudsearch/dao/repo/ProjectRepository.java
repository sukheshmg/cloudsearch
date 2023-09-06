package com.havd.cloudsearch.dao.repo;

import com.havd.cloudsearch.dao.model.Project;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, String> {
}
