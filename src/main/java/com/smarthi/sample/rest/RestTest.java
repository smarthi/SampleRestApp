package com.smarthi.sample.rest;

import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RestTest {

  private static final int MAX_THREADS_IN_POOL = 10;

  private HttpServer server;
  private WebTarget target;
  private ExecutorService executorService;

  public static void main(String[] args) throws Exception {
    RestTest restTest = new RestTest();
    restTest.init();
    restTest.testPost();
    restTest.testGetSample();
    restTest.testGet();
    restTest.testDelete();
    restTest.stop();
  }

  private void init() {
    // start the server
    server = Main.startServer();
    // create the client
    Client c = ClientBuilder.newClient();
    target = c.target(Main.BASE_URI);
    executorService = Executors.newFixedThreadPool(MAX_THREADS_IN_POOL);
  }

  public void testPost() {
    List<ResponsePostCallable> responseList = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      responseList.add(new ResponsePostCallable(target, new Sample(i, "Suneel " + i, "Delhi")));
    }
    try {
      long startTime = System.nanoTime();
      this.executorService.invokeAll(responseList);
      long stopTime = System.nanoTime();
      System.out.println("Create Samples time = " + (stopTime - startTime) / 1.0e6 + "\n");
    } catch (InterruptedException e) {
      // do nothing
    }
  }

  public void testGetSample() {
    GenericType<List<Sample>> list = new GenericType<List<Sample>>() {
    };
    long startTime = System.nanoTime();
    List<Sample> sampleList = target.path("myresource/sampleList").request().accept(MediaType.APPLICATION_JSON_TYPE).get(list);
    System.out.println(sampleList.toString());
    long stopTime = System.nanoTime();
    System.out.println("Fetch all samples time = " + (stopTime - startTime) / 1.0e6);
  }

  public void testGet() {
    List<ResponseGetCallable> responseList = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      responseList.add(new ResponseGetCallable(target, getRandom()));
    }

    try {
      long startTime = System.nanoTime();
      this.executorService.invokeAll(responseList);
      long stopTime = System.nanoTime();
      System.out.println("Fetch time = " + (stopTime - startTime) / 1.0e6 + "\n");
    } catch (InterruptedException e) {
      // do nothing
    }
  }

  public void testDelete() {
    long startTime = System.nanoTime();
    Response responseMsg = target.path("myresource/sample").path(String.valueOf(getRandom())).request().accept(MediaType.APPLICATION_JSON_TYPE).delete();
    System.out.println(responseMsg.getStatus());
    long stopTime = System.nanoTime();
    System.out.println("Delete time = " + (stopTime - startTime) / 1.0e6);
  }

  public void stop() {
    this.executorService.shutdown();
    try {
      if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        System.out.println("Threadpool timed out on await termination");
      }
    } catch (InterruptedException e) {
      // do nothing
    }
    server.shutdown();
  }

  private int getRandom() {
    return (new Random()).nextInt(10);
  }
}
