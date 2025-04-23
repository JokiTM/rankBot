package com.kaiser.rankbot.repo;

import com.kaiser.rankbot.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankRepo extends JpaRepository<UserRank, Integer>{
    @Modifying
    @Query("UPDATE UserRank u SET u.discordName = :name WHERE u.discordId = :id")
    void updateDiscordName(@Param("id") String id, @Param("name") String name);

}
