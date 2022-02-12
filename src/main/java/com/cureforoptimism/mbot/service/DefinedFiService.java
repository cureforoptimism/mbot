package com.cureforoptimism.mbot.service;

import com.cureforoptimism.mbot.application.DiscordBot;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefinedFiService implements MagicValueService {
  private final DiscordBot discordClient;
  private final boolean enabled = false;

  @Override
  @Scheduled(fixedDelay = 30000)
  public void refreshMagicPrice() {
    if(!enabled) {
      return;
    }

    final var client = HttpClient.newHttpClient();
    final HttpRequest req;
    try {
      String jsonBody =
          "{\"operationName\": \"GetPairMetadata\",\n"
              + "  \"variables\": {\n"
              + "    \"pairId\": \"0xb7e50106a5bd3cf21af210a755f9c8740890a8c9:42161\"\n"
              + "  },\n"
              + "  \"query\": \"query GetPairMetadata($pairId: String!) {pairMetadata(pairId: $pairId) {\\n    price\\n    exchangeId\\n    fee\\n    id\\n    liquidity\\n    liquidityToken\\n    nonLiquidityToken\\n    pairAddress\\n    priceChange\\n    priceChange1\\n    priceChange12\\n    priceChange24\\n    priceChange4\\n    tickSpacing\\n    volume\\n    volume1\\n    volume12\\n    volume24\\n    volume4\\n    token0 {\\n      address\\n      decimals\\n      name\\n      networkId\\n      pooled\\n      price\\n      symbol\\n      __typename\\n    }\\n    token1 {\\n      address\\n      decimals\\n      name\\n      networkId\\n      pooled\\n      price\\n      symbol\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"\n"
              + "}";

      req =
          HttpRequest.newBuilder(
                  new URI(
                      "https://7ng6kythprhcjaby3nodr77leu.appsync-api.us-west-2.amazonaws.com/graphql"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .header("x-api-key", "da2-4isjqg3vu5c5leyskyr2tw2tja")
              .build();

      final var response = client.send(req, HttpResponse.BodyHandlers.ofString());

      JSONObject obj =
          new JSONObject(response.body()).getJSONObject("data").getJSONObject("pairMetadata");
      BigDecimal price = obj.getBigDecimal("price");
      Double change = obj.getDouble("priceChange24") * 100.0d;
      Double change12h = obj.getDouble("priceChange12") * 100.0d;
      Double change4h = obj.getDouble("priceChange4") * 100.0d;
      Double change1h = obj.getDouble("priceChange1") * 100.0d;

      Double volume24h = obj.getDouble("volume24");
      Double volume12h = obj.getDouble("volume12");
      Double volume4h = obj.getDouble("volume4");
      Double volume1h = obj.getDouble("volume1");

      price = price.setScale(4, RoundingMode.HALF_UP);

      discordClient.refreshMagicPrice(
          price.doubleValue(),
          change,
          change12h,
          change4h,
          change1h,
          volume24h,
          volume12h,
          volume4h,
          volume1h);
    } catch (URISyntaxException | InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }
}
