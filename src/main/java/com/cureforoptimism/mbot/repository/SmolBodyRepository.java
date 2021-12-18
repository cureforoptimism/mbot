package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.SmolBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SmolBodyRepository extends JpaRepository<SmolBody, Long> {
  long countByTraits_Type(String type);

  long countByTraits_TypeAndTraits_Value(String type, String value);
}
