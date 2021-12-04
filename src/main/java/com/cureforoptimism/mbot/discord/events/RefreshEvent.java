package com.cureforoptimism.mbot.discord.events;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.ShardInfo;

public class RefreshEvent extends Event {
  public RefreshEvent(GatewayDiscordClient gateway, ShardInfo shardInfo) {
    super(gateway, shardInfo);
  }
}
