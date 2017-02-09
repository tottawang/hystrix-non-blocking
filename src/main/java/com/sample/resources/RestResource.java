package com.sample.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sample.service.HystrixService;

import rx.Observable;
import rx.Observer;

@Component
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
public class RestResource {

  private static int POOL_SIZE = 5;
  private static final ExecutorService workers = Executors.newFixedThreadPool(POOL_SIZE);

  @Autowired
  private HystrixService service;

  @GET
  @Path("hystrix-blocking")
  public String getUserProjectsBlocking() {
    String result = "";
    long start = System.currentTimeMillis();
    result = service.getContent();
    long end = System.currentTimeMillis();
    System.out.println("Time taken to get results " + (end - start) + " milliseconds");
    return result;
  }

  @GET
  @Path("hystrix-blocking-loop")
  public String getUserProjectsBlockingLoop() {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 30; i++) {
      getUserProjectsBlocking();
    }
    long end = System.currentTimeMillis();
    System.out.println("###LOOP - Time taken to get results " + (end - start) + " milliseconds");
    return "";
  }

  @GET
  @Path("hystrix-non-blocking-loop")
  public String getUserProjectsNonBlockingLoop() {
    for (int i = 0; i < 30; i++) {
      getUserProjectsNonBlocking();
    }
    return "";
  }

  @GET
  @Path("hystrix-non-blocking")
  public String getUserProjectsNonBlocking() {
    long start = System.currentTimeMillis();
    Observable<String> observableResult = service.getContentNonBlocking();
    observableResult.subscribe(new Observer<String>() {
      @Override
      public void onCompleted() {
        System.out.println(Thread.currentThread().getName() + ": " + "completed");
      }

      @Override
      public void onError(Throwable e) {
        System.out.println(Thread.currentThread().getName() + ": error: " + e.getMessage());
      }

      @Override
      public void onNext(String t) {
        // System.out.println(Thread.currentThread().getName() + ": " + "onNext");
      }
    });
    long end = System.currentTimeMillis();
    System.out.println("Time taken to get results " + (end - start) + " milliseconds");
    return observableResult.toString();
  }

  @GET
  @Path("hystrix-blocking-currency")
  public String getUserProjectsCurrency() throws InterruptedException, ExecutionException {

    String result = "";
    long start = System.currentTimeMillis();

    // call the service.getContent 30 times in parallel
    Collection<Callable<String>> tasks = new ArrayList<Callable<String>>();
    for (int i = 0; i < POOL_SIZE; i++) {
      tasks.add(new Callable<String>() {
        public String call() throws Exception {
          return service.getContent();
        }
      });
    }

    List<Future<String>> results = workers.invokeAll(tasks, 500, TimeUnit.SECONDS);
    for (Future<String> f : results) {
      result += f.get();
    }

    long end = System.currentTimeMillis();
    System.out.println("Time taken to get results " + (end - start) + " milliseconds");
    return result;
  }

}
