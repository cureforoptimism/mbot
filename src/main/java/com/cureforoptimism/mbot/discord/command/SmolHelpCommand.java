package com.cureforoptimism.mbot.discord.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class SmolHelpCommand implements MbotCommand {
  final ApplicationContext context;
  final Collection<MbotCommand> commands;

  public SmolHelpCommand(ApplicationContext context) {
    this.context = context;
    this.commands = context.getBeansOfType(MbotCommand.class).values();
  }

  @Override
  public String getName() {
    return "smolhelp";
  }

  @Override
  public String getDescription() {
    return "This help message";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    StringBuilder helpMsg = new StringBuilder();

    for (MbotCommand command : commands) {
      helpMsg.append("`!").append(command.getName());

      if (command.getUsage() != null) {
        helpMsg.append(" ").append(command.getUsage());
      }

      helpMsg.append("` - ").append(command.getDescription()).append("\n");
    }

    final var msg =
        EmbedCreateSpec.builder()
            .title("SmolBot Help")
            .author(
                "SmolBot",
                null,
                "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
            .description(helpMsg.toString())
            .addField(
                "Note: ",
                "This bot developed for fun by `Cure For Optimism#5061`, and is unofficial. Smol admins aren't associated with this bot and can't help you with issues. Ping smol cureForOptimism with any feedback/questions",
                false)
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
