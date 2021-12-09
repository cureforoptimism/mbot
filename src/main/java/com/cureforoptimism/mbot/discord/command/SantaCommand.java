package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Slf4j
public class SantaCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage imageHat = null;

  public SantaCommand(Utilities utilities) {
    this.utilities = utilities;

    var hatRes = new ClassPathResource("santa-hat.png");

    try {
      imageHat = ImageIO.read(hatRes.getInputStream());
    } catch (Exception ex) {
      log.error("Unable to process images");
    }
  }

  @Override
  public String getName() {
    return "santa";
  }

  @Override
  public String getDescription() {
    return "Ho ho ho with a smol smol smol!";
  }

  @Override
  public String getUsage() {
    return "<token_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2) {
      String tokenId = parts[1];

      try {
        final var smolUri = new URI(utilities.getSmolImage(tokenId, SmolType.SMOL).orElse(""));
        final var imageSmol = ImageIO.read(smolUri.toURL());

        BufferedImage output =
            new BufferedImage(
                imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imageSmol, 0, 0, null);

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imageHat, 110, 35, null);

        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(output, "png", outputStream);

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c ->
                    c.createMessage(
                        MessageCreateSpec.builder()
                            .addFile(
                                tokenId + ".png",
                                new ByteArrayInputStream(outputStream.toByteArray()))
                            .build()));
      } catch (URISyntaxException | IOException e) {
        e.printStackTrace();
      }
    }

    return Mono.empty();
  }
}
