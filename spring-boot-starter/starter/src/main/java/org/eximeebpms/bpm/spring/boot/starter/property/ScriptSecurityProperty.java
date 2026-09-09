package org.eximeebpms.bpm.spring.boot.starter.property;

import static org.eximeebpms.bpm.spring.boot.starter.property.EximeeBpmsBpmProperties.joinOn;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityMode;

@Getter
@Setter
public class ScriptSecurityProperty {

  private ScriptSecurityMode mode = ScriptSecurityMode.ENFORCE;

  private Set<String> allowlistedProcessDefinitionKeys = new HashSet<>();

  private int violationStoreSize = 1000;

  /**
   * Number of days to retain script violation records in ACT_RU_SCRIPT_VIOLATION.
   * Records older than this value are deleted by the periodic cleanup job.
   * Set to 0 (default) to disable automatic cleanup.
   */
  private int retentionDays = 0;

  public boolean isDisabled() {
    return mode == ScriptSecurityMode.DISABLED;
  }

  public boolean isAuditMode() {
    return mode == ScriptSecurityMode.AUDIT;
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
