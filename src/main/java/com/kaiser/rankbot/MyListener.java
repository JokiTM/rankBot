package com.kaiser.rankbot;

import com.kaiser.rankbot.repo.RankRepo;
import com.kaiser.rankbot.repo.UserRank;
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

    @Autowired
    private RankService rankService;
    @Autowired
    private RankRepo repo;
    @Autowired
    private JDA jda;

    @PostConstruct
            public void init() {
        jda.addEventListener(this);
    }


    String helpMessage = """
        **Verfügbare Befehle:**
        - `/setrank <Summoner-Name>` - Verlinkt Discord Profil mit League Account Format:[Joki#ANT].
        - `/help` - Zeigt diese Nachricht an.
        """;



    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        logger.info("Slash command interaction");

        if(event.getName().equals("setuser")){
            setUser(event);
        }else if(event.getName().equals("help")){
               event.getChannel().sendMessage(helpMessage).queue();

        }

    }

    private void setUser(SlashCommandInteractionEvent event){
        logger.info("setuser");
        //if user exists -> update
        try {
            var user = rankService.setUser(event);
            logger.info("setuser user retrieved: {}", user);
            repo.save(user);
            event.reply(user.getTier() + " " + user.getRank() + " | " + user.getLeaguePoints()).queue();
        } catch (Exception e) {
            logger.info(e.getMessage());
            if(e.getMessage().equals("riotId is invalid!")) {
                event.reply("riotId is invalid!").queue();
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    private void updateUser(){

        logger.info("Updating users");
        List<UserRank> userList = repo.findAll();
        Guild guild = jda.getGuildById("765991080528969798");

        for (UserRank userRank : userList) {
            assert guild != null;
            rankService.modifyNickname(guild, userRank);
        }
    }
}
