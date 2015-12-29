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

#include "gfxd_struct_StatementResult.h"

#include <algorithm>

namespace com { namespace pivotal { namespace gemfirexd { namespace thrift {

const char* StatementResult::ascii_fingerprint = "EED01C30C42FD1F6915891F00BDB97A5";
const uint8_t StatementResult::binary_fingerprint[16] = {0xEE,0xD0,0x1C,0x30,0xC4,0x2F,0xD1,0xF6,0x91,0x58,0x91,0xF0,0x0B,0xDB,0x97,0xA5};

uint32_t StatementResult::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;


  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRUCT) {
          xfer += this->resultSet.read(iprot);
          this->__isset.resultSet = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->updateCount);
          this->__isset.updateCount = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->batchUpdateCounts.clear();
            uint32_t _size273;
            ::apache::thrift::protocol::TType _etype276;
            xfer += iprot->readListBegin(_etype276, _size273);
            this->batchUpdateCounts.resize(_size273);
            uint32_t _i277;
            for (_i277 = 0; _i277 < _size273; ++_i277)
            {
              xfer += iprot->readI32(this->batchUpdateCounts[_i277]);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.batchUpdateCounts = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_STRUCT) {
          xfer += this->procedureOutParams.read(iprot);
          this->__isset.procedureOutParams = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 5:
        if (ftype == ::apache::thrift::protocol::T_STRUCT) {
          xfer += this->generatedKeys.read(iprot);
          this->__isset.generatedKeys = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 6:
        if (ftype == ::apache::thrift::protocol::T_STRUCT) {
          xfer += this->warnings.read(iprot);
          this->__isset.warnings = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 7:
        if (ftype == ::apache::thrift::protocol::T_STRUCT) {
          xfer += this->preparedResult.read(iprot);
          this->__isset.preparedResult = true;
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

  return xfer;
}

uint32_t StatementResult::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("StatementResult");

  if (this->__isset.resultSet) {
    xfer += oprot->writeFieldBegin("resultSet", ::apache::thrift::protocol::T_STRUCT, 1);
    xfer += this->resultSet.write(oprot);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.updateCount) {
    xfer += oprot->writeFieldBegin("updateCount", ::apache::thrift::protocol::T_I32, 2);
    xfer += oprot->writeI32(this->updateCount);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.batchUpdateCounts) {
    xfer += oprot->writeFieldBegin("batchUpdateCounts", ::apache::thrift::protocol::T_LIST, 3);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_I32, static_cast<uint32_t>(this->batchUpdateCounts.size()));
      std::vector<int32_t> ::const_iterator _iter278;
      for (_iter278 = this->batchUpdateCounts.begin(); _iter278 != this->batchUpdateCounts.end(); ++_iter278)
      {
        xfer += oprot->writeI32((*_iter278));
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.procedureOutParams) {
    xfer += oprot->writeFieldBegin("procedureOutParams", ::apache::thrift::protocol::T_STRUCT, 4);
    xfer += this->procedureOutParams.write(oprot);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.generatedKeys) {
    xfer += oprot->writeFieldBegin("generatedKeys", ::apache::thrift::protocol::T_STRUCT, 5);
    xfer += this->generatedKeys.write(oprot);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.warnings) {
    xfer += oprot->writeFieldBegin("warnings", ::apache::thrift::protocol::T_STRUCT, 6);
    xfer += this->warnings.write(oprot);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.preparedResult) {
    xfer += oprot->writeFieldBegin("preparedResult", ::apache::thrift::protocol::T_STRUCT, 7);
    xfer += this->preparedResult.write(oprot);
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

void swap(StatementResult &a, StatementResult &b) {
  using ::std::swap;
  swap(a.resultSet, b.resultSet);
  swap(a.updateCount, b.updateCount);
  swap(a.batchUpdateCounts, b.batchUpdateCounts);
  swap(a.procedureOutParams, b.procedureOutParams);
  swap(a.generatedKeys, b.generatedKeys);
  swap(a.warnings, b.warnings);
  swap(a.preparedResult, b.preparedResult);
  swap(a.__isset, b.__isset);
}

}}}} // namespace
