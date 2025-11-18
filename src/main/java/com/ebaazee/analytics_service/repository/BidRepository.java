package com.ebaazee.analytics_service.repository;

import com.ebaazee.analytics_service.model.BidEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<BidEntity, String> {
    List<BidEntity> findAllByAuctionId(String auctionId);
}
