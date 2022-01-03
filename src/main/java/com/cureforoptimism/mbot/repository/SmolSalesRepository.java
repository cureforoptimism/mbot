package com.cureforoptimism.mbot.repository;

import com.cureforoptimism.mbot.domain.SmolSale;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmolSalesRepository extends JpaRepository<SmolSale, String> {
  SmolSale findFirstByTweetedIsTrueOrderByBlockTimestampDesc();

  List<SmolSale> findByBlockTimestampIsAfterAndTweetedIsFalseOrderByBlockTimestampAsc(
      Date blockTimestamp);
}
