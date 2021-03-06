/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.cache;

import static org.apache.geode.distributed.ConfigurationProperties.LOG_LEVEL;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Fork(1)
public class UpdateOnRegionBenchmark {
  private static final int ENTRIES = 1_000_000;

  Cache cache;
  Region<String, String> region;

  @Setup(Level.Trial)
  public void setup() {
    cache = new CacheFactory().set(LOG_LEVEL, "warn").create();
    region = createRegion(cache, ENTRIES);
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    cache.close();
  }

  @State(Scope.Thread)
  public static class MyState {
    Random random = new Random();
  }

  @Benchmark
  @Measurement(iterations = 50)
  @Warmup(iterations = 5)
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public String updateRegion(MyState state) {
    String key = Integer.toString(state.random.nextInt(ENTRIES));
    return region.put(key, "value");
  }

  private Region<String, String> createRegion(Cache cache, int maxSize) {
    Region<String, String> region = cache.<String, String>createRegionFactory(RegionShortcut.LOCAL)
        .setEvictionAttributes(
            EvictionAttributes.createLRUEntryAttributes(maxSize, EvictionAction.LOCAL_DESTROY))
        .create("testRegion");
    for (int i = 0; i < ENTRIES; i++) {
      region.put(Integer.toString(i), "value");
    }
    return region;
  }
}
