package com.prog3.security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.prog3.security.Models.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    @Query("{ 'email': ?0 }")
    User getUserByEmail(String email);
}
