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
        var rank = riotApiService.fetchRankFromRiotApi(puuid);
        var discordName = event.getUser().getName();
        logger.info("discordName: {}", discordName);
        if (rank == null){
            event.reply("Unranked in Solo/Duo.").queue();
            UserRank user = new UserRank(
                    event.getUser().getId(),
                    event.getUser().getName(),
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
                    event.getUser().getName(),
                    riotId,
                    puuid,
                    rank.getRank(),
                    rank.getTier(),
                    rank.getLeaguePoints(),
                    LocalDateTime.now()
            );

        }
    }


    public void modifyNickname(Guild guild, UserRank user){

        System.out.println("Modify nickname from: " + user.getDiscordId() + " rank: " + user.getTier() + " " + user.getRank() + " | " + user.getLeaguePoints() + " LP");

        guild.retrieveMemberById(user.getDiscordId()).queue(member -> {


            guild.modifyNickname(member,user.getDiscordName() + " ~ " + user.getTier().charAt(0) + " " + user.getRank() + " | " + user.getLeaguePoints() + "LP").queue();

         });
    }

    public void updateUser(UserRank userRank) {
        var rank = riotApiService.fetchRankFromRiotApi(userRank.getPuuid());
        userRank.setRank(rank.getRank());
        userRank.setTier(rank.getTier());
        userRank.setLeaguePoints(rank.getLeaguePoints());
    }
}
