package com.cureforoptimism.mbot.discord.command;

import static com.cureforoptimism.mbot.domain.SmolType.VROOM;
import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.RIGHT_ALIGN;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.domain.Land;
import com.cureforoptimism.mbot.domain.Pet;
import com.cureforoptimism.mbot.domain.Smol;
import com.cureforoptimism.mbot.domain.SmolBody;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.domain.UserFloor;
import com.cureforoptimism.mbot.domain.Vroom;
import com.cureforoptimism.mbot.repository.LandRepository;
import com.cureforoptimism.mbot.repository.PetRepository;
import com.cureforoptimism.mbot.repository.SmolBodyRepository;
import com.cureforoptimism.mbot.repository.SmolRepository;
import com.cureforoptimism.mbot.repository.UserFloorRepository;
import com.cureforoptimism.mbot.repository.VroomRepository;
import com.cureforoptimism.mbot.service.CoinGeckoService;
import com.cureforoptimism.mbot.service.TreasureService;
import com.inamik.text.tables.SimpleTable;
import com.smolbrains.PetsContract;
import com.smolbrains.SmolBodiesContract;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsVroomContract;
import com.smolbrains.SmolLandContract;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class MyFloor implements MbotCommand {
  private UserFloorRepository userFloorRepository;
  private SmolRepository smolRepository;
  private SmolBodyRepository smolBodyRepository;
  private VroomRepository vroomRepository;
  private LandRepository landRepository;
  private final DiscordBot discordBot;
  private final TreasureService treasureService;
  private final SmolBrainsContract smolBrainsContract;
  private final SmolBodiesContract smolBodiesContract;
  private final SmolBrainsVroomContract vroomContract;
  private final SmolLandContract smolLandContract;
  private final CoinGeckoService coinGeckoService;
  private final PetsContract petsContract;
  private final PetRepository petRepository;

  @Override
  public String getName() {
    return "myfloor";
  }

  @Override
  public String getDescription() {
    return "Get or set your own personal floor";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    return null;
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    var existingFloor =
        userFloorRepository.findByDiscordUserId(event.getInteraction().getUser().getId().asLong());
    if (existingFloor == null) {
      userFloorRepository.save(
          UserFloor.builder()
              .discordId(
                  event.getInteraction().getUser().getUsername()
                      + "#"
                      + event.getInteraction().getUser().getDiscriminator())
              .discordUserId(event.getInteraction().getUser().getId().asLong())
              .build());
      existingFloor =
          userFloorRepository.findByDiscordUserId(
              event.getInteraction().getUser().getId().asLong());
    }

    event.deferReply().withEphemeral(true).block();

    String userDiscriminator =
        event.getInteraction().getUser().getUsername()
            + "#"
            + event.getInteraction().getUser().getDiscriminator();

    if (event.getOption("remove").isPresent()) {
      log.info("/myfloor remove received: " + userDiscriminator);

      final var removeOptions = event.getOption("remove").get();
      existingFloor = handleAddOrRemove(existingFloor, true, removeOptions).orElse(null);
      if (existingFloor == null) {
        return Mono.empty();
      }

      existingFloor = userFloorRepository.save(existingFloor);
    } else if (event.getOption("add").isPresent()) {
      log.info("/myfloor add received: " + userDiscriminator);

      final var addOptions = event.getOption("add").get();
      existingFloor = handleAddOrRemove(existingFloor, false, addOptions).orElse(null);
      if (existingFloor == null) {
        return Mono.empty();
      }

      existingFloor = userFloorRepository.save(existingFloor);
    } else {
      log.info("/myfloor get received: " + userDiscriminator);
    }

    // Get all objects for this fine person
    final Set<Smol> smols =
        existingFloor.getSmols().stream()
            .map(s -> smolRepository.findById(s).get())
            .collect(Collectors.toSet());

    final Set<SmolBody> swols =
        existingFloor.getSwols().stream()
            .map(s -> smolBodyRepository.findById(s.longValue()).get())
            .collect(Collectors.toSet());

    final Set<Vroom> vrooms =
        existingFloor.getVrooms().stream()
            .map(v -> vroomRepository.findById(v.longValue()).get())
            .collect(Collectors.toSet());

    final Set<Land> lands =
        existingFloor.getLand().stream()
            .map(v -> landRepository.findById(v.longValue()).get())
            .collect(Collectors.toSet());

    final Set<Pet> pets =
        existingFloor.getPets().stream()
            .map(v -> petRepository.findById(v.longValue()).get())
            .collect(Collectors.toSet());

    Double currentPrice = discordBot.getCurrentPrice();

    final var landFloor = treasureService.getLandFloor();
    final var usdLandFloor = landFloor.multiply(BigDecimal.valueOf(currentPrice));
    final var cheapestMale = treasureService.getCheapestMale();
    final var usdCheapestMale = cheapestMale.multiply(BigDecimal.valueOf(currentPrice));
    final var cheapestFemale = treasureService.getCheapestFemale();
    final var usdCheapestFemale = cheapestFemale.multiply(BigDecimal.valueOf(currentPrice));
    final var cheapestVroom = treasureService.getCheapestVroom();
    final var usdCheapestVroom = cheapestVroom.multiply(BigDecimal.valueOf(currentPrice));
    final var bodyFloor = treasureService.getBodyFloor();
    final var usdBodyFloor = bodyFloor.multiply(BigDecimal.valueOf(currentPrice));
    final var petFloor = treasureService.getPetFloor();
    final var usdPetFloor = petFloor.multiply(BigDecimal.valueOf(currentPrice));

    double ethMktPrice;
    final Optional<Double> ethMktPriceOpt = coinGeckoService.getEthPrice();
    if (ethMktPriceOpt.isEmpty()) {
      // This will retry once we have an ethereum price
      return null;
    }

    ethMktPrice = ethMktPriceOpt.get();

    BigDecimal totalMagic = BigDecimal.ZERO;
    BigDecimal totalUsd = BigDecimal.ZERO;

    final StringBuilder header = new StringBuilder();
    final SimpleTable table =
        new SimpleTable()
            .nextRow()
            .nextCell("TYPE")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("MAGIC")
            .applyToCell(HORIZONTAL_CENTER.withWidth(7))
            .nextCell("USD")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("ETH")
            .applyToCell(HORIZONTAL_CENTER.withWidth(7));

    if (!smols.isEmpty()) {
      int numMales = 0;
      int numLadies = 0;

      for (Smol smol : smols) {
        final var gender =
            smol.getTraits().stream()
                .filter(t -> t.getType().equalsIgnoreCase("Gender"))
                .findFirst();
        if (gender.isEmpty()) {
          log.warn("smol bot does not understand this gender: " + smol.getId());
          return Mono.empty();
        }

        if (gender.get().getValue().equalsIgnoreCase("male")) {
          numMales++;
        } else {
          numLadies++;
        }
      }

      header.append("SMOLS: ");
      smols.forEach(s -> header.append("#").append(s.getId()).append(" "));

      if (numMales > 0) {
        totalMagic = totalMagic.add(cheapestMale.multiply(BigDecimal.valueOf(numMales)));
        totalUsd = totalUsd.add(usdCheapestMale.multiply(BigDecimal.valueOf(numMales)));

        table
            .nextRow()
            .nextCell("SMOL MALE" + (numMales > 1 ? "x" + numMales : ""))
            .nextCell(String.format("%.00f", cheapestMale.multiply(BigDecimal.valueOf(numMales))))
            .applyToCell(RIGHT_ALIGN.withWidth(7))
            .nextCell(
                String.format("$%.2f", usdCheapestMale.multiply(BigDecimal.valueOf(numMales))))
            .applyToCell(RIGHT_ALIGN.withWidth(12))
            .nextCell(
                String.format(
                    "Ξ%.2f",
                    usdCheapestMale.multiply(BigDecimal.valueOf(numMales)).doubleValue()
                        / ethMktPrice))
            .applyToCell(RIGHT_ALIGN.withWidth(7));
      }

      if (numLadies > 0) {
        totalMagic = totalMagic.add(cheapestMale.multiply(BigDecimal.valueOf(numLadies)));
        totalUsd = totalUsd.add(usdCheapestFemale.multiply(BigDecimal.valueOf(numLadies)));

        table
            .nextRow()
            .nextCell("SMOL LADY" + (numLadies > 1 ? "x" + numLadies : ""))
            .nextCell(
                String.format("%.00f", cheapestFemale.multiply(BigDecimal.valueOf(numLadies))))
            .applyToCell(RIGHT_ALIGN.withWidth(7))
            .nextCell(
                String.format("$%.2f", usdCheapestFemale.multiply(BigDecimal.valueOf(numLadies))))
            .applyToCell(RIGHT_ALIGN.withWidth(12))
            .nextCell(
                String.format(
                    "Ξ%.2f",
                    usdCheapestFemale.multiply(BigDecimal.valueOf(numLadies)).doubleValue()
                        / ethMktPrice))
            .applyToCell(RIGHT_ALIGN.withWidth(7));
      }
    }

    if (!swols.isEmpty()) {
      int numSwols = swols.size();
      totalMagic = totalMagic.add(bodyFloor.multiply(BigDecimal.valueOf(numSwols)));
      totalUsd = totalUsd.add(usdBodyFloor.multiply(BigDecimal.valueOf(numSwols)));

      table
          .nextRow()
          .nextCell("SMOLBODY" + (numSwols > 1 ? "x" + numSwols : ""))
          .nextCell(String.format("%.00f", bodyFloor.multiply(BigDecimal.valueOf(numSwols))))
          .applyToCell(RIGHT_ALIGN.withWidth(7))
          .nextCell(String.format("$%.2f", usdBodyFloor.multiply(BigDecimal.valueOf(numSwols))))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(
              String.format(
                  "Ξ%.2f",
                  usdBodyFloor.multiply(BigDecimal.valueOf(numSwols)).doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));

      if (!header.isEmpty()) {
        header.append("\n");
      }
      header.append("SWOLS: ");
      swols.forEach(s -> header.append("#").append(s.getId()).append(" "));
    }

    if (!lands.isEmpty()) {
      int numLands = lands.size();
      totalMagic = totalMagic.add(landFloor.multiply(BigDecimal.valueOf(numLands)));
      totalUsd = totalUsd.add(usdLandFloor.multiply(BigDecimal.valueOf(numLands)));

      table
          .nextRow()
          .nextCell("LAND" + (numLands > 1 ? "x" + numLands : ""))
          .nextCell(String.format("%.00f", landFloor.multiply(BigDecimal.valueOf(numLands))))
          .applyToCell(RIGHT_ALIGN.withWidth(7))
          .nextCell(String.format("$%.2f", usdLandFloor.multiply(BigDecimal.valueOf(numLands))))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(
              String.format(
                  "Ξ%.2f",
                  usdLandFloor.multiply(BigDecimal.valueOf(numLands)).doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));

      if (!header.isEmpty()) {
        header.append("\n");
      }
      header.append("LANDS: ");
      lands.forEach(s -> header.append("#").append(s.getId()).append(" "));
    }

    if (!vrooms.isEmpty()) {
      int numVrooms = vrooms.size();
      totalMagic = totalMagic.add(cheapestVroom.multiply(BigDecimal.valueOf(numVrooms)));
      totalUsd = totalUsd.add(usdCheapestVroom.multiply(BigDecimal.valueOf(numVrooms)));

      table
          .nextRow()
          .nextCell("VROOM" + (numVrooms > 1 ? "x" + numVrooms : ""))
          .nextCell(String.format("%.00f", cheapestVroom.multiply(BigDecimal.valueOf(numVrooms))))
          .applyToCell(RIGHT_ALIGN.withWidth(7))
          .nextCell(
              String.format("$%.2f", usdCheapestVroom.multiply(BigDecimal.valueOf(numVrooms))))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(
              String.format(
                  "Ξ%.2f",
                  usdCheapestVroom.multiply(BigDecimal.valueOf(numVrooms)).doubleValue()
                      / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));

      if (!header.isEmpty()) {
        header.append("\n");
      }
      header.append("VROOMS: ");
      vrooms.forEach(s -> header.append("#").append(s.getId()).append(" "));
    }

    if (!pets.isEmpty()) {
      int numPets = vrooms.size();
      totalMagic = totalMagic.add(petFloor.multiply(BigDecimal.valueOf(numPets)));
      totalUsd = totalUsd.add(petFloor.multiply(BigDecimal.valueOf(numPets)));

      table
          .nextRow()
          .nextCell("SMOLPET" + (numPets > 1 ? "x" + numPets : ""))
          .nextCell(String.format("%.00f", petFloor.multiply(BigDecimal.valueOf(numPets))))
          .applyToCell(RIGHT_ALIGN.withWidth(7))
          .nextCell(String.format("$%.2f", usdPetFloor.multiply(BigDecimal.valueOf(numPets))))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(
              String.format(
                  "Ξ%.2f",
                  usdPetFloor.multiply(BigDecimal.valueOf(numPets)).doubleValue() / ethMktPrice))
          .applyToCell(RIGHT_ALIGN.withWidth(7));

      if (!header.isEmpty()) {
        header.append("\n");
      }
      header.append("PETS: ");
      vrooms.forEach(s -> header.append("#").append(s.getId()).append(" "));
    }

    table
        .nextRow()
        .nextCell("TOTAL")
        .nextCell(String.format("%.00f", totalMagic))
        .applyToCell(RIGHT_ALIGN.withWidth(7))
        .nextCell(String.format("$%.2f", totalUsd))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("Ξ%.2f", totalUsd.doubleValue() / ethMktPrice))
        .applyToCell(RIGHT_ALIGN.withWidth(7));

    final var output = "```\n" + Utilities.simpleTableToString(table) + "```";

    EmbedCreateSpec embed =
        EmbedCreateSpec.builder()
            .title(
                "Smol Floor for "
                    + existingFloor.getDiscordId()
                    + "\nTreasure Marketplace\nMAGIC: $"
                    + currentPrice)
            .author(
                "SmolBot",
                null,
                "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
            .description(header + "\n\n" + output)
            .build();

    event
        .createFollowup(
            InteractionFollowupCreateSpec.builder().addEmbed(embed).ephemeral(true).build())
        .block();

    return Mono.empty();
  }

  private Optional<UserFloor> handleAddOrRemove(
      UserFloor existingFloor, boolean remove, ApplicationCommandInteractionOption options) {
    final Long id = Utilities.getOptionLong(options, "id").orElse(null);
    final String typeStr = Utilities.getOptionString(options, "type").orElse("smol");
    final SmolType type = smolTypeFromString(typeStr);

    String address = null;

    if (type == SmolType.FAMILY) {
      try {
        address = smolBrainsContract.ownerOf(new BigInteger(String.valueOf(id))).send();
      } catch (Exception ex) {
        ex.printStackTrace();
        return Optional.empty();
      }
    }

    switch (type) {
      case SMOL:
        if (remove) {
          existingFloor.getSmols().remove(id);
        } else {
          existingFloor.getSmols().add(id);
        }
        break;
      case SMOL_BODY:
        if (remove) {
          existingFloor.getSwols().remove(id);
        } else {
          existingFloor.getSwols().add(id);
        }
        break;
      case VROOM:
        if (remove) {
          existingFloor.getVrooms().remove(id);
        } else {
          existingFloor.getVrooms().add(id);
        }
        break;
      case LAND:
        if (remove) {
          existingFloor.getLand().remove(id);
        } else {
          existingFloor.getLand().add(id);
        }
        break;
      case PET:
        if (remove) {
          existingFloor.getPets().remove(id);
        } else {
          existingFloor.getPets().add(id);
        }
        break;
      case FAMILY:
        try {
          final BigInteger smolsBalance = smolBrainsContract.balanceOf(address).send();

          for (int x = 0; x < smolsBalance.intValue(); x++) {
            if (remove) {
              existingFloor
                  .getSmols()
                  .remove(
                      smolBrainsContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            } else {
              existingFloor
                  .getSmols()
                  .add(
                      smolBrainsContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          }

          final BigInteger vroomsBalance = vroomContract.balanceOf(address).send();

          for (int x = 0; x < vroomsBalance.intValue(); x++) {
            if (remove) {
              existingFloor
                  .getVrooms()
                  .remove(
                      vroomContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            } else {
              existingFloor
                  .getVrooms()
                  .add(
                      vroomContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          }

          final BigInteger swolsBalance = smolBodiesContract.balanceOf(address).send();

          for (int x = 0; x < swolsBalance.intValue(); x++) {
            if (remove) {
              existingFloor
                  .getSwols()
                  .remove(
                      smolBodiesContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            } else {
              existingFloor
                  .getSwols()
                  .add(
                      smolBodiesContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          }

          final BigInteger landBalance = smolLandContract.balanceOf(address).send();

          for (int x = 0; x < landBalance.intValue(); x++) {
            if (remove) {
              existingFloor
                  .getLand()
                  .remove(
                      smolLandContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            } else {
              existingFloor
                  .getLand()
                  .add(
                      smolLandContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          }

          // Pets
          final BigInteger petBalance = petsContract.balanceOf(address).send();

          for (int x = 0; x < petBalance.intValue(); x++) {
            if (remove) {
              existingFloor
                  .getPets()
                  .remove(
                      petsContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            } else {
              existingFloor
                  .getPets()
                  .add(
                      petsContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          return Optional.empty();
        }
    }

    return Optional.of(userFloorRepository.save(existingFloor));
  }

  private SmolType smolTypeFromString(String typeStr) {
    return switch (typeStr) {
      case "swol" -> SmolType.SMOL_BODY;
      case "vroom" -> VROOM;
      case "land" -> SmolType.LAND;
      case "family" -> SmolType.FAMILY;
      case "smolpet" -> SmolType.PET;
      default -> SmolType.SMOL;
    };
  }
}
