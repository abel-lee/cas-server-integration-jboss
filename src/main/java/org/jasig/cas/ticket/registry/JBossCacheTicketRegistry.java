/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.Ticket;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of TicketRegistry that is backed by a JBoss TreeCache.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class JBossCacheTicketRegistry extends AbstractDistributedTicketRegistry {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** Indicator of what tree branch to put tickets in. */
    private static final String FQN_TICKET = "ticket";

    /** Instance of JBoss TreeCache. */
    @NotNull
    private Cache<String, Ticket> cache;
    
    
    
    protected void updateTicket(Ticket ticket) {
        try {
        	  if(log.isInfoEnabled()){
        		  log.info("Updating ticket  to registry for:"+ticket.getId());
        	  }
            this.cache.put(FQN_TICKET+"/"+ticket.getId(), ticket.getId(), ticket);
        } catch (final CacheException e) {
            throw new RuntimeException(e);
        }
    }

    public void addTicket(final Ticket ticket) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Adding ticket to registry for: " + ticket.getId());
            }
            if(log.isInfoEnabled()){
            	 log.info("Adding ticket to registry for: " + ticket.getId());
         	  }
            this.cache.put(FQN_TICKET+"/"+ticket.getId(), ticket.getId(), ticket);
        } catch (final CacheException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean deleteTicket(final String ticketId) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Removing ticket from registry for: " + ticketId);
            }
            if(log.isInfoEnabled()){
           	 log.info("Removing ticket from registry for: " + ticketId);
        	  }
           
            return this.cache.removeNode(FQN_TICKET+"/"+ticketId);
        } catch (final CacheException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Returns a proxied instance.
     * 
     * @see org.jasig.cas.ticket.registry.TicketRegistry#getTicket(java.lang.String)
     */
    public Ticket getTicket(final String ticketId) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Retrieving ticket from registry for: " + ticketId);
            }
            if(log.isInfoEnabled()){
              	 log.info("Retrieving ticket from registry for: " + ticketId);
           	  }
            return getProxiedTicketInstance(this.cache.get(FQN_TICKET+"/"+ticketId, ticketId));
        } catch (final CacheException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public Collection<Ticket> getTickets() {
        try {
        	 Node<String, Ticket> root =  this.cache.getNode(FQN_TICKET);
        	 if(root == null){
        		 return Collections.emptyList();
        	 }
            final Set<Node<String, Ticket>> nodes = root.getChildren();

            if (nodes == null) {
                return Collections.emptyList();
              }
            final List<Ticket> list = new ArrayList<Ticket>();
            for (final Node<String, Ticket> node : nodes) {
            	 Set<String> subKeys = node.getKeys();
            	 for (String subkey : subKeys) {
            		 list.add(node.get(subkey));
				}
             }
            if(log.isInfoEnabled()){
             	 log.info("Getting ticket from registry for: " +list.size());
          	  }
            return list;
        } catch (final CacheException e) {
            return Collections.emptyList();
        }
    }

    public void setCache(final Cache<String, Ticket> cache) {
        this.cache = cache;
    }

    @Override
    protected boolean needsCallback() {
        return true;
    }
}
