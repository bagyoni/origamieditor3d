/*
 * This file is part of Origami Editor 3D.
 * Copyright (C) 2013, 2014, 2015 Bágyoni Attila <ba-sz-at@users.sourceforge.net>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package origamieditor3d.resources;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class Instructor {
    
    static private java.util.ResourceBundle messages;
    
    static {
        setLocale(new java.util.Locale(System.getProperty("user.language"), System.getProperty("user.country")));
    }
    
    static public String getString(String key, Object... obj) {
        try {
            return String.format(messages.getString(key), obj);
        } catch (Exception ex) {
            return key;
        }
    }
    
    static public void setLocale(java.util.Locale locale) {
        if (new Instructor().getClass().getResource("/diagram" + "_" + locale.getLanguage() + ".properties") != null) {
            messages = java.util.ResourceBundle.getBundle("diagram", locale);
            System.out.println("Diagramming language set to " + locale.getDisplayName(java.util.Locale.ENGLISH));
        } else {
            messages = java.util.ResourceBundle.getBundle("diagram", new java.util.Locale("en", "US"));
            System.out.println("Could not set diagramming language to " + locale.getDisplayName(java.util.Locale.ENGLISH));
        }
    }
}
