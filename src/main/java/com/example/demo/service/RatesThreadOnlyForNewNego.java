package com.example.demo.service;

import com.travelport.rates.ChainCodesType;
import com.travelport.rates.RateAccessCodeType;
import com.travelport.rates.RateAccessDetailType;
import com.travelport.rates.RateAccessRQ;
import com.travelport.rates.RateAccessRS;
import com.travelport.rates.RatePlanCodesType;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Log4j2
public class RatesThreadOnlyForNewNego implements Runnable {

  private Thread thread;
  private String threadName;
  private List<String> pccs;
  private List<String> chains;
  private List<String> ratePlanCodes;

  public RatesThreadOnlyForNewNego(String threadName, List<String> pccs, List<String> chains, List<String> ratePlanCodes) {
    this.pccs = pccs;
    this.chains = chains;
    this.ratePlanCodes = ratePlanCodes;
    this.threadName = threadName;
  }


  @Override
  public void run() {
    //System.out.println(threadName + " has started");
    //log.info("---------------thread " + threadName + " has started");
    long localStart = System.currentTimeMillis();

    Random random = new Random();

    for (String pcc : pccs) {
      ChainCodesType chainCodes = new ChainCodesType();
      RatePlanCodesType ratePlans = new RatePlanCodesType();

      while (chainCodes.getChainCode().size() < 5) {
        int position = random.nextInt(chains.size());
        if (!chainCodes.getChainCode().contains(chains.get(position))) {
          chainCodes.getChainCode().add(chains.get(position));
        }
      }
      while (ratePlans.getRatePlanCode().size() < 5) {
        int position = random.nextInt(ratePlanCodes.size());
        if (!ratePlans.getRatePlanCode().contains(ratePlanCodes.get(position))) {
          ratePlans.getRatePlanCode().add(ratePlanCodes.get(position));
        }
      }

      try {
        requestProcessing(pcc, chainCodes, ratePlans);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }
    //log.info("---------------thread " + threadName + " execution time {}ms", System.currentTimeMillis() - localStart);
    //System.out.println(threadName + " is finished");
  }

  public void requestProcessing(String pcc, ChainCodesType chainCodes, RatePlanCodesType ratePlanCodes) throws IOException {
    RateAccessRQ rateAccessRQ = new RateAccessRQ();
    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");
    rateAccessRQ.setPseudoCityCode(pcc);
    rateAccessRQ.setChainCodes(chainCodes);
    rateAccessRQ.setRatePlanCodes(ratePlanCodes);

    RestTemplate restTemplate = new RestTemplate();
    //String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    //String urlNew = "http://localhost:8045/rates";
    String urlNew = "https://hotel-at-negotiated-rates-hccd-pn.ocp-a.hc1.prod.travelport.io/hotel-at-negotiated-rates/rates";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    HttpEntity<RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);
    ResponseEntity<RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, RateAccessRS.class, 100);

    if(rateEntityNew.getBody().getStatus().getMessage().equals("Success")) {
      RateAccessRS rateAccessRS=rateEntityNew.getBody();

      for (RateAccessDetailType rateAccessDetail : rateAccessRS.getRateAccessDetails().getRateAccessDetail()) {
        for (RateAccessCodeType rateAccessCodeType : rateAccessDetail.getRateAccessCodes().getRateAccessCode()) {
          if (rateAccessCodeType.isUserAllowed()){
            System.out.println("pcc " + pcc
                + " chain " + rateAccessDetail.getChainCode()
                + " ratePlanCode " + rateAccessCodeType.getRatePlanCode()
                +" user_allowed="+ rateAccessCodeType.isUserAllowed());
          }
        }
      }

   }
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this, threadName);
      thread.start();

    }
  }

}
