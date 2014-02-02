/*
 * JTA plugin to customize the help menu
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.MenuElement;

import de.mud.jta.Plugin;
import de.mud.jta.PluginBus;
import de.mud.jta.VisualPlugin;
import de.mud.jta.event.OnlineStatusListener;
import de.mud.jta.plugin.utils.JTAUtils;

/**
 * @author Walter Di Carlo
 *
 */
public class CustomHelp extends Plugin implements VisualPlugin {

    private JMenu dummyMenu;

    public CustomHelp(final PluginBus bus, final String id) {
        super(bus, id);

        bus.registerPluginListener(new OnlineStatusListener() {

            @Override
            public void online() {
                // once online replace the online help
                customizeHelp();
            }

            @Override
            public void offline() {}
        });
    }

    @Override
    public JComponent getPluginVisual() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JMenu getPluginMenu() {
        // return a dummy menu
        dummyMenu = new JMenu("");

        return dummyMenu;
    }

    private void customizeHelp() {
        if (dummyMenu.getParent() instanceof JMenuBar == false)
            return;
        JMenuBar menuBar = (JMenuBar) dummyMenu.getParent();

        MenuElement item = JTAUtils.findMenuElement(menuBar, "General");
        if (item instanceof JMenuItem == true) {
            JMenuItem mItem = (JMenuItem) item;
            //mItem.setEnabled(false);
            installListener(mItem);
        }
    }

    private void installListener(final JMenuItem mItem) {
        // remove current listeners
        for (ActionListener listener : mItem.getListeners(ActionListener.class)) {
            mItem.removeActionListener(listener);
        }
        // add custom help action listener
        mItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(
                        mItem.getParent(),
                        "JTA - Telnet/SSH for the JAVA(tm) platform\r\n(c) 1996-2005 Matthias L. Jugel, Marcus Meißner\r\n\r\n",
                        "jta", JOptionPane.INFORMATION_MESSAGE);

            }
        });
    }

}
