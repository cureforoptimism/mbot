package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.domain.SmolBodyRarityRank;
import com.cureforoptimism.mbot.repository.SmolBodyRarityRankRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@AllArgsConstructor
public class Top20SwolsCommand implements MbotCommand {
  private final SmolBodyRarityRankRepository smolBodyRarityRankRepository;

  @Override
  public String getName() {
    return "top20swols";
  }

  @Override
  public String getDescription() {
    return "List the top 20 ranked Smol Bodies";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    StringBuilder ranks = new StringBuilder("```");
    Set<SmolBodyRarityRank> rarities = smolBodyRarityRankRepository.findTop20();
    for (SmolBodyRarityRank rarityRank : rarities) {
      ranks
          .append(rarityRank.getRank())
          .append(": #")
          .append(rarityRank.getSmolBodyId())
          .append("\n");
    }

    ranks.append("```");

    final var msg =
        EmbedCreateSpec.builder()
            .title("Top 20 SmolBodies")
            .author(
                "SmolBot",
                null,
                "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmolbodies.94d441bb.png&w=1920&q=75")
            .description(ranks.toString())
            .build();
    return event.getMessage().getChannel().flatMap(c -> c.createMessage(msg));
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
