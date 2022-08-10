package com.example.demo;


import com.example.demo.service.FileReading;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CodesFromFiles {
  private ArrayList<String> pccs;
  private ArrayList<String> chains;
  private ArrayList<String> ratePlanCodes;

  public static ArrayList<String> readPCCs() throws IOException {
    return FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/pcc.csv");
  }

  public static ArrayList<String> readChains() throws IOException {
    return FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/chains.csv");
  }

  public static ArrayList<String> readRatePlanCodes() throws IOException {
    return FileReading.readFromFile("C:/Users/carmen.pele/Downloads/demo/demo/src/main/resources/ratePlanCode.csv");
  }
  public void initialize() throws IOException {
    pccs=readPCCs();
    chains=readChains();
    ratePlanCodes=readRatePlanCodes();
  }
  public static void main(String[] args) throws IOException {
    CodesFromFiles codes=new CodesFromFiles();
    codes.initialize();
    List<List<String>> chainsSplitted= Lists.partition(codes.chains, 10);
    System.out.println(chainsSplitted);
  }

}
