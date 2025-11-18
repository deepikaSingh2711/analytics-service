package com.ebaazee.analytics_service.repository;

import com.ebaazee.analytics_service.model.AuctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<AuctionEntity, String> {
}
