package com.cureforoptimism.mbot;

import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.domain.VroomRarityRank;
import com.cureforoptimism.mbot.domain.VroomTrait;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import com.cureforoptimism.mbot.repository.VroomRarityRankRepository;
import com.cureforoptimism.mbot.repository.VroomTraitsRepository;
import com.cureforoptimism.mbot.service.TreasureService;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.SimpleTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsVroomContract;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.cureforoptimism.mbot.Constants.*;

@Component
@AllArgsConstructor
@Slf4j
public class Utilities {
  private final TreasureService treasureService;
  private final RarityRankRepository rarityRankRepository;
  private final TraitsRepository traitsRepository;
  private final VroomTraitsRepository vroomTraitsRepository;
  private final SmolBrainsContract smolBrainsContract;
  private final SmolBrainsVroomContract smolBrainsVroomContract;
  private final VroomRarityRankRepository vroomRarityRankRepository;

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

    output.append("IQ: ").append(treasureService.getIq(smolId)).append("\n\n");

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
              .title("SMOL #" + id + "\nRANK: #" + rarityRank.getRank() + " (WIP)")
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
              .image(getSmolImage(id).orElse("")) // Hardcoded to 0 brain size, for now
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

  public Optional<String> getSmolImage(String id) {
    try {
      String baseUri = smolBrainsContract.baseURI().send();

      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder().uri(new URI(baseUri + id + "/0")).GET().build();
      final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return Optional.of(new JSONObject(response.body()).getString("image"));
    } catch (Exception ex) {
      log.error("Error retrieving smol image", ex);
    }

    return Optional.empty();
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
  private void generateRanks() {
    Map<String, Map<String, Double>> rarityCache = new HashMap<>();
    Map<Long, Double> smolScores = new HashMap<>();

    Set<Long> knownRares = new HashSet<>();
    knownRares.add(0L); // spacesuit
    knownRares.add(224L);
    knownRares.add(1690L);
    knownRares.add(4579L);
    knownRares.add(5093L);
    knownRares.add(2430L);
    knownRares.add(3232L); // brain
    knownRares.add(3391L);
    knownRares.add(5203L); // gold

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
        if (knownRares.contains(x)) {
          if (x == 0) {
            currentScore -= 400.0;
          } else {
            // Unique; heavy weight
            currentScore -= 300.0;
          }
        } else if (percentage < 0.65d) {
          currentScore -= 150;
        } else if (percentage < 0.8d) {
          currentScore -= 100;
        }

        currentScore += rarityCache.get(trait.getType()).get(trait.getValue());
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
}
