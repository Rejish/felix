/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.base.internal.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;

import org.apache.felix.http.base.internal.runtime.ServletContextHelperInfo;

/**
 * Registry for all services.
 *
 * The registry is organized per servlet context and is dispatching to one
 * of the {@link PerContextHandlerRegistry} registries.
 */
public final class HandlerRegistry
{
    /** Current list of context registrations. */
    private volatile List<PerContextHandlerRegistry> registrations = Collections.emptyList();

    public HandlerRegistry()
    {
        this.add(new PerContextHandlerRegistry());
    }

    /**
     * Shutdown
     */
    public void shutdown()
    {
        final List<PerContextHandlerRegistry> list;

        synchronized ( this )
        {
            list = new ArrayList<PerContextHandlerRegistry>(this.registrations);
            this.registrations = Collections.emptyList();

        }

        for(final PerContextHandlerRegistry r : list)
        {
            r.removeAll();
        }
    }

    /**
     * Add a context registration.
     * @param info The servlet context helper info
     */
    public void add(@Nonnull ServletContextHelperInfo info)
    {
        this.add(new PerContextHandlerRegistry(info));
    }

    /**
     * Remove a context registration.
     * @param info The servlet context helper info
     */
    public void remove(@Nonnull ServletContextHelperInfo info)
    {
        synchronized ( this )
        {
            final List<PerContextHandlerRegistry> updatedList = new ArrayList<PerContextHandlerRegistry>(this.registrations);
            final Iterator<PerContextHandlerRegistry> i = updatedList.iterator();
            while ( i.hasNext() )
            {
                final PerContextHandlerRegistry reg = i.next();
                if ( reg.getContextServiceId() == info.getServiceId() )
                {
                    i.remove();
                    this.registrations = updatedList;
                    break;
                }
            }
        }
    }


    /**
     * Add a new context registration.
     */
    private void add(@Nonnull PerContextHandlerRegistry registry)
    {
        synchronized ( this )
        {
            final List<PerContextHandlerRegistry> updatedList = new ArrayList<PerContextHandlerRegistry>(this.registrations);
            updatedList.add(registry);
            Collections.sort(updatedList);

            this.registrations = updatedList;
        }
    }

    /**
     * Get the per context registry.
     * @param info The servlet context helper info or {@code null} for the Http Service context.
     * @return A per context registry or {@code null}
     */
    public PerContextHandlerRegistry getRegistry(final ServletContextHelperInfo info)
    {
        final long key = (info == null ? 0 : info.getServiceId());

        synchronized ( this )
        {
            for(final PerContextHandlerRegistry r : this.registrations)
            {
                if ( key == r.getContextServiceId())
                {
                    return r;
                }
            }
        }

        return null;
    }

    public ErrorsMapping getErrorsMapping(final String requestURI, final Long serviceId)
    {
        final List<PerContextHandlerRegistry> regs = this.registrations;
        for(final PerContextHandlerRegistry r : regs)
        {
            if ( serviceId != null && serviceId == r.getContextServiceId() )
            {
                return r.getErrorsMapping();
            }
            else if ( serviceId == null && requestURI.startsWith(r.getPrefixPath()) )
            {
                return r.getErrorsMapping();
            }
        }

        return null;
    }

    private static FilterHandler[] EMPTY_FILTER_HANDLER = new FilterHandler[0];

    public FilterHandler[] getFilterHandlers(final ServletHandler servletHandler,
            final DispatcherType dispatcherType,
            final String requestURI)
    {
        final long id = servletHandler.getContextServiceId();
        final List<PerContextHandlerRegistry> regs = this.registrations;
        for(final PerContextHandlerRegistry r : regs)
        {
            if ( id == r.getContextServiceId() )
            {
                return r.getFilterHandlers(servletHandler, dispatcherType, requestURI);
            }
        }
        return EMPTY_FILTER_HANDLER;
    }

    public ServletHandler getServletHandlerByName(final Long contextId, final String name)
    {
        if ( contextId != null )
        {
            final List<PerContextHandlerRegistry> regs = this.registrations;
            for(final PerContextHandlerRegistry r : regs)
            {
                if ( contextId == r.getContextServiceId() )
                {
                    return r.getServletHandlerByName(name);
                }
            }
        }
        return null;
    }

    public ServletHandler getServletHander(final String requestURI)
    {
        final List<PerContextHandlerRegistry> regs = this.registrations;
        for(final PerContextHandlerRegistry r : regs)
        {
            if ( requestURI.startsWith(r.getPrefixPath()))
            {
                final ServletHandler handler = r.getServletHander(requestURI.substring(r.getPrefixPath().length() - 1));
                if ( handler != null )
                {
                    return handler;
                }
            }
        }
        return null;
    }
}
