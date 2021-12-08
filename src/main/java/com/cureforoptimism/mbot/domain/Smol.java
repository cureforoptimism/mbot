package com.cureforoptimism.mbot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Smol {
  @Id Long id;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smol", cascade = CascadeType.ALL)
  Set<Trait> traits;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smol", cascade = CascadeType.ALL)
  Set<VroomTrait> vroomTraits;
}
