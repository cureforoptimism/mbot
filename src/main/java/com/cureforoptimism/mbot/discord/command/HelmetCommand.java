package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class HelmetCommand implements MbotCommand {
  private final Utilities utilities;
  private final TraitsRepository traitsRepository;
  Map<String, BufferedImage> helmetMap;

  public HelmetCommand(Utilities utilities, TraitsRepository traitsRepository) {
    this.utilities = utilities;
    this.traitsRepository = traitsRepository;
    this.helmetMap = new HashMap<>();

    try {
      this.helmetMap.put(
          "smol_male",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/male/smol_helmet_male.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_male_cyber",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/male/smol_helmet_male_cyber.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_male_eyepatch",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/male/smol_helmet_male_eyepatch.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_male_glasses",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/male/smol_helmet_male_glasses.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_male_monocle",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/male/smol_helmet_male_monocle.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_male_sunglasses",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/male/smol_helmet_male_sunglasses.png")
                  .getInputStream()));

      this.helmetMap.put(
          "smol_female",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/female/smol_helmet_female.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_female_cyber-glasses",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/female/smol_helmet_female_cyber.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_female_band",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/female/smol_helmet_female_headband.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_female_sunglasses",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/female/smol_helmet_female_sunglasses.png")
                  .getInputStream()));
      this.helmetMap.put(
          "smol_female_vr",
          ImageIO.read(
              new ClassPathResource("helmets/smolbrains/female/smol_helmet_female_vr.png")
                  .getInputStream()));

      this.helmetMap.put(
          "swol_all_helmet",
          ImageIO.read(
              new ClassPathResource("helmets/smolbodies/swol_helmet_all.png").getInputStream()));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "helmet";
  }

  @Override
  public String getDescription() {
    return "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE";
  }

  @Override
  public String getUsage() {
    return "[smol|swol] <token_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!license command recieved");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length >= 2) {
      String tokenId = parts[1];

      try {
        Long.parseLong(tokenId);
      } catch (Exception ex) {
        return Mono.empty();
      }

      SmolType smolType = SmolType.SMOL;

      if (parts.length >= 3) {
        String smolTypeStr = parts[2];

        smolType =
            switch (smolTypeStr.toLowerCase()) {
              case "swol", "body" -> SmolType.SMOL_BODY;
              default -> SmolType.SMOL;
            };
      }

      final var outputStream = getImageOutputStream(tokenId, smolType);

      return event
          .getMessage()
          .getChannel()
          .flatMap(
              c -> {
                final var embed =
                    EmbedCreateSpec.builder()
                        .title("EEEEEEEEEEEEEEEEEEE HELMETS")
                        .author(
                            "SmolBot",
                            null,
                            "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                        .image("attachment://" + tokenId + "helmet.png")
                        .build();

                return c.createMessage(
                    MessageCreateSpec.builder()
                        .addFile(
                            tokenId + "helmet.png",
                            new ByteArrayInputStream(outputStream.toByteArray()))
                        .addEmbed(embed)
                        .build());
              });
    }
    return Mono.empty();
  }

  private ByteArrayOutputStream getImageOutputStream(String tokenId, SmolType smolType) {
    try {
      BufferedImage imageSmol;
      BufferedImage genderOverlay = null;
      BufferedImage overlay = null;
      boolean pivotImage = false; // HACK: Mask misses a few pixels for gangster hat

      if (smolType == SmolType.SMOL) {
        List<Trait> traits = traitsRepository.findBySmol_Id(Long.parseLong(tokenId));

        String glasses = "";
        String gender = "";
        String hat = "";

        for (Trait trait : traits) {
          if (trait.getType().equals("Glasses")) {
            glasses = trait.getValue();
          } else if (trait.getType().equals("Gender")) {
            gender = trait.getValue();
          } else if (trait.getType().equals("Hat")) {
            hat = trait.getValue();
          }
        }

        if (hat.equalsIgnoreCase("gangster")) {
          pivotImage = true;
        }

        switch (gender.toLowerCase()) {
          case "male" -> {
            genderOverlay = helmetMap.get("smol_male");
            switch (glasses.toLowerCase()) {
              case "cyber", "eyepatch", "glasses", "monocle", "sunglasses" -> overlay =
                  helmetMap.get("smol_male_" + glasses.toLowerCase());
            }
          }
          case "female" -> {
            genderOverlay = helmetMap.get("smol_female");
            overlay =
                switch (glasses.toLowerCase()) {
                  case "cyber-glasses", "band", "sunglasses", "vr" -> helmetMap.get(
                      "smol_female_" + glasses.toLowerCase());
                  default -> overlay;
                };
          }
          default -> {
            log.error("unable to find gender for token ID" + tokenId);
            return null;
          }
        }

        try {
          imageSmol = utilities.getSmolBufferedImage(tokenId, SmolType.SMOL, true).get();
        } catch (Exception ex) {
          log.error("unable to retrieve smol for tokenId " + tokenId, ex);
          return null;
        }
      } else {
        try {
          imageSmol =
              utilities.getSmolBufferedImage(tokenId, SmolType.SMOL_BODY, false, true).get();
          genderOverlay = helmetMap.get("swol_all_helmet");
        } catch (Exception ex) {
          log.error("unable to retrieve swol for tokenId " + tokenId, ex);
          return null;
        }
      }

      final var backgroundColor = imageSmol.getRGB(0, 0);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BufferedImage output =
          new BufferedImage(
              imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = output.createGraphics();
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.drawImage(imageSmol, 0, 0, null);

      if (pivotImage) {
        graphics.drawImage(genderOverlay, 1, 1, null);
      } else {
        graphics.drawImage(genderOverlay, 0, 0, null);
      }

      // Replace green screen area with original background color
      final var greenScreenColor = output.getRGB(1, 1);

      ImageFilter imageFilter =
          new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
              if ((rgb | 0xFF000000) == greenScreenColor) {
                return backgroundColor;
              }

              return rgb;
            }
          };

      ImageProducer imageProducer = new FilteredImageSource(output.getSource(), imageFilter);
      Image imgSmol = Toolkit.getDefaultToolkit().createImage(imageProducer);
      graphics.drawImage(imgSmol, 0, 0, null);

      if (overlay != null) {
        graphics.drawImage(overlay, 0, 0, null);
      }

      graphics.dispose();

      ImageIO.write(output, "png", outputStream);
      return outputStream;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private InteractionFollowupCreateSpec getFollowup(String tokenId, SmolType smolType) {
    final var outputStream = getImageOutputStream(tokenId, smolType);

    return InteractionFollowupCreateSpec.builder()
        .addFile("helmet" + tokenId + ".png", new ByteArrayInputStream(outputStream.toByteArray()))
        .addEmbed(
            EmbedCreateSpec.builder()
                .title("Smols, start your engines!")
                .author(
                    "SmolBot",
                    null,
                    "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                .image("attachment://helmet" + tokenId + ".png")
                .build())
        .build();
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/helmet command recieved");
    event.deferReply().block();

    final var tokenId = Utilities.getOptionString(event, "id").orElse("");
    final var type = Utilities.getOptionString(event, "type");

    SmolType smolType = SmolType.SMOL;

    if (type.isPresent()) {
      smolType =
          switch (type.get()) {
            case "smol" -> SmolType.SMOL;
            case "swol" -> SmolType.SMOL_BODY;
            default -> SmolType.SMOL;
          };
    }

    event.createFollowup(getFollowup(tokenId, smolType)).block();

    return Mono.empty();
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
