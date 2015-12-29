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

#include "gfxd_struct_PDXObject.h"

#include <algorithm>

namespace com { namespace pivotal { namespace gemfirexd { namespace thrift {

const char* PDXObject::ascii_fingerprint = "F75ACE223AA50F825551005491EE0D5D";
const uint8_t PDXObject::binary_fingerprint[16] = {0xF7,0x5A,0xCE,0x22,0x3A,0xA5,0x0F,0x82,0x55,0x51,0x00,0x54,0x91,0xEE,0x0D,0x5D};

uint32_t PDXObject::read(::apache::thrift::protocol::TProtocol* iprot) {

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
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->fields.clear();
            uint32_t _size61;
            ::apache::thrift::protocol::TType _etype64;
            xfer += iprot->readListBegin(_etype64, _size61);
            this->fields.resize(_size61);
            uint32_t _i65;
            for (_i65 = 0; _i65 < _size61; ++_i65)
            {
              xfer += this->fields[_i65].read(iprot);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.fields = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->nodes.clear();
            uint32_t _size66;
            ::apache::thrift::protocol::TType _etype69;
            xfer += iprot->readListBegin(_etype69, _size66);
            this->nodes.resize(_size66);
            uint32_t _i70;
            for (_i70 = 0; _i70 < _size66; ++_i70)
            {
              xfer += this->nodes[_i70].read(iprot);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.nodes = true;
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

uint32_t PDXObject::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("PDXObject");

  if (this->__isset.fields) {
    xfer += oprot->writeFieldBegin("fields", ::apache::thrift::protocol::T_LIST, 1);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_STRUCT, static_cast<uint32_t>(this->fields.size()));
      std::vector<FieldValue> ::const_iterator _iter71;
      for (_iter71 = this->fields.begin(); _iter71 != this->fields.end(); ++_iter71)
      {
        xfer += (*_iter71).write(oprot);
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.nodes) {
    xfer += oprot->writeFieldBegin("nodes", ::apache::thrift::protocol::T_LIST, 2);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_STRUCT, static_cast<uint32_t>(this->nodes.size()));
      std::vector<PDXNode> ::const_iterator _iter72;
      for (_iter72 = this->nodes.begin(); _iter72 != this->nodes.end(); ++_iter72)
      {
        xfer += (*_iter72).write(oprot);
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

void swap(PDXObject &a, PDXObject &b) {
  using ::std::swap;
  swap(a.fields, b.fields);
  swap(a.nodes, b.nodes);
  swap(a.__isset, b.__isset);
}

}}}} // namespace
