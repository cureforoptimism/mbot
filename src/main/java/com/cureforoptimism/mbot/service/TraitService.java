package com.cureforoptimism.mbot.service;

import com.cureforoptimism.mbot.repository.TraitsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TraitService {
  private final TraitsRepository traitsRepository;
}
