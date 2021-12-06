package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.RarityRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface RarityRankRepository extends JpaRepository<RarityRank, Long> {
  RarityRank findBySmolId(Long smolId);

    @Query(value = "select * from rarity_rank limit 20", nativeQuery = true)
    Set<RarityRank> findTop20();
}
