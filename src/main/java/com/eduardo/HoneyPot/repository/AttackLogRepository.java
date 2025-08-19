package com.eduardo.HoneyPot.repository;

import com.eduardo.HoneyPot.model.AttackLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttackLogRepository extends MongoRepository<AttackLog, String> {
    
    List<AttackLog> findBySourceIpOrderByTimestampDesc(String sourceIp);
    
    List<AttackLog> findByProtocolOrderByTimestampDesc(String protocol);
    
    List<AttackLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("{'sourceIp': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<AttackLog> findBySourceIpAndTimestampBetween(String sourceIp, LocalDateTime start, LocalDateTime end);
    
    @Query("{'username': ?0}")
    List<AttackLog> findByUsername(String username);
    
    @Query("{'sourceIp': ?0, 'protocol': ?1}")
    List<AttackLog> findBySourceIpAndProtocol(String sourceIp, String protocol);
    
    long countBySourceIp(String sourceIp);
    
    long countByProtocol(String protocol);
}
