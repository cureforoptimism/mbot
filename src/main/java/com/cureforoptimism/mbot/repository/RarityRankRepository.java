package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.RarityRank;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RarityRankRepository extends JpaRepository<RarityRank, Long> {
  RarityRank findBySmolId(Long smolId);

  List<RarityRank> findByRank(Integer rank);

  @Query(value = "select * from rarity_rank limit 20", nativeQuery = true)
  Set<RarityRank> findTop20();
}
