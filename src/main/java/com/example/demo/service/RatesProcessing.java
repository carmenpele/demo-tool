package com.example.demo.service;

import com.travelport.rates.RateAccessCodeType;
import com.travelport.rates.RateAccessDetailType;
import com.travelport.rates.RateAccessRS;
import java.io.IOException;
import java.util.stream.Collectors;

public class RatesProcessing {

  public static boolean compareResponses(com.travelport.rates.RateAccessRS rateAccessRS1, com.travelport.rates.RateAccessRS rateAccessRS2,
      FileWrite fileWrite, String pcc)
      throws IOException {
    if (rateAccessRS1.getStatus().getMessage().equals("Success") && rateAccessRS1.getStatus().getMessage().equals("Success")) {
      for (RateAccessDetailType rateAccessDetail : rateAccessRS1.getRateAccessDetails().getRateAccessDetail()) {
        for (RateAccessCodeType rateAccessCodeType : rateAccessDetail.getRateAccessCodes().getRateAccessCode()) {
          if (getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(), rateAccessCodeType.getRatePlanCode()) == null) {
            System.out.println("PCC NOT FOUND IN OLD APP: " + rateAccessRS1.getPseudoCityCode() + " -> " + false);
            fileWrite.writeText("PCC NOT FOUND IN OLD APP: " + rateAccessRS1.getPseudoCityCode() + " -> " + false);
            return false;
          }
          if (rateAccessCodeType.isUserAllowed() != getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
              rateAccessCodeType.getRatePlanCode()).isUserAllowed()) {
            System.out.println("DIFFERENT RESULT: " + rateAccessRS1.getPseudoCityCode() + " " + rateAccessDetail.getChainCode() + " "
                + rateAccessCodeType.getRatePlanCode() + " -> " + rateAccessCodeType.isUserAllowed()+" , "+getRateAccessCodeTypeFromRateAccessRS(rateAccessRS2, rateAccessDetail.getChainCode(),
                rateAccessCodeType.getRatePlanCode()).isUserAllowed());
            fileWrite.writeText(
                rateAccessRS1.getPseudoCityCode() + " " + rateAccessDetail.getChainCode() + " " + rateAccessCodeType.getRatePlanCode() + " -> "
                    + false + "\n");
            return false;
          }
        }
      }
    } else {
      System.out.println("UNSUCCESSFULL STATUS: " + pcc + " -> " + false + " " + rateAccessRS1.getStatus().getMessage() + "\n");
      fileWrite.writeText("UNSUCCESSFULL STATUS: " + pcc + " -> " + false + " " + rateAccessRS1.getStatus().getMessage() + "\n");
      return false;
    }
    return true;

  }

  //TO ACCESS THE UserAllowed VALUE FROM THE OTHER RateAccessRS
  public static RateAccessCodeType getRateAccessCodeTypeFromRateAccessRS(RateAccessRS rateAccessRS, String chainCode, String ratePlanCode) {
    if (rateAccessRS.getRateAccessDetails() == null) {
      return null;
    }
    return rateAccessRS.getRateAccessDetails().getRateAccessDetail().stream().filter(r -> {
              if (!r.getChainCode().equals(chainCode)) {
                return false;
              }
              r.getRateAccessCodes().getRateAccessCode().stream().filter(ra -> ra.getRatePlanCode().equals(ratePlanCode));
              return true;
            }
        ).collect(Collectors.toList()).get(0).getRateAccessCodes().getRateAccessCode().stream().filter(rac -> rac.getRatePlanCode().equals(ratePlanCode))
        .collect(Collectors.toList()).get(0);
  }
}
