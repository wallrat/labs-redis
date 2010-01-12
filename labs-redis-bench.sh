CP=build.idea/production/core/:build.ant/labs-redis-0.1.0.ALPHA.jar
for j in lib/*.jar; do
  CP=$CP:$j;
  done
export CP
# -XX:+UnlockDiagnosticVMOptions 
java -server -Xms512M -Xmx512M -XX:CompileThreshold=1000 -Xbatch -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -classpath $CP se.preemptive.redis.testing.BenchRedisClient $*
