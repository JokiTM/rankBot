package com.kaiser.rankbot;

import com.kaiser.rankbot.repo.RankRepo;
import com.kaiser.rankbot.service.CommandService;
import com.kaiser.rankbot.service.RankService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class MyListener extends ListenerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MyListener.class);
  private int updateCount;

  @Autowired
  private RankService rankService;
  @Autowired
  private RankRepo repo;
  @Autowired
  private JDA jda;
  
  @PostConstruct
  public void init() {
    logger.info("Initiating rankBot");
    jda.addEventListener(this);
    logger.info("Added event listener");
    CommandService.registerCommands(jda);
    logger.info("Registrated slash Commands");
    logger.info("Intiated rankBot. Listening for events...");
  }


  String helpMessage = """
      **Verfügbare Befehle:**
      - `/setuser <Summoner-Name>` - Verlinkt Discord Profil mit League Account Format:[Joki#ANT].
      - `/help` - Zeigt diese Nachricht an.
      - `/removeuser` - Entfernt den nutzer aus der Datenbank, sodass der name erneut geändert werden kann.
      - `/setnickname` - Ändert den nickname des Users.

  """;

  @Override
  public void onMessageReceived(MessageReceivedEvent event)
{
    logger.info("message recieved");
    if (event.getAuthor().isBot()) return;
    // We don't want to respond to other bot accounts, including ourself
    Message message = event.getMessage();
    String content = message.getContentRaw(); 
    // getContentRaw() is an atomic getter
    // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
    MessageChannel channel = event.getChannel();
    if(content.contains("rong")){
      message.addReaction(Emoji.fromUnicode("U+1F1F7")).queue(); // R
      message.addReaction(Emoji.fromUnicode("U+1F1F4")).queue(); // O
      message.addReaction(Emoji.fromUnicode("U+1F1F3")).queue(); // N
      message.addReaction(Emoji.fromUnicode("U+1F1EC")).queue(); // G
    }
    if(channel.getName().contains("essen")){
      logger.info("Message in essen recieved. Adding fish!");
      message.addReaction(Emoji.fromUnicode("U+1F41F")).queue(); 
    }
    else{
      logger.info("channel nicht essen");
    }
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    logger.info("Slash command interaction");

    if (event.getName().equals("setuser")) {
      setUser(event);
    } else if (event.getName().equals("removeuser")) {
      removeUser(event);
    } else if (event.getName().equals("help")) {
      event.getChannel().sendMessage(helpMessage).queue();
    } else if (event.getName().equals("setnickname")) {
      setNickname(event);
    }

  }

  private void setNickname(SlashCommandInteractionEvent event) {
    logger.info("Set Nickname");
    var user = event.getUser();
    var newName = Objects.requireNonNull(event.getOption("nickname")).getAsString();
    if (newName.length() > 16) {
      event.reply("Nickname is too long(Max 16 chars").queue();
      return;
    }
    var userRank = repo.findById(user.getId()).orElse(null);
    if (userRank == null) {
      event.reply("Nutzer nicht registriert! Benutzer /setuser, um dich zu registrieren!").queue();
      return;
    }
    repo.updateDiscordName(user.getId(), newName);
    if(!userRank.getTier().equals("Unranked")){
      rankService.modifyNickname(Objects.requireNonNull(event.getGuild()), userRank, newName + " ~ " + userRank.getTier().charAt(0) + " " + userRank.getRank() + " | " + userRank.getLeaguePoints() + "LP", true);
    }else{
      rankService.modifyNickname(Objects.requireNonNull(event.getGuild()), userRank, newName + " ~ " + "Unranked in LoL", true);
    }
    event.reply("Nickname set!").queue();
  }

  private void removeUser(SlashCommandInteractionEvent event) {
    logger.info("Remove user");
    repo.deleteById(event.getUser().getId());
    event.reply("Deleted user").queue();
  }

  private void setUser(SlashCommandInteractionEvent event) {
    logger.info("setuser");

    try {
      var user = rankService.setUser(event);
      repo.save(user);
      logger.info("Set user successfull: {}", event.getUser());
      event.reply(user.getTier() + " " + user.getRank() + " | " + user.getLeaguePoints() + " LP").queue();
    } catch (Exception e) {
      logger.info(e.getMessage());
      if (e.getMessage().equals("riotId is invalid!")) {
        event.reply("riotId is invalid!").queue();
      } else if (e.getMessage().equals("Error while fetching puuid from riot API")) {
        event.reply("Error while fetching puuid from riot API. User was not saved.").queue();
      }
    }
  }

  @Scheduled(fixedRate = 5000)
  private void updateUser() {
    boolean log = false;
    if (updateCount % 100 == 0)
    log = true;
    if (log)
    logger.info("Updating users..");
    if (log)
    logger.info("Update Count: {}", updateCount);
    List<UserRank> userList = repo.findAll();
    if (log)
    logger.info("Retrieved {} users from DB", userList.size());
    for (UserRank user : userList) {
      if (log)
      logger.info("Updating User: {}; id: {}", user.getDiscordName(), user.getDiscordId());
      var guild = jda.getGuildById(user.getGuildId());
      if (guild == null) {
        logger.error("Guild {} not found. Skipping User {}", user.getGuildId(), user.getDiscordId());
        // repo.deleteById(user.getDiscordId());
        continue;
      }
      try {
        rankService.updateUser(user);
        if(!user.getTier().equals("Unranked")){
          rankService.modifyNickname(guild, user, user.getDiscordName() + " ~ " + user.getTier().charAt(0) + " " + user.getRank() + " | " + user.getLeaguePoints() + "LP", log);
        }else{
          rankService.modifyNickname(guild, user, user.getDiscordName() + " ~ " + "Unranked in LoL", log);
        }

      } catch (Exception e) {
        logger.error("Couldn't update user: {}; Error: {}. ", user.getDiscordName(), e.getMessage());
      }
    }
    if (log)
    logger.info("Finished updating users\n\n\n");
    updateCount++;
  }
}
