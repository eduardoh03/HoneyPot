package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.Notification;
import com.eduardo.HoneyPot.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
     * Busca todas as notificações com paginação e ordenação
     */
    public Map<String, Object> getAllNotifications(int page, int size, String sortField, String sortDirection) {
        try {
            // Configurar ordenação
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
            String field = (sortField != null && !sortField.isEmpty()) ? sortField : "timestamp";
            Sort sort = Sort.by(direction, field);
            
            // Criar Pageable
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Buscar com paginação
            Page<Notification> notificationPage = notificationRepository.findAll(pageable);
            
            return Map.of(
                "notifications", notificationPage.getContent(),
                "totalPages", notificationPage.getTotalPages(),
                "totalElements", notificationPage.getTotalElements(),
                "currentPage", notificationPage.getNumber(),
                "size", notificationPage.getSize(),
                "sortField", field,
                "sortDirection", sortDirection,
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar notificações: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações", e);
        }
    }
    
    /**
     * Busca notificações com filtros, ordenação e paginação
     */
    public Map<String, Object> getFilteredNotifications(int page, int size, String type, String category, String sortField, String sortDirection) {
        try {
            List<Notification> filteredNotifications = new ArrayList<>();
            
            // Aplicar filtros
            if (type != null && !type.isEmpty() && category != null && !category.isEmpty()) {
                // Filtro por tipo E categoria
                filteredNotifications = notificationRepository.findByTypeAndCategoryOrderByTimestampDesc(type, category);
            } else if (type != null && !type.isEmpty()) {
                // Filtro apenas por tipo
                filteredNotifications = notificationRepository.findByTypeOrderByTimestampDesc(type);
            } else if (category != null && !category.isEmpty()) {
                // Filtro apenas por categoria
                filteredNotifications = notificationRepository.findByCategoryOrderByTimestampDesc(category);
            } else {
                // Sem filtros - usar método principal
                return getAllNotifications(page, size, sortField, sortDirection);
            }
            
            if (filteredNotifications.isEmpty()) {
                return Map.of(
                    "notifications", List.of(),
                    "totalPages", 0,
                    "totalElements", 0L,
                    "currentPage", page,
                    "size", size,
                    "type", type != null ? type : "",
                    "category", category != null ? category : "",
                    "sortField", sortField != null ? sortField : "timestamp",
                    "sortDirection", sortDirection != null ? sortDirection : "desc",
                    "timestamp", LocalDateTime.now()
                );
            }
            
            // Aplicar ordenação
            filteredNotifications = sortNotifications(filteredNotifications, sortField, sortDirection);
            
            // Implementar paginação manual
            int start = page * size;
            int end = Math.min(start + size, filteredNotifications.size());
            List<Notification> pageContent = start < filteredNotifications.size() ? 
                filteredNotifications.subList(start, end) : List.of();
            
            int totalPages = (int) Math.ceil((double) filteredNotifications.size() / size);
            
            return Map.of(
                "notifications", pageContent,
                "totalPages", totalPages,
                "totalElements", (long) filteredNotifications.size(),
                "currentPage", page,
                "size", size,
                "type", type != null ? type : "",
                "category", category != null ? category : "",
                "sortField", sortField != null ? sortField : "timestamp",
                "sortDirection", sortDirection != null ? sortDirection : "desc",
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar notificações filtradas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar notificações filtradas", e);
        }
    }
    
    /**
     * Aplica ordenação nas notificações
     */
    private List<Notification> sortNotifications(List<Notification> notifications, String sortField, String sortDirection) {
        // Validação de entrada
        if (notifications == null || notifications.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Valores padrão
        if (sortField == null || sortField.isEmpty()) {
            sortField = "timestamp";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        
        // Criar uma nova lista para não modificar a original
        List<Notification> sortedList = new ArrayList<>(notifications);
        
        try {
            // Aplicar ordenação baseada no campo
            switch (sortField.toLowerCase()) {
                case "timestamp":
                    sortedList.sort((n1, n2) -> {
                        if (n1.getTimestamp() == null && n2.getTimestamp() == null) return 0;
                        if (n1.getTimestamp() == null) return 1;
                        if (n2.getTimestamp() == null) return -1;
                        return n1.getTimestamp().compareTo(n2.getTimestamp());
                    });
                    break;
                    
                case "type":
                    sortedList.sort((n1, n2) -> {
                        String t1 = n1.getType() != null ? n1.getType() : "";
                        String t2 = n2.getType() != null ? n2.getType() : "";
                        return t1.compareTo(t2);
                    });
                    break;
                    
                case "category":
                    sortedList.sort((n1, n2) -> {
                        String c1 = n1.getCategory() != null ? n1.getCategory() : "";
                        String c2 = n2.getCategory() != null ? n2.getCategory() : "";
                        return c1.compareTo(c2);
                    });
                    break;
                    
                case "priority":
                    sortedList.sort((n1, n2) -> {
                        int p1 = n1.getPriority();
                        int p2 = n2.getPriority();
                        return Integer.compare(p1, p2);
                    });
                    break;
                    
                case "read":
                    sortedList.sort((n1, n2) -> {
                        boolean r1 = n1.isRead();
                        boolean r2 = n2.isRead();
                        return Boolean.compare(r1, r2);
                    });
                    break;
                    
                default:
                    // Fallback para timestamp
                    sortedList.sort((n1, n2) -> {
                        if (n1.getTimestamp() == null && n2.getTimestamp() == null) return 0;
                        if (n1.getTimestamp() == null) return 1;
                        if (n2.getTimestamp() == null) return -1;
                        return n1.getTimestamp().compareTo(n2.getTimestamp());
                    });
                    break;
            }
            
            // Aplicar direção da ordenação
            if ("desc".equalsIgnoreCase(sortDirection)) {
                java.util.Collections.reverse(sortedList);
            }
            
            return sortedList;
            
        } catch (Exception e) {
            log.error("Erro ao ordenar notificações: {}", e.getMessage(), e);
            return new ArrayList<>(notifications); // Retornar lista original em caso de erro
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
