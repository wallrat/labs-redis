/*
 * Copyright (c) 2010 Preemptive Labs / Andreas Bielk (http://preemptive.se)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.preemptive.redis.testing;

import se.preemptive.redis.testing.bench.BenchTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class BenchRedisClient
{
  private long testSumItems = 0;
  private long testSumTaken = 0;

  private static Map<String, String> options = new HashMap<String, String>();

  static
  {
    // setup some default options
    options.put("h", "127.0.0.1");
    options.put("p", "6379");

    options.put("key", "16");
    options.put("value", "16");

    options.put("l", "1");
    options.put("t", "1");
    options.put("n", "100000");

  }

  public static void main(String[] args)
    throws Exception
  {
    if (args.length == 0 || args.length == 1 && "--help".equals(args[0]))
    {
      System.out.println("Usage: BenchRedisClient [-h <host>] [-p <port>] [-key <length>] [-value <length>] [-n <requests>] [-t <threads>] [-l <loops>] <classname(s)>");
      System.out.println("");
      System.out.println("<classname(s)>      One or more tests to run (use , as separator)");
      System.out.println("");
      System.out.println("-h <host>           Server hostname (default 127.0.0.1)");
      System.out.println("-p <port>           Server port (default 6379)");
      System.out.println("-t <threads>        Run parallel tests in n threads (total requests = requests * threads) (default 1)");
      System.out.println("-n <requests>       Run n requests in each test (default 100000)");
      System.out.println("-l <loops>          Run all tests n times. Use 0 to loop tests forever (default 1)");
      System.out.println("-key <length>       Key lengths in bytes (default 16)");
      System.out.println("-value <size>       Value sizes in bytes (default 16)");
      return;
    }

    // parse options
    parseOptions(args);
    System.out.println("Options: "+options);

    // Headlines
    System.out.printf("%-10s %8s %6s %15s  %5s  %15s  %15s\n",
      "",
      "", "Load",
      "Write",
      "Read",
      "Complete",
      "Average");

    System.out.printf("%-10s %8s %6s " +
      "%5s %9s  " +
      "%5s  " +
      "%5s %9s  " +
      "%5s %9s  " +
      "\n",
      "test", "threads", "items",
      "ms", "req/s",
      "ms",
      "ms", "req/s",
      "ms", "req/s"
    );

    BenchRedisClient bench = new BenchRedisClient();

    // options
    final String[] testNames = args[args.length-1].split(",");
    final int requests = Integer.parseInt(options.get("n"));
    final int loops = Integer.parseInt(options.get("l"));
    final int threads = Integer.parseInt(options.get("t"));

    for (String testName : testNames)
      for (int i = 0; i < loops; i++)
      {
        bench.runTest(testName, createTest(testName), threads, requests);

        // skip first run
        if (i <= 0)
        {
          bench.testSumTaken = 0;
          bench.testSumItems = 0;
          System.out.println();
        }
        else
        {
          // rolling stats
          double ms = bench.testSumTaken / (double) (i + 1);
          double itemspers = bench.testSumItems / (bench.testSumTaken / 1000.0);
          System.out.printf("%5.0f %8.0f  ", ms, itemspers);
          System.out.println();
        }

        // cleanup
        System.gc();
        Thread.sleep(1000);
        System.gc();
      }
  }


  class TestWorker implements Runnable
  {
    private final BenchTest test;

    private final int items;
    private final CountDownLatch startLatch;
    private final CountDownLatch doneWritingLatch;
    private final CountDownLatch doneLatch;

    TestWorker(BenchTest test, int items, CountDownLatch startLatch, CountDownLatch doneWritingLatch, CountDownLatch doneLatch)
    {
      this.test = test;
      this.items = items;
      this.startLatch = startLatch;
      this.doneWritingLatch = doneWritingLatch;
      this.doneLatch = doneLatch;
    }

    @Override
    public void run()
    {
      String threadName = Thread.currentThread().getName();

      try
      {
        startLatch.await();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
        return;
      }

      Future resp = null;

      for (int i = 0; i < items; i++)
        resp = test.run();

      doneWritingLatch.countDown();

      Object o = null;
      try
      {
        //o = resp.get((long) Math.min(items, 30), TimeUnit.SECONDS);
        o = resp.get((long) Math.min(items, 30), TimeUnit.MINUTES);
      }
      catch (TimeoutException e)
      {
        System.out.println(threadName + ">> Timed out");
        return;
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      catch (ExecutionException e)
      {
        e.printStackTrace();
      }

      if (o == Boolean.FALSE)
        System.out.println(o);

      doneLatch.countDown();
    }
  }

  private void runTest(String testName, BenchTest test, int threads, int items)
    throws InterruptedException
  {
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch doneWriting = new CountDownLatch(threads);
    final CountDownLatch doneReading = new CountDownLatch(threads);

    // report test and load
    System.out.printf("%-10s %8d %6d ", testName, threads, items);

    test.setup();

    for (int i = 0; i < threads; i++)
    {
      Thread t = new Thread(new TestWorker(test, items, start, doneWriting, doneReading));
      t.setName("TestThread[" + i + "]");
      t.start();
    }

    final double totalitems = items * threads;
    long started = System.currentTimeMillis();

    // start test
    start.countDown();

    // wait for all cmds sent
    doneWriting.await();
    long writeEnded = System.currentTimeMillis();

    // wait for all responses done
    doneReading.await();
    long readEnded = System.currentTimeMillis();

    // stats for write
    {
      long took = writeEnded - started;
      double itemspers = totalitems / (took / 1000.0); // 1000000000.0
      System.out.printf("%5d %9.0f  ", took, itemspers);
    }

    // stats for read
    {
      long took = readEnded - writeEnded;
      //double itemspers = totalitems / (took / 1000.0);
      System.out.printf("%5d  ", took);
    }

    // stats for write + read
    long took = System.currentTimeMillis() - started;
    double itemspers = totalitems / (took / 1000.0);
    System.out.printf("%5d %9.0f  ", took, itemspers);

    test.teardown();

    testSumItems += totalitems;
    testSumTaken += took;
  }


  private static BenchTest createTest(String testName)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
    String cName = testName.indexOf('.') == -1 ? "se.preemptive.redis.testing.bench." + testName : testName;

    @SuppressWarnings({"unchecked"})
    Class<BenchTest> aClass = (Class<BenchTest>) Class.forName(cName);
    Constructor<BenchTest> testConstructor = aClass.getConstructor(int.class, int.class, Map.class);
    int keySize = Integer.parseInt(options.get("key"));
    int valueSize = Integer.parseInt(options.get("value"));
    return testConstructor.newInstance(keySize,valueSize,options);
  }


    private static void parseOptions(String args[])
  {
    if (args.length < 1)
      return;

    String val = null;
    for (int i=args.length-1; i > -1; i--)
    {
      if (args[i].startsWith("-"))
      {
        String option = args[i].substring(1, args[i].length());

        if (val != null)
          options.put(option,val);
        else
          options.put(option,"true");

        val = null;
      }
      else
        val = args[i];
    }
  }

}