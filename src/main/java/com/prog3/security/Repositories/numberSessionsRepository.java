package com.prog3.security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.prog3.security.Models.numberSessions;

public interface numberSessionsRepository extends MongoRepository<numberSessions, String> {

}
