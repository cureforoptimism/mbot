package com.cureforoptimism.mbot.discord.listener;

import com.cureforoptimism.mbot.Constants;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.discord.command.MbotCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.http.client.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Slf4j
public class MbotCommandListener {
  private final Collection<MbotCommand> commands;
  private final DiscordBot discordBot;

  public MbotCommandListener(ApplicationContext applicationContext, DiscordBot discordBot) {
    commands = applicationContext.getBeansOfType(MbotCommand.class).values();
    this.discordBot = discordBot;
  }

  public Mono<Void> handle(ChatInputInteractionEvent event) {
    try {
      Flux.fromIterable(commands)
          .filter(command -> command.getName().equals(event.getCommandName()))
          .next()
          .flatMap(
              command -> {
                if (!command.adminOnly()) {
                  return command.handle(event);
                }

                return Mono.empty();
              })
          .block();
    } catch (Exception ex) {
      log.error(ex.getMessage());
    }

    return Mono.empty();
  }

  public void handle(MessageCreateEvent event) {
    try {
      String message = event.getMessage().getContent().toLowerCase();
      if (!message.startsWith("!")) {
        return;
      }

      // Trim leading !
      String[] parts = message.split(" ");
      if (parts.length > 0) {
        String commandName = parts[0].substring(1);

        Flux.fromIterable(commands)
            .filter(command -> command.getName().equals(commandName))
            .next()
            .flatMap(
                command -> {
                  if (command.adminOnly()) {
                    // Only allow "Smol Team" (and me) to perform admin only commands
                    if (event.getMember().get().getId().asLong() != Constants.CFO_USER_ID
                        && !event
                            .getMember()
                            .get()
                            .getRoleIds()
                            .contains(Snowflake.of(Constants.SMOL_TEAM_ROLE_ID))) {
                      return Mono.empty();
                    }
                  }

                  // Verify that this is a message in a server and not a DM (for now)
                  Message msg = event.getMessage();
                  if (msg.getGuildId().isEmpty()) {
                    return Mono.empty();
                  }

                  return command.handle(event);
                })
            .block();
      }
    } catch (Exception ex) {
      if (ex instanceof final ClientException clientException) {
        if (clientException.getStatus().code() != 403) {
          log.error("Error received in listener loop. Will resume.", ex);
        }
      } else {
        log.error("Error received in listener loop. Will resume.", ex);
      }
    }
  }
}
