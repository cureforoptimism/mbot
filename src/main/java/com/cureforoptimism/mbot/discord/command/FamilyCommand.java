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
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.*;
import java.awt.image.*;
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
  private BufferedImage imgMoonSurface;
  private BufferedImage imgBackground;
  private BufferedImage imgIntergalactic;

  private static class FamilyResponse {
    EmbedCreateSpec familyPhoto;
    byte[] familyPhotoBytes;
    EmbedCreateSpec vrooms;
    byte[] vroomPhotoBytes;
  }

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
      this.imgBackground = ImageIO.read(new ClassPathResource("lasers.png").getInputStream());
      this.imgMoonSurface =
          ImageIO.read(new ClassPathResource("moon_surface_bone.png").getInputStream());
      this.imgIntergalactic =
          ImageIO.read(new ClassPathResource("intergalactic.jpg").getInputStream());
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
    return "Display all the things in the Smolverse in this Smol's family";
  }

  @Override
  public String getUsage() {
    return "<smol_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    String tokenId = parts[1];
    String background = null;
    boolean forceSmolBrain = false;
    if (parts.length >= 3) {
      for (int x = 2; x < parts.length; x++) {
        if (parts[x].equalsIgnoreCase("smol")) {
          forceSmolBrain = true;
        } else {
          background = parts[x];
        }
      }
    }

    final var familyResponse = getFamilyResponse(tokenId, background, forceSmolBrain);
    if (familyResponse == null) {
      log.warn("Unable to retrieve family for " + tokenId);
      return Mono.empty();
    }

    final var response =
        MessageCreateSpec.builder()
            .addFile(
                "smol_damned_family.png", new ByteArrayInputStream(familyResponse.familyPhotoBytes))
            .addEmbed(familyResponse.familyPhoto);

    if (familyResponse.vrooms != null) {
      response.addFile(
          "family_vrooms.png", new ByteArrayInputStream(familyResponse.vroomPhotoBytes));
      response.addEmbed(familyResponse.vrooms);
    }

    return event.getMessage().getChannel().flatMap(c -> c.createMessage(response.build()));
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/family command received");

    final var tokenIdOption = event.getOption("id").orElse(null);
    if (tokenIdOption == null || tokenIdOption.getValue().isEmpty()) {
      return Mono.empty();
    }

    final var tokenId = Utilities.getOptionString(event, "id").orElse(null);
    final var background = Utilities.getOptionString(event, "background").orElse(null);
    final var forceSmolBrain = Utilities.getOptionBoolean(event, "smolbrain").orElse(false);

    event.deferReply().block();
    event.createFollowup(getFamilyFollowUp(tokenId, background, forceSmolBrain)).block();

    return Mono.empty();
  }

  private InteractionFollowupCreateSpec getFamilyFollowUp(
      String tokenId, String background, boolean forceSmolBrain) {
    final var familyResponse = getFamilyResponse(tokenId, background, forceSmolBrain);
    if (familyResponse == null) {
      log.warn("Unable to retrieve family for " + tokenId);
      return InteractionFollowupCreateSpec.builder()
          .content(
              "Something went wrong. Try again, and if you think this is an error, whack smol bot with a wrench and tell Cure For Optimism about it")
          .build();
    }

    final var response =
        InteractionFollowupCreateSpec.builder()
            .addFile(
                "smol_damned_family.png", new ByteArrayInputStream(familyResponse.familyPhotoBytes))
            .addEmbed(familyResponse.familyPhoto);

    if (familyResponse.vrooms != null) {
      response.addFile(
          "family_vrooms.png", new ByteArrayInputStream(familyResponse.vroomPhotoBytes));
      response.addEmbed(familyResponse.vrooms);
    }

    return response.build();
  }

  private FamilyResponse getFamilyResponse(
      String tokenId, String background, boolean forceSmolBrain) {
    BufferedImage bgImage = this.imgIntergalactic;

    if (background != null) {
      bgImage =
          switch (background) {
            case "lasers" -> this.imgBackground;
            case "galaxy" -> this.imgGalaxy;
            case "moon" -> this.imgMoonSurface;
            default -> this.imgIntergalactic;
          };
    }

    boolean isTwitterBanner = bgImage.equals(this.imgIntergalactic);

    try {
      final var address = smolBrainsContract.ownerOf(new BigInteger(tokenId)).send();

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
        return null;
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
          VroomRarityRank rarityRank = vroomRarityRankRepository.findBySmolId(vroomId.longValue());
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
                var imgOpt =
                    utilities.getSmolBufferedImage(id.toString(), SmolType.SMOL, forceSmolBrain);
                imgOpt.ifPresent(smolImages::add);
              });

      final List<BufferedImage> vroomImages = new ArrayList<>();
      vroomIds.parallelStream()
          .forEach(
              id -> {
                var imgOpt = utilities.getSmolBufferedImage(id.toString(), SmolType.VROOM, false);
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

      BufferedImage output;

      if (isTwitterBanner) {
        output = new BufferedImage(1500, 500, BufferedImage.TYPE_INT_ARGB);
      } else {
        output = new BufferedImage(maxSmolWidths, 350, BufferedImage.TYPE_INT_ARGB);
      }

      Graphics2D graphics = output.createGraphics();

      graphics.setComposite(AlphaComposite.SrcOver);

      int xOffset;
      if (isTwitterBanner) {
        // Assume this JPG is already 1500x500; we can resize other ones in the future, if they're
        // not.
        graphics.drawImage(bgImage, 0, 0, null);

        xOffset = 1300;
        for (BufferedImage smolImage : smolImagesTransparent) {
          xOffset -= 130;
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(smolImage, xOffset - 40, 150, null);
        }
      } else {
        graphics.drawImage(Scalr.resize(bgImage, Mode.FIT_EXACT, maxSmolWidths), 0, 0, null);

        xOffset = smolImages.size() * 130;
        for (BufferedImage smolImage : smolImagesTransparent) {
          xOffset -= 130;
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(smolImage, xOffset - 40, 0, null);
        }
      }

      ByteArrayOutputStream smolOutputStream = new ByteArrayOutputStream();
      ImageIO.write(output, "png", smolOutputStream);

      graphics.dispose();

      ByteArrayOutputStream vroomOutputStream = new ByteArrayOutputStream();
      if (!vroomIds.isEmpty()) {
        output = new BufferedImage(vroomIds.size() * 350, 350, BufferedImage.TYPE_INT_ARGB);
        graphics = output.createGraphics();

        xOffset = 0;
        for (BufferedImage vroomImage : vroomImages) {
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(vroomImage, xOffset, 0, null);
          xOffset += vroomImage.getWidth();
        }

        ImageIO.write(output, "png", vroomOutputStream);

        graphics.dispose();
      }

      FamilyResponse familyResponse = new FamilyResponse();
      familyResponse.familyPhotoBytes = smolOutputStream.toByteArray();
      familyResponse.familyPhoto =
          EmbedCreateSpec.builder()
              .title("Smol Family")
              .description(description.toString())
              .image("attachment://smol_damned_family.png")
              .build();

      if (!vroomIds.isEmpty()) {
        familyResponse.vrooms =
            EmbedCreateSpec.builder()
                .title("Vrooms")
                .description(vroomsDescription.toString())
                .image("attachment://family_vrooms.png")
                .timestamp(Instant.now())
                .build();
        familyResponse.vroomPhotoBytes = vroomOutputStream.toByteArray();
      }

      return familyResponse;
    } catch (Exception ex) {
      // TODO: I should really stop writing code in a hurry and properly handle specific
      // exceptions
      log.error("Error retrieving profile", ex);
      return null;
    }
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
