package com.kaiser.rankbot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class jdaConfig {
  private static final Logger logger = LoggerFactory.getLogger(jdaConfig.class);

  @Bean
  public JDA jda() throws InterruptedException {

    var discordBotToken = System.getenv("DISCORD_BOT_TOKEN");
    var jda = JDABuilder.createDefault(discordBotToken)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build();
    jda.awaitReady();
    logger.info("JDA constructed");
    return jda;
  }
}
