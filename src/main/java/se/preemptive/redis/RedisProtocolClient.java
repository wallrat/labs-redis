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
package se.preemptive.redis;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import se.preemptive.redis.netty.FrameDecoder;
import se.preemptive.redis.netty.ReadonlyCompositeChannelBuffer;
import se.preemptive.redis.util.Pair;
import se.preemptive.redis.util.RedisClientError;

import java.nio.ByteOrder;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

/**
 * Client for the Redis protocol
 */
public class RedisProtocolClient
{
  private final ChannelFactory channelFactory;
  private Channel channel;


  /**
   * Creates a client for 127.0.0.1 port 6379
   */
  public RedisProtocolClient()
  {
    this("127.0.0.1", 6379);
  }

  public RedisProtocolClient(String host, int port)
  {
    this(new ChannelFactory(host, port));
  }

  public RedisProtocolClient(ChannelFactory channelFactory)
  {
    this.channelFactory = channelFactory;
  }

  private static ChannelBuffer wrappedReadOnlyBuffer(ChannelBuffer... args)
  {
    //TODO: check ByteOrder in args
    assert args.length > 0;
    if (args.length == 1) return args[0];
    return new ReadonlyCompositeChannelBuffer(args[0].order(), args);
  }


  public ResponseFuture send(String command)
  {
    return send(copiedBuffer(command + "\r\n", "US-ASCII"));
  }

  public ResponseFuture sendMultiBulk(ChannelBuffer... args)
  {
    //List<ChannelBuffer> parts = new ArrayList<ChannelBuffer>(args.length*2+1);
    //parts.add(copiedBuffer("*"+args.length+"\r\n","US-ASCII"));
    //for(ChannelBuffer arg: args)
    //{
    //  parts.add(copiedBuffer("$"+arg.readableBytes()+"\r\n","US-ASCII"));
    //  parts.add(arg);
    //  parts.add(FrameDecoder.CRLF);
    //}
    //
    //ChannelBuffer request = new CompositeChannelBuffer(ByteOrder.BIG_ENDIAN, parts);

    int i = 0;
    ChannelBuffer[] parts = new ChannelBuffer[args.length * 3 + 1];
    parts[i++] = copiedBuffer("*" + args.length + "\r\n", "US-ASCII");

    for (ChannelBuffer arg : args)
    {
      parts[i++] = copiedBuffer("$" + arg.readableBytes() + "\r\n", "US-ASCII");
      parts[i++] = arg;
      parts[i++] = FrameDecoder.CRLF;
    }

    ChannelBuffer request = new ReadonlyCompositeChannelBuffer(ByteOrder.BIG_ENDIAN, parts);

    //System.out.println(request.toString("UTF-8"));

    return send(request);
  }

  public ResponseFuture send(ChannelBuffer... args)
  {
    return send(wrappedReadOnlyBuffer(args));
/*
    // in some situations (dep. on network topology and key/value sizes)
    // it's faster do do fragmented writes instead of creating a composite
    // buffer. Testing seems to indicate a variance of about 10% so we're
    // staying with composite buffers for now.

    // lazy connect
    if (channel == null)
      connect();

    // write buffers
    for (ChannelBuffer request : args)
      channel.write(request);

    // queue future
    ResponseFuture<Object> f = new ResponseFuture<Object>();
    channel.write(f);

    return f;

    // queue future
    //ResponseFuture<Object> f = new ResponseFuture<Object>();
    //channel.write(new Pair<ResponseFuture,ChannelBuffer[]>(f,args));
    //return f;
*/

  }

  public ResponseFuture send(ChannelBuffer request)
  {
    ResponseFuture f = new ResponseFuture();

    try
    {
      // lazy connect
      if (channel == null || !channel.isConnected())
        connect();

      channel.write(new Pair<ResponseFuture, ChannelBuffer>(f, request));
    }
    catch (RedisClientError e)
    {
      f.cancel(e);
    }

    return f;
  }


  public void connect()
  {
    if (channel != null && channel.isConnected())
      return;
    channel = channelFactory.connect();
  }

  public void disconnect()
  {
    // no-op if not connected
    if (channel == null || channel.isConnected())
      return;

    // send QUIT
    send("QUIT");
    channelFactory.disconnect(channel);
    channel = null;
  }

  /**
   * exposed for testing and debug
   */
  public Channel getChannel()
  {
    return channel;
  }
}