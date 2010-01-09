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
import se.preemptive.redis.netty.FrameDecoder;
import se.preemptive.redis.netty.ReadonlyCompositeChannelBuffer;

import java.nio.ByteOrder;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

public class BufferCreationBench
{
  public static void main(String[] args)
  {
    ChannelBuffer value = copiedBuffer("asdasdasdasdasdasdasd", "UTF-8");

    ChannelBuffer staticSetCommand = null;

    long started = System.currentTimeMillis();
    int items = 1000000 * 10;

    for (int i = 0; i < items; i++)
    {
      //staticSetCommand = wrappedBuffer(copiedBuffer("SET " + "mykey" + " " + value.readableBytes() + "\r\n", "UTF-8"), value, FrameDecoder.CRLF);
      staticSetCommand = new ReadonlyCompositeChannelBuffer(ByteOrder.BIG_ENDIAN, copiedBuffer("SET " + "mykey" + " " + value.readableBytes() + "\r\n", "UTF-8"), value, FrameDecoder.CRLF);
    }

    {
      long took = System.currentTimeMillis() - started;
      double msperitem = took / items;
      double itemspers = items / (took / 1000.0);
      System.out.println("took = " + took);
      System.out.println("msperitem = " + msperitem);
      System.out.println("itemspers = " + itemspers);
    }

    System.out.println("staticSetCommand = " + staticSetCommand);
  }
}
