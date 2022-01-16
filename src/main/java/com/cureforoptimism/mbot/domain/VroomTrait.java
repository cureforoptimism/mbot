package com.cureforoptimism.mbot.domain;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VroomTrait {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Getter String type;
  @Getter String value;

  @ManyToOne
  @JoinColumn(name = "smol_id")
  Smol smol;

  @Setter
  @ManyToOne
  @JoinColumn(name = "vroom_id")
  Vroom vroom;
}
