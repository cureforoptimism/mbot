package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.domain.VroomRarityRank;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import com.cureforoptimism.mbot.repository.VroomRarityRankRepository;
import com.cureforoptimism.mbot.service.TreasureService;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsVroomContract;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class FamilyCommand implements MbotCommand {
  private final Utilities utilities;
  private final SmolBrainsContract smolBrainsContract;
  private final SmolBrainsVroomContract smolBrainsVroomContract;
  private final TreasureService treasureService;
  private final RarityRankRepository rarityRankRepository;
  private final VroomRarityRankRepository vroomRarityRankRepository;

  @Override
  public String getName() {
    return "family";
  }

  @Override
  public String getDescription() {
    return "Display all the things in the Smoliverse in this Smol's family";
  }

  @Override
  public String getUsage() {
    return "<smol_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2) {
      try {

        final var address = smolBrainsContract.ownerOf(new BigInteger(parts[1])).send();

        // Get all smols
        List<Integer> smolIds = new ArrayList<>();
        final var smolsBalance = smolBrainsContract.balanceOf(address).send();
        for (int x = 0; x < smolsBalance.intValue(); x++) {
          smolIds.add(
              smolBrainsContract
                  .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                  .send()
                  .intValue());
        }

        // TODO: Verify that we can even get here
        if (smolIds.isEmpty()) {
          return Mono.empty();
        }

        // Get all vrooms
        List<Integer> vroomIds = new ArrayList<>();
        final var vroomsBalance = smolBrainsVroomContract.balanceOf(address).send();
        for (int x = 0; x < vroomsBalance.intValue(); x++) {
          vroomIds.add(
              smolBrainsVroomContract
                  .tokenOfOwnerByIndex(address, new BigInteger(String.valueOf(x)))
                  .send()
                  .intValue());
        }

        StringBuilder description = new StringBuilder();
        description.append("The happy family!\n\n").append("SMOLS\n");
        BigDecimal totalIq = BigDecimal.ZERO;
        for (Integer smolId : smolIds) {
          RarityRank rarityRank = rarityRankRepository.findBySmolId(smolId.longValue());

          BigDecimal iq = treasureService.getIq(smolId);
          totalIq = totalIq.add(iq);
          description
              .append("#")
              .append(smolId)
              .append(" (Rank ")
              .append(rarityRank.getRank())
              .append(") - ")
              .append(iq)
              .append(" IQ\n");
        }

        description.append("\nTotal IQ in family: ").append(totalIq);

        if (!vroomIds.isEmpty()) {
          description.append("\n\nVROOMS\n");
          for (Integer vroomId : vroomIds) {
            VroomRarityRank rarityRank =
                vroomRarityRankRepository.findBySmolId(vroomId.longValue());
            description
                .append("#")
                .append(vroomId)
                .append(" (Rank ")
                .append(rarityRank.getRank())
                .append(")\n");
          }
        }

        // Let's see how horrible of an idea it would be to build a composite image
        final List<BufferedImage> smolImages = new ArrayList<>();
        smolIds.parallelStream()
            .forEach(
                id -> {
                  var imgOpt = utilities.getSmolBufferedImage(id.toString(), SmolType.SMOL);
                  imgOpt.ifPresent(smolImages::add);
                });

        final List<BufferedImage> vroomImages = new ArrayList<>();
        vroomIds.parallelStream()
            .forEach(
                id -> {
                  var imgOpt = utilities.getSmolBufferedImage(id.toString(), SmolType.VROOM);
                  imgOpt.ifPresent(vroomImages::add);
                });

        final var maxWidth =
            smolImages.get(0).getWidth() * Math.max(smolImages.size(), vroomImages.size());
        final var maxHeight = smolImages.get(0).getHeight() * 2; // 2 is SMOL, VROOM
        BufferedImage output = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();

        int xOffset = 0;
        for (BufferedImage smolImage : smolImages) {
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(smolImage, xOffset, 0, null);
          xOffset += smolImage.getWidth();
        }

        xOffset = 0;
        int yOffset = smolImages.get(0).getHeight();
        for (BufferedImage vroomImage : vroomImages) {
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(vroomImage, xOffset, yOffset, null);
          xOffset += vroomImage.getWidth();
        }

        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(output, "png", outputStream);

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  EmbedCreateSpec embed =
                      EmbedCreateSpec.builder()
                          .title("Smol Family")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .description(description.toString())
                          .image("attachment://smol_damned_family.png")
                          .timestamp(Instant.now())
                          .build();
                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              "smol_damned_family.png",
                              new ByteArrayInputStream(outputStream.toByteArray()))
                          .addEmbed(embed)
                          .build());
                });
      } catch (Exception ex) {
        // TODO: I should really stop writing code in a hurry and properly handle specific
        // exceptions
        log.error("Error retrieving profile", ex);
        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c ->
                    c.createMessage(
                        "The smart contract shows no record of "
                            + parts[1]
                            + " existing, and smol bot has no idea why!"));
      }
    }

    return Mono.empty();
  }
}
