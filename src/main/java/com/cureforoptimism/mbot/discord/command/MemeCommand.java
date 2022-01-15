package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

  public MemeCommand(Utilities utilities) {
    this.utilities = utilities;

    try {
      imgMeme =
          ImageIO.read(new ClassPathResource("meme_distracted_boyfriend.jpg").getInputStream());
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
        final var imageSmol = utilities.getTransparentImage(tokenId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage output =
            new BufferedImage(imgMeme.getWidth(), imgMeme.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imgMeme, 0, 0, null);

        final var resized = Scalr.resize(imageSmol, 1400);
        graphics.drawImage(resized, -130, 205, null);
        graphics.dispose();

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
    return null;
  }
}
