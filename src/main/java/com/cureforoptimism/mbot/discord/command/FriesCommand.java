package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public class FriesCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage imgHat;

  public FriesCommand(Utilities utilities) {
    this.utilities = utilities;

    try {
      this.imgHat = ImageIO.read(new ClassPathResource("mcds.png").getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "fries";
  }

  @Override
  public String getDescription() {
    return "Welcome to SmolDonald's! May I take your order?";
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
        final var imageSmol = utilities.getTransparentImage(tokenId, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage output =
            new BufferedImage(
                imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imageSmol, 0, 0, null);
        graphics.drawImage(imgHat, 0, 0, null);

        graphics.dispose();

        ImageIO.write(output, "png", outputStream);

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title("Welcome to SmolDonald's! May I take your order?")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .image("attachment://" + tokenId + "mcds.png")
                          .addField(
                              "Notes",
                              "Stay safe out there, frens. Much <3 from Cure",
                              true)
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              tokenId + "mcds.png",
                              new ByteArrayInputStream(outputStream.toByteArray()))
                          .addEmbed(embed)
                          .build());
                });

      } catch (URISyntaxException | IOException e) {
        e.printStackTrace();
      }
    }
    return Mono.empty();
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    return null;
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
