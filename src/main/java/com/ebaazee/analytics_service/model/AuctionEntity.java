package com.ebaazee.analytics_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "auctions")
@Data
public class AuctionEntity {
    @Id
    private String auctionId;
    private String title;
    private long totalBids;
    private double highestBid;
    private double lowestBid;
    private double sumBids; // to compute average
    private String status;
    private String startTime;
    private String endTime;
}
