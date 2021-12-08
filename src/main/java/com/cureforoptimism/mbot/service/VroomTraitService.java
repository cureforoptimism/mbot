package com.cureforoptimism.mbot.service;

import com.smolbrains.SmolBrainsVroomContract;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class VroomTraitService {
  private final SmolBrainsVroomContract smolBrainsVroomContract;
}
