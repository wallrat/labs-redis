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
import se.preemptive.redis.util.Strings;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

public class RedisBaseCommands
{
  protected final RedisProtocolClient client;

  private static final ChannelBuffer PING_CRLF = toBuffer("PING\r\n");

  public RedisBaseCommands(RedisProtocolClient client)
  {
    this.client = client;
  }

  protected static ChannelBuffer toBuffer(String value) {return copiedBuffer(value, Strings.US_ASCII);}

  public ResponseFuture ping() { return client.send(PING_CRLF); }

  public ResponseFuture auth(String key) { return client.send("AUTH " + key); }

  public ResponseFuture select(int index) { return client.send("SELECT " + index); }

  public ResponseFuture flushdb() { return client.send("FLUSHDB"); }

  public ResponseFuture flushall() { return client.send("FLUSHALL"); }

  public ResponseFuture dbsize(String key) { return client.send("DBSIZE " + key); }

  public ResponseFuture exists(String key) { return client.send("EXISTS " + key); }

  public ResponseFuture del(String key) { return client.send("DEL " + key); }

  public ResponseFuture type(String key) { return client.send("TYPE " + key); }

  public ResponseFuture keys(String key) { return client.send("KEYS " + key); }

  public ResponseFuture randomkey(String key) { return client.send("RANDOMKEY " + key); }

  public ResponseFuture rename(String oldName, String newName)
  {
    return client.send("RENAME " + oldName + " " + newName);
  }

  public ResponseFuture renamenx(String oldName, String newName)
  {
    return client.send("RENAMENX " + oldName + " " + newName);
  }

  public ResponseFuture move(String key, int dbindex) { return client.send("MOVE " + key + " " + dbindex); }


  public ResponseFuture expire(String key, int ttlInSeconds)
  {
    return client.send("EXPIRE " + key + " " + ttlInSeconds);
  }

  public ResponseFuture ttl(String key) { return client.send("TTL " + key); }

  public ResponseFuture save() { return client.send("SAVE"); }
  public ResponseFuture lastsave() { return client.send("LASTSAVE"); }
  public ResponseFuture bgsave() { return client.send("BGSAVE"); }
  public ResponseFuture bgrewriteaof() { return client.send("BGREWRITEAOF"); }

  public RedisProtocolClient getProtocolClient()
  {
    return client;
  }

}