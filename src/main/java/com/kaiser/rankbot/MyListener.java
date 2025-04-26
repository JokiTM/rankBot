package com.kaiser.rankbot;

import com.kaiser.rankbot.repo.RankRepo;
import com.kaiser.rankbot.service.CommandService;
import com.kaiser.rankbot.service.RankService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MyListener.class);
    private Guild guild;

    @Autowired
    private RankService rankService;
    @Autowired
    private RankRepo repo;
    @Autowired
    private JDA jda;


    @PostConstruct
            public void init() {
        jda.addEventListener(this);
        guild = jda.getGuildById(System.getenv("GUILD_ID"));
        CommandService.registerCommands(jda);

    }


    String helpMessage = """
        **Verfügbare Befehle:**
        - `/setrank <Summoner-Name>` - Verlinkt Discord Profil mit League Account Format:[Joki#ANT].
        - `/help` - Zeigt diese Nachricht an.
        - `/removeuser` - Entfernt den nutzer aus der Datenbank, sodass der name erneut geändert werden kann.
        - `/setnickname` - Ändert den nickname des Users.
    
        """;



    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        logger.info("Slash command interaction");

        if(event.getName().equals("setuser")){
            setUser(event);
        }else if(event.getName().equals("removeuser")){
            removeUser(event);
        }else if(event.getName().equals("help")){
               event.getChannel().sendMessage(helpMessage).queue();
        } else if(event.getName().equals("setnickname")){
            setNickname(event);
        }

    }

    private void setNickname(SlashCommandInteractionEvent event) {
        logger.info("Set Nickname");
        var user = event.getUser();
        var newName = event.getOption("nickname").getAsString();
        if(newName.length() > 16){
            event.reply("Nickname is too long(Max 16 chars").queue();
            return;
        }
        var userRank = repo.findById(user.getId()).orElse(null);
        if(userRank == null) {
            event.reply("Nutzer nicht registriert! Benutzer /setuser, um dich zu registrieren!").queue();
            return;
        }
        repo.updateDiscordName(user.getId(), newName);
        rankService.modifyNickname(guild, userRank, newName + " ~ " + userRank.getTier().charAt(0) + " " + userRank.getRank() + " | " + userRank.getLeaguePoints() + "LP");
        event.reply("Nickname set!").queue();
    }

    private void removeUser(SlashCommandInteractionEvent event){
        logger.info("Remove user");
        repo.deleteById(event.getUser().getId());
        event.reply("Deleted user").queue();
    }


    private void setUser(SlashCommandInteractionEvent event){
        logger.info("setuser");
        //if user exists -> update

        try {
            var user = rankService.setUser(event);
            logger.info("setuser user retrieved: {}", user);
            repo.save(user);
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

    @Scheduled(fixedRate = 15000)
    private void updateUser(){

        logger.info("Updating users");
        List<UserRank> userList = repo.findAll();

        for (UserRank userRank : userList) {
            assert guild != null;
            try {
                rankService.updateUser(userRank);
                rankService.modifyNickname(guild, userRank, userRank.getDiscordName() + " ~ " + userRank.getTier().charAt(0) + " " + userRank.getRank() + " | " + userRank.getLeaguePoints() + "LP");

            }catch (Exception e) {
                logger.info("Couldn't update user: {}. User will be deleted from db", e.getMessage());
                rankService.modifyNickname(guild, userRank, userRank.getDiscordName());
                repo.deleteById(userRank.getDiscordId());
            }
        }

    }
}
