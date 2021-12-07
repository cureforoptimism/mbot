package com.cureforoptimism.mbot.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class SmolCommand implements MbotCommand {
    @Override
    public String getName() {
        return "smol";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Mono<Message> handle(MessageCreateEvent event) {
        return null;
    }
}
