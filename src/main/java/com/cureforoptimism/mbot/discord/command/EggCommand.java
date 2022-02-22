package com.cureforoptimism.mbot.discord.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EggCommand implements MbotCommand {
  private boolean isGuessed = false;

  @Override
  public String getName() {
    return "eg";
  }

  @Override
  public String getDescription() {
    return "eggggGGggGg";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    if (isGuessed) {
      return Mono.empty();
    }

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length == 2) {
      String guess = parts[1];
      long guessId = -1;
      try {
        guessId = Long.parseLong(guess);
      } catch (NumberFormatException ex) {
        return Mono.empty();
      }

      if (guessId == 222) {
        isGuessed = true;
        event
            .getMessage()
            .getChannel()
            .flatMap(
                c ->
                    c.createMessage(
                        "egggGGgGGgGgG!!! You won the eg, "
                            + event.getMessage().getUserData().username()
                            + "! DM smol Cure For Optimism and he'll send it!"))
            .block();
      } else {
        event
            .getMessage()
            .getChannel()
            .flatMap(c -> c.createMessage(guess + " is incorrect; try again!"))
            .block();
      }
    }

    return Mono.empty();
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    return null;
  }
}
