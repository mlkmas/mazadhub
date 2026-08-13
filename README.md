# mazadhub — Electronic Auction Marketplace

A Jakarta EE web application implementing an online auction (eBay-style),
built for the Java software-engineering course project.

## Technologies
- **Jakarta Faces (JSF)** — web UI
- **JAX-RS (REST)** — service API
- **JPA / Hibernate** — persistence (MySQL)
- **JMS** — real-time bid updates and auction-close notifications

## Status

### Day 1 (complete) — auction logic core

Pure, framework-free, fully unit-tested:

| Component | Responsibility |
|-----------|----------------|
| `pricing/PriceIncrementRules` | Minimum bid increment by configurable price tier |
| `bidding/AuctionSnapshot` | Immutable view of auction state for validation |
| `bidding/BidValidator` | Rejects bids that are too low or on closed auctions |
| `bidding/StandingBid` | A bidder's standing maximum (manual or proxy) |
| `bidding/ProxyBidEngine` | Resolves winner + current price (automatic bidding) |
| `bidding/BidResolution` | Result of the proxy-bid engine |
| `domain/ItemStatus`, `domain/UserRole` | Core enums |
| `exception/*` | `BidTooLowException`, `AuctionClosedException` |

**30 unit tests, all passing.**

### Day 2 (complete) — persistence layer

JPA entities (Jakarta Persistence 3.x) mapped to the ERD, plus repositories:

| Entity | Table | Notes |
|--------|-------|-------|
| `domain/User` | `users` | unique username, role enum |
| `domain/Category` | `categories` | unique name |
| `domain/Item` | `items` | **`@Version` optimistic locking**, FKs to seller/category/winner |
| `domain/Bid` | `bids` | amount, time, auto flag |
| `domain/AutoBid` | `auto_bids` | hidden max, unique per (item, bidder) |

| Repository | Highlights |
|------------|-----------|
| `repository/AbstractRepository<T>` | generic find/save/delete |
| `repository/UserRepository` | `findByUsername`, `existsByUsername` |
| `repository/CategoryRepository` | `findAll` |
| `repository/ItemRepository` | `findByIdForUpdate` (pessimistic lock), active/search/expired queries |
| `repository/BidRepository` | bid history, count |
| `repository/AutoBidRepository` | active proxy bids per item |

Config: `src/main/resources/META-INF/persistence.xml` (JTA unit `mazadhubPU`,
data source `jdbc/mazadhub`, MySQL).

> Concurrency: money is `BigDecimal`; concurrent bids on the same item are made
> safe by the item `@Version` field (optimistic) and `findByIdForUpdate`
> (pessimistic), wired together in the Day 3 service layer.

### Day 3 (complete) — service layer

Business operations, wiring the Day 1 logic to the Day 2 repositories:

| Component | Responsibility |
|-----------|----------------|
| `security/PasswordHasher` | PBKDF2 salted password hashing (pure JDK) |
| `notification/NotificationPort` | abstraction for auction events (JMS impl comes later) |
| `notification/LoggingNotificationService` | default logging implementation |
| `service/UserService` | register (hashed) + login |
| `service/ItemService` | list for sale, browse, search, details |
| `service/BiddingService` | **place bid / proxy bid / buy-now**, end to end |
| `service/BidOutcome` | result of a bidding action |

**Bidding model:** every bid is the bidder's maximum; the proxy engine resolves
the standing maxima into the leader and current price after each action (winner
pays just enough to beat the runner-up). `placeBid` and `placeAutoBid` share one
core. The item is loaded with a pessimistic lock and updated in one transaction.

**18 more unit tests (48 total), all passing** — including the full bidding flow
verified with in-memory fakes (proxy auto-counter, competing maxima, buy-now,
too-low / closed-auction rejections).

## Build & test (locally, with Maven)

```bash
mvn test          # run the unit tests
mvn package       # build the deployable WAR (target/mazadhub.war)
```

> The first `mvn` run downloads dependencies from Maven Central.

## Roadmap
- ~~Day 2–3: JPA entities + repositories~~ ✓ done
- ~~Day 3–4: service layer (wires the bidding core in, with DB row locking)~~ ✓ done
- Day 5: REST API
- Day 6–8: JSF screens
- Day 9–10: JMS live updates + auction-close scheduler
