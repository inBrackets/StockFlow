package com.stockflow.portfolioservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {
    private Long id;
    private String stockSymbol;
    private Integer quantity;
    private BigDecimal priceAtTrade;
    private String tradeType;
    private LocalDateTime tradedAt;
}
