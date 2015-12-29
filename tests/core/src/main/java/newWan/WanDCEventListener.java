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
package newWan;

import hydra.Log;

import java.util.HashMap;

import util.ValueHolder;
import wan.WANClient;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;

import durableClients.DurableClientsBB;

/**
 * 
 * @author Rahul Diyewar
 * @since 7.0
 * 
 */
public class WanDCEventListener extends hct.EventListener {

  static Object lock = new Object();

  /**
   * A local map to store the last received values for the keys in the callback
   * events.
   */
  public void afterCreate(EntryEvent event) {
    super.afterCreate(event);
    WANClient.lastEventReceivedTime = System.currentTimeMillis();

    String key = (String)event.getKey();

    Log.getLogWriter().info("Invoking the durable Listener");
    ValueHolder value = (ValueHolder)event.getNewValue();

    String VmDurableId = ((InternalDistributedSystem)InternalDistributedSystem
        .getAnyInstance()).getConfig().getDurableClientId();
    Log.getLogWriter().info("Durable vm id is " + VmDurableId);

    synchronized (lock) {

      boolean isDuplicate = false;

      if (((Long)value.getMyValue()).longValue() != 0) {
        isDuplicate = checkDuplicate(key, value);
      }

      if (!isDuplicate) {
        HashMap map = (HashMap)DurableClientsBB.getBB().getSharedMap().get(
            VmDurableId);
        if (!map.containsKey(key)) {
          HashMap threadMap = new HashMap();
          threadMap.put("EVENT No :", new Integer(0));
          map.put(key, threadMap);
        }

        HashMap threadMap = (HashMap)map.get(key);

        int eventNo = ((Integer)threadMap.get("EVENT No :")).intValue();
        threadMap.put("EVENT No :", new Integer(++eventNo));

        threadMap.put("EVENT SR. No : " + eventNo, event.getNewValue());
        threadMap.put(key, value);
        map.put(key, threadMap);

        DurableClientsBB.getBB().getSharedMap().put(VmDurableId, map);
        Log.getLogWriter().info(
            "Updated the BB for the key " + key + " for the event "
                + event.getNewValue() + " EVENT No: " + eventNo);
      }

    }
  }

  public void afterUpdate(EntryEvent event) {
    super.afterUpdate(event);
    WANClient.lastEventReceivedTime = System.currentTimeMillis();

    Log.getLogWriter().info("Invoking the durable Listener");

    String key = (String)event.getKey();
    ValueHolder newValue = (ValueHolder)event.getNewValue();

    String VmDurableId = ((InternalDistributedSystem)InternalDistributedSystem
        .getAnyInstance()).getConfig().getDurableClientId();

    synchronized (lock) {

      boolean isDuplicate = false;

      if (((Long)newValue.getMyValue()).longValue() != 0) {
        isDuplicate = checkDuplicate(key, newValue);
      }

      if (!isDuplicate) {

        HashMap map = (HashMap)DurableClientsBB.getBB().getSharedMap().get(
            VmDurableId);
        if (!map.containsKey(key)) {
          HashMap threadMap = new HashMap();
          threadMap.put("EVENT No :", new Integer(0));
          map.put(key, threadMap);
        }

        HashMap threadMap = (HashMap)map.get(key);

        int eventNo = ((Integer)threadMap.get("EVENT No :")).intValue();
        threadMap.put("EVENT No :", new Integer(++eventNo));
        threadMap.put("EVENT SR. No : " + eventNo, event.getNewValue());
        threadMap.put(key, newValue);
        map.put(key, threadMap);
        DurableClientsBB.getBB().getSharedMap().put(VmDurableId, map);
        Log.getLogWriter().info(
            "Updated the BB for the key " + key + " for the event "
                + event.getNewValue() + " EVENT No: " + eventNo);
      }
    }

  }

  public void afterDestroy(EntryEvent event) {
    super.afterDestroy(event);
    WANClient.lastEventReceivedTime = System.currentTimeMillis();
    Log.getLogWriter().info("Invoking the durable Listener");

    String key = (String)event.getKey();
    ValueHolder value = (ValueHolder)event.getOldValue();

    String VmDurableId = ((InternalDistributedSystem)InternalDistributedSystem
        .getAnyInstance()).getConfig().getDurableClientId();

    synchronized (lock) {
      HashMap map = (HashMap)DurableClientsBB.getBB().getSharedMap().get(
          VmDurableId);
      if (!map.containsKey(key)) {
        HashMap threadMap = new HashMap();
        threadMap.put("EVENT No :", new Integer(0));
        map.put(key, threadMap);
      }

      HashMap threadMap = (HashMap)map.get(key);
      int eventNo = ((Integer)threadMap.get("EVENT No :")).intValue();
      threadMap.put("EVENT No :", new Integer(++eventNo));
      threadMap.put("EVENT SR. No : " + eventNo, event.getNewValue());
      if (value != null) {
        threadMap.put(key, value);
      }

      map.put(key, threadMap);

      DurableClientsBB.getBB().getSharedMap().put(VmDurableId, map);

      Log.getLogWriter().info(
          "Updated the BB for the key " + key + " for the event "
              + event.getNewValue() + " EVENT No: " + eventNo);
    }

  }

  public void afterInvalidate(EntryEvent event) {
    super.afterInvalidate(event);
    WANClient.lastEventReceivedTime = System.currentTimeMillis();
    Log.getLogWriter().info("Invoking the durable Listener");

    String key = (String)event.getKey();
    ValueHolder oldValue = (ValueHolder)event.getOldValue();
    String VmDurableId = ((InternalDistributedSystem)InternalDistributedSystem
        .getAnyInstance()).getConfig().getDurableClientId();

    synchronized (lock) {
      HashMap map = (HashMap)DurableClientsBB.getBB().getSharedMap().get(
          VmDurableId);
      if (!map.containsKey(key)) {
        HashMap threadMap = new HashMap();
        threadMap.put("EVENT No :", new Integer(0));
        map.put(key, threadMap);
      }

      HashMap threadMap = (HashMap)map.get(key);
      int eventNo = ((Integer)threadMap.get("EVENT No :")).intValue();
      threadMap.put("EVENT No :", new Integer(++eventNo));
      threadMap.put("EVENT SR. No : " + eventNo, event.getNewValue());
      threadMap.put(key, oldValue);
      map.put(key, threadMap);

      DurableClientsBB.getBB().getSharedMap().put(VmDurableId, map);

      Log.getLogWriter().info(
          "Updated the BB for the key " + key + " for the event "
              + event.getNewValue() + " EVENT No: " + eventNo);
    }
  }

  private boolean checkDuplicate(String key, ValueHolder newValue) {
    boolean isDuplicate = false;
    String VmDurableId = ((InternalDistributedSystem)InternalDistributedSystem
        .getAnyInstance()).getConfig().getDurableClientId();
    HashMap map = (HashMap)DurableClientsBB.getBB().getSharedMap().get(
        VmDurableId);
    HashMap threadMap = (HashMap)map.get(key);
    if (threadMap == null) {
      Log.getLogWriter().info(
          "ThreadMap is null for vm " + VmDurableId + " for key " + key);
    }
    else {
      ValueHolder oldValue = (ValueHolder)threadMap.get(key);

      long diff = ((Long)newValue.getMyValue()).longValue() - ((Long)oldValue.getMyValue()).longValue();

      if (diff < 1) {
        isDuplicate = true;
      }
    }
    return isDuplicate;
  }

}
