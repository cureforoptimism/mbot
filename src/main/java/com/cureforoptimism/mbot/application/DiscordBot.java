package com.cureforoptimism.mbot.application;

import com.cureforoptimism.mbot.discord.events.RefreshEvent;
import com.cureforoptimism.mbot.domain.Trait;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
  Double currentPrice;
  Double currentChange;

  public DiscordBot(
      ApplicationContext context,
      TokenService tokenService,
      TreasureService treasureService,
      Web3j web3j,
      TraitsRepository traitsRepository,
      SmolRepository smolRepository,
      SmolBrainsContract smolBrainsContract) {
    this.context = context;
    this.tokenService = tokenService;
    this.treasureService = treasureService;
    this.web3j = web3j;
    this.traitsRepository = traitsRepository;
    this.smolRepository = smolRepository;
    this.smolBrainsContract = smolBrainsContract;
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
                if (e.getMessage().getContent().toLowerCase().startsWith("!averageiq")) {
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
                        traitsRepository.findDistinctByTypeOrderByValueAsc(parts[1].trim());
                    if (!values.isEmpty()) {
                      StringBuilder output = new StringBuilder();
                      Map<String, Float> percentages = new HashMap<>();

                      for (String value : values) {
                        percentages.put(value, (getTraitRarity(parts[1].trim(), value)));
                      }

                      final var sorted =
                          percentages.entrySet().stream()
                              .sorted(Map.Entry.comparingByValue())
                              .toList();
                      for (Map.Entry<String, Float> entry : sorted) {
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
                    // Uncomment this if you ever need to refresh traits; otherwise, yeah, this is a
                    // super heavy op
                  }
                  //                  } else if
                  // (e.getMessage().getContent().startsWith("!rarities")) {
                  //                    treasureService.getAllRarities();
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
              });
    }
  }

  private float getTraitRarity(String type, String value) {
    int totalSmols = 10363; // TODO: Just make this a global somewhere, retrieved from db
    long count = traitsRepository.countByTypeIgnoreCaseAndValueIgnoreCase(type, value);
    return ((float) count / (float) totalSmols) * 100.0f;
  }

  public void printSmol(MessageCreateEvent e, String id) {
    StringBuilder output = new StringBuilder();
    output.append("IQ: ").append(treasureService.getIq(Integer.parseInt(id))).append("\n\n");

    List<Trait> traits = traitsRepository.findBySmol_Id(Long.parseLong(id));

    Map<String, Float> percentages = new TreeMap<>();
    for (Trait trait : traits) {
      percentages.put(
          trait.getType() + " - " + trait.getValue(),
          getTraitRarity(trait.getType(), trait.getValue()));
    }

    final var sorted =
        percentages.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    for (Map.Entry<String, Float> entry : sorted) {
      output
          .append(entry.getKey())
          .append(" ")
          .append(String.format("(%.3f%%)", entry.getValue()))
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
              .title("SMOL #" + id)
              .author(
                  "SmolBot",
                  null,
                  "https://www.smolverse.lol/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fsmol-brain-monkey.b82c9b83.png&w=64&q=75")
              .image(obj) // Hardcoded to 0 brain size, for now
              .description(output.toString())
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
