package com.example.notificationservice.service;
import com.example.notificationservice.model.Notification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "user_notifications:";
    private static final long MAX_NOTIFS = 50;

    public NotificationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveNotification(String userId, String message, String type) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(), userId, message, type, System.currentTimeMillis()
        );
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForList().leftPush(key, notification);
        redisTemplate.opsForList().trim(key, 0, MAX_NOTIFS - 1);
    }

    public List<Notification> getRecentNotifications(String userId) {
        String key = KEY_PREFIX + userId;
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null) return List.of();
        return raw.stream().map(obj -> (Notification) obj).collect(Collectors.toList());
    }
}
