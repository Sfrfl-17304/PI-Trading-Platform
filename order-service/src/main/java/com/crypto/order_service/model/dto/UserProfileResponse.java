package com.crypto.order_service.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileResponse {
    private Long id;
    private BigDecimal balance;   // on ne récupère que le solde
    // les autres champs sont ignorés
}