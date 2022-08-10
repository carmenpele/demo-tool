package com.example.demo.service;

import static com.example.demo.CodesFromFiles.readChains;
import static com.example.demo.CodesFromFiles.readPCCs;
import static com.example.demo.CodesFromFiles.readRatePlanCodes;

import com.example.demo.model.CombinationsWanted;
import com.google.common.collect.Lists;
import com.travelport.rates.ChainCodesType;
import com.travelport.rates.RateAccessCodeType;
import com.travelport.rates.RateAccessDetailType;
import com.travelport.rates.RateAccessRQ;
import com.travelport.rates.RateAccessRS;
import com.travelport.rates.RatePlanCodesType;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RatesService {

  private final RestTemplate restTemplate;
  Logger logger = LoggerFactory.getLogger(RatesService.class);

  @Autowired
  public RatesService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder
        .setConnectTimeout(Duration.ofMillis(60000))
        .setReadTimeout(Duration.ofMillis(60000))
        .build();
  }
//  @Async
//  public CompletableFuture<RateAccessRS> saveRate(RateAccessRQ rateAccessRQ) {
//    RateAccessRS rateAccessRS = new RateAccessRS();
//    rateAccessRS.setGdsContext(rateAccessRQ.getGdsContext());
//    logger.info(" rate received- 1");
//    return CompletableFuture.completedFuture(rateAccessRS);
//  }
//  @Async
//  public CompletableFuture<Boolean> compareRates(RateAccessRQ rateAccessRQ1){
//    if(rateAccessRQ1.equals(rateAccessRQ1)){
//      return CompletableFuture.completedFuture(true);
//    }
//    else{
//      return CompletableFuture.completedFuture(false);
//    }
//  }
  public com.travelport.rates.RateAccessRS buildResponse(com.travelport.rates.RateAccessRQ rateAccessRQ) {
    com.travelport.rates.RateAccessRS rateAccessRS = new com.travelport.rates.RateAccessRS();
    rateAccessRS.setGdsContext(rateAccessRQ.getGdsContext());

    return rateAccessRS;
  }
  public boolean compareResponses(com.travelport.rates.RateAccessRS rateAccessRS1, com.travelport.rates.RateAccessRS rateAccessRS2, FileWrite fileWrite, String pcc)
      throws IOException {
    if(rateAccessRS1.getStatus().getMessage().equals("Success") && rateAccessRS1.getStatus().getMessage().equals("Success")){
      for (RateAccessDetailType rateAccessDetail: rateAccessRS1.getRateAccessDetails().getRateAccessDetail()) {
        for(RateAccessCodeType rateAccessCodeType: rateAccessDetail.getRateAccessCodes().getRateAccessCode()){
            if(getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2,rateAccessDetail.getChainCode(),rateAccessCodeType.getRatePlanCode())==null){
              System.out.println("PCC NOT FOUND IN OLD APP: "+rateAccessRS1.getPseudoCityCode()+" -> "+false);
              return false;
            }
            if(rateAccessCodeType.isUserAllowed() != getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2,rateAccessDetail.getChainCode(),rateAccessCodeType.getRatePlanCode()).isUserAllowed()){
              System.out.println("DIFFERENT RESULT: "+rateAccessRS1.getPseudoCityCode()+" "+rateAccessDetail.getChainCode()+" "+rateAccessCodeType.getRatePlanCode() +" -> "+false);
              fileWrite.writeText(rateAccessRS1.getPseudoCityCode()+" "+rateAccessDetail.getChainCode()+" "+rateAccessCodeType.getRatePlanCode() +" -> "+false+"\n");
              return false;
            }
        }
      }
    }
    else{
      System.out.println("UNSUCCESSFULL STATUS: "+pcc+" -> "+false+" "+rateAccessRS1.getStatus().getMessage()+"\n");
      fileWrite.writeText("UNSUCCESSFULL STATUS: "+pcc+" -> "+false+" "+rateAccessRS1.getStatus().getMessage()+"\n");
      return false;
    }
    return true;

  }
  //TO ACCESS THE UserAllowed VALUE FROM THE OTHER RateAccessRS
  public RateAccessCodeType getRateAccessCodeTypeFromRateAccessRS(RateAccessRS rateAccessRS,String chainCode, String ratePlanCode){
    if(rateAccessRS.getRateAccessDetails()==null)
      return null;
    return rateAccessRS.getRateAccessDetails().getRateAccessDetail().stream().filter(r-> {
          if (!r.getChainCode().equals(chainCode)) {
            return false;
          }
          r.getRateAccessCodes().getRateAccessCode().stream().filter(ra -> ra.getRatePlanCode().equals(ratePlanCode));
          return true;
        }
        ).collect(Collectors.toList()).get(0).getRateAccessCodes().getRateAccessCode().stream().filter(rac->rac.getRatePlanCode().equals(ratePlanCode)).collect(Collectors.toList()).get(0);
  }
  public void compareWithCustomNumbersOfCodes(int numberOfChains,int numberOfRates) throws IOException {
    com.travelport.rates.RateAccessRQ rateAccessRQ=new com.travelport.rates.RateAccessRQ();
    //extract the codel from files
    ArrayList<String> pccs=readPCCs();
    ArrayList<String> chains=readChains();
    ArrayList<String> ratePlanCodes=readRatePlanCodes();
    //splitting the lists
    List<List<String>> chainsIntoHalf=Lists.partition(chains,2);
    List<List<String>> chainsSplitted=Lists.partition(chainsIntoHalf.get(0),numberOfChains);
    List<List<String>> rateCondesInto4=Lists.partition(ratePlanCodes,4);
    List<List<String>> ratePlanCodesSplitted=Lists.partition(rateCondesInto4.get(0), numberOfRates);
    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    //String urlNew = "http://localhost:8045/rates";
    //String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    String urlOld = "http://localhost:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    FileWrite fileWrite=new FileWrite();
    for(String pcc:pccs){

      rateAccessRQ.setPseudoCityCode(pcc);
      System.out.println(pcc);
      for(List<String> chain:chainsSplitted){

        com.travelport.rates.ChainCodesType chainCodesType=new com.travelport.rates.ChainCodesType();
        for(String s:chain){
          chainCodesType.getChainCode().add(s);
        }
        rateAccessRQ.setChainCodes(chainCodesType);

        for(List<String> ratePlanCode: ratePlanCodesSplitted){

          com.travelport.rates.RatePlanCodesType ratePlanCodesType=new com.travelport.rates.RatePlanCodesType();
          for(String s:ratePlanCode){
            ratePlanCodesType.getRatePlanCode().add(s);
          }
          rateAccessRQ.setRatePlanCodes(ratePlanCodesType);

          HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);

          long localStartEntityNew = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
          //System.out.println("primul call- entityNew- " + (System.currentTimeMillis()-localStartEntityNew));

          long localStartEntityOld = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
          //System.out.println("al doilea call- entityOld- " + (System.currentTimeMillis()-localStartEntityOld));

          boolean result=compareResponses(rateEntityNew.getBody(),rateEntityOld.getBody(),fileWrite,pcc);
          //System.out.println("AB1"+" "+chain+" "+ratePlanCode+" => "+result);

        }
      }
    }


    fileWrite.close();




  }
  public void compareAll() throws IOException {

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    com.travelport.rates.RateAccessRQ rateAccessRQ=new com.travelport.rates.RateAccessRQ();

    FileWrite fileWrite=new FileWrite();

    ArrayList<String> pccs=readPCCs();
    ArrayList<String> chains=readChains();
    ArrayList<String> ratePlanCodes=readRatePlanCodes();

    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    for(String pcc:pccs){

      rateAccessRQ.setPseudoCityCode("AB1");

      for(String chain:chains){

        com.travelport.rates.ChainCodesType chainCodesType=new com.travelport.rates.ChainCodesType();
        chainCodesType.getChainCode().add(chain);
        rateAccessRQ.setChainCodes(chainCodesType);

        for(String ratePlanCode: ratePlanCodes){

          com.travelport.rates.RatePlanCodesType ratePlanCodesType=new com.travelport.rates.RatePlanCodesType();
          ratePlanCodesType.getRatePlanCode().add(ratePlanCode);
          rateAccessRQ.setRatePlanCodes(ratePlanCodesType);

          HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);

          long localStartEntityNew = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
          System.out.println("primul call- entityNew- " + (System.currentTimeMillis()-localStartEntityNew));

          long localStartEntityOld = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
          System.out.println("al doilea call- entityOld- " + (System.currentTimeMillis()-localStartEntityOld));

          boolean result=compareResponses(rateEntityNew.getBody(),rateEntityOld.getBody(),fileWrite,pcc);
          System.out.println("AB1"+" "+chain+" "+ratePlanCode+" => "+result);

        }
      }
    }


    fileWrite.close();
  }

  public RateAccessRQ generateRateAccessRQ(CombinationsWanted combinationsWanted) throws IOException {
    RateAccessRQ rateAccessRQ= new RateAccessRQ();

    ArrayList<String> pccs=readPCCs();
    ArrayList<String> chains=readChains();
    List<List<String>> chainsSplitted= Lists.partition(chains, 10);
    ArrayList<String> ratePlanCodes=readRatePlanCodes();

    Random rand=new Random();

    // 1 pcc, 10 chains , 8 ratePlanCode

    int indexPcc=rand.nextInt(pccs.size());
    rateAccessRQ.setPseudoCityCode(pccs.get(indexPcc));

    ChainCodesType chainCodesType=new ChainCodesType();
    for(int i=0;i<combinationsWanted.getNumberOfChains();i++){
      int indexChain=rand.nextInt(chains.size());
      while(chainCodesType.getChainCode().contains(chains.get(indexChain))){
        indexChain=rand.nextInt(chains.size());
      }
      chainCodesType.getChainCode().add(chains.get(indexChain));
    }

    RatePlanCodesType ratePlanCodesType=new RatePlanCodesType();
    for(int i=0;i<combinationsWanted.getNumberOfRatePlanCodes();i++){
      int indexRatePlans =rand.nextInt(ratePlanCodes.size());
      while(chainCodesType.getChainCode().contains(chains.get(indexRatePlans))){
        indexRatePlans =rand.nextInt(ratePlanCodes.size());
      }
      ratePlanCodesType.getRatePlanCode().add(ratePlanCodes.get(indexRatePlans));
    }

    rateAccessRQ.setChainCodes(chainCodesType);
    rateAccessRQ.setRatePlanCodes(ratePlanCodesType);
    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    return rateAccessRQ;
  }
}
