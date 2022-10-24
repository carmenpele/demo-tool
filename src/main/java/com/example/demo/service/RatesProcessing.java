package com.example.demo.service;

import com.travelport.rates.RateAccessCodeType;
import com.travelport.rates.RateAccessDetailType;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RatesProcessing {

  public static boolean compareResponses(com.travelport.rates.RateAccessRS rateAccessRS1, com.travelport.rates.RateAccessRS rateAccessRS2, String pcc)
      throws IOException {
    if (rateAccessRS1.getStatus().getMessage().equals("Success") && rateAccessRS2.getStatus().getMessage().equals("Success")) {
      for (RateAccessDetailType rateAccessDetail : rateAccessRS1.getRateAccessDetails().getRateAccessDetail()) {
        for (RateAccessCodeType rateAccessCodeType : rateAccessDetail.getRateAccessCodes().getRateAccessCode()) {
          if (getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(), rateAccessCodeType.getRatePlanCode()) == null) {
            log.info("M1 MASTER CHAIN PROBLEM : pcc- " + pcc + " chains ("
                + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getChainCode() + " "
                + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(1).getChainCode() + " "
                + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(2).getChainCode() + " "
                + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(3).getChainCode() + " "
                + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(4).getChainCode()
                + ") ratePlanCodes-(" + rateAccessDetail.getRateAccessCodes().getRateAccessCode().get(0).getRatePlanCode() + " "
                + rateAccessDetail.getRateAccessCodes().getRateAccessCode().get(1).getRatePlanCode() + " "
                + rateAccessDetail.getRateAccessCodes().getRateAccessCode().get(2).getRatePlanCode() + " "
                + rateAccessDetail.getRateAccessCodes().getRateAccessCode().get(3).getRatePlanCode() + " "
                + rateAccessDetail.getRateAccessCodes().getRateAccessCode().get(4).getRatePlanCode() + ")");
            return false;
          }
          if (rateAccessCodeType.isUserAllowed() != getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
                                                                                rateAccessCodeType.getRatePlanCode()).isUserAllowed()) {
            if ("MC,RC,FN,BR,ET,TO,XV,CY,VC,EE,AK,EB,AR,GE,OX,RZ,DE,PR,SI,WI,WH,MD,LC,XR,AL,EL,GX,TX".contains(rateAccessDetail.getChainCode())) {
              log.info("MARRIOTT CHAIN CODE : " + pcc + " " + rateAccessDetail.getChainCode() + " "
                  + rateAccessCodeType.getRatePlanCode() + " -> NEW- " + rateAccessCodeType.isUserAllowed() + " , OLD-"
                  + getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
                  rateAccessCodeType.getRatePlanCode()).isUserAllowed());
            } else if ("AN,CP,IC,IN,HI,SP,VN,YO,YZ,UL".contains(rateAccessDetail.getChainCode())) {
              log.info("IHG CHAIN CODE : " + pcc + " " + rateAccessDetail.getChainCode() + " "
                  + rateAccessCodeType.getRatePlanCode() + " -> NEW- " + rateAccessCodeType.isUserAllowed() + " , OLD-"
                  + getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
                  rateAccessCodeType.getRatePlanCode()).isUserAllowed());
            } else {
              log.info("DIFFERENT RESULT: " + pcc + " " + rateAccessDetail.getChainCode() + " "
                  + rateAccessCodeType.getRatePlanCode() + " -> NEW- " + rateAccessCodeType.isUserAllowed() + " , OLD-"
                  + getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
                  rateAccessCodeType.getRatePlanCode()).isUserAllowed());
            }
            return false;
          }
        }
      }
    } else {
//      log.info(
//          "UNSUCCESSFUL STATUS: " + pcc + " -> " + false + " NEW: " + rateAccessRS1.getStatus().getMessage() + " OLD: " + rateAccessRS2.getStatus()
//              .getMessage());

      return false;
    }
//    log.info(pcc + " chains ("
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getChainCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(1).getChainCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(2).getChainCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(3).getChainCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(4).getChainCode()
//        + ") ratePlanCodes-(" + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getRateAccessCodes().getRateAccessCode().get(0).getRatePlanCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getRateAccessCodes().getRateAccessCode().get(1).getRatePlanCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getRateAccessCodes().getRateAccessCode().get(2).getRatePlanCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getRateAccessCodes().getRateAccessCode().get(3).getRatePlanCode() + " "
//        + rateAccessRS1.getRateAccessDetails().getRateAccessDetail().get(0).getRateAccessCodes().getRateAccessCode().get(4).getRatePlanCode() + ") ->true");
    return true;

  }

  //TO ACCESS THE UserAllowed VALUE FROM THE OTHER RateAccessRS
  public static RateAccessCodeType getRateAccessCodeTypeFromRateAccessRS(RateAccessRS rateAccessRS, String chainCode, String ratePlanCode) {
    if (rateAccessRS.getRateAccessDetails() == null) {
      return null;
    }
    for (RateAccessDetailType rateAccessDetail : rateAccessRS.getRateAccessDetails().getRateAccessDetail()) {
      for (RateAccessCodeType rateAccessCodeType : rateAccessDetail.getRateAccessCodes().getRateAccessCode()) {
        if (chainCode.equals(rateAccessDetail.getChainCode()) && rateAccessCodeType.getRatePlanCode().equals(ratePlanCode)) {
          return rateAccessCodeType;
        }
      }
    }
    return null;
  }
}
