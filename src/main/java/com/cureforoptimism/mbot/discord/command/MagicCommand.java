package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.application.DiscordBot;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.NumberFormat;
import java.time.Instant;

@AllArgsConstructor
@Component
public class MagicCommand implements MbotCommand {
  private final DiscordBot discordBot;

  @Override
  public String getName() {
    return "magic";
  }

  @Override
  public String getDescription() {
    return "shows detailed information about $MAGIC";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    return event
        .getMessage()
        .getChannel()
        .flatMap(
            c ->
                c.createMessage(
                    EmbedCreateSpec.builder()
                        .title("$MAGIC - $" + discordBot.getCurrentPrice())
                        .author(
                            "Defined.fi",
                            null,
                            "https://miro.medium.com/fit/c/262/262/1*GQz1T6gRmenQRhTS-aWiLA.png")
                        .thumbnail("https://s2.coinmarketcap.com/static/img/coins/64x64/14783.png")
                        .addField(
                            "24h Δ", String.format("%.2f%%", discordBot.getCurrentChange()), true)
                        .addField(
                            "12h Δ",
                            String.format("%.2f%%", discordBot.getCurrentChange12h()),
                            true)
                        .addField(
                            "4h Δ", String.format("%.2f%%", discordBot.getCurrentChange4h()), true)
                        .addField(
                            "1h Δ", String.format("%.2f%%", discordBot.getCurrentChange1h()), true)
                        .addField(
                            "24 Vol",
                            "$"
                                + NumberFormat.getIntegerInstance()
                                    .format(discordBot.getCurrentVolume24h()),
                            true)
                        .addField(
                            "12h Vol",
                            "$"
                                + NumberFormat.getIntegerInstance()
                                    .format(discordBot.getCurrentVolume12h()),
                            true)
                        .addField(
                            "4h Vol",
                            "$"
                                + NumberFormat.getIntegerInstance()
                                    .format(discordBot.getCurrentVolume4h()),
                            true)
                        .addField(
                            "1h Vol",
                            "$"
                                + NumberFormat.getIntegerInstance()
                                    .format(discordBot.getCurrentVolume1h()),
                            true)
                        .timestamp(Instant.now())
                        .build()));
  }
}
