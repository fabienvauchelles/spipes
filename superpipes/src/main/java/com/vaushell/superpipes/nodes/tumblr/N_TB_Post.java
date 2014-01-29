/*
 * Copyright (C) 2013 Fabien Vauchelles (fabien_AT_vauchelles_DOT_com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3, 29 June 2007, of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.vaushell.superpipes.nodes.tumblr;

import com.vaushell.superpipes.dispatch.Message;
import com.vaushell.superpipes.dispatch.Tags;
import com.vaushell.superpipes.nodes.A_Node;
import com.vaushell.superpipes.tools.retry.A_Retry;
import com.vaushell.superpipes.tools.scribe.tumblr.TumblrClient;
import com.vaushell.superpipes.tools.scribe.tumblr.TumblrException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post a message to Tumblr.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class N_TB_Post
    extends A_Node
{
    // PUBLIC
    public N_TB_Post()
    {
        super( null ,
               DEFAULT_ANTIBURST );

        this.client = new TumblrClient();
    }

    // PROTECTED
    @Override
    protected void prepareImpl()
        throws Exception
    {
        final Path tokenPath = getDispatcher().getDatas().resolve( Paths.get( getNodeID() ,
                                                                              "token" ) );

        client.login( getProperties().getConfigString( "key" ) ,
                      getProperties().getConfigString( "secret" ) ,
                      tokenPath ,
                      getDispatcher().getVCodeFactory().create( "[" + getClass().getName() + " / " + getNodeID() + "] " ) );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void loop()
        throws Exception
    {
        // Receive
        setMessage( getLastMessageOrWait() );

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] receive message : " + Message.formatSimple( getMessage() ) );
        }

        if ( !getMessage().contains( Message.KeyIndex.URI ) )
        {
            throw new IllegalArgumentException( "message doesn't have an uri" );
        }

        // Send to TB
        final URI uri = (URI) getMessage().getProperty( Message.KeyIndex.URI );

        final DateTime date;
        if ( getProperties().getConfigBoolean( "backdating" ,
                                               Boolean.FALSE ) )
        {
            date = (DateTime) getMessage().getProperty( Message.KeyIndex.PUBLISHED_DATE );
        }
        else
        {
            date = null;
        }

        final String desc;
        if ( getMessage().contains( Message.KeyIndex.CONTENT ) )
        {
            desc = (String) getMessage().getProperty( Message.KeyIndex.CONTENT );
        }
        else
        {
            desc = (String) getMessage().getProperty( Message.KeyIndex.DESCRIPTION );
        }

        final long ID = new A_Retry<Long>()
        {
            @Override
            protected Long executeContent()
                throws IOException , TumblrException
            {
                final String title = (String) getMessage().getProperty( Message.KeyIndex.TITLE );

                final long ID = client.postLink( getProperties().getConfigString( "blogname" ) ,
                                                 uri == null ? null : uri.toString() ,
                                                 title ,
                                                 desc ,
                                                 (Tags) getMessage().getProperty( Message.KeyIndex.TAGS ) ,
                                                 date );

                if ( ID < 0 )
                {
                    throw new IOException( "Cannot post link with title=" + title );
                }
                else
                {
                    return ID;
                }
            }
        }
            .setRetry( getProperties().getConfigInteger( "retry" ,
                                                         10 ) )
            .setWaitTime( getProperties().getConfigDuration( "wait-time" ,
                                                             new Duration( 5000L ) ) )
            .setWaitTimeMultiplier( getProperties().getConfigDouble( "wait-time-multiplier" ,
                                                                     2.0 ) )
            .setJitterRange( getProperties().getConfigInteger( "jitter-range" ,
                                                               500 ) )
            .setMaxDuration( getProperties().getConfigDuration( "max-duration" ,
                                                                new Duration( 0L ) ) )
            .execute();

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] receive ID : " + ID );
        }

        getMessage().setProperty( "id-tumblr" ,
                                  ID );

        sendMessage();
    }

    @Override
    protected void terminateImpl()
        throws Exception
    {
        // Nothing
    }
    // PRIVATE
    private static final Logger LOGGER = LoggerFactory.getLogger( N_TB_Post.class );
    private final TumblrClient client;
}
