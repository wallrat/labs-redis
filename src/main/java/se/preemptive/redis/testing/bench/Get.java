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

import se.preemptive.redis.RedisClient;
import se.preemptive.redis.RedisProtocolClient;

import java.util.Map;
import java.util.concurrent.Future;

public class Get extends BenchTest
{
  // setup test data
  //private final ChannelBuffer value = copiedBuffer("asdasdasdasdasdasdasd", "UTF-8");
  //private final ChannelBuffer setKeyPart = copiedBuffer("SET " + "mykey" + " " + value.readableBytes() + "\r\n", "UTF-8");
  //private final ChannelBuffer staticSetCommand = ChannelBuffers.wrappedBuffer(setKeyPart, value, FrameDecoder.CRLF);

  protected final RedisClient client;

  protected final String key;
  protected final String value;

  public Get(int keySize, int valueSize, Map<String, String> options)
  {
    super(keySize, valueSize, options);

    client = new RedisClient(new RedisProtocolClient(options.get("h"),Integer.parseInt(options.get("p"))));
    client.getProtocolClient().connect();
    client.select(10);
    client.flushdb();

    key = createString(keySize);
    value = createString(valueSize);

    client.set(key, value).block();
  }

  public Future run()
  {
    //return client.ping();
    return client.get(key);
    //return client.set(key, value);
    //resp = q.set("mykey2", value);
    //resp = q.set2("mykey2", value);
    //resp = q.getProtocolClient().test();

    //ChannelBuffer setCommand = new ReadonlyCompositeChannelBuffer(ByteOrder.BIG_ENDIAN, setKeyPart, value, FrameDecoder.CRLF);
    //ChannelBuffer staticSetCommand = ChannelBuffers.wrappedBuffer(setKeyPart, value, FrameDecoder.CRLF);
    //resp = q.getProtocolClient().send(setCommand);
    //resp = q.getProtocolClient().send(staticSetCommand);
  }

}
