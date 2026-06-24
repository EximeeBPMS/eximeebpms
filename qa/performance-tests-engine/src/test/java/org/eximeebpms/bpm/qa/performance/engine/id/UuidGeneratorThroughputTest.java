/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.qa.performance.engine.id;

import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <p>Throughput benchmark for UUID generator strategies.</p>
 *
 * <p>Measures and compares the throughput (IDs/second) of:
 * <ul>
 *   <li>{@link StrongUuidGenerator} — UUID v7 (time-ordered epoch, RFC 9562) — <b>default</b></li>
 *   <li>UuidV1Generator — UUID v1 (time-based, deprecated, will be removed in 1.4.0)</li>
 * </ul>
 *
 * <p>This test does <b>not</b> require a database connection. It measures pure generator throughput,
 * which directly impacts INSERT performance in high-throughput scenarios
 * (B-Tree index fragmentation with random UUIDs vs. sequential with time-ordered UUIDs).</p>
 *
 * <p>Results are written to {@code target/id-generator-benchmark.csv} and printed to stdout.</p>
 *
 * <p>Run explicitly — excluded from default Surefire execution:</p>
 * <pre>
 *   mvn test -pl qa/performance-tests-engine -Dtest=UuidGeneratorThroughputTest
 * </pre>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UuidGeneratorThroughputTest {

  private static final int WARMUP_ITERATIONS  = 100_000;
  private static final int MEASURE_ITERATIONS = 1_000_000;

  private static final String RESULTS_DIR = "target";
  private static final String CSV_FILE    = RESULTS_DIR + "/id-generator-benchmark.csv";

  private static final List<String[]> csvRows = new ArrayList<>();

  @BeforeClass
  public static void prepareResultsDir() throws IOException {
    Files.createDirectories(Paths.get(RESULTS_DIR));
    csvRows.add(new String[]{ "generator", "threads", "iterations", "durationMs", "throughputPerSec", "avgLatencyNs" });
  }

  // ────────────────────────────────────────────────────────────────────────────
  // UUID v7 — StrongUuidGenerator (default)
  // ────────────────────────────────────────────────────────────────────────────

  @Test
  public void a_uuidV7_singleThread() throws InterruptedException {
    runBenchmark("UUID v7 (StrongUuidGenerator — default)", new StrongUuidGenerator(), 1);
  }

  @Test
  public void b_uuidV7_multiThread_2() throws InterruptedException {
    runBenchmark("UUID v7 (StrongUuidGenerator — default)", new StrongUuidGenerator(), 2);
  }

  @Test
  public void c_uuidV7_multiThread_4() throws InterruptedException {
    runBenchmark("UUID v7 (StrongUuidGenerator — default)", new StrongUuidGenerator(), 4);
  }

  @Test
  public void d_uuidV7_multiThread_8() throws InterruptedException {
    runBenchmark("UUID v7 (StrongUuidGenerator — default)", new StrongUuidGenerator(), 8);
  }

  // ────────────────────────────────────────────────────────────────────────────
  // UUID v1 — UuidV1Generator (deprecated)
  // ────────────────────────────────────────────────────────────────────────────

  @Test
  @SuppressWarnings("removal")
  public void e_uuidV1_singleThread() throws InterruptedException {
    runBenchmark("UUID v1 (UuidV1Generator — deprecated)", new org.eximeebpms.bpm.engine.impl.persistence.UuidV1Generator(), 1);
  }

  @Test
  @SuppressWarnings("removal")
  public void f_uuidV1_multiThread_2() throws InterruptedException {
    runBenchmark("UUID v1 (UuidV1Generator — deprecated)", new org.eximeebpms.bpm.engine.impl.persistence.UuidV1Generator(), 2);
  }

  @Test
  @SuppressWarnings("removal")
  public void g_uuidV1_multiThread_4() throws InterruptedException {
    runBenchmark("UUID v1 (UuidV1Generator — deprecated)", new org.eximeebpms.bpm.engine.impl.persistence.UuidV1Generator(), 4);
  }

  @Test
  @SuppressWarnings("removal")
  public void h_uuidV1_multiThread_8() throws InterruptedException {
    runBenchmark("UUID v1 (UuidV1Generator — deprecated)", new org.eximeebpms.bpm.engine.impl.persistence.UuidV1Generator(), 8);
  }

  // ────────────────────────────────────────────────────────────────────────────
  // Write CSV after all tests
  // ────────────────────────────────────────────────────────────────────────────

  @Test
  public void z_writeCsvReport() throws IOException {
    Path csvPath = Paths.get(CSV_FILE);
    try (FileWriter writer = new FileWriter(csvPath.toFile())) {
      for (String[] row : csvRows) {
        writer.write(String.join(";", row));
        writer.write("\n");
      }
    }
    System.out.println("\n[UuidGeneratorThroughputTest] Results written to: " + csvPath.toAbsolutePath());
    Assert.assertTrue("CSV report should exist", csvPath.toFile().exists());
  }

  // ────────────────────────────────────────────────────────────────────────────
  // Core benchmark logic
  // ────────────────────────────────────────────────────────────────────────────

  private void runBenchmark(String label, IdGenerator generator, int threads) throws InterruptedException {
    // warmup — avoid JIT cold start affecting results
    for (int i = 0; i < WARMUP_ITERATIONS; i++) {
      generator.getNextId();
    }

    int iterationsPerThread = MEASURE_ITERATIONS / threads;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch  = new CountDownLatch(threads);
    AtomicLong totalIds = new AtomicLong(0);
    long[] durationNsRef = new long[1];

    try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
      for (int t = 0; t < threads; t++) {
        executor.submit(() -> {
          try {
            startLatch.await();
            long count = 0;
            for (int i = 0; i < iterationsPerThread; i++) {
              generator.getNextId();
              count++;
            }
            totalIds.addAndGet(count);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      long startNs = System.nanoTime();
      startLatch.countDown();
      boolean finished = doneLatch.await(60, TimeUnit.SECONDS);
      durationNsRef[0] = System.nanoTime() - startNs;

      Assert.assertTrue("Benchmark did not finish within 60 seconds", finished);
    }

    long durationNs       = durationNsRef[0];
    long durationMs       = TimeUnit.NANOSECONDS.toMillis(durationNs);
    long totalGenerated   = totalIds.get();
    long throughputPerSec = durationNs > 0 ? (totalGenerated * 1_000_000_000L) / durationNs : 0;
    long avgLatencyNs     = totalGenerated > 0 ? durationNs / totalGenerated : 0;

    String output = String.format(
        "[%s] threads=%d | iterations=%,d | duration=%,d ms | throughput=%,d IDs/s | avg latency=%d ns",
        label, threads, totalGenerated, durationMs, throughputPerSec, avgLatencyNs);
    System.out.println(output);

    csvRows.add(new String[]{
        label,
        String.valueOf(threads),
        String.valueOf(totalGenerated),
        String.valueOf(durationMs),
        String.valueOf(throughputPerSec),
        String.valueOf(avgLatencyNs)
    });

    // sanity check — all IDs were generated
    Assert.assertEquals("All iterations should complete", (long) iterationsPerThread * threads, totalGenerated);
  }

}

