package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.domain.VroomRarityRank;
import com.cureforoptimism.mbot.repository.VroomRarityRankRepository;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@AllArgsConstructor
public class Top20VroomCommand implements MbotCommand {
  private final VroomRarityRankRepository vroomRarityRankRepository;

  @Override
  public String getName() {
    return "top20vrooms";
  }

  @Override
  public String getDescription() {
    return "List the top 20 ranked Vrooms";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    StringBuilder ranks = new StringBuilder("```");
    Set<VroomRarityRank> rarities = vroomRarityRankRepository.findTop20();
    for (VroomRarityRank rarityRank : rarities) {
      ranks.append(rarityRank.getRank()).append(": #").append(rarityRank.getSmolId()).append("\n");
    }

    ranks.append("```");

    final var msg =
        EmbedCreateSpec.builder()
            .title("Top 20 Vrooms")
            .author(
                "SmolBot",
                null,
                "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
            .description(ranks.toString())
            .build();
    return event.getMessage().getChannel().flatMap(c -> c.createMessage(msg));
  }
}
