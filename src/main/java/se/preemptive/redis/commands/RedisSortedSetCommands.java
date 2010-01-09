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

public class RedisSortedSetCommands extends RedisSetCommands
{
  public RedisSortedSetCommands(RedisProtocolClient client)
  {
    super(client);
  }

 /*
  * ZADD key score member Add the specified member to the Sorted Set value at key or update the score if it already exist
  * ZREM key member Remove the specified member from the Sorted Set value at key
  * ZINCRBY key increment member If the member already exists increment its score by _increment_, otherwise add the member setting _increment_ as score
  * ZRANGE key start end Return a range of elements from the sorted set at key
  * ZREVRANGE key start end Return a range of elements from the sorted set at key, exactly like ZRANGE, but the sorted set is ordered in traversed in reverse order, from the greatest to the smallest score
  * ZRANGEBYSCORE key min max Return all the elements with score >= min and score <= max (a range query) from the sorted set
  * ZCARD key Return the cardinality (number of elements) of the sorted set at key
  * ZSCORE key element Return the score associated with the specified element of the sorted set at key
  * ZREMRANGEBYSCORE key min max Remove all the elements with score >= min and score <= max from the sorted set
  */

  public ResponseFuture zadd(String key, double score, String member)
  {
    return zadd(key,score, toBuffer(member));
  }

  public ResponseFuture zadd(String key, double score, ChannelBuffer member)
  {
    return client.send(
      toBuffer("ZADD " + key + " " +score+" "+ member.readableBytes() + "\r\n"),
      member,
      FrameDecoder.CRLF);
  }

  public ResponseFuture zrem(String key, String  value)
  {
    return zrem(key, toBuffer(value));
  }

  public ResponseFuture zrem(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("ZREM " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

  public ResponseFuture zincrby(String key, double score, String member)
  {
    return zincrby(key,score, toBuffer(member));
  }

  public ResponseFuture zincrby(String key, double score, ChannelBuffer member)
  {
    return client.send(
      toBuffer("ZINCRBY " + key + " " +score+" "+ member.readableBytes() + "\r\n"),
      member,
      FrameDecoder.CRLF);
  }

  public ResponseFuture zrange(String key, int start, int end)
  {
    return zrange(key,start,end,false);
  }

  public ResponseFuture zrange(String key, int start, int end, boolean withScores)
  {
    StringBuilder s = new StringBuilder().append("ZRANGE ").append(key).append(" ").append(start).append(" ").append(end);
    if (withScores) s.append(" WITHSCORES");
    return client.send(s.toString());
  }

  public ResponseFuture zrevrange(String key, int start, int end)
  {
    return zrevrange(key,start,end,false);
  }

  public ResponseFuture zrevrange(String key, int start, int end, boolean withScores)
  {
    StringBuilder s = new StringBuilder().append("ZREVRANGE ").append(key).append(" ").append(start).append(" ").append(end);
    if (withScores) s.append(" WITHSCORES");
    return client.send(s.toString());
  }

  public ResponseFuture zrangebyscore(String key, int start, int end)
  {
    return client.send("ZRANGEBYSCORE " + key + " " + start + " " + end+" WITHSCORES");
  }

  public ResponseFuture zcard(String key)
  {
    return client.send("ZCARD " + key);
  }


  public ResponseFuture zscore(String key, String  value)
  {
    return zscore(key, toBuffer(value));
  }

  public ResponseFuture zscore(String key, ChannelBuffer value)
  {
    return client.send(
      toBuffer("ZSCORE " + key + " " + value.readableBytes() + "\r\n"),
      value,
      FrameDecoder.CRLF);
  }

}
