package com.cureforoptimism.mbot.discord.command;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.RIGHT_ALIGN;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.service.TreasureService;
import com.inamik.text.tables.SimpleTable;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
    final var cheapestVroom = treasureService.getCheapestVroom();
    final var cheapestVroomId = treasureService.getCheapestVroomId();
    final var usdCheapestVroom = cheapestVroom.multiply(BigDecimal.valueOf(currentPrice));
    final var totalVroomListings = treasureService.getTotalVroomListings();

    final SimpleTable table =
        new SimpleTable()
            .nextRow()
            .nextCell("TYPE")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("MAGIC")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("USD")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12));

    table
        .nextRow()
        .nextCell("SMOL")
        .nextCell(String.format("%.2f", magicFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("LAND")
        .nextCell(String.format("%.2f", landFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdLandFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("MALE")
        .nextCell(String.format("%.2f", cheapestMale))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdCheapestMale))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("FEMALE")
        .nextCell(String.format("%.2f", cheapestFemale))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdCheapestFemale))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("VROOM")
        .nextCell(String.format("%.2f", cheapestVroom))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdCheapestVroom))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("PAIR")
        .nextCell(String.format("%.2f", cheapestPair))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdCheapestPair))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("PAIR+LAND")
        .nextCell(String.format("%.2f", cheapestPair.add(landFloor)))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdCheapestPair.add(usdLandFloor)))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("PAIR+LAND+VROOM")
        .nextCell(String.format("%.2f", cheapestPair.add(landFloor).add(cheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdCheapestPair.add(usdLandFloor).add(usdCheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    table
        .nextRow()
        .nextCell("PAIR+LAND+VROOMx2")
        .nextCell(
            String.format(
                "%.2f", cheapestPair.add(landFloor).add(cheapestVroom).add(cheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(
            String.format(
                "$%.2f",
                usdCheapestPair.add(usdLandFloor).add(usdCheapestVroom).add(usdCheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    final var output =
        String.format(
            "Total SMOL listings: %d (%d LAND, %d VROOM)\nCheapest Male ID: #%d, Cheapest Female ID: #%d\nCheapest Vroom ID: #%d```\n%s```\n",
            totalListings,
            totalLandListings,
            totalVroomListings,
            cheapestMaleId,
            cheapestFemaleId,
            cheapestVroomId,
            Utilities.simpleTableToString(table));

    return event
        .getMessage()
        .getChannel()
        .flatMap(
            c ->
                c.createMessage(
                    EmbedCreateSpec.builder()
                        .title("Smol Floor - Treasure Marketplace\nMAGIC: $" + currentPrice)
                        .author(
                            "SmolBot",
                            null,
                            "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                        .description(output)
                        .addField(
                            "Note",
                            "Choose your own floor; 2x VROOM is default for OG minters, but new Smols don't require 2 VROOMs! Added here by a smol lot of requests",
                            true)
                        .build()));
  }
}
