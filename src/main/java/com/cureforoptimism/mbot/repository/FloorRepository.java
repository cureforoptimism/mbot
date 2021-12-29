package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.Floor;
import java.util.Date;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, Long> {
  Set<Floor> findByCreatedIsAfter(Date created);
}
