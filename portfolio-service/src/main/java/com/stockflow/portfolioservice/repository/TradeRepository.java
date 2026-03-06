package com.stockflow.portfolioservice.repository;

import com.stockflow.portfolioservice.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByPortfolioIdOrderByTradedAtDesc(Long portfolioId);
}
