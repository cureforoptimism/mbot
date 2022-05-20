package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Constants;
import com.cureforoptimism.mbot.application.DiscordBot;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OgUpdateCommand implements MbotCommand {
  private final DiscordBot discordBot;

  @Override
  public String getName() {
    return "ogupdate";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    //    final var migratedUsers =
    //        discordBot.migrateRoles(
    //            Constants.CFO_TEST_GUILD_ID,
    //            Constants.CFO_TEST_GUILD_ID,
    //            Constants.CFO_TEST_ROLE_ID_FROM,
    //            Constants.CFO_TEST_ROLE_ID_TO);

    final var migratedUsers =
        discordBot.migrateRoles(
            Constants.SWOL_GUILD_ID,
            Constants.SMOL_GUILD_ID,
            Constants.SWOL_OG_ROLE_ID,
            Constants.SMOL_OG_ROLE_ID);

    return event
        .getMessage()
        .getChannel()
        .flatMap(
            c ->
                c.createMessage(
                    MessageCreateSpec.builder()
                        .messageReference(event.getMessage().getId())
                        .content(migratedUsers + "Swols have been migrated to Smol OG's!")
                        .build()));
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    return null;
  }

  @Override
  public Boolean adminOnly() {
    return true;
  }
}
