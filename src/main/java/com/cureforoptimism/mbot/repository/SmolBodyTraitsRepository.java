package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.SmolBodyTrait;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SmolBodyTraitsRepository extends JpaRepository<SmolBodyTrait, Long> {
  @Query(value = "select distinct type from smol_body_trait order by type asc", nativeQuery = true)
  List<String> findDistinctTraits();

  List<SmolBodyTrait> findBySmolBody_Id(Long id);

  long countByTypeIgnoreCaseAndValueIgnoreCase(String type, String value);

  @Query(
      value = "SELECT DISTINCT value FROM smol_body_trait WHERE type = ?1 ORDER BY value asc",
      nativeQuery = true)
  List<String> findDistinctByTypeIgnoreCaseOrderByValueAsc(String type);
}
