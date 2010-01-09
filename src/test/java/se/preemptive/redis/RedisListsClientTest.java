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

package se.preemptive.redis;

import org.jboss.netty.buffer.ChannelBuffers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import se.preemptive.redis.commands.RedisListsCommands;
import se.preemptive.redis.commands.RedisStringCommands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertNull;

@Test
public class RedisListsClientTest
{
  private RedisListsCommands client = new RedisListsCommands(new RedisProtocolClient());

  @BeforeTest
  public void initClient()
  {
    assert client.select(10).asBoolean();
    assert client.flushdb().asBoolean();
  }

  public void testLeftPushPop()
    throws TimeoutException, ExecutionException, InterruptedException
  {
    assert client.flushdb().asBoolean();

    client.lpush("key","v1").withTimeout(1, SECONDS);
    client.lpush("key","v2").withTimeout(1, SECONDS);

    assert 2 == client.llen("key").withTimeout(1, SECONDS).asInteger();
    
    assert "v2".equals(client.lpop("key").withTimeout(1, SECONDS).asString());
    assert "v1".equals(client.lpop("key").withTimeout(1, SECONDS).asString());

    assert 0 == client.llen("key").withTimeout(1, SECONDS).asInteger();
   }

  public void testRightPushPop()
    throws TimeoutException, ExecutionException, InterruptedException
  {
    assert client.flushdb().asBoolean();

    client.rpush("key","v1").withTimeout(1, SECONDS);
    client.rpush("key","v2").withTimeout(1, SECONDS);

    assert 2 == client.llen("key").withTimeout(1, SECONDS).asInteger();

    assert "v2".equals(client.rpop("key").withTimeout(1, SECONDS).asString());
    assert "v1".equals(client.rpop("key").withTimeout(1, SECONDS).asString());

    assert 0 == client.llen("key").withTimeout(1, SECONDS).asInteger();
   }

  public void testPopEmtyKey()
    throws TimeoutException, ExecutionException, InterruptedException
  {
    assert client.flushdb().asBoolean();

    ResponseFuture f = client.lpop("key").withTimeout(1, SECONDS);
    assert f.get() == null;
    assert !f.isCancelled();

    f = client.rpop("key").withTimeout(1, SECONDS);
    assert f.get() == null;
    assert !f.isCancelled();
   }

  public void testLRange()
    throws TimeoutException, ExecutionException, InterruptedException
  {
    assert client.flushdb().asBoolean();

    client.rpush("key","v1").withTimeout(1, SECONDS);
    client.rpush("key","v2").withTimeout(1, SECONDS);

    ResponseFuture f = client.lrange("key", 0, -1).withTimeout(1, SECONDS);
    String[] s = f.asStrings();

    assert s.length == 2;
    assert "v1".equals(s[0]);
    assert "v2".equals(s[1]);
   }

  public void testLRem()
    throws TimeoutException, ExecutionException, InterruptedException
  {
    assert client.flushdb().asBoolean();

    client.rpush("key","v").withTimeout(1, SECONDS);
    client.rpush("key","v").withTimeout(1, SECONDS);
    client.rpush("key","v").withTimeout(1, SECONDS);

    assert 2 == client.lrem("key",2,"v").withTimeout(1, SECONDS).asInteger();

    assert 1 == client.llen("key").withTimeout(1, SECONDS).asInteger();
   }
}