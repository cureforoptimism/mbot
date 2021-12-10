package com.cureforoptimism.mbot.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DonateCommand implements MbotCommand {
  @Override
  public String getName() {
    return "donate";
  }

  @Override
  public String getDescription() {
    return "donation information to Smolbot maintainer";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    String description =
            """
                    This Discord bot is built and maintained by `Cure For Optimism#5061`, who is not a part of the Smol/Treasure development team (just a fan).

                    You can find the source code for this bot at https://github.com/cureforoptimism/mbot; PR's are welcome!

                    I write this for fun and to help keep people interested in the Smoliverse. A few people have asked me about donations, though, so if you'd like to buy me a whiskey feel free to toss something at `cureforoptimism.eth`
                    I want to stress that I'm running and hosting this bot for fun, though, so donations aren't necessary! I'll continue to add fun new things to this bot as long as people are finding use in it!""";

    final var msg =
        EmbedCreateSpec.builder()
            .title("SmolBot Donation Info")
            .author(
                "SmolBot",
                null,
                "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
            .description(description)
            .build();
    return event.getMessage().getChannel().flatMap(c -> c.createMessage(msg));
  }
}
