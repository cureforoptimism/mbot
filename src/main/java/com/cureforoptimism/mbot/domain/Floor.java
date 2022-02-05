package com.cureforoptimism.mbot.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal magicPrice;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal landFloor;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal maleFloor;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal femaleFloor;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal vroomFloor;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal bodyFloor;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal petFloor;

  @Column(precision = 19, scale = 10)
  @Getter
  BigDecimal bodyPetFloor;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "create_date")
  @Getter
  private Date created;
}
