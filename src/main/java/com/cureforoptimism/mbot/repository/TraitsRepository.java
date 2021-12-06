package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.Trait;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TraitsRepository extends JpaRepository<Trait, Long> {
  @Query(value = "select distinct type from trait order by type asc", nativeQuery = true)
  List<String> findDistinctTraits();

  List<Trait> findBySmol_Id(Long id);

  long countByTypeIgnoreCaseAndValueIgnoreCase(String type, String value);

  @Query(
      value = "SELECT DISTINCT value FROM trait WHERE type = ?1 ORDER BY value asc",
      nativeQuery = true)
  List<String> findDistinctByTypeIgnoreCaseOrderByValueAsc(String type);
}
