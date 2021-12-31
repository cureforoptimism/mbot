package com.cureforoptimism.mbot.service;

import com.cureforoptimism.mbot.application.DiscordBot;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Coins.CoinFullData;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CoinGeckoService implements MagicValueService {
  private final CoinGeckoApiClient client;
  private final DiscordBot discordClient;
  private final boolean enabled = false;
  @Getter private CoinFullData coinFullData;

  public CoinGeckoService(CoinGeckoApiClient client, DiscordBot discordClient) {
    this.client = client;
    this.discordClient = discordClient;
  }

  @Scheduled(fixedDelay = 30000)
  public synchronized void refreshMagicPrice() {
    this.coinFullData = client.getCoinById("magic");

    if (!enabled) {
      return;
    }

    try {
      final var priceMap = client.getPrice("magic", "usd", false, false, true, false);
      if (priceMap.containsKey("magic")
          && priceMap.get("magic").containsKey("usd")
          && priceMap.get("magic").containsKey("usd_24h_change")) {
        discordClient.refreshMagicPrice(
            priceMap.get("magic").get("usd"),
            priceMap.get("magic").get("usd_24h_change"),
            null,
            null,
            null,
            null,
            null,
            null,
            null);
      }
    } catch (Exception ex) {
      // Ignore, it'll retry
    }
  }
}
