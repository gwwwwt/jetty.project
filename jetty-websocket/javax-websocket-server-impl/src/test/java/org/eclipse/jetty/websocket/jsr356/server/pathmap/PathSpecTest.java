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

package org.eclipse.jetty.websocket.jsr356.server.pathmap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class PathSpecTest
{
    public static void assertMatches(PathSpec spec, String path)
    {
        String msg = String.format("Spec(\"%s\").matches(\"%s\")",spec.getPathSpec(),path);
        assertThat(msg,spec.matches(path),is(true));
    }

    public static void assertNotMatches(PathSpec spec, String path)
    {
        String msg = String.format("!Spec(\"%s\").matches(\"%s\")",spec.getPathSpec(),path);
        assertThat(msg,spec.matches(path),is(false));
    }

    @Test
    public void testExactSpec()
    {
        PathSpec spec = new PathSpec("^/a$");
        assertEquals("Spec.pathSpec","^/a$",spec.getPathSpec());
        assertEquals("Spec.pattern","^/a$",spec.getPattern().pattern());
        assertEquals("Spec.pathDepth",1,spec.getPathDepth());
        assertEquals("Spec.group",PathSpecGroup.EXACT,spec.group);

        assertMatches(spec,"/a");

        assertNotMatches(spec,"/aa");
        assertNotMatches(spec,"/a/");
    }

    @Test
    public void testMiddleSpec()
    {
        PathSpec spec = new PathSpec("^/rest/([^/]*)/list$");
        assertEquals("Spec.pathSpec","^/rest/([^/]*)/list$",spec.getPathSpec());
        assertEquals("Spec.pattern","^/rest/([^/]*)/list$",spec.getPattern().pattern());
        assertEquals("Spec.pathDepth",3,spec.getPathDepth());
        assertEquals("Spec.group",PathSpecGroup.MIDDLE_GLOB,spec.group);

        assertMatches(spec,"/rest/api/list");
        assertMatches(spec,"/rest/1.0/list");
        assertMatches(spec,"/rest/2.0/list");
        assertMatches(spec,"/rest/accounts/list");

        assertNotMatches(spec,"/a");
        assertNotMatches(spec,"/aa");
        assertNotMatches(spec,"/aa/bb");
        assertNotMatches(spec,"/rest/admin/delete");
        assertNotMatches(spec,"/rest/list");
    }

    @Test
    public void testPrefixSpec()
    {
        PathSpec spec = new PathSpec("^/a/(.*)$");
        assertEquals("Spec.pathSpec","^/a/(.*)$",spec.getPathSpec());
        assertEquals("Spec.pattern","^/a/(.*)$",spec.getPattern().pattern());
        assertEquals("Spec.pathDepth",2,spec.getPathDepth());
        assertEquals("Spec.group",PathSpecGroup.PREFIX_GLOB,spec.group);

        assertMatches(spec,"/a/");
        assertMatches(spec,"/a/b");
        assertMatches(spec,"/a/b/c/d/e");

        assertNotMatches(spec,"/a");
        assertNotMatches(spec,"/aa");
        assertNotMatches(spec,"/aa/bb");
    }

    @Test
    public void testSuffixSpec()
    {
        PathSpec spec = new PathSpec("^(.*).do$");
        assertEquals("Spec.pathSpec","^(.*).do$",spec.getPathSpec());
        assertEquals("Spec.pattern","^(.*).do$",spec.getPattern().pattern());
        assertEquals("Spec.pathDepth",0,spec.getPathDepth());
        assertEquals("Spec.group",PathSpecGroup.SUFFIX_GLOB,spec.group);

        assertMatches(spec,"/a.do");
        assertMatches(spec,"/a/b/c.do");
        assertMatches(spec,"/abcde.do");
        assertMatches(spec,"/abc/efg.do");

        assertNotMatches(spec,"/a");
        assertNotMatches(spec,"/aa");
        assertNotMatches(spec,"/aa/bb");
        assertNotMatches(spec,"/aa/bb.do/more");
    }
}
