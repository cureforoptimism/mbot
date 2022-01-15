package com.cureforoptimism.mbot.domain;

import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFloor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  Long discordUserId;

  @Getter String discordId;

  @Fetch(FetchMode.JOIN)
  @ElementCollection
  @Getter
  Set<Long> smols;

  @Fetch(FetchMode.JOIN)
  @ElementCollection
  @Getter
  Set<Long> vrooms;

  @Fetch(FetchMode.JOIN)
  @ElementCollection
  @Getter
  Set<Long> swols;

  @Fetch(FetchMode.JOIN)
  @ElementCollection
  @Getter
  Set<Long> land;
}
