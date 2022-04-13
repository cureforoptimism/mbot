package com.cureforoptimism.mbot.discord.command;

import com.cureforoptimism.mbot.Utilities;
import com.cureforoptimism.mbot.domain.Smol;
import com.cureforoptimism.mbot.domain.SmolType;
import com.cureforoptimism.mbot.repository.SmolRepository;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BirthdaysCommand implements MbotCommand {
  private final SmolRepository smolRepository;
  private final Utilities utilities;
  private BufferedImage bgImage;
  private BufferedImage partyHat;

  public BirthdaysCommand(SmolRepository smolRepository, Utilities utilities) {
    this.smolRepository = smolRepository;
    this.utilities = utilities;

    try {
      this.bgImage = ImageIO.read(new ClassPathResource("birthday_backdrop.png").getInputStream());
      this.partyHat = ImageIO.read(new ClassPathResource("party-hat.png").getInputStream());
    } catch (Exception ex) {
      log.error(ex.getMessage());
      System.exit(-1);
    }
  }

  @Override
  public String getName() {
    return "birthdays";
  }

  @Override
  public String getDescription() {
    return "Use /birthdays";
  }

  @Override
  public String getUsage() {
    return null;
  }

  @Override
  public Mono<Message> handle(MessageCreateEvent event) {
    return null;
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    event.deferReply().block();

    final var startDate =
        Date.from(
            LocalDate.now()
                .withYear(2021)
                .atStartOfDay()
                .toInstant(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())));
    final var endDate =
        Date.from(
            LocalDate.now()
                .withYear(2021)
                .atStartOfDay()
                .plusDays(1)
                .minusSeconds(1)
                .toInstant(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())));

    Set<Smol> birthdaySmols = smolRepository.findByBirthdayIsBetween(startDate, endDate);

    final var ids = birthdaySmols.stream().map(smol -> smol.getId().toString()).sorted().toList();

    List<String> smolDescriptions = new ArrayList<>();
    final List<BufferedImage> smolImages = new ArrayList<>();
    birthdaySmols.stream()
        .forEach(
            s -> {
              smolDescriptions.add(s.getName() + " (#" + s.getId() + ")");
              var imgOpt =
                  utilities.getSmolBufferedImage(s.getId().toString(), SmolType.SMOL, true);
              imgOpt.ifPresent(smolImages::add);
            });

    List<BufferedImage> smolImagesTransparent = new ArrayList<>();
    for (BufferedImage smolImage : smolImages) {
      final var transparentColor = smolImage.getRGB(0, 0);
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

      ImageProducer imageProducer = new FilteredImageSource(smolImage.getSource(), imageFilter);
      smolImagesTransparent.add(
          imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(imageProducer)));
    }

    final var maxSmolWidths = (smolImagesTransparent.size() * 130) + 130;

    BufferedImage output = new BufferedImage(maxSmolWidths, 350, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = output.createGraphics();

    graphics.setComposite(AlphaComposite.SrcOver);
    for (int tileX = 0; tileX < maxSmolWidths; tileX += 404) {
      graphics.drawImage(Scalr.resize(bgImage, Mode.FIT_EXACT, 404), tileX, 0, null);
    }

    int xOffset = smolImages.size() * 130;
    for (BufferedImage smolImage : smolImagesTransparent) {
      xOffset -= 130;
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.drawImage(smolImage, xOffset - 40, 0, null);
      graphics.drawImage(partyHat, xOffset - 40, 0, null);
    }

    try {
      ByteArrayOutputStream smolOutputStream = new ByteArrayOutputStream();
      ImageIO.write(output, "png", smolOutputStream);

      EmbedCreateSpec embed =
          EmbedCreateSpec.builder()
              .title("Birthday Photo!")
              .description(
                  "Happy Birthday to the following Smols!\n\n"
                      + String.join(", ", smolDescriptions)
                      + "\n\nBrought to you by Commonopoly x Cure For Optimism")
              .image("attachment://birthdays.png")
              .build();

      event
          .createFollowup(
              InteractionFollowupCreateSpec.builder()
                  .addFile(
                      "birthdays.png", new ByteArrayInputStream(smolOutputStream.toByteArray()))
                  .addEmbed(embed)
                  .build())
          .block();
    } catch (Exception ex) {
      log.error(ex.toString());
    }

    return Mono.empty();
  }

  // TODO: This belongs somewhere common. Remove from here and FamilyCommand
  private static BufferedImage imageToBufferedImage(Image image) {
    BufferedImage bufferedImage =
        new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bufferedImage.createGraphics();
    g2.drawImage(image, 0, 0, null);
    g2.dispose();

    return bufferedImage;
  }

  @Override
  public Boolean adminOnly() {
    return false;
  }
}
