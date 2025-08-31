package com.prog3.security.Repositories;

import com.prog3.security.Models.NegocioInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NegocioInfoRepository extends MongoRepository<NegocioInfo, String> {
    // No additional methods needed for basic operations
}
