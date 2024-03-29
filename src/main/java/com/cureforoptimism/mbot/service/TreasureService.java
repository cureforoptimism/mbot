package com.cureforoptimism.mbot.service;

import static com.cureforoptimism.mbot.Constants.*;

import com.cureforoptimism.mbot.Constants;
import com.cureforoptimism.mbot.domain.*;
import com.cureforoptimism.mbot.repository.*;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.smolbrains.*;
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
import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TreasureService {
  private final SmolBodiesContract smolBodiesContract;
  private final SmolBrainsContract smolBrainsContract;
  private final SmolBrainsVroomContract smolBrainsVroomContract;
  @Getter private BigDecimal floor;
  @Getter private int totalListings;
  @Getter private int totalVroomListings;
  @Getter private BigDecimal landFloor;
  @Getter private int totalFloorListings;
  @Getter private BigDecimal cheapestMale;
  @Getter private BigDecimal cheapestFemale;
  @Getter private BigDecimal cheapestVroom;
  @Getter private int cheapestMaleId;
  @Getter private int cheapestFemaleId;
  @Getter private int cheapestVroomId;
  @Getter private BigDecimal bodyFloor;
  @Getter private BigDecimal petFloor;
  @Getter private BigDecimal bodyPetFloor;
  final SmolRepository smolRepository;
  final SmolBodyRepository smolBodyRepository;
  final VroomTraitsRepository vroomTraitsRepository;
  final FloorService floorService;
  final SmolSalesRepository smolSalesRepository;
  final PetRepository petRepository;
  final BodyPetRepository bodyPetRepository;
  final PetsContract petsContract;
  final BodyPetsContact bodyPetsContact;

  public BigDecimal getIq(int tokenId) {
    try {
      final var iqBig =
          smolBrainsContract.scanBrain(new BigInteger(String.valueOf(tokenId))).send();

      BigDecimal iq = BigDecimal.ZERO;
      if (!iqBig.equals(BigInteger.ZERO)) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        iq = new BigDecimal(iqBig, 18, mc);
      }

      return iq;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return BigDecimal.ZERO;
  }

  public BigDecimal getPlatez(int tokenId) {
    try {
      final var iqBig =
          smolBodiesContract.scanPlates(new BigInteger(String.valueOf(tokenId))).send();

      BigDecimal iq = BigDecimal.ZERO;
      if (!iqBig.equals(BigInteger.ZERO)) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        iq = new BigDecimal(iqBig, 18, mc);
      }

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
      final Map<String, Map<String, Integer>> rarityMap = new HashMap<>();

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

          // hot fix for bad metadata
          if (value.equalsIgnoreCase("dark-brown")) {
            value = "dark_brown";
          } else if (trait.equalsIgnoreCase("body") && value.equalsIgnoreCase("red")) {
            value = "orange";
          }

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

        if (x % 50 == 0) {
          log.info("GENERATED " + x);
        }

        smolRepository.save(Smol.builder().id((long) x).traits(traits).build());
      }
    } catch (Exception e) {
      log.error("Error retrieving rarity stats");
    }
  }

  @Transactional
  public void getAllVroomRarities() {
    try {
      String baseUri = smolBrainsVroomContract.baseURI().send();
      HttpClient httpClient = HttpClient.newHttpClient();
      final Map<String, Map<String, Integer>> rarityMap = new HashMap<>();

      for (int x = 1; x <= SMOL_VROOM_TOTAL_SUPPLY; x++) {
        Set<VroomTrait> traits = new HashSet<>();

        HttpRequest request = HttpRequest.newBuilder().uri(new URI(baseUri + x)).GET().build();

        final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray attributes = new JSONObject(response.body()).getJSONArray("attributes");
        for (int y = 0; y < attributes.length(); y++) {
          JSONObject obj = attributes.getJSONObject(y);
          String trait = obj.getString("trait_type");
          String value = obj.get("value").toString();

          traits.add(
              VroomTrait.builder()
                  .type(trait)
                  .value(value)
                  .smol(Smol.builder().id((long) x).build())
                  .build());

          if (!rarityMap.containsKey(trait)) {
            rarityMap.put(trait, new HashMap<>());
          }

          rarityMap.get(trait).merge(value, 1, Integer::sum);
        }
        smolRepository.save(Smol.builder().id((long) x).vroomTraits(traits).build());
      }
    } catch (Exception e) {
      log.error("Error retrieving vroom rarity stats");
    }
  }

  @Transactional
  public void getAllSmallBodyRarities() {
    try {
      String baseUri = smolBodiesContract.baseURI().send();
      HttpClient httpClient = HttpClient.newHttpClient();
      final Map<String, Map<String, Integer>> rarityMap = new HashMap<>();

      for (int x = 0; x <= 1; x++) {
        Set<SmolBodyTrait> traits = new HashSet<>();

        HttpRequest request =
            HttpRequest.newBuilder().uri(new URI(baseUri + x + "/0")).GET().build();

        final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray attributes = new JSONObject(response.body()).getJSONArray("attributes");
        for (int y = 0; y < attributes.length(); y++) {
          JSONObject obj = attributes.getJSONObject(y);
          String trait = obj.getString("trait_type");
          String value = obj.get("value").toString();

          traits.add(
              SmolBodyTrait.builder()
                  .type(trait)
                  .value(value)
                  .smolBody(SmolBody.builder().id((long) x).build())
                  .build());

          if (!rarityMap.containsKey(trait)) {
            rarityMap.put(trait, new HashMap<>());
          }

          rarityMap.get(trait).merge(value, 1, Integer::sum);
        }

        if (x % 100 == 0) {
          log.info("Rarities: " + x + " (" + SMOL_BODY_HIGHEST_ID + ")");
        }

        smolBodyRepository.save(SmolBody.builder().id((long) x).traits(traits).build());
      }
    } catch (Exception e) {
      log.error("Error retrieving vroom rarity stats");
    }
  }

  public void getAllBodyPetsRarities() {
    try {
      String baseUri = bodyPetsContact.baseURI().send();
      baseUri = baseUri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");
      HttpClient httpClient = HttpClient.newHttpClient();
      final Map<String, Map<String, Integer>> rarityMap = new HashMap<>();

      for (int x = 0; x <= Constants.BODY_PET_HIGHEST_ID; x++) {
        if (bodyPetRepository.existsById((long) x)) {
          continue;
        }

        Set<BodyPetTrait> traits = new HashSet<>();

        HttpRequest request =
            HttpRequest.newBuilder().uri(new URI(baseUri + x + ".json")).GET().build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        while (response.statusCode() != 200) {
          log.info("Error retrieving token; will try again (token id: " + x + ")");
          Thread.sleep(1000L);
          response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        JSONArray attributes = new JSONObject(response.body()).getJSONArray("attributes");
        for (int y = 0; y < attributes.length(); y++) {
          JSONObject obj = attributes.getJSONObject(y);
          String trait = obj.getString("trait_type");
          String value = obj.get("value").toString();

          traits.add(
              BodyPetTrait.builder()
                  .type(trait)
                  .value(value)
                  .bodyPet(BodyPet.builder().id((long) x).build())
                  .build());

          if (!rarityMap.containsKey(trait)) {
            rarityMap.put(trait, new HashMap<>());
          }

          rarityMap.get(trait).merge(value, 1, Integer::sum);
        }

        if (x % 100 == 0) {
          log.info("Rarities: " + (x + 1) + " (" + Constants.BODY_PET_TOTAL_SUPPLY + ")");
        }

        bodyPetRepository.save(BodyPet.builder().id((long) x).traits(traits).build());
      }
    } catch (Exception e) {
      log.error("Error retrieving body pet rarity stats");
    }
  }

  @Transactional
  public void getAllPetsRarities() {
    try {
      String baseUri = petsContract.baseURI().send();
      baseUri = baseUri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");
      HttpClient httpClient = HttpClient.newHttpClient();
      final Map<String, Map<String, Integer>> rarityMap = new HashMap<>();

      for (int x = 0; x <= Constants.PET_HIGHEST_ID; x++) {
        if (petRepository.existsById((long) x)) {
          continue;
        }

        Set<PetTrait> traits = new HashSet<>();

        HttpRequest request =
            HttpRequest.newBuilder().uri(new URI(baseUri + x + ".json")).GET().build(); // TODO

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        while (response.statusCode() != 200) {
          log.info("Error retrieving token; will try again (token id: " + x + ")");
          Thread.sleep(1000L);
          response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        JSONArray attributes = new JSONObject(response.body()).getJSONArray("attributes");
        for (int y = 0; y < attributes.length(); y++) {
          JSONObject obj = attributes.getJSONObject(y);
          String trait = obj.getString("trait_type");
          String value = obj.get("value").toString();

          traits.add(
              PetTrait.builder()
                  .type(trait)
                  .value(value)
                  .pet(Pet.builder().id((long) x).build())
                  .build());

          if (!rarityMap.containsKey(trait)) {
            rarityMap.put(trait, new HashMap<>());
          }

          rarityMap.get(trait).merge(value, 1, Integer::sum);
        }

        if (x % 100 == 0) {
          log.info("Rarities: " + x + " (" + Constants.PET_HIGHEST_ID + ")");
        }

        petRepository.save(Pet.builder().id((long) x).traits(traits).build()); // TODO
      }
    } catch (Exception e) {
      log.error("Error retrieving pet rarity stats");
    }
  }

  public byte[] getAnimatedGif(String tokenId, SmolType smolType, boolean reverse, int msDelay) {
    try {
      String baseUri =
          switch (smolType) {
            case SMOL -> smolBrainsContract.baseURI().send();
            case SMOL_BODY -> smolBodiesContract.baseURI().send();
            default -> "";
          };

      if (baseUri.isEmpty()) {
        return null;
      }

      HttpClient httpClient = HttpClient.newHttpClient();

      ByteArrayOutputStream finishedImage = new ByteArrayOutputStream();
      AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
      gifEncoder.start(finishedImage);
      gifEncoder.setRepeat(0);
      gifEncoder.setDelay(msDelay);

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
  public synchronized void updateLatestSales() {
    String jsonBody =
        "{\"query\":\"query getActivity($id: String!, $orderBy: Listing_orderBy!) {\\n  listings(\\n    where: {status: Sold, collection: $id}\\n    orderBy: $orderBy\\n    orderDirection: desc\\n  ) {\\n    ...ListingFields\\n  }\\n}\\n\\nfragment ListingFields on Listing {\\n  blockTimestamp\\n  buyer {\\n    id\\n  }\\n  id\\n  pricePerItem\\n  quantity\\n  seller {\\n    id\\n  }\\n  token {\\n    id\\n    tokenId\\n  }\\n  collection {\\n    id\\n  }\\n  transactionLink\\n}\",\"variables\":{\"id\":\"0x6325439389e0797ab35752b4f43a14c004f22a9c\",\"orderBy\":\"blockTimestamp\"},\"operationName\":\"getActivity\"}";
    try {
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/treasureproject/marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      JSONObject obj = new JSONObject(response.body()).getJSONObject("data");
      JSONArray listings = obj.getJSONArray("listings");

      MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

      for (int x = 0; x < listings.length(); x++) {
        final var listing = listings.getJSONObject(x);

        String transactionId = listing.getString("transactionLink");
        if (!smolSalesRepository.existsById(transactionId)) {
          int tokenId = listing.getJSONObject("token").getInt("tokenId");
          BigDecimal pricePerItem = new BigDecimal(listing.getBigInteger("pricePerItem"), 18, mc);
          Date blockTimeStamp = new Date(listing.getLong("blockTimestamp") * 1000);

          smolSalesRepository.save(
              SmolSale.builder()
                  .id(transactionId)
                  .tokenId(tokenId)
                  .salePrice(pricePerItem)
                  .blockTimestamp(blockTimeStamp)
                  .tweeted(false)
                  .build());
        }
      }
    } catch (Exception ex) {
      log.error("Exception updating latest sales", ex);
    }
  }

  @Scheduled(fixedDelay = 60000)
  public void getFloorPrice() {
    List<String> tokenListings = new ArrayList<>();
    MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

    try {
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-brains/tokens?offset=0&limit=25&sort_by=price&order=asc&traits=Gender:male"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      // Get cheapest male
      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray tokens = new JSONObject(response.body()).getJSONArray("tokens");

        final var price =
            tokens.getJSONObject(0).getJSONObject("priceSummary").getBigInteger("floorPrice");
        final var tokenId = tokens.getJSONObject(0).getInt("tokenId");

        this.cheapestMale = new BigDecimal(price, 18, mc);
        this.cheapestMaleId = tokenId;
      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }

      // Get cheapest female
      request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-brains/tokens?offset=0&limit=25&sort_by=price&order=asc&traits=Gender:female"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray tokens = new JSONObject(response.body()).getJSONArray("tokens");

        final var price =
            tokens.getJSONObject(0).getJSONObject("priceSummary").getBigInteger("floorPrice");
        final var tokenId = tokens.getJSONObject(0).getInt("tokenId");

        this.cheapestFemale = new BigDecimal(price, 18, mc);
        this.cheapestFemaleId = tokenId;
      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }

      if (cheapestMale == null) {
        cheapestMale = new BigDecimal(0);
        floor = cheapestFemale;
      } else {
        floor = cheapestMale.compareTo(cheapestFemale) > 0 ? cheapestFemale : cheapestMale;
      }

      // Get total listings
      // TODO: FIX
      String jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    floorPrice\\n    totalListings\\n    totalVolume\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        name\\n      }\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0x6325439389e0797ab35752b4f43a14c004f22a9c\"},\"operationName\":\"getCollectionStats\"}";
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

        totalListings = obj.getInt("totalListings");
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
        return;
      }

      // Get land data
      request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-brains-land"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj = new JSONObject(response.body());
        final var floorPrice = obj.getBigInteger("floorPrice");

        // TODO: Fix
        //        totalFloorListings = obj.getInt("totalListings");

        landFloor = new BigDecimal(floorPrice, 18, mc);
      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }

      // Get vroom total listings/floor
      request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-cars/tokens?offset=0&limit=25&sort_by=price&order=asc"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray tokens = new JSONObject(response.body()).getJSONArray("tokens");
        final var price =
            tokens.getJSONObject(0).getJSONObject("priceSummary").getBigInteger("floorPrice");
        final var tokenId = tokens.getJSONObject(0).getInt("tokenId");

        this.cheapestVroom = new BigDecimal(price, 18, mc);
        this.cheapestVroomId = tokenId;

        // TODO: Fix
        //        totalVroomListings = obj.getInt("totalListings");
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
        return;
      }

      // Body floor
      request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-bodies/tokens?offset=0&limit=25&sort_by=price&order=asc"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray tokens = new JSONObject(response.body()).getJSONArray("tokens");

        final var price =
            tokens.getJSONObject(0).getJSONObject("priceSummary").getBigInteger("floorPrice");

        bodyFloor = new BigDecimal(price, 18, mc);
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
      }

      // Pet (Brains) floor
      request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-brains-pets/tokens?offset=0&limit=25&sort_by=price&order=asc"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray tokens = new JSONObject(response.body()).getJSONArray("tokens");

        final var price =
            tokens.getJSONObject(0).getJSONObject("priceSummary").getBigInteger("floorPrice");

        petFloor = new BigDecimal(price, 18, mc);
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
      }

      // Pet (Swols) floor
      request =
          HttpRequest.newBuilder(
                  new URI(
                      "https://hfihu314z3.execute-api.us-east-1.amazonaws.com/collection/arb/smol-bodies-pets/tokens?offset=0&limit=25&sort_by=price&order=asc"))
              .GET()
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray tokens = new JSONObject(response.body()).getJSONArray("tokens");

        final var price =
            tokens.getJSONObject(0).getJSONObject("priceSummary").getBigInteger("floorPrice");

        bodyPetFloor = new BigDecimal(price, 18, mc);
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
      }

      // Record data in floor table
      floorService.addFloorPrice(
          cheapestMale,
          cheapestFemale,
          landFloor,
          cheapestVroom,
          bodyFloor,
          petFloor,
          bodyPetFloor);
    } catch (IOException | URISyntaxException ex) {
      log.warn("Failed to retrieve treasure: ", ex);
    }
  }
}
