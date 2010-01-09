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

public class RedisListsCommands extends RedisStringCommands
{
  public RedisListsCommands(RedisProtocolClient client)
  {
    super(client);
  }

  public ResponseFuture llen(String key)
  {
    return client.send("LLEN " + key);
  }

  public ResponseFuture lpop(String key)
  {
    return client.send("LPOP " + key);
  }

  public ResponseFuture rpop(String key)
  {
    return client.send("RPOP " + key);
  }

  public ResponseFuture rpoplpush(String srckey, String destkey)
  {
    return client.send("RPOPLPUSH " + srckey + " " + destkey);
  }

  public ResponseFuture ltrim(String key, int start, int end)
  {
    return client.send("LTRIM " + key + " " + start + " " + end);
  }

  public ResponseFuture lrange(String key, int start, int end)
  {
    return client.send("LRANGE " + key + " " + start + " " + end);
  }

  public ResponseFuture lindex(String key, int index)
  {
    return client.send("LINDEX " + key + " " + index);
  }

  public ResponseFuture rpush(String key, String value)
  {
    return rpush(key, toBuffer(value));
  }


  public ResponseFuture lpush(String key, String value)
  {
    return lpush(key, toBuffer(value));
  }

  public ResponseFuture lset(String key, int index, String value)
  {
    return lset(key, index, toBuffer(value));
  }

  public ResponseFuture lrem(String key, int count, String value)
  {
    return lrem(key, count, toBuffer(value));
  }

  public ResponseFuture rpush(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("RPUSH " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture lpush(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("LPUSH " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture lset(String key, int index, ChannelBuffer value)
  {
    return client.send(
      toBuffer("LSET " + key + " " + index + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture lrem(String key, int count, ChannelBuffer value)
  {
    return client.send(
      toBuffer("LREM " + key + " " + count + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

}
