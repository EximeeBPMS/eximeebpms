package org.eximeebpms.bpm.commons.eventbus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Builder;

@Builder
public record Event(
    @NotNull(message = "Headers are required") @Valid EventMetadata metadata,
    @NotNull(message = "Payload is required") String payload
) implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

}
