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

import se.preemptive.redis.commands.RedisSortCommands;

/**
 * The standard client mixin of all redis commands
 */
public class RedisClient extends RedisSortCommands
{
  /**
   * Creates a client for 127.0.0.1 port 6379
   */
  public RedisClient()
  {
    super(new RedisProtocolClient());
  }

  public RedisClient(String host, int port)
  {
    super(new RedisProtocolClient(host, port));
  }

  public RedisClient(RedisProtocolClient client)
  {
    super(client);
  }
}
