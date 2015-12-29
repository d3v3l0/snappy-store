/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

#include <thrift/Thrift.h>
#include <thrift/TApplicationException.h>
#include <thrift/protocol/TProtocol.h>
#include <thrift/transport/TTransport.h>

#include <thrift/cxxfunctional.h>

#include "gfxd_struct_ClobChunk.h"

#include <algorithm>

namespace com { namespace pivotal { namespace gemfirexd { namespace thrift {

const char* ClobChunk::ascii_fingerprint = "96777C614AC7898ECF1FFE99933F4372";
const uint8_t ClobChunk::binary_fingerprint[16] = {0x96,0x77,0x7C,0x61,0x4A,0xC7,0x89,0x8E,0xCF,0x1F,0xFE,0x99,0x93,0x3F,0x43,0x72};

uint32_t ClobChunk::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;

  bool isset_chunk = false;
  bool isset_last = false;

  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->chunk);
          isset_chunk = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->last);
          isset_last = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->lobId);
          this->__isset.lobId = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_I64) {
          xfer += iprot->readI64(this->offset);
          this->__isset.offset = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 5:
        if (ftype == ::apache::thrift::protocol::T_I64) {
          xfer += iprot->readI64(this->totalLength);
          this->__isset.totalLength = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      default:
        xfer += iprot->skip(ftype);
        break;
    }
    xfer += iprot->readFieldEnd();
  }

  xfer += iprot->readStructEnd();

  if (!isset_chunk)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_last)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  return xfer;
}

uint32_t ClobChunk::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("ClobChunk");

  xfer += oprot->writeFieldBegin("chunk", ::apache::thrift::protocol::T_STRING, 1);
  xfer += oprot->writeString(this->chunk);
  xfer += oprot->writeFieldEnd();

  xfer += oprot->writeFieldBegin("last", ::apache::thrift::protocol::T_BOOL, 2);
  xfer += oprot->writeBool(this->last);
  xfer += oprot->writeFieldEnd();

  if (this->__isset.lobId) {
    xfer += oprot->writeFieldBegin("lobId", ::apache::thrift::protocol::T_I32, 3);
    xfer += oprot->writeI32(this->lobId);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.offset) {
    xfer += oprot->writeFieldBegin("offset", ::apache::thrift::protocol::T_I64, 4);
    xfer += oprot->writeI64(this->offset);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.totalLength) {
    xfer += oprot->writeFieldBegin("totalLength", ::apache::thrift::protocol::T_I64, 5);
    xfer += oprot->writeI64(this->totalLength);
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

void swap(ClobChunk &a, ClobChunk &b) {
  using ::std::swap;
  swap(a.chunk, b.chunk);
  swap(a.last, b.last);
  swap(a.lobId, b.lobId);
  swap(a.offset, b.offset);
  swap(a.totalLength, b.totalLength);
  swap(a.__isset, b.__isset);
}

}}}} // namespace
