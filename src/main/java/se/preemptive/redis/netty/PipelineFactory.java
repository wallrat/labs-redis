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

import static org.jboss.netty.channel.Channels.pipeline;

public class PipelineFactory implements ChannelPipelineFactory
{
  @ChannelPipelineCoverage("one")
  class DebugLogHander implements ChannelDownstreamHandler, ChannelUpstreamHandler
  {
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
      throws Exception
    {
      System.out.println("LogHandler[U] >> " + e);
      if (e instanceof ExceptionEvent)
      {
        ExceptionEvent ee = (ExceptionEvent) e;
        //ee.getCause().printStackTrace();
      }
      ctx.sendUpstream(e);
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
      throws Exception
    {
      System.out.println("LogHandler[D] >> " + e);
      ctx.sendDownstream(e);
    }
  }

  @Override
  public ChannelPipeline getPipeline()
    throws Exception
  {
    //TODO: StaticChannelPipeline is marginally faster. Switch if profiling shows it's a win.

    // Create a default pipeline implementation.
    ChannelPipeline pipeline = pipeline();

    //pipeline.addLast("log", new DebugLogHander());
    //pipeline.addLast("splitter", new MessageSplitHandler());

    // decode redis frames
    pipeline.addLast("framer", new FrameDecoder());

    // request / response coord
    pipeline.addLast("coordinator", new CoordinationHandler());

    // async writes
    //pipeline.addLast("async", new AsyncDownStreamHandler());

    return pipeline;
  }

}
