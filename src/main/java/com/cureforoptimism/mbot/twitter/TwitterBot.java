package com.cureforoptimism.mbot.twitter;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.domain.SmolSale;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import com.cureforoptimism.mbot.repository.SmolSalesRepository;
import com.cureforoptimism.mbot.service.CoinGeckoService;
import discord4j.core.spec.MessageCreateSpec;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.dto.tweet.TweetParameters.Media;
import io.github.redouane59.twitter.dto.tweet.TweetV2.TweetData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwitterBot {
  private final TwitterClient twitterClient;
  private final SmolSalesRepository smolSalesRepository;
  private final DiscordBot discordBot;
  private final CoinGeckoService coinGeckoService;
  private final Utilities utilities;
  private final RarityRankRepository rarityRankRepository;
  private Date lastTweetedBlockTimestamp = null;
  private LocalDateTime lastPostedTweetTime = null;
  private final List<Long> notifyChannelList;

  public TwitterBot(
      TwitterClient twitterClient,
      SmolSalesRepository smolSalesRepository,
      DiscordBot discordBot,
      CoinGeckoService coinGeckoService,
      Utilities utilities,
      RarityRankRepository rarityRankRepository) {
    this.twitterClient = twitterClient;
    this.smolSalesRepository = smolSalesRepository;
    this.discordBot = discordBot;
    this.coinGeckoService = coinGeckoService;
    this.utilities = utilities;
    this.rarityRankRepository = rarityRankRepository;

    // dev channel: 832825371941535817L
    // prod channel: 926303694801223751L
    // smolverse alpha #bot-commands: 916862412777488384
    // smolverse alpha #tweets: 916839134411243580L
    // smol brains #bot-spam: 917430194049007616L
    this.notifyChannelList =
        List.of(926303694801223751L, 917430194049007616L, 916862412777488384L, 916839134411243580L);
  }

  @Scheduled(fixedDelay = 15000)
  public synchronized void postNewTweets() {
    if (lastTweetedBlockTimestamp == null) {
      lastPostedTweetTime = LocalDateTime.now(ZoneOffset.UTC).minus(11, ChronoUnit.MINUTES);
    }

    final var tweets =
        twitterClient.searchTweets(
            "from:SmolBrainsNFT",
            AdditionalParameters.builder().startTime(lastPostedTweetTime).build());

    final var sortedByCreate =
        tweets.getData().stream()
            .sorted(Comparator.comparing(TweetData::getCreatedAt, Comparator.naturalOrder()))
            .toList();

    for (TweetData tweet : sortedByCreate) {
      // Skip replies; we only want top level tweets
      if (tweet.getReferencedTweets() != null && !tweet.getReferencedTweets().isEmpty()) {
        continue;
      }

      if (tweet.getCreatedAt().isAfter(lastPostedTweetTime)) {
        lastPostedTweetTime = tweet.getCreatedAt();
      } else {
        continue;
      }

      log.info(tweet.getCreatedAt().toString());

      log.info("New tweet! Posting to Discord: " + tweet.getId());
      discordBot.postMessage(
          MessageCreateSpec.builder()
              .content(
                  "New Tweet from **SmolBrainsNFT**: https://twitter.com/SmolBrainsNFT/status/"
                      + tweet.getId())
              .build(),
          notifyChannelList);
    }
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 1000)
  public synchronized void postNewSales() {
    if (lastTweetedBlockTimestamp == null) {
      lastTweetedBlockTimestamp =
          smolSalesRepository
              .findFirstByTweetedIsTrueOrderByBlockTimestampDesc()
              .getBlockTimestamp();
    }

    // Can uncomment and replace with recent ID to test new functionality (delete existing tweet
    // first!)
    //    List<SmolSale> newSales =
    // smolSalesRepository.findById("https://arbiscan.io/tx/0x7fc9d0a8c1961e200dd1bfcf5ca78fd23f04c5f96023fdc7c9de6e9b16b1d75b").stream().toList();

    List<SmolSale> newSales =
        smolSalesRepository.findByBlockTimestampIsAfterAndTweetedIsFalseOrderByBlockTimestampAsc(
            lastTweetedBlockTimestamp);
    if (!newSales.isEmpty()) {
      final Optional<Double> ethMktPriceOpt = coinGeckoService.getEthPrice();
      if (ethMktPriceOpt.isEmpty()) {
        // This will retry once we have an ethereum price
        return;
      }
      final NumberFormat decimalFormatZeroes = new DecimalFormat("#,###.00");
      final NumberFormat decimalFormatOptionalZeroes = new DecimalFormat("0.##");
      Double currentPrice = discordBot.getCurrentPrice();
      for (SmolSale smolSale : newSales) {
        RarityRank rarityRank =
            rarityRankRepository.findBySmolId(Long.valueOf(smolSale.getTokenId()));

        final BigDecimal usdPrice =
            smolSale.getSalePrice().multiply(BigDecimal.valueOf(currentPrice));
        final Double ethPrice = usdPrice.doubleValue() / ethMktPriceOpt.get();
        final String ethValue = decimalFormatOptionalZeroes.format(ethPrice);
        final String usdValue = decimalFormatZeroes.format(usdPrice);

        final var imgOpt =
            utilities.getSmolBufferedImage(smolSale.getTokenId().toString(), SmolType.SMOL, false);
        if (imgOpt.isEmpty()) {
          return;
        }

        final var img = imgOpt.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          ImageIO.write(img, "png", baos);
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }

        byte[] bytes = baos.toByteArray();
        final var mediaResponse =
            twitterClient.uploadMedia(
                smolSale.getTokenId() + "_smol.png", bytes, MediaCategory.TWEET_IMAGE);
        final var media = Media.builder().mediaIds(List.of(mediaResponse.getMediaId())).build();
        TweetParameters tweetParameters =
            TweetParameters.builder()
                .media(media)
                .text(
                    "Smol Brains #"
                        + smolSale.getTokenId()
                        + " (Rarity Rank #"
                        + rarityRank.getRank()
                        + ")\nSold for\nMAGIC: "
                        + decimalFormatOptionalZeroes.format(smolSale.getSalePrice())
                        + "\nUSD: $"
                        + usdValue
                        + "\nETH: "
                        + ethValue
                        + "\n\n"
                        + "https://marketplace.treasure.lol/collection/0x6325439389e0797ab35752b4f43a14c004f22a9c/"
                        + smolSale.getTokenId()
                        + "\n\n"
                        + "#smolbrains #treasuredao")
                .build();

        twitterClient.postTweet(tweetParameters);

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
