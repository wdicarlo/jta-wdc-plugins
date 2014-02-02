/*
 * JTA plugin to disable the connection menu
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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import de.mud.jta.Plugin;
import de.mud.jta.PluginBus;
import de.mud.jta.VisualPlugin;
import de.mud.jta.event.OnlineStatusListener;
import de.mud.jta.event.SocketRequest;
import de.mud.jta.plugin.utils.JTAUtils;

/**
 * @author Walter Di Carlo
 *
 */
public class DisableMainConnectionMenu extends Plugin implements VisualPlugin {
    private JMenu  dummyMenuItem;

    /**
     * Plugin constructor
     * @param bus
     * @param id
     */
    public DisableMainConnectionMenu(final PluginBus bus, final String id) {
        super(bus, id);

        bus.registerPluginListener(new OnlineStatusListener() {

            @Override
            public void online() {
                // once online disable main connection menu items
                disableMainConnectionMenus();
            }

            @Override
            public void offline() {
            }
        });
    }

    @Override
    public JComponent getPluginVisual() {
        return null;
    }

    @Override
    public JMenu getPluginMenu() {
        // return a dummy menu
        dummyMenuItem = new JMenu("");

        return dummyMenuItem;
    }

    private void disableMainConnectionMenus() {
        if (dummyMenuItem.getParent() instanceof JMenuBar == false)
            return;
        JMenuBar menuBar = (JMenuBar) dummyMenuItem.getParent();

        MenuElement item = JTAUtils.findMenuElement(menuBar, "Connect");
        if (item instanceof JMenuItem == true) {
            JMenuItem mItem = (JMenuItem) item;
            mItem.setEnabled(false);
        }
        item = JTAUtils.findMenuElement(menuBar, "Disconnect");
        if (item instanceof JMenuItem == true) {
            JMenuItem mItem = (JMenuItem) item;
            mItem.setEnabled(false);
        }
        
//begin add @sp 12.05.11 disconnect on exit
        item = JTAUtils.findMenuElement(menuBar, "Exit");
        if (item instanceof JMenuItem == true) {
            JMenuItem mItem = (JMenuItem) item;
            ActionListener[] listeners = mItem.getActionListeners();
            for (int i=0; i<listeners.length; i++){
            	mItem.removeActionListener(listeners[i]);
            }
            mItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                	 JMenuBar menuBar = (JMenuBar) dummyMenuItem.getParent();
                	 JFrame frame = ((JFrame)menuBar.getParent().getParent().getParent());
                	 DisableMainConnectionMenu.this.bus.broadcast(new SocketRequest());
                     frame.setVisible(false);
                     frame.dispose();
                     System.exit(0);
                }
              });
        }
//end add @sp 12.05.11 disconnect on exit
        
        // TODO: remove the dummy menu
    }

}
