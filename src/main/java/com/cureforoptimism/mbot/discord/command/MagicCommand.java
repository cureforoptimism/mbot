package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.service.CoinGeckoService;
import com.litesoftwares.coingecko.domain.Coins.CoinFullData;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateFields.Footer;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@AllArgsConstructor
@Component
@Slf4j
public class MagicCommand implements MbotCommand {
  private final DiscordBot discordBot;
  private final CoinGeckoService coinGeckoService;

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
    log.info("!magic command received");

    return event.getMessage().getChannel().flatMap(c -> c.createMessage(getMagicEmbed()));
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/magic command received");

    event.reply().withEmbeds(getMagicEmbed()).block();

    return Mono.empty();
  }

  private EmbedCreateSpec getMagicEmbed() {
    CoinFullData coinData = coinGeckoService.getCoinFullData();

    DecimalFormat decimalFormat = new DecimalFormat("#.00");
    final Instant athDate = Instant.parse(coinData.getMarketData().getAthDate().get("usd"));
    final long daysSinceAth =
        ChronoUnit.DAYS.between(
            LocalDate.ofInstant(athDate, ZoneId.systemDefault()), LocalDate.now());

    return EmbedCreateSpec.builder()
        .title("MAGIC - $" + discordBot.getCurrentPrice())
        .description(
            "MC Rank: #"
                + coinData.getMarketCapRank()
                + "\n"
                + "Market cap: $"
                + NumberFormat.getIntegerInstance()
                    .format(coinData.getMarketData().getMarketCap().get("usd"))
                + "\n"
                + "24 hour volume: $"
                + NumberFormat.getIntegerInstance().format(discordBot.getCurrentVolume24h())
                + "\n"
                + "In circulation: "
                + NumberFormat.getIntegerInstance()
                    .format(coinData.getMarketData().getCirculatingSupply())
                + " MAGIC\n"
                + "Total supply: "
                + NumberFormat.getIntegerInstance()
                    .format(coinData.getMarketData().getTotalSupply())
                + " MAGIC\n"
                + "Max supply: "
                + NumberFormat.getIntegerInstance().format(coinData.getMarketData().getMaxSupply())
                + " MAGIC\n"
                + "All time high: $"
                + decimalFormat.format(coinData.getMarketData().getAth().get("usd"))
                + (daysSinceAth == 0 ? " (Today)" : " (" + daysSinceAth + " days ago)")
                + "\n24hr high/low: $"
                + decimalFormat.format(coinData.getMarketData().getHigh24h().get("usd"))
                + " / $"
                + decimalFormat.format(coinData.getMarketData().getLow24h().get("usd")))
        .addField(
            "Current Prices",
            "USD: `"
                + discordBot.getCurrentPrice()
                + "`\n"
                + "ETH: `"
                + String.format("`%.6f`", coinData.getMarketData().getCurrentPrice().get("eth"))
                + "`\n"
                + "BTC: `"
                + String.format("`%.8f`", coinData.getMarketData().getCurrentPrice().get("btc"))
                + "`\n",
            true)
        .addField(
            "Price Changes",
            "1h: `"
                + String.format("`%.2f%%`", discordBot.getCurrentChange1h())
                + "`\n"
                + "24h: `"
                + String.format("`%.2f%%`", discordBot.getCurrentChange())
                + "`\n"
                + "7d: `"
                + String.format("`%.2f%%`", coinData.getMarketData().getPriceChangePercentage7d())
                + "`\n"
                + "1m: `"
                + String.format("`%.2f%%`", coinData.getMarketData().getPriceChangePercentage30d())
                + "`\n",
            true)
        .thumbnail("https://assets.coingecko.com/coins/images/18623/large/Magic.png?1635755672")
        .footer(
            Footer.of(
                "Powered by Defined.fi",
                "https://miro.medium.com/fit/c/262/262/1*GQz1T6gRmenQRhTS-aWiLA.png"))
        .timestamp(Instant.now())
        .build();
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
