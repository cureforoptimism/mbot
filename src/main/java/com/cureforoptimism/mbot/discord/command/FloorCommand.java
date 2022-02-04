package com.cureforoptimism.mbot.discord.command;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.RIGHT_ALIGN;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.service.CoinGeckoService;
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
import java.util.Optional;
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
  final FloorService floorService;
  final CoinGeckoService coinGeckoService;
  private final Double yachtPrice = 10.0; // ETH

  private static class FloorResponse {
    EmbedCreateSpec floorEmbed;
    EmbedCreateSpec floorMagicEmbed;
    EmbedCreateSpec floorUsdEmbed;

    FloorResponse(
        EmbedCreateSpec floorEmbed,
        EmbedCreateSpec floorMagicEmbed,
        EmbedCreateSpec floorUsdEmbed) {
      this.floorEmbed = floorEmbed;
      this.floorMagicEmbed = floorMagicEmbed;
      this.floorUsdEmbed = floorUsdEmbed;
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
              // TODO: true
              FloorResponse floorResponse = getFloorMessage(true);
              if (floorResponse == null) {
                return Mono.empty();
              }

              return c.createMessage(
                  MessageCreateSpec.builder()
                      .content("Like !floor? Then you're gonna LOVE /floor! Give it a try, today!")
                      .addFile(
                          "floor.png",
                          new ByteArrayInputStream(floorService.getCurrentFloorImageBytes()))
                      .addFile(
                          "floor_usd.png",
                          new ByteArrayInputStream(floorService.getCurrentFloorUsdImageBytes()))
                      .addEmbed(floorResponse.floorMagicEmbed)
                      .addEmbed(floorResponse.floorUsdEmbed)
                      .addEmbed(floorResponse.floorEmbed)
                      .build());
            });
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/floor command received");

    // TODO: True
    FloorResponse floorResponse = getFloorMessage(true);
    if (floorResponse == null) {
      return Mono.empty();
    }

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
                    .addEmbed(floorResponse.floorMagicEmbed)
                    .addEmbed(floorResponse.floorUsdEmbed)
                    .addEmbed(floorResponse.floorEmbed)
                    .build()))
        .block();

    return Mono.empty();
  }

  private FloorResponse getFloorMessage(boolean includeEth) {
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
    final var petFloor = treasureService.getPetFloor();
    final var usdPetFloor = petFloor.multiply(BigDecimal.valueOf(currentPrice));
    double yachtPct = 0.0;

    double ethMktPrice = 0.0;
    if (includeEth) {
      final Optional<Double> ethMktPriceOpt = coinGeckoService.getEthPrice();
      if (ethMktPriceOpt.isEmpty()) {
        // This will retry once we have an ethereum price
        return null;
      }

      ethMktPrice = ethMktPriceOpt.get();

      final var cheapestSmol =
          usdCheapestMale.doubleValue() < usdCheapestFemale.doubleValue()
              ? usdCheapestMale
              : usdCheapestFemale;
      final Double cheapestSmolEth = cheapestSmol.doubleValue() / ethMktPrice;
      yachtPct = (cheapestSmolEth / yachtPrice) * 100.0;
    }

    final SimpleTable table =
        new SimpleTable()
            .nextRow()
            .nextCell("TYPE")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("MAGIC")
            .applyToCell(HORIZONTAL_CENTER.withWidth(7))
            .nextCell("USD")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12));

    if (includeEth) {
      table.nextCell("ETH").applyToCell(HORIZONTAL_CENTER.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("SMOL")
        .nextCell(String.format("%.00f", magicFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdFloor.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("SMOLBODY")
        .nextCell(String.format("%.00f", bodyFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdBodyFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdBodyFloor.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("LAND")
        .nextCell(String.format("%.00f", landFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdLandFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdLandFloor.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("MALE")
        .nextCell(String.format("%.00f", cheapestMale))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdCheapestMale))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdCheapestMale.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("FEMALE")
        .nextCell(String.format("%.00f", cheapestFemale))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdCheapestFemale))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdCheapestFemale.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("VROOM")
        .nextCell(String.format("%.00f", cheapestVroom))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdCheapestVroom))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdCheapestVroom.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("SMOLPET")
        .nextCell(String.format("%.00f", petFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdPetFloor))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdPetFloor.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("PAIR")
        .nextCell(String.format("%.00f", cheapestPair))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdCheapestPair))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(String.format("Ξ%.2f", usdCheapestPair.doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("PAIR+LAND")
        .nextCell(String.format("%.00f", cheapestPair.add(landFloor)))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdCheapestPair.add(usdLandFloor)))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(
              String.format("Ξ%.2f", usdCheapestPair.add(usdLandFloor).doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("PAIR+LAND+VROOM")
        .nextCell(String.format("%.00f", cheapestPair.add(landFloor).add(cheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", usdCheapestPair.add(usdLandFloor).add(usdCheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(
              String.format(
                  "Ξ%.2f",
                  usdCheapestPair.add(usdLandFloor).add(usdCheapestVroom).doubleValue()
                      / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

    table
        .nextRow()
        .nextCell("PAIR+LAND+VROOMx2")
        .nextCell(
            String.format(
                "%.00f", cheapestPair.add(landFloor).add(cheapestVroom).add(cheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(
            String.format(
                "$%.2f",
                usdCheapestPair.add(usdLandFloor).add(usdCheapestVroom).add(usdCheapestVroom)))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

    if (includeEth) {
      table
          .nextCell(
              String.format(
                  "Ξ%.2f",
                  usdCheapestPair
                          .add(usdLandFloor)
                          .add(usdCheapestVroom)
                          .add(usdCheapestVroom)
                          .doubleValue()
                      / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));
    }

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

    String title = "Smol Floor - Treasure Marketplace\nMAGIC: $" + currentPrice;
    if (includeEth) {
      title +=
          "\nETH: $"
              + ethMktPrice
              + "\nYacht Party Goal (Ξ"
              + String.format("%.0f", yachtPrice)
              + "): "
              + String.format("%.1f%%", yachtPct);
    }

    EmbedCreateSpec floorEmbed =
        EmbedCreateSpec.builder()
            .title(title)
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

    final var floorMagicEmbed = EmbedCreateSpec.builder().image("attachment://floor.png").build();

    final var floorUsdEmbed = EmbedCreateSpec.builder().image("attachment://floor_usd.png").build();

    return new FloorResponse(floorEmbed, floorMagicEmbed, floorUsdEmbed);
  }
}
