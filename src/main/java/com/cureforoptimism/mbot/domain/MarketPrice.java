package com.cureforoptimism.mbot.domain;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MarketPrice implements Serializable {
  private String id;

  private Double price;
  private Double change;
  private Double change12h;
  private Double change4h;
  private Double change1h;

  private Double volume24h;
  private Double volume12h;
  private Double volume4h;
  private Double volume1h;

  private Double ethPrice;
}
