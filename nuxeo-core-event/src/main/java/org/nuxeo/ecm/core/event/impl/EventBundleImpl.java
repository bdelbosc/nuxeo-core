/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ecm.core.event.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class EventBundleImpl implements EventBundle {

    protected List<Event> events;

    /**
     * If true the bundle is controlled by a transaction otherwsie it is controlled by the SAVE event.
     * This means the bundle will be fired either at transaction commit either at SAVE event
     */
    protected boolean isTransacted;  
    
    public EventBundleImpl() {
        events = new ArrayList<Event>();
    }
    
    public boolean isTransacted() {
        return isTransacted;
    }
    
    public void setTransacted(boolean isTransacted) {
        this.isTransacted = isTransacted;
    }
    
    public String[] getEventNames() {
        String[] names = new String[events.size()];
        for (int i=0; i<names.length; i++) {
            names[i] = events.get(i).getName();
        }
        return names;
    }


    public Event[] getEvents() {
        return events.toArray(new Event[events.size()]);
    }


    public String getName() {
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0).getName();
    }


    public boolean isEmpty() {
        return events.isEmpty();
    }


    public Event peek() {
        return events.get(0);
    }


    public void push(Event event) {
        events.add(event);        
    }
    
    public int size() {
        return events.size();
    }


    public Iterator<Event> iterator() {
        return events.iterator();
    }

}