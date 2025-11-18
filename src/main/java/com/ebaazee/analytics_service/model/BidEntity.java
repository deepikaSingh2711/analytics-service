package com.ebaazee.analytics_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "bids")
@Data
public class BidEntity {
    @Id
    private String bidId;
    private String auctionId;
    private String userId;
    private double amount;
    private Instant timestamp;
}
