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

package com.vaushell.spipes.nodes.twitter;

import com.vaushell.spipes.dispatch.Message;
import com.vaushell.spipes.nodes.A_Node;
import com.vaushell.spipes.tools.scribe.twitter.TwitterClient;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a tweet from Twitter. Return message if successfully delete.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class N_TW_Delete
    extends A_Node
{
    // PUBLIC
    public N_TW_Delete()
    {
        super( null ,
               DEFAULT_ANTIBURST );

        this.client = new TwitterClient();
    }

    // PROTECTED
    @Override
    protected void prepareImpl()
        throws Exception
    {
        final Path tokenPath = getDispatcher().getDatas().resolve( Paths.get( getNodeID() ,
                                                                              "token" ) );

        client.login( getConfig( "key" ) ,
                      getConfig( "secret" ) ,
                      tokenPath ,
                      getDispatcher().getVCodeFactory().create( "[" + getClass().getName() + " / " + getNodeID() + "] " ) );
    }

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

        // Send to Twitter
        if ( getMessage().contains( "id-twitter" ) )
        {
            final long ID = (long) getMessage().getProperty( "id-twitter" );

            if ( client.deleteTweet( ID ) )
            {
                if ( LOGGER.isTraceEnabled() )
                {
                    LOGGER.trace( "[" + getNodeID() + "] can delete tweet ID=" + ID );
                }

                sendMessage();
            }
            else
            {
                if ( LOGGER.isTraceEnabled() )
                {
                    LOGGER.trace( "[" + getNodeID() + "] cannot delete tweet ID=" + ID );
                }
            }
        }
    }

    @Override
    protected void terminateImpl()
        throws Exception
    {
        // Nothing
    }
    // PRIVATE
    private static final Logger LOGGER = LoggerFactory.getLogger( N_TW_Delete.class );
    private final TwitterClient client;
}