package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.service.TreasureService;
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
public class PfpCommand implements MbotCommand {
  private final TreasureService treasureService;

  @Override
  public String getName() {
    return "pfp";
  }

  @Override
  public String getDescription() {
    return "Creates an animated gif of a Smol's brain growing (optional: try `!pfp <token_id> reverse`)";
  }

  @Override
  public String getUsage() {
    return "<token_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!pfp command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2 || parts.length == 3) {
      String tokenId = parts[1];
      boolean reverse = parts.length != 2;

      final var image = treasureService.getAnimatedGif(tokenId, reverse);
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
}
