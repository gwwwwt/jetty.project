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

package org.eclipse.jetty.server.session;

import java.util.Set;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;

/**
 * NullSessionDataStore
 *
 * Does not actually store anything, useful for testing.
 */
@ManagedObject
public class NullSessionDataStore extends AbstractSessionDataStore
{

    @Override
    public SessionData doLoad(String id) throws Exception
    {
        return null;
    }

    @Override
    public SessionData newSessionData(String id, long created, long accessed, long lastAccessed, long maxInactiveMs)
    {
        return new SessionData(id, _context.getCanonicalContextPath(), _context.getVhost(), created, accessed, lastAccessed, maxInactiveMs);
    }

    @Override
    public boolean delete(String id) throws Exception
    {
        return true;
    }

    @Override
    public void doStore(String id, SessionData data, long lastSaveTime) throws Exception
    {
        //noop
    }

    @Override
    public Set<String> doGetExpired(Set<String> candidates)
    {
        return candidates; //whatever is suggested we accept
    }

    @ManagedAttribute(value = "does this store serialize sessions", readonly = true)
    @Override
    public boolean isPassivating()
    {
        return false;
    }

    @Override
    public boolean exists(String id)
    {
        return false;
    }
}
