package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SwolNewYearCommand implements MbotCommand {
  private final List<BufferedImage> hatFrames = new ArrayList<>();
  private final Utilities utilities;

  public SwolNewYearCommand(Utilities utilities) {
    this.utilities = utilities;

    for (int x = 1; x <= 27; x++) {
      try {
        hatFrames.add(
            ImageIO.read(
                new ClassPathResource("new_years_layers/swol_hat_" + x + ".png").getInputStream()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String getName() {
    return "swolnewyear";
  }

  @Override
  public String getDescription() {
    return "2021 is so yesterday. Get your swol ass into 2022. Art courtesy of `commonopoly#2944`.";
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
            new URI(utilities.getSmolImage(tokenId, SmolType.SMOL_BODY, true).orElse(""));
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
            graphics.drawImage(hatFrame, 105, 29, null);

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
          graphics.drawImage(hatFrames.get(18), 105, 29, null);

          graphics.dispose();

          ImageIO.write(output, "png", outputStream);
        }

        ByteArrayOutputStream finalOutputStream = outputStream;
        Files.write(Path.of("out.gif"), finalOutputStream.toByteArray());
        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title("Happy Swol Year, #" + tokenId + "!")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .image(
                              "attachment://swol_"
                                  + tokenId
                                  + "_fun."
                                  + (isAnimated ? "gif" : "png"))
                          .addField(
                              "Notes",
                              "You can use !swolnewyear <id> anim for animated gif, and !swolnewyear <token> for a single frame. Happy New Year from `commonopoly` x `Cure For Optimism`",
                              true)
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              "swol_" + tokenId + "_fun." + (isAnimated ? "gif" : "png"),
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
