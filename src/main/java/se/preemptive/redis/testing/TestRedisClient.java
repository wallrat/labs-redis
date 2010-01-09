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

import org.jboss.netty.buffer.ChannelBuffer;
import se.preemptive.redis.RedisClient;
import se.preemptive.redis.RedisProtocolClient;
import se.preemptive.redis.ResponseFuture;
import se.preemptive.redis.netty.FrameDecoder;

public class TestRedisClient
{
  public static void main(String[] args)
    throws Exception
  {
    RedisClient q = new RedisClient(new RedisProtocolClient());

    ResponseFuture resp;
    //resp = q.ping();
    //resp = q.set("name","annika-"+System.currentTimeMillis());
    //resp = q.get("name");

    //while (true)
    {
      System.out.println("Before ping");
      resp = q.ping();

      System.out.println("Waiting for >> ");
      System.out.println("  " + resp.asString() + " (" + resp.isCancelled() + ")");

      if (resp.isCancelled())
        System.out.println("  Cancelled by " + resp.getCancellationCause());

      //Thread.sleep(3000);
      //if (false)
      //  break;
    }

    System.out.println(resp.asString());
    System.out.println(resp.asString());

    //resp = q.set("name","andreas");
    //resp = q.set("name2","camilla");
    //resp = q.mset(array("name","name3"),array("lisa","agnes"));
    //resp = q.mget("name","name2","name3");

    //resp = q.lpop("testlist");

    //System.out.println("list length: " + q.getProtocolClient().send("LLEN mylist").asLong());

    //Object o = resp.get(40, TimeUnit.SECONDS);
    Object o = resp.get();

    if (o instanceof ChannelBuffer)
      System.out.println("o = " + ((ChannelBuffer) o).toString("UTF-8"));
    else if (o instanceof Object[])
    {
      System.out.println("o is an array of " + ((Object[]) o).length);
      for (Object obj : (Object[]) o)
        if (obj instanceof ChannelBuffer)
          System.out.println("  " + ((ChannelBuffer) obj).toString("UTF-8"));
        else
          System.out.println("  " + obj);
    }
    else
      System.out.println("o = " + o);


    //CommandDecoder decoder = (CommandDecoder) q.getProtocolClient().getChannel().getPipeline().get("decoder");
    //System.out.println("decoder.getCalls() = " + decoder.getCalls());
    //System.out.println("decoder.queue size = " + decoder.getQueue().size());
    //
    //FrameDecoder framer = (FrameDecoder) q.getProtocolClient().getChannel().getPipeline().get("framer");
    //System.out.println("framer.getCalls() = " + framer.getCalls());
    //System.out.println("framer.getFrames() = " + framer.getFrames());

    q.getProtocolClient().disconnect();

  }
}
