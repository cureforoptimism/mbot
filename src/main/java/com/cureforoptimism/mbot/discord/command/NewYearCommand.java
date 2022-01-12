package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class NewYearCommand implements MbotCommand {
  private final List<BufferedImage> hatFrames = new ArrayList<>();
  private final Utilities utilities;

  public NewYearCommand(Utilities utilities) {
    this.utilities = utilities;

    for (int x = 1; x <= 27; x++) {
      try {
        hatFrames.add(
            ImageIO.read(
                new ClassPathResource("new_years_layers/hat_" + x + ".png").getInputStream()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String getName() {
    return "newyear";
  }

  @Override
  public String getDescription() {
    return "2021 is so yesterday. Get your smol ass into 2022. Art courtesy of commonopoly#2944.";
  }

  @Override
  public String getUsage() {
    return "<token_id> [anim]";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length >= 2) {
      boolean isAnimated = parts.length >= 3;
      String tokenId = parts[1];

      try {
        final var smolUri =
            new URI(utilities.getSmolImage(tokenId, SmolType.SMOL, true).orElse(""));
        final var imageSmol = ImageIO.read(smolUri.toURL());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Just assume anything after part 2 is "animate"
        if (isAnimated) {
          ByteArrayOutputStream finishedImage = new ByteArrayOutputStream();

          AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
          gifEncoder.start(finishedImage);
          gifEncoder.setRepeat(0);
          gifEncoder.setDelay(60);

          for (BufferedImage hatFrame : hatFrames) {
            BufferedImage output =
                new BufferedImage(
                    imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = output.createGraphics();
            graphics.setComposite(AlphaComposite.SrcOver);

            graphics.drawImage(imageSmol, 0, 0, null);
            graphics.drawImage(hatFrame, 0, 0, null);

            ByteArrayOutputStream frameStream = new ByteArrayOutputStream();
            ImageIO.write(output, "png", frameStream);

            gifEncoder.addFrame(output);
            graphics.dispose();
          }

          gifEncoder.finish();

          outputStream = finishedImage;
        } else {
          BufferedImage output =
              new BufferedImage(
                  imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
          Graphics2D graphics = output.createGraphics();
          graphics.setComposite(AlphaComposite.SrcOver);

          graphics.drawImage(imageSmol, 0, 0, null);
          graphics.drawImage(hatFrames.get(18), 0, 0, null);

          graphics.dispose();

          ImageIO.write(output, "png", outputStream);
        }

        ByteArrayOutputStream finalOutputStream = outputStream;
        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title("Happy Smol Year, #" + tokenId + "!")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .image("attachment://" + tokenId + "_fun." + (isAnimated ? "gif" : "png"))
                          .addField(
                              "Notes",
                              "You can use !newyear <id> anim for animated gif, and !newyear <token> for a single frame. Happy New Year from `commonopoly` x `Cure For Optimism`",
                              true)
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              tokenId + "_fun." + (isAnimated ? "gif" : "png"),
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

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    return null;
  }
}
