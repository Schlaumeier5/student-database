package de.igslandstuhl.database.holidays;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record SchoolWeek(Instant start, Instant end) {
    public static SchoolWeek of(Instant instant, ZoneId zone) {
        LocalDate date = instant.atZone(zone).toLocalDate();
        
        // Move to Monday of the same week
        LocalDate monday = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate friday = monday.plusDays(4);

        Instant startInstant = monday.atStartOfDay(zone).toInstant();
        Instant endInstant = friday.atTime(LocalTime.MAX).atZone(zone).toInstant();

        return new SchoolWeek(startInstant, endInstant);
    }
    public static List<SchoolWeek> getAll(Instant start, Instant end, ZoneId zone) {
        List<SchoolWeek> weeks = new ArrayList<>();
        SchoolWeek current = of(start, zone);

        while (!current.start().isAfter(end)) {
            weeks.add(current);
            current = current.nextWeek(zone);
        }

        return weeks;
    }
    public SchoolWeek nextWeek(ZoneId zone) {
        // Find Monday of the next week
        LocalDate nextMonday = this.start.atZone(zone).toLocalDate().plusWeeks(1);
        LocalDate nextFriday = nextMonday.plusDays(4);

        Instant startInstant = nextMonday.atStartOfDay(zone).toInstant();
        Instant endInstant = nextFriday.atTime(LocalTime.MAX).atZone(zone).toInstant();

        return new SchoolWeek(startInstant, endInstant);
    }
    public SchoolWeek lastWeek(ZoneId zone) {
        // Find Monday of the next week
        LocalDate nextMonday = this.start.atZone(zone).toLocalDate().minusWeeks(1);
        LocalDate nextFriday = nextMonday.plusDays(4);

        Instant startInstant = nextMonday.atStartOfDay(zone).toInstant();
        Instant endInstant = nextFriday.atTime(LocalTime.MAX).atZone(zone).toInstant();

        return new SchoolWeek(startInstant, endInstant);
    }
    public Holiday[] getHolidays() {
        return Holiday.holidaysInterval(start, end);
    }
    public boolean currentWeek(ZoneId zone) {
        return start.isBefore(Instant.now()) && nextWeek(zone).start.isAfter(Instant.now());
    }
    private boolean schoolOnDay(Holiday[] holidays, DayOfWeek day, ZoneId zone) {
        Instant weekday = start.atZone(zone).toLocalDate().with(day).atTime(12, 0).atZone(zone).toInstant();
        return Arrays.stream(holidays).anyMatch((h) -> h.getStart().isBefore(weekday) && h.getEnd().isAfter(weekday));
    }
    public boolean hasSchool(ZoneId zone) {
        Holiday[] holidays = getHolidays();
        if (holidays.length == 0) return false;
        return Arrays.stream(DayOfWeek.values()).filter((d) -> d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY).allMatch((d) -> schoolOnDay(holidays, d, zone));
    }
    public boolean noSchoolUTC() {
        return !hasSchool(ZoneId.of("UTC"));
    }
}
