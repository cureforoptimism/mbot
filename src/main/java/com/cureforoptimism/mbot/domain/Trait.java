package com.cureforoptimism.mbot.domain;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trait {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Getter String type;
  @Getter String value;

  @ManyToOne
  @JoinColumn(name = "smol_id")
  Smol smol;
}
