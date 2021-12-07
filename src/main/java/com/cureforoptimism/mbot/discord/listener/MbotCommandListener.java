package com.cureforoptimism.mbot.discord.listener;

import com.cureforoptimism.mbot.discord.command.MbotCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class MbotCommandListener {
    private final Collection<MbotCommand> commands;

    public MbotCommandListener(ApplicationContext applicationContext) {
        commands = applicationContext.getBeansOfType(MbotCommand.class).values();
    }

    public Mono<Message> handle(MessageCreateEvent event) {
        String message = event.getMessage().getContent().toLowerCase();
        if (!message.startsWith("!")) {
            return Mono.empty();
        }

        // Trim leading !
        message = message.substring(1);

        String[] parts = message.split(" ");
        if(parts.length > 0) {
            String commandName = parts[0];

            return Flux.fromIterable(commands)
                    .filter(command -> command.getName().equals(commandName))
                    .next()
                    .flatMap(
                            command -> {
                                // Verify that this is a message in a server and not a DM (for now)
                                Message msg = event.getMessage();
                                if (msg.getGuildId().isEmpty()) {
                                    return Mono.empty();
                                }

                                return command.handle(
                                        event);
                            });
        }

        return Mono.empty();
    }
}
