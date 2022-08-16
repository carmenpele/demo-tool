package com.example.demo.controller;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import com.example.demo.service.RatesService;
import com.travelport.rates.RateAccessRQ;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/rates")
public class RatesController {

  @Autowired
  private RatesService ratesService;
  @Autowired
  RestTemplate restTemplate;

  @PostMapping(value = "/test", consumes = APPLICATION_XML_VALUE, produces = APPLICATION_XML_VALUE)
  public ResponseEntity<com.travelport.rates.RateAccessRS> saveRate(@RequestBody com.travelport.rates.RateAccessRQ rateAccessRQ) {
    System.out.println("***********test1**************");
    return new ResponseEntity<>(ratesService.buildResponse(rateAccessRQ), HttpStatus.OK);
  }

  
  @PostMapping(value = "/compareResponses")
  public void seeAllStructures() throws IOException {
    ratesService.compare();
  }
  @PostMapping(value="/seeResponses")
  public List<RateAccessRS> seeResponses(@RequestBody RateAccessRQ rateAccessRQ) throws IOException {
    return ratesService.seeResponses(rateAccessRQ);
  }

}
