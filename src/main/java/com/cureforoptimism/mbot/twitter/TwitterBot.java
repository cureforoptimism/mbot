package com.cureforoptimism.mbot.twitter;

import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.domain.SmolSale;
import com.cureforoptimism.mbot.repository.SmolSalesRepository;
import io.github.redouane59.twitter.TwitterClient;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwitterBot {
  private final TwitterClient twitterClient;
  private final SmolSalesRepository smolSalesRepository;
  private final DiscordBot discordBot;
  private Date lastTweetedBlockTimestamp = null;

  public TwitterBot(
      TwitterClient twitterClient, SmolSalesRepository smolSalesRepository, DiscordBot discordBot) {
    this.twitterClient = twitterClient;
    this.smolSalesRepository = smolSalesRepository;
    this.discordBot = discordBot;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 1000)
  public synchronized void postNewSales() {
    if (lastTweetedBlockTimestamp == null) {
      lastTweetedBlockTimestamp =
          smolSalesRepository.findFirstByTweetedIsTrueOrderByBlockTimestampDesc().getBlockTimestamp();
    }

    List<SmolSale> newSales =
        smolSalesRepository.findByBlockTimestampIsAfterAndTweetedIsFalseOrderByBlockTimestampAsc(
            lastTweetedBlockTimestamp);
    if (!newSales.isEmpty()) {
      final NumberFormat decimalFormatZeroes = new DecimalFormat("0.00");
      final NumberFormat decimalFormatOptionalZeroes = new DecimalFormat("0.##");
      Double currentPrice = discordBot.getCurrentPrice();
      for (SmolSale smolSale : newSales) {
        final String usdValue =
            decimalFormatZeroes.format(
                smolSale.getSalePrice().multiply(BigDecimal.valueOf(currentPrice)));
        twitterClient.postTweet(
            "Smol Brains #"
                + smolSale.getTokenId()
                + " sold for "
                + decimalFormatOptionalZeroes.format(smolSale.getSalePrice())
                + " MAGIC ($"
                + usdValue
                + ")!\n\n"
                + "https://marketplace.treasure.lol/collection/0x6325439389e0797ab35752b4f43a14c004f22a9c/"
                + smolSale.getTokenId() + "\n\n"
            + "#smolbrains #treasuredao"
        );

        smolSale.setTweeted(true);
        smolSalesRepository.save(smolSale);

        log.info("New smol sale tweeted for " + smolSale.getTokenId());

        if (smolSale.getBlockTimestamp().after(lastTweetedBlockTimestamp)) {
          lastTweetedBlockTimestamp = smolSale.getBlockTimestamp();
        }
      }
    }
  }
}
