package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class RankCommand implements MbotCommand {
  private final RarityRankRepository rarityRankRepository;
  private final Utilities utilities;

  @Override
  public String getName() {
    return "rank";
  }

  @Override
  public String getDescription() {
    return "Show the smol(s) at specified rank number";
  }

  @Override
  public String getUsage() {
    return "<rank_number>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!rank command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2) {
      String rankNumberStr = parts[1].trim();
      Integer rankNumber;

      try {
        rankNumber = Integer.parseInt(rankNumberStr);
      } catch (NumberFormatException ex) {
        return Mono.empty();
      }

      List<RarityRank> ranks = rarityRankRepository.findByRank(rankNumber);

      if (ranks.size() == 1) {
        RarityRank rank = ranks.get(0);
        Optional<EmbedCreateSpec> embed = utilities.getSmolEmbed(rank.getSmolId().toString());
        return embed
            .map(
                embedCreateSpec ->
                    event.getMessage().getChannel().flatMap(c -> c.createMessage(embedCreateSpec)))
            .orElseGet(Mono::empty);
      } else if (ranks.size() >= 2) {
        StringBuilder output = new StringBuilder();
        output.append("There are multiple Smols with rank ").append(rankNumber).append(": ");
        for (RarityRank rarityRank : ranks) {
          output.append(rarityRank.getSmolId()).append(" ");
        }

        return event.getMessage().getChannel().flatMap(c -> c.createMessage(output.toString()));
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
