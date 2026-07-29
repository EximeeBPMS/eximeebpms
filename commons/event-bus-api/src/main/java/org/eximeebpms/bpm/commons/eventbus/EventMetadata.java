package org.eximeebpms.bpm.commons.eventbus;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;

@Builder
public record EventMetadata(
    Instant timestamp,
    @NotBlank(message = "Event UUID is required") String uuid,
    @NotBlank(message = "Event type is required") String type,
    @NotBlank(message = "Event version is required") String version,
    @NotBlank(message = "Event origin is required") String origin,
    String correlationId,
    String processInstanceId,
    String processDefinitionKey,
    boolean noProcessContext
) implements Serializable {

  private static final String NO_PROCESS_CONTEXT_VALUE = "no-process-context";

  @Serial
  private static final long serialVersionUID = 1L;

  @AssertTrue(message = "processInstanceId is required unless noProcessContext = true")
  private boolean isProcessKeyValid() {
    return noProcessContext || (processInstanceId != null && !processInstanceId.isBlank());
  }

  @AssertTrue(message = "processDefinitionKey is required unless noProcessContext = true")
  private boolean isProcessNameValid() {
    return noProcessContext || (processDefinitionKey != null && !processDefinitionKey.isBlank());
  }

  public EventMetadata {
    if (noProcessContext) {
      processInstanceId = NO_PROCESS_CONTEXT_VALUE;
      processDefinitionKey = NO_PROCESS_CONTEXT_VALUE;
    }
  }
}
