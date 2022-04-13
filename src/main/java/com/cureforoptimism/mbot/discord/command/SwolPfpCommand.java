package com.cureforoptimism.mbot.discord.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class SwolPfpCommand implements MbotCommand {
  private final PfpBodyCommand pfpBodyCommand;

  @Override
  public String getName() {
    return "swolpfp";
  }

  @Override
  public String getDescription() {
    return "Same as `!pfpbody`)";
  }

  @Override
  public String getUsage() {
    return "<token_id> [reverse] [faster...]";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    return pfpBodyCommand.handle(event);
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
