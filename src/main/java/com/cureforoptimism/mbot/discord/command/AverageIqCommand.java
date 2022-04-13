package com.cureforoptimism.mbot.discord.command;

import com.smolbrains.SmolBrainsContract;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class AverageIqCommand implements MbotCommand {
  private final SmolBrainsContract smolBrainsContract;

  @Override
  public String getName() {
    return "averageiq";
  }

  @Override
  public String getDescription() {
    return "Shows the average Smol IQ across the Smolverse";
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
            c -> {
              try {
                BigInteger averageIqBig = smolBrainsContract.averageIQ().send();
                MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
                final var iq = new BigDecimal(averageIqBig, 18, mc);

                return c.createMessage("Average IQ is currently: " + iq);
              } catch (Exception ignored) {
                return Mono.empty();
              }
            });
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    try {
      BigInteger averageIqBig = smolBrainsContract.averageIQ().send();
      MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
      final var iq = new BigDecimal(averageIqBig, 18, mc);

      event.reply("Average IQ is currently: " + iq).block();
    } catch (Exception ignored) {
      return Mono.empty();
    }

    return Mono.empty();
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
