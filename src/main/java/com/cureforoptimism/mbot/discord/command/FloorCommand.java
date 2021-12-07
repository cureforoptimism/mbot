package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.service.TreasureService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
@Slf4j
public class FloorCommand implements MbotCommand {
  final TreasureService treasureService;
  final DiscordBot discordBot;

  @Override
  public String getName() {
    return "floor";
  }

  @Override
  public String getDescription() {
    return "Shows the current floor price of Smols on the Treasure marketplace";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!floor command received");
    Double currentPrice = discordBot.getCurrentPrice();

    final var magicFloor = treasureService.getFloor();
    final var usdFloor = magicFloor.multiply(BigDecimal.valueOf(currentPrice));
    final var landFloor = treasureService.getLandFloor();
    final var usdLandFloor = landFloor.multiply(BigDecimal.valueOf(currentPrice));
    final var totalLandListings = treasureService.getTotalFloorListings();
    final var totalListings = treasureService.getTotalListings();
    final var cheapestMale = treasureService.getCheapestMale();
    final var usdCheapestMale = cheapestMale.multiply(BigDecimal.valueOf(currentPrice));
    final var cheapestFemale = treasureService.getCheapestFemale();
    final var usdCheapestFemale = cheapestFemale.multiply(BigDecimal.valueOf(currentPrice));
    final var cheapestMaleId = treasureService.getCheapestMaleId();
    final var cheapestFemaleId = treasureService.getCheapestFemaleId();
    final var cheapestPair = cheapestFemale.add(cheapestMale);
    final var usdCheapestPair = cheapestPair.multiply(BigDecimal.valueOf(currentPrice));
    final var output =
        String.format(
            "```\nSMOL      - MAGIC: %.2f ($%.2f). Total listings: %d.\nSMOL Land - MAGIC: %.2f ($%.2f). Total listings: %d.\nCheapest male (#%d)      - MAGIC: %.2f ($%.2f).\nCheapest female (#%d)   - MAGIC: %.2f ($%.2f).\nCheapest pair              - MAGIC: %.2f ($%.2f).\n```",
            magicFloor,
            usdFloor,
            totalListings,
            landFloor,
            usdLandFloor,
            totalLandListings,
            cheapestMaleId,
            cheapestMale,
            usdCheapestMale,
            cheapestFemaleId,
            cheapestFemale,
            usdCheapestFemale,
            cheapestPair,
            usdCheapestPair);

    return event
        .getMessage()
        .getChannel()
        .flatMap(
            c ->
                c.createMessage(
                    EmbedCreateSpec.builder()
                        .title("Smol Floor - Treasure Marketplace")
                        .author(
                            "SmolBot",
                            null,
                            "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                        .description(output)
                        .build()));
  }
}
