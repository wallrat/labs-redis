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

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import se.preemptive.redis.netty.PipelineFactory;
import se.preemptive.redis.util.RedisClientError;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Factory for connected Netty channels
 */
public class ChannelFactory
{
  private final String host;
  private final int port;
  private final ClientBootstrap bootstrap;

  public ChannelFactory(String host, int port)
  {
    this.host = host;
    this.port = port;

    // create bootstrap
    ThreadFactory tf = new ThreadFactory()
    {
      @Override
      public Thread newThread(Runnable r)
      {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    };

    bootstrap = new ClientBootstrap(
      new OioClientSocketChannelFactory(Executors.newCachedThreadPool(tf)));

    //bootstrap = new ClientBootstrap(
    //  new NioClientSocketChannelFactory(
    //    Executors.newCachedThreadPool(tf),
    //    Executors.newCachedThreadPool(tf)));
    //

    //bootstrap.setOption("tcpNoDelay", true);
    //bootstrap.setOption(
    //        "child.receiveBufferSizePredictor",
    //        new DefaultReceiveBufferSizePredictor(
    //                Constant.MIN_READ_BUFFER_SIZE,
    //                Constant.INITIAL_READ_BUFFER_SIZE,
    //                Constant.MAX_READ_BUFFER_SIZE)
    //        );


    // configure pipeline
    bootstrap.setPipelineFactory(new PipelineFactory());
  }

  public Channel connect()
  {
    // Start the connection attempt.
    ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

    // Wait until the connection attempt succeeds or fails.
    Channel channel = future.awaitUninterruptibly().getChannel();

    // todo: handle this
    if (!future.isSuccess())
    {
      bootstrap.releaseExternalResources();
      throw new RedisClientError("Could not connect to redis server at " + host + ":" + port, future.getCause());
    }

    return channel;
  }

  public void disconnect(Channel channel)
  {
    // wait for redis server to close connection
    channel.getCloseFuture().awaitUninterruptibly(1000, TimeUnit.MILLISECONDS);

    // Close the connection.  Make sure the close operation ends because
    // all I/O operations are asynchronous in Netty.
    channel.close().awaitUninterruptibly(1000, TimeUnit.MILLISECONDS);
  }

  public void close()
  {
    // Shut down all thread pools to exit.
    bootstrap.releaseExternalResources();
  }
}
