package com.prog3.security.Repositories;

import com.prog3.security.Models.User;
import com.prog3.security.Models.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRoleRepository extends MongoRepository<UserRole, String> {

    @Query("{ 'user._id' : ?0 }")
    public List<UserRole> getRolesByUserId(String userId);

    @Query("{ 'role._id' : ?0 }")
    List<UserRole> getUsersByRoleId(String roleId);
    
    @Query("{ 'user._id' : ?#{#user._id} }")
    List<UserRole> findByUser(User user);
}
