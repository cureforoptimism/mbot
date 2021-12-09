package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class VroomCommand implements MbotCommand {
  private final Utilities utilities;

  @Override
  public String getName() {
    return "vroom";
  }

  @Override
  public String getDescription() {
    return "shows a vroom and details (WIP)";
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
      Optional<EmbedCreateSpec> embed = utilities.getCarEmbed(tokenId);
      return embed
          .map(
              embedCreateSpec ->
                  event.getMessage().getChannel().flatMap(c -> c.createMessage(embedCreateSpec)))
          .orElse(Mono.empty());
    }

    return Mono.empty();
  }
}
