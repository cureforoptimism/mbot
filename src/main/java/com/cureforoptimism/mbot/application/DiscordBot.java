package com.cureforoptimism.mbot.application;

import static com.cureforoptimism.mbot.Constants.SMOL_GUILD_ID;

import com.cureforoptimism.mbot.discord.events.RefreshEvent;
import com.cureforoptimism.mbot.discord.listener.MbotCommandListener;
import com.cureforoptimism.mbot.service.TokenService;
import discord4j.common.JacksonResources;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.service.ApplicationService;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DiscordBot implements ApplicationRunner {
  final ApplicationContext context;
  static GatewayDiscordClient client;
  final TokenService tokenService;

  // TODO: This sucks. Makes this suck less with a rational pattern.
  @Getter Double currentPrice;
  @Getter Double currentChange;
  @Getter Double currentChange12h;
  @Getter Double currentChange4h;
  @Getter Double currentChange1h;
  @Getter Double currentVolume24h;
  @Getter Double currentVolume12h;
  @Getter Double currentVolume4h;
  @Getter Double currentVolume1h;

  public DiscordBot(ApplicationContext context, TokenService tokenService) {
    this.context = context;
    this.tokenService = tokenService;
  }

  public void refreshMagicPrice(
      Double price,
      Double usd24HChange,
      Double change12h,
      Double change4h,
      Double change1h,
      Double volume24h,
      Double volume12h,
      Double volume4h,
      Double volume1h) {
    currentPrice = price;
    currentChange = usd24HChange;
    currentChange12h = change12h;
    currentChange4h = change4h;
    currentChange1h = change1h;
    currentVolume24h = volume24h;
    currentVolume12h = volume12h;
    currentVolume4h = volume4h;
    currentVolume1h = volume1h;

    if (client != null) {
      client.getEventDispatcher().publish(new RefreshEvent(null, null));
    }
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    client =
        DiscordClientBuilder.create(tokenService.getDiscordToken())
            .build()
            .gateway()
            .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES))
            .login()
            .block();

    //    dumpOgRoleIDs();

    if (client == null) {
      log.error("Unable to create Discord client");
      System.exit(-1);
    }

    final JacksonResources mapper = JacksonResources.create();
    PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
    final ApplicationService applicationService = client.rest().getApplicationService();
    final long applicationId = client.rest().getApplicationId().block();

    // Already registered commands...
    Map<String, ApplicationCommandData> slashCommands =
        applicationService
            .getGlobalApplicationCommands(applicationId)
            .collectMap(ApplicationCommandData::name)
            .block();

    Map<String, ApplicationCommandRequest> jsonCommands = new HashMap<>();

    // Add new commands
    for (Resource resource : matcher.getResources("commands/*.json")) {
      ApplicationCommandRequest request =
          mapper
              .getObjectMapper()
              .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

      jsonCommands.put(request.name(), request);

      if (!slashCommands.containsKey(request.name())) {
        applicationService.createGlobalApplicationCommand(applicationId, request).block();
        log.info("Created new global command: " + request.name());
      }
    }

    // Delete old/unused commands
    for (ApplicationCommandData command : slashCommands.values()) {
      long commandId = Long.parseLong(command.id());

      ApplicationCommandRequest request = jsonCommands.get(command.name());

      if (request == null) {
        applicationService.deleteGlobalApplicationCommand(applicationId, commandId).block();

        log.info("Deleted global command: " + command.name());
        continue;
      }

      if (hasChanged(command, request)) {
        applicationService
            .modifyGlobalApplicationCommand(applicationId, commandId, request)
            .block();

        log.info("Modified global command: " + request.name());
      }
    }

    MbotCommandListener mbotCommandListener = new MbotCommandListener(context, this);

    client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(mbotCommandListener::handle);
    client
        .getEventDispatcher()
        .on(ChatInputInteractionEvent.class)
        .subscribe(mbotCommandListener::handle);

    client
        .on(RefreshEvent.class)
        .subscribe(
            event -> {
              try {
                String posNeg = currentChange >= 0.0 ? "\uD83C\uDF4C" : "\uD83C\uDF46";
                String nickName = ("MAGIC $" + currentPrice + " " + posNeg);
                String presence = String.format("24h: %.2f%%", currentChange);
                client
                    .getGuilds()
                    .toStream()
                    .forEach(
                        g -> {
                          try {
                            g.changeSelfNickname(nickName).block();
                          } catch (Exception ex) {
                            log.warn(
                                "Unable to change nickname for server: "
                                    + g.getId()
                                    + "; will try again");
                          }
                        });
                client
                    .updatePresence(ClientPresence.online(ClientActivity.watching(presence)))
                    .block();
              } catch (Exception ex) {
                log.warn("Unable to change nickname: " + ex);
              }
            });

    // Update avatar (we don't need to do this very often, so leave it commented out)
    //    try {
    //      final var bytes = new ClassPathResource("smol_bot.png").getInputStream();
    //      client
    //          .edit(
    //              UserEditSpec.builder()
    //                  .avatar(discord4j.rest.util.Image.ofRaw(bytes.readAllBytes(), Format.PNG))
    //                  .build())
    //          .block();
    //    } catch (Exception ex) {
    //      log.error(ex.getMessage());
    //    }

    log.info("Discord client logged in");
  }

  public void postMessage(MessageCreateSpec messageCreateSpec, List<Long> discordChannelIds) {
    for (Long discordChannelId : discordChannelIds) {
      try {
        final var messages =
            client
                .getChannelById(Snowflake.of(discordChannelId))
                .ofType(MessageChannel.class)
                .flatMap(c -> c.createMessage(messageCreateSpec));

        messages.block();
      } catch (Exception ex) {
        log.warn("Unable to post to channel: " + discordChannelId);
      }
    }
  }

  private void dumpOgRoleIDs() {
    final var fromGuild = client.getGuildById(Snowflake.of(SMOL_GUILD_ID)).block();
    final var membersAll = fromGuild.getMembers().collectList().block();

    Set<String> members = new HashSet<>();
    for (Member member : membersAll) {
      if (member.getRoleIds().contains(Snowflake.of(899175139546116147L))) {
        members.add(member.getId().asString());
      }
    }

    log.info("OG ID's: " + members.stream().collect(Collectors.joining("\n")));
  }

  public int migrateRoles(Long fromGuildId, Long toGuildId, Long fromRole, Long toRole) {
    final var fromGuild = client.getGuildById(Snowflake.of(fromGuildId)).block();
    final var fromMembersAll = fromGuild.getMembers().collectList().block();

    Set<Long> fromMembers = new HashSet<>();

    for (Member member : fromMembersAll) {
      if (member.getRoleIds().contains(Snowflake.of(fromRole))) {
        fromMembers.add(member.getId().asLong());
      }
    }

    final var toGuild = client.getGuildById(Snowflake.of(toGuildId)).block();
    final var toMembersAll = toGuild.getMembers().collectList().block();

    AtomicInteger rolesMigrated = new AtomicInteger(0);
    toMembersAll.stream()
        .filter(
            m ->
                fromMembers.contains(m.getId().asLong())
                    && !m.getRoleIds().contains(Snowflake.of(toRole)))
        .forEach(
            m -> {
              log.info(
                  "Migrating: "
                      + m.getUsername()
                      + "#"
                      + m.getDiscriminator()
                      + "; "
                      + m.getDisplayName()
                      + " ("
                      + m.getId()
                      + ")");
              m.addRole(Snowflake.of(toRole)).block();
              rolesMigrated.incrementAndGet();
            });

    return rolesMigrated.get();
  }

  // Basically an isEquals for ApplicationCommandData
  private boolean hasChanged(
      ApplicationCommandData discordCommand, ApplicationCommandRequest command) {
    // Compare types
    if (!discordCommand.type().toOptional().orElse(1).equals(command.type().toOptional().orElse(1)))
      return true;

    // Check if description has changed.
    if (!discordCommand.description().equals(command.description().toOptional().orElse("")))
      return true;

    // Check if default permissions have changed
    boolean discordCommandDefaultPermission =
        discordCommand.defaultPermission().toOptional().orElse(true);
    boolean commandDefaultPermission = command.defaultPermission().toOptional().orElse(true);

    if (discordCommandDefaultPermission != commandDefaultPermission) return true;

    // Check and return if options have changed.
    return !discordCommand.options().equals(command.options());
  }
}
