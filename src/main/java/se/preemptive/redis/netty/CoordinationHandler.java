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
import se.preemptive.redis.ResponseFuture;
import se.preemptive.redis.util.Pair;
import se.preemptive.redis.util.RedisClientError;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.jboss.netty.channel.Channels.fireMessageReceived;
import static se.preemptive.redis.netty.FrameDecoder.Response.NIL;

/**
 * Handles the coordination of requests with responses. Also responsible for
 * cancellation of futures on errors.
 * <br><br>
 * To make RedisProtocolClient threadsafe make handleDownstream(..) syncronized
 * or use AsyncDownStreamHandler.
 */
@SuppressWarnings({"CastToConcreteClass", "unchecked", "InstanceofInterfaces"})
@ChannelPipelineCoverage("one")
public class CoordinationHandler implements ChannelDownstreamHandler, ChannelUpstreamHandler
{
  private final BlockingQueue<ResponseFuture> queue = new LinkedBlockingQueue<ResponseFuture>();

  /**
   * Handles writes. Unwraps Pair<ResponseFuture,ChannelBuffer> and sends Request downstream
   */
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt)
    throws Exception
  {
    if (!(evt instanceof MessageEvent))
    {
      ctx.sendDownstream(evt);
      return;
    }

    // unwrap event
    final MessageEvent e = (MessageEvent) evt;
    final Object originalMessage = e.getMessage();

    // unwrap message
    if (!(originalMessage instanceof Pair))
      throw new Exception("Unknown message type");

    final Pair<ResponseFuture, ChannelBuffer> request = (Pair<ResponseFuture, ChannelBuffer>) originalMessage;

    // queue up Futures for upstream
    // queue before write, handleUpstream will cancel future on exception
    if (!queue.offer(request.first(), 10, TimeUnit.SECONDS))
    {
      // todo: queue stalled
      throw new Exception("Queue stalled (size " + queue.size() + ")");
    }

    // we got a write req, so just pass it down stream
    Channels.write(ctx, e.getFuture(), request.second(), e.getRemoteAddress());
  }


  /**
   * Handles responses. Pairs the response with RequestFuturePair
   */
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent evt)
    throws Exception
  {
    // handle ChannelState
    if (evt instanceof UpstreamChannelStateEvent)
    {
      UpstreamChannelStateEvent stateEvent = (UpstreamChannelStateEvent) evt;

      // handle disconnects. See ChannelState for getValue() defs
      if (stateEvent.getState() == ChannelState.CONNECTED && stateEvent.getValue() == null)
      {
        // cancel all queued requests
        for (ResponseFuture f : queue)
          f.cancel(false);

        queue.clear();
      }
    }

    // handle Exceptions
    if (evt instanceof ExceptionEvent)
    {
      ExceptionEvent eevt = (ExceptionEvent) evt;

      //todo: log?

      // wrote to a closed channel or server returned an error -> cancel next request in queue
      //if (eevt.getCause() instanceof ClosedChannelException || eevt.getCause() instanceof RedisClientError)
      {
        ResponseFuture f = queue.poll();
        if (f != null)
          f.cancel(eevt.getCause());
      }
    }

    // send all state changes etc upstream
    if (!(evt instanceof MessageEvent))
    {
      ctx.sendUpstream(evt);
      return;
    }

    MessageEvent e = (MessageEvent) evt;
    Object response = e.getMessage();

    // assert that there actually is a command?
    ResponseFuture f = queue.take();

    // handle NIL -> NULL conversion
    f.setResponse(NIL == response ? null : response);

    if (f != null)
      fireMessageReceived(ctx, f, e.getRemoteAddress());
  }
}