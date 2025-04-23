package com.kaiser.rankbot;

import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiscordBot {

	@Autowired
    static JDA api;
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(DiscordBot.class, args);

		// Get the listener bean from the application context
	}

	/*@PostConstruct
	void registerListener(){
		MyListener myListener = applicationContext.getBean(MyListener.class);

		api.addEventListener(myListener);
	}*/
}