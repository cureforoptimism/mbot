package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
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
import java.net.URI;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SpaceCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage suitDefault;

  public SpaceCommand(Utilities utilities) {
    this.utilities = utilities;

    try {
      this.suitDefault =
          ImageIO.read(new ClassPathResource("smol-space-suit.png").getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "space";
  }

  @Override
  public String getDescription() {
    return "Ground control to major Smol! Suit up for space! Art by `thegall#5404`";
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
        final var smolUri =
            new URI(utilities.getSmolImage(tokenId, SmolType.SMOL, false).orElse(""));
        final var imageSmol = ImageIO.read(smolUri.toURL());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        BufferedImage output =
            new BufferedImage(
                imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);

        graphics.drawImage(imageSmol, 0, 0, null);
        graphics.drawImage(suitDefault, 0, 0, null);

        graphics.dispose();

        ImageIO.write(output, "png", outputStream);

        ByteArrayOutputStream finalOutputStream = outputStream;
        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title(
                              "Captain's Log, Smoldate 53391.3: The Smolerprise is departing from Land. Crew includes #"
                                  + tokenId
                                  + "!")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .image("attachment://" + tokenId + "space.")
                          .addField(
                              "Notes",
                              "Ready for lift off! From `thegall#5404` x `Cure For Optimism`",
                              true)
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              tokenId + "space.png",
                              new ByteArrayInputStream(finalOutputStream.toByteArray()))
                          .addEmbed(embed)
                          .build());
                });

      } catch (URISyntaxException | IOException e) {
        e.printStackTrace();
      }
    }
    return Mono.empty();
  }
}