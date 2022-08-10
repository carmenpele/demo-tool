package com.example.demo.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import com.example.demo.model.CombinationsWanted;
import com.example.demo.service.FileWrite;
import com.example.demo.service.RatesService;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/rates")
public class RatesController {
  @Autowired
  private RatesService ratesService;
  @Autowired
  RestTemplate restTemplate;

  @PostMapping(value="/test",consumes = APPLICATION_XML_VALUE, produces=APPLICATION_XML_VALUE)
  public ResponseEntity<com.travelport.rates.RateAccessRS> saveRate(@RequestBody com.travelport.rates.RateAccessRQ rateAccessRQ){
    System.out.println("***********test1**************");
    return new ResponseEntity<>(ratesService.buildResponse(rateAccessRQ),HttpStatus.OK);
  }
  @RequestMapping(value="/testRates", method = RequestMethod.POST)
  public List<RateAccessRS> callRates(){
    com.travelport.rates.RateAccessRQ rateAccessRQ=new com.travelport.rates.RateAccessRQ();

    com.travelport.rates.ChainCodesType chainCodesType=new com.travelport.rates.ChainCodesType();
    chainCodesType.getChainCode().add("AL");
    rateAccessRQ.setChainCodes(chainCodesType);

    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");
    rateAccessRQ.setPseudoCityCode("W5P");

    com.travelport.rates.RatePlanCodesType ratePlanCodesType=new com.travelport.rates.RatePlanCodesType();
    ratePlanCodesType.getRatePlanCode().add("TRW");
    ratePlanCodesType.getRatePlanCode().add("ATFA");
    rateAccessRQ.setRatePlanCodes(ratePlanCodesType);

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "http://localhost:8045/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    HttpEntity<com.travelport.rates.RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);
    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
    List<RateAccessRS> rateAccessRSList=new ArrayList<RateAccessRS>();
    rateAccessRSList.add(rateEntityNew.getBody());
    rateAccessRSList.add(rateEntityOld.getBody());
    return rateAccessRSList;

  }
  @RequestMapping(value="/compareRates", method = RequestMethod.POST)
  public boolean compareRates() throws IOException {
    com.travelport.rates.RateAccessRQ rateAccessRQ=new com.travelport.rates.RateAccessRQ();

    com.travelport.rates.ChainCodesType chainCodesType=new com.travelport.rates.ChainCodesType();
    chainCodesType.getChainCode().add("AL");
    rateAccessRQ.setChainCodes(chainCodesType);

    rateAccessRQ.setGdsContext("1G");
    rateAccessRQ.setCorrelationId("tesssst");
    rateAccessRQ.setPseudoCityCode("W5P");

    com.travelport.rates.RatePlanCodesType ratePlanCodesType=new com.travelport.rates.RatePlanCodesType();
    ratePlanCodesType.getRatePlanCode().add("TRW");
    ratePlanCodesType.getRatePlanCode().add("ATFA");
    rateAccessRQ.setRatePlanCodes(ratePlanCodesType);

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    HttpEntity<com.travelport.rates.RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);

    long localStartEntityNew = System.currentTimeMillis();

    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);

    System.out.println("primul call- entityNew- " + (System.currentTimeMillis()-localStartEntityNew));

    long localStartEntityOld = System.currentTimeMillis();

    ResponseEntity<com.travelport.rates.RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);

    System.out.println("al doilea call- entityOld- " + (System.currentTimeMillis()-localStartEntityOld));

    FileWrite fileWrite=new FileWrite();

    boolean result=ratesService.compareResponses(rateEntityNew.getBody(),rateEntityOld.getBody(),fileWrite,"");

    fileWrite.close();
    return result;
  }

  @PostMapping(value="/compareRatesRandom", consumes = APPLICATION_JSON_VALUE)
  public boolean compareRatesRandom(@RequestBody CombinationsWanted combinationsWanted) throws IOException {
    com.travelport.rates.RateAccessRQ rateAccessRQ=ratesService.generateRateAccessRQ(combinationsWanted);

    FileWrite fileWrite=new FileWrite();

    RestTemplate restTemplate = new RestTemplate();
    String urlNew = "https://hotel-at-negotiated-rates-hccd-dev.ocp-a.hc1.nonprod.travelport.io:443/hotel-at-negotiated-rates/rates";
    String urlOld = "http://vhlppdobe059.tvlport.net:50054/rates";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    HttpEntity<com.travelport.rates.RateAccessRQ> entity = new HttpEntity<>(rateAccessRQ, headers);
    ResponseEntity<RateAccessRS> rateEntityNew = restTemplate.exchange(urlNew, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
    ResponseEntity<RateAccessRS> rateEntityOld = restTemplate.exchange(urlOld, HttpMethod.POST, entity, com.travelport.rates.RateAccessRS.class, 100);
    boolean result = ratesService.compareResponses(rateEntityNew.getBody(),rateEntityOld.getBody(),fileWrite,"");
    fileWrite.close();
    return result;
  }

  @PostMapping(value="/compareAllTheCodes")
  public void compareAll() throws IOException {
    ratesService.compareAll();
  }
  @PostMapping(value="/compareAllCustom")
  public void compareAllCustom() throws IOException {
    ratesService.compareWithCustomNumbersOfCodes(10,8);
  }

//  @PostMapping(value="/getResponseFromRates",consumes = APPLICATION_XML_VALUE)
//  public RateAccessRS getResponseFromRates(@RequestBody RateAccessRQ rateAccessRQ){
//    return ratesService.responseFromNegoRate(rateAccessRQ);
//  }

}
