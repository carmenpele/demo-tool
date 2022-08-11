package com.example.demo.service;

import com.example.demo.model.RatesStructure;
import com.travelport.rates.RateAccessRQ;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RatesThread implements Runnable {

  private Thread thread;
  private String threadName;
  private List<String> pccs;
  private List<List<String>> chains;
  private List<List<String>> ratePlanCodes;

  public RatesThread(String threadName, List<String> pccs, List<List<String>> chains, List<List<String>> ratePlanCodes) {
    this.pccs = pccs;
    this.chains = chains;
    this.ratePlanCodes = ratePlanCodes;
    this.threadName = threadName;
  }


  @Override
  public void run() {
    System.out.println(threadName + " has started");
    List<RatesStructure> rates = new ArrayList<RatesStructure>();
    RatesStructure rate = new RatesStructure();

    for (String pcc : pccs) {
      rate.setPcc(pcc);
      for (List<String> chain : chains) {

        com.travelport.rates.ChainCodesType chainCodesType = new com.travelport.rates.ChainCodesType();
        //System.out.println("** "+chain);
        for (String s : chain) {
          chainCodesType.getChainCode().add(s);
        }
        rate.setChains(chainCodesType);

        for (List<String> ratePlanCode : ratePlanCodes) {
          //System.out.println("*** "+ratePlanCode);
          com.travelport.rates.RatePlanCodesType ratePlanCodesType = new com.travelport.rates.RatePlanCodesType();
          for (String s : ratePlanCode) {
            ratePlanCodesType.getRatePlanCode().add(s);
          }
          rate.setRatePlanCodes(ratePlanCodesType);
          try {
            requestProcessing(rate);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          rates.add(rate);
          //System.out.println(threadName+"---- PPC "+rate.getPcc()+" CHAINS "+rate.getChains().getChainCode()+" RATE PLAN CODES "+ rate.getRatePlanCodes().getRatePlanCode());

        }
      }
    }

//    for(RatesStructure r:rates){
//      System.out.println(threadName+"---- PPC "+r.getPcc()+" CHAINS "+r.getChains().getChainCode()+" RATE PLAN CODES "+ r.getRatePlanCodes().getRatePlanCode());
//    }
    System.out.println(threadName + " is finished");
  }

  public void requestProcessing(RatesStructure rate) throws IOException {
    RateAccessRQ rateAccessRQ = new RateAccessRQ();
    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");
    rateAccessRQ.setPseudoCityCode(rate.getPcc());
    rateAccessRQ.setChainCodes(rate.getChains());
    rateAccessRQ.setRatePlanCodes(rate.getRatePlanCodes());

    RestTemplate restTemplate = new RestTemplate();
    //String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    String urlNew = "http://localhost:8045/rates";
    //String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    String urlOld = "http://localhost:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    FileWrite fileWrite = new FileWrite();

    HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);

    long localStartEntityNew = System.currentTimeMillis();
    ResponseEntity<RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
    //System.out.println("primul call- entityNew- " + (System.currentTimeMillis()-localStartEntityNew));

    long localStartEntityOld = System.currentTimeMillis();
    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity,
        com.travelport.rates.RateAccessRS.class, 100);
    //System.out.println("al doilea call- entityOld- " + (System.currentTimeMillis()-localStartEntityOld));

    boolean result = RatesProcessing.compareResponses(rateEntityNew.getBody(), rateEntityOld.getBody(), fileWrite, rate.getPcc());
    //System.out.println(threadName+" : "+rate.getPcc()+" "+rate.getChains().getChainCode()+" "+rate.getRatePlanCodes().getRatePlanCode()+" -> "+result);
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this, threadName);
      thread.start();

    }
  }

}
