package com.kaiser.rankbot.repo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_ranks")
@AllArgsConstructor
@NoArgsConstructor
public class UserRank {
    @Id
    private String discordId;
    private String riotId;
    private String puuid;
    private String rank;
    private String tier;
    private Integer leaguePoints;
    private LocalDateTime lastUpdated;
}
