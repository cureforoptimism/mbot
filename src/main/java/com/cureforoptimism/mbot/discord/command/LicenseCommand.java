package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LicenseCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage frame;

  public LicenseCommand(Utilities utilities) {
    this.utilities = utilities;

    try {
      this.frame = ImageIO.read(new ClassPathResource("license.png").getInputStream());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "license";
  }

  @Override
  public String getDescription() {
    return "Vroom vroom!";
  }

  @Override
  public String getUsage() {
    return "<token_id>";
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
              case "pet", "smolpet" -> SmolType.PET;
              case "swolpet", "bodypet" -> SmolType.BODY_PET;
              default -> SmolType.SMOL;
            };
      }

      try {
        final var imageSmol =
            utilities.getSmolBufferedImage(tokenId, smolType, false, true).orElse(null);
        if (imageSmol == null) {
          log.warn("Unable to retrieve on ! command");
          return Mono.empty();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Note: All frames are same dimensions; just use values for frameSmol
        BufferedImage output =
            new BufferedImage(
                this.frame.getWidth(), this.frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(frame, 0, 0, null);
        graphics.drawImage(Scalr.resize(imageSmol, Mode.FIT_EXACT, 230), 316, 272, null);

        graphics.dispose();

        ImageIO.write(output, "png", outputStream);

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title("Vroom vroom EEEEEEEEEEEEEEEEEEE")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .image("attachment://" + tokenId + "license.png")
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              tokenId + "license.png",
                              new ByteArrayInputStream(outputStream.toByteArray()))
                          .addEmbed(embed)
                          .build());
                });

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return Mono.empty();
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/license command recieved");

    event.deferReply().block();

    final var tokenId = Utilities.getOptionString(event, "id").orElse("");
    final var type = Utilities.getOptionString(event, "type");

    SmolType smolType = SmolType.SMOL;

    if (type.isPresent()) {
      smolType =
          switch (type.get()) {
            case "smol" -> SmolType.SMOL;
            case "smolpet" -> SmolType.PET;
            case "swol" -> SmolType.SMOL_BODY;
            case "swolpet" -> SmolType.BODY_PET;
            default -> SmolType.SMOL;
          };
    }

    event.createFollowup(getFollowup(tokenId, smolType)).block();

    return Mono.empty();
  }

  private InteractionFollowupCreateSpec getFollowup(String tokenId, SmolType smolType) {
    // TODO: Refactor for DRY - you're better than this, Cure
    try {
      final var imageSmol =
          utilities.getSmolBufferedImage(tokenId, smolType, false, true).orElse(null);
      if (imageSmol == null) {
        return InteractionFollowupCreateSpec.builder()
            .addEmbed(
                EmbedCreateSpec.builder()
                    .title("EEEEEEEEE")
                    .description(
                        "Sorry! Too many people are requesting smols and the Smolverse is angry! Try again!")
                    .author(
                        "SmolBot",
                        null,
                        "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                    .build())
            .build();
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      // Note: All frames are same dimensions; just use values for frameSmol
      BufferedImage output =
          new BufferedImage(
              this.frame.getWidth(), this.frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = output.createGraphics();
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.drawImage(frame, 0, 0, null);
      graphics.drawImage(Scalr.resize(imageSmol, Mode.FIT_EXACT, 230), 316, 272, null);

      graphics.dispose();

      ImageIO.write(output, "png", outputStream);

      return InteractionFollowupCreateSpec.builder()
          .addFile("license.png", new ByteArrayInputStream(outputStream.toByteArray()))
          .addEmbed(
              EmbedCreateSpec.builder()
                  .title("Vroom vroom EEEEEEEEEEEEEEEEEEE")
                  .author(
                      "SmolBot",
                      null,
                      "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                  .image("attachment://license.png")
                  .build())
          .build();

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
