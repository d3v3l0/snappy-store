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

package com.pivotal.gemfirexd.thrift.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.internal.shared.SystemProperties;
import com.pivotal.gemfirexd.thrift.common.GfxdTSSLSocket;
import com.pivotal.gemfirexd.thrift.common.SocketParameters;

/**
 * Server side custom TServerSocket replacement for SSL transport allowing to
 * increase input/output buffer sizes.
 * 
 * @author swale
 * @since gfxd 1.1
 */
public final class GfxdTSSLServerSocket extends TServerTransport {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(GfxdTSSLServerSocket.class.getName());

  /**
   * Underlying ServerSocket.
   */
  private final ServerSocket serverSocket;

  /**
   * Socket parameters like SSL parameters, buffer sizes, keep-alive settings.
   */
  private final SocketParameters socketParams;

  /**
   * Creates a server socket from underlying socket object
   */
  public GfxdTSSLServerSocket(ServerSocket serverSocket,
      InetSocketAddress bindAddress, SocketParameters params)
      throws TTransportException {
    this.socketParams = params;
    try {
      this.serverSocket = serverSocket;
      // Prevent 2MSL delay problem on server restarts
      serverSocket.setReuseAddress(true);
      // Bind to listening port
      if (!serverSocket.isBound()) {
        // backlog hardcoded to 100 as in TSSLTransportFactory
        serverSocket.bind(bindAddress, 100);
      }
    } catch (IOException ioe) {
      throw new TTransportException(TTransportException.NOT_OPEN,
          "Could not bind to host:port " + bindAddress.toString(), ioe);
    }
  }

  @Override
  public void listen() throws TTransportException {
    // Make sure not to block on accept
    try {
      this.serverSocket.setSoTimeout(0);
    } catch (SocketException se) {
      LOGGER.error("Could not set socket timeout to 0.", se);
    }
  }

  @Override
  protected GfxdTSSLSocket acceptImpl() throws TTransportException {
    try {
      Socket srvSock = this.serverSocket.accept();
      return new GfxdTSSLSocket(srvSock, this.socketParams.getReadTimeout(0),
          this.socketParams, SystemProperties.getServerInstance());
    } catch (IOException ioe) {
      throw new TTransportException(ioe);
    }
  }

  @Override
  public void close() {
    try {
      this.serverSocket.close();
    } catch (IOException ioe) {
      LOGGER.warn("Could not close server socket.", ioe);
    }
  }

  @Override
  public void interrupt() {
    // The thread-safeness of this is dubious, but Java documentation suggests
    // that it is safe to do this from a different thread context
    close();
  }

  public ServerSocket getServerSocket() {
    return this.serverSocket;
  }
}
