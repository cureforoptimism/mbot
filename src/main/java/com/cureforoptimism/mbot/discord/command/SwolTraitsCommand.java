package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.repository.SmolBodyTraitsRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class SwolTraitsCommand implements MbotCommand {
  private final SmolBodyTraitsRepository smolBodyTraitsRepository;
  private final Utilities utilities;

  @Override
  public String getName() {
    return "swoltraits";
  }

  @Override
  public String getDescription() {
    return "List all smol body traits. Rarities listed if type specified.";
  }

  @Override
  public String getUsage() {
    return "[type]";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!swoltraits command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2) {
      final var values =
          smolBodyTraitsRepository.findDistinctByTypeIgnoreCaseOrderByValueAsc(parts[1].trim());
      if (!values.isEmpty()) {
        StringBuilder output = new StringBuilder();
        Map<String, Double> percentages = new HashMap<>();

        for (String value : values) {
          percentages.put(value, (utilities.getSmolBodyTraitRarity(parts[1].trim(), value)));
        }

        final var sorted =
            percentages.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
        for (Map.Entry<String, Double> entry : sorted) {
          output
              .append(entry.getKey())
              .append(": ")
              .append(String.format("(%.3f%%)", entry.getValue()))
              .append("\n");
        }

        String finalOutput = output.toString();
        return event.getMessage().getChannel().flatMap(c -> c.createMessage(finalOutput));
      } else {
        return event
            .getMessage()
            .getChannel()
            .flatMap(c -> c.createMessage("I couldn't find that trait: " + parts[1].trim()));
      }
    } else {
      final var types = smolBodyTraitsRepository.findDistinctTraits();
      StringBuilder output = new StringBuilder();
      for (String trait : types) {
        output.append(trait).append("\n");
      }

      String finalOutput = output.toString();
      return event.getMessage().getChannel().flatMap(c -> c.createMessage(finalOutput));
    }
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
