package org.eximeebpms.bpm.commons.eventbus;

import javax.validation.constraints.NotNull;
import java.util.Map;

public interface BusinessEventPublisher extends AutoCloseable {

  String getName();

  default void init(Map<String, String> properties) {
  }

  BusinessEventPublishResult publish(@NotNull Event event);

  @Override
  default void close() {
  }
}
