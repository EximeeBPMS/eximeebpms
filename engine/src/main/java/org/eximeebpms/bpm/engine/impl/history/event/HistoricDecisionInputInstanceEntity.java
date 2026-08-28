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
package org.eximeebpms.bpm.engine.impl.history.event;

import java.io.Serial;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.history.HistoricDecisionInputInstance;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.util.ByteArrayField;
import org.eximeebpms.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.eximeebpms.bpm.engine.impl.variable.serializer.ValueFields;
import org.eximeebpms.bpm.engine.repository.ResourceTypes;
import org.eximeebpms.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class HistoricDecisionInputInstanceEntity extends HistoryEvent implements HistoricDecisionInputInstance, ValueFields {

  @Serial
  private static final long serialVersionUID = 1L;

  @Setter
  protected String decisionInstanceId;

  @Setter
  protected String clauseId;
  @Setter
  protected String clauseName;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  @Getter
  @Setter
  protected String tenantId;

  protected ByteArrayField byteArrayField;
  protected TypedValueField typedValueField = new TypedValueField(this, false);

  /**
   * Not persisted. Holds the value recorded by {@link #setValue(TypedValue)} until
   * {@link #materializeValue()} applies it. Applying it in the producer would insert a
   * byte array for an object-typed value before anything had decided the row it belongs
   * to would exist (BPMS-662).
   */
  protected TypedValue pendingValue;


  @Setter
  protected Date createTime;

  public HistoricDecisionInputInstanceEntity() {
    byteArrayField = new ByteArrayField(this, ResourceTypes.HISTORY);
  }

  public HistoricDecisionInputInstanceEntity(String rootProcessInstanceId, Date removalTime) {
    this.rootProcessInstanceId = rootProcessInstanceId;
    this.removalTime = removalTime;
    byteArrayField = new ByteArrayField(this, ResourceTypes.HISTORY, getRootProcessInstanceId(), getRemovalTime());
  }

  @Override
  public String getDecisionInstanceId() {
    return decisionInstanceId;
  }

  @Override
  public String getClauseId() {
    return clauseId;
  }

  @Override
  public String getClauseName() {
    return clauseName;
  }

  @Override
  public Date getCreateTime() {
    return createTime;
  }

  @Override
  public String getTypeName() {
    return typedValueField.getTypeName();
  }

  public void setTypeName(String typeName) {
    typedValueField.setSerializerName(typeName);
  }

  @Override
  public Object getValue() {
    return pendingValue != null ? pendingValue.getValue() : typedValueField.getValue();
  }

  @Override
  public TypedValue getTypedValue() {
    return pendingValue != null ? pendingValue : typedValueField.getTypedValue(false);
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    return pendingValue != null ? pendingValue : typedValueField.getTypedValue(deserializeValue, false);
  }

  @Override
  public String getErrorMessage() {
    return typedValueField.getErrorMessage();
  }

  @Override
  public String getName() {
    // used for save a byte value
    return clauseId;
  }

  @Override
  public String getTextValue() {
    return textValue;
  }

  @Override
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  @Override
  public String getTextValue2() {
    return textValue2;
  }

  @Override
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  @Override
  public Long getLongValue() {
    return longValue;
  }

  @Override
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  @Override
  public Double getDoubleValue() {
    return doubleValue;
  }

  @Override
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public String getByteArrayValueId() {
    return byteArrayField.getByteArrayId();
  }

  public void setByteArrayValueId(String byteArrayId) {
    byteArrayField.setByteArrayId(byteArrayId);
  }

  @Override
  public byte[] getByteArrayValue() {
    return byteArrayField.getByteArrayValue();
  }

  @Override
  public void setByteArrayValue(byte[] bytes) {
    byteArrayField.setByteArrayValue(bytes);
  }

  public void setValue(TypedValue typedValue) {
    this.pendingValue = typedValue;
  }

  /**
   * Applies the value recorded by {@link #setValue(TypedValue)}, creating the byte array
   * for an object-typed value. Called immediately before this entity is inserted.
   */
  public void materializeValue() {
    if (pendingValue != null) {
      typedValueField.setValue(pendingValue);
      pendingValue = null;
    }
  }

  public String getSerializerName() {
    return typedValueField.getSerializerName();
  }

  public void setSerializerName(String serializerName) {
    typedValueField.setSerializerName(serializerName);
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

    public void delete() {
    byteArrayField.deleteByteArrayValue();

    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

}
