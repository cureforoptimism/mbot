package com.cureforoptimism.mbot;

import static com.cureforoptimism.mbot.Constants.SMOL_HIGHEST_ID;
import static com.cureforoptimism.mbot.Constants.SMOL_TOTAL_SUPPLY;

import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import com.cureforoptimism.mbot.service.TreasureService;
import com.smolbrains.SmolBrainsContract;
import discord4j.core.spec.EmbedCreateSpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class Utilities {
  private final TreasureService treasureService;
  private final RarityRankRepository rarityRankRepository;
  private final TraitsRepository traitsRepository;
  private final SmolBrainsContract smolBrainsContract;

  public Optional<EmbedCreateSpec> getSmolEmbed(String id) {
    StringBuilder output = new StringBuilder();
    output.append("IQ: ").append(treasureService.getIq(Integer.parseInt(id))).append("\n\n");

    List<Trait> traits = traitsRepository.findBySmol_Id(Long.parseLong(id));
    RarityRank rarityRank = rarityRankRepository.findBySmolId(Long.parseLong(id));

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
      String baseUri = smolBrainsContract.baseURI().send();

      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder().uri(new URI(baseUri + id + "/0")).GET().build();
      final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      final var obj = new JSONObject(response.body()).getString("image");
      return Optional.of(
          EmbedCreateSpec.builder()
              .title("SMOL #" + id + "\nRANK: #" + rarityRank.getRank() + " (WIP)")
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
              .image(obj) // Hardcoded to 0 brain size, for now
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

  public double getTraitRarity(String type, String value) {
    long count = traitsRepository.countByTypeIgnoreCaseAndValueIgnoreCase(type, value);
    return ((double) count / (double) SMOL_TOTAL_SUPPLY) * 100.0d;
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
}
