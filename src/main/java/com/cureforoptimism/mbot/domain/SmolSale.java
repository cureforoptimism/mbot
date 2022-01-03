package com.cureforoptimism.mbot.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmolSale {
  @Id String id; // tx link

  @Getter Integer tokenId;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal salePrice; // in MAGIC

  @Getter
  @Temporal(TemporalType.TIMESTAMP)
  private Date blockTimestamp;

  @Getter @Setter private Boolean tweeted;
}
