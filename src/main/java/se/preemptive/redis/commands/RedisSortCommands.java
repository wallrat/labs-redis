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
package se.preemptive.redis.commands;

import se.preemptive.redis.RedisProtocolClient;
import se.preemptive.redis.ResponseFuture;

public class RedisSortCommands extends RedisSortedSetCommands
{
  interface Send
  {
    ResponseFuture send();
  }

  interface Store extends Send
  {
    Send store(String dstKey);
  }

  interface Alpha extends Store
  {
    Store alpha();
  }

  interface Desc extends Alpha
  {
    Alpha desc();
  }

  interface Get extends Desc
  {
    Desc get(String pattern);
  }

  interface Limit extends Get
  {
    Get limit(int idx, int items);
  }

  interface By extends Limit
  {
    Limit by(String pattern);
  }

  public RedisSortCommands(RedisProtocolClient client)
  {
    super(client);
  }

  class SortBuilder implements By
  {
    private RedisProtocolClient client;
    private StringBuffer s = new StringBuffer("SORT ");

    public SortBuilder(RedisProtocolClient client, String key)
    {
      this.client = client;
      s.append(key);
    }

    @Override
    public Limit by(String pattern)
    {
      s.append(" BY ").append(pattern);
      return this;
    }

    @Override
    public Get limit(int idx, int items)
    {
      s.append(" LIMIT ").append(idx).append(' ').append(items);
      return this;
    }

    @Override
    public Desc get(String pattern)
    {
      s.append(" GET ").append(pattern);
      return this;
    }

    @Override
    public Alpha alpha()
    {
      s.append(" ALPHA");
      return this;
    }

    @Override
    public Desc desc()
    {
      s.append(" DESC");
      return this;
    }

    @Override
    public Send store(String dstKey)
    {
      s.append(" STORE ").append(dstKey);
      return this;
    }

    @Override
    public ResponseFuture send()
    {
      return client.send(s.toString());
    }
  }


  public By sort(String key)
  {
    return new SortBuilder(client, key);
  }
}
