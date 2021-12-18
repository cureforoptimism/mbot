package com.cureforoptimism.mbot;

import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;
import com.smolbrains.SmolBodiesContract;
import com.smolbrains.SmolBrainsContract;
import com.smolbrains.SmolBrainsVroomContract;
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

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Slf4j
@Configuration
@EnableScheduling
@EnableTransactionManagement
public class SpringConfiguration {
  @Bean
  public CoinGeckoApiClient coinGeckoApiClient() {
    return new CoinGeckoApiClientImpl();
  }

  @Bean
  public Web3j web3j() {
    return Web3j.build(new HttpService("https://arb1.arbitrum.io/rpc"));
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
}
