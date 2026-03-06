package com.stockflow.marketservice.repository;

import com.stockflow.marketservice.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByStockIdOrderByRecordedAtDesc(Long stockId);
}
