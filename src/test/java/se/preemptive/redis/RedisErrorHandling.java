/*
 * Copyright (c) 2008 Preemptive Labs / Andreas Bielk (http://www.preemptivelabs.com)
 * All Rights Reserved.
 *
 * Created Jan 7, 2010, 10:04:38 PM
 *
 * $Source: $
 * $Revision: $
 * Last checkin $Date: $  $Author: $ 
 *
 *************************************************************************************/
package se.preemptive.redis;

import org.testng.annotations.Test;
import se.preemptive.redis.util.RedisClientError;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class RedisErrorHandling
{
  @Test
  public void testConnectionError()
    throws Exception
  {
    // make sure there is no server here!
    try
    {
      RedisProtocolClient c = new RedisProtocolClient("127.0.0.2", 2);
      c.connect();
      assert false : "Found server at 127.0.0.2:2";
    }
    catch (RedisClientError e)
    {
      // ignore
    }

    RedisProtocolClient c = new RedisProtocolClient("127.0.0.2", 2);
    ResponseFuture f = c.send("PING").withTimeout(2, TimeUnit.SECONDS);
    assert f.isCancelled();
    assertNotNull(f.getCancellationCause());
    assertNull(f.get());
  }

  @Test
  public void testBadCommand()
    throws Exception
  {
    RedisProtocolClient c = new RedisProtocolClient();
    c.connect();

    ResponseFuture f = c.send("BADREDIS").withTimeout(2, TimeUnit.SECONDS);
    assert f.isCancelled() : "Response not cancelled";
    assertNotNull(f.getCancellationCause());
    assertNull(f.get());

    // make sure next req/resp is ok
    assert "PONG".equals(c.send("PING").withTimeout(2, TimeUnit.SECONDS).asString());
  }

  @Test
  public void testBadArguments()
    throws Exception
  {
    RedisProtocolClient c = new RedisProtocolClient();
    c.connect();

    ResponseFuture f = c.send("PING BING STING").withTimeout(2, TimeUnit.SECONDS);
    assert f.isCancelled() : "Response not cancelled";
    assertNotNull(f.getCancellationCause());
    assertNull(f.get());

    // make sure next req/resp is ok
    assert "PONG".equals(c.send("PING").withTimeout(2, TimeUnit.SECONDS).asString());
  }
}
