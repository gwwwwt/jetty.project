//
// ========================================================================
// Copyright (c) 1995-2020 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty;

import com.acme.ChatServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ChatServletTest
{

    private final ServletTester tester = new ServletTester();

    @BeforeEach
    public void setUp() throws Exception
    {
        tester.setContextPath("/");

        ServletHolder dispatch = tester.addServlet(ChatServlet.class, "/chat/*");
        dispatch.setInitParameter("asyncTimeout", "500");
        tester.start();
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        tester.stop();
    }

    @Test
    public void testLogin() throws Exception
    {
        assertResponse("user=test&join=true&message=has%20joined!", "{\"from\":\"test\",\"chat\":\"has joined!\"}");
    }

    @Test
    public void testChat() throws Exception
    {
        assertResponse("user=test&join=true&message=has%20joined!", "{\"from\":\"test\",\"chat\":\"has joined!\"}");
        String response = tester.getResponses(createRequestString("user=test&message=message"));
        assertThat(response.contains("{"), is(false)); // make sure we didn't get a json body
    }

    @Test
    public void testPoll() throws Exception
    {
        assertResponse("user=test", "{action:\"poll\"}");
    }

    private void assertResponse(String requestBody, String expectedResponse) throws Exception
    {
        String response = tester.getResponses(createRequestString(requestBody));
        assertThat(response.contains(expectedResponse), is(true));
    }

    private String createRequestString(String body)
    {
        StringBuilder req1 = new StringBuilder();
        req1.append("POST /chat/ HTTP/1.1\r\n");
        req1.append("Host: tester\r\n");
        req1.append("Content-length: " + body.length() + "\r\n");
        req1.append("Content-type: application/x-www-form-urlencoded\r\n");
        req1.append("Connection: close\r\n");
        req1.append("\r\n");
        req1.append(body);
        return req1.toString();
    }
}
