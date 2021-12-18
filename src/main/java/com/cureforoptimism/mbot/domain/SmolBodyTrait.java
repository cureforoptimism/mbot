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
public class SmolBodyTrait {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Getter String type;
  @Getter String value;

  @ManyToOne
  @JoinColumn(name = "smol_body_id")
  private SmolBody smolBody;
}
