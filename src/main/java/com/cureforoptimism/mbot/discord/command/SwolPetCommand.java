package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class SwolPetCommand implements MbotCommand {
  private final Utilities utilities;

  @Override
  public String getName() {
    return "swolpet";
  }

  @Override
  public String getDescription() {
    return "Shows a swol pet picture, traits, etc...";
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
      Optional<EmbedCreateSpec> embed = utilities.getBodyPetEmbed(tokenId);
      return embed
          .map(
              embedCreateSpec ->
                  event.getMessage().getChannel().flatMap(c -> c.createMessage(embedCreateSpec)))
          .orElse(Mono.empty());
    }

    return Mono.empty();
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/swolpet command received");

    try {
      final var tokenId = event.getOption("id").orElse(null);
      if (tokenId == null) {
        return null;
      }

      if (tokenId.getValue().isEmpty()) {
        return Mono.empty();
      }

      final var tokenIdStrOpt = tokenId.getValue().get();

      final var embed = utilities.getBodyPetEmbed(tokenIdStrOpt.getRaw());
      if (embed.isEmpty()) {
        return null;
      }

      event.reply().withEmbeds(embed.get()).block();

    } catch (Exception ex) {
      log.error("Error with smol command: " + ex.getMessage());
    }

    return Mono.empty();
  }
}
