/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.store.access.conglomerate.GenericConglomerate

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

package com.pivotal.gemfirexd.internal.impl.store.access.conglomerate;

import com.gemstone.gemfire.internal.offheap.ByteSource;
import com.gemstone.gemfire.pdx.internal.unsafe.UnsafeWrapper;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.SQLState;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.store.access.conglomerate.Conglomerate;
import com.pivotal.gemfirexd.internal.iapi.types.DataType;
import com.pivotal.gemfirexd.internal.iapi.types.DataTypeDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**

A class that implements the methods shared across all implementations of
the Conglomerate interface.

**/

public abstract class GenericConglomerate 
    extends DataType implements Conglomerate
{

    /**************************************************************************
     * Public Methods implementing DataValueDescriptor interface.
     **************************************************************************
     */

	/**
	 * Gets the length of the data value.  The meaning of this is
	 * implementation-dependent.  For string types, it is the number of
	 * characters in the string.  For numeric types, it is the number of
	 * bytes used to store the number.  This is the actual length
	 * of this value, not the length of the type it was defined as.
	 * For example, a VARCHAR value may be shorter than the declared
	 * VARCHAR (maximum) length.
	 *
	 * @return	The length of the data value
	 *
	 * @exception StandardException   On error
     * 
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#getLength
	 */
	public int	getLength() 
        throws StandardException
    {
        throw(StandardException.newException(
                SQLState.HEAP_UNIMPLEMENTED_FEATURE));
    }
	/**
	 * Gets the value in the data value descriptor as a String.
	 * Throws an exception if the data value is not a string.
	 *
	 * @return	The data value as a String.
	 *
	 * @exception StandardException   Thrown on error
     *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#getString
	 */
	public String	getString() throws StandardException
    {
        throw(StandardException.newException(
                SQLState.HEAP_UNIMPLEMENTED_FEATURE));
    }

	/**
	 * Gets the value in the data value descriptor as a Java Object.
	 * The type of the Object will be the Java object type corresponding
	 * to the data value's SQL type. JDBC defines a mapping between Java
	 * object types and SQL types - we will allow that to be extended
	 * through user type definitions. Throws an exception if the data
	 * value is not an object (yeah, right).
	 *
	 * @return	The data value as an Object.
	 *
	 * @exception StandardException   Thrown on error
     *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#getObject
	 */
	public Object	getObject() throws StandardException
    {
        return(this);
    }

	/**
	 * <U>Shallow copy</U>. 
	 * <p> 
	 * Clone the DataValueDescriptor and copy its contents.
	 * We clone the data value wrapper (e.g. SQLDecimal)
	 * and reuse its contents (the underlying BigDecimal).
	 * The resultant DataValueDescriptor will point to the same
	 * value as the original DataValueDescriptor (unless the value
	 * is a primitive type, e.g. SQLInteger/integer).
	 *
	 * @return A clone of the DataValueDescriptor reusing its contents.
     *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#getClone
	 */
	public DataValueDescriptor getClone()
    {
        if (SanityManager.DEBUG)
            SanityManager.THROWASSERT("Not implemented!.");

        return(null);
    }

	/**
	 * Get a new null value of the same type as this data value.
	 *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#getNewNull
	 */
	public DataValueDescriptor getNewNull()
    {
        if (SanityManager.DEBUG)
            SanityManager.THROWASSERT("Not implemented!.");

        return(null);
    }

	/**
	 * Set the value based on the value for the specified DataValueDescriptor
	 * from the specified ResultSet.
	 *
	 * @param resultSet		The specified ResultSet.
	 * @param colNumber		The 1-based column # into the resultSet.
	 * @param isNullable	Whether or not the column is nullable
	 *						(No need to call wasNull() if not)
	 * 
	 * @exception StandardException		Thrown on error
	 * @exception SQLException		Error accessing the result set
     *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#setValueFromResultSet
	 */
	public void setValueFromResultSet(
    ResultSet   resultSet, 
    int         colNumber,
    boolean     isNullable)
		throws StandardException, SQLException
    {
        throw(StandardException.newException(
                SQLState.HEAP_UNIMPLEMENTED_FEATURE));
    }


	/**
	 * Set the value of this DataValueDescriptor from another.
	 *
	 * @param theValue	The Date value to set this DataValueDescriptor to
	 *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#setValue
	 */
	protected void setFrom(DataValueDescriptor theValue) 
        throws StandardException
    {
        throw(StandardException.newException(
                SQLState.HEAP_UNIMPLEMENTED_FEATURE));
    }

	/**
	 * Get the SQL name of the datatype
	 *
	 * @return	The SQL name of the datatype
     *
     * @see com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor#getTypeName
	 */
	public String	getTypeName()
    {
        if (SanityManager.DEBUG)
            SanityManager.THROWASSERT("Not implemented!.");

        return(null);
    }

	/**
	 * Compare this Orderable with a given Orderable for the purpose of
	 * index positioning.  This method treats nulls as ordered values -
	 * that is, it treats SQL null as equal to null and less than all
	 * other values.
	 *
	 * @param other		The Orderable to compare this one to.
	 *
	 * @return  <0 - this Orderable is less than other.
	 * 			 0 - this Orderable equals other.
	 *			>0 - this Orderable is greater than other.
     *
     *			The code should not explicitly look for -1, or 1.
	 *
	 * @exception StandardException		Thrown on error
     *
     * @see DataValueDescriptor#compare
	 */
	public int compare(DataValueDescriptor other) 
        throws StandardException
	{
        throw(StandardException.newException(
                SQLState.HEAP_UNIMPLEMENTED_FEATURE));
	}

// GemStone changes BEGIN
  // Used with Global Index, returns base table id on which index is created.
  public long getBaseConglomerateId() {
    if (SanityManager.DEBUG)
      SanityManager.THROWASSERT("Not implemented!.");
    return 0;
  }

  public boolean[] getAscDescInfo() {
    if (SanityManager.DEBUG)
      SanityManager.THROWASSERT("Not implemented!.");
    return new boolean[] {};
  }

  @Override
  public final int writeBytes(byte[] outBytes, int offset,
      DataTypeDescriptor dtd) {
    throw new UnsupportedOperationException("unexpected invocation for "
        + toString());
  }

  @Override
  public int readBytes(byte[] inBytes, int offset, int columnWidth) {
    throw new UnsupportedOperationException("unexpected invocation for "
        + getClass());
  }

  @Override
  public int readBytes(UnsafeWrapper unsafe, long memOffset, int columnWidth,
      ByteSource bs) {
    throw new UnsupportedOperationException("unexpected invocation for "
        + getClass());
  }

  @Override
  public int computeHashCode(int maxWidth, int hash) {
    throw new UnsupportedOperationException("unexpected invocation for "
        + toString());
  }

  @Override
  public final void toDataForOptimizedResultHolder(java.io.DataOutput dos)
      throws java.io.IOException {
    throw new UnsupportedOperationException("unexpected invocation for "
        + toString());
  }

  @Override
  public final void fromDataForOptimizedResultHolder(java.io.DataInput dis)
      throws java.io.IOException, ClassNotFoundException {
    throw new UnsupportedOperationException("unexpected invocation for "
        + toString());
  }
// GemStone changes END
}
