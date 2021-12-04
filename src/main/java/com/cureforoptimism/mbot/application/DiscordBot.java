package com.cureforoptimism.mbot.application;

import com.cureforoptimism.mbot.discord.events.RefreshEvent;
import com.cureforoptimism.mbot.service.TokenService;
import com.cureforoptimism.mbot.service.TreasureService;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.MessageCreateSpec;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

@Component
@Slf4j
public class DiscordBot {
  final Web3j web3j;
  final ApplicationContext context;
  final GatewayDiscordClient client;
  final TokenService tokenService;
  final TreasureService treasureService;
  Double currentPrice;
  Double currentChange;

  public DiscordBot(
      ApplicationContext context,
      TokenService tokenService,
      TreasureService treasureService,
      Web3j web3j) {
    this.context = context;
    this.tokenService = tokenService;
    this.treasureService = treasureService;
    this.web3j = web3j;
    this.client =
        DiscordClientBuilder.create(tokenService.getDiscordToken()).build().login().block();

    if (client != null) {
      client
          .getEventDispatcher()
          .on(RefreshEvent.class)
          .subscribe(
              event -> {
                String nickName = ("MAGIC $" + currentPrice);
                String presence = String.format("24h: %.2f%%", currentChange);
                client
                    .getGuilds()
                    .toStream()
                    .forEach(
                        g -> g.changeSelfNickname(nickName).block());
                client
                    .updatePresence(ClientPresence.online(ClientActivity.watching(presence)))
                    .block();
              });
      client
          .getEventDispatcher()
          .on(MessageCreateEvent.class)
          .subscribe(
              e -> {
                if (e.getMessage().getContent().startsWith("!floor")) {
                  log.info("!floor command received");
                  final var magicFloor = treasureService.getFloor();
                  final var usdFloor = magicFloor.multiply(BigDecimal.valueOf(currentPrice));
                  final var landFloor = treasureService.getLandFloor();
                  final var usdLandFloor = landFloor.multiply(BigDecimal.valueOf(currentPrice));
                  final var totalLandListings = treasureService.getTotalFloorListings();
                  final var totalListings = treasureService.getTotalListings();
                  final var output =
                      String.format(
                          "SMOL           - MAGIC: %.2f ($%.2f). Total listings: %d.\nSMOL Land - MAGIC: %.2f ($%.2f). Total listings: %d.",
                          magicFloor,
                          usdFloor,
                          totalListings,
                          landFloor,
                          usdLandFloor,
                          totalLandListings);

                  e.getMessage().getChannel().flatMap(c -> c.createMessage(output)).block();
                } else if (e.getMessage().getContent().startsWith("!iq")) {
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2) {
                    log.info("!iq command received");
                    String tokenId = parts[1];
                    BigDecimal iq = treasureService.getIq(Integer.parseInt(tokenId));
                    String output = tokenId + " has an IQ of " + iq.toString();
                    e.getMessage().getChannel().flatMap(c -> c.createMessage(output)).block();
                  }
                } else if (e.getMessage().getContent().startsWith("!pfp")) {
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2) {
                    String tokenId = parts[1];
                    log.info("!pfp command received");
                    final var image = treasureService.getAnimatedGif(tokenId);
                    if(image == null) {
                      e.getMessage().getChannel().flatMap(c -> c.createMessage("I can't find a token with ID " + tokenId)).block();
                      return;
                    }

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image);
                    RequestBody body = MultipartBody.create(image, MediaType.parse("image/png"));
                    e.getMessage()
                        .getChannel()
                        .flatMap(
                            c ->
                                c.createMessage(
                                    MessageCreateSpec.builder()
                                        .addFile(tokenId + ".gif", byteArrayInputStream)
                                        .build()))
                        .block();
                  }
                }
              });
    }
  }

  public void refreshMagicPrice(Double price, Double usd24HChange) {
    currentPrice = price;
    currentChange = usd24HChange;
    client.getEventDispatcher().publish(new RefreshEvent(null, null));
  }
}
