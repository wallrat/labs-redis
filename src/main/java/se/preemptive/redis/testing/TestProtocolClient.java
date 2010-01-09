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

import se.preemptive.redis.RedisProtocolClient;
import se.preemptive.redis.ResponseFuture;

import java.util.concurrent.ExecutionException;

public class TestProtocolClient
{
  public static void main(String[] args)
    throws ExecutionException, InterruptedException
  {
    RedisProtocolClient c = new RedisProtocolClient(null);
    c.connect();
    ResponseFuture future = c.send("PING");
    System.out.println(future.get());
    c.disconnect();
  }
}
