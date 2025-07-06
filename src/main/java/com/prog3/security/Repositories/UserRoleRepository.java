package com.prog3.security.Repositories;

import com.prog3.security.Models.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    @Query("{ 'user.$id' : ObjectId(?0) }")
    public List<UserRole> getRolesByUserId(String userId);

    @Query("{ 'role.$id' : ObjectId(?0) }")
    List<UserRole> getUsersByRoleId(String roleId);
}
