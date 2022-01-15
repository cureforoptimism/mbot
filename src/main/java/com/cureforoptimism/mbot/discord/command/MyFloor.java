package com.cureforoptimism.mbot.discord.command;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.RIGHT_ALIGN;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.domain.Land;
import com.cureforoptimism.mbot.domain.Smol;
import com.cureforoptimism.mbot.domain.SmolBody;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.domain.UserFloor;
import com.cureforoptimism.mbot.domain.Vroom;
import com.cureforoptimism.mbot.repository.LandRepository;
import com.cureforoptimism.mbot.repository.SmolBodyRepository;
import com.cureforoptimism.mbot.repository.SmolRepository;
import com.cureforoptimism.mbot.repository.UserFloorRepository;
import com.cureforoptimism.mbot.repository.VroomRepository;
import com.cureforoptimism.mbot.service.TreasureService;
import com.inamik.text.tables.SimpleTable;
import com.smolbrains.SmolBodiesContract;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsVroomContract;
import com.smolbrains.SmolLandContract;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
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
    log.info("/myfloor received");

    var existingFloor =
        userFloorRepository.findByDiscordUserId(event.getInteraction().getUser().getId().asLong());

    event.deferReply().withEphemeral(true).block();

    if (event.getOption("remove").isPresent() && existingFloor != null) {
      final var removeOptions = event.getOption("remove").get();
      final Long addId = Utilities.getOptionLong(removeOptions, "id").orElse(null);
      final String typeStr = Utilities.getOptionString(removeOptions, "type").orElse("smol");
      final SmolType type =
          switch (typeStr) {
            case "swol" -> SmolType.SMOL_BODY;
            case "vroom" -> SmolType.VROOM;
            case "land" -> SmolType.LAND;
            case "family" -> SmolType.FAMILY;
            default -> SmolType.SMOL;
          };

      String address = null;

      if (type == SmolType.FAMILY) {
        try {
          address = smolBrainsContract.ownerOf(new BigInteger(String.valueOf(addId))).send();
        } catch (Exception ex) {
          ex.printStackTrace();
          return Mono.empty();
        }
      }

      switch (type) {
        case SMOL -> existingFloor.getSmols().remove(addId);
        case SMOL_BODY -> existingFloor.getSwols().remove(addId);
        case VROOM -> existingFloor.getVrooms().remove(addId);
        case LAND -> existingFloor.getLand().remove(addId);
        case FAMILY -> {
          try {
            final BigInteger smolsBalance = smolBrainsContract.balanceOf(address).send();

            for (int x = 0; x < smolsBalance.intValue(); x++) {
              try {
                existingFloor
                    .getSmols()
                    .remove(
                        smolBrainsContract
                            .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                            .send()
                            .longValue());
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            return Mono.empty();
          }

          try {
            final BigInteger vroomsBalance = vroomContract.balanceOf(address).send();

            for (int x = 0; x < vroomsBalance.intValue(); x++) {
              try {
                existingFloor
                    .getVrooms()
                    .remove(
                        vroomContract
                            .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                            .send()
                            .longValue());
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            return Mono.empty();
          }

          try {
            final BigInteger swolsBalance = smolBodiesContract.balanceOf(address).send();

            for (int x = 0; x < swolsBalance.intValue(); x++) {
              existingFloor
                  .getSwols()
                  .remove(
                      smolBodiesContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          } catch (Exception e) {
            e.printStackTrace();
            return Mono.empty();
          }

          try {
            final BigInteger landBalance = smolLandContract.balanceOf(address).send();

            for (int x = 0; x < landBalance.intValue(); x++) {
              existingFloor
                  .getLand()
                  .remove(
                      smolLandContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue());
            }
          } catch (Exception e) {
            e.printStackTrace();
            return Mono.empty();
          }
        }
      }

      existingFloor = userFloorRepository.save(existingFloor);
    }

    if (event.getOption("add").isPresent()) {
      final var addOptions = event.getOption("add").get();
      final Long addId = Utilities.getOptionLong(addOptions, "id").orElse(null);
      final String typeStr = Utilities.getOptionString(addOptions, "type").orElse("smol");
      final SmolType type =
          switch (typeStr) {
            case "swol" -> SmolType.SMOL_BODY;
            case "vroom" -> SmolType.VROOM;
            case "land" -> SmolType.LAND;
            case "family" -> SmolType.FAMILY;
            default -> SmolType.SMOL;
          };

      String address = null;

      if (type == SmolType.FAMILY) {

        if (existingFloor == null) {
          try {
            address = smolBrainsContract.ownerOf(new BigInteger(String.valueOf(addId))).send();

            final var floorBuilder =
                UserFloor.builder()
                    .discordId(
                        event.getInteraction().getUser().getUsername()
                            + "#"
                            + event.getInteraction().getUser().getDiscriminator())
                    .discordUserId(event.getInteraction().getUser().getId().asLong())
                    .smols(new HashSet<>())
                    .swols(new HashSet<>())
                    .land(new HashSet<>())
                    .vrooms(new HashSet<>());

            final BigInteger smolsBalance = smolBrainsContract.balanceOf(address).send();
            if (smolsBalance.intValue() > 0) {
              Set<Long> smolIds = new TreeSet<>();
              for (int x = 0; x < smolsBalance.intValue(); x++) {
                smolIds.add(
                    smolBrainsContract
                        .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                        .send()
                        .longValue());
              }
              floorBuilder.smols(smolIds);
            }

            final BigInteger vroomsBalance = vroomContract.balanceOf(address).send();
            if (vroomsBalance.intValue() > 0) {
              Set<Long> vroomIds = new TreeSet<>();
              for (int x = 0; x < vroomsBalance.intValue(); x++) {
                vroomIds.add(
                    vroomContract
                        .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                        .send()
                        .longValue());
              }
              floorBuilder.vrooms(vroomIds);
            }

            final BigInteger swolsBalance = smolBodiesContract.balanceOf(address).send();
            if (swolsBalance.intValue() > 0) {
              Set<Long> swolIds = new TreeSet<>();
              for (int x = 0; x < swolsBalance.intValue(); x++) {
                swolIds.add(
                    smolBodiesContract
                        .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                        .send()
                        .longValue());
              }
              floorBuilder.swols(swolIds);
            }

            final BigInteger landBalance = smolLandContract.balanceOf(address).send();
            if (landBalance.intValue() > 0) {
              Set<Long> landIds = new TreeSet<>();
              for (int x = 0; x < landBalance.intValue(); x++) {
                landIds.add(
                    smolLandContract
                        .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                        .send()
                        .longValue());
              }

              floorBuilder.land(landIds);
            }

            existingFloor = userFloorRepository.save(floorBuilder.build());
          } catch (Exception ex) {
            ex.printStackTrace();
            return Mono.empty();
          }
        } else {
          switch (type) {
            case SMOL -> existingFloor.getSmols().add(addId);
            case SMOL_BODY -> existingFloor.getSwols().add(addId);
            case VROOM -> existingFloor.getVrooms().add(addId);
            case LAND -> existingFloor.getLand().add(addId);
            case FAMILY -> {
              try {
                address = smolBrainsContract.ownerOf(new BigInteger(String.valueOf(addId))).send();

                final BigInteger smolsBalance = smolBrainsContract.balanceOf(address).send();

                for (int x = 0; x < smolsBalance.intValue(); x++) {
                  final var smolId =
                      smolBrainsContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue();

                  if (!existingFloor.getSmols().contains(smolId)) {
                    existingFloor.getSmols().add(smolId);
                  }
                }

                final BigInteger vroomsBalance = vroomContract.balanceOf(address).send();

                for (int x = 0; x < vroomsBalance.intValue(); x++) {
                  final var vroomId =
                      vroomContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue();

                  if (!existingFloor.getVrooms().contains(vroomId)) {
                    existingFloor.getVrooms().add(vroomId);
                  }
                }

                final BigInteger swolsBalance = smolBodiesContract.balanceOf(address).send();

                for (int x = 0; x < swolsBalance.intValue(); x++) {
                  final var swolId =
                      smolBodiesContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue();

                  if (!existingFloor.getSwols().contains(swolId)) {
                    existingFloor.getSwols().add(swolId);
                  }
                }

                final BigInteger landBalance = smolLandContract.balanceOf(address).send();
                for (int x = 0; x < landBalance.intValue(); x++) {
                  final var landId =
                      smolLandContract
                          .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                          .send()
                          .longValue();

                  if (!existingFloor.getLand().contains(landId)) {
                    existingFloor.getLand().add(landId);
                  }
                }
              } catch (Exception ex) {
                ex.printStackTrace();
                return Mono.empty();
              }
            }
          }
        }
      }
    }
    existingFloor = userFloorRepository.save(existingFloor);

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

    BigDecimal totalMagic = BigDecimal.ZERO;
    BigDecimal totalUsd = BigDecimal.ZERO;

    final StringBuilder header = new StringBuilder();
    final SimpleTable table =
        new SimpleTable()
            .nextRow()
            .nextCell("TYPE")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("MAGIC")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12))
            .nextCell("USD")
            .applyToCell(HORIZONTAL_CENTER.withWidth(12));
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
            .nextCell(String.format("%.2f", cheapestMale.multiply(BigDecimal.valueOf(numMales))))
            .applyToCell(RIGHT_ALIGN.withWidth(12))
            .nextCell(
                String.format("$%.2f", usdCheapestMale.multiply(BigDecimal.valueOf(numMales))))
            .applyToCell(RIGHT_ALIGN.withWidth(12));
      }

      if (numLadies > 0) {
        totalMagic = totalMagic.add(cheapestMale.multiply(BigDecimal.valueOf(numLadies)));
        totalUsd = totalUsd.add(usdCheapestFemale.multiply(BigDecimal.valueOf(numLadies)));

        table
            .nextRow()
            .nextCell("SMOL LADY" + (numLadies > 1 ? "x" + numLadies : ""))
            .nextCell(String.format("%.2f", cheapestFemale.multiply(BigDecimal.valueOf(numLadies))))
            .applyToCell(RIGHT_ALIGN.withWidth(12))
            .nextCell(
                String.format("$%.2f", usdCheapestFemale.multiply(BigDecimal.valueOf(numLadies))))
            .applyToCell(RIGHT_ALIGN.withWidth(12));
      }
    }

    if (!swols.isEmpty()) {
      int numSwols = swols.size();
      totalMagic = totalMagic.add(bodyFloor.multiply(BigDecimal.valueOf(numSwols)));
      totalUsd = totalUsd.add(usdBodyFloor.multiply(BigDecimal.valueOf(numSwols)));

      table
          .nextRow()
          .nextCell("SMOLBODY" + (numSwols > 1 ? "x" + numSwols : ""))
          .nextCell(String.format("%.2f", bodyFloor))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(String.format("$%.2f", usdBodyFloor.multiply(BigDecimal.valueOf(numSwols))))
          .applyToCell(RIGHT_ALIGN.withWidth(12));

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
          .nextCell(String.format("%.2f", landFloor.multiply(BigDecimal.valueOf(numLands))))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(String.format("$%.2f", usdLandFloor.multiply(BigDecimal.valueOf(numLands))))
          .applyToCell(RIGHT_ALIGN.withWidth(12));

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
          .nextCell(String.format("%.2f", cheapestVroom.multiply(BigDecimal.valueOf(numVrooms))))
          .applyToCell(RIGHT_ALIGN.withWidth(12))
          .nextCell(
              String.format("$%.2f", usdCheapestVroom.multiply(BigDecimal.valueOf(numVrooms))))
          .applyToCell(RIGHT_ALIGN.withWidth(12));

      if (!header.isEmpty()) {
        header.append("\n");
      }
      header.append("VROOMS: ");
      vrooms.forEach(s -> header.append("#").append(s.getId()).append(" "));
    }

    table
        .nextRow()
        .nextCell("TOTAL")
        .nextCell(String.format("%.2f", totalMagic))
        .applyToCell(RIGHT_ALIGN.withWidth(12))
        .nextCell(String.format("$%.2f", totalUsd))
        .applyToCell(RIGHT_ALIGN.withWidth(12));

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
}
