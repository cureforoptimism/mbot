package com.cureforoptimism.mbot.domain;

import java.util.Set;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Smol {
  @Getter @Id Long id;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smol", cascade = CascadeType.ALL)
  Set<Trait> traits;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smol", cascade = CascadeType.ALL)
  Set<VroomTrait> vroomTraits;
}
