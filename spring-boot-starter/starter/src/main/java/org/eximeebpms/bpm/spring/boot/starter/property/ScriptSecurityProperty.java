package org.eximeebpms.bpm.spring.boot.starter.property;

import static org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptSecurityProperty {

  public enum Mode {
    ENFORCE,
    AUDIT,
    DISABLED
  }

  private Mode mode = Mode.ENFORCE;

  private Set<String> allowlistedProcessDefinitionKeys = new HashSet<>();

  private int violationStoreSize = 1000;

  /**
   * Number of days to retain script violation records in ACT_RU_SCRIPT_VIOLATION.
   * Records older than this value are deleted by the periodic cleanup job.
   * Set to 0 (default) to disable automatic cleanup.
   */
  private int retentionDays = 0;

  public boolean isDisabled() {
    return mode == Mode.DISABLED;
  }

  public boolean isAuditMode() {
    return mode == Mode.AUDIT;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
        .add("mode=" + mode)
        .add("allowlistedProcessDefinitionKeys=" + allowlistedProcessDefinitionKeys)
        .add("violationStoreSize=" + violationStoreSize)
        .add("retentionDays=" + retentionDays)
        .toString();
  }

}
