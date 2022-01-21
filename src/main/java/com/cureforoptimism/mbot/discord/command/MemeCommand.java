package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MemeCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage imgMeme;
  private BufferedImage imgMcDonaldsHat;
  private BufferedImage imgMcDonaldsBg;

  public MemeCommand(Utilities utilities) {
    this.utilities = utilities;

    try {
      imgMeme =
          ImageIO.read(new ClassPathResource("meme_distracted_boyfriend.jpg").getInputStream());
      imgMcDonaldsHat = ImageIO.read(new ClassPathResource("mcdonalds_hat.png").getInputStream());
      imgMcDonaldsBg =
          ImageIO.read(new ClassPathResource("mcdonalds_background.png").getInputStream());

    } catch (Exception ex) {
      log.error(ex.getMessage());
      System.exit(-1);
    }
  }

  @Override
  public String getName() {
    return "meme";
  }

  @Override
  public String getDescription() {
    return "WHO KNOWS?!?";
  }

  @Override
  public String getUsage() {
    return "<token_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length >= 2) {
      String tokenId = parts[1];

      try {
        Long.parseLong(tokenId);
      } catch (Exception ex) {
        return Mono.empty();
      }

      try {
        final var output = getDistracted(tokenId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(output, "jpg", outputStream);

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title("What the Smol?!?")
                          .image("attachment://wts_" + tokenId + ".jpg")
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              "wts_" + tokenId + ".jpg",
                              new ByteArrayInputStream(outputStream.toByteArray()))
                          .addEmbed(embed)
                          .build());
                });
      } catch (Exception ex) {
        log.error("Exception memeing" + ex.getMessage());
      }
    }
    return null;
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    event.deferReply().block();

    final Long id = Utilities.getOptionLong(event, "id").orElse(null);
    final String typeStr = Utilities.getOptionString(event, "type").orElse("mcds");

    if (id == null) {
      return Mono.empty();
    }

    try {
      BufferedImage output =
          switch (typeStr) {
            case "distracted" -> getDistracted(id.toString());
            default -> getWagie(id.toString());
          };

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(output, "jpg", outputStream);

      final var embed =
          EmbedCreateSpec.builder()
              .title("What the Smol?!?")
              .image("attachment://wts_" + id + ".jpg")
              .build();

      event
          .createFollowup(
              InteractionFollowupCreateSpec.builder()
                  .addFile(
                      "wts_" + id + ".jpg", new ByteArrayInputStream(outputStream.toByteArray()))
                  .addEmbed(embed)
                  .build())
          .block();
    } catch (Exception ex) {
      log.error("Exception memeing" + ex.getMessage());
    }

    return Mono.empty();
  }

  private BufferedImage getWagie(String id) throws URISyntaxException, IOException {
    final BufferedImage imageSmol = utilities.getTransparentImage(id, true);
    final var output =
        new BufferedImage(
            imgMcDonaldsBg.getWidth(), imgMcDonaldsBg.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = output.createGraphics();
    graphics.setComposite(AlphaComposite.SrcOver);
    graphics.drawImage(imgMcDonaldsBg, 0, 0, null);

    final var resized = Scalr.resize(imageSmol, 500);
    graphics.drawImage(resized, 210, 110, null);
    graphics.drawImage(imgMcDonaldsHat, 370, 275, null);
    graphics.dispose();

    return output;
  }

  private BufferedImage getDistracted(String id) throws URISyntaxException, IOException {
    final BufferedImage imageSmol = utilities.getTransparentImage(id);
    final var output =
        new BufferedImage(imgMeme.getWidth(), imgMeme.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = output.createGraphics();
    g.setComposite(AlphaComposite.SrcOver);
    g.drawImage(imgMeme, 0, 0, null);

    final var r = Scalr.resize(imageSmol, 1400);
    g.drawImage(r, -130, 205, null);
    g.dispose();

    return output;
  }
}
