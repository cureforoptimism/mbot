package com.cureforoptimism.mbot.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@PropertySource({"classpath:application.yml"})
@Service
public class TokenService {
  @Value("${tokens.file}")
  private String file;

  public String getDiscordToken() {
    ClassPathResource classPathResource = new ClassPathResource(file);

    try (InputStream input = classPathResource.getInputStream()) {
      Properties properties = new Properties();
      properties.load(input);
      return properties.get("discord_access_token").toString();
    } catch (IOException ex) {
      log.error("Unable to retrieve Discord token", ex);
    }

    return null;
  }
}
