package com.mazadhub.bidding;

import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.pricing.PriceIncrementRules;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Validates whether a proposed bid amount is acceptable for a given auction
 */
public final class BidValidator
{

    private final PriceIncrementRules rules;

    public BidValidator(PriceIncrementRules rules)
    {
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    /**
     * @param snapshot current auction state
     * @param amount   the proposed bid amount
     * @param now      the moment the bid is evaluated
     * @throws AuctionClosedException if the auction is not open for bidding
     * @throws BidTooLowException     if the amount is below the minimum next bid
     */
    public void validate(AuctionSnapshot snapshot, BigDecimal amount, Instant now) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(now, "now");

        if (!snapshot.isOpenAt(now)) {
            throw new AuctionClosedException(
                    "Auction is not open for bidding (status=" + snapshot.status()
                            + ", endTime=" + snapshot.endTime() + ")");
        }
        BigDecimal minNext = rules.minNextBid(snapshot.currentPrice());
        if (amount.compareTo(minNext) < 0) {
            throw new BidTooLowException(amount, minNext);
        }
    }


    public boolean isValid(AuctionSnapshot snapshot, BigDecimal amount, Instant now)
    {
        try {
            validate(snapshot, amount, now);
            return true;
        } catch (AuctionClosedException | BidTooLowException e)
        {

            return false;
        }
    }
}
