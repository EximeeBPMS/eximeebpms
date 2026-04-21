package org.eximeebpms.trustedcode.plugin;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TrustedCodeViolation {
  String ruleCode;
  String resourceName;
  String processDefinitionKey;
  String elementId;
  String elementName;
  String elementType;
  String message;
}
