package dev.nikkune.msrisk.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class AgeCalculator {
    private static LocalDate toLocalDate(Date date, ZoneId zoneId){
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Instant instant = date.toInstant();
        return instant.atZone(zoneId).toLocalDate();
    }

    public static int ageInYears(Date birthDate){
        return ageInYears(birthDate, ZoneId.systemDefault());
    }

    public static int ageInYears(Date birthDate, ZoneId zoneId){
        LocalDate birth = toLocalDate(birthDate, zoneId);
        LocalDate today = LocalDate.now(zoneId);
        if (!birth.isBefore(today)) return 0;
        return Period.between(birth, today).getYears();
    }
}
