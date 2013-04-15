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

package org.eclipse.jetty.websocket.jsr356.server;

import java.net.URI;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.jsr356.server.samples.BasicEchoSocket;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class BasicAnnotatedTest
{
    @Rule
    public TestingDir testdir = new TestingDir();

    @Test
    public void testEcho() throws Exception
    {
        WSServer wsb = new WSServer(testdir,"app");
        wsb.createWebInf();
        wsb.copyEndpoint(BasicEchoSocket.class);

        try
        {
            wsb.start();
            URI uri = wsb.getServerBaseURI();

            WebAppContext webapp = wsb.createWebAppContext();
            AnnotationConfiguration annocfg = new AnnotationConfiguration();
            annocfg.addDiscoverableAnnotationHandler(new ServerEndpointAnnotationHandler(webapp));
            // @formatter:off
            webapp.setConfigurations(new Configuration[] {
                    annocfg, 
                    new WebXmlConfiguration(),
                    new WebInfConfiguration(),
                    new PlusConfiguration(), 
                    new MetaInfConfiguration(),
                    new FragmentConfiguration(), 
                    new EnvConfiguration()});
            // @formatter:on
            wsb.deployWebapp(webapp);
            // wsb.dump();

            WebSocketClient client = new WebSocketClient();
            try
            {
                client.start();
                JettyEchoSocket clientEcho = new JettyEchoSocket();
                Future<Session> foo = client.connect(clientEcho,uri.resolve("/echo"));
                // wait for connect
                foo.get(1,TimeUnit.SECONDS);
                clientEcho.sendMessage("Hello World");
                Queue<String> msgs = clientEcho.awaitMessages(1);
                Assert.assertEquals("Expected message","Hello World",msgs.poll());
            }
            finally
            {
                client.stop();
            }
        }
        finally
        {
            wsb.stop();
        }
    }
}
