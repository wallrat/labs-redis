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
package se.preemptive.redis.testing.bench;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class BenchTest
  {
    protected final int keySize;
    protected final int valueSize;
    protected final Map<String,String> options;

    public BenchTest(int keySize, int valueSize, Map<String,String> options)
    {
      this.options = options;
      this.keySize = keySize;
      this.valueSize = valueSize;
    }

    protected String createString(int size)
    {
      byte[] b = new byte[size];
      b[0] = 'S';
      b[size - 1] = 'E';
      Arrays.fill(b, (byte) 'a');
      return new String(b);
    }

    public void setup() {}

    public void teardown() {}

    public abstract Future run();

  }
