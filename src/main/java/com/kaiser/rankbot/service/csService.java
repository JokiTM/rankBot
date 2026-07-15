package com.kaiser.rankbot.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

@Service
public class csService {
  private static final Logger log = LoggerFactory.getLogger(csService.class);

  @Autowired
  private JDA jda;

  public void notifyUsers(SlashCommandInteractionEvent event){
      for (Member usersWithCsRole : getUsersWithCsRole(event)) {
          usersWithCsRole.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("cs2"));
      }
  }

  public List<Member> getUsersWithCsRole(SlashCommandInteractionEvent event){
      return event.getGuild().getMembersWithRoles(jda.getRolesByName("cs", true));
  }
}
