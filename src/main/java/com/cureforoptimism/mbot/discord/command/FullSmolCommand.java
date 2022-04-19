package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.domain.Trait;
import com.cureforoptimism.mbot.repository.TraitsRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.awt.AlphaComposite;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FullSmolCommand implements MbotCommand {
  private final TraitsRepository traitsRepository;
  private final Utilities utilities;
  private final Map<String, BufferedImage> bodies;

  private static class FullSmolResponse {
    private byte[] image;
    private EmbedCreateSpec embed;

    public FullSmolResponse(byte[] image, EmbedCreateSpec embed) {
      this.image = image;
      this.embed = embed;
    }
  }

  public FullSmolCommand(TraitsRepository traitsRepository, Utilities utilities) {
    this.traitsRepository = traitsRepository;
    this.utilities = utilities;

    bodies = new HashMap<>();

    try {
      bodies.put(
          "FEMALE_DARK_BROWN",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/DARK-BROWN.PNG")
                  .getInputStream()));
      bodies.put(
          "FEMALE_ORANGE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/RED.PNG")
                  .getInputStream()));
      bodies.put(
          "FEMALE_GREEN",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/GREEN.PNG").getInputStream()));
      bodies.put(
          "FEMALE_BROWN",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/LIGHT-BROWN.PNG")
                  .getInputStream()));
      bodies.put(
          "FEMALE_OLIVE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/OLIVE.PNG").getInputStream()));
      bodies.put(
          "FEMALE_PURPLE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/PURPLE.PNG").getInputStream()));
      bodies.put(
          "FEMALE_RED",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_body/RED.PNG").getInputStream()));

      bodies.put(
          "FEMALE_FANCY-DRESS",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_clothed/FANCY-DRESS.PNG")
                  .getInputStream()));
      bodies.put(
          "FEMALE_JACKET",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_clothed/JACKET.PNG")
                  .getInputStream()));
      bodies.put(
          "FEMALE_STRAPLESS-DRESS",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_clothed/STRAPLESS-DRESS.PNG")
                  .getInputStream()));
      bodies.put(
          "FEMALE_SUIT",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_clothed/SUIT.PNG").getInputStream()));
      bodies.put(
          "FEMALE_SWEATER",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/female_clothed/SWEATER.PNG")
                  .getInputStream()));

      bodies.put(
          "MALE_BLACK",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/BLACK.PNG").getInputStream()));
      bodies.put(
          "MALE_BLUE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/BLUE.PNG").getInputStream()));
      bodies.put(
          "MALE_BROWN",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/BROWN.PNG").getInputStream()));
      bodies.put(
          "MALE_DARK_BROWN",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/DARK-BROWN.PNG").getInputStream()));
      bodies.put(
          "MALE_LIGHT_ORANGE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/LIGHT-ORANGE.PNG")
                  .getInputStream()));
      bodies.put(
          "MALE_LIME",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/LIME.PNG").getInputStream()));
      bodies.put(
          "MALE_OLIVE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/OLIVE.PNG").getInputStream()));
      bodies.put(
          "MALE_ORANGE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/ORANGE.PNG").getInputStream()));
      bodies.put(
          "MALE_PINK",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/PINK.PNG").getInputStream()));
      bodies.put(
          "MALE_PURPLE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_body/PURPLE.PNG").getInputStream()));

      bodies.put(
          "MALE_CYBORG",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_clothed/CYBORG.PNG").getInputStream()));
      bodies.put(
          "MALE_FANCY-DRESS",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_clothed/FANCY-DRESS.PNG")
                  .getInputStream()));
      bodies.put(
          "MALE_JACKET",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_clothed/JACKET.PNG").getInputStream()));
      bodies.put(
          "MALE_PIRATE",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_clothed/PIRATE.PNG").getInputStream()));
      bodies.put(
          "MALE_PRISONER",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_clothed/PRISONER.PNG")
                  .getInputStream()));
      bodies.put(
          "MALE_SUIT",
          ImageIO.read(
              new ClassPathResource("smol_brains_body/male_clothed/SUIT.PNG").getInputStream()));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "fullsmol";
  }

  @Override
  public String getDescription() {
    return "Smols have brains AND bodies?!?";
  }

  @Override
  public String getUsage() {
    return "<token_id>";
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    log.info("!fullsmol command received");

    String msg = event.getMessage().getContent();
    String[] parts = msg.split(" ");

    if (parts.length < 2) {
      return Mono.empty();
    }

    String tokenId = parts[1];

    final var response = getBodyEmbed(tokenId);
    return event
        .getMessage()
        .getChannel()
        .flatMap(
            c ->
                c.createMessage(
                    MessageCreateSpec.builder()
                        .addEmbed(response.embed)
                        .addFile(tokenId + "body.png", new ByteArrayInputStream(response.image))
                        .build()));
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("/fullsmol command received");

    event.deferReply().block();

    final var tokenId = event.getOption("id").orElse(null);
    if (tokenId == null) {
      return null;
    }

    if (tokenId.getValue().isEmpty()) {
      return Mono.empty();
    }

    final var tokenIdStrOpt = tokenId.getValue().get();

    event.createFollowup(getBodyEmbedFollowup(tokenIdStrOpt.getRaw())).block();

    return Mono.empty();
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }

  private InteractionFollowupCreateSpec getBodyEmbedFollowup(String tokenId) {
    final var fullSmolResponse = getBodyEmbed(tokenId);

    return InteractionFollowupCreateSpec.builder()
        .addFile(tokenId + "body.png", new ByteArrayInputStream(fullSmolResponse.image))
        .addEmbed(fullSmolResponse.embed)
        .build();
  }

  private FullSmolResponse getBodyEmbed(String tokenId) {
    List<Trait> traits = traitsRepository.findBySmol_Id(Long.parseLong(tokenId));

    String gender = null;
    String color = null;
    String clothes = null;
    for (Trait trait : traits) {
      if (trait.getType().equalsIgnoreCase("gender")) {
        gender = trait.getValue().toUpperCase();
      } else if (trait.getType().equalsIgnoreCase("body")) {
        color = trait.getValue().toUpperCase();
      } else if (trait.getType().equalsIgnoreCase("clothes")
          && !trait.getValue().equalsIgnoreCase("none")) {
        clothes = trait.getValue().toUpperCase();
      }
    }

    String key = gender + "_" + color;
    String clothesKey = clothes == null ? null : gender + "_" + clothes;

    try {
      final var imageSmolOpt = utilities.getSmolBufferedImage(tokenId, SmolType.SMOL, true);
      if (imageSmolOpt.isEmpty()) {
        log.warn("Could not retrieve smol " + tokenId);
        return null;
      }

      final var imageSmol = imageSmolOpt.get();
      final var bgColor = imageSmol.getRGB(0, 0);

      BufferedImage body = bodies.get(key);
      ImageFilter imageFilter =
          new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
              if ((rgb >> 24) == 0x00) {
                return bgColor;
              }

              return rgb;
            }
          };

      ImageProducer imageProducer = new FilteredImageSource(body.getSource(), imageFilter);
      Image bodyImg = Toolkit.getDefaultToolkit().createImage(imageProducer);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BufferedImage output = new BufferedImage(body.getWidth(), 622, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = output.createGraphics();
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.drawImage(bodyImg, 0, -148, null);
      graphics.drawImage(imageSmol, 0, -80, null);
      if (clothesKey != null) {
        graphics.drawImage(bodies.get(clothesKey), 0, -148, null);
      }

      graphics.dispose();

      try {
        ImageIO.write(output, "png", outputStream);
      } catch (IOException ex) {
        log.warn("Unable to write PNG", ex);
      }
      final var embed =
          EmbedCreateSpec.builder()
              .title("Free body, good vibes! #" + tokenId + "!")
              .image("attachment://" + tokenId + "body.png")
              .footer("Body rocking! From commonopoly x Cure For Optimism", null)
              .build();

      return new FullSmolResponse(outputStream.toByteArray(), embed);
    } catch (Exception ex) {
      log.warn("Error rendering full smol", ex);
      return null;
    }
  }
}
