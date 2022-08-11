package com.example.demo.service;

import static com.example.demo.CodesFromFiles.readChains;
import static com.example.demo.CodesFromFiles.readPCCs;
import static com.example.demo.CodesFromFiles.readRatePlanCodes;

import com.example.demo.model.RatesStructure;
import com.google.common.collect.Lists;
import com.travelport.rates.RateAccessRQ;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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

  public com.travelport.rates.RateAccessRS buildResponse(com.travelport.rates.RateAccessRQ rateAccessRQ) {
    com.travelport.rates.RateAccessRS rateAccessRS = new com.travelport.rates.RateAccessRS();
    rateAccessRS.setGdsContext(rateAccessRQ.getGdsContext());

    return rateAccessRS;
  }

  public String comparewithGeneratedRateStructures(int numberOfChains, int numberOfRates) throws IOException {
    List<RatesStructure> rates = new ArrayList<RatesStructure>();

    //extract the codel from files
    ArrayList<String> pccs = readPCCs();
    ArrayList<String> chains = readChains();
    ArrayList<String> ratePlanCodes = readRatePlanCodes();
    //splitting the lists
    List<List<String>> pccsSplitted = Lists.partition(pccs, 5);
    List<List<String>> chainsSplitted = Lists.partition(chains, numberOfChains);
    List<List<String>> ratePlanCodesSplitted = Lists.partition(ratePlanCodes, numberOfRates);

    for (int i = 0; i < 4; i++) {
      RatesThread ratesThread = new RatesThread("thread " + i + " ", pccsSplitted.get(0), chainsSplitted, ratePlanCodesSplitted);
      ratesThread.start();

    }

    System.out.println("done");
    return "done";
  }

  public void compareWithCustomNumbersOfCodes(int numberOfChains, int numberOfRates) throws IOException {
    com.travelport.rates.RateAccessRQ rateAccessRQ = new com.travelport.rates.RateAccessRQ();
    //extract the codel from files
    ArrayList<String> pccs = readPCCs();
    ArrayList<String> chains = readChains();
    ArrayList<String> ratePlanCodes = readRatePlanCodes();
    //splitting the lists
    List<List<String>> chainsIntoHalf = Lists.partition(chains, 2);
    List<List<String>> chainsSplitted = Lists.partition(chainsIntoHalf.get(0), numberOfChains);
    List<List<String>> rateCondesInto4 = Lists.partition(ratePlanCodes, 4);
    List<List<String>> ratePlanCodesSplitted = Lists.partition(rateCondesInto4.get(0), numberOfRates);
    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    //String urlNew = "http://localhost:8045/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    //String urlOld = "http://localhost:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    FileWrite fileWrite = new FileWrite();
    for (String pcc : pccs) {

      rateAccessRQ.setPseudoCityCode(pcc);
      System.out.println(pcc);
      for (List<String> chain : chainsSplitted) {

        com.travelport.rates.ChainCodesType chainCodesType = new com.travelport.rates.ChainCodesType();
        for (String s : chain) {
          chainCodesType.getChainCode().add(s);
        }
        rateAccessRQ.setChainCodes(chainCodesType);

        for (List<String> ratePlanCode : ratePlanCodesSplitted) {

          com.travelport.rates.RatePlanCodesType ratePlanCodesType = new com.travelport.rates.RatePlanCodesType();
          for (String s : ratePlanCode) {
            ratePlanCodesType.getRatePlanCode().add(s);
          }
          rateAccessRQ.setRatePlanCodes(ratePlanCodesType);

          HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);

          long localStartEntityNew = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity,
              com.travelport.rates.RateAccessRS.class, 100);
          //System.out.println("primul call- entityNew- " + (System.currentTimeMillis()-localStartEntityNew));

          long localStartEntityOld = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity,
              com.travelport.rates.RateAccessRS.class, 100);
          //System.out.println("al doilea call- entityOld- " + (System.currentTimeMillis()-localStartEntityOld));

          boolean result = RatesProcessing.compareResponses(rateEntityNew.getBody(), rateEntityOld.getBody(), fileWrite, pcc);
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

    com.travelport.rates.RateAccessRQ rateAccessRQ = new com.travelport.rates.RateAccessRQ();

    FileWrite fileWrite = new FileWrite();

    ArrayList<String> pccs = readPCCs();
    ArrayList<String> chains = readChains();
    ArrayList<String> ratePlanCodes = readRatePlanCodes();

    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    for (String pcc : pccs) {

      rateAccessRQ.setPseudoCityCode("AB1");

      for (String chain : chains) {

        com.travelport.rates.ChainCodesType chainCodesType = new com.travelport.rates.ChainCodesType();
        chainCodesType.getChainCode().add(chain);
        rateAccessRQ.setChainCodes(chainCodesType);

        for (String ratePlanCode : ratePlanCodes) {

          com.travelport.rates.RatePlanCodesType ratePlanCodesType = new com.travelport.rates.RatePlanCodesType();
          ratePlanCodesType.getRatePlanCode().add(ratePlanCode);
          rateAccessRQ.setRatePlanCodes(ratePlanCodesType);

          HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);

          long localStartEntityNew = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity,
              com.travelport.rates.RateAccessRS.class, 100);
          System.out.println("primul call- entityNew- " + (System.currentTimeMillis() - localStartEntityNew));

          long localStartEntityOld = System.currentTimeMillis();
          ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity,
              com.travelport.rates.RateAccessRS.class, 100);
          System.out.println("al doilea call- entityOld- " + (System.currentTimeMillis() - localStartEntityOld));

          boolean result = RatesProcessing.compareResponses(rateEntityNew.getBody(), rateEntityOld.getBody(), fileWrite, pcc);
          System.out.println("AB1" + " " + chain + " " + ratePlanCode + " => " + result);
        }
      }
    }

    fileWrite.close();
  }

}
