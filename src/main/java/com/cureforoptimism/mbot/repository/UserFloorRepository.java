package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.UserFloor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFloorRepository extends JpaRepository<UserFloor, Long> {
  UserFloor findByDiscordUserId(Long discordUserId);

  List<UserFloor> findByDiscordIdIsNull();
}
