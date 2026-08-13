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

/**
 * Small formatting helpers usable directly from the pages, e.g.
 * {@code #{fmt.money(item.currentPrice)}} or {@code #{fmt.timeLeft(item.endDate)}}.
 */
@Named("fmt")
@ApplicationScoped
public class Format {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.##");
    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /** e.g. 1250 -> "1,250 ILS". */
    public String money(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return MONEY.format(value) + " ILS";
    }

    /** e.g. "2026-06-18 14:30". */
    public String dateTime(Instant when) {
        return when == null ? "-" : DT.format(when);
    }

    /** e.g. "2d 4h", "5h 12m", or "Ended". */
    public String timeLeft(Instant end) {
        if (end == null) {
            return "-";
        }
        Duration d = Duration.between(Instant.now(), end);
        if (d.isNegative() || d.isZero()) {
            return "Ended";
        }
        long days = d.toDays();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
