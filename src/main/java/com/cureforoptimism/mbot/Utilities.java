package com.cureforoptimism.mbot;

import static com.cureforoptimism.mbot.Constants.SMOL_BODY_HIGHEST_ID;
import static com.cureforoptimism.mbot.Constants.SMOL_BODY_TOTAL_SUPPLY;
import static com.cureforoptimism.mbot.Constants.SMOL_HIGHEST_ID;
import static com.cureforoptimism.mbot.Constants.SMOL_TOTAL_SUPPLY;
import static com.cureforoptimism.mbot.Constants.SMOL_VROOM_TOTAL_SUPPLY;

import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.domain.SmolBodyRarityRank;
import com.cureforoptimism.mbot.domain.SmolBodyTrait;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.domain.VroomRarityRank;
import com.cureforoptimism.mbot.domain.VroomTrait;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import com.cureforoptimism.mbot.repository.SmolBodyRarityRankRepository;
import com.cureforoptimism.mbot.repository.SmolBodyTraitsRepository;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import com.cureforoptimism.mbot.repository.VroomRarityRankRepository;
import com.cureforoptimism.mbot.repository.VroomTraitsRepository;
import com.cureforoptimism.mbot.service.TreasureService;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.SimpleTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import com.smolbrains.SmolBodiesContract;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsRocketContract;
import com.smolbrains.SmolBrainsVroomContract;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class Utilities {
  private final TreasureService treasureService;
  private final RarityRankRepository rarityRankRepository;
  private final TraitsRepository traitsRepository;
  private final VroomTraitsRepository vroomTraitsRepository;
  private final SmolBodyTraitsRepository smolBodyTraitsRepository;
  private final SmolBrainsVroomContract smolBrainsVroomContract;
  private final SmolBrainsContract smolBrainsContract;
  private final VroomRarityRankRepository vroomRarityRankRepository;
  private final SmolBodyRarityRankRepository smolBodyRarityRankRepository;
  private final SmolBrainsRocketContract smolBrainsRocketContract;
  private String smolBaseUri;
  private String vroomBaseUri;
  private String smolBodyBaseUri;

  public Utilities(
      TreasureService treasureService,
      RarityRankRepository rarityRankRepository,
      TraitsRepository traitsRepository,
      VroomTraitsRepository vroomTraitsRepository,
      SmolBrainsContract smolBrainsContract,
      SmolBrainsVroomContract smolBrainsVroomContract,
      VroomRarityRankRepository vroomRarityRankRepository,
      SmolBodyTraitsRepository smolBodyTraitsRepository,
      SmolBodyRarityRankRepository smolBodyRarityRankRepository,
      SmolBodiesContract smolBodiesContract,
      SmolBrainsRocketContract smolBrainsRocketContract) {
    this.treasureService = treasureService;
    this.rarityRankRepository = rarityRankRepository;
    this.traitsRepository = traitsRepository;
    this.vroomTraitsRepository = vroomTraitsRepository;
    this.smolBrainsVroomContract = smolBrainsVroomContract;
    this.vroomRarityRankRepository = vroomRarityRankRepository;
    this.smolBodyTraitsRepository = smolBodyTraitsRepository;
    this.smolBodyRarityRankRepository = smolBodyRarityRankRepository;
    this.smolBrainsContract = smolBrainsContract;
    this.smolBrainsRocketContract = smolBrainsRocketContract;

    try {
      this.smolBaseUri = smolBrainsContract.baseURI().send();
      this.vroomBaseUri = smolBrainsVroomContract.baseURI().send();
      this.smolBodyBaseUri = smolBodiesContract.baseURI().send();
    } catch (Exception ex) {
      log.error("Unable to retrieve SMOL contract base URI");
      System.exit(-1);
    }
  }

  public Optional<EmbedCreateSpec> getSmolEmbed(String id) {
    StringBuilder output = new StringBuilder();
    int smolId;
    long smolLongId;

    try {
      smolId = Integer.parseInt(id);
      smolLongId = Long.parseLong(id);
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }

    boolean boarded = false;
    boolean boardedBeforeDeadline = false;
    Date boardingTime = null;

    try {
      final var boardingTimeEpoch =
          smolBrainsRocketContract.timestampJoined(new BigInteger(id)).send();

      boarded = !boardingTimeEpoch.equals(BigInteger.ZERO);
      boardingTime = new Date(boardingTimeEpoch.longValue() * 1000);

      // NOTE: Using the contract API for this; the deadline was adjusted in the contract recently
      boardedBeforeDeadline =
          smolBrainsRocketContract.boardedBeforeDeadline(new BigInteger(id)).send();
    } catch (Exception ex) {
      // no-op; execution failed means not boarded
    }

    output.append("IQ: ").append(treasureService.getIq(smolId)).append("\n");
    try {
      output.append("Currently boarded \uD83D\uDE80: ").append(boarded ? "Yes!" : "Nope.");

      if (boarded && boardingTime != null) {
        output
            .append(" Boarded at <t:")
            .append(boardingTime.getTime() / 1000)
            .append(":f>")
            .append(", ")
            .append(
                boardedBeforeDeadline
                    ? "before first snapshot deadline."
                    : "**after** first snapshot deadline.");
      }
    } catch (Exception e) {
      output.append("Nope.");
    }
    output.append("\n\n");

    List<Trait> traits = traitsRepository.findBySmol_Id(smolLongId);
    RarityRank rarityRank = rarityRankRepository.findBySmolId(smolLongId);

    Map<String, Double> percentages = new TreeMap<>();
    for (Trait trait : traits) {
      if (trait.getType().equalsIgnoreCase("head size")) {
        continue;
      }

      percentages.put(
          trait.getType() + " - " + trait.getValue(),
          getTraitRarity(trait.getType(), trait.getValue()));
    }

    final var sorted =
        percentages.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    for (Map.Entry<String, Double> entry : sorted) {
      String marker = "";
      if (entry.getValue() < 0.009d) {
        marker = " (Unique)";
      } else if (entry.getValue() < 0.65d) {
        marker = " (Ultra rare)";
      } else if (entry.getValue() < 0.8d) {
        marker = " (Rare)";
      }

      output
          .append(entry.getKey())
          .append(" ")
          .append(String.format("(%.3f%%)", entry.getValue()))
          .append(marker)
          .append("\n");
    }

    try {
      return Optional.of(
          EmbedCreateSpec.builder()
              .title("SMOL #" + id + "\nRANK: #" + rarityRank.getRank() + " (Unofficial)")
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
              .image(
                  getSmolImage(id, SmolType.SMOL, false)
                      .orElse("")) // Hardcoded to 0 brain size, for now
              .description(output.toString())
              .addField(
                  "Ranking Notes",
                  "Ranking is unofficial. Smols with unique traits are weighted the highest. Traits that occur < %0.65 are weighted 2nd highest, %0.80 third highest",
                  true)
              .build());
    } catch (Exception ex) {
      log.error("Error retrieving smol", ex);
    }

    return Optional.empty();
  }

  public Optional<EmbedCreateSpec> getSwolEmbed(String id) {
    StringBuilder output = new StringBuilder();
    int smolId;
    long smolLongId;

    try {
      smolId = Integer.parseInt(id);
      smolLongId = Long.parseLong(id);
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }

    output.append("Platez: ").append(treasureService.getPlatez(smolId)).append("\n\n");

    List<SmolBodyTrait> traits = smolBodyTraitsRepository.findBySmolBody_Id(smolLongId);
    SmolBodyRarityRank rarityRank = smolBodyRarityRankRepository.findBySmolBodyId(smolLongId);

    Map<String, Double> percentages = new TreeMap<>();
    for (SmolBodyTrait trait : traits) {
      if (trait.getType().equalsIgnoreCase("swol size")) {
        continue;
      }

      percentages.put(
          trait.getType() + " - " + trait.getValue(),
          getSmolBodyTraitRarity(trait.getType(), trait.getValue()));
    }

    final var sorted =
        percentages.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    for (Map.Entry<String, Double> entry : sorted) {
      String marker = "";
      if (entry.getValue() < 0.016d) {
        marker = " (Unique)";
      } else if (entry.getValue() < 0.21d) {
        marker = " (Ultra rare)";
      } else if (entry.getValue() < 0.30d) {
        marker = " (Rare)";
      }

      output
          .append(entry.getKey())
          .append(" ")
          .append(String.format("(%.3f%%)", entry.getValue()))
          .append(marker)
          .append("\n");
    }

    try {
      return Optional.of(
          EmbedCreateSpec.builder()
              .title("SMOLBODIES #" + id + "\nRANK: #" + rarityRank.getRank() + " (WIP)")
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmolbodies.94d441bb.png&w=1920&q=75")
              .image(
                  getSmolImage(id, SmolType.SMOL_BODY, true)
                      .orElse("")) // Hardcoded to 0 brain size, for now
              .description(output.toString())
              .addField(
                  "Ranking Notes",
                  "Ranking is unofficial. SmolBodies with unique traits are weighted the highest. Traits that occur < %2.10 are weighted 2nd highest, %3.00 third highest",
                  true)
              .build());
    } catch (Exception ex) {
      log.error("Error retrieving smol", ex);
    }

    return Optional.empty();
  }

  public Optional<EmbedCreateSpec> getCarEmbed(String id) {
    StringBuilder output = new StringBuilder();
    long smolLongId;

    try {
      smolLongId = Long.parseLong(id);
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }

    List<VroomTrait> traits = vroomTraitsRepository.findBySmol_Id(smolLongId);
    VroomRarityRank rarityRank = vroomRarityRankRepository.findBySmolId(smolLongId);

    Map<String, Double> percentages = new TreeMap<>();
    for (VroomTrait trait : traits) {
      percentages.put(
          trait.getType() + " - " + trait.getValue(),
          getVroomTraitRarity(trait.getType(), trait.getValue()));
    }

    final var sorted =
        percentages.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    for (Map.Entry<String, Double> entry : sorted) {
      String marker = "";

      if (entry.getValue() < 0.01d) {
        marker = " (Unique)";
      } else if (entry.getValue() < 1.50d) {
        marker = " (Ultra rare)";
      } else if (entry.getValue() < 2.50d) {
        marker = " (Rare)";
      }

      output
          .append(entry.getKey())
          .append(" ")
          .append(String.format("(%.3f%%)", entry.getValue()))
          .append(marker)
          .append("\n");
    }

    final var img = getCarImage(id);
    if (img.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(
          EmbedCreateSpec.builder()
              .title("VROOM #" + id + "\nRANK: # " + rarityRank.getRank() + " (WIP)")
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
              .image(img.get())
              .description(output.toString())
              .addField(
                  "Ranking Notes",
                  "Ranking is unofficial. Vrooms with unique traits are weighted the highest. Traits that occur < %1.50 are weighted 2nd highest, %2.50 third highest",
                  true)
              .build());
    } catch (Exception ex) {
      log.error("Error retrieving vroom", ex);
    }

    return Optional.empty();
  }

  public Optional<BufferedImage> getSmolBufferedImage(
      String id, SmolType smolType, boolean forceSmolBrain) {
    final var pathPiece =
        switch (smolType) {
          case SMOL -> "smols";
          case VROOM -> "vrooms";
          case SMOL_BODY -> "smol_body";
        };

    final int brainSize = forceSmolBrain ? 0 : getSmolBrainSize(id);
    final Path path = Paths.get("img_cache", pathPiece, id + "_" + brainSize + ".png");
    if (path.toFile().exists()) {
      // Read
      try {
        ByteArrayInputStream bytes = new ByteArrayInputStream(Files.readAllBytes(path));
        BufferedImage img = ImageIO.read(bytes);
        return Optional.of(img);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // Fetch and write
      final var imgOpt = getSmolImage(id, smolType, forceSmolBrain);
      if (imgOpt.isPresent()) {
        try {
          HttpClient httpClient = HttpClient.newHttpClient();
          HttpRequest request = HttpRequest.newBuilder().uri(new URI(imgOpt.get())).build();

          for (int retry = 0; retry <= 5; retry++) {
            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
              log.info("Writing new cached object: " + path + "; try: " + (retry + 1));
              Files.write(path, response.body());

              ByteArrayInputStream imgBytes = new ByteArrayInputStream(response.body());
              BufferedImage img = ImageIO.read(imgBytes);

              return Optional.of(img);
            } else {
              Thread.sleep(250);
              log.error("Unable to retrieve image (will retry): " + response.statusCode());
            }
          }
        } catch (Exception ex) {
          log.error("Error retrieving SMOL image");
        }
      }
    }

    return Optional.empty();
  }

  public Optional<String> getCarImage(String id) {
    try {
      String baseUri = smolBrainsVroomContract.baseURI().send();

      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(new URI(baseUri + id)).GET().build();
      final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return Optional.of(new JSONObject(response.body()).getString("image"));
    } catch (Exception ex) {
      log.error("Error retrieving car image", ex);
    }

    return Optional.empty();
  }

  public Integer getSmolBrainSize(String id) {
    try {
      final var tokenUri = this.smolBrainsContract.tokenURI(new BigInteger(id)).send();
      final var parts = tokenUri.split("/");
      return Integer.parseInt(parts[parts.length - 1]);
    } catch (Exception ex) {
      log.warn("Unable to retrieve head size", ex);
    }

    return 0;
  }

  // TODO: Get rid of this forceSmolBrains arg ASAP (adjust santa command)
  public Optional<String> getSmolImage(String id, SmolType smolType, boolean forceSmolBrain) {
    try {
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          switch (smolType) {
            case SMOL -> forceSmolBrain
                ? HttpRequest.newBuilder().uri(new URI(this.smolBaseUri + id + "/0")).GET().build()
                : HttpRequest.newBuilder()
                    .uri(new URI(this.smolBaseUri + id + "/" + getSmolBrainSize(id)))
                    .GET()
                    .build();
            case VROOM -> HttpRequest.newBuilder()
                .uri(new URI(this.vroomBaseUri + id))
                .GET()
                .build();
            case SMOL_BODY -> HttpRequest.newBuilder()
                .uri(new URI(this.smolBodyBaseUri + id + "/0"))
                .GET()
                .build();
          };

      if (request == null) {
        return Optional.empty();
      }

      final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return Optional.of(new JSONObject(response.body()).getString("image"));
    } catch (Exception ex) {
      log.error("Error retrieving smol image", ex);
    }

    return Optional.empty();
  }

  public double getSmolBodyTraitRarity(String type, String value) {
    long count = smolBodyTraitsRepository.countByTypeIgnoreCaseAndValueIgnoreCase(type, value);
    return ((double) count / (double) SMOL_BODY_TOTAL_SUPPLY) * 100.0d;
  }

  public double getVroomTraitRarity(String type, String value) {
    long count = vroomTraitsRepository.countByTypeIgnoreCaseAndValueIgnoreCase(type, value);
    return ((double) count / (double) SMOL_VROOM_TOTAL_SUPPLY) * 100.0d;
  }

  public double getTraitRarity(String type, String value) {
    long count = traitsRepository.countByTypeIgnoreCaseAndValueIgnoreCase(type, value);
    return ((double) count / (double) SMOL_TOTAL_SUPPLY) * 100.0d;
  }

  @Transactional
  public void generateBodyRanks() {
    Map<String, Map<String, Double>> rarityCache = new HashMap<>();
    Map<Long, Double> scores = new HashMap<>();

    Set<Long> knownRares = new HashSet<>();
    knownRares.add(97L);
    knownRares.add(888L);
    knownRares.add(1664L);
    knownRares.add(2375L);
    knownRares.add(3113L);
    knownRares.add(3553L);
    knownRares.add(4444L);
    knownRares.add(5126L);
    knownRares.add(5987L);
    knownRares.add(6331L);

    for (long x = 0; x <= SMOL_BODY_HIGHEST_ID; x++) {
      double currentScore = 0.0f;
      List<SmolBodyTrait> traits = smolBodyTraitsRepository.findBySmolBody_Id(x);
      for (SmolBodyTrait trait : traits) {
        Map<String, Double> rarity = rarityCache.get(trait.getType());

        if (rarity == null) {
          rarity = new HashMap<>();
        }

        if (!rarity.containsKey(trait.getValue())) {
          rarity.put(trait.getValue(), getSmolBodyTraitRarity(trait.getType(), trait.getValue()));

          rarityCache.put(trait.getType(), rarity);
        }

        // Apply weights for rare traits
        final var percentage = rarityCache.get(trait.getType()).get(trait.getValue());
        if (knownRares.contains(x)) {
          // Unique; heavy weight
          currentScore -= 300.0;
        } else if (percentage < 0.21d) {
          currentScore -= 150;
        } else if (percentage < 0.30d) {
          currentScore -= 100;
        }

        currentScore += rarityCache.get(trait.getType()).get(trait.getValue());
      }

      if (x % 100 == 0) {
        log.info("Gen #" + x);
      }

      scores.put(x, currentScore);
    }

    final var sorted = scores.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    double lastRankScore = sorted.get(0).getValue();
    int currentRank = 1;
    for (Map.Entry<Long, Double> longDoubleEntry : sorted) {
      double score = longDoubleEntry.getValue();
      if (lastRankScore != score) {
        currentRank++;
      }

      smolBodyRarityRankRepository.save(
          SmolBodyRarityRank.builder()
              .smolBodyId(longDoubleEntry.getKey())
              .rank(currentRank)
              .score(longDoubleEntry.getValue())
              .build());

      lastRankScore = longDoubleEntry.getValue();
    }

    log.info("Finished generating body ranks");
  }

  @Transactional
  public void generateVroomRanks() {
    Map<String, Map<String, Double>> rarityCache = new HashMap<>();
    Map<Long, Double> vroomScores = new HashMap<>();

    Set<Long> knownRares = new HashSet<>();
    knownRares.add(420L);

    for (long x = 1; x <= SMOL_VROOM_TOTAL_SUPPLY; x++) {
      double currentScore = 0.0f;
      List<VroomTrait> traits = vroomTraitsRepository.findBySmol_Id(x);
      for (VroomTrait trait : traits) {
        Map<String, Double> rarity = rarityCache.get(trait.getType());

        if (rarity == null) {
          rarity = new HashMap<>();
        }

        if (!rarity.containsKey(trait.getValue())) {
          rarity.put(trait.getValue(), getVroomTraitRarity(trait.getType(), trait.getValue()));

          rarityCache.put(trait.getType(), rarity);
        }

        // Apply weights for rare traits
        final var percentage = rarityCache.get(trait.getType()).get(trait.getValue());
        if (knownRares.contains(x)) {
          // Unique; heavy weight
          currentScore -= 300.0;
        } else if (percentage < 0.65d) {
          currentScore -= 150;
        } else if (percentage < 0.8d) {
          currentScore -= 100;
        }

        currentScore += rarityCache.get(trait.getType()).get(trait.getValue());
      }

      if (x % 100 == 0) {
        log.info("Gen #" + x);
      }

      vroomScores.put(x, currentScore);
    }

    final var sorted =
        vroomScores.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    double lastRankScore = sorted.get(0).getValue();
    int currentRank = 1;
    for (Map.Entry<Long, Double> longDoubleEntry : sorted) {
      double score = longDoubleEntry.getValue();
      if (lastRankScore != score) {
        currentRank++;
      }

      if (longDoubleEntry.getKey() % 100 == 0) {
        log.info("Commit #" + longDoubleEntry.getKey());
      }

      vroomRarityRankRepository.save(
          VroomRarityRank.builder()
              .smolId(longDoubleEntry.getKey())
              .rank(currentRank)
              .score(longDoubleEntry.getValue())
              .build());

      lastRankScore = longDoubleEntry.getValue();
    }

    log.info("Finished generating vroom ranks");
  }

  @Transactional
  void generateRanks() {
    Map<String, Map<String, Double>> rarityCache = new HashMap<>();
    Map<Long, Double> smolScores = new HashMap<>();

    Set<Long> knownUniques = new TreeSet<>();
    knownUniques.add(0L); // spacesuit
    knownUniques.add(1690L);
    knownUniques.add(4579L);
    knownUniques.add(5093L);
    knownUniques.add(2430L);
    knownUniques.add(3391L);
    knownUniques.add(5203L); // gold

    Set<Long> knownClearBrains = new HashSet<>();
    knownClearBrains.add(224L); // brain
    knownClearBrains.add(3232L); // brain

    for (long x = 0; x <= SMOL_HIGHEST_ID; x++) {
      double currentScore = 0.0f;
      List<Trait> traits = traitsRepository.findBySmol_Id(x);
      for (Trait trait : traits) {
        Map<String, Double> rarity = rarityCache.get(trait.getType());

        if (rarity == null) {
          rarity = new HashMap<>();
        }

        if (!rarity.containsKey(trait.getValue())) {
          rarity.put(trait.getValue(), getTraitRarity(trait.getType(), trait.getValue()));

          rarityCache.put(trait.getType(), rarity);
        }

        // Apply weights for rare traits
        final var percentage = rarityCache.get(trait.getType()).get(trait.getValue());
        if (percentage < 0.65d) {
          currentScore -= 150;
        } else if (percentage < 0.8d) {
          currentScore -= 100;
        }

        currentScore += rarityCache.get(trait.getType()).get(trait.getValue());
      }

      if (knownUniques.contains(x)) {
        currentScore = -100.0d;
      } else if (knownClearBrains.contains(x)) {
        currentScore = -99.0d;
      }

      if (x % 100 == 0) {
        log.info("Generated " + x);
      }

      smolScores.put(x, currentScore);
    }

    final var sorted = smolScores.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    double lastRankScore = sorted.get(0).getValue();
    int currentRank = 1;
    for (Map.Entry<Long, Double> longDoubleEntry : sorted) {
      double score = longDoubleEntry.getValue();
      if (lastRankScore != score) {
        currentRank++;
      }

      rarityRankRepository.save(
          RarityRank.builder()
              .smolId(longDoubleEntry.getKey())
              .rank(currentRank)
              .score(longDoubleEntry.getValue())
              .build());

      lastRankScore = longDoubleEntry.getValue();
    }

    log.info("Finished generating ranks");
  }

  public BufferedImage getTransparentImage(String tokenId) throws URISyntaxException, IOException {
    final var smolUri = new URI(getSmolImage(tokenId, SmolType.SMOL, false).orElse(""));

    var imageSmol = ImageIO.read(smolUri.toURL());

    final var transparentColor = imageSmol.getRGB(0, 0);
    ImageFilter imageFilter =
        new RGBImageFilter() {
          @Override
          public int filterRGB(int x, int y, int rgb) {
            if ((rgb | 0xFF000000) == transparentColor) {
              return 0x00FFFFFF & rgb;
            }

            return rgb;
          }
        };

    ImageProducer imageProducer = new FilteredImageSource(imageSmol.getSource(), imageFilter);
    Image imgSmol = Toolkit.getDefaultToolkit().createImage(imageProducer);

    BufferedImage transparentImg =
        new BufferedImage(
            imgSmol.getWidth(null), imgSmol.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = transparentImg.createGraphics();
    g2.drawImage(imgSmol, 0, 0, null);
    g2.dispose();

    return transparentImg;
  }

  public static String simpleTableToString(SimpleTable simpleTable) {
    GridTable gridTable = simpleTable.toGrid();
    gridTable = Border.of(Border.Chars.of('+', '-', '|')).apply(gridTable);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(baos);
    Util.print(gridTable, printStream);

    String response;
    response = baos.toString(StandardCharsets.UTF_8);

    return response;
  }

  public static Optional<String> getOptionString(ChatInputInteractionEvent event, String key) {
    final var option = event.getOption(key);
    if (option.isEmpty() || option.get().getValue().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(option.get().getValue().get().getRaw());
  }

  public static Optional<Long> getOptionLong(ChatInputInteractionEvent event, String key) {
    final var option = event.getOption(key);
    if (option.isEmpty() || option.get().getValue().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(option.get().getValue().get().asLong());
  }

  public static Optional<Boolean> getOptionBoolean(ChatInputInteractionEvent event, String key) {
    final var option = event.getOption(key);
    if (option.isEmpty() || option.get().getValue().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(option.get().getValue().get().asBoolean());
  }
}
