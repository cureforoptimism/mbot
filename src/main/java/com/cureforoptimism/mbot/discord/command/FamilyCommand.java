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
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class FamilyCommand implements MbotCommand {
  private final Utilities utilities;
  private final SmolBrainsContract smolBrainsContract;
  private final SmolBrainsVroomContract smolBrainsVroomContract;
  private final TreasureService treasureService;
  private final RarityRankRepository rarityRankRepository;
  private final VroomRarityRankRepository vroomRarityRankRepository;
  private BufferedImage imgGalaxy;

  public FamilyCommand(
      Utilities utilities,
      SmolBrainsContract smolBrainsContract,
      SmolBrainsVroomContract smolBrainsVroomContract,
      TreasureService treasureService,
      RarityRankRepository rarityRankRepository,
      VroomRarityRankRepository vroomRarityRankRepository) {
    this.utilities = utilities;
    this.smolBrainsContract = smolBrainsContract;
    this.smolBrainsVroomContract = smolBrainsVroomContract;
    this.treasureService = treasureService;
    this.rarityRankRepository = rarityRankRepository;
    this.vroomRarityRankRepository = vroomRarityRankRepository;

    try {
      this.imgGalaxy = ImageIO.read(new ClassPathResource("galaxy.png").getInputStream());
    } catch (Exception ex) {
      log.error(ex.getMessage());
      System.exit(-1);
    }
  }

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
        StringBuilder vroomsDescription = new StringBuilder();

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
          vroomsDescription.append("\n\nVROOMS\n");
          for (Integer vroomId : vroomIds) {
            VroomRarityRank rarityRank =
                vroomRarityRankRepository.findBySmolId(vroomId.longValue());
            vroomsDescription
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

        // Let's make the backgrounds of all the smols transparent (we should replace with one of
        // the background colors later)
        List<BufferedImage> smolImagesTransparent = new ArrayList<>();
        for (BufferedImage smolImage : smolImages) {
          final var transparentColor = smolImage.getRGB(0, 0);
          ImageFilter imageFilter =
              new RGBImageFilter() {
                @Override
                public int filterRGB(int x, int y, int rgb) {
                  if ((rgb | 0xFF000000) == transparentColor) {
                    return 0x00FFFFFF & rgb;
                  }

                  return rgb;
                }
              };

          ImageProducer imageProducer = new FilteredImageSource(smolImage.getSource(), imageFilter);
          smolImagesTransparent.add(
              imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(imageProducer)));
        }

        final var maxSmolWidths = (smolImagesTransparent.size() * 130) + 130;

        BufferedImage output = new BufferedImage(maxSmolWidths, 350, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(Scalr.resize(imgGalaxy, Mode.FIT_EXACT, maxSmolWidths), 0, 0, null);

        int xOffset = smolImages.size() * 130;
        for (BufferedImage smolImage : smolImagesTransparent) {
          xOffset -= 130;
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(smolImage, xOffset - 55, 0, null);
        }

        ByteArrayOutputStream smolOutputStream = new ByteArrayOutputStream();
        ImageIO.write(output, "png", smolOutputStream);

        graphics.dispose();

        output = new BufferedImage(vroomIds.size() * 350, 350, BufferedImage.TYPE_INT_ARGB);
        graphics = output.createGraphics();

        xOffset = 0;
        for (BufferedImage vroomImage : vroomImages) {
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(vroomImage, xOffset, 0, null);
          xOffset += vroomImage.getWidth();
        }

        ByteArrayOutputStream vroomOutputStream = new ByteArrayOutputStream();
        ImageIO.write(output, "png", vroomOutputStream);

        graphics.dispose();

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  EmbedCreateSpec embed =
                      EmbedCreateSpec.builder()
                          .title("Smol Family")
                          .description(description.toString())
                          .image("attachment://smol_damned_family.png")
                          .build();

                  final var response =
                      MessageCreateSpec.builder()
                          .addFile(
                              "smol_damned_family.png",
                              new ByteArrayInputStream(smolOutputStream.toByteArray()))
                          .addEmbed(embed);

                  if (!vroomIds.isEmpty()) {
                    response.addEmbed(
                        EmbedCreateSpec.builder()
                            .title("Vrooms")
                            .description(vroomsDescription.toString())
                            .image("attachment://family_vrooms.png")
                            .timestamp(Instant.now())
                            .build());
                    response.addFile(
                        "family_vrooms.png",
                        new ByteArrayInputStream(vroomOutputStream.toByteArray()));
                  }

                  return c.createMessage(response.build());
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

  private static BufferedImage imageToBufferedImage(Image image) {
    BufferedImage bufferedImage =
        new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bufferedImage.createGraphics();
    g2.drawImage(image, 0, 0, null);
    g2.dispose();

    return bufferedImage;
  }
}
