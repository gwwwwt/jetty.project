//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.websocket.client;

import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.toolchain.test.TestTracker;
import org.eclipse.jetty.toolchain.test.annotation.Slow;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.blockhead.BlockheadServer;
import org.eclipse.jetty.websocket.client.blockhead.BlockheadServer.ServerConnection;
import org.eclipse.jetty.websocket.client.masks.ZeroMasker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SlowServerTest
{
    @Rule
    public TestTracker tt = new TestTracker();

    private BlockheadServer server;
    private WebSocketClient client;

    @Before
    public void startClient() throws Exception
    {
        client = new WebSocketClient();
        client.getPolicy().setIdleTimeout(60000);
        client.start();
    }

    @Before
    public void startServer() throws Exception
    {
        server = new BlockheadServer();
        server.start();
    }

    @After
    public void stopClient() throws Exception
    {
        client.stop();
    }

    @After
    public void stopServer() throws Exception
    {
        server.stop();
    }

    @Test
    @Slow
    public void testServerSlowToRead() throws Exception
    {
        TrackingSocket tsocket = new TrackingSocket();
        client.setMasker(new ZeroMasker());
        client.getPolicy().setIdleTimeout(60000);

        URI wsUri = server.getWsUri();
        Future<Session> future = client.connect(tsocket,wsUri);

        ServerConnection sconnection = server.accept();
        sconnection.setSoTimeout(60000);
        sconnection.upgrade();

        // Confirm connected
        future.get(500,TimeUnit.MILLISECONDS);
        tsocket.waitForConnected(500,TimeUnit.MILLISECONDS);

        int messageCount = 10; // TODO: increase to 1000

        // Setup slow server read thread
        ServerReadThread reader = new ServerReadThread(sconnection);
        reader.setExpectedMessageCount(messageCount);
        reader.setSlowness(100); // slow it down
        reader.start();

        // Have client write as quickly as it can.
        ClientWriteThread writer = new ClientWriteThread(tsocket.getConnection());
        writer.setMessageCount(messageCount);
        writer.setMessage("Hello");
        writer.setSlowness(-1); // disable slowness
        writer.start();
        writer.join();

        // Verify receive
        reader.waitForExpectedMessageCount(10,TimeUnit.SECONDS);
        Assert.assertThat("Frame Receive Count",reader.getFrameCount(),is(messageCount));

        // Close
        tsocket.getConnection().close(StatusCode.NORMAL,"Done");

        Assert.assertTrue("Client Socket Closed",tsocket.closeLatch.await(10,TimeUnit.SECONDS));
        tsocket.assertCloseCode(StatusCode.NORMAL);

        reader.cancel(); // stop reading
    }

    @Test
    @Slow
    public void testServerSlowToSend() throws Exception
    {
        // final Exchanger<String> exchanger = new Exchanger<String>();
        TrackingSocket tsocket = new TrackingSocket();
        // tsocket.messageExchanger = exchanger;
        client.setMasker(new ZeroMasker());
        client.getPolicy().setIdleTimeout(60000);

        URI wsUri = server.getWsUri();
        Future<Session> future = client.connect(tsocket,wsUri);

        ServerConnection sconnection = server.accept();
        sconnection.setSoTimeout(60000);
        sconnection.upgrade();

        // Confirm connected
        future.get(500,TimeUnit.MILLISECONDS);
        tsocket.waitForConnected(500,TimeUnit.MILLISECONDS);

        // Have server write slowly.
        int messageCount = 1000;

        ServerWriteThread writer = new ServerWriteThread(sconnection);
        writer.setMessageCount(messageCount);
        writer.setMessage("Hello");
        // writer.setExchanger(exchanger);
        writer.setSlowness(10);
        writer.start();
        writer.join();

        // Verify receive
        Assert.assertThat("Message Receive Count",tsocket.messageQueue.size(),is(messageCount));

        // Close
        sconnection.close(StatusCode.NORMAL);

        Assert.assertTrue("Client Socket Closed",tsocket.closeLatch.await(10,TimeUnit.SECONDS));
        tsocket.assertCloseCode(StatusCode.NORMAL);
    }
}
