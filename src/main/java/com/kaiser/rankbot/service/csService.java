package com.kaiser.rankbot.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.jdi.event.Event;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;

import java.util.List;

@Service
public class csService {
  private static final Logger log = LoggerFactory.getLogger(csService.class);

  private String[] users = {"535859189721989156", "429624664046829571", "437204945788207105", "345277304408375297", "548596609295056896", "550344514418507806"};

  @Autowired
  private JDA jda;

  private void notifyUsers(Event event){
      for (String user : users)
          jda.getUserById(user).openPrivateChannel().flatMap(channel -> channel.sendMessage("cs2")).queue();
  }
}
