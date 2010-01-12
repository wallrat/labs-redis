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
package se.preemptive.redis.testing.bench;

import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import se.preemptive.redis.testing.BenchRedisClient;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JRedisGet extends BenchTest
  {
    protected final JRedisPipeline pipeline;
    //private final JRedisAsynchClient pipeline = new JRedisAsynchClient(connectionSpec);

    protected final String key;
    protected final String value;

    Future o = null;

    // kill JRedis logging
    static
    {
      System.setOut(new PrintStream(System.out) {
        @Override
        public PrintStream format(String format, Object... args)
        {
          if (format.startsWith("--"))
            return this;
          return super.format(format, args);
        }
      });
    }

    public JRedisGet(int keySize, int valueSize, Map<String, String> options)
      throws ExecutionException, InterruptedException
    {
      super(keySize, valueSize, options);

      ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec(options.get("h"),Integer.parseInt(options.get("p")), 10, "jredis".getBytes());

      pipeline = new JRedisPipeline(connectionSpec);
      pipeline.flushdb().get();

      key = createString(keySize);
      value = createString(valueSize);

      pipeline.set(key, value).get();
    }

    @Override
    public void teardown()
    {
      pipeline.sync().quit();
    }

    @Override
    public Future run()
    {
      o = pipeline.get(key);
      //o = pipeline.ping();
      return o;
    }
  }
