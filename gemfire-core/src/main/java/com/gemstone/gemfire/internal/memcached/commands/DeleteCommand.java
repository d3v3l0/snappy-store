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
package com.gemstone.gemfire.internal.memcached.commands;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.memcached.KeyWrapper;
import com.gemstone.gemfire.internal.memcached.Reply;
import com.gemstone.gemfire.internal.memcached.RequestReader;
import com.gemstone.gemfire.internal.memcached.ResponseStatus;
import com.gemstone.gemfire.internal.memcached.ValueWrapper;
import com.gemstone.gemfire.memcached.GemFireMemcachedServer.Protocol;

/**
 * The command "delete" allows for explicit deletion of items:
 * delete <key> [noreply]\r\n
 * 
 * @author Swapnil Bawaskar
 *
 */
public class DeleteCommand extends AbstractCommand {

  @Override
  public ByteBuffer processCommand(RequestReader request, Protocol protocol, Cache cache) {
    if (protocol == protocol.ASCII) {
      return processAsciiCommand(request.getRequest(), cache);
    }
    return processBinaryCommand(request, cache);
  }

  private ByteBuffer processAsciiCommand(ByteBuffer buffer, Cache cache) {
    CharBuffer flb = getFirstLineBuffer();
    getAsciiDecoder().reset();
    getAsciiDecoder().decode(buffer, flb, false);
    flb.flip();
    String firstLine = getFirstLine();
    String[] firstLineElements = firstLine.split(" ");

    assert "delete".equals(firstLineElements[0]);
    String key = stripNewline(firstLineElements[1]);
    boolean noReply = firstLineElements.length > 2;
    Region<Object, ValueWrapper> r = getMemcachedRegion(cache);
    String reply = null;
    try {
      r.destroy(key);
      reply = Reply.DELETED.toString();
    } catch (EntryNotFoundException e) {
      reply = Reply.NOT_FOUND.toString();
    }
    return noReply ? null : asciiCharset.encode(reply);
  }

  private ByteBuffer processBinaryCommand(RequestReader request, Cache cache) {
    ByteBuffer buffer = request.getRequest();
    ByteBuffer response = request.getResponse();
    
    KeyWrapper key = getKey(buffer, HEADER_LENGTH);
    
    Region<Object, ValueWrapper> r = getMemcachedRegion(cache);
    try {
      r.destroy(key);
      if (isQuiet()) {
        return null;
      }
      response.putShort(POSITION_RESPONSE_STATUS, ResponseStatus.NO_ERROR.asShort());
    } catch (EntryNotFoundException e) {
      response.putShort(POSITION_RESPONSE_STATUS, ResponseStatus.KEY_NOT_FOUND.asShort());
    }
    if (getLogger().fineEnabled()) {
      getLogger().fine("delete:key:"+key);
    }
    return response;
  }
  
  /**
   * Overridden by Q command
   */
  protected boolean isQuiet() {
    return false;
  }
}
