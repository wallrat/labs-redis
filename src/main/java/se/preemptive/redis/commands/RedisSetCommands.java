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

import static se.preemptive.redis.util.Strings.join;

public class RedisSetCommands extends RedisListsCommands
{
  public RedisSetCommands(RedisProtocolClient client)
  {
    super(client);
  }

  //* SADD key member Add the specified member to the Set value at key
  //* SREM key member Remove the specified member from the Set value at key
  //* SPOP key Remove and return (pop) a random element from the Set value at key
  //* SMOVE srckey dstkey member Move the specified member from one Set to another atomically
  //* SCARD key Return the number of elements (the cardinality) of the Set at key
  //* SISMEMBER key member Test if the specified value is a member of the Set at key
  //* SINTER key1 key2 ... keyN Return the intersection between the Sets stored at key1, key2, ..., keyN
  //* SINTERSTORE dstkey key1 key2 ... keyN Compute the intersection between the Sets stored at key1, key2, ..., keyN, and store the resulting Set at dstkey
  //* SUNION key1 key2 ... keyN Return the union between the Sets stored at key1, key2, ..., keyN
  //* SUNIONSTORE dstkey key1 key2 ... keyN Compute the union between the Sets stored at key1, key2, ..., keyN, and store the resulting Set at dstkey
  //* SDIFF key1 key2 ... keyN Return the difference between the Set stored at key1 and all the Sets key2, ..., keyN
  //* SDIFFSTORE dstkey key1 key2 ... keyN Compute the difference between the Set key1 and all the Sets key2, ..., keyN, and store the resulting Set at dstkey
  //* SMEMBERS key Return all the members of the Set value at key
  //* SRANDMEMBER key Return a random member of the Set value at key

  public ResponseFuture srandmember(String key)
  {
    return client.send("SRANDMEMBER " + key);
  }

  public ResponseFuture smembers(String key)
  {
    return client.send("SMEMBERS " + key);
  }

  public ResponseFuture sdiffstore(String dstKey, String... keys)
  {
    return client.send(join(' ', "SDIFFSTORE", dstKey, keys));
  }

  public ResponseFuture sdiff(String... keys)
  {
    return client.send(join(' ', "SDIFF", keys));
  }

  public ResponseFuture sunionstore(String dstKey, String... keys)
  {
    return client.send(join(' ', "SUNIONSTORE", dstKey, keys));
  }

  public ResponseFuture sunion(String... keys)
  {
    return client.send(join(' ', "SUNION", keys));
  }

  public ResponseFuture sinterstore(String dstKey, String... keys)
  {
    return client.send(join(' ', "SINTERSTORE", dstKey, keys));
  }

  public ResponseFuture sinter(String... keys)
  {
    return client.send(join(' ', "SINTER", keys));
  }

  public ResponseFuture sismember(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("SISMEMBER " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture scard(String key)
  {
    return client.send("SCARD " + key);
  }

  public ResponseFuture smove(String srcKey, String dstKey, ChannelBuffer member)
  {
    return client.send(
      toBuffer("SMOVE " + srcKey + " " + dstKey + " " + member.readableBytes() + "\r\n"),
      member,
      FrameDecoder.CRLF);
  }

  public ResponseFuture spop(String key)
  {
    return client.send("SPOP " + key);
  }

  public ResponseFuture srem(String key, String  value)
  {
    return srem(key, toBuffer(value));
  }

  public ResponseFuture srem(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("SREM " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture sadd(String key, String value)
  {
    return sadd(key,toBuffer(value));
  }

  public ResponseFuture sadd(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("SADD " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }
}
