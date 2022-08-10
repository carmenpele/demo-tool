package com.example.demo.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileReading {

  public static ArrayList<String> readFromFile(String path) throws IOException {
    ArrayList<String> list = new ArrayList<>();
    String pathToCsv = path;
    String row;
    BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
    while ((row = csvReader.readLine()) != null) {
      list.add(row);
    }
    csvReader.close();
    return list;
  }
}
