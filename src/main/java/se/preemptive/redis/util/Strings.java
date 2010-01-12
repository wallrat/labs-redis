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

package se.preemptive.redis.util;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class Strings
{
  public static final Charset US_ASCII = Charset.forName("US-ASCII");
  public static final Charset UTF8 = Charset.forName("UTF-8");

  public static String join(char c, String first, String... rest)
  {
    // sum size
    int size = first.length();
    for (String s : rest)
      size += s.length();

    StringBuilder s = new StringBuilder(size + rest.length);
    s.append(first);
    for (String r : rest)
    {
      s.append(c);
      s.append(r);
    }
    return s.toString();
  }

  public static String join(char c, String first, String second, String... rest)
  {
    // sum size
    int size = first.length() + second.length();
    for (String s : rest)
      size += s.length();

    StringBuilder s = new StringBuilder(size + rest.length);
    s.append(first);
    s.append(second);
    for (String r : rest)
    {
      s.append(c);
      s.append(r);
    }
    return s.toString();
  }

  public static <V> V[] array(V... args) { return args; }

  public static <V> List<V> list(V... args) {return Arrays.asList(args);}
}