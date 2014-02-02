/*
 * JTA plugin to add a connection menu
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.mud.jta.Plugin;
import de.mud.jta.PluginBus;
import de.mud.jta.PluginConfig;
import de.mud.jta.VisualPlugin;
import de.mud.jta.event.ConfigurationListener;
import de.mud.jta.event.OnlineStatusListener;
import de.mud.jta.event.SocketRequest;

public class ConnectionMenu extends Plugin implements VisualPlugin {
    private String host;
    private String port;
    private JMenu  conMgm;

    public ConnectionMenu(final PluginBus bus, final String id) {
        super(bus, id);

        // register for config events
        bus.registerPluginListener(new ConfigurationListener() {
            public void setConfiguration(PluginConfig config) {
                //                String conf = config.getProperty("ConnectionMenu", id, "enabled");
                System.out.println("ConnectionMenu: processing config");
            }
        });
        bus.registerPluginListener(new OnlineStatusListener() {

            @Override
            public void online() {
                // once online disable Connect
                System.out.println("ConnectionMenu: processing online event");
                System.out.println(conMgm.getParent().toString());

            }

            @Override
            public void offline() {
                // TODO Auto-generated method stub

            }
        });


        // disable main menu

    }

    @Override
    public JComponent getPluginVisual() {
        return null;
    }

    @Override
    public JMenu getPluginMenu() {
        conMgm = new JMenu("Server");
        JMenuItem tmp;
        conMgm.add(tmp = new JMenuItem("Connect"));
        tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK));
        tmp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String destination = JOptionPane.showInputDialog(null, new JLabel(
                        "Enter your destination host (host[:port])"), "Connect", JOptionPane.QUESTION_MESSAGE);
                if (destination != null) {
                    int sep = 0;
                    if ((sep = destination.indexOf(' ')) > 0 || (sep = destination.indexOf(':')) > 0) {
                        host = destination.substring(0, sep);
                        port = destination.substring(sep + 1);
                        bus.broadcast(new SocketRequest());
                        bus.broadcast(new SocketRequest(host, Integer.parseInt(port)));
                    } else {
                        host = destination;
                        bus.broadcast(new SocketRequest());
                        bus.broadcast(new SocketRequest(host, 23));// TODO: use constant
                    }
                }
            }
        });

        conMgm.add(tmp = new JMenuItem("Disconnect"));
        tmp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bus.broadcast(new SocketRequest());
            }
        });

        return conMgm;
    }

}
