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
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import se.preemptive.redis.util.RedisClientError;

@ChannelPipelineCoverage("one")
public class FrameDecoder extends org.jboss.netty.handler.codec.frame.FrameDecoder
{
  public static final ChannelBuffer CRLF = ChannelBuffers.wrappedBuffer(new byte[]{'\r', '\n'});

  public static enum Response
  {
    NIL
  }

  // multi-bulk-buffer
  private volatile Object[] multiBulkBuffer = null;
  private volatile int multiBulkBufferIndex = 0;

  // debug counters
  private volatile int frames = 0;
  private volatile int calls = 0;

  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
    throws Exception
  {
    calls++;

    // read first byte
    int availableBytes = buffer.readableBytes();

    // shortes meaningful frame is 4 bytes (- x CR LF)
    if (availableBytes < 4) return null;

    buffer.markReaderIndex();

    // read replytype
    final byte firstByte = buffer.readByte();

    if (firstByte == '+' || firstByte == '-' || firstByte == ':')
    {
      // find CRLF
      int idx = indexOfCRLF(buffer);

      if (idx == -1)
      {
        buffer.resetReaderIndex();
        return null;
      }

      frames++;
      ChannelBuffer frame = buffer.readBytes(idx);

      buffer.skipBytes(2);

      // handle error replies
      if (firstByte == '-')
      {
        throw new RedisClientError(frame.toString("UTF-8"));
        //throw new Error();
      }

      return frame;
    }
    else if (firstByte == '*')
    {
      // find CRLF
      int idx = indexOfCRLF(buffer);

      if (idx == -1)
      {
        buffer.resetReaderIndex();
        return null;
      }

      // read i
      ChannelBuffer lenb = buffer.readBytes(idx);
      String lens = lenb.toString("US-ASCII");
      int len = Integer.parseInt(lens);

      // skip CRLF
      buffer.skipBytes(2);

      // handle multibulk responses by accumelating in an array
      {
        multiBulkBuffer = new Object[len];
        multiBulkBufferIndex = 0;
      }

      // dispath to decode more frames
      return null;
    }
    else if (firstByte == '$')
    {
      // find CRLF
      int idx = indexOfCRLF(buffer);

      if (idx == -1)
      {
        buffer.resetReaderIndex();
        return null;
      }

      // read i
      final int replyLength = atoi(buffer, idx);

      // skip CRLF
      buffer.skipBytes(2);

      // handle len = -1
      if (replyLength == -1)
      {
        //System.out.println("Got null response " + buffer.readableBytes());

        // accumelate?
        if (multiBulkBuffer != null)
        {
          // we don't use NIL in multiBulk
          //multiBulkBuffer[multiBulkBufferIndex++] = FrameDecoder.Resonse.NIL;
          multiBulkBufferIndex++;

          if (multiBulkBufferIndex == multiBulkBuffer.length)
          {
            Object[] mbb = multiBulkBuffer;
            multiBulkBuffer = null;
            return mbb;
          }
          else
            return null;
        }

        // dispatch frame
        frames++;
        return Response.NIL;
      }

      // do we have enough bytes to read the whole reply? (frame fragmentation)
      if (availableBytes - 1 - idx - 2 < (replyLength + 2))
      {
        //System.out.println("Not enough bytes left at call "+calls+" wanted "+len+" available "+(availableBytes-1-idx-2));
        //System.out.println(buffer.readableBytes());
        buffer.resetReaderIndex();
        return null;
      }

      ChannelBuffer frame = buffer.readBytes(replyLength);

      // skip CRLF
      buffer.skipBytes(2);

      // accumelate?
      if (multiBulkBuffer != null)
      {
        multiBulkBuffer[multiBulkBufferIndex++] = frame;
        if (multiBulkBufferIndex == multiBulkBuffer.length)
        {
          Object[] mbb = multiBulkBuffer;
          multiBulkBuffer = null;
          frames++;
          return mbb;
        }
        else
          return null;
      }

      // dispatch frame
      frames++;
      return frame;
    }
    else
    {
      buffer.skipBytes(availableBytes - 1);
      System.out.println("Uknown reply type at call " + calls + " type " + ((char) firstByte));
      System.out.println(buffer.readableBytes());

      channel.close().awaitUninterruptibly();
      throw new Exception("Uknown reply type " + ((char) firstByte));
    }
  }


  /**
   * Returns the number of bytes between the readerIndex of the haystack and CRLF
   */
  private int indexOfCRLF(ChannelBuffer h)
  {
    // this is a really dirty thing to do to a Netty provided buffer
    byte[] haystack = h.array();

    int ri = h.readerIndex();
    int wi = h.writerIndex();
    for (int i = ri; i < wi - 1;)
    {
      byte b = haystack[i];
      byte b2 = haystack[i + 1];
      if (b == '\r')
        if (b2 == '\n')
          return i - ri;
        else
          i++;
      else if (b2 != '\r')
        i++;
      i++;
    }

    return -1;

  }

  private int atoi(ChannelBuffer buffer, int maxlen)
  {
    //assert maxlen > buffer.readableBytes();

    //ChannelBuffer lenb = buffer.readBytes(maxlen);
    //String s = lenb.toString("UTF-8");
    //return Integer.parseInt(s);

    // dirty atoi() implementation so we don't have
    // to create String-copies of the buffer

    boolean neg = false;
    int result = 0;
    int cnt = 0;
    do
    {
      byte c = (byte) buffer.readByte();

      if (c == '-' && cnt == 0)
      {
        neg = true;
        continue;
      }

      if (c < '0' || c > '9')
        throw new NumberFormatException("Found char '" + ((char) c) + " while decoding frame");

      result = result * 10 + (c - '0');
    }
    while (++cnt < maxlen);

    return neg ? result * -1 : result;
  }

  public int getFrames()
  {
    return frames;
  }

  public int getCalls()
  {
    return calls;
  }
}