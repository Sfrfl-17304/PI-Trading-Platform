package com.crypto.order_service.client;

import com.crypto.order_service.model.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${app.feign.user-service.url}")
public interface UserServiceClient {
    @GetMapping("/api/users/{userId}")
    UserProfileResponse getUserProfile(@PathVariable Long userId);
}