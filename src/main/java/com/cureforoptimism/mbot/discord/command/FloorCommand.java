package com.cureforoptimism.mbot.discord.command;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.RIGHT_ALIGN;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.service.FloorService;
import com.cureforoptimism.mbot.service.TreasureService;
import com.inamik.text.tables.SimpleTable;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class FloorCommand implements MbotCommand {
  final TreasureService treasureService;
  final DiscordBot discordBot;
  final FloorService floorService;

  private static class FloorResponse {
    EmbedCreateSpec floorEmbed;
    EmbedCreateSpec floorMagicEmbed;
    EmbedCreateSpec floorUsdEmbed;
    EmbedCreateSpec mcDonaldsEmbed;

    FloorResponse(
        EmbedCreateSpec floorEmbed,
        EmbedCreateSpec floorMagicEmbed,
        EmbedCreateSpec floorUsdEmbed,
        EmbedCreateSpec mcDonaldsEmbed) {
      this.floorEmbed = floorEmbed;
      this.floorMagicEmbed = floorMagicEmbed;
      this.floorUsdEmbed = floorUsdEmbed;
      this.mcDonaldsEmbed = mcDonaldsEmbed;
    }
  }

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

    return event
        .getMessage()
        .getChannel()
        .flatMap(
            c -> {
              FloorResponse floorResponse = getFloorMessage();

              try {
                return c.createMessage(
                    MessageCreateSpec.builder()
                        .content(
                            "Like !floor? Then you're gonna LOVE /floor! Give it a try, today!")
                        .addFile(
                            "floor.png",
                            new ByteArrayInputStream(floorService.getCurrentFloorImageBytes()))
                        .addFile(
                            "floor_usd.png",
                            new ByteArrayInputStream(floorService.getCurrentFloorUsdImageBytes()))
                        .addFile(
                            "mcdonalds_application.png",
                            new ByteArrayInputStream(
                                new ClassPathResource("mcdonalds_application.png")
                                    .getInputStream()
                                    .readAllBytes()))
                        .addEmbed(floorResponse.floorMagicEmbed)
                        .addEmbed(floorResponse.floorUsdEmbed)
                        .addEmbed(floorResponse.floorEmbed)
                        .addEmbed(floorResponse.mcDonaldsEmbed)
                        .build());
              } catch (Exception ex) {
                return Mono.empty();
              }
            });
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/floor command received");

    FloorResponse floorResponse = getFloorMessage();

    try {
      event
          .deferReply()
          .then(
              event.createFollowup(
                  InteractionFollowupCreateSpec.builder()
                      .addFile(
                          "floor.png",
                          new ByteArrayInputStream(floorService.getCurrentFloorImageBytes()))
                      .addFile(
                          "floor_usd.png",
                          new ByteArrayInputStream(floorService.getCurrentFloorUsdImageBytes()))
                      .addFile(
                          "mcdonalds_application.png",
                          new ByteArrayInputStream(
                              new ClassPathResource("mcdonalds_application.png")
                                  .getInputStream()
                                  .readAllBytes()))
                      .addEmbed(floorResponse.floorMagicEmbed)
                      .addEmbed(floorResponse.floorUsdEmbed)
                      .addEmbed(floorResponse.floorEmbed)
                      .addEmbed(floorResponse.mcDonaldsEmbed)
                      .build()))
          .block();

    } catch (Exception ex) {
    }
    return Mono.empty();
  }

  private FloorResponse getFloorMessage() {
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
    final var bodyFloor = treasureService.getBodyFloor();
    final var usdBodyFloor = bodyFloor.multiply(BigDecimal.valueOf(currentPrice));

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
        .nextCell("SMOLBODY")
        .nextCell(String.format("%.2f", bodyFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", usdBodyFloor))
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

    EmbedCreateSpec floorEmbed =
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
            .build();

    final var mcDonaldsEmbed =
        EmbedCreateSpec.builder().image("attachment://mcdonalds_application.png").build();

    final var floorMagicEmbed = EmbedCreateSpec.builder().image("attachment://floor.png").build();

    final var floorUsdEmbed = EmbedCreateSpec.builder().image("attachment://floor_usd.png").build();

    return new FloorResponse(floorEmbed, floorMagicEmbed, floorUsdEmbed, mcDonaldsEmbed);
  }
}
