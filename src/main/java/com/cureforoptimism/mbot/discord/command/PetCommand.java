package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PetCommand implements MbotCommand {
  private final Utilities utilities;

  @Override
  public String getName() {
    return "pet";
  }

  @Override
  public String getDescription() {
    return "Shows a pet picture, traits, etc...";
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
      Optional<EmbedCreateSpec> embed = utilities.getPetEmbed(tokenId);
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
    log.info("/pet command received");

    try {
      final var tokenId = event.getOption("id").orElse(null);
      if (tokenId == null) {
        return null;
      }

      if (tokenId.getValue().isEmpty()) {
        return Mono.empty();
      }

      final var tokenIdStrOpt = tokenId.getValue().get();

      final var embed = utilities.getPetEmbed(tokenIdStrOpt.getRaw());
      if (embed.isEmpty()) {
        return null;
      }

      event.reply().withEmbeds(embed.get()).block();

    } catch (Exception ex) {
      log.error("Error with smol command: " + ex.getMessage());
    }

    return Mono.empty();
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
