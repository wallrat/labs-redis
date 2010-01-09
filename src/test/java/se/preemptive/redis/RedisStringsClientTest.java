/*
 * Copyright (c) 2010 Preemptive Labs / Andreas Bielk (http://www.preemptivelabs.com)
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
import se.preemptive.redis.commands.RedisStringCommands;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

@Test
public class RedisStringsClientTest
{
  private RedisStringCommands client = new RedisStringCommands(new RedisProtocolClient());

  @BeforeTest
  public void initClient()
  {
    assert client.select(10).asBoolean();
    assert client.flushdb().asBoolean();
  }

  public void testConnect()
  {
    client.getProtocolClient().connect();
  }

  public void testPing()
  {
    assert "PONG".equals(client.ping().asString()) : "GOT NO PONG";
  }

  public void testGet()
    throws TimeoutException
  {
    assert client.flushdb().asBoolean();

    client.set("key", "value").withTimeout(1, SECONDS);
    assert "value".equals(client.get("key").withTimeout(1, SECONDS).asString());
  }

  public void testSet()
    throws TimeoutException
  {
    assert client.flushdb().asBoolean();

    client.set("key", "string").withTimeout(1, SECONDS);
    assert "string".equals(client.get("key").withTimeout(1, SECONDS).asString());

    client.set("key", ChannelBuffers.copiedBuffer("buffer", "US-ASCII")).withTimeout(1, SECONDS);
    assert "buffer".equals(client.get("key").withTimeout(1, SECONDS).asString());
  }

  public void testGetSet()
    throws TimeoutException
  {
    assert client.flushdb().asBoolean();

    client.set("key", "first").withTimeout(1, SECONDS);
    assert "first".equals(client.getset("key", "second").withTimeout(1, SECONDS).asString());
    assert "second".equals(client.get("key").withTimeout(1, SECONDS).asString());
  }

  public void testIncrDecr()
    throws TimeoutException
  {
    assert client.flushdb().asBoolean();
    assert 1 == client.incr("key").withTimeout(1, SECONDS).asInteger();
    assert 2 == client.incr("key").withTimeout(1, SECONDS).asInteger();
    assert 1 == client.decr("key").withTimeout(1, SECONDS).asInteger();
    assert 0 == client.decr("key").withTimeout(1, SECONDS).asInteger();
    assert -1 == client.decr("key").withTimeout(1, SECONDS).asInteger();
    assert -1 == client.get("key").withTimeout(1, SECONDS).asInteger();
  }

  public void testMGet()
    throws TimeoutException
  {
    assert client.flushdb().asBoolean();
    client.set("key1", "val1");
    client.set("key2", "val2");
    String[] r = client.mget("key1", "key2").withTimeout(1, SECONDS).asStrings();
    assert r.length == 2;
    assert "val1".equals(r[0]) && "val2".equals(r[1]);
  }

}
