package org.eximeebpms.bpm.spring.boot.starter.property;

import static org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptSecurityProperty {

  private boolean enabled = true;

  @Override
  public String toString() {
    return joinOn(this.getClass())
        .add("enabled=" + enabled)
        .toString();
  }

}
