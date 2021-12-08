package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.VroomTrait;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VroomTraitsRepository extends JpaRepository<VroomTrait, Long> {
  @Query(value = "select distinct type from vroom_trait order by type asc", nativeQuery = true)
  List<String> findDistinctTraits();

  List<VroomTrait> findBySmol_Id(Long id);

  long countByTypeIgnoreCaseAndValueIgnoreCase(String type, String value);

  @Query(
      value = "SELECT DISTINCT value FROM vroom_trait WHERE type = ?1 ORDER BY value asc",
      nativeQuery = true)
  List<String> findDistinctByTypeIgnoreCaseOrderByValueAsc(String type);
}
