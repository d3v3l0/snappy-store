/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.pdx.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.pdx.FieldType;

public class PdxField implements DataSerializable, Comparable<PdxField> {

  private static final long serialVersionUID = -1095459461236458274L;
  
  private String fieldName;
  private int fieldIndex;
  private int varLenFieldSeqId;
  private FieldType type;
  /**
   * If >= 0 then it is relative to the first byte of field data.
   * Otherwise it is relative to the base determined by
   * vlfOffsetIndex.
   */
  private int relativeOffset;

  /**
   * if >= 0 then it is the index of the vlfOffsets that
   * this field should use as its base to find its data.
   * If < 0 then it should be -1 which means the base
   * is the first byte after the last byte of field data.
   */
  private int vlfOffsetIndex;
  
  private boolean identityField;

  public PdxField() {
  }

  public PdxField(String fieldName, int index, int varId,
      FieldType type, boolean identityField) {
    this.fieldName = fieldName;
    this.fieldIndex = index;
    this.varLenFieldSeqId = varId;
    this.type = type;
    this.identityField = identityField;
  }

  /**
   * Used by {@link PdxInstanceImpl#equals(Object)} to act as if it has
   * a field whose value is always the default.
   */
  protected PdxField(PdxField other) {
    this.fieldName = other.fieldName;
    this.fieldIndex = other.fieldIndex;
    this.varLenFieldSeqId = other.varLenFieldSeqId;
    this.type = other.type;
    this.identityField = other.identityField;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public int getFieldIndex() {
    return this.fieldIndex;
  }

  public int getVarLenFieldSeqId() {
    return this.varLenFieldSeqId;
  }

  public boolean isVariableLengthType() {
    return !this.type.isFixedWidth();
  }

  public FieldType getFieldType() {
    return this.type;
  }

  public int getRelativeOffset() {
    return this.relativeOffset;
  }

  public void setRelativeOffset(int relativeOffset) {
    this.relativeOffset = relativeOffset;
  }

  public int getVlfOffsetIndex() {
    return this.vlfOffsetIndex;
  }

  public void setVlfOffsetIndex(int vlfOffsetIndex) {
    this.vlfOffsetIndex = vlfOffsetIndex;
  }
  
  public void setIdentityField(boolean identityField) {
    this.identityField = identityField;
  }
  
  public boolean isIdentityField() {
    return this.identityField;
  }
  
  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.fieldName = DataSerializer.readString(in);
    this.fieldIndex = in.readInt();
    this.varLenFieldSeqId = in.readInt();
    this.type = DataSerializer.readEnum(FieldType.class, in);
    this.relativeOffset = in.readInt();
    this.vlfOffsetIndex = in.readInt();
    this.identityField = in.readBoolean();
  }

  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeString(this.fieldName, out);
    out.writeInt(this.fieldIndex);
    out.writeInt(this.varLenFieldSeqId);
    DataSerializer.writeEnum(this.type, out);
    out.writeInt(this.relativeOffset);
    out.writeInt(this.vlfOffsetIndex);
    out.writeBoolean(this.identityField);
  }
  
  @Override
  public int hashCode() {
    int hash =1 ;
    if(fieldName != null) {
      hash = hash*31 + fieldName.hashCode();
    }
    if(type != null) {
      hash = hash * 31 + type.hashCode();
    }
    
    return hash;
  }

  //We don't compare the offsets here, because
  //this method is to see if two different PDXTypes
  //have equivalent fields. See PdxReaderImpl.equals.
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other == null || !(other instanceof PdxField)) {
      return false;
    }  
    PdxField  otherVFT = (PdxField)other;
  
    if (otherVFT.fieldName == null) {
      return false;
    }

    if (otherVFT.fieldName.equals(this.fieldName)
        && this.type.equals(otherVFT.type)) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return this.fieldName + ":" + this.type
    + (isIdentityField() ? ":identity" : "")
    + ":" + this.fieldIndex 
    + ((this.varLenFieldSeqId > 0) ? (":" + this.varLenFieldSeqId) : "")
    + ":idx0(relativeOffset)=" + this.relativeOffset + ":idx1(vlfOffsetIndex)=" + this.vlfOffsetIndex;
  }

  public String getTypeIdString() {
    return getFieldType().toString();
  }

  public int compareTo(PdxField o) {
    return getFieldName().compareTo(o.getFieldName());
  }
}
