/**
 * Autogenerated by Thrift Compiler (1.0.0-dev)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

#include <iosfwd>

#include <thrift/Thrift.h>
#include <thrift/TApplicationException.h>
#include <thrift/protocol/TProtocol.h>
#include <thrift/transport/TTransport.h>

#include <thrift/cxxfunctional.h>
#include "snappydata_struct_StatementAttrs.h"

#include <algorithm>
#include <ostream>

#include <thrift/TToString.h>

namespace io { namespace snappydata { namespace thrift {


StatementAttrs::~StatementAttrs() noexcept {
}


void StatementAttrs::__set_resultSetType(const int8_t val) {
  this->resultSetType = val;
__isset.resultSetType = true;
}

void StatementAttrs::__set_updatable(const bool val) {
  this->updatable = val;
__isset.updatable = true;
}

void StatementAttrs::__set_holdCursorsOverCommit(const bool val) {
  this->holdCursorsOverCommit = val;
__isset.holdCursorsOverCommit = true;
}

void StatementAttrs::__set_requireAutoIncCols(const bool val) {
  this->requireAutoIncCols = val;
__isset.requireAutoIncCols = true;
}

void StatementAttrs::__set_autoIncColumns(const std::vector<int32_t> & val) {
  this->autoIncColumns = val;
__isset.autoIncColumns = true;
}

void StatementAttrs::__set_autoIncColumnNames(const std::vector<std::string> & val) {
  this->autoIncColumnNames = val;
__isset.autoIncColumnNames = true;
}

void StatementAttrs::__set_batchSize(const int32_t val) {
  this->batchSize = val;
__isset.batchSize = true;
}

void StatementAttrs::__set_fetchReverse(const bool val) {
  this->fetchReverse = val;
__isset.fetchReverse = true;
}

void StatementAttrs::__set_lobChunkSize(const int32_t val) {
  this->lobChunkSize = val;
__isset.lobChunkSize = true;
}

void StatementAttrs::__set_maxRows(const int32_t val) {
  this->maxRows = val;
__isset.maxRows = true;
}

void StatementAttrs::__set_maxFieldSize(const int32_t val) {
  this->maxFieldSize = val;
__isset.maxFieldSize = true;
}

void StatementAttrs::__set_timeout(const int32_t val) {
  this->timeout = val;
__isset.timeout = true;
}

void StatementAttrs::__set_cursorName(const std::string& val) {
  this->cursorName = val;
__isset.cursorName = true;
}

void StatementAttrs::__set_possibleDuplicate(const bool val) {
  this->possibleDuplicate = val;
__isset.possibleDuplicate = true;
}

void StatementAttrs::__set_poolable(const bool val) {
  this->poolable = val;
__isset.poolable = true;
}

void StatementAttrs::__set_doEscapeProcessing(const bool val) {
  this->doEscapeProcessing = val;
__isset.doEscapeProcessing = true;
}

void StatementAttrs::__set_pendingTransactionAttrs(const std::map<TransactionAttribute::type, bool> & val) {
  this->pendingTransactionAttrs = val;
__isset.pendingTransactionAttrs = true;
}

uint32_t StatementAttrs::read(::apache::thrift::protocol::TProtocol* iprot) {

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
        if (ftype == ::apache::thrift::protocol::T_BYTE) {
          xfer += iprot->readByte(this->resultSetType);
          this->__isset.resultSetType = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->updatable);
          this->__isset.updatable = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->holdCursorsOverCommit);
          this->__isset.holdCursorsOverCommit = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->requireAutoIncCols);
          this->__isset.requireAutoIncCols = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 5:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->autoIncColumns.clear();
            uint32_t _size183;
            ::apache::thrift::protocol::TType _etype186;
            xfer += iprot->readListBegin(_etype186, _size183);
            this->autoIncColumns.resize(_size183);
            uint32_t _i187;
            for (_i187 = 0; _i187 < _size183; ++_i187)
            {
              xfer += iprot->readI32(this->autoIncColumns[_i187]);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.autoIncColumns = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 6:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->autoIncColumnNames.clear();
            uint32_t _size188;
            ::apache::thrift::protocol::TType _etype191;
            xfer += iprot->readListBegin(_etype191, _size188);
            this->autoIncColumnNames.resize(_size188);
            uint32_t _i192;
            for (_i192 = 0; _i192 < _size188; ++_i192)
            {
              xfer += iprot->readString(this->autoIncColumnNames[_i192]);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.autoIncColumnNames = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 7:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->batchSize);
          this->__isset.batchSize = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 8:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->fetchReverse);
          this->__isset.fetchReverse = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 9:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->lobChunkSize);
          this->__isset.lobChunkSize = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 10:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->maxRows);
          this->__isset.maxRows = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 11:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->maxFieldSize);
          this->__isset.maxFieldSize = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 12:
        if (ftype == ::apache::thrift::protocol::T_I32) {
          xfer += iprot->readI32(this->timeout);
          this->__isset.timeout = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 13:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->cursorName);
          this->__isset.cursorName = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 14:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->possibleDuplicate);
          this->__isset.possibleDuplicate = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 15:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->poolable);
          this->__isset.poolable = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 16:
        if (ftype == ::apache::thrift::protocol::T_BOOL) {
          xfer += iprot->readBool(this->doEscapeProcessing);
          this->__isset.doEscapeProcessing = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 17:
        if (ftype == ::apache::thrift::protocol::T_MAP) {
          {
            this->pendingTransactionAttrs.clear();
            uint32_t _size193;
            ::apache::thrift::protocol::TType _ktype194;
            ::apache::thrift::protocol::TType _vtype195;
            xfer += iprot->readMapBegin(_ktype194, _vtype195, _size193);
            uint32_t _i197;
            for (_i197 = 0; _i197 < _size193; ++_i197)
            {
              TransactionAttribute::type _key198;
              int32_t ecast200;
              xfer += iprot->readI32(ecast200);
              _key198 = (TransactionAttribute::type)ecast200;
              bool& _val199 = this->pendingTransactionAttrs[_key198];
              xfer += iprot->readBool(_val199);
            }
            xfer += iprot->readMapEnd();
          }
          this->__isset.pendingTransactionAttrs = true;
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

uint32_t StatementAttrs::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("StatementAttrs");

  if (this->__isset.resultSetType) {
    xfer += oprot->writeFieldBegin("resultSetType", ::apache::thrift::protocol::T_BYTE, 1);
    xfer += oprot->writeByte(this->resultSetType);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.updatable) {
    xfer += oprot->writeFieldBegin("updatable", ::apache::thrift::protocol::T_BOOL, 2);
    xfer += oprot->writeBool(this->updatable);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.holdCursorsOverCommit) {
    xfer += oprot->writeFieldBegin("holdCursorsOverCommit", ::apache::thrift::protocol::T_BOOL, 3);
    xfer += oprot->writeBool(this->holdCursorsOverCommit);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.requireAutoIncCols) {
    xfer += oprot->writeFieldBegin("requireAutoIncCols", ::apache::thrift::protocol::T_BOOL, 4);
    xfer += oprot->writeBool(this->requireAutoIncCols);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.autoIncColumns) {
    xfer += oprot->writeFieldBegin("autoIncColumns", ::apache::thrift::protocol::T_LIST, 5);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_I32, static_cast<uint32_t>(this->autoIncColumns.size()));
      std::vector<int32_t> ::const_iterator _iter201;
      for (_iter201 = this->autoIncColumns.begin(); _iter201 != this->autoIncColumns.end(); ++_iter201)
      {
        xfer += oprot->writeI32((*_iter201));
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.autoIncColumnNames) {
    xfer += oprot->writeFieldBegin("autoIncColumnNames", ::apache::thrift::protocol::T_LIST, 6);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_STRING, static_cast<uint32_t>(this->autoIncColumnNames.size()));
      std::vector<std::string> ::const_iterator _iter202;
      for (_iter202 = this->autoIncColumnNames.begin(); _iter202 != this->autoIncColumnNames.end(); ++_iter202)
      {
        xfer += oprot->writeString((*_iter202));
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.batchSize) {
    xfer += oprot->writeFieldBegin("batchSize", ::apache::thrift::protocol::T_I32, 7);
    xfer += oprot->writeI32(this->batchSize);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.fetchReverse) {
    xfer += oprot->writeFieldBegin("fetchReverse", ::apache::thrift::protocol::T_BOOL, 8);
    xfer += oprot->writeBool(this->fetchReverse);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.lobChunkSize) {
    xfer += oprot->writeFieldBegin("lobChunkSize", ::apache::thrift::protocol::T_I32, 9);
    xfer += oprot->writeI32(this->lobChunkSize);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.maxRows) {
    xfer += oprot->writeFieldBegin("maxRows", ::apache::thrift::protocol::T_I32, 10);
    xfer += oprot->writeI32(this->maxRows);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.maxFieldSize) {
    xfer += oprot->writeFieldBegin("maxFieldSize", ::apache::thrift::protocol::T_I32, 11);
    xfer += oprot->writeI32(this->maxFieldSize);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.timeout) {
    xfer += oprot->writeFieldBegin("timeout", ::apache::thrift::protocol::T_I32, 12);
    xfer += oprot->writeI32(this->timeout);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.cursorName) {
    xfer += oprot->writeFieldBegin("cursorName", ::apache::thrift::protocol::T_STRING, 13);
    xfer += oprot->writeString(this->cursorName);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.possibleDuplicate) {
    xfer += oprot->writeFieldBegin("possibleDuplicate", ::apache::thrift::protocol::T_BOOL, 14);
    xfer += oprot->writeBool(this->possibleDuplicate);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.poolable) {
    xfer += oprot->writeFieldBegin("poolable", ::apache::thrift::protocol::T_BOOL, 15);
    xfer += oprot->writeBool(this->poolable);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.doEscapeProcessing) {
    xfer += oprot->writeFieldBegin("doEscapeProcessing", ::apache::thrift::protocol::T_BOOL, 16);
    xfer += oprot->writeBool(this->doEscapeProcessing);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.pendingTransactionAttrs) {
    xfer += oprot->writeFieldBegin("pendingTransactionAttrs", ::apache::thrift::protocol::T_MAP, 17);
    {
      xfer += oprot->writeMapBegin(::apache::thrift::protocol::T_I32, ::apache::thrift::protocol::T_BOOL, static_cast<uint32_t>(this->pendingTransactionAttrs.size()));
      std::map<TransactionAttribute::type, bool> ::const_iterator _iter203;
      for (_iter203 = this->pendingTransactionAttrs.begin(); _iter203 != this->pendingTransactionAttrs.end(); ++_iter203)
      {
        xfer += oprot->writeI32((int32_t)_iter203->first);
        xfer += oprot->writeBool(_iter203->second);
      }
      xfer += oprot->writeMapEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

void swap(StatementAttrs &a, StatementAttrs &b) noexcept {
  using ::std::swap;
  static_assert(noexcept(swap(a, b)), "throwing swap");
  swap(a.resultSetType, b.resultSetType);
  swap(a.updatable, b.updatable);
  swap(a.holdCursorsOverCommit, b.holdCursorsOverCommit);
  swap(a.requireAutoIncCols, b.requireAutoIncCols);
  swap(a.autoIncColumns, b.autoIncColumns);
  swap(a.autoIncColumnNames, b.autoIncColumnNames);
  swap(a.batchSize, b.batchSize);
  swap(a.fetchReverse, b.fetchReverse);
  swap(a.lobChunkSize, b.lobChunkSize);
  swap(a.maxRows, b.maxRows);
  swap(a.maxFieldSize, b.maxFieldSize);
  swap(a.timeout, b.timeout);
  swap(a.cursorName, b.cursorName);
  swap(a.possibleDuplicate, b.possibleDuplicate);
  swap(a.poolable, b.poolable);
  swap(a.doEscapeProcessing, b.doEscapeProcessing);
  swap(a.pendingTransactionAttrs, b.pendingTransactionAttrs);
  swap(a.__isset, b.__isset);
}

StatementAttrs::StatementAttrs(const StatementAttrs& other204) {
  resultSetType = other204.resultSetType;
  updatable = other204.updatable;
  holdCursorsOverCommit = other204.holdCursorsOverCommit;
  requireAutoIncCols = other204.requireAutoIncCols;
  autoIncColumns = other204.autoIncColumns;
  autoIncColumnNames = other204.autoIncColumnNames;
  batchSize = other204.batchSize;
  fetchReverse = other204.fetchReverse;
  lobChunkSize = other204.lobChunkSize;
  maxRows = other204.maxRows;
  maxFieldSize = other204.maxFieldSize;
  timeout = other204.timeout;
  cursorName = other204.cursorName;
  possibleDuplicate = other204.possibleDuplicate;
  poolable = other204.poolable;
  doEscapeProcessing = other204.doEscapeProcessing;
  pendingTransactionAttrs = other204.pendingTransactionAttrs;
  __isset = other204.__isset;
}
StatementAttrs::StatementAttrs( StatementAttrs&& other205) noexcept {
  resultSetType = std::move(other205.resultSetType);
  updatable = std::move(other205.updatable);
  holdCursorsOverCommit = std::move(other205.holdCursorsOverCommit);
  requireAutoIncCols = std::move(other205.requireAutoIncCols);
  autoIncColumns = std::move(other205.autoIncColumns);
  autoIncColumnNames = std::move(other205.autoIncColumnNames);
  batchSize = std::move(other205.batchSize);
  fetchReverse = std::move(other205.fetchReverse);
  lobChunkSize = std::move(other205.lobChunkSize);
  maxRows = std::move(other205.maxRows);
  maxFieldSize = std::move(other205.maxFieldSize);
  timeout = std::move(other205.timeout);
  cursorName = std::move(other205.cursorName);
  possibleDuplicate = std::move(other205.possibleDuplicate);
  poolable = std::move(other205.poolable);
  doEscapeProcessing = std::move(other205.doEscapeProcessing);
  pendingTransactionAttrs = std::move(other205.pendingTransactionAttrs);
  __isset = std::move(other205.__isset);
}
StatementAttrs& StatementAttrs::operator=(const StatementAttrs& other206) {
  resultSetType = other206.resultSetType;
  updatable = other206.updatable;
  holdCursorsOverCommit = other206.holdCursorsOverCommit;
  requireAutoIncCols = other206.requireAutoIncCols;
  autoIncColumns = other206.autoIncColumns;
  autoIncColumnNames = other206.autoIncColumnNames;
  batchSize = other206.batchSize;
  fetchReverse = other206.fetchReverse;
  lobChunkSize = other206.lobChunkSize;
  maxRows = other206.maxRows;
  maxFieldSize = other206.maxFieldSize;
  timeout = other206.timeout;
  cursorName = other206.cursorName;
  possibleDuplicate = other206.possibleDuplicate;
  poolable = other206.poolable;
  doEscapeProcessing = other206.doEscapeProcessing;
  pendingTransactionAttrs = other206.pendingTransactionAttrs;
  __isset = other206.__isset;
  return *this;
}
StatementAttrs& StatementAttrs::operator=(StatementAttrs&& other207) noexcept {
  resultSetType = std::move(other207.resultSetType);
  updatable = std::move(other207.updatable);
  holdCursorsOverCommit = std::move(other207.holdCursorsOverCommit);
  requireAutoIncCols = std::move(other207.requireAutoIncCols);
  autoIncColumns = std::move(other207.autoIncColumns);
  autoIncColumnNames = std::move(other207.autoIncColumnNames);
  batchSize = std::move(other207.batchSize);
  fetchReverse = std::move(other207.fetchReverse);
  lobChunkSize = std::move(other207.lobChunkSize);
  maxRows = std::move(other207.maxRows);
  maxFieldSize = std::move(other207.maxFieldSize);
  timeout = std::move(other207.timeout);
  cursorName = std::move(other207.cursorName);
  possibleDuplicate = std::move(other207.possibleDuplicate);
  poolable = std::move(other207.poolable);
  doEscapeProcessing = std::move(other207.doEscapeProcessing);
  pendingTransactionAttrs = std::move(other207.pendingTransactionAttrs);
  __isset = std::move(other207.__isset);
  return *this;
}
void StatementAttrs::printTo(std::ostream& out) const {
  using ::apache::thrift::to_string;
  out << "StatementAttrs(";
  out << "resultSetType="; (__isset.resultSetType ? (out << to_string(resultSetType)) : (out << "<null>"));
  out << ", " << "updatable="; (__isset.updatable ? (out << to_string(updatable)) : (out << "<null>"));
  out << ", " << "holdCursorsOverCommit="; (__isset.holdCursorsOverCommit ? (out << to_string(holdCursorsOverCommit)) : (out << "<null>"));
  out << ", " << "requireAutoIncCols="; (__isset.requireAutoIncCols ? (out << to_string(requireAutoIncCols)) : (out << "<null>"));
  out << ", " << "autoIncColumns="; (__isset.autoIncColumns ? (out << to_string(autoIncColumns)) : (out << "<null>"));
  out << ", " << "autoIncColumnNames="; (__isset.autoIncColumnNames ? (out << to_string(autoIncColumnNames)) : (out << "<null>"));
  out << ", " << "batchSize="; (__isset.batchSize ? (out << to_string(batchSize)) : (out << "<null>"));
  out << ", " << "fetchReverse="; (__isset.fetchReverse ? (out << to_string(fetchReverse)) : (out << "<null>"));
  out << ", " << "lobChunkSize="; (__isset.lobChunkSize ? (out << to_string(lobChunkSize)) : (out << "<null>"));
  out << ", " << "maxRows="; (__isset.maxRows ? (out << to_string(maxRows)) : (out << "<null>"));
  out << ", " << "maxFieldSize="; (__isset.maxFieldSize ? (out << to_string(maxFieldSize)) : (out << "<null>"));
  out << ", " << "timeout="; (__isset.timeout ? (out << to_string(timeout)) : (out << "<null>"));
  out << ", " << "cursorName="; (__isset.cursorName ? (out << to_string(cursorName)) : (out << "<null>"));
  out << ", " << "possibleDuplicate="; (__isset.possibleDuplicate ? (out << to_string(possibleDuplicate)) : (out << "<null>"));
  out << ", " << "poolable="; (__isset.poolable ? (out << to_string(poolable)) : (out << "<null>"));
  out << ", " << "doEscapeProcessing="; (__isset.doEscapeProcessing ? (out << to_string(doEscapeProcessing)) : (out << "<null>"));
  out << ", " << "pendingTransactionAttrs="; (__isset.pendingTransactionAttrs ? (out << to_string(pendingTransactionAttrs)) : (out << "<null>"));
  out << ")";
}

}}} // namespace