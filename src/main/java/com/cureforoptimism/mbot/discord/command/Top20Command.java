package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class Top20Command implements MbotCommand {
  private final RarityRankRepository rarityRankRepository;

  @Override
  public String getName() {
    return "top20";
  }

  @Override
  public String getDescription() {
    return "List the top 20 ranked Smols";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    StringBuilder ranks = new StringBuilder("```");
    Set<RarityRank> rarities = rarityRankRepository.findTop20();
    for (RarityRank rarityRank : rarities) {
      ranks.append(rarityRank.getRank()).append(": #").append(rarityRank.getSmolId()).append("\n");
    }
    ranks.append("```");

    final var msg =
        EmbedCreateSpec.builder()
            .title("Top 20 Smols")
            .author(
                "SmolBot",
                null,
                "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
            .description(ranks.toString())
            .build();
    return event.getMessage().getChannel().flatMap(c -> c.createMessage(msg));
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/top20 command received");

    // TODO: DRY
    StringBuilder ranks = new StringBuilder("```");
    Set<RarityRank> rarities = rarityRankRepository.findTop20();
    for (RarityRank rarityRank : rarities) {
      ranks.append(rarityRank.getRank()).append(": #").append(rarityRank.getSmolId()).append("\n");
    }
    ranks.append("```");

    try {
      event
          .reply()
          .withEmbeds(
              EmbedCreateSpec.builder()
                  .title("Top 20 Smols")
                  .author(
                      "SmolBot",
                      null,
                      "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                  .description(ranks.toString())
                  .build())
          .block();
    } catch (Exception ex) {
      log.error("Error with top20 command: " + ex.getMessage());
    }

    return null;
  }
}
