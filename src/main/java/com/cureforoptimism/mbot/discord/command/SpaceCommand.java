package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.repository.TraitsRepository;
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
import java.net.URISyntaxException;
import java.util.List;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SpaceCommand implements MbotCommand {
  private final Utilities utilities;
  private final TraitsRepository traitsRepository;
  private BufferedImage suitDefault;
  private BufferedImage suitPipeFix;
  private BufferedImage suitGumFix;
  private BufferedImage suitArmorFix;
  private BufferedImage imgGalaxy;

  public SpaceCommand(Utilities utilities, TraitsRepository traitsRepository) {
    this.utilities = utilities;
    this.traitsRepository = traitsRepository;

    try {
      this.imgGalaxy = ImageIO.read(new ClassPathResource("galaxy.png").getInputStream());
      this.suitDefault =
          ImageIO.read(new ClassPathResource("smol-space-suit.png").getInputStream());
      this.suitPipeFix =
          ImageIO.read(new ClassPathResource("smol-space-suit-mouth-fix.png").getInputStream());
      this.suitGumFix =
          ImageIO.read(new ClassPathResource("smol-space-suit-gum.png").getInputStream());
      this.suitArmorFix =
          ImageIO.read(new ClassPathResource("smol-space-suit-armor.png").getInputStream());
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
        Long.parseLong(tokenId);
      } catch (Exception ex) {
        return Mono.empty();
      }

      try {
        boolean usePipeFix = false;
        boolean useGumFix = false;
        boolean useArmorFix = false;
        List<Trait> traits = traitsRepository.findBySmol_Id(Long.parseLong(tokenId));
        for (Trait trait : traits) {
          if (trait.getType().equalsIgnoreCase("mouth")) {
            String mouth = trait.getValue();
            if (mouth.equalsIgnoreCase("gum")) {
              useGumFix = true;
              break;
            } else if (mouth.equalsIgnoreCase("pipe")) {
              usePipeFix = true;
              break;
            } else if (mouth.equalsIgnoreCase("armor")) {
              useArmorFix = true;
            }
          }
        }

        final var imageSmol = utilities.getTransparentImage(tokenId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage output =
            new BufferedImage(
                imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(Scalr.resize(imgGalaxy, Mode.FIT_EXACT, 350), 0, 0, null);

        graphics.drawImage(imageSmol, 0, 0, null);

        if (useGumFix) {
          graphics.drawImage(suitGumFix, 0, 0, null);
        } else if (usePipeFix) {
          graphics.drawImage(suitPipeFix, 0, 0, null);
        } else if (useArmorFix) {
          graphics.drawImage(suitArmorFix, 0, 0, null);
        } else {
          graphics.drawImage(suitDefault, 0, 0, null);
        }

        graphics.dispose();

        ImageIO.write(output, "png", outputStream);

        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c -> {
                  final var embed =
                      EmbedCreateSpec.builder()
                          .title("Ground control to Major Smol! Crew includes #" + tokenId + "!")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .image("attachment://" + tokenId + "space.png")
                          .addField(
                              "Notes",
                              "Ready for lift off! From `thegall#5404` x `Cure For Optimism`",
                              true)
                          .build();

                  return c.createMessage(
                      MessageCreateSpec.builder()
                          .addFile(
                              tokenId + "space.png",
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
