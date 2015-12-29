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

package com.pivotal.gemfirexd.internal.engine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.gemstone.gemfire.cache.DiskAccessException;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.gemstone.gemfire.internal.LocalLogWriter;
import com.gemstone.gemfire.internal.LogWriterImpl;
import com.gemstone.gemfire.internal.cache.DiskStoreImpl;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.shared.unsafe.ChannelBufferUnsafeDataInputStream;
import com.pivotal.gemfirexd.internal.engine.store.RowFormatter;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.ColumnDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.DataTypeDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.TypeId;

public class OplogIndexReader {

  // for index file records
  public static final byte INDEXID_RECORD = 0x01;

  public static final byte INDEX_RECORD = 0x02;

  public static final byte INDEX_END_OF_FILE = 0x03;

  // randomly generated bytes to mark the valid end of an index file.
  public static final byte[] INDEX_END_OF_FILE_MAGIC = new byte[] {
      INDEX_END_OF_FILE, -0x37, -0x11, -0x26, -0x46, 0x25, 0x71, 0x3b, 0x1f,
      0x4b, -0x77, 0x2b, -0x6f, -0x1f, 0x6b, -0x02 };

  public static void init() {
  }

  public static void main(String[] args) throws StandardException {
    HashMap<String, ArrayList<ColumnDescriptor>> descriptorMap = new HashMap<>();
    // create column descriptors from remaining arguments
    ArrayList<ColumnDescriptor> descriptors;
    for (int i = 1; i < args.length; i++) {
      String[] columnParts = args[i].split(":");
      String tableName = columnParts[0];
      descriptors = descriptorMap.get(tableName);
      if (descriptors == null) {
        descriptors = new ArrayList<>();
        descriptorMap.put(tableName, descriptors);
      }
      String columnName = columnParts[1];
      String columnTypeName = columnParts[2].trim().toLowerCase();
      boolean nullable = columnParts.length <= 3 ||
          Boolean.parseBoolean(columnParts[3]);
      DataTypeDescriptor columnType;
      if (columnTypeName.equals("int")) {
        columnType = DataTypeDescriptor
            .getBuiltInDataTypeDescriptor(Types.INTEGER, nullable);
      } else if (columnTypeName.equals("bigint")
          || columnTypeName.equals("long")) {
        columnType = DataTypeDescriptor
            .getBuiltInDataTypeDescriptor(Types.BIGINT, nullable);
      } else if (columnTypeName.startsWith("varchar")) {
        String[] parts = columnTypeName.split("\\(|\\)");
        int size = Integer.parseInt(parts[1].trim());
        columnType = DataTypeDescriptor
            .getBuiltInDataTypeDescriptor(Types.VARCHAR, nullable, size);
      } else if (columnTypeName.startsWith("char")) {
        String[] parts = columnTypeName.split("\\(|\\)");
        int size = Integer.parseInt(parts[1].trim());
        columnType = DataTypeDescriptor
            .getBuiltInDataTypeDescriptor(Types.CHAR, nullable, size);
      } else if (columnTypeName.equals("double")) {
        columnType = DataTypeDescriptor
            .getBuiltInDataTypeDescriptor(Types.DOUBLE, nullable);
      } else if (columnTypeName.startsWith("decimal")
          || columnTypeName.startsWith("numeric")) {
        String[] parts = columnTypeName.split("\\(|\\)|,");
        int precision = Integer.parseInt(parts[1].trim());
        int scale = Integer.parseInt(parts[2].trim());
        // below is taken from NumericTypeCompiler.resolveArithmeticOperation
        int maximumWidth = (scale > 0) ? precision + 3 : precision + 1;
        columnType = new DataTypeDescriptor(
            TypeId.getBuiltInTypeId(columnTypeName.charAt(0) == 'd'
                ? Types.DECIMAL : Types.NUMERIC),
            precision, scale, nullable, maximumWidth);
      } else {
        throw new IllegalArgumentException(
            "unsupported type = " + columnParts[2]);
      }
      descriptors.add(new ColumnDescriptor(columnName, i, columnType, null,
          null, null, null, 0, 0, false));
    }

    System.out.println("DescriptorMap: " + descriptorMap);
    readIndex(args[0], descriptorMap);
  }

  public static void readIndex(String fileName,
      Map<String, ArrayList<ColumnDescriptor>> descriptorMap)
          throws StandardException {
    try {
      RowFormatter currentFormatter = null;
      final LogWriterI18n logger = new LocalLogWriter(
          LogWriterImpl.FINE_LEVEL);
      final boolean logEnabled = DiskStoreImpl.INDEX_LOAD_DEBUG
          || logger.fineEnabled();
      final boolean logFinerEnabled = DiskStoreImpl.INDEX_LOAD_DEBUG_FINER
          || logger.finerEnabled();

      if (logEnabled || DiskStoreImpl.INDEX_LOAD_PERF_DEBUG) {
        logger.info(LocalizedStrings.DEBUG,
            "OplogIndex#readIndex: for " + fileName);
      }

      final RandomAccessFile raf = new RandomAccessFile(fileName, "r");
      final FileChannel channel = raf.getChannel();
      final ChannelBufferUnsafeDataInputStream in =
          new ChannelBufferUnsafeDataInputStream(channel,
              128 * 1024);
      boolean endOfFile = false;

      String currentIndexID;

      while (!endOfFile) {
        final int opCode = in.read();
        switch (opCode) {
          case INDEX_END_OF_FILE:
            if (logEnabled) {
              logger.info(LocalizedStrings.DEBUG, "OplogIndex#readIndex: "
                  + "read end 0f file record for " + fileName);
            }
            byte[] data = new byte[INDEX_END_OF_FILE_MAGIC.length];
            data[0] = INDEX_END_OF_FILE;
            in.readFully(data, 1, INDEX_END_OF_FILE_MAGIC.length - 1);
            if (!Arrays.equals(data, INDEX_END_OF_FILE_MAGIC)) {
              throw new DiskAccessException(
                  "Did not find end of file magic at the end of index "
                      + fileName, "LOCAL_READ");
            }
            break;

          case INDEXID_RECORD:
            if (logEnabled) {
              logger.info(LocalizedStrings.DEBUG,
                  "Reading an indexId record");
            }
            currentIndexID = InternalDataSerializer.readString(in);
            String indexName = currentIndexID.split(":")[0];
            ArrayList<ColumnDescriptor> descriptors = descriptorMap
                .get(indexName);
            if (descriptors != null) {
              currentFormatter = new RowFormatter(
                  descriptors
                      .toArray(new ColumnDescriptor[descriptors.size()]),
                  descriptors.size(), 1, null, true);
            } else {
              currentFormatter = null;
            }
            if (logEnabled) {
              logger.info(LocalizedStrings.DEBUG, "OplogIndex#"
                  + "readIndex: read indexUUID=" + currentIndexID);
            }
            break;

          case INDEX_RECORD:
            if (logEnabled) {
              logger.info(LocalizedStrings.DEBUG, "Reading an index record");
            }
            byte[] indexKeyBytes = InternalDataSerializer.readByteArray(in);
            int numRegionKeys = (int)InternalDataSerializer
                .readUnsignedVL(in);
            if (logFinerEnabled) {
              logger.info(LocalizedStrings.DEBUG,
                  "Read index bytes " + Arrays.toString(indexKeyBytes)
                      + ", numRegionKeys=" + numRegionKeys);
            } else if (logEnabled && currentFormatter != null) {
              for (int i = 1; i <= currentFormatter.getNumColumns(); i++) {
                logger.info(LocalizedStrings.DEBUG, "    column" + i + '='
                    + currentFormatter.getColumn(i, indexKeyBytes));
              }
            }
            long regionEntryKeyId = 0;
            for (int i = 0; i < numRegionKeys; i++) {
              if (i == 0) {
                regionEntryKeyId = InternalDataSerializer.readUnsignedVL(in);
                if (logFinerEnabled) {
                  logger.info(LocalizedStrings.DEBUG,
                      "    read regionEntryId=" + regionEntryKeyId);
                }
              } else {
                long deltaKeyId = InternalDataSerializer.readUnsignedVL(in);
                regionEntryKeyId = regionEntryKeyId + deltaKeyId;
                if (logFinerEnabled) {
                  logger.info(LocalizedStrings.DEBUG, "    read deltaKeyId="
                      + deltaKeyId + " regionEntryKeyId=" + regionEntryKeyId);
                }
              }
            }
            if (logEnabled) {
              logger.info(LocalizedStrings.DEBUG, "Read an index key with "
                  + numRegionKeys + " region entries");
            }
            break;

          default:
            if (opCode < 0) {
              endOfFile = true;
              break;
            } else {
              throw new IOException("unexpected opCode=" + opCode
                  + " encountered while reading file: " + fileName);
            }
        }
      }
      in.close();
      raf.close();
      if (logEnabled || DiskStoreImpl.INDEX_LOAD_PERF_DEBUG) {
        logger.info(LocalizedStrings.DEBUG,
            "OplogIndex#readIndex: " + "Processed file: " + fileName);
      }
    } catch (IOException ioe) {
      throw new DiskAccessException(ioe);
    }
  }
}
