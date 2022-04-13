package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
public class SwolSantaCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage imageHat = null;

  public SwolSantaCommand(Utilities utilities) {
    this.utilities = utilities;

    var hatRes = new ClassPathResource("santa-hat-swol.png");

    try {
      imageHat = ImageIO.read(hatRes.getInputStream());
    } catch (Exception ex) {
      log.error("Unable to process images");
    }
  }

  @Override
  public String getName() {
    return "swolsanta";
  }

  @Override
  public String getDescription() {
    return "Ho ho ho with a swol swol swol! Credit to `DomZ#0362` for the hat!";
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
            new URI(utilities.getSmolImage(tokenId, SmolType.SMOL_BODY, true).orElse(""));
        final var imageSmol = ImageIO.read(smolUri.toURL());

        BufferedImage output =
            new BufferedImage(
                imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imageSmol, 0, 0, null);

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imageHat, 132, 76, null);

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

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    return null;
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
