package com.cureforoptimism.mbot.application;

import com.cureforoptimism.mbot.discord.events.RefreshEvent;
import com.cureforoptimism.mbot.domain.RarityRank;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.repository.RarityRankRepository;
import com.cureforoptimism.mbot.repository.SmolRepository;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import com.cureforoptimism.mbot.service.TokenService;
import com.cureforoptimism.mbot.service.TreasureService;
import com.smolbrains.SmolBrainsContract;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static com.cureforoptimism.mbot.Constants.SMOL_HIGHEST_ID;
import static com.cureforoptimism.mbot.Constants.SMOL_TOTAL_SUPPLY;

@Component
@Slf4j
public class DiscordBot {
  final Web3j web3j;
  final ApplicationContext context;
  final GatewayDiscordClient client;
  final TokenService tokenService;
  final TreasureService treasureService;
  final TraitsRepository traitsRepository;
  final SmolRepository smolRepository;
  final SmolBrainsContract smolBrainsContract;
  final RarityRankRepository rarityRankRepository;
  Double currentPrice;
  Double currentChange;

  public DiscordBot(
      ApplicationContext context,
      TokenService tokenService,
      TreasureService treasureService,
      Web3j web3j,
      TraitsRepository traitsRepository,
      SmolRepository smolRepository,
      SmolBrainsContract smolBrainsContract,
      RarityRankRepository rarityRankRepository) {
    this.context = context;
    this.tokenService = tokenService;
    this.treasureService = treasureService;
    this.web3j = web3j;
    this.traitsRepository = traitsRepository;
    this.smolRepository = smolRepository;
    this.smolBrainsContract = smolBrainsContract;
    this.rarityRankRepository = rarityRankRepository;
    this.client =
        DiscordClientBuilder.create(tokenService.getDiscordToken()).build().login().block();

    if (client != null) {
      client
          .getEventDispatcher()
          .on(RefreshEvent.class)
          .subscribe(
              event -> {
                String nickName = ("MAGIC $" + currentPrice);
                String presence = String.format("24h: %.2f%%", currentChange);
                client.getGuilds().toStream().forEach(g -> g.changeSelfNickname(nickName).block());
                client
                    .updatePresence(ClientPresence.online(ClientActivity.watching(presence)))
                    .block();
              });
      client
          .getEventDispatcher()
          .on(MessageCreateEvent.class)
          .subscribe(
              e -> {
                if (e.getMessage().getContent().toLowerCase().startsWith("!rank")) {
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2) {
                    log.info("!rank command received");
                    String rankNumber = parts[1].trim();

                    List<RarityRank> ranks =
                        rarityRankRepository.findByRank(Integer.parseInt(rankNumber));
                    if (ranks.size() == 1) {
                      RarityRank rank = ranks.get(0);
                      printSmol(e, rank.getSmolId().toString());
                    } else if (ranks.size() >= 2) {
                      StringBuilder msg = new StringBuilder();
                      msg.append("There are multiple Smols with rank ")
                          .append(rankNumber)
                          .append(": ");
                      for (RarityRank rarityRank : ranks) {
                        msg.append(rarityRank.getSmolId()).append(" ");
                      }
                      e.getMessage()
                          .getChannel()
                          .flatMap(c -> c.createMessage(msg.toString()))
                          .block();
                    }
                  }
                } else if (e.getMessage().getContent().toLowerCase().startsWith("!smolhelp")) {
                  String helpMessage =
                      """
                          `!smol <token_id>` - shows your smol, rank, picture, and smol traits/rarities
                          `!averageiq` - shows the average Smol IQ across the Smoliverse
                          `!floor` - Shows the current floor price of Smols on the Treasure marketplace
                          `!pfp <token_id>` - Creates an animated gif of a Smol's brain growing (optional: try `!pfp <token_id> reverse`)
                          `!traits` - List all top level traits
                          `!traits <type>` - List all possible trait values, and their rarities
                          `!top20` - List the top 20 ranked Smols
                          `!rank <rank_number>` - Show the smol(s) at specified rank number
                          """;

                  final var msg =
                      EmbedCreateSpec.builder()
                          .title("SmolBot Help")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .description(helpMessage)
                          .addField(
                              "Note: ",
                              "This bot developed for fun by `Cure For Optimism#5061`, and is unofficial. Smol admins aren't associated with this bot and can't help you with issues. Ping smol cureForOptimism with any feedback/questions",
                              false)
                          .build();
                  e.getMessage().getChannel().flatMap(c -> c.createMessage(msg)).block();
                } else if (e.getMessage().getContent().toLowerCase().startsWith("!top20")) {
                  StringBuilder ranks = new StringBuilder("```");
                  Set<RarityRank> rarities = rarityRankRepository.findTop20();
                  for (RarityRank rarityRank : rarities) {
                    ranks
                        .append(rarityRank.getRank())
                        .append(": #")
                        .append(rarityRank.getSmolId())
                        .append("\n");
                  }
                  ranks.append("```");

                  final var msg =
                      EmbedCreateSpec.builder()
                          .title("Top 20 Smols")
                          .author(
                              "SmolBot",
                              null,
                              "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
                          .description(ranks.toString())
                          .build();
                  e.getMessage().getChannel().flatMap(c -> c.createMessage(msg)).block();
                } else if (e.getMessage().getContent().toLowerCase().startsWith("!averageiq")) {
                  e.getMessage()
                      .getChannel()
                      .flatMap(
                          c -> {
                            try {
                              BigInteger averageIqBig = smolBrainsContract.averageIQ().send();
                              MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
                              final var iq = new BigDecimal(averageIqBig, 18, mc);

                              return c.createMessage("Average IQ is currently: " + iq);
                            } catch (Exception ignored) {
                              return Mono.empty();
                            }
                          })
                      .block();
                } else if (e.getMessage().getContent().startsWith("!floor")) {
                  log.info("!floor command received");
                  final var magicFloor = treasureService.getFloor();
                  final var usdFloor = magicFloor.multiply(BigDecimal.valueOf(currentPrice));
                  final var landFloor = treasureService.getLandFloor();
                  final var usdLandFloor = landFloor.multiply(BigDecimal.valueOf(currentPrice));
                  final var totalLandListings = treasureService.getTotalFloorListings();
                  final var totalListings = treasureService.getTotalListings();
                  final var cheapestMale = treasureService.getCheapestMale();
                  final var usdCheapestMale =
                      cheapestMale.multiply(BigDecimal.valueOf(currentPrice));
                  final var cheapestFemale = treasureService.getCheapestFemale();
                  final var usdCheapestFemale =
                      cheapestFemale.multiply(BigDecimal.valueOf(currentPrice));
                  final var cheapestMaleId = treasureService.getCheapestMaleId();
                  final var cheapestFemaleId = treasureService.getCheapestFemaleId();
                  final var cheapestPair = cheapestFemale.add(cheapestMale);
                  final var usdCheapestPair =
                      cheapestPair.multiply(BigDecimal.valueOf(currentPrice));
                  final var output =
                      String.format(
                          "SMOL           - MAGIC: %.2f ($%.2f). Total listings: %d.\nSMOL Land - MAGIC: %.2f ($%.2f). Total listings: %d.\nCheapest male (#%d)       - MAGIC: %.2f ($%.2f).\nCheapest female (#%d)   - MAGIC: %.2f ($%.2f).\nCheapest pair                          - MAGIC: %.2f ($%.2f).",
                          magicFloor,
                          usdFloor,
                          totalListings,
                          landFloor,
                          usdLandFloor,
                          totalLandListings,
                          cheapestMaleId,
                          cheapestMale,
                          usdCheapestMale,
                          cheapestFemaleId,
                          cheapestFemale,
                          usdCheapestFemale,
                          cheapestPair,
                          usdCheapestPair);

                  e.getMessage().getChannel().flatMap(c -> c.createMessage(output)).block();
                } else if (e.getMessage().getContent().startsWith("!iq")) {
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2) {
                    log.info("!iq command received");
                    String tokenId = parts[1];
                    BigDecimal iq = treasureService.getIq(Integer.parseInt(tokenId));
                    String output = tokenId + " has an IQ of " + iq.toString();
                    e.getMessage().getChannel().flatMap(c -> c.createMessage(output)).block();
                  }
                } else if (e.getMessage().getContent().startsWith("!pfp")) {
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2 || parts.length == 3) {
                    String tokenId = parts[1];
                    log.info("!pfp command received");
                    boolean reverse = parts.length != 2;

                    final var image = treasureService.getAnimatedGif(tokenId, reverse);
                    if (image == null) {
                      e.getMessage()
                          .getChannel()
                          .flatMap(c -> c.createMessage("I can't find a token with ID " + tokenId))
                          .block();
                      return;
                    }

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image);
                    e.getMessage()
                        .getChannel()
                        .flatMap(
                            c ->
                                c.createMessage(
                                    MessageCreateSpec.builder()
                                        .addFile(tokenId + ".gif", byteArrayInputStream)
                                        .build()))
                        .block();
                  }
                } else if (e.getMessage().getContent().startsWith("!traits")) {
                  log.info("!traits called");
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2) {
                    final var values =
                        traitsRepository.findDistinctByTypeIgnoreCaseOrderByValueAsc(
                            parts[1].trim());
                    if (!values.isEmpty()) {
                      StringBuilder output = new StringBuilder();
                      Map<String, Double> percentages = new HashMap<>();

                      for (String value : values) {
                        percentages.put(value, (getTraitRarity(parts[1].trim(), value)));
                      }

                      final var sorted =
                          percentages.entrySet().stream()
                              .sorted(Map.Entry.comparingByValue())
                              .toList();
                      for (Map.Entry<String, Double> entry : sorted) {
                        output
                            .append(entry.getKey())
                            .append(": ")
                            .append(String.format("(%.3f%%)", entry.getValue()))
                            .append("\n");
                      }

                      String finalOutput = output.toString();
                      e.getMessage()
                          .getChannel()
                          .flatMap(c -> c.createMessage(finalOutput))
                          .block();
                    } else {
                      e.getMessage()
                          .getChannel()
                          .flatMap(
                              c ->
                                  c.createMessage(
                                      "I couldn't find that trait: " + parts[1].trim()));
                    }
                  } else {
                    final var types = traitsRepository.findDistinctTraits();
                    StringBuilder output = new StringBuilder();
                    for (String trait : types) {
                      output.append(trait).append("\n");
                    }

                    String finalOutput = output.toString();
                    e.getMessage().getChannel().flatMap(c -> c.createMessage(finalOutput)).block();
                  }
                } else if (e.getMessage().getContent().startsWith("!trait")) {
                  log.info("!trait called");
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 3) {
                    String trait = parts[1].trim();
                    String value = parts[2].trim();
                    long count = treasureService.getTraitsCount(trait, value);
                    e.getMessage()
                        .getChannel()
                        .flatMap(
                            c -> c.createMessage(count + " smols have " + trait + " of " + value))
                        .block();
                  }
                  // Uncomment this if you ever need to refresh traits; otherwise, yeah, this is a
                  // super heavy op
                  //                  }
                  //                                    } else if
                  //                   (e.getMessage().getContent().startsWith("!rarities")) {
                  //                                      treasureService.getAllRarities();
                } else if (e.getMessage().getContent().startsWith("!smol")) {
                  log.info("!smol called");
                  String[] parts = e.getMessage().getContent().split(" ");

                  if (parts.length == 2) {
                    String id = parts[1].trim();

                    try {
                      printSmol(e, id);
                    } catch (Exception ignored) {
                    }
                  }
                }
                // Uncomment to enable re-generating ranks
                //                else if(e.getMessage().getContent().equalsIgnoreCase("!genrank"))
                // {
                //                  generateRanks();
                //                }
              });
    }
  }

  @Transactional
  private void generateRanks() {
    Map<String, Map<String, Double>> rarityCache = new HashMap<>();
    Map<Long, Double> smolScores = new HashMap<>();

    Set<Long> knownRares = new HashSet<>();
    knownRares.add(0L);
    knownRares.add(1690L);
    knownRares.add(4579L);
    knownRares.add(5093L);

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
          // Unique; heavy weight
          currentScore -= 300.0;
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

  private double getTraitRarity(String type, String value) {
    long count = traitsRepository.countByTypeIgnoreCaseAndValueIgnoreCase(type, value);
    return ((double) count / (double) SMOL_TOTAL_SUPPLY) * 100.0d;
  }

  public void printSmol(MessageCreateEvent e, String id) {
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
      final var msg =
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
              .build();

      e.getMessage().getChannel().flatMap(c -> c.createMessage(msg)).block();
    } catch (Exception ignored) {
    }
  }

  public void refreshMagicPrice(Double price, Double usd24HChange) {
    currentPrice = price;
    currentChange = usd24HChange;
    client.getEventDispatcher().publish(new RefreshEvent(null, null));
  }
}
