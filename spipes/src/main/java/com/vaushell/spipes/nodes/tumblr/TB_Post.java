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

package com.vaushell.spipes.nodes.tumblr;

import com.vaushell.spipes.transforms.bitly.I_URIshorten;
import com.vaushell.spipes.transforms.done.I_Identifier;
import com.vaushell.spipes.transforms.tags.I_Tags;
import java.net.URI;
import java.util.Set;

/**
 * A Tubmlr post.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class TB_Post
    implements I_Identifier , I_URIshorten , I_Tags
{
    // PUBLIC
    public TB_Post( final String message ,
                    final URI uri ,
                    final URI uriSource ,
                    final String uriName ,
                    final String uriDescription ,
                    final Set<String> tags )
    {
        this.ID = Long.MIN_VALUE;
        this.message = message;
        this.uri = uri;
        this.uriSource = uriSource;
        this.uriName = uriName;
        this.uriDescription = uriDescription;
        this.tags = tags;
    }

    @Override
    public String getID()
    {
        return Long.toString( ID );
    }

    @Override
    public void setID( final String ID )
    {
        this.ID = Long.parseLong( ID );
    }

    public long getTumblrID()
    {
        return ID;
    }

    public void setTumblrID( final long ID )
    {
        this.ID = ID;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( final String message )
    {
        this.message = message;
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public void setURI( final URI uri )
    {
        this.uri = uri;
    }

    @Override
    public URI getURIsource()
    {
        return uriSource;
    }

    @Override
    public void setURIsource( final URI uriSource )
    {
        this.uriSource = uriSource;
    }

    public String getURIname()
    {
        return uriName;
    }

    public void setURIname( final String uriName )
    {
        this.uriName = uriName;
    }

    public String getURIdescription()
    {
        return uriDescription;
    }

    public void setURIdescription( final String uriDescription )
    {
        this.uriDescription = uriDescription;
    }

    @Override
    public Set<String> getTags()
    {
        return tags;
    }

    @Override
    public void setTags( final Set<String> tags )
    {
        this.tags = tags;
    }
    // PRIVATE
    private long ID;
    private String message;
    private URI uri;
    private URI uriSource;
    private String uriName;
    private String uriDescription;
    private Set<String> tags;
}
