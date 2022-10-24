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

  /**
   * this is just to test if the endpoint works
   * @param rateAccessRQ
   * @return
   */
  @PostMapping(value = "/test", consumes = APPLICATION_XML_VALUE, produces = APPLICATION_XML_VALUE)
  public ResponseEntity<com.travelport.rates.RateAccessRS> saveRate(@RequestBody com.travelport.rates.RateAccessRQ rateAccessRQ) {
    System.out.println("***********test1**************");
    return new ResponseEntity<>(ratesService.buildResponse(rateAccessRQ), HttpStatus.OK);
  }

  /**
   * this method is to show the responses from both old and new Nego Rate
   * @param rateAccessRQ
   * @return
   * @throws IOException
   */
  @PostMapping(value = "/seeResponses")
  public List<RateAccessRS> seeResponses(@RequestBody RateAccessRQ rateAccessRQ) throws IOException {
    return ratesService.seeResponses(rateAccessRQ);
  }

  /**
   *  this method compares all the combinations from the lists, combining 1 pcc with 5 chainCodes and 5 ratePlanCodes
   * @param pccLink
   * @param chainsLink
   * @param ratePlanCodesLink
   * @throws IOException
   */
  @PostMapping(value = "/compare")
  public void compare(@RequestParam String pccLink, @RequestParam String chainsLink, @RequestParam String ratePlanCodesLink) throws IOException {
    ratesService.compare(pccLink, chainsLink, ratePlanCodesLink);
  }

  /**
   * this method will call the new nego rate endpoint for all the combination of pccs,chainCodes and ratePlanCodes,
   * and will only print in the console the cases with userAllowed=true
   * @param pccLink
   * @param chainsLink
   * @param ratePlanCodesLink
   * @throws IOException
   */
  @PostMapping(value = "/seeTrueCases")
  public void seeTrueCasesFromNew(@RequestParam String pccLink, @RequestParam String chainsLink, @RequestParam String ratePlanCodesLink) throws IOException {
    ratesService.seeOnlyUserAllowedCases(pccLink, chainsLink, ratePlanCodesLink);
  }

}
