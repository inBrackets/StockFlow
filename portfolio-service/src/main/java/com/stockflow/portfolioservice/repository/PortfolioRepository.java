package com.stockflow.portfolioservice.repository;

import com.stockflow.portfolioservice.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
