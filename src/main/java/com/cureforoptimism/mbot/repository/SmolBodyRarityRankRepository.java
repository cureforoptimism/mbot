package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.SmolBodyRarityRank;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SmolBodyRarityRankRepository extends JpaRepository<SmolBodyRarityRank, Long> {
  SmolBodyRarityRank findBySmolBodyId(Long smolBodyId);

  List<SmolBodyRarityRank> findByRank(Integer rank);

  @Query(value = "select * from smol_body_rarity_rank limit 20", nativeQuery = true)
  Set<SmolBodyRarityRank> findTop20();
}
