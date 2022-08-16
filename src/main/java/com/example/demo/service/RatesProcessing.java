package com.example.demo.service;

import com.travelport.rates.RateAccessCodeType;
import com.travelport.rates.RateAccessDetailType;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
public class RatesProcessing {

  public static boolean compareResponses(com.travelport.rates.RateAccessRS rateAccessRS1, com.travelport.rates.RateAccessRS rateAccessRS2, String pcc)
      throws IOException {
    if (rateAccessRS1.getStatus().getMessage().equals("Success") && rateAccessRS1.getStatus().getMessage().equals("Success")) {
      for (RateAccessDetailType rateAccessDetail : rateAccessRS1.getRateAccessDetails().getRateAccessDetail()) {
        for (RateAccessCodeType rateAccessCodeType : rateAccessDetail.getRateAccessCodes().getRateAccessCode()) {
          if (getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(), rateAccessCodeType.getRatePlanCode()) == null) {
            //System.out.println("PCC NOT FOUND IN OLD APP: " + rateAccessRS1.getPseudoCityCode() + " -> " + false);
           // log.info("NULL RATE DETAILS IN OLD APP: " + rateAccessRS1.getPseudoCityCode() + " "+rateAccessDetail.getChainCode()+" "+ rateAccessCodeType.getRatePlanCode()+" -> " + false);
            return false;
          }
          if (rateAccessCodeType.isUserAllowed() != getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
              rateAccessCodeType.getRatePlanCode()).isUserAllowed()) {
            log.info("DIFFERENT RESULT: " + pcc + " " + rateAccessDetail.getChainCode() + " "
                + rateAccessCodeType.getRatePlanCode() + " -> NEW- " + rateAccessCodeType.isUserAllowed() + " , OLD-"
                + getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
                rateAccessCodeType.getRatePlanCode()).isUserAllowed());

            return false;
          }
        }
      }
    } else {
      log.info(
          "UNSUCCESSFUL STATUS: " + pcc + " -> " + false + " NEW: " + rateAccessRS1.getStatus().getMessage() + " OLD: " + rateAccessRS2.getStatus()
              .getMessage());

      return false;
    }
    return true;

  }

  //TO ACCESS THE UserAllowed VALUE FROM THE OTHER RateAccessRS
  public static RateAccessCodeType getRateAccessCodeTypeFromRateAccessRS(RateAccessRS rateAccessRS, String chainCode, String ratePlanCode) {
    if (rateAccessRS.getRateAccessDetails() == null) {
      return null;
    }
    RateAccessCodeType result=null;
    try{
     result=rateAccessRS.getRateAccessDetails().getRateAccessDetail().stream().filter(r -> {
               if (!r.getChainCode().equals(chainCode)) {
                 return false;
               }
               r.getRateAccessCodes().getRateAccessCode().stream().filter(ra -> ra.getRatePlanCode().equals(ratePlanCode));
               return true;
             }
         ).collect(Collectors.toList()).get(0).getRateAccessCodes().getRateAccessCode().stream().filter(rac -> rac.getRatePlanCode().equals(ratePlanCode))
         .collect(Collectors.toList()).get(0);
    }catch(Exception exception){
      log.info("NULL RATE DETAILS IN OLD APP:"+rateAccessRS.getPseudoCityCode()+" "+chainCode+" "+ratePlanCode+" * "+rateAccessRS.getRateAccessDetails().getRateAccessDetail().get(0).getRateAccessCodes().getRateAccessCode().get(0).getRatePlanCode());
    }
    return result;
  }
}
