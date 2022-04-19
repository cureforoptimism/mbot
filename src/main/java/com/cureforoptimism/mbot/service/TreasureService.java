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

    try {
      // First, we need to get the list of tokenIds (collection + random identifier)
      String jsonBody =
          "{\"query\":\"query getCollectionsListedTokens($collection: String!) {\\n  listings(\\n    first: 1000\\n    where: {collection: $collection, status: Active, quantity_gt: 0}\\n    orderBy: id\\n  ) {\\n    token {\\n      id\\n    }\\n  }\\n}\",\"variables\":{\"collection\":\"0x6325439389e0797ab35752b4f43a14c004f22a9c\"},\"operationName\":\"getCollectionsListedTokens\"}";

      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/treasureproject/marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray listings =
            new JSONObject(response.body()).getJSONObject("data").getJSONArray("listings");
        for (int x = 0; x < listings.length(); x++) {
          String tokenId = listings.getJSONObject(x).getJSONObject("token").getString("id");
          tokenListings.add(tokenId);
        }
      } catch (InterruptedException | JSONException ex) {
        log.warn("Error parsing treasure response", ex);
      }

      // Join the tokenId's for the next graphql filter (poor man's join)
      StringBuilder joined = new StringBuilder();
      joined.append("\"");
      for (String tokenId : tokenListings) {
        joined.append(tokenId).append("\",\"");
      }

      // Chop of trailing ,"
      String tokenIds = joined.substring(0, joined.length() - 2);

      jsonBody =
          "{\"query\":\"query getCollectionListings($erc1155Filters: Token_filter, $erc1155Ordering: Token_orderBy, $erc721Filters: Listing_filter, $erc721Ordering: Listing_orderBy, $isERC1155: Boolean!, $orderDirection: OrderDirection, $skip: Int) {\\n  tokens(\\n    first: 200\\n    orderBy: floorPrice\\n    orderDirection: $orderDirection\\n    where: $erc1155Filters\\n  ) @include(if: $isERC1155) {\\n    __typename\\n    id\\n    floorPrice\\n    tokenId\\n    listings(where: {status: Active, quantity_gt: 0}, orderBy: pricePerItem) {\\n      pricePerItem\\n      quantity\\n    }\\n  }\\n  listings(\\n    first: 42\\n    orderBy: $erc721Ordering\\n    orderDirection: $orderDirection\\n    skip: $skip\\n    where: $erc721Filters\\n  ) @skip(if: $isERC1155) {\\n    __typename\\n    seller {\\n      id\\n    }\\n    expires\\n    id\\n    pricePerItem\\n    token {\\n      id\\n      tokenId\\n      name\\n    }\\n    quantity\\n  }\\n}\",\"variables\":{\"erc1155Filters\":{\"id_in\":[";
      jsonBody += tokenIds;
      jsonBody += "]},\"erc721Filters\":{\"status\":\"Active\",\"token_in\":[";
      jsonBody += tokenIds;
      jsonBody +=
          "],\"quantity_gt\":0},\"erc721Ordering\":\"pricePerItem\",\"isERC1155\":false,\"orderDirection\":\"asc\",\"skip\":0},\"operationName\":\"getCollectionListings\"}";

      httpClient = HttpClient.newHttpClient();
      request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/treasureproject/marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

        // Find cheapest male/female
        boolean cheapestMaleFound = false;
        boolean cheapestFemaleFound = false;
        JSONArray listings =
            new JSONObject(response.body()).getJSONObject("data").getJSONArray("listings");

        for (int x = 0; x < listings.length(); x++) {
          int tokenId = listings.getJSONObject(x).getJSONObject("token").getInt("tokenId");

          try {
            var gender =
                smolBrainsContract.getGender(new BigInteger(String.valueOf(tokenId))).send();

            // TODO: Remove 1944L; this is a floor hotfix
            if (gender.intValue() == 0 && !cheapestMaleFound && tokenId != 1944L) {
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

      // Get total listings
      jsonBody =
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

      // Get vroom total listings/floor
      jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    floorPrice\\n    totalListings\\n    totalVolume\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        name\\n      }\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0xb16966dad2b5a5282b99846b23dcdf8c47b6132c\"},\"operationName\":\"getCollectionStats\"}";
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

        totalVroomListings = obj.getInt("totalListings");
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
        return;
      }

      jsonBody =
          "{\"query\":\"query getCollectionListings($id: ID!, $orderDirection: OrderDirection!, $tokenName: String, $skipBy: Int!, $first: Int!, $orderBy: Listing_orderBy!, $isERC1155: Boolean!) {\\n  collection(id: $id) {\\n    name\\n    address\\n    standard\\n    tokens(\\n      orderBy: floorPrice\\n      orderDirection: $orderDirection\\n      where: {name_contains: $tokenName}\\n    ) @include(if: $isERC1155) {\\n      id\\n      name\\n      tokenId\\n      listings(where: {status: Active}, orderBy: pricePerItem) {\\n        pricePerItem\\n        quantity\\n      }\\n      metadata {\\n        image\\n        name\\n        description\\n      }\\n    }\\n    listings(\\n      first: $first\\n      skip: $skipBy\\n      orderBy: $orderBy\\n      orderDirection: $orderDirection\\n      where: {status: Active, tokenName_contains: $tokenName}\\n    ) @skip(if: $isERC1155) {\\n      user {\\n        id\\n      }\\n      expires\\n      id\\n      pricePerItem\\n      token {\\n        tokenId\\n        metadata {\\n          image\\n          name\\n          description\\n        }\\n        name\\n      }\\n      quantity\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0xb16966dad2b5a5282b99846b23dcdf8c47b6132c\",\"isERC1155\":false,\"tokenName\":\"\",\"skipBy\":0,\"first\":42,\"orderBy\":\"pricePerItem\",\"orderDirection\":\"asc\"},\"operationName\":\"getCollectionListings\"}";
      request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/wyze/treasure-marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj =
            new JSONObject(response.body()).getJSONObject("data").getJSONObject("collection");

        // Hotfix: Skip 4547 (bugged listing)
        JSONObject firstListing = obj.getJSONArray("listings").getJSONObject(0);
        if (firstListing.getJSONObject("token").getInt("tokenId") == 4547) {
          firstListing = obj.getJSONArray("listings").getJSONObject(1);
        }

        final var price = firstListing.getBigInteger("pricePerItem");
        this.cheapestVroom = new BigDecimal(price, 18, mc);
        this.cheapestVroomId = firstListing.getJSONObject("token").getInt("tokenId");
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
      }

      // Body floor (also, for the love of God cure, just rewrite thegraph query to do this in fewer
      // calls)
      jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        tokenId\\n        name\\n      }\\n    }\\n    standard\\n    stats {\\n      floorPrice\\n      listings\\n      items\\n      volume\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0x17dacad7975960833f374622fad08b90ed67d1b5\"},\"operationName\":\"getCollectionStats\"}";
      request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/treasureproject/marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj =
            new JSONObject(response.body())
                .getJSONObject("data")
                .getJSONObject("collection")
                .getJSONObject("stats");

        final var floorPrice = obj.getBigInteger("floorPrice");

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        bodyFloor = new BigDecimal(floorPrice, 18, mc);
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
      }

      // Pet (Brains) floor
      jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    name\\n    floorPrice\\n    totalListings\\n    totalVolume\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        tokenId\\n        name\\n      }\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0xf6cc57c45ce730496b4d3df36b9a4e4c3a1b9754\"},\"operationName\":\"getCollectionStats\"}";
      request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/treasureproject/marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj =
            new JSONObject(response.body()).getJSONObject("data").getJSONObject("collection");

        final var floorPrice = obj.getBigInteger("floorPrice");

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        petFloor = new BigDecimal(floorPrice, 18, mc);
      } catch (InterruptedException ex) {
        // Whatever; it'll retry
      }

      // Pet (Brains) floor
      jsonBody =
          "{\"query\":\"query getCollectionStats($id: ID!) {\\n  collection(id: $id) {\\n    name\\n    floorPrice\\n    totalListings\\n    totalVolume\\n    listings(where: {status: Active}) {\\n      token {\\n        floorPrice\\n        tokenId\\n        name\\n      }\\n    }\\n  }\\n}\",\"variables\":{\"id\":\"0xae0d0c4cc3335fd49402781e406adf3f02d41bca\"},\"operationName\":\"getCollectionStats\"}";
      request =
          HttpRequest.newBuilder(
                  new URI("https://api.thegraph.com/subgraphs/name/treasureproject/marketplace"))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .header("Content-Type", "application/json")
              .build();

      try {
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject obj =
            new JSONObject(response.body()).getJSONObject("data").getJSONObject("collection");

        final var floorPrice = obj.getBigInteger("floorPrice");

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        bodyPetFloor = new BigDecimal(floorPrice, 18, mc);
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
