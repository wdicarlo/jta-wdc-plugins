/*
 * JTA plugin to auto login
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

import de.mud.jta.FilterPlugin;
import de.mud.jta.Plugin;
import de.mud.jta.PluginBus;
import de.mud.jta.event.OnlineStatusListener;

/**
 * @author Walter Di Carlo
 */
public class AutoAnswer extends Plugin implements FilterPlugin {

    protected FilterPlugin   source;
    private final static int debug    = 0;
    private boolean          consumed = false;
    private boolean          online   = false;

    /**
     * Create a new telnet plugin.
     */
    public AutoAnswer(final PluginBus bus, String id) {
        super(bus, id);
        // register an online status listener
        bus.registerPluginListener(new OnlineStatusListener() {

            public void online() {
                online = true;
            }

            public void offline() {
                online = false;
            }
        });

    }

    public void setFilterSource(FilterPlugin source) {
        if (debug > 0)
            System.err.println("Telnet: connected to: " + source);
        this.source = source;
    }

    public FilterPlugin getFilterSource() {
        return source;
    }

    public int read(byte[] b) throws IOException {
        int n = source.read(b);

        String msg = convert(b, n);

        if (msg.indexOf("username:") >= 0) {
            String username = System.getProperty("telnet.username");
            if (username != null) {
                this.write(new String(username + "\n").getBytes());
            }
        }
        if (msg.indexOf("password:") >= 0) {
            String password = System.getProperty("telnet.password");
            if (password != null && consumed == false) {
                this.write(new String(password + "\n").getBytes());
                consumed = true;
            }
        }
        return n;
    }

    public void write(byte[] b) throws IOException {
        if (online == true) {
            String msg = convert(b, b.length);
            source.write(b);
        }
    }

    private String convert(byte[] b, int n) {
        String msg = "";
        for (int i = 0; i < n; i++) {
            char c = (char) b[i];
            if (b[i] > 32 && b[i] < 126) {
                msg += Character.toString((char) b[i]);
            }
        }
        return msg;

    }
}
