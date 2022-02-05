package com.cureforoptimism.mbot.domain;

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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyPet {
  @Getter @Id Long id;

  @Getter
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "bodyPet", cascade = CascadeType.ALL)
  Set<BodyPetTrait> traits;
}
