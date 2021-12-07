package com.cureforoptimism.mbot.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface MbotCommand {
    String getName();

    String getDescription();

    Mono<Message> handle(MessageCreateEvent event);
}
