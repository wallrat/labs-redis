labs-redis
==========

'labs-redis' is a [Redis][0] client library for Java.

## Why? What about JRedis?

[JRedis][3] seems very solid with good performance. So why another client?

The rationale for labs-redis is part good, old, not-invented-here syndrome, and part wanting to evaluate Netty and Redis.
I'm in the process of deciding if we are going to use Redis in an upcoming project. _If_ so, I'll finish up the
labs-redis code and make it production stable. If not, then probably not.

The codebase is small though, so fork and hack to taste!

If we decide to use Redis for an upcoming project we'll probably make some customizations to the Redis server
and I would rather support our own codebase for the client side than patches against [JRedis][3].

The result is more or less the same performance, smaller codebase and different flavour of API.
Uses piplining with blocking writes and asynchronous reads. Lazy connection handling.

Evaluating Netty and Redis
--------------------------

So far i'm quite happy with the results. Gonna take a look at the Redis internals next.

Status
------

labs-redis is pre-alpha at this stage and is *not* ready for production. Although the low level stuff seems stable
enough, a lot of the redis API is untested. So if you need a Redis client for production, definitly take a look at [JRedis][3].

## Whats missing

The biggest missing piece is complete tests for the API. Some memory profiling and some low hanging optimization-fruit
is also wise before a production ready beta. We'll also need a sharding (consistend hashing) and channel multiplexing layer.

Examples
--------

labs-redis exposes two levels of API. The lowest level implements the Redis request/response protocol.

    // creates a protocol client connecting to 127.0.0.1
    RedisProtocolClient client = new RedisProtocolClient();

    // sends PING and prints response
    System.out.println("ping -> " + client.send("PING").asString());

We also expose a Redis command API

    RedisClient client = new RedisClient("127.0.0.1", 6379);

    // set a value and wait for server response
    // set() returns a ResponseFuture
    client.set("mykey", "myvalue").block();

    // the future allows client to control timeouts
    Object value = client.
      get("mykey").
      withTimeout(1, TimeUnit.SECONDS);

    System.out.println("mykey = " + value);

    // and response type demarshalling and conversions
    String value2 = client.get("mykey").asString();

    System.out.println("mykey = " + value2);


Performance
-----------

labs-redis contains a trivial tool (see BenchRedisClient) for running microbenchmarks. There are also
some tests for [JRedis][3] we used as a sanity-check when testing.

Example from test on Amazon EC2 (High CPU) instance to instance:

    [labs-redis]# ./labs-redis-bench.sh -n 600000 -l 10 -h $s2 Ping
    Options: {t=1, p=6379, n=600000, value=16, l=10, key=16, h=ip-10-228-110-31.eu-west-1.compute.internal}
                          Load           Write   Read         Complete          Average
    test        threads  items    ms     req/s     ms     ms     req/s     ms     req/s
    Ping              1 600000  2371    253058      1   2378    252313
    Ping              1 600000  1754    342075      1   1755    341880    878   341880
    Ping              1 600000  1694    354191      1   1695    353982   1150   347826
    Ping              1 600000  1664    360577      1   1669    359497   1280   351631
    Ping              1 600000  1602    374532      1   1603    374298   1344   357037
    Ping              1 600000  1762    340522      1   1763    340329   1414   353565
    Ping              1 600000  1768    339367      1   1770    338983   1465   351048
    Ping              1 600000  1658    361882      1   1659    361664   1489   352526
    Ping              1 600000  1634    367197      1   1636    366748   1506   354244
    Ping              1 600000  1621    370142      1   1622    369914   1517   355919

For small (16 bytes) keys and values [JRedis][3] is faster for Get/Set. For larger keys/values about the same.

    [labs-redis]# ./labs-redis-bench.sh -n 100000 -l 10 -h $s2 -key 16 -value 16 Get,JRedisGet
    Options: {t=1, p=6379, n=100000, value=16, l=10, key=16, h=ip-10-228-110-31.eu-west-1.compute.internal}
                          Load           Write   Read         Complete          Average
    test        threads  items    ms     req/s     ms     ms     req/s     ms     req/s
    Get               1 100000  1459     68540      0   1465     68259
    Get               1 100000   509    196464      1    510    196078    255   196078
    Get               1 100000   457    218818      0    458    218341    323   206612
    Get               1 100000   469    213220      1    474    210970    361   208044
    Get               1 100000   469    213220      1    470    212766    382   209205
    Get               1 100000   449    222717      1    450    222222    394   211685
    Get               1 100000   403    248139    125    529    189036    413   207541
    Get               1 100000   463    215983      0    464    215517    419   208644
    Get               1 100000   461    216920     15    476    210084    426   208823
    Get               1 100000   433    230947     38    472    211864    430   209156
    JRedisGet         1 100000   642    155763     43    686    145773
    JRedisGet         1 100000   488    204918     40    546    183150    273   183150
    JRedisGet         1 100000   435    229885     36    505    198020    350   190295
    JRedisGet         1 100000   414    241546     38    452    221239    376   199601
    JRedisGet         1 100000   404    247525     38    442    226244    389   205656
    JRedisGet         1 100000   402    248756     41    443    225734    398   209380
    JRedisGet         1 100000   400    250000     40    440    227273    404   212164
    JRedisGet         1 100000   426    234742     40    466    214592    412   212508
    JRedisGet         1 100000   426    234742     42    468    213675    418   212653
    JRedisGet         1 100000   408    245098     39    473    211416    424   212515

For bigger payloads (8k and up), both labs-redis and jredis saturates the ec2 instance to instance bandwidth

    [labs-redis]# ./labs-redis-bench.sh -n 100000 -l 10 -h $s2 -key 16 -value 32768 Set,JRedisSet
    Options: {t=1, p=6379, n=100000, value=32768, l=10, key=16, h=ip-10-228-110-31.eu-west-1.compute.internal}
                          Load           Write   Read         Complete          Average
    test        threads  items    ms     req/s     ms     ms     req/s     ms     req/s
    Set               1 100000 32560      3071     26  32590      3068
    Set               1 100000 31519      3173     19  31539      3171  15770     3171
    Set               1 100000 31739      3151     30  31769      3148  21103     3159
    Set               1 100000 31333      3192     25  31359      3189  23667     3169
    Set               1 100000 30897      3237     33  30931      3233  25120     3185
    Set               1 100000 31395      3185     33  31428      3182  26171     3184
    Set               1 100000 30956      3230     24  30981      3228  26858     3191
    Set               1 100000 31280      3197     11  31291      3196  27412     3192
    Set               1 100000 30064      3326     14  30078      3325  27708     3208
    Set               1 100000 31383      3186     38  31421      3183  28080     3205
    JRedisSet         1 100000 34449      2903      1  34450      2903
    JRedisSet         1 100000 34333      2913      1  34334      2913  17167     2913
    JRedisSet         1 100000 33852      2954      1  33853      2954  22729     2933
    JRedisSet         1 100000 34713      2881      2  34716      2881  25726     2915
    JRedisSet         1 100000 34188      2925      2  34200      2924  27421     2918
    JRedisSet         1 100000 33663      2971      2  33679      2969  28464     2928
    JRedisSet         1 100000 33628      2974      1  33629      2974  29202     2935
    JRedisSet         1 100000 33207      3011      1  33209      3011  29703     2946
    JRedisSet         1 100000 32998      3030      2  33000      3030  30069     2956
    JRedisSet         1 100000 34373      2909      2  34375      2909  30500     2951



Dependencies
------------

labs-redis depends on [Netty 3.2.0-ALPHA3][1] for production and [TestNG 5.11][2] for running unit tests.
Benchmarking code contains tests for [JRedis][3] and is needed for compiling and running JRedis benchmarks.

All three jars are present in lib/ on github.


Building
--------

    [/tmp]$ git clone git@github.com:wallrat/labs-redis.git
    ..
    [/tmp/labs-redis]$ ant
    Buildfile: build.xml

    compile.core.production:
        [mkdir] Created dir: /tmp/labs-redis/build.ant/production/core
        [javac] Compiling 26 source files to /tmp/labs-redis/build.ant/production/core

    jar:
          [jar] Building jar: /tmp/labs-redis/build.ant/labs-redis-0.1.0.ALPHA.jar
          [jar] Building jar: /tmp/labs-redis/build.ant/labs-redis-0.1.0.ALPHA.jar

    all:

    BUILD SUCCESSFUL
    Total time: 2 seconds

    [/tmp/labs-redis]$ ./labs-redis-bench.sh
    Usage: BenchRedisClient [-h <host>] [-p <port>] [-key <length>] [-value <length>] [-n <requests>] [-t <threads>] [-l <loops>] <classname(s)>

    <classname(s)>      One or more tests to run (use , as separator)

    -h <host>           Server hostname (default 127.0.0.1)
    -p <port>           Server port (default 6379)
    -t <threads>        Run parallel tests in n threads (total requests = requests * threads) (default 1)
    -n <requests>       Run n requests in each test (default 100000)
    -l <loops>          Run all tests n times. Use 0 to loop tests forever (default 1)
    -key <length>       Key lengths in bytes (default 16)
    -value <size>       Value sizes in bytes (default 16)


Author
------

Andreas Bielk :: andreas@bielk.se :: @wallrat


[0]: http://code.google.com/p/redis/
[1]: https://www.jboss.org/netty
[2]: http://testng.org/doc/download.html
[3]: http://code.google.com/p/jredis/
