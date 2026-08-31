package com.mazadhub.web.support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// Formatting helpers the XHTML pages call directly, for example #{fmt.money(item.currentPrice)}
@Named("fmt")
@ApplicationScoped
public class Format
{
    // shared number and date patterns
    private static final DecimalFormat MONEY=new DecimalFormat("#,##0.##");
    private static final DateTimeFormatter DT=
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    // A price with thousands separators, for example 1250 -> "1,250 ILS"
    public String money(BigDecimal value)
    {
        if(value==null)
        {
            return "-";
        }

        return MONEY.format(value)+" ILS";
    }

    // A timestamp as "yyyy-MM-dd HH:mm" in the server time zone
    public String dateTime(Instant when)
    {
        return when==null?"-":DT.format(when);
    }

    // How long an auction still has to run, for example "2d 4h" or "Ended"
    public String timeLeft(Instant end)
    {
        if(end==null)
        {
            return "-";
        }

        Duration d=Duration.between(Instant.now(), end);
        if(d.isNegative()||d.isZero())
        {
            return "Ended";
        }

        long days=d.toDays();
        long hours=d.toHoursPart();
        long minutes=d.toMinutesPart();
        if(days>0)
        {
            return days+"d "+hours+"h";
        }

        if(hours>0)
        {
            return hours+"h "+minutes+"m";
        }

        return minutes+"m";
    }
}
