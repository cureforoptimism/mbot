package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.Smol;
import java.util.Date;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SmolRepository extends JpaRepository<Smol, Long> {
  long countByTraits_Type(String type);

  Set<Smol> findByBirthday(Date birthday);

  Set<Smol> findByBirthdayIsBetween(Date birthdayStart, Date birthdayEnd);

  Set<Smol> findByTraits_Value(String value);

  long countByTraits_TypeAndTraits_Value(String type, String value);
}
