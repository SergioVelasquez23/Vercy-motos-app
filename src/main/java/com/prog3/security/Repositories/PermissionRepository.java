package com.prog3.security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.prog3.security.Models.Permission;

public interface PermissionRepository extends MongoRepository<Permission, String> {

    @Query("{ 'url': ?0, 'method': ?1 }")
    Permission getPermission(String url, String method);
}
