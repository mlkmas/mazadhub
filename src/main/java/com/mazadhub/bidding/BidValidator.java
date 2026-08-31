package com.mazadhub.bidding;

import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.pricing.PriceIncrementRules;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

// Decides whether a proposed amount is a legal bid on an auction
public final class BidValidator
{
    private final PriceIncrementRules rules;

    // Validates against the given price-step table
    public BidValidator(PriceIncrementRules rules)
    {
        this.rules=Objects.requireNonNull(rules, "rules");
    }

    // Throws if the auction is closed, or if the amount is under the minimum next bid
    public void validate(AuctionSnapshot snapshot, BigDecimal amount, Instant now)
    {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(now, "now");

        if(!snapshot.isOpenAt(now))
        {
            throw new AuctionClosedException(
                    "Auction is not open for bidding (status="+snapshot.status()
                            +", endTime="+snapshot.endTime()+")");
        }

        BigDecimal minNext=rules.minNextBid(snapshot.currentPrice());
        if(amount.compareTo(minNext)<0)
        {
            throw new BidTooLowException(amount, minNext);
        }
    }

    // Same check as validate(), but answers true/false instead of throwing
    public boolean isValid(AuctionSnapshot snapshot, BigDecimal amount, Instant now)
    {
        try
        {
            validate(snapshot, amount, now);
            return true;
        }
        catch(AuctionClosedException|BidTooLowException e)
        {
            return false;
        }
    }
}
