package com.eduardo.HoneyPot.repository;

import com.eduardo.HoneyPot.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    List<Notification> findByReadOrderByTimestampDesc(boolean read);
    
    List<Notification> findByTypeOrderByTimestampDesc(String type);
    
    List<Notification> findByCategoryOrderByTimestampDesc(String category);
    
    List<Notification> findByPriorityGreaterThanEqualOrderByTimestampDesc(int priority);
    
    List<Notification> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("{'read': false, 'priority': {$gte: ?0}}")
    List<Notification> findUnreadHighPriorityNotifications(int minPriority);
    
    @Query("{'read': false}")
    List<Notification> findUnreadNotifications();
    
    long countByRead(boolean read);
    
    long countByType(String type);
    
    long countByCategory(String category);
    
    long countByPriorityGreaterThanEqual(int priority);
    
    @Query("{'timestamp': {$gte: ?0}}")
    long countRecentNotifications(LocalDateTime since);
    
    // Buscar notificações por IP específico
    List<Notification> findBySourceIpOrderByTimestampDesc(String sourceIp);
    
    // Buscar notificações por protocolo
    List<Notification> findByProtocolOrderByTimestampDesc(String protocol);
    
    // Buscar notificações acionáveis
    List<Notification> findByActionableOrderByTimestampDesc(boolean actionable);
}
