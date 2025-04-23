package com.kaiser.rankbot.service;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CommandService {


    @Autowired
    JDA api;

    @PostConstruct
            public void registerCommands() {
        api.updateCommands().addCommands(
                Commands.slash("setuser", "Sets the Summoners Name for the account")
                        .addOption(OptionType.STRING, "riotid", "riot Id (Example : JokiTM#ANT)", true),
                Commands.slash("help", "Displays help message"),
                Commands.slash("removeuser", "Removes the user from database")

        ).queue();
    }
}
