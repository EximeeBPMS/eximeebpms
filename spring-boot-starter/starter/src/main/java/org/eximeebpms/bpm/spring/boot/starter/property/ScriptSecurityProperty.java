package org.eximeebpms.bpm.spring.boot.starter.property;

import static org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptSecurityProperty {

  private boolean enabled = true;
  private Set<String> allowlistedProcessDefinitionKeys = new HashSet<>();

  @Override
  public String toString() {
    return joinOn(this.getClass())
        .add("enabled=" + enabled)
        .add("allowlistedProcessDefinitionKeys=" + allowlistedProcessDefinitionKeys)
        .toString();
  }

}
