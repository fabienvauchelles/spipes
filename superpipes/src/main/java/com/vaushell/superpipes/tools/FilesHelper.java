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

package com.vaushell.superpipes.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * Files helper.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public final class FilesHelper
{
    // PUBLIC
    /**
     * Read a file content, or wait the file to be created. Delete the file after read.
     *
     * @param p the file path
     * @return the content
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings( "unchecked" )
    public static String fileContent( final Path p )
        throws IOException , InterruptedException
    {
        if ( p == null )
        {
            throw new IllegalArgumentException();
        }

        if ( Files.exists( p ) )
        {
            return readAndDelete( p );
        }

        final Path parent = p.getParent();
        if ( Files.notExists( parent ) )
        {
            Files.createDirectory( parent );
        }
        else
        {
            if ( !Files.isDirectory( parent ) )
            {
                throw new IllegalArgumentException( parent + " exists but is not a directory" );
            }
        }

        try( final WatchService watcher = FileSystems.getDefault().newWatchService() )
        {
            parent.register( watcher ,
                             StandardWatchEventKinds.ENTRY_CREATE );

            while ( true )
            {
                final WatchKey key = watcher.take();

                for ( final WatchEvent<?> event : key.pollEvents() )
                {
                    final WatchEvent.Kind<?> kind = event.kind();

                    if ( kind == StandardWatchEventKinds.ENTRY_CREATE )
                    {
                        final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        final Path p2 = parent.resolve( ev.context() );
                        if ( p.equals( p2 ) )
                        {
                            return readAndDelete( p );
                        }
                    }
                }
            }
        }
    }

    /**
     * Read and delete file and return the content.
     *
     * @param p the file path
     * @return the content
     * @throws IOException
     */
    public static String readAndDelete( final Path p )
        throws IOException
    {
        final StringBuilder sb = new StringBuilder();

        try( BufferedReader bfr = Files.newBufferedReader( p ,
                                                           Charset.forName( "UTF-8" ) ) )
        {
            String line = bfr.readLine();
            while ( line != null )
            {
                sb.append( line );

                line = bfr.readLine();
            }
        }

        Files.delete( p );

        return sb.toString();
    }

    /**
     * Create a non existent filename.
     *
     * @param parent Parent directory
     * @param prefix File's prefix
     * @param suffix File's suffix
     * @return Path
     */
    public static Path createIncrNonExistentFilename( final Path parent ,
                                                      final String prefix ,
                                                      final String suffix )
    {
        int i = 1;
        Path result;
        do
        {
            result = parent.resolve( prefix + i + suffix );
            ++i;
        }
        while ( Files.exists( result ) );

        return result;
    }

    // PRIVATE
    private FilesHelper()
    {
        // Nothing
    }
}
