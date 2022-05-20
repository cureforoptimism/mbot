package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.service.TreasureService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import java.io.ByteArrayInputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class PfpBodyCommand implements MbotCommand {
  private final TreasureService treasureService;

  @Override
  public String getName() {
    return "pfpbody";
  }

  @Override
  public String getDescription() {
    return "Creates an animated gif of a SmolBodies musclez growing (optional: try `!pfp <token_id> reverse`, `!pfp <token_id> faster`, `!pfp <token_id> faster faster faster reverse`)";
  }

  @Override
  public String getUsage() {
    return "<token_id> [reverse] [faster...]";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!pfpbody command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");
    int msDelay = 1000; // 1 fps
    boolean reverse = false;

    if (parts.length >= 2) {
      String tokenId = parts[1];

      for (String part : parts) {
        if (part.equalsIgnoreCase("reverse")) {
          reverse = true;
        } else if (part.equalsIgnoreCase("faster")) {
          // No idea how low this can actually go, but may as well set a limit so we don't go
          // negative
          if (msDelay - 250 > 25) {
            msDelay -= 250;
          } else {
            msDelay = 25;
          }
        }
      }

      final var image =
          treasureService.getAnimatedGif(tokenId, SmolType.SMOL_BODY, reverse, msDelay);
      if (image == null) {
        return event
            .getMessage()
            .getChannel()
            .flatMap(c -> c.createMessage("I can't find a token with ID " + tokenId));
      }

      try {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image);
        return event
            .getMessage()
            .getChannel()
            .flatMap(
                c ->
                    c.createMessage(
                        MessageCreateSpec.builder()
                            .addFile(tokenId + ".gif", byteArrayInputStream)
                            .build()));

      } catch (Exception ex) {
        log.error("Error rendering PFP: " + ex);
        return Mono.empty();
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
