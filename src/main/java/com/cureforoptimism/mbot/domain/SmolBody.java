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
public class SmolBody {
  @Id Long id;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "smolBody", cascade = CascadeType.ALL)
  Set<SmolBodyTrait> traits;
}
