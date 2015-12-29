/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

package com.pivotal.gemfirexd.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTime implements org.apache.thrift.TBase<DateTime, DateTime._Fields>, java.io.Serializable, Comparable<DateTime> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DateTime");

  private static final org.apache.thrift.protocol.TField SECS_SINCE_EPOCH_FIELD_DESC = new org.apache.thrift.protocol.TField("secsSinceEpoch", org.apache.thrift.protocol.TType.I64, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new DateTimeStandardSchemeFactory());
    schemes.put(TupleScheme.class, new DateTimeTupleSchemeFactory());
  }

  public long secsSinceEpoch; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SECS_SINCE_EPOCH((short)1, "secsSinceEpoch");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SECS_SINCE_EPOCH
          return SECS_SINCE_EPOCH;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __SECSSINCEEPOCH_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SECS_SINCE_EPOCH, new org.apache.thrift.meta_data.FieldMetaData("secsSinceEpoch", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DateTime.class, metaDataMap);
  }

  public DateTime() {
  }

  public DateTime(
    long secsSinceEpoch)
  {
    this();
    this.secsSinceEpoch = secsSinceEpoch;
    setSecsSinceEpochIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DateTime(DateTime other) {
    __isset_bitfield = other.__isset_bitfield;
    this.secsSinceEpoch = other.secsSinceEpoch;
  }

  public DateTime deepCopy() {
    return new DateTime(this);
  }

  @Override
  public void clear() {
    setSecsSinceEpochIsSet(false);
    this.secsSinceEpoch = 0;
  }

  public long getSecsSinceEpoch() {
    return this.secsSinceEpoch;
  }

  public DateTime setSecsSinceEpoch(long secsSinceEpoch) {
    this.secsSinceEpoch = secsSinceEpoch;
    setSecsSinceEpochIsSet(true);
    return this;
  }

  public void unsetSecsSinceEpoch() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SECSSINCEEPOCH_ISSET_ID);
  }

  /** Returns true if field secsSinceEpoch is set (has been assigned a value) and false otherwise */
  public boolean isSetSecsSinceEpoch() {
    return EncodingUtils.testBit(__isset_bitfield, __SECSSINCEEPOCH_ISSET_ID);
  }

  public void setSecsSinceEpochIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SECSSINCEEPOCH_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case SECS_SINCE_EPOCH:
      if (value == null) {
        unsetSecsSinceEpoch();
      } else {
        setSecsSinceEpoch((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case SECS_SINCE_EPOCH:
      return Long.valueOf(getSecsSinceEpoch());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case SECS_SINCE_EPOCH:
      return isSetSecsSinceEpoch();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof DateTime)
      return this.equals((DateTime)that);
    return false;
  }

  public boolean equals(DateTime that) {
    if (that == null)
      return false;

    boolean this_present_secsSinceEpoch = true;
    boolean that_present_secsSinceEpoch = true;
    if (this_present_secsSinceEpoch || that_present_secsSinceEpoch) {
      if (!(this_present_secsSinceEpoch && that_present_secsSinceEpoch))
        return false;
      if (this.secsSinceEpoch != that.secsSinceEpoch)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(DateTime other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetSecsSinceEpoch()).compareTo(other.isSetSecsSinceEpoch());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSecsSinceEpoch()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.secsSinceEpoch, other.secsSinceEpoch);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DateTime(");
    boolean first = true;

    sb.append("secsSinceEpoch:");
    sb.append(this.secsSinceEpoch);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'secsSinceEpoch' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class DateTimeStandardSchemeFactory implements SchemeFactory {
    public DateTimeStandardScheme getScheme() {
      return new DateTimeStandardScheme();
    }
  }

  private static class DateTimeStandardScheme extends StandardScheme<DateTime> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DateTime struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SECS_SINCE_EPOCH
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.secsSinceEpoch = iprot.readI64();
              struct.setSecsSinceEpochIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetSecsSinceEpoch()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'secsSinceEpoch' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, DateTime struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(SECS_SINCE_EPOCH_FIELD_DESC);
      oprot.writeI64(struct.secsSinceEpoch);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DateTimeTupleSchemeFactory implements SchemeFactory {
    public DateTimeTupleScheme getScheme() {
      return new DateTimeTupleScheme();
    }
  }

  private static class DateTimeTupleScheme extends TupleScheme<DateTime> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DateTime struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI64(struct.secsSinceEpoch);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DateTime struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.secsSinceEpoch = iprot.readI64();
      struct.setSecsSinceEpochIsSet(true);
    }
  }

}

