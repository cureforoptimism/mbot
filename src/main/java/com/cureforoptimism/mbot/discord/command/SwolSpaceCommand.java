package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolBodyTrait;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.repository.SmolBodyTraitsRepository;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SwolSpaceCommand implements MbotCommand {
  private final Utilities utilities;
  private BufferedImage ladySuit;
  private BufferedImage maleSuit;
  private final SmolBodyTraitsRepository smolBodyTraitsRepository;

  public SwolSpaceCommand(Utilities utilities, SmolBodyTraitsRepository smolBodyTraitsRepository) {
    this.utilities = utilities;
    this.smolBodyTraitsRepository = smolBodyTraitsRepository;

    try {
      this.ladySuit = ImageIO.read(new ClassPathResource("swol_female_suit.png").getInputStream());
      this.maleSuit = ImageIO.read(new ClassPathResource("swol_male_suit.png").getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "swolspace";
  }

  @Override
  public String getDescription() {
    return "One smol step for swol, one giant leap for swolkind! Art by `maximee`";
  }

  @Override
  public String getUsage() {
    return "<token_id>";
  }

  private static class SwolSpaceResponse {
    EmbedCreateSpec embed;
    byte[] image;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!swolspace command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length >= 2) {
      String tokenId = parts[1];

      try {
        Long.parseLong(tokenId);
      } catch (Exception ex) {
        return Mono.empty();
      }

      final var response = createEmbed(tokenId);
      if (response == null) {
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
                              tokenId + "swol_space.png", new ByteArrayInputStream(response.image))
                          .addEmbed(response.embed)
                          .build()));
    }
    return Mono.empty();
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/swolspace command received");

    event.deferReply().block();

    final var tokenId = Utilities.getOptionString(event, "id").orElse(null);
    if (tokenId == null) {
      return null;
    }

    final var embed = createEmbed(tokenId);
    if (embed == null) {
      return Mono.empty();
    }

    event
        .createFollowup(
            InteractionFollowupCreateSpec.builder()
                .addFile(tokenId + "swol_space.png", new ByteArrayInputStream(embed.image))
                .addEmbed(embed.embed)
                .build())
        .block();

    return Mono.empty();
  }

  private SwolSpaceResponse createEmbed(String tokenId) {
    try {

      final var swolUri =
          new URI(utilities.getSmolImage(tokenId, SmolType.SMOL_BODY, true).orElse(""));
      if (swolUri.toString().equals("")) {
        return null;
      }

      boolean isMale = true;
      List<SmolBodyTrait> traits =
          smolBodyTraitsRepository.findBySmolBody_Id(Long.parseLong(tokenId));
      for (SmolBodyTrait trait : traits) {
        if (trait.getType().equalsIgnoreCase("gender")
            && trait.getValue().equalsIgnoreCase("female")) {
          isMale = false;
          break;
        }
      }

      var imageSmol = ImageIO.read(swolUri.toURL());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BufferedImage output =
          new BufferedImage(
              imageSmol.getWidth(), imageSmol.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = output.createGraphics();
      graphics.setComposite(AlphaComposite.SrcOver);

      graphics.drawImage(imageSmol, 0, 0, null);
      graphics.drawImage(isMale ? maleSuit : ladySuit, 0, 0, null);

      graphics.dispose();

      ImageIO.write(output, "png", outputStream);

      SwolSpaceResponse response = new SwolSpaceResponse();
      response.image = outputStream.toByteArray();
      response.embed =
          EmbedCreateSpec.builder()
              .title(
                  "One smol step for swol, one giant leap for swolkind! Crew includes #"
                      + tokenId
                      + "!")
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
              .image("attachment://" + tokenId + "swol_space.png")
              .addField("Notes", "Ready for lift off! From `maximee` x `Cure For Optimism`", true)
              .build();

      return response;
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
