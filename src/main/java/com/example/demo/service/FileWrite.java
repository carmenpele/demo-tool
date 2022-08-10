package com.example.demo.service;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors

public class FileWrite {
  private FileWriter myWriter;
  public FileWrite() throws IOException {
    myWriter = new FileWriter("results.txt");
  }
  public void writeText(String s) throws IOException {
    myWriter.write(s);
  }
  public void close() throws IOException {
    myWriter.close();
  }
  public static void main(String[] args) {
    try {
     FileWrite fileWrite=new FileWrite();
     fileWrite.writeText("merge");
     fileWrite.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}