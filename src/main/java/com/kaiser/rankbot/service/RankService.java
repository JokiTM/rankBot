package com.kaiser.rankbot.service;

import com.kaiser.rankbot.UserRank;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class RankService {
    private static final Logger logger = LoggerFactory.getLogger(RankService.class);
    String riotIdPattern = "^[A-Za-z0-9 _\\.]{3,16}#[A-Za-z0-9]{2,5}$";
    @Autowired
    private RiotApiService riotApiService;


    public UserRank setUser(SlashCommandInteractionEvent event) throws Exception {

        logger.info("setUser: {}", event.getUser());

        var riotId = event.getOption("riotid").getAsString();

        if (!riotId.matches(riotIdPattern))
            throw new Exception("riotId is invalid!");

        var puuid = riotApiService.getPuuid(riotId);
        if (puuid == null){
            throw new Exception("puuid is null!");
        }
        var rank = riotApiService.fetchRankFromRiotApi(puuid);
        var discordName = event.getMember().getNickname();

        String newName = discordName.substring(0, Math.min(discordName.length(), 16));

        logger.info("discordName: {}", discordName);
        if (rank == null){
            event.reply("Unranked in Solo/Duo.").queue();
            UserRank user = new UserRank(
                    event.getUser().getId(),
                    newName,
                    event.getGuild().getId(),
                    riotId,
                    puuid,
                    "Unranked",
                    "Unranked",
                    0,
                    LocalDateTime.now());

            logger.info("RankService got User: {}", user);
            return user;
        }
        else {
            //Guild guild = event.getGuild();


            return new UserRank(
                    event.getUser().getId(),
                    newName,
                    event.getGuild().getId(),
                    riotId,
                    puuid,
                    rank.getRank(),
                    rank.getTier(),
                    rank.getLeaguePoints(),
                    LocalDateTime.now()
            );

        }
    }


    public void modifyNickname(Guild guild, UserRank user, String newNickname){

        System.out.println("Modify nickname from: " + user.getDiscordId() + " rank: " + user.getTier() + " " + user.getRank() + " | " + user.getLeaguePoints() + " LP");

        guild.retrieveMemberById(user.getDiscordId()).queue(member -> {
            guild.modifyNickname(member,newNickname).queue();
        });
    }

    public void updateUser(UserRank userRank) throws Exception {
        try {
            var rank = riotApiService.fetchRankFromRiotApi(userRank.getPuuid());
            if (rank == null) {
                // User is unranked, but this isn't an error
                userRank.setRank("Unranked");
                userRank.setTier("Unranked");
                userRank.setLeaguePoints(0);
                return;
            }
            userRank.setRank(rank.getRank());
            userRank.setTier(rank.getTier());
            userRank.setLeaguePoints(rank.getLeaguePoints());
        } catch (Exception e) {
            if (e.getMessage().contains("Rate limit")) {
                // Don't update the user if we hit rate limits, just skip this update
                logger.warn("Skipping update for user {} due to rate limit", userRank.getDiscordId());
                throw new Exception("Rate limit reached - skipping update");
            }
            // For other API errors, log but don't delete the user
            logger.error("Error updating user {}: {}", userRank.getDiscordId(), e.getMessage());
            throw e;
        }
    }
}