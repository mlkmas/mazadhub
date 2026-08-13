package com.mazadhub.api.dto;

import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.service.BidOutcome;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the REST read/response models. They pin down two things:
 * the plain field mapping, and the confidentiality rules (seller and bidder
 * identity are never in the public DTOs; the winner appears only once SOLD).
 */
class DtoMappingTest {

    private User seller() {
        User u = new User("seller1", "hash", UserRole.USER);
        u.setFullName("Seller One");
        u.setEmail("seller@example.com");
        u.setPhone("050-1111111");
        return TestIds.withId(u, 1L);
    }

    private User winner() {
        User u = new User("dave", "hash", UserRole.USER);
        u.setFullName("Dave Cohen");
        u.setEmail("dave@example.com");
        u.setPhone("052-1234567");
        return TestIds.withId(u, 2L);
    }

    private Category category() {
        return TestIds.withId(new Category("Collectibles", "old stuff"), 7L);
    }

    private Item item() {
        Item i = new Item(seller(), category(), "Canon AE-1",
                new BigDecimal("250.00"), Instant.now().plus(2, ChronoUnit.DAYS));
        i.setDescription("Vintage camera");
        i.setImageUrl("https://img/x.jpg");
        i.setBuyNowPrice(new BigDecimal("600.00"));
        i.setCurrentPrice(new BigDecimal("340.00"));
        return TestIds.withId(i, 104L);
    }

    @Test
    void itemSummary_maps_fields_and_hides_seller() {
        ItemSummaryDTO dto = ItemSummaryDTO.from(item(), 7);
        assertEquals(104L, dto.id());
        assertEquals("Canon AE-1", dto.title());
        assertEquals("Collectibles", dto.categoryName());
        assertEquals(new BigDecimal("340.00"), dto.currentPrice());
        assertEquals(new BigDecimal("600.00"), dto.buyNowPrice());
        assertEquals("ACTIVE", dto.status());
        assertEquals(7, dto.bidCount());
        // Confidentiality: the DTO has no seller field at all.
        assertFalse(java.util.Arrays.stream(ItemSummaryDTO.class.getRecordComponents())
                .anyMatch(c -> c.getName().toLowerCase().contains("seller")));
    }

    @Test
    void itemDetail_computes_minNextBid_from_current_price() {
        ItemDetailDTO dto = ItemDetailDTO.from(item(), 3);
        // current 340 is in the 100..1000 tier → increment 10 → min next 350.
        assertEquals(0, dto.minNextBid().compareTo(new BigDecimal("350")));
        assertEquals("Vintage camera", dto.description());
        assertEquals(3, dto.bidCount());
    }

    @Test
    void bidDto_hides_bidder_identity() {
        Bid b = new Bid(item(), winner(), new BigDecimal("340.00"), true);
        BidDTO dto = BidDTO.from(b);
        assertEquals(new BigDecimal("340.00"), dto.amount());
        assertTrue(dto.auto());
        assertFalse(java.util.Arrays.stream(BidDTO.class.getRecordComponents())
                .anyMatch(c -> c.getName().toLowerCase().contains("bidder")));
    }

    @Test
    void bidOutcome_maps_actorLeading_but_not_leaderId() {
        BidOutcome outcome = new BidOutcome(new BigDecimal("340.00"), 2L, true, ItemStatus.ACTIVE);
        BidOutcomeDTO dto = BidOutcomeDTO.from(outcome);
        assertTrue(dto.youAreLeading());
        assertEquals("ACTIVE", dto.status());
        // Leader's id is not exposed.
        assertFalse(java.util.Arrays.stream(BidOutcomeDTO.class.getRecordComponents())
                .anyMatch(c -> c.getName().toLowerCase().contains("leader")));
    }

    @Test
    void sellerResult_reveals_winner_only_when_sold() {
        Item active = item();
        SellerResultDTO before = SellerResultDTO.from(active);
        assertFalse(before.sold());
        assertNull(before.winnerName());
        assertNull(before.winnerEmail());

        active.setWinner(winner());
        active.setStatus(ItemStatus.SOLD);
        SellerResultDTO after = SellerResultDTO.from(active);
        assertTrue(after.sold());
        assertEquals("Dave Cohen", after.winnerName());
        assertEquals("dave@example.com", after.winnerEmail());
        assertEquals("052-1234567", after.winnerPhone());
    }

    @Test
    void userDto_never_carries_password_hash() {
        UserDTO dto = UserDTO.from(seller());
        assertEquals("seller1", dto.username());
        assertEquals("USER", dto.role());
        assertFalse(java.util.Arrays.stream(UserDTO.class.getRecordComponents())
                .anyMatch(c -> c.getName().toLowerCase().contains("password")));
    }
}
