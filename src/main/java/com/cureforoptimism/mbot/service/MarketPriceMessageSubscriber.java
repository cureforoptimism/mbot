package com.cureforoptimism.mbot.service;

import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.domain.MarketPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketPriceMessageSubscriber {
  private final DiscordBot discordBot;

  public void handleMessage(MarketPrice marketPrice) {
    discordBot.refreshMagicPrice(
        marketPrice.getPrice(),
        marketPrice.getChange(),
        marketPrice.getVolume12h(),
        marketPrice.getVolume24h(),
        marketPrice.getChange1h(),
        marketPrice.getVolume24h(),
        marketPrice.getChange12h(),
        marketPrice.getChange4h(),
        marketPrice.getVolume1h());
  }
}
