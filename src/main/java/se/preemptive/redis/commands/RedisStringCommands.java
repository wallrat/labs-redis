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
package se.preemptive.redis.commands;

import org.jboss.netty.buffer.ChannelBuffer;
import se.preemptive.redis.RedisProtocolClient;
import se.preemptive.redis.ResponseFuture;
import se.preemptive.redis.netty.FrameDecoder;
import se.preemptive.redis.util.Strings;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static se.preemptive.redis.util.Strings.join;

/**
 * Groups implemention of GET,SET,MGET etc together
 */
public class RedisStringCommands extends RedisBaseCommands
{
  private static final ChannelBuffer SET = toBuffer("SET");

  public RedisStringCommands(RedisProtocolClient client)
  {
    super(client);
  }

  public ResponseFuture incr(String key)
  {
    return client.send("INCR " + key);
  }

  public ResponseFuture incr(String key, int by)
  {
    return client.send("INCRBY " + key + " " + by);
  }

  public ResponseFuture decr(String key)
  {
    return client.send("DECR " + key);
  }

  public ResponseFuture decr(String key, int by)
  {
    return client.send("DECRBY " + key + " " + by);
  }

  public ResponseFuture get(String key)
  {
    return client.send("GET " + key);
  }

  public ResponseFuture mget(String... keys)
  {
    return client.send(join(' ', "MGET", keys));
  }

  public ResponseFuture set(String key, String value)
  {
    return set(key, toBuffer(value));
  }

  public ResponseFuture set(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("SET " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture getset(String key, String value)
  {
    return getset(key, toBuffer(value));
  }

  public ResponseFuture getset(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("GETSET " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture setnx(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("SETNX " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  /**
   * Set with binary safe key. Uses multi-bulk protocol == slow
   */
  public ResponseFuture setWithMultiBulk(ChannelBuffer key, ChannelBuffer value)
  {
    return client.sendMultiBulk(SET, key, value);
  }


  public ResponseFuture mset(String[] keys, String[] values)
  {
    assert keys.length == values.length;
    ChannelBuffer[] args = new ChannelBuffer[1 + keys.length * 2];

    args[0] = toBuffer("MSET");

    for (int i = 0, j = 0; i < keys.length; i++, j += 2)
    {
      args[1 + j] = toBuffer(keys[i]);
      args[1 + j + 1] = copiedBuffer(values[i], Strings.UTF8);
    }

    //TODO: create a sendMultiBulk that does the interleaving instead
    return client.sendMultiBulk(args);
  }

  public ResponseFuture msetnx(String[] keys, String[] values)
  {
    assert keys.length == values.length;
    ChannelBuffer[] args = new ChannelBuffer[1 + keys.length * 2];

    args[0] = toBuffer("MSETNX");

    for (int i = 0, j = 0; i < keys.length; i++, j += 2)
    {
      args[1 + j] = toBuffer(keys[i]);
      args[1 + j + 1] = copiedBuffer(values[i], Strings.UTF8);
    }

    //TODO: create a sendMultiBulk that does the interleaving instead
    return client.sendMultiBulk(args);
  }

  public ResponseFuture mset(String[] keys, ChannelBuffer[] values)
  {
    assert keys.length == values.length;

    ChannelBuffer[] args = new ChannelBuffer[1 + keys.length * 2];
    args[0] = toBuffer("MSET");

    for (int i = 0, j = 0; i < keys.length; i++, j += 2)
    {
      args[1 + j] = toBuffer(keys[i]);
      args[1 + j + 1] = values[i];
    }

    //TODO: create a sendMultiBulk that does the interleaving instead
    return client.sendMultiBulk(args);
  }

}
