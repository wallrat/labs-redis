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
package se.preemptive.redis.netty;

import org.jboss.netty.channel.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Add to pipeline for async writes
 */
@ChannelPipelineCoverage("one")
public class AsyncDownStreamHandler implements ChannelDownstreamHandler
{
  private static volatile int workerCount = 0;

  class Event
  {
    ChannelHandlerContext ctx;
    ChannelEvent evt;

    Event(ChannelHandlerContext ctx, ChannelEvent evt)
    {
      this.ctx = ctx;
      this.evt = evt;
    }
  }

  private final BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();

  private final Runnable worker = new Runnable()
  {
    @Override
    public void run()
    {
      while (true)
      {
        try
        {
          Event e = queue.take();
          e.ctx.sendDownstream(e.evt);
        }
        catch (InterruptedException e1)
        {
          Thread.currentThread().interrupt();
        }
      }
    }
  };

  public AsyncDownStreamHandler()
  {
    Thread t = new Thread(worker);
    t.setDaemon(true);
    t.setName("AsyncDownstreamWorker[" + (workerCount++) + "]");
    t.start();
  }

  @Override
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt)
    throws Exception
  {
    if (!(evt instanceof MessageEvent))
    {
      ctx.sendDownstream(evt);
      return;
    }

    queue.offer(new Event(ctx, evt));
  }
}
