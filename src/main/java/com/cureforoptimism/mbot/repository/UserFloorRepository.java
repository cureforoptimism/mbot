package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.UserFloor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFloorRepository extends JpaRepository<UserFloor, Long> {
  UserFloor findByDiscordUserId(Long discordUserId);
}
