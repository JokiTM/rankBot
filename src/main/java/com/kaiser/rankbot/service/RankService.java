package com.kaiser.rankbot.service;

import com.kaiser.rankbot.repo.UserRank;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;


@Service
public class RankService {
    private static final Logger logger = LoggerFactory.getLogger(RankService.class);

    @Autowired
    private RiotApiService riotApiService;





    public UserRank setUser(SlashCommandInteractionEvent event) throws Exception {

        logger.debug("setUser: {}", event.getUser());

        var riotId = event.getOption("riotid").getAsString();

        if (!riotId.contains("#"))
            throw new Exception("riotId is invalid!");

        var puuid = riotApiService.getPuuid(riotId);
        var rank = riotApiService.fetchRankFromRiotApi(puuid);

        if (rank == null){
            event.reply("Unranked in Solo/Duo.").queue();
            UserRank user = new UserRank(
                    event.getUser().getId(),
                    riotId,
                    puuid,
                    "Unranked",
                    "Unranked",
                    0,
                    LocalDateTime.now());

            logger.debug("RankService got User: {}", user);
            return user;
        }
        else {
            UserRank user = new UserRank(
                    event.getUser().getId(),
                    riotId,
                    puuid,
                    rank.getRank(),
                    rank.getTier(),
                    rank.getLeaguePoints(),
                    LocalDateTime.now()
            );
            //Guild guild = event.getGuild();


            return user;

        }
    }


    public void modifyNickname(Guild guild, UserRank user){

        System.out.println("Modify nickname from: " + user.getDiscordId() + " rank: " + user.getTier() + " " + user.getRank() + " | " + user.getLeaguePoints());

        guild.retrieveMemberById(user.getDiscordId()).queue(member -> {


            guild.modifyNickname(member, user.getTier() + " " + user.getRank() + " | " + user.getLeaguePoints()).queue();

         });
    }
}
