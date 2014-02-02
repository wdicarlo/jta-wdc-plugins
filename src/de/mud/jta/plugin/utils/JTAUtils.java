/*
 * JTA utilities
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
package de.mud.jta.plugin.utils;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

public class JTAUtils {
    public static MenuElement findMenuElement(final MenuElement root, final String text) {
        for (MenuElement item : root.getSubElements()) {
            if (item instanceof JMenu == true) {
                JMenu mItem = (JMenu) item;
                if (mItem.getSubElements().length > 0) {
                    MenuElement elem = findMenuElement(mItem, text);
                    if (elem != null)
                        return elem;
                }
                continue;
            }
            if (item instanceof JPopupMenu == true) {
                JPopupMenu mItem = (JPopupMenu) item;
                if (mItem.getSubElements().length > 0) {
                    MenuElement elem = findMenuElement(mItem, text);
                    if (elem != null)
                        return elem;
                }
                continue;
            }

            if (item instanceof JMenuItem == false)
                continue;
            JMenuItem mItem = (JMenuItem) item;
            if (mItem.getText().equals(text) == true)
                return mItem;
        }
        return null;
    }

}
