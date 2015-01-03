// This file is part of Origami Editor 3D.
// Copyright (C) 2013, 2014, 2015 Bágyoni Attila <bagyoni.attila@gmail.com>
// Origami Editor 3D is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http:// www.gnu.org/licenses/>.
package origamieditor3d.resources;

/**
 *
 * @author Attila Bágyoni <bagyoni.attila@gmail.com>
 */
public class Dictionary {
    
    
    static private java.util.ResourceBundle messages;
    static {
        try {
            messages = java.util.ResourceBundle.getBundle("language", new java.util.Locale(System.getProperty("user.language"), System.getProperty("user.country")));
        } catch (Exception ex) {
            messages = java.util.ResourceBundle.getBundle("language", new java.util.Locale("en", "US"));
        }
    }
    
    public static String getString(String key) {
        try {
            return messages.getString(key);
        } catch (Exception ex) {
            return key;
        }
    }
}
