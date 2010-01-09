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
import se.preemptive.redis.util.NotImplementedError;

import java.nio.charset.Charset;
import java.util.concurrent.*;

/**
 * Returned from RedisProtocolClient and RedisClient
 * request/response status methods and some default
 * value conversions.
 */
public class ResponseFuture<V> implements Future<V>
{
  private final CountDownLatch signal = new CountDownLatch(1);
  private volatile Throwable cancellationCasuse = null;
  private V response;

  public ResponseFuture()
  {
  }

  public void setResponse(V response)
  {
    this.response = response;
    signal.countDown();
  }

  //TODO: is it important to support thread interuption?

  public boolean cancel(boolean mayInterruptIfRunning)
  {
    return cancel(null);
  }

  public boolean cancel(Throwable cause)
  {
    if (cancellationCasuse != null) return false;
    cancellationCasuse = cause;
    setResponse(null);
    return true;
  }


  public boolean isCancelled()
  {
    return cancellationCasuse != null;
  }

  public Throwable getCancellationCause()
  {
    return cancellationCasuse;
  }

  public boolean isDone()
  {
    //return response != null || cancellationCause != null || signal.getCount() == 0;
    throw new NotImplementedError();
  }

  public V get()
    throws InterruptedException, ExecutionException
  {
    signal.await();
    return response;
  }

  public V get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    if (!signal.await(timeout, unit))
      throw new TimeoutException();
    return response;
  }

  /* ------------------- API  ---------------------*/

  public ResponseFuture withTimeout(long timeout, TimeUnit unit)
    throws TimeoutException
  {
    try
    {
      if (!signal.await(timeout, unit))
        throw new TimeoutException();
    }
    catch (InterruptedException e)
    {
      throw new TimeoutException("Interrupted " + e.getMessage());
    }
    return this;
  }

  public Object block()
  {
    try
    {
      return get();
    }
    catch (InterruptedException e)
    {
      return null;
    }
    catch (ExecutionException e)
    {
      return null;
    }
  }

  public ChannelBuffer asChannelBuffer()
  {
    Object res = block();
    if (res == null) return null;
    return (ChannelBuffer) res;
  }

  public String asString()
  {
    return asString(Charset.defaultCharset().name());
  }

  public String asString(String charsetName)
  {
    Object res = block();
    if (res == null) return null;
    if (res instanceof ChannelBuffer) return ((ChannelBuffer) res).toString(charsetName);
    return res.toString();
  }

  public String[] asStrings()
  {
    return asStrings(Charset.defaultCharset().name());
  }

  public String[] asStrings(String charsetName)
  {
    Object res = block();
    if (res == null) return null;
    if (res instanceof Object[])
    {
      Object[] b = (Object[]) res;
      String[] a = new String[b.length];
      for (int i = 0; i < b.length; i++)
        a[i] = ((ChannelBuffer) b[i]).toString(charsetName);
      return a;
    }

    // try to split
    // todo: use a sane split() method
    String s = asString();
    if (s != null && s.length() > 0)
      return s.split(" ");

    // just wrap in a new array
    return new String[]{s};
  }

  public int asInteger()
  {
    Object res = block();
    if (res == null) return 0;
    if (res instanceof ChannelBuffer)
    {
      String s = ((ChannelBuffer) res).toString("US-ASCII");
      return Integer.parseInt(s);
    }

    return 0;
  }

  public long asLong()
  {
    Object res = block();
    if (res == null) return 0;
    if (res instanceof ChannelBuffer)
    {
      String s = ((ChannelBuffer) res).toString("US-ASCII");
      return Long.parseLong(s);
    }

    return 0;
  }

  public boolean asBoolean()
  {
    Object res = block();
    if (res == null) return false;

    // handle simple false numeric responses
    if (res instanceof ChannelBuffer && ((ChannelBuffer) res).readableBytes() == 1)
      return '0' != ((ChannelBuffer) res).getByte(0);

    String s = asString("US-ASCII");
    return s.equals("OK");
  }


  // java primitives
  // todo: implement support for V_Int
  // redis.get("mykey").asJavaInteger()?
  // as builder? redis.get("mykey").asPrimitive().asInteger()?
  // as builder? redis.get("mykey").fromString().asInteger()?

  public int asJavaInteger()
  {
    Object res = block();
    if (res == null) return 0;
    if (res instanceof ChannelBuffer)
    {
      ChannelBuffer b = (ChannelBuffer) res;
      b.readerIndex(0);
      return b.readInt();
    }

    return 0;
  }

  public long asJavaLong()
  {
    Object res = block();
    if (res == null) return 0;
    if (res instanceof ChannelBuffer)
    {
      ChannelBuffer b = (ChannelBuffer) res;
      b.readerIndex(0);
      return b.readLong();
    }

    return 0;
  }

  public double asJavaDouble()
  {
    Object res = block();
    if (res == null) return 0;
    if (res instanceof ChannelBuffer)
    {
      ChannelBuffer b = (ChannelBuffer) res;
      b.readerIndex(0);
      return b.readDouble();
    }

    return 0;
  }

  public double asJavaFloat()
  {
    Object res = block();
    if (res == null) return 0;
    if (res instanceof ChannelBuffer)
    {
      ChannelBuffer b = (ChannelBuffer) res;
      b.readerIndex(0);
      return b.readFloat();
    }

    return 0;
  }


  @Override
  public String toString()
  {
    return "ResponseFuture[" + response + "]";
  }
}
