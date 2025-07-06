package com.prog3.security.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.prog3.security.Models.Session;

public interface SessionRepository extends MongoRepository<Session, String> {

    @Query("{'user.$id': ObjectId(?0)}")
    public List<Session> getSessionByUser(String userId);

    // @Query(value = "{'user.$id': ObjectId(?0)}", fields = "{'timesErrorValidationCode': 1}")
    // List<Session> findSessionsByUser(String userId);
    @Query("{ 'validationCode': ?0 }")
    Session findByValidationCode(String validationCode);
}
