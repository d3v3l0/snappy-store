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
package com.gemstone.gemfire.internal.cache.xmlcache;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.ClientSession;
import com.gemstone.gemfire.cache.InterestRegistrationListener;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.cache.server.ClientSubscriptionConfig;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.cache.AbstractBridgeServer;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;

/**
 * Represents a {@link CacheServer} that is created declaratively.
 *
 * @author David Whitlock
 * @since 4.0
 */
public class BridgeServerCreation extends AbstractBridgeServer {

  // moved to AbstractBridgeServer
  
  //////////////////////  Constructors  //////////////////////

  /**
   * Creates a new <code>BridgeServerCreation</code> with the default
   * configuration.
   *
   * @param cache
   *        The cache being served
   */
  BridgeServerCreation(Cache cache) {
    super(cache);
  }

  BridgeServerCreation(Cache cache, boolean attachListener) {
    super(cache, attachListener);
  }

  /////////////////////  Instance Methods  /////////////////////

  @Override
  public void start() throws IOException {
    // This method is invoked during testing, but it is not necessary
    // to do anything.
  }

  @Override
  public void setNotifyBySubscription(boolean b) {
    this.notifyBySubscription = b;
  }

  @Override
  public boolean getNotifyBySubscription() {
    return this.notifyBySubscription;
  }

  @Override
  public void setSocketBufferSize(int socketBufferSize) {
    this.socketBufferSize = socketBufferSize;
  }
  
  @Override
  public int getSocketBufferSize() {
    return this.socketBufferSize;
  }
  
  @Override
  public void setTcpNoDelay(boolean setting) {
    this.tcpNoDelay = setting;
  }
  
  @Override
  public boolean getTcpNoDelay() {
    return this.tcpNoDelay;
  }

  @Override
  public void setMaximumTimeBetweenPings(int maximumTimeBetweenPings) {
    this.maximumTimeBetweenPings = maximumTimeBetweenPings;
  }
  
  @Override
  public int getMaximumTimeBetweenPings() {
    return this.maximumTimeBetweenPings;
  }
  
  @Override
  public int getMaximumMessageCount() {
    return this.maximumMessageCount;
  }

  @Override
  public void setMaximumMessageCount(int maximumMessageCount) {
    this.maximumMessageCount = maximumMessageCount;
  }
  
  @Override
  public int getMessageTimeToLive() {
    return this.messageTimeToLive;
  }

  @Override
  public void setMessageTimeToLive(int messageTimeToLive) {
    this.messageTimeToLive = messageTimeToLive;
  }
  
  public boolean isRunning() {
    throw new UnsupportedOperationException(LocalizedStrings.SHOULDNT_INVOKE.toLocalizedString());
  }

  public void stop() {
    throw new UnsupportedOperationException(LocalizedStrings.SHOULDNT_INVOKE.toLocalizedString());
  }

  /**
   * Returns whether or not this bridge server has the same
   * configuration as another bridge server.
   */
  @Override
  public boolean sameAs(CacheServer other) {
    ClientSubscriptionConfig cscThis = this.getClientSubscriptionConfig();
    ClientSubscriptionConfig cscOther = other.getClientSubscriptionConfig();
    boolean result = 
        this.getPort() == other.getPort() &&
        this.getSocketBufferSize() == other.getSocketBufferSize() &&
        this.getMaximumTimeBetweenPings() == other.getMaximumTimeBetweenPings() &&
        this.getNotifyBySubscription() == other.getNotifyBySubscription() &&
        this.getMaxConnections() == other.getMaxConnections() &&
        this.getMaxThreads() == other.getMaxThreads() &&
        this.getMaximumMessageCount() == other.getMaximumMessageCount() &&
        this.getMessageTimeToLive() == other.getMessageTimeToLive() &&
        this.getTcpNoDelay() == other.getTcpNoDelay() &&
        cscThis.getCapacity() == cscOther.getCapacity() &&
        cscThis.getEvictionPolicy().equals(cscOther.getEvictionPolicy());
    String diskStoreName = cscThis.getDiskStoreName();
    if (diskStoreName != null) {
      result = result && diskStoreName.equals(cscOther.getDiskStoreName());
    } else {
      result = result && cscThis.getOverflowDirectory().equals(cscOther.getOverflowDirectory());
    }
    return result;
  }

  @Override
  public String toString()
  {
    return "BridgeServerCreation on port " + this.getPort() +
    " notify by subscription " + this.getNotifyBySubscription() +
    " maximum time between pings " + this.getMaximumTimeBetweenPings() + 
    " socket buffer size " + this.getSocketBufferSize() + 
    " maximum connections " + this.getMaxConnections() +
    " maximum threads " + this.getMaxThreads() +
    " maximum message count " + this.getMaximumMessageCount() +
    " message time to live " + this.getMessageTimeToLive() +
    " groups " + Arrays.asList(getGroups()) +
    " loadProbe " + loadProbe +
    " loadPollInterval " + loadPollInterval +
    this.getClientSubscriptionConfig().toString();
  }
  
  public ClientSubscriptionConfig getClientSubscriptionConfig(){
    return this.clientSubscriptionConfig;
  }

  public Set getInterestRegistrationListeners() {
    //TODO Yogesh : implement me 
    return null;
  }

  public void registerInterestRegistrationListener(
      InterestRegistrationListener listener) {
    //TODO Yogesh : implement me
  }

  public void unregisterInterestRegistrationListener(
      InterestRegistrationListener listener) {
    //TODO Yogesh : implement me
  }

  /* (non-Javadoc)
   * @see com.gemstone.gemfire.cache.util.BridgeServer#getAllClientSessions()
   */
  public Set getAllClientSessions() {
    throw new UnsupportedOperationException("Shouldn't be invoked");
  }

  /* (non-Javadoc)
   * @see com.gemstone.gemfire.cache.util.BridgeServer#getClientSession(com.gemstone.gemfire.distributed.DistributedMember)
   */
  public ClientSession getClientSession(DistributedMember member) {
    throw new UnsupportedOperationException("Shouldn't be invoked");
  }

  /* (non-Javadoc)
   * @see com.gemstone.gemfire.cache.util.BridgeServer#getClientSession(java.lang.String)
   */
  public ClientSession getClientSession(String durableClientId) {
    throw new UnsupportedOperationException("Shouldn't be invoked");
  }

}
