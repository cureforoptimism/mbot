package com.cureforoptimism.mbot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

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

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "create_date")
  @Getter
  private Date created;
}
