package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class SmolCommand implements MbotCommand {
  private final Utilities utilities;

  @Override
  public String getName() {
    return "smol";
  }

  @Override
  public String getDescription() {
    return "shows a smol IQ, rank, picture, and traits";
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
      Optional<EmbedCreateSpec> embed = utilities.getSmolEmbed(tokenId);
      return embed
          .map(
              embedCreateSpec ->
                  event.getMessage().getChannel().flatMap(c -> c.createMessage(embedCreateSpec)))
          .orElse(Mono.empty());
    }

    return Mono.empty();
  }
}
