package com.smolbrains;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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
public class SmolBrainsRocketContract extends Contract {
  public static final String BINARY = "Bin file was not provided";

  public static final String FUNC_BOARD = "board";

  public static final String FUNC_BOARDEDBEFOREDEADLINE = "boardedBeforeDeadline";

  public static final String FUNC_DEADLINE = "deadline";

  public static final String FUNC_OWNER = "owner";

  public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

  public static final String FUNC_SCHOOL = "school";

  public static final String FUNC_SETDEADLINE = "setDeadline";

  public static final String FUNC_SETSCHOOL = "setSchool";

  public static final String FUNC_SETSMOLBRAIN = "setSmolBrain";

  public static final String FUNC_SMOLBRAIN = "smolBrain";

  public static final String FUNC_TIMESTAMPJOINED = "timestampJoined";

  public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

  public static final Event BOARD_EVENT =
      new Event(
          "Board",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
  ;

  public static final Event DEADLINESET_EVENT =
      new Event("DeadlineSet", Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
  ;

  public static final Event OWNERSHIPTRANSFERRED_EVENT =
      new Event(
          "OwnershipTransferred",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
  ;

  public static final Event SCHOOLSET_EVENT =
      new Event("SchoolSet", Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
  ;

  public static final Event SMOLBRAINSET_EVENT =
      new Event("SmolBrainSet", Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
  ;

  @Deprecated
  protected SmolBrainsRocketContract(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected SmolBrainsRocketContract(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected SmolBrainsRocketContract(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected SmolBrainsRocketContract(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public List<BoardEventResponse> getBoardEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(BOARD_EVENT, transactionReceipt);
    ArrayList<BoardEventResponse> responses = new ArrayList<BoardEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      BoardEventResponse typedResponse = new BoardEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.smolBrainTokenId =
          (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<BoardEventResponse> boardEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, BoardEventResponse>() {
              @Override
              public BoardEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(BOARD_EVENT, log);
                BoardEventResponse typedResponse = new BoardEventResponse();
                typedResponse.log = log;
                typedResponse.smolBrainTokenId =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.timestamp =
                    (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<BoardEventResponse> boardEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(BOARD_EVENT));
    return boardEventFlowable(filter);
  }

  public List<DeadlineSetEventResponse> getDeadlineSetEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DEADLINESET_EVENT, transactionReceipt);
    ArrayList<DeadlineSetEventResponse> responses =
        new ArrayList<DeadlineSetEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DeadlineSetEventResponse typedResponse = new DeadlineSetEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DeadlineSetEventResponse> deadlineSetEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, DeadlineSetEventResponse>() {
              @Override
              public DeadlineSetEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DEADLINESET_EVENT, log);
                DeadlineSetEventResponse typedResponse = new DeadlineSetEventResponse();
                typedResponse.log = log;
                typedResponse.deadline =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DeadlineSetEventResponse> deadlineSetEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DEADLINESET_EVENT));
    return deadlineSetEventFlowable(filter);
  }

  public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
    ArrayList<OwnershipTransferredEventResponse> responses =
        new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
      EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, OwnershipTransferredEventResponse>() {
              @Override
              public OwnershipTransferredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse =
                    new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner =
                    (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
    return ownershipTransferredEventFlowable(filter);
  }

  public List<SchoolSetEventResponse> getSchoolSetEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(SCHOOLSET_EVENT, transactionReceipt);
    ArrayList<SchoolSetEventResponse> responses =
        new ArrayList<SchoolSetEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      SchoolSetEventResponse typedResponse = new SchoolSetEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.school = (String) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<SchoolSetEventResponse> schoolSetEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, SchoolSetEventResponse>() {
              @Override
              public SchoolSetEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(SCHOOLSET_EVENT, log);
                SchoolSetEventResponse typedResponse = new SchoolSetEventResponse();
                typedResponse.log = log;
                typedResponse.school = (String) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<SchoolSetEventResponse> schoolSetEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(SCHOOLSET_EVENT));
    return schoolSetEventFlowable(filter);
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

  public RemoteFunctionCall<TransactionReceipt> board() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_BOARD, Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<Boolean> boardedBeforeDeadline(BigInteger _tokenId) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_BOARDEDBEFOREDEADLINE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<BigInteger> deadline() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_DEADLINE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<String> owner() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_OWNER,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_RENOUNCEOWNERSHIP,
            Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<String> school() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SCHOOL,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<TransactionReceipt> setDeadline(BigInteger _deadline) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETDEADLINE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_deadline)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setSchool(String _school) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_SETSCHOOL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _school)),
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

  public RemoteFunctionCall<BigInteger> timestampJoined(BigInteger param0) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TIMESTAMPJOINED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  @Deprecated
  public static SmolBrainsRocketContract load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new SmolBrainsRocketContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static SmolBrainsRocketContract load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new SmolBrainsRocketContract(
        contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static SmolBrainsRocketContract load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    return new SmolBrainsRocketContract(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static SmolBrainsRocketContract load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    return new SmolBrainsRocketContract(
        contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static class BoardEventResponse extends BaseEventResponse {
    public BigInteger smolBrainTokenId;

    public BigInteger timestamp;
  }

  public static class DeadlineSetEventResponse extends BaseEventResponse {
    public BigInteger deadline;
  }

  public static class OwnershipTransferredEventResponse extends BaseEventResponse {
    public String previousOwner;

    public String newOwner;
  }

  public static class SchoolSetEventResponse extends BaseEventResponse {
    public String school;
  }

  public static class SmolBrainSetEventResponse extends BaseEventResponse {
    public String smolBrain;
  }
}
