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
  private BufferedImage imageNewHat = null;
  private BufferedImage imageJohn = null;
  private BufferedImage imageSuit = null;

  public SantaCommand(Utilities utilities) {
    this.utilities = utilities;

    var hatRes = new ClassPathResource("santa-hat.png");
    var newHatRes = new ClassPathResource("santa-hat-new.png");
    var suitRes = new ClassPathResource("santa-suit-new.png");
    var johnRes = new ClassPathResource("santa-john.png");

    try {
      imageHat = ImageIO.read(hatRes.getInputStream());
      imageNewHat = ImageIO.read(newHatRes.getInputStream());
      imageSuit = ImageIO.read(suitRes.getInputStream());
      imageJohn = ImageIO.read(johnRes.getInputStream());
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
    return "Ho ho ho with a smol smol smol! Hint: add 'old' for old hat, and 'suit' to include suit; you can leave the hat off with `nocap`. Credit to `DomZ#0362` for new hat/suit!";
  }

  @Override
  public String getUsage() {
    return "<token_id> [old] [suit] [nocap]";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");
    boolean useOldHat = false;
    boolean useNewHat = true;
    boolean useSuit = false;

    if (parts.length >= 2) {
      String tokenId = parts[1];

      if (tokenId.equalsIgnoreCase("john")) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
          ImageIO.write(imageJohn, "png", outputStream);
        } catch (IOException e) {
          log.error("exception fetching john", e);
          return Mono.empty();
        }

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c ->
                    c.createMessage(
                        MessageCreateSpec.builder()
                            .addFile(
                                "santa-john.png",
                                new ByteArrayInputStream(outputStream.toByteArray()))
                            .build()));
      }

      for (String part : parts) {
        if (part.equalsIgnoreCase("old")) {
          useOldHat = true;
        } else if (part.equalsIgnoreCase("suit")) {
          useSuit = true;
        } else if (part.equalsIgnoreCase("nocap")) {
          useNewHat = false;
          useSuit = true;
        }
      }

      try {
        final var smolUri =
            new URI(utilities.getSmolImage(tokenId, SmolType.SMOL, true).orElse(""));
        final var imageSmol = ImageIO.read(smolUri.toURL());

        BufferedImage output =
            new BufferedImage(
                imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(imageSmol, 0, 0, null);

        if (useOldHat) {
          graphics.drawImage(imageHat, 110, 35, null);
        } else if (useNewHat) {
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(imageNewHat, 0, 0, null);
        }

        if (useSuit) {
          graphics.setComposite(AlphaComposite.SrcOver);
          graphics.drawImage(imageSuit, 0, 0, null);
        }

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
