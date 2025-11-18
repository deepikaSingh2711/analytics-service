# ebaazee Analytics Service AI Instructions

## Project Overview
This is a Spring Boot 3.5.7 microservice that provides analytics for an auction platform. It processes bid events, maintains aggregated data, and exposes analytics endpoints. The service operates on Java 17 with Maven build tool.

## Architecture & Service Boundaries

### Core Components
- **Event Processing**: `EventsController` receives auction/bid events via REST API
- **Analytics Service**: `AnalyticsService` handles event processing and analytics queries
- **Data Layer**: JPA entities (`AuctionEntity`, `BidEntity`) with H2 in-memory database
- **External Integration**: `UserClientService` calls user service via WebClient (reactive)

### Data Flow Pattern
1. External services POST events to `/api/v1/events/` endpoints
2. Events are processed transactionally, updating both raw data (bids) and aggregates (auctions)
3. Analytics endpoints serve pre-computed aggregations for performance

## Key Patterns & Conventions

### Package Structure
Follow strict layered architecture: `controller` → `service` → `repository` → `model`
- DTOs in `dto` package for API contracts
- Configuration classes in `config` package
- All classes use the corrected package name `analytics_service` (not `analytics-service`)

### Event Processing Pattern
Events use dedicated DTOs (`NewBidEventDto`, `AuctionStatusEventDto`) and update both:
- Raw data storage (individual bids/auctions)
- Aggregated analytics data (totals, min/max values)

Example for new features:
```java
@Transactional
public void processNewEvent(EventDto event) {
    // 1. Save raw data
    // 2. Update aggregations
    // 3. Save updated aggregates
}
```

### External Service Integration
Use reactive WebClient with fallback patterns:
- Configure base URL via `application.yaml` properties
- Handle failures gracefully (return "Unknown" for user lookups)
- Use `@Value` injection for configuration

### Data Aggregation Strategy
Pre-compute analytics during event processing rather than on-demand:
- Maintain running totals (`totalBids`, `sumBids`)
- Track min/max values (`highestBid`, `lowestBid`)
- Calculate derived values (averages) in service methods

## Development Workflow

### Running the Service
```bash
# Standard Spring Boot Maven commands
./mvnw spring-boot:run          # Start on port 8085
./mvnw clean package           # Build JAR
./mvnw test                   # Run tests
```

### Database Access
- H2 console available at `http://localhost:8085/h2-console`
- Database URL: `jdbc:h2:mem:analyticsdb`
- Username: `sa`, no password

### API Testing
Key endpoints for testing:
- POST `/api/v1/events/new-bid` - Process bid events
- POST `/api/v1/events/auction-status` - Update auction status
- GET `/api/v1/analytics/top-bidders?limit=N` - Top bidders by total amount
- GET `/api/v1/analytics/popular-auctions?limit=N` - Auctions by bid count

### Dependencies Configuration
The service expects a user service running on `localhost:8080` (configurable via `user.service.base-url`). For isolated testing, mock this dependency or update configuration.

## Code Style & Implementation Notes

### Lombok Usage
All DTOs and entities use `@Data` annotation. When creating new classes, follow this pattern for consistency.

### Error Handling
Current implementation uses simple patterns:
- Optional returns for not-found scenarios
- Exception handling in external service calls with fallbacks
- HTTP status codes: 201 for created events, 404 for missing resources

### Testing Strategy
Minimal test coverage currently exists. When adding tests:
- Use `@SpringBootTest` for integration tests
- Mock external dependencies (UserClientService)
- Test both event processing and analytics endpoints