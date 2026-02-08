package com.kaiser.rankbot.service;

import com.kaiser.rankbot.UserRank;
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
  String riotIdPattern = "^[A-Za-z0-9 _.]{3,16}#[A-Za-z0-9]{2,5}$";
  @Autowired
  private RiotApiService riotApiService;

  public UserRank setUser(SlashCommandInteractionEvent event) throws Exception {

    logger.info("setUser: {}", event.getUser());

    var riotId = Objects.requireNonNull(event.getOption("riotid")).getAsString();

    if (!riotId.matches(riotIdPattern))
    throw new Exception("riotId is invalid!");

    var puuid = riotApiService.getPuuid(riotId);
    if (puuid == null) {
      throw new Exception("puuid is null!");
    }
    var rank = riotApiService.fetchRankFromRiotApi(puuid);
    // var discordName = Objects.requireNonNull(event.getMember()).getNickname();

    // String discordName = event.getMember().getNickname();
    // if (discordName == null) {
    // discordName = event.getUser().getName(); // fallback to username if no
    // nickname
    // }
    String discordName = event.getUser().getName();
    assert discordName != null;
    String newName = discordName.substring(0, Math.min(discordName.length(), 16));
    UserRank user = null;
    if (rank == null) {
      event.reply("Unranked in Solo/Duo.").queue();
      user = new UserRank(
        event.getUser().getId(),
        newName,
        Objects.requireNonNull(event.getGuild()).getId(),
        riotId,
        puuid,
        "Unranked",
        "Unranked",
        0,
        LocalDateTime.now());

    } else {
      // Guild guild = event.getGuild();

      user = new UserRank(
        event.getUser().getId(),
        newName,
        Objects.requireNonNull(event.getGuild()).getId(),
        riotId,
        puuid,
        rank.getRank(),
        rank.getTier(),
        rank.getLeaguePoints(),
        LocalDateTime.now());

    }
    logger.info("Got User: {}", user);
    return user;
  }

  public void modifyNickname(Guild guild, UserRank user, String newNickname, boolean log) {

    if(log) logger.info("Modify nickname from: {} | rank:{} {} | {} LP", user.getDiscordName(), user.getTier(), user.getRank(), user.getLeaguePoints());

    guild.retrieveMemberById(user.getDiscordId()).queue(member -> guild.modifyNickname(member, newNickname).queue());

}

public void updateUser(UserRank userRank) throws Exception {
  try {
    var rank = riotApiService.fetchRankFromRiotApi(userRank.getPuuid());
    if (rank == null) {
      // User is unranked, but this isn't an error
      userRank.setRank("0");
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
