package com.example.demo.model;

import com.travelport.rates.ChainCodesType;
import com.travelport.rates.RatePlanCodesType;
import java.util.List;
import lombok.Data;

@Data
public class RatesStructure {

  private String pcc;
  private ChainCodesType chains;
  private RatePlanCodesType ratePlanCodes;

}
