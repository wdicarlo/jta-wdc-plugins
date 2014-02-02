/*
 * JTA plugin to use Apache Telnet in JTA
 * 
 * (c) Walter Di Carlo 2010. All Rights Reserved.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */

package de.mud.jta.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.telnet.TelnetClient;

import de.mud.jta.FilterPlugin;
import de.mud.jta.Plugin;
import de.mud.jta.PluginBus;
import de.mud.jta.PluginConfig;
import de.mud.jta.event.ConfigurationListener;
import de.mud.jta.event.OnlineStatus;
import de.mud.jta.event.SocketListener;

/**
 * @author Walter Di Carlo
 */
public class ApacheTelnet extends Plugin implements FilterPlugin, SocketListener {

    private final static int debug = 0;
    private String           error = null;
    private TelnetClient     _telnet = new TelnetClient();
    private InputStream      _in;
    private OutputStream     _out;
    private boolean          online;

    /**
     * Create a new socket plugin.
     */
    public ApacheTelnet(final PluginBus bus, final String id) {
        super(bus, id);

        // register socket listener
        bus.registerPluginListener(this);

        bus.registerPluginListener(new ConfigurationListener() {
            public void setConfiguration(PluginConfig config) {
                String conf = config.getProperty("ApacheTelnet", id, "conf");
            }
        });

        //        // register an online status listener
        //        bus.registerPluginListener(new OnlineStatusListener() {
        //
        //
        //            public void online() {
        //                online = true;
        //            }
        //
        //            public void offline() {
        //                online = false;
        //            }
        //        });

    }

    /**
     * Connect to the host and port passed. If the multi relayd (mrelayd) is
     * used to allow connections to any host and the Socket.relay property
     * is configured this method will connect to the relay first, send
     * off the string "relay host port\n" and then the real connection will
     * be published to be online.
     */
    public void connect(String host, int port) throws IOException {
        if (host == null)
            return;
        if (debug > 0)
            error("connect(" + host + "," + port + ")");

        try {
            // Connect to the specified server
            _telnet.connect(host, port);

            // Get input and output stream references
            _in = _telnet.getInputStream();
            _out = _telnet.getOutputStream();

        } catch (Exception e) {
            error = "Sorry, Could not connect to: " + host + " " + port + "\r\n" + "Reason: " + e + "\r\n\r\n";
        }
        online = true;
        bus.broadcast(new OnlineStatus(online));
    }

    public boolean isConnected() {
        return _telnet.isConnected();
    }

    /** Disconnect the socket and close the connection. */
    public void disconnect() throws IOException {
        if (online == false || isConnected() == false) {
            return;
        }
        if (debug > 0)
            error("disconnect()");
        online = false;
        bus.broadcast(new OnlineStatus(false));
        try {
            _in.close();
            _out.close();
            _telnet.disconnect();
        } catch (Exception e) {
            error("disconnect() - cannot close everything");
        }

    }

    public void setFilterSource(FilterPlugin plugin) {
        // we do not have a source other than our socket
    }

    public FilterPlugin getFilterSource() {
        return null;
    }

    public int read(byte[] b) throws IOException {

        if (online == false || isConnected() == false) {
            disconnect();
            return -1;
        }

        // send error messages upward
        if (error != null && error.length() > 0) {
            int n = error.length() < b.length ? error.length() : b.length;
            System.arraycopy(error.getBytes(), 0, b, 0, n);
            error = error.substring(n);
            return n;
        }
        if (_in == null) {
            disconnect();
            return -1;
        }
        int n = _in.read(b);
        if (n < 0)
            disconnect();

        return n;
    }

    public void write(byte[] b) throws IOException {
        if (_out == null)
            return;
        if (online == false || isConnected() == false) {
            disconnect();
            return;
        }
        if (((char) b[0]) == '\r' && b.length == 1) {
            b = new byte[2];
            b[0] = '\r';
            b[1] = '\n';
        }
        try {
            _out.write(b);
            _out.flush();
        } catch (IOException e) {
            disconnect();
        }
    }
}
