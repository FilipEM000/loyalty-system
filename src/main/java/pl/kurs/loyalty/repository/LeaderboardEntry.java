package pl.kurs.loyalty.repository;

public interface LeaderboardEntry {
    Long getUserId();
    String getDisplayName();
    Long getTotalPoints();
}