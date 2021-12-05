package com.cureforoptimism.mbot.service;

import com.cureforoptimism.mbot.domain.Smol;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.repository.SmolRepository;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.smolbrains.SmolBrainsContract;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
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
import java.util.*;

import static com.cureforoptimism.mbot.Constants.SMOL_TOTAL_SUPPLY;

@Component
@Slf4j
public class TreasureService {
  private final SmolBrainsContract smolBrainsContract;
  @Getter private BigDecimal floor;
  @Getter private int totalListings;
  @Getter private BigDecimal landFloor;
  @Getter private int totalFloorListings;
  @Getter private BigDecimal cheapestMale;
  @Getter private BigDecimal cheapestFemale;
  @Getter private int cheapestMaleId;
  @Getter private int cheapestFemaleId;
  private final Map<String, Map<String, Integer>> rarityMap;
  final SmolRepository smolRepository;

  public TreasureService(SmolBrainsContract smolBrainsContract, SmolRepository smolRepository) {
    this.smolBrainsContract = smolBrainsContract;
    this.smolRepository = smolRepository;
    this.rarityMap = new HashMap<>();
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

  @Transactional
  public void getAllRarities() {
    try {
      String baseUri = smolBrainsContract.baseURI().send();
      HttpClient httpClient = HttpClient.newHttpClient();

      for (int x = 0; x <= SMOL_TOTAL_SUPPLY; x++) {
        Set<Trait> traits = new HashSet<>();

        HttpRequest request =
            HttpRequest.newBuilder().uri(new URI(baseUri + x + "/0")).GET().build();

        final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray attributes = new JSONObject(response.body()).getJSONArray("attributes");
        for (int y = 0; y < attributes.length(); y++) {
          JSONObject obj = attributes.getJSONObject(y);
          String trait = obj.getString("trait_type");
          String value = obj.get("value").toString();

          traits.add(
              Trait.builder()
                  .type(trait)
                  .value(value)
                  .smol(Smol.builder().id((long) x).build())
                  .build());

          if (!rarityMap.containsKey(trait)) {
            rarityMap.put(trait, new HashMap<>());
          }

          rarityMap.get(trait).merge(value, 1, Integer::sum);
        }
        smolRepository.save(Smol.builder().id((long) x).traits(traits).build());
      }
    } catch (Exception e) {
      log.error("Error retrieving rarity stats");
    }
  }

  public long getTraitsCount(String trait, String value) {
    return smolRepository.countByTraits_TypeAndTraits_Value(trait, value);
  }

  public byte[] getAnimatedGif(String tokenId, boolean reverse) {
    try {
      String baseUri = smolBrainsContract.baseURI().send();
      HttpClient httpClient = HttpClient.newHttpClient();

      ByteArrayOutputStream finishedImage = new ByteArrayOutputStream();
      AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
      gifEncoder.start(finishedImage);
      gifEncoder.setRepeat(0);
      gifEncoder.setDelay(1000);

      List<BufferedImage> images = new ArrayList<>();

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
          images.add(bufferedImage);
          gifEncoder.addFrame(bufferedImage);
        } catch (JSONException ex) {
          // Probably not a valid token. Just return.
          return null;
        }
      }

      if (reverse) {
        for (int x = 5; x >= 0; x--) {
          BufferedImage bufferedImage = images.get(x);
          gifEncoder.addFrame(bufferedImage);
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
      final var totalSupply = 13421;
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
          "{\"query\":\"query getCollectionListings($id: ID!, $orderDirection: OrderDirection!, $tokenName: String, $skipBy: Int!, $first: Int!, $orderBy: Listing_orderBy!) {\\n  collection(id: $id) {\\n    name\\n    address\\n    listings(\\n      first: $first\\n      skip: $skipBy\\n      orderBy: $orderBy\\n      orderDirection: $orderDirection\\n      where: {status: Active, tokenName_contains: $tokenName, quantity_gt: 0}\\n    ) {\\n      user {\\n        id\\n      }\\n      expires\\n      id\\n      pricePerItem\\n      token {\\n        tokenId\\n        metadata {\\n          image\\n          name\\n          description\\n        }\\n        name\\n      }\\n      quantity\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0x6325439389e0797ab35752b4f43a14c004f22a9c\",\"tokenName\":\"\",\"skipBy\":0,\"first\":42,\"orderBy\":\"pricePerItem\",\"orderDirection\":\"asc\"},\"operationName\":\"getCollectionListings\"}";

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
        //        final var floorPrice = obj.getBigInteger("floorPrice");
        //        totalListings = obj.getInt("totalListings");

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

        // Find cheapest male/female
        boolean cheapestMaleFound = false;
        boolean cheapestFemaleFound = false;
        JSONArray listings = obj.getJSONArray("listings");
        totalListings = listings.length();

        for (int x = 0; x < listings.length(); x++) {
          int tokenId = listings.getJSONObject(x).getJSONObject("token").getInt("tokenId");

          try {
            var gender =
                smolBrainsContract.getGender(new BigInteger(String.valueOf(tokenId))).send();
            if (gender.intValue() == 0 && !cheapestMaleFound) {
              cheapestMaleFound = true;

              final var price = listings.getJSONObject(x).getBigInteger("pricePerItem");
              this.cheapestMale = new BigDecimal(price, 18, mc);
              this.cheapestMaleId = tokenId;
            } else if (gender.intValue() == 1 && !cheapestFemaleFound) {
              cheapestFemaleFound = true;

              final var price = listings.getJSONObject(x).getBigInteger("pricePerItem");
              this.cheapestFemale = new BigDecimal(price, 18, mc);
              this.cheapestFemaleId = tokenId;
            }

            if (cheapestMaleFound && cheapestFemaleFound) {
              break;
            }
          } catch (Exception ex) {
            log.warn("Unable to retrieve gender", ex);
          }
        }

      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }

      floor = cheapestMale.compareTo(cheapestFemale) > 0 ? cheapestFemale : cheapestMale;

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
