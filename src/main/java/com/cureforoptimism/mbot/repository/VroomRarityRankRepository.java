package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.VroomRarityRank;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VroomRarityRankRepository extends JpaRepository<VroomRarityRank, Long> {
  VroomRarityRank findBySmolId(Long smolId);

  List<VroomRarityRank> findByRank(Integer rank);

  @Query(value = "select * from vroom_rarity_rank limit 20", nativeQuery = true)
  Set<VroomRarityRank> findTop20();
}
