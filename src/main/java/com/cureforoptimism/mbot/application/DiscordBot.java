package com.cureforoptimism.mbot.application;

import com.cureforoptimism.mbot.discord.events.RefreshEvent;
import com.cureforoptimism.mbot.discord.listener.MbotCommandListener;
import com.cureforoptimism.mbot.service.TokenService;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DiscordBot implements ApplicationRunner {
  final ApplicationContext context;
  static GatewayDiscordClient client;
  final TokenService tokenService;

  // TODO: This sucks. Makes this suck less with a rational pattern.
  @Getter Double currentPrice;
  @Getter Double currentChange;

  public DiscordBot(ApplicationContext context, TokenService tokenService) {
    this.context = context;
    this.tokenService = tokenService;
  }

  public void refreshMagicPrice(Double price, Double usd24HChange) {
    currentPrice = price;
    currentChange = usd24HChange;
    client.getEventDispatcher().publish(new RefreshEvent(null, null));
  }

  @Override
  public void run(ApplicationArguments args) {
    MbotCommandListener mbotCommandListener = new MbotCommandListener(context);

    client =
        DiscordClientBuilder.create(tokenService.getDiscordToken())
            .build()
            .gateway()
            .login()
            .block();

    if (client != null) {
      client
          .getEventDispatcher()
          .on(MessageCreateEvent.class)
          .subscribe(mbotCommandListener::handle);

      client
          .on(RefreshEvent.class)
          .subscribe(
              event -> {
                String nickName = ("MAGIC $" + currentPrice);
                String presence = String.format("24h: %.2f%%", currentChange);
                client.getGuilds().toStream().forEach(g -> g.changeSelfNickname(nickName).block());
                client
                    .updatePresence(ClientPresence.online(ClientActivity.watching(presence)))
                    .block();
              });
    }

    log.info("Discord client logged in");
  }
}
