package org.eximeebpms.bpm.spring.boot.starter.property;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FetchAndLockPropertiesTest {

  final FetchAndLockProperties uut = new FetchAndLockProperties();

  @ParameterizedTest
  @CsvSource({
      "true,100,true,100",
      "true,200,true,",
      "false,100,,100",
      "false,200,,"
  })
  void shouldGetValidInitParams(
      final boolean uniqueWorkerRequest,
      final int queueCapacity,
      final String expectedUniqueWorker,
      final String expectedQueueCapacity) {
    // given
    uut.setUniqueWorkerRequest(uniqueWorkerRequest);
    uut.setQueueCapacity(queueCapacity);

    // when
    final Map<String, String> result = uut.getInitParams();

    // then
    if (isNotBlank(expectedUniqueWorker)) {
      assertThat(result).contains(entry("fetch-and-lock-unique-worker-request", expectedUniqueWorker));
    }
    if (isNotBlank(expectedQueueCapacity)) {
      assertThat(result).contains(entry("fetch-and-lock-queue-capacity", expectedQueueCapacity));
    }
  }
}