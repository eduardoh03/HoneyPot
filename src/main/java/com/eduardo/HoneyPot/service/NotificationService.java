package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.Notification;
import com.eduardo.HoneyPot.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * Cria uma nova notificação
     */
    public Notification createNotification(String type, String category, String title, String message) {
        try {
            Notification notification = new Notification(type, category, title, message);
            Notification saved = notificationRepository.save(notification);
            log.info("Notificação criada: {} - {}", type, title);
            return saved;
        } catch (Exception e) {
            log.error("Erro ao criar notificação: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar notificação", e);
        }
    }
    
    /**
     * Cria uma notificação relacionada a um ataque
     */
    public Notification createAttackNotification(String type, String title, String message, String sourceIp, String protocol, String username) {
        try {
            Notification notification = new Notification(type, "ATTACK", title, message, sourceIp, protocol);
            notification.setUsername(username);
            Notification saved = notificationRepository.save(notification);
            log.info("Notificação de ataque criada: {} - {} de {}", type, title, sourceIp);
            return saved;
        } catch (Exception e) {
            log.error("Erro ao criar notificação de ataque: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar notificação de ataque", e);
        }
    }
    
    /**
     * Cria uma notificação do sistema
     */
    public Notification createSystemNotification(String type, String title, String message) {
        return createNotification(type, "SYSTEM", title, message);
    }
    
    /**
     * Cria uma notificação de segurança
     */
    public Notification createSecurityNotification(String type, String title, String message) {
        return createNotification(type, "SECURITY", title, message);
    }
    
    /**
     * Busca todas as notificações com paginação
     */
    public Map<String, Object> getAllNotifications(int page, int size) {
        try {
            List<Notification> allNotifications = notificationRepository.findAll();
            
            if (allNotifications.isEmpty()) {
                return Map.of(
                    "notifications", List.of(),
                    "totalPages", 0,
                    "totalElements", 0L,
                    "currentPage", page,
                    "size", size,
                    "timestamp", LocalDateTime.now()
                );
            }
            
            // Implementar paginação manual
            int start = page * size;
            int end = Math.min(start + size, allNotifications.size());
            List<Notification> pageContent = allNotifications.subList(start, end);
            
            int totalPages = (int) Math.ceil((double) allNotifications.size() / size);
            
            return Map.of(
                "notifications", pageContent,
                "totalPages", totalPages,
                "totalElements", (long) allNotifications.size(),
                "currentPage", page,
                "size", size,
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar notificações: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações", e);
        }
    }
    
    /**
     * Busca notificações não lidas
     */
    public List<Notification> getUnreadNotifications() {
        try {
            return notificationRepository.findUnreadNotifications();
        } catch (Exception e) {
            log.error("Erro ao buscar notificações não lidas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações não lidas", e);
        }
    }
    
    /**
     * Busca notificações de alta prioridade
     */
    public List<Notification> getHighPriorityNotifications() {
        try {
            return notificationRepository.findUnreadHighPriorityNotifications(3);
        } catch (Exception e) {
            log.error("Erro ao buscar notificações de alta prioridade: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações de alta prioridade", e);
        }
    }
    
    /**
     * Busca notificações por tipo
     */
    public List<Notification> getNotificationsByType(String type) {
        try {
            return notificationRepository.findByTypeOrderByTimestampDesc(type);
        } catch (Exception e) {
            log.error("Erro ao buscar notificações por tipo {}: {}", type, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações por tipo: " + type, e);
        }
    }
    
    /**
     * Busca notificações por categoria
     */
    public List<Notification> getNotificationsByCategory(String category) {
        try {
            return notificationRepository.findByCategoryOrderByTimestampDesc(category);
        } catch (Exception e) {
            log.error("Erro ao buscar notificações por categoria {}: {}", category, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações por categoria: " + category, e);
        }
    }
    
    /**
     * Marca uma notificação como lida
     */
    public void markAsRead(String id) {
        try {
            Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada: " + id));
            
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Notificação marcada como lida: {}", id);
        } catch (Exception e) {
            log.error("Erro ao marcar notificação como lida {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao marcar notificação como lida", e);
        }
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    public void markAllAsRead() {
        try {
            List<Notification> unreadNotifications = notificationRepository.findUnreadNotifications();
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
            log.info("Todas as notificações foram marcadas como lidas");
        } catch (Exception e) {
            log.error("Erro ao marcar todas as notificações como lidas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao marcar todas as notificações como lidas", e);
        }
    }
    
    /**
     * Busca estatísticas das notificações
     */
    public Map<String, Object> getNotificationStats() {
        try {
            long totalNotifications = notificationRepository.count();
            long unreadNotifications = notificationRepository.countByRead(false);
            long highPriorityNotifications = notificationRepository.countByPriorityGreaterThanEqual(3);
            long criticalNotifications = notificationRepository.countByPriorityGreaterThanEqual(4);
            long recentNotifications = notificationRepository.countRecentNotifications(LocalDateTime.now().minusHours(24));
            
            return Map.of(
                "totalNotifications", totalNotifications,
                "unreadNotifications", unreadNotifications,
                "highPriorityNotifications", highPriorityNotifications,
                "criticalNotifications", criticalNotifications,
                "recentNotifications", recentNotifications,
                "timestamp", LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas das notificações: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar estatísticas das notificações", e);
        }
    }
    
    /**
     * Remove notificações antigas (mais de 30 dias)
     */
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            List<Notification> oldNotifications = notificationRepository.findByTimestampBetweenOrderByTimestampDesc(
                LocalDateTime.MIN, cutoff);
            
            if (!oldNotifications.isEmpty()) {
                notificationRepository.deleteAll(oldNotifications);
                log.info("{} notificações antigas foram removidas", oldNotifications.size());
            }
        } catch (Exception e) {
            log.error("Erro ao limpar notificações antigas: {}", e.getMessage(), e);
        }
    }
}
