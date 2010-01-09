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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import static org.jboss.netty.channel.Channels.write;

/**
 * Not used
 */
@ChannelPipelineCoverage("all")
public class MessageSplitHandler implements ChannelDownstreamHandler
{
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt)
    throws Exception
  {
    if (!(evt instanceof MessageEvent))
    {
      ctx.sendDownstream(evt);
      return;
    }

    MessageEvent e = (MessageEvent) evt;
    Object originalMessage = e.getMessage();

    if (!(originalMessage instanceof ChannelBuffer[]))
    {
      ctx.sendDownstream(evt);
      return;
    }

    ChannelBuffer[] buffers = (ChannelBuffer[]) originalMessage;

    for (ChannelBuffer buffer : buffers)
      write(ctx, e.getFuture(), buffer, e.getRemoteAddress());
  }
}

