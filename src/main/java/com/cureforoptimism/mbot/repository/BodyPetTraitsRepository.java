package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.BodyPetTrait;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BodyPetTraitsRepository extends JpaRepository<BodyPetTrait, Long> {
  @Query(value = "select distinct type from body_pet_trait order by type asc", nativeQuery = true)
  List<String> findDistinctTraits();

  List<BodyPetTrait> findByBodyPet_Id(Long id);

  long countByTypeIgnoreCaseAndValueIgnoreCase(String type, String value);

  @Query(
      value = "SELECT DISTINCT value FROM body_pet_trait WHERE type = ?1 ORDER BY value asc",
      nativeQuery = true)
  List<String> findDistinctByTypeIgnoreCaseOrderByValueAsc(String type);
}
