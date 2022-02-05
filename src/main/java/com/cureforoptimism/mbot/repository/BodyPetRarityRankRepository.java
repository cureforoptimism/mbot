package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.BodyPetRarityRank;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BodyPetRarityRankRepository extends JpaRepository<BodyPetRarityRank, Long> {
  BodyPetRarityRank findByBodyPetId(Long bodyPetId);

  List<BodyPetRarityRank> findByRank(Integer rank);

  @Query(value = "select * from body_pet_rarity_rank limit 20", nativeQuery = true)
  Set<BodyPetRarityRank> findTop20();
}
