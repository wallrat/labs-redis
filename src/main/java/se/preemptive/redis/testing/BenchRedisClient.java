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

import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import se.preemptive.redis.RedisClient;
import se.preemptive.redis.RedisProtocolClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class BenchRedisClient
{
  private static int KEY_SIZE = 16;
  private static int VALUE_SIZE = 16;

  private long testSumItems = 0;
  private long testSumTaken = 0;

  private static Map<String, String> options = new HashMap<String, String>();

  static
  {
    options.put("-h", "127.0.0.1");
    options.put("-p", "6379");

    options.put("-key", "16");
    options.put("-value", "16");

    options.put("-l", "1");
    options.put("-t", "1");
    options.put("-n", "100000");

  }

  public static void main(String[] args)
    throws Exception
  {
    if (args.length == 1 && "--help".equals(args[0]))
    {
      System.out.println("Usage: BenchRedisClient [-h <host>] [-p <port>] [-key <length>] [-value <length>] [-n <requests>] [-t <threads>] [-l <loops>] <classname>");
      System.out.println("");
      System.out.println("<classname>         One or more tests to run (default Get)");
      System.out.println("");
      System.out.println("-h <host>           Server hostname (default 127.0.0.1)");
      System.out.println("-p <port>           Server port (default 6379)");
      System.out.println("-t <threads>        Run paralell tests in n threads (total requests = requests * threads) (default 1)");
      System.out.println("-n <requests>       Run n requests in each test (default 100000)");
      System.out.println("-l <loops>          Run all tests n times. Use 0 to loop tests forever (default 1)");
      System.out.println("-key <length>       Key lengths in bytes (default 16)");
      System.out.println("-value <size>       Value sizes in bytes (default 16)");
      return;
    }

    // parse options
    System.out.println("Key size " + KEY_SIZE + " bytes.");
    System.out.println("Value size " + VALUE_SIZE + " bytes.");

    // Headlines
    System.out.printf("%-10s %8s %6s %15s  %15s  %15s  %15s\n",
      "",
      "", "Load",
      "Write",
      "Read",
      "Complete",
      "Average");

    System.out.printf("%-10s %8s %6s " +
      "%5s %9s  " +
      "%5s %9s  " +
      "%5s %9s  " +
      "%5s %9s  " +
      "\n",
      "test", "threads", "items",
      "ms", "req/s",
      "ms", "req/s",
      "ms", "req/s",
      "ms", "req/s"
    );

    BenchRedisClient bench = new BenchRedisClient();

    for (int i = 0; i < 10; i++)
    {
      bench.runTest("get", new GetSetTest(), 2, 500000);
      //bench.runTest("JR_get", new JRedisGetSetTest(), 1, 500000);

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

  static class JRedisGetSetTest extends BenchTest
  {
    private ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec("localhost", 6379, 10, "jredis".getBytes());
    private final JRedisPipeline pipeline = new JRedisPipeline(connectionSpec);
    //private final JRedisAsynchClient pipeline = new JRedisAsynchClient(connectionSpec);

    private final String key = createString(BenchRedisClient.KEY_SIZE);

    Future o = null;

    JRedisGetSetTest()
      throws ExecutionException, InterruptedException
    {
      pipeline.flushdb().get();
      pipeline.set(key, createString(BenchRedisClient.VALUE_SIZE)).get();
    }

    @Override
    void teardown()
    {
      pipeline.sync().quit();
      //pipeline.quit();
      //try
      //{
      //  System.out.println("o = " + o.get());
      //}
      //catch (InterruptedException e)
      //{
      //  e.printStackTrace();
      //}
      //catch (ExecutionException e)
      //{
      //  e.printStackTrace();
      //}
    }

    @Override
    public Future run()
    {
      o = pipeline.get(key);
      //o = pipeline.ping();
      return o;
    }
  }

  static class GetSetTest extends BenchTest
  {
    // setup test data
    //private final ChannelBuffer value = copiedBuffer("asdasdasdasdasdasdasd", "UTF-8");
    //private final ChannelBuffer setKeyPart = copiedBuffer("SET " + "mykey" + " " + value.readableBytes() + "\r\n", "UTF-8");
    //private final ChannelBuffer staticSetCommand = ChannelBuffers.wrappedBuffer(setKeyPart, value, FrameDecoder.CRLF);

    private final RedisClient client = new RedisClient(new RedisProtocolClient());

    private final String key = createString(BenchRedisClient.KEY_SIZE);
    private final String value = createString(BenchRedisClient.VALUE_SIZE);

    GetSetTest()
    {
      client.getProtocolClient().connect();
      client.select(10);
      client.flushdb();
      client.set(key, value).block();
    }

    public Future run()
    {
      //return client.ping();
      //return client.get(key);
      return client.set(key, value);
      //resp = q.set("mykey2", value);
      //resp = q.set2("mykey2", value);
      //resp = q.getProtocolClient().test();

      //ChannelBuffer setCommand = new ReadonlyCompositeChannelBuffer(ByteOrder.BIG_ENDIAN, setKeyPart, value, FrameDecoder.CRLF);
      //ChannelBuffer staticSetCommand = ChannelBuffers.wrappedBuffer(setKeyPart, value, FrameDecoder.CRLF);
      //resp = q.getProtocolClient().send(setCommand);
      //resp = q.getProtocolClient().send(staticSetCommand);
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
      double itemspers = totalitems / (took / 1000.0);
      System.out.printf("%5d %9.0f  ", took, itemspers);
    }

    // stats for write + read
    long took = System.currentTimeMillis() - started;
    double itemspers = totalitems / (took / 1000.0);
    System.out.printf("%5d %9.0f  ", took, itemspers);

    test.teardown();

    testSumItems += totalitems;
    testSumTaken += took;
  }


  static abstract class BenchTest
  {
    String createString(int size)
    {
      byte[] b = new byte[size];
      b[0] = 'S';
      b[size - 1] = 'E';
      Arrays.fill(b, (byte) 'a');
      return new String(b);
    }

    void setup() {}

    void teardown() {}

    abstract Future run();
  }
}