/*
 * Copyright (c) 2008 Preemptive Labs / Andreas Bielk (http://www.preemptivelabs.com)
 * All Rights Reserved.
 *
 * Created Jan 4, 2010, 1:41:31 AM
 *
 * $Source: $
 * $Revision: $
 * Last checkin $Date: $  $Author: $ 
 *
 *************************************************************************************/
package se.preemptive.redis;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@Test
public class RedisProtocolClientTest
{
  private RedisProtocolClient client = new RedisProtocolClient();

  public void testConnect()
  {
    client.connect();
    assertNotNull(client.getChannel());
    assert client.getChannel().isConnected();
  }

  public void testDisconnect()
  {
    client.disconnect();
    assertNull(client.getChannel());
  }

  public void testSend()
    throws TimeoutException
  {
    assert "PONG".equals(
      client.send("PING").
        withTimeout(1, TimeUnit.SECONDS).asString()) : "GOT NO PONG";
    assert "PONG".equals(
      client.send(copiedBuffer("PING\r\n", "US-ASCII")).
        withTimeout(1, TimeUnit.SECONDS).asString()) : "GOT NO PONG";
    assert "PONG".equals(
      client.send(
        copiedBuffer("PING", "US-ASCII"),
        copiedBuffer("\r\n", "US-ASCII")).
        withTimeout(1, TimeUnit.SECONDS).asString()) : "GOT NO PONG";
  }

  public void testSendMultiBulk()
    throws TimeoutException
  {
    assert "PONG".equals(
      client.sendMultiBulk(
        copiedBuffer("PING", "US-ASCII"))
        .withTimeout(1, TimeUnit.SECONDS).asString()) : "GOT NO PONG";
  }
}