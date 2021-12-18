package com.cureforoptimism.mbot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmolBodyRarityRank {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Getter Integer rank;

  @Getter Long smolBodyId; // This should be called something else, fix later

  double score;
}
