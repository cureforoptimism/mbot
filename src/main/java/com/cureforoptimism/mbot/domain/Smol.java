package com.cureforoptimism.mbot.domain;

import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Smol {
  @Getter @Id Long id;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smol", cascade = CascadeType.ALL)
  Set<Trait> traits;

  @Getter @Setter Date birthday;

  @Getter @Setter String name;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smol", cascade = CascadeType.ALL)
  Set<VroomTrait> vroomTraits;
}
