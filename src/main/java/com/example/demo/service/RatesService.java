package com.example.demo.service;


import com.google.common.collect.Lists;
import com.travelport.rates.RateAccessRQ;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RatesService {

  Logger logger = LoggerFactory.getLogger(RatesService.class);

  public com.travelport.rates.RateAccessRS buildResponse(com.travelport.rates.RateAccessRQ rateAccessRQ) {
    com.travelport.rates.RateAccessRS rateAccessRS = new com.travelport.rates.RateAccessRS();
    rateAccessRS.setGdsContext(rateAccessRQ.getGdsContext());
    return rateAccessRS;
  }

  // 1 pcc, 5 random chains, 5 random ratePlanCodes
  public void compare(String pccLink, String chainsLink, String ratePlanCodesLink) throws IOException {

    //extract the codel from files
    ArrayList<String> pccs = FileReading.readFromFile(pccLink);
    ArrayList<String> chains = FileReading.readFromFile(chainsLink);
    ArrayList<String> ratePlanCodes = FileReading.readFromFile(ratePlanCodesLink);
    //splitting the lists
    List<List<String>> pccsSplitted = Lists.partition(pccs, 150);

    for (int i = 0; i < pccsSplitted.size(); i++) {
      RatesThread ratesThread = new RatesThread("thread " + i + " ", pccsSplitted.get(i), chains, ratePlanCodes);
      ratesThread.start();
    }

  }
  public void seeOnlyUserAllowedCases(String pccLink, String chainsLink, String ratePlanCodesLink) throws IOException {

    //extract the codel from files
    ArrayList<String> pccs = FileReading.readFromFile(pccLink);
    ArrayList<String> chains = FileReading.readFromFile(chainsLink);
    ArrayList<String> ratePlanCodes = FileReading.readFromFile(ratePlanCodesLink);
    //splitting the lists
    List<List<String>> pccsSplitted = Lists.partition(pccs, 150);

    for (int i = 0; i < pccsSplitted.size(); i++) {
      RatesThreadOnlyForNewNego ratesThread = new RatesThreadOnlyForNewNego ("thread " + i + " ", pccsSplitted.get(i), chains, ratePlanCodes);
      ratesThread.start();
    }

  }

  //1 pcc, "numberOfChains" chains, "numberOfRates" ratePlanCodes
  public void compareWithCustomNumbersOfCodes(int numberOfChains, int numberOfRates) throws IOException {
    com.travelport.rates.RateAccessRQ rateAccessRQ = new com.travelport.rates.RateAccessRQ();
    //extract the codel from files
    ArrayList<String> pccs = FileReading.readFromFile("C:/Users/carmen.pele/Documents/GitHub/Hotels/Repos/demo/src/files/pcc.csv");
    ArrayList<String> chains = FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/chains.csv");
    ArrayList<String> ratePlanCodes = FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/ratePlanCode.csv");
    //splitting the lists
    List<List<String>> chainsIntoHalf = Lists.partition(chains, 2);
    List<List<String>> chainsSplitted = Lists.partition(chainsIntoHalf.get(0), numberOfChains);
    List<List<String>> rateCondesInto4 = Lists.partition(ratePlanCodes, 4);
    List<List<String>> ratePlanCodesSplitted = Lists.partition(rateCondesInto4.get(0), numberOfRates);
    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    RestTemplate restTemplate = new RestTemplate();
    //String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    //String urlNew = "http://localhost:8045/rates";
    String urlNew = "";
    String urlOld = "http://hotelrateplanres.pp.tvlport.com:50054/rates";
    //String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    //String urlOld = "http://localhost:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

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

          boolean result = RatesProcessing.compareResponses(rateEntityNew.getBody(), rateEntityOld.getBody(), pcc);
          //System.out.println("AB1"+" "+chain+" "+ratePlanCode+" => "+result);

        }
      }
    }
  }

  //1 pcc, 1 chain, 1 ratePlanCode
  public void compareAll() throws IOException {

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    com.travelport.rates.RateAccessRQ rateAccessRQ = new com.travelport.rates.RateAccessRQ();

    ArrayList<String> pccs = FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/pcc.csv");
    ArrayList<String> chains = FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/chains.csv");
    ArrayList<String> ratePlanCodes = FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/ratePlanCode.csv");

    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");

    for (String pcc : pccs) {

      rateAccessRQ.setPseudoCityCode(pcc);

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

          boolean result = RatesProcessing.compareResponses(rateEntityNew.getBody(), rateEntityOld.getBody(), pcc);
          System.out.println("AB1" + " " + chain + " " + ratePlanCode + " => " + result);
        }
      }
    }
  }

  public List<RateAccessRS> seeResponses(RateAccessRQ rateAccessRQ) throws IOException {
    List<RateAccessRS> results = new ArrayList<>();

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);
    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity,
        com.travelport.rates.RateAccessRS.class, 100);

    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity,
        com.travelport.rates.RateAccessRS.class, 100);

    results.add(rateEntityNew.getBody());
    results.add(rateEntityOld.getBody());

    boolean result = RatesProcessing.compareResponses(rateEntityNew.getBody(), rateEntityOld.getBody(), rateAccessRQ.getPseudoCityCode());

    System.out.println("->" + result);
    return results;
  }
}
