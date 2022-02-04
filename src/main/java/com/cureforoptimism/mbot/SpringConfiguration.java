package com.cureforoptimism.mbot;

import com.cureforoptimism.mbot.service.TokenService;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;
import com.smolbrains.PetsContract;
import com.smolbrains.SmolBodiesContract;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsRocketContract;
import com.smolbrains.SmolBrainsVroomContract;
import com.smolbrains.SmolLandContract;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@Slf4j
@Configuration
@EnableScheduling
@EnableTransactionManagement
@AllArgsConstructor
public class SpringConfiguration {
  final TokenService tokenService;

  @Bean
  public CoinGeckoApiClient coinGeckoApiClient() {
    return new CoinGeckoApiClientImpl();
  }

  @Bean
  public Web3j web3j() {
    return Web3j.build(new HttpService("https://arb1.arbitrum.io/rpc"));
  }

  @Bean
  public SmolBrainsRocketContract smolBrainsRocketContract() {
    ContractGasProvider contractGasProvider = new DefaultGasProvider();

    try {
      Credentials dummyCredentials = Credentials.create(Keys.createEcKeyPair());
      return SmolBrainsRocketContract.load(
          "0x8957A18a77451d762dE204b61EA4F858Bb3bED4d",
          web3j(),
          dummyCredentials,
          contractGasProvider);

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException ex) {
      log.error("unable to create dummy credentials", ex);
      return null;
    }
  }

  @Bean
  public SmolBrainsVroomContract smolBrainsVroomContract() {
    ContractGasProvider contractGasProvider = new DefaultGasProvider();

    try {
      Credentials dummyCredentials = Credentials.create(Keys.createEcKeyPair());
      return SmolBrainsVroomContract.load(
          "0xB16966daD2B5a5282b99846B23dcDF8C47b6132C",
          web3j(),
          dummyCredentials,
          contractGasProvider);

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException ex) {
      log.error("unable to create dummy credentials", ex);
      return null;
    }
  }

  @Bean
  public SmolBodiesContract smolBodiesContract() {
    ContractGasProvider contractGasProvider = new DefaultGasProvider();

    try {
      Credentials dummyCredentials = Credentials.create(Keys.createEcKeyPair());
      return SmolBodiesContract.load(
          "0x17dacad7975960833f374622fad08b90ed67d1b5",
          web3j(),
          dummyCredentials,
          contractGasProvider);

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException ex) {
      log.error("unable to create dummy credentials", ex);
      return null;
    }
  }

  @Bean
  public SmolBrainsContract smolBrainsContract() {
    ContractGasProvider contractGasProvider = new DefaultGasProvider();

    try {
      Credentials dummyCredentials = Credentials.create(Keys.createEcKeyPair());
      return SmolBrainsContract.load(
          "0x6325439389e0797ab35752b4f43a14c004f22a9c",
          web3j(),
          dummyCredentials,
          contractGasProvider);

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException ex) {
      log.error("unable to create dummy credentials", ex);
      return null;
    }
  }

  @Bean
  public SmolLandContract smolLandContract() {
    ContractGasProvider contractGasProvider = new DefaultGasProvider();

    try {
      Credentials dummyCredentials = Credentials.create(Keys.createEcKeyPair());
      return SmolLandContract.load(
          "0xd666d1cc3102cd03e07794a61e5f4333b4239f53",
          web3j(),
          dummyCredentials,
          contractGasProvider);

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException ex) {
      log.error("unable to create dummy credentials", ex);
      return null;
    }
  }

  @Bean
  public PetsContract petsContract() {
    ContractGasProvider contractGasProvider = new DefaultGasProvider();

    try {
      Credentials dummyCredentials = Credentials.create(Keys.createEcKeyPair());
      return PetsContract.load(
          "0xF6Cc57C45CE730496b4d3Df36b9A4E4C3a1B9754",
          web3j(),
          dummyCredentials,
          contractGasProvider);

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException ex) {
      log.error("unable to create dummy credentials", ex);
      return null;
    }
  }

  @Bean
  public TwitterClient twitterClient() {
    return new TwitterClient(
        TwitterCredentials.builder()
            .accessToken(tokenService.getTwitterApiToken())
            .accessTokenSecret(tokenService.getTwitterApiTokenSecret())
            .bearerToken(tokenService.getTwitterApiBearerToken())
            .apiKey(tokenService.getTwitterApiKey())
            .apiSecretKey(tokenService.getTwitterApiSecret())
            .build());
  }
}
