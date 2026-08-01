package com.mazadhub.exception;
import java.math.BigDecimal;

/**
 * Thrown when a submitted bid doesn't  meet the minimum next bid requirement
 * (current price plus the minimum increment for that price).
 */
public class BidTooLowException extends RuntimeException
{

    private final BigDecimal submitted;
    private final BigDecimal minRequired;

    public BidTooLowException(BigDecimal submitted, BigDecimal minRequired)
    {
        super("Bid " + submitted + " is below the minimum required bid of " + minRequired);
        this.submitted=submitted;
        this.minRequired=minRequired;
    }

    public BigDecimal getSubmitted()
    {
        return submitted;
    }

    public BigDecimal getMinRequired()
    {
        return minRequired;
    }
}

