package com.ebaazee.analytics_service.service;

import com.ebaazee.analytics_service.dto.NewBidEventDto;
import com.ebaazee.analytics_service.dto.AuctionStatusEventDto;
import com.ebaazee.analytics_service.model.AuctionEntity;
import com.ebaazee.analytics_service.model.BidEntity;
import com.ebaazee.analytics_service.repository.AuctionRepository;
import com.ebaazee.analytics_service.repository.BidRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserClientService userClientService;

    public AnalyticsService(BidRepository bidRepository,
                            AuctionRepository auctionRepository,
                            UserClientService userClientService) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.userClientService = userClientService;
    }

    @Transactional
    public void processNewBid(NewBidEventDto event) {
        // Save bid
        BidEntity bid = new BidEntity();
        bid.setBidId(event.getBidId());
        bid.setAuctionId(event.getAuctionId());
        bid.setUserId(event.getUserId());
        bid.setAmount(event.getAmount());
        bid.setTimestamp(Instant.parse(event.getTimestamp()));
        bidRepository.save(bid);

        // Update auction aggregates
        AuctionEntity auction = auctionRepository.findById(event.getAuctionId())
                .orElseGet(() -> {
                    AuctionEntity a = new AuctionEntity();
                    a.setAuctionId(event.getAuctionId());
                    a.setTitle(event.getAuctionId()); // placeholder title
                    a.setLowestBid(event.getAmount());
                    return a;
                });

        auction.setTotalBids(auction.getTotalBids() + 1);
        auction.setHighestBid(Math.max(auction.getHighestBid(), event.getAmount()));
        if (auction.getLowestBid() == 0) auction.setLowestBid(event.getAmount());
        auction.setLowestBid(Math.min(auction.getLowestBid(), event.getAmount()));
        auction.setSumBids(auction.getSumBids() + event.getAmount());
        auctionRepository.save(auction);
    }

    @Transactional
    public void processAuctionStatus(AuctionStatusEventDto event) {
        AuctionEntity auction = auctionRepository.findById(event.getAuctionId())
                .orElseGet(() -> {
                    AuctionEntity a = new AuctionEntity();
                    a.setAuctionId(event.getAuctionId());
                    return a;
                });
        auction.setStatus(event.getStatus());
        auction.setStartTime(event.getStartTime());
        auction.setEndTime(event.getEndTime());
        auctionRepository.save(auction);
    }

    public List<Map<String, Object>> getTopBidders(int limit) {
        List<BidEntity> bids = bidRepository.findAll();
        Map<String, List<BidEntity>> grouped = bids.stream()
                .collect(Collectors.groupingBy(BidEntity::getUserId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            String userId = entry.getKey();
            long count = entry.getValue().size();
            double totalAmount = entry.getValue().stream().mapToDouble(BidEntity::getAmount).sum();
            String name = userClientService.getUserName(userId);
            Map<String, Object> m = new HashMap<>();
            m.put("userId", userId);
            m.put("name", name);
            m.put("totalBids", count);
            m.put("totalAmount", totalAmount);
            result.add(m);
        }
        result.sort((a,b) -> Double.compare((double)b.get("totalAmount"), (double)a.get("totalAmount")));
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPopularAuctions(int limit) {
        return auctionRepository.findAll().stream()
                .sorted((a,b) -> Long.compare(b.getTotalBids(), a.getTotalBids()))
                .limit(limit)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("auctionId", a.getAuctionId());
                    m.put("title", a.getTitle());
                    m.put("totalBids", a.getTotalBids());
                    m.put("highestBid", a.getHighestBid());
                    return m;
                }).collect(Collectors.toList());
    }

    public Optional<Map<String,Object>> getAuctionStats(String auctionId) {
        return auctionRepository.findById(auctionId).map(a -> {
            Map<String,Object> m = new HashMap<>();
            m.put("auctionId", a.getAuctionId());
            m.put("title", a.getTitle());
            m.put("totalBids", a.getTotalBids());
            m.put("highestBid", a.getHighestBid());
            m.put("lowestBid", a.getLowestBid());
            double avg = a.getTotalBids() == 0 ? 0 : a.getSumBids() / a.getTotalBids();
            m.put("averageBid", avg);
            m.put("startTime", a.getStartTime());
            m.put("endTime", a.getEndTime());
            return m;
        });
    }
}
