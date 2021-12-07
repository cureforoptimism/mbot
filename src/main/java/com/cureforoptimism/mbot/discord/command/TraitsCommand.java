package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class TraitsCommand implements MbotCommand {
  private final TraitsRepository traitsRepository;
  private final Utilities utilities;

  @Override
  public String getName() {
    return "traits";
  }

  @Override
  public String getDescription() {
    return "List all traits. Rarities listed if type specified.";
  }

  @Override
  public String getUsage() {
    return "[type]";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!rank command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2) {
      final var values =
          traitsRepository.findDistinctByTypeIgnoreCaseOrderByValueAsc(parts[1].trim());
      if (!values.isEmpty()) {
        StringBuilder output = new StringBuilder();
        Map<String, Double> percentages = new HashMap<>();

        for (String value : values) {
          percentages.put(value, (utilities.getTraitRarity(parts[1].trim(), value)));
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
      final var types = traitsRepository.findDistinctTraits();
      StringBuilder output = new StringBuilder();
      for (String trait : types) {
        output.append(trait).append("\n");
      }

      String finalOutput = output.toString();
      return event.getMessage().getChannel().flatMap(c -> c.createMessage(finalOutput));
    }
  }
}
