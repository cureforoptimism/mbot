package com.cureforoptimism.mbot.service;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.smolbrains.SmolBrainsContract;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TreasureService {
  private final SmolBrainsContract smolBrainsContract;
  @Getter private BigDecimal floor;
  @Getter private int totalListings;
  @Getter private BigDecimal landFloor;
  @Getter private int totalFloorListings;

  public TreasureService(SmolBrainsContract smolBrainsContract) {
    this.smolBrainsContract = smolBrainsContract;
  }

  public BigDecimal getIq(int tokenId) {
    try {
      final var iqBig =
          smolBrainsContract.scanBrain(new BigInteger(String.valueOf(tokenId))).send();

      MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
      final var iq = new BigDecimal(iqBig, 18, mc);

      log.info(iq.toString());

      return iq;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return BigDecimal.ZERO;
  }

  public byte[] getAnimatedGif(String tokenId) {
    try {
      String baseUri = smolBrainsContract.baseURI().send();
      HttpClient httpClient = HttpClient.newHttpClient();

      ByteArrayOutputStream finishedImage = new ByteArrayOutputStream();
      AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
      gifEncoder.start(finishedImage);
      gifEncoder.setRepeat(0);
      gifEncoder.setDelay(1000);

      for (int x = 0; x <= 5; x++) {
        HttpRequest request =
            HttpRequest.newBuilder().uri(new URI(baseUri + tokenId + "/" + x)).GET().build();

        final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        try {
          final var obj = new JSONObject(response.body()).getString("image");
          request = HttpRequest.newBuilder(new URI(obj)).GET().build();

          final var image = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
          InputStream inputStream = new ByteArrayInputStream(image.body());
          BufferedImage bufferedImage = ImageIO.read(inputStream);
          gifEncoder.addFrame(bufferedImage);
        } catch (JSONException ex) {
          // Probably not a valid token. Just return.
          return null;
        }
      }

      gifEncoder.finish();

      return finishedImage.toByteArray();
    } catch (Exception ex) {
      log.error("Error generating animated pfp", ex);
    }

    return null;
  }

  public Map.Entry<Integer, BigDecimal> getHighestIq() {
    try {
      final var totalSupply = smolBrainsContract.totalSupply().send();
      Map<Integer, BigDecimal> iqs = new HashMap<>();
      for (int x = 1; x <= 100; x++) {
        try {
          final var iqBig =
              smolBrainsContract.scanBrain(new BigInteger(Integer.toString(x))).send();
          MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
          final var iq = new BigDecimal(iqBig, 18, mc);

          iqs.put(x, iq);
          log.info("fetched IQ for " + x);
        } catch (Exception ex) {
          log.warn("error fetching " + x + "; ", ex);
        }
      }

      final var highest = iqs.entrySet().stream().max(Map.Entry.comparingByValue());
      if (highest.isEmpty()) {
        return null;
      }
      return highest.get();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  @Scheduled(fixedDelay = 60000)
  public void getFloorPrice() {
    try {
      String jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    floorPrice\\n    totalListings\\n    totalVolume\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        name\\n      }\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0x6325439389e0797ab35752b4f43a14c004f22a9c\"},\"operationName\":\"getCollectionStats\"}";

      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/wyze/treasure-marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj =
            new JSONObject(response.body()).getJSONObject("data").getJSONObject("collection");
        final var floorPrice = obj.getBigInteger("floorPrice");
        totalListings = obj.getInt("totalListings");

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        floor = new BigDecimal(floorPrice, 18, mc);
      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }

      jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    floorPrice\\n    totalListings\\n    totalVolume\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        name\\n      }\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0xd666d1cc3102cd03e07794a61e5f4333b4239f53\"},\"operationName\":\"getCollectionStats\"}";
      request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/wyze/treasure-marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj =
            new JSONObject(response.body()).getJSONObject("data").getJSONObject("collection");
        final var floorPrice = obj.getBigInteger("floorPrice");
        totalFloorListings = obj.getInt("totalListings");

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        landFloor = new BigDecimal(floorPrice, 18, mc);
      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }
    } catch (IOException | URISyntaxException ex) {
      log.warn("Failed to retrieve treasure: ", ex);
    }
  }
}
