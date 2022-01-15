package com.smolbrains;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a
 * href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.1.
 */
@SuppressWarnings("rawtypes")
public class SmolLandContract extends Contract {
  public static final String BINARY = "Bin file was not provided";

  public static final String FUNC_DEFAULT_ADMIN_ROLE = "DEFAULT_ADMIN_ROLE";

  public static final String FUNC_SMOLBRAIN_MINTER_ROLE = "SMOLBRAIN_MINTER_ROLE";

  public static final String FUNC_SMOLBRAIN_OWNER_ROLE = "SMOLBRAIN_OWNER_ROLE";

  public static final String FUNC_APPROVE = "approve";

  public static final String FUNC_BALANCEOF = "balanceOf";

  public static final String FUNC_BASEURI = "baseURI";

  public static final String FUNC_CANUPGRADE = "canUpgrade";

  public static final String FUNC_FINDBIGGESTBRAINIQ = "findBiggestBrainIQ";

  public static final String FUNC_GETAPPROVED = "getApproved";

  public static final String FUNC_GETROLEADMIN = "getRoleAdmin";

  public static final String FUNC_GRANTMINTER = "grantMinter";

  public static final String FUNC_GRANTOWNER = "grantOwner";

  public static final String FUNC_GRANTROLE = "grantRole";

  public static final String FUNC_HASROLE = "hasRole";

  public static final String FUNC_ISAPPROVEDFORALL = "isApprovedForAll";

  public static final String FUNC_ISMINTER = "isMinter";

  public static final String FUNC_ISOWNER = "isOwner";

  public static final String FUNC_LANDLEVELS = "landLevels";

  public static final String FUNC_LANDMAXLEVEL = "landMaxLevel";

  public static final String FUNC_LEVELIQCOST = "levelIQCost";

  public static final String FUNC_MINT = "mint";

  public static final String FUNC_NAME = "name";

  public static final String FUNC_OWNEROF = "ownerOf";

  public static final String FUNC_RENOUNCEROLE = "renounceRole";

  public static final String FUNC_REVOKEROLE = "revokeRole";

  public static final String FUNC_safeTransferFrom = "safeTransferFrom";

  public static final String FUNC_SETAPPROVALFORALL = "setApprovalForAll";

  public static final String FUNC_SETBASEURI = "setBaseURI";

  public static final String FUNC_SETLEVELIQCOST = "setLevelIQCost";

  public static final String FUNC_SETMAXLEVEL = "setMaxLevel";

  public static final String FUNC_SETSMOLBRAIN = "setSmolBrain";

  public static final String FUNC_SMOLBRAIN = "smolBrain";

  public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

  public static final String FUNC_SYMBOL = "symbol";

  public static final String FUNC_TOKENBYINDEX = "tokenByIndex";

  public static final String FUNC_TOKENOFOWNERBYINDEX = "tokenOfOwnerByIndex";

  public static final String FUNC_TOKENURI = "tokenURI";

  public static final String FUNC_TOTALSUPPLY = "totalSupply";

  public static final String FUNC_TRANSFERFROM = "transferFrom";

  public static final String FUNC_UPGRADE = "upgrade";

  public static final String FUNC_UPGRADESAFE = "upgradeSafe";

  public static final Event APPROVAL_EVENT =
      new Event(
          "Approval",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Uint256>(true) {}));
  ;

  public static final Event APPROVALFORALL_EVENT =
      new Event(
          "ApprovalForAll",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Bool>() {}));
  ;

  public static final Event LANDMAXLEVEL_EVENT =
      new Event("LandMaxLevel", Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
  ;

  public static final Event LANDMINT_EVENT =
      new Event(
          "LandMint",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
  ;

  public static final Event LANDUPGRADE_EVENT =
      new Event(
          "LandUpgrade",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
  ;

  public static final Event LEVELIQCOST_EVENT =
      new Event("LevelIQCost", Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
  ;

  public static final Event ROLEADMINCHANGED_EVENT =
      new Event(
          "RoleAdminChanged",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Bytes32>(true) {},
              new TypeReference<Bytes32>(true) {},
              new TypeReference<Bytes32>(true) {}));
  ;

  public static final Event ROLEGRANTED_EVENT =
      new Event(
          "RoleGranted",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Bytes32>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {}));
  ;

  public static final Event ROLEREVOKED_EVENT =
      new Event(
          "RoleRevoked",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Bytes32>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {}));
  ;

  public static final Event SMOLBRAINSET_EVENT =
      new Event("SmolBrainSet", Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
  ;

  public static final Event TRANSFER_EVENT =
      new Event(
          "Transfer",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Uint256>(true) {}));
  ;

  @Deprecated
  protected SmolLandContract(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected SmolLandContract(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected SmolLandContract(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected SmolLandContract(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
    ArrayList<ApprovalEventResponse> responses =
        new ArrayList<ApprovalEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      ApprovalEventResponse typedResponse = new ApprovalEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, ApprovalEventResponse>() {
              @Override
              public ApprovalEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(APPROVAL_EVENT, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse.log = log;
                typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.tokenId =
                    (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<ApprovalEventResponse> approvalEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
    return approvalEventFlowable(filter);
  }

  public List<ApprovalForAllEventResponse> getApprovalForAllEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(APPROVALFORALL_EVENT, transactionReceipt);
    ArrayList<ApprovalForAllEventResponse> responses =
        new ArrayList<ApprovalForAllEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, ApprovalForAllEventResponse>() {
              @Override
              public ApprovalForAllEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(APPROVALFORALL_EVENT, log);
                ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
                typedResponse.log = log;
                typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.approved =
                    (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(APPROVALFORALL_EVENT));
    return approvalForAllEventFlowable(filter);
  }

  public List<LandMaxLevelEventResponse> getLandMaxLevelEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(LANDMAXLEVEL_EVENT, transactionReceipt);
    ArrayList<LandMaxLevelEventResponse> responses =
        new ArrayList<LandMaxLevelEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      LandMaxLevelEventResponse typedResponse = new LandMaxLevelEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.landMaxLevel = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<LandMaxLevelEventResponse> landMaxLevelEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, LandMaxLevelEventResponse>() {
              @Override
              public LandMaxLevelEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(LANDMAXLEVEL_EVENT, log);
                LandMaxLevelEventResponse typedResponse = new LandMaxLevelEventResponse();
                typedResponse.log = log;
                typedResponse.landMaxLevel =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<LandMaxLevelEventResponse> landMaxLevelEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(LANDMAXLEVEL_EVENT));
    return landMaxLevelEventFlowable(filter);
  }

  public List<LandMintEventResponse> getLandMintEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(LANDMINT_EVENT, transactionReceipt);
    ArrayList<LandMintEventResponse> responses =
        new ArrayList<LandMintEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      LandMintEventResponse typedResponse = new LandMintEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.to = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<LandMintEventResponse> landMintEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, LandMintEventResponse>() {
              @Override
              public LandMintEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(LANDMINT_EVENT, log);
                LandMintEventResponse typedResponse = new LandMintEventResponse();
                typedResponse.log = log;
                typedResponse.to = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.tokenId =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<LandMintEventResponse> landMintEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(LANDMINT_EVENT));
    return landMintEventFlowable(filter);
  }

  public List<LandUpgradeEventResponse> getLandUpgradeEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(LANDUPGRADE_EVENT, transactionReceipt);
    ArrayList<LandUpgradeEventResponse> responses =
        new ArrayList<LandUpgradeEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      LandUpgradeEventResponse typedResponse = new LandUpgradeEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.availableLevel =
          (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<LandUpgradeEventResponse> landUpgradeEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, LandUpgradeEventResponse>() {
              @Override
              public LandUpgradeEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(LANDUPGRADE_EVENT, log);
                LandUpgradeEventResponse typedResponse = new LandUpgradeEventResponse();
                typedResponse.log = log;
                typedResponse.tokenId =
                    (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.availableLevel =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<LandUpgradeEventResponse> landUpgradeEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(LANDUPGRADE_EVENT));
    return landUpgradeEventFlowable(filter);
  }

  public List<LevelIQCostEventResponse> getLevelIQCostEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(LEVELIQCOST_EVENT, transactionReceipt);
    ArrayList<LevelIQCostEventResponse> responses =
        new ArrayList<LevelIQCostEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      LevelIQCostEventResponse typedResponse = new LevelIQCostEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.levelIQCost = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<LevelIQCostEventResponse> levelIQCostEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, LevelIQCostEventResponse>() {
              @Override
              public LevelIQCostEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(LEVELIQCOST_EVENT, log);
                LevelIQCostEventResponse typedResponse = new LevelIQCostEventResponse();
                typedResponse.log = log;
                typedResponse.levelIQCost =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<LevelIQCostEventResponse> levelIQCostEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(LEVELIQCOST_EVENT));
    return levelIQCostEventFlowable(filter);
  }

  public List<RoleAdminChangedEventResponse> getRoleAdminChangedEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(ROLEADMINCHANGED_EVENT, transactionReceipt);
    ArrayList<RoleAdminChangedEventResponse> responses =
        new ArrayList<RoleAdminChangedEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      RoleAdminChangedEventResponse typedResponse = new RoleAdminChangedEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.previousAdminRole = (byte[]) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.newAdminRole = (byte[]) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<RoleAdminChangedEventResponse> roleAdminChangedEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, RoleAdminChangedEventResponse>() {
              @Override
              public RoleAdminChangedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(ROLEADMINCHANGED_EVENT, log);
                RoleAdminChangedEventResponse typedResponse = new RoleAdminChangedEventResponse();
                typedResponse.log = log;
                typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.previousAdminRole =
                    (byte[]) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.newAdminRole =
                    (byte[]) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<RoleAdminChangedEventResponse> roleAdminChangedEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(ROLEADMINCHANGED_EVENT));
    return roleAdminChangedEventFlowable(filter);
  }

  public List<RoleGrantedEventResponse> getRoleGrantedEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(ROLEGRANTED_EVENT, transactionReceipt);
    ArrayList<RoleGrantedEventResponse> responses =
        new ArrayList<RoleGrantedEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      RoleGrantedEventResponse typedResponse = new RoleGrantedEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<RoleGrantedEventResponse> roleGrantedEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, RoleGrantedEventResponse>() {
              @Override
              public RoleGrantedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(ROLEGRANTED_EVENT, log);
                RoleGrantedEventResponse typedResponse = new RoleGrantedEventResponse();
                typedResponse.log = log;
                typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<RoleGrantedEventResponse> roleGrantedEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(ROLEGRANTED_EVENT));
    return roleGrantedEventFlowable(filter);
  }

  public List<RoleRevokedEventResponse> getRoleRevokedEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(ROLEREVOKED_EVENT, transactionReceipt);
    ArrayList<RoleRevokedEventResponse> responses =
        new ArrayList<RoleRevokedEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      RoleRevokedEventResponse typedResponse = new RoleRevokedEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<RoleRevokedEventResponse> roleRevokedEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, RoleRevokedEventResponse>() {
              @Override
              public RoleRevokedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(ROLEREVOKED_EVENT, log);
                RoleRevokedEventResponse typedResponse = new RoleRevokedEventResponse();
                typedResponse.log = log;
                typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<RoleRevokedEventResponse> roleRevokedEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(ROLEREVOKED_EVENT));
    return roleRevokedEventFlowable(filter);
  }

  public List<SmolBrainSetEventResponse> getSmolBrainSetEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(SMOLBRAINSET_EVENT, transactionReceipt);
    ArrayList<SmolBrainSetEventResponse> responses =
        new ArrayList<SmolBrainSetEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      SmolBrainSetEventResponse typedResponse = new SmolBrainSetEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.smolBrain = (String) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<SmolBrainSetEventResponse> smolBrainSetEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, SmolBrainSetEventResponse>() {
              @Override
              public SmolBrainSetEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(SMOLBRAINSET_EVENT, log);
                SmolBrainSetEventResponse typedResponse = new SmolBrainSetEventResponse();
                typedResponse.log = log;
                typedResponse.smolBrain =
                    (String) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<SmolBrainSetEventResponse> smolBrainSetEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(SMOLBRAINSET_EVENT));
    return smolBrainSetEventFlowable(filter);
  }

  public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
    ArrayList<TransferEventResponse> responses =
        new ArrayList<TransferEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      TransferEventResponse typedResponse = new TransferEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, TransferEventResponse>() {
              @Override
              public TransferEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(TRANSFER_EVENT, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.tokenId =
                    (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<TransferEventResponse> transferEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
    return transferEventFlowable(filter);
  }

  public RemoteFunctionCall<byte[]> DEFAULT_ADMIN_ROLE() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_DEFAULT_ADMIN_ROLE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    return executeRemoteCallSingleValueReturn(function, byte[].class);
  }

  public RemoteFunctionCall<byte[]> SMOLBRAIN_MINTER_ROLE() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SMOLBRAIN_MINTER_ROLE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    return executeRemoteCallSingleValueReturn(function, byte[].class);
  }

  public RemoteFunctionCall<byte[]> SMOLBRAIN_OWNER_ROLE() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SMOLBRAIN_OWNER_ROLE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    return executeRemoteCallSingleValueReturn(function, byte[].class);
  }

  public RemoteFunctionCall<TransactionReceipt> approve(String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_APPROVE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<BigInteger> balanceOf(String owner) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_BALANCEOF,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<String> baseURI() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_BASEURI,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<Tuple2<Boolean, BigInteger>> canUpgrade(BigInteger _tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_CANUPGRADE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId)),
            Arrays.<TypeReference<?>>asList(
                new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
    return new RemoteFunctionCall<Tuple2<Boolean, BigInteger>>(
        function,
        new Callable<Tuple2<Boolean, BigInteger>>() {
          @Override
          public Tuple2<Boolean, BigInteger> call() throws Exception {
            List<Type> results = executeCallMultipleValueReturn(function);
            return new Tuple2<Boolean, BigInteger>(
                (Boolean) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
          }
        });
  }

  public RemoteFunctionCall<BigInteger> findBiggestBrainIQ(String _owner) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_FINDBIGGESTBRAINIQ,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _owner)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<String> getApproved(BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_GETAPPROVED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<byte[]> getRoleAdmin(byte[] role) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_GETROLEADMIN,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(role)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    return executeRemoteCallSingleValueReturn(function, byte[].class);
  }

  public RemoteFunctionCall<TransactionReceipt> grantMinter(String _minter) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_GRANTMINTER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _minter)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> grantOwner(String _owner) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_GRANTOWNER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _owner)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> grantRole(byte[] role, String account) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_GRANTROLE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(role),
                new org.web3j.abi.datatypes.Address(160, account)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<Boolean> hasRole(byte[] role, String account) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_HASROLE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(role),
                new org.web3j.abi.datatypes.Address(160, account)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<Boolean> isApprovedForAll(String owner, String operator) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_ISAPPROVEDFORALL,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, owner),
                new org.web3j.abi.datatypes.Address(160, operator)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<Boolean> isMinter(String _minter) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_ISMINTER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _minter)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<Boolean> isOwner(String _owner) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_ISOWNER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _owner)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<BigInteger> landLevels(BigInteger param0) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_LANDLEVELS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<BigInteger> landMaxLevel() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_LANDMAXLEVEL,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<BigInteger> levelIQCost() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_LEVELIQCOST,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<TransactionReceipt> mint(String _to) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_MINT,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _to)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<String> name() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_NAME,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<String> ownerOf(BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_OWNEROF,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<TransactionReceipt> renounceRole(byte[] role, String account) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_RENOUNCEROLE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(role),
                new org.web3j.abi.datatypes.Address(160, account)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> revokeRole(byte[] role, String account) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_REVOKEROLE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(role),
                new org.web3j.abi.datatypes.Address(160, account)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(
      String from, String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_safeTransferFrom,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(
      String from, String to, BigInteger tokenId, byte[] _data) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_safeTransferFrom,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(tokenId),
                new org.web3j.abi.datatypes.DynamicBytes(_data)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setApprovalForAll(
      String operator, Boolean approved) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETAPPROVALFORALL,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, operator),
                new org.web3j.abi.datatypes.Bool(approved)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setBaseURI(String _baseURItoSet) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETBASEURI,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_baseURItoSet)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setLevelIQCost(BigInteger _levelIQCost) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETLEVELIQCOST,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_levelIQCost)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setMaxLevel(BigInteger _landMaxLevel) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETMAXLEVEL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_landMaxLevel)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setSmolBrain(String _smolBrain) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETSMOLBRAIN,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _smolBrain)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<String> smolBrain() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SMOLBRAIN,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SUPPORTSINTERFACE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<String> symbol() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SYMBOL,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<BigInteger> tokenByIndex(BigInteger index) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TOKENBYINDEX,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(index)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<BigInteger> tokenOfOwnerByIndex(String owner, BigInteger index) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TOKENOFOWNERBYINDEX,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, owner),
                new org.web3j.abi.datatypes.generated.Uint256(index)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<String> tokenURI(BigInteger _tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TOKENURI,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<BigInteger> totalSupply() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TOTALSUPPLY,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<TransactionReceipt> transferFrom(
      String from, String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TRANSFERFROM,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> upgrade(BigInteger _tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_UPGRADE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> upgradeSafe(BigInteger _tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_UPGRADESAFE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  @Deprecated
  public static SmolLandContract load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new SmolLandContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static SmolLandContract load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new SmolLandContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static SmolLandContract load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    return new SmolLandContract(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static SmolLandContract load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    return new SmolLandContract(contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static class ApprovalEventResponse extends BaseEventResponse {
    public String owner;

    public String approved;

    public BigInteger tokenId;
  }

  public static class ApprovalForAllEventResponse extends BaseEventResponse {
    public String owner;

    public String operator;

    public Boolean approved;
  }

  public static class LandMaxLevelEventResponse extends BaseEventResponse {
    public BigInteger landMaxLevel;
  }

  public static class LandMintEventResponse extends BaseEventResponse {
    public String to;

    public BigInteger tokenId;
  }

  public static class LandUpgradeEventResponse extends BaseEventResponse {
    public BigInteger tokenId;

    public BigInteger availableLevel;
  }

  public static class LevelIQCostEventResponse extends BaseEventResponse {
    public BigInteger levelIQCost;
  }

  public static class RoleAdminChangedEventResponse extends BaseEventResponse {
    public byte[] role;

    public byte[] previousAdminRole;

    public byte[] newAdminRole;
  }

  public static class RoleGrantedEventResponse extends BaseEventResponse {
    public byte[] role;

    public String account;

    public String sender;
  }

  public static class RoleRevokedEventResponse extends BaseEventResponse {
    public byte[] role;

    public String account;

    public String sender;
  }

  public static class SmolBrainSetEventResponse extends BaseEventResponse {
    public String smolBrain;
  }

  public static class TransferEventResponse extends BaseEventResponse {
    public String from;

    public String to;

    public BigInteger tokenId;
  }
}
