// This file is part of Origami Editor 3D.
// Copyright (C) 2013, 2014, 2015 Bágyoni Attila <ba-sz-at@users.sourceforge.net>
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
package origamieditor3d.origami;

import origamieditor3d.resources.Dictionary;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public OrigamiException(String ex) {
        super(ex);
    }
    final static public OrigamiException H001 = new OrigamiException(Dictionary.getString("h001"));
    final static public OrigamiException H002 = new OrigamiException(Dictionary.getString("h002"));
    final static public OrigamiException H003 = new OrigamiException(Dictionary.getString("h003"));
    final static public OrigamiException H004 = new OrigamiException(Dictionary.getString("h004"));
    final static public OrigamiException H005 = new OrigamiException(Dictionary.getString("h005"));
    final static public OrigamiException H006 = new OrigamiException(Dictionary.getString("h006"));
    final static public OrigamiException H007 = new OrigamiException(Dictionary.getString("h007"));
    final static public OrigamiException H008 = new OrigamiException(Dictionary.getString("h008"));
    final static public OrigamiException H009 = new OrigamiException(Dictionary.getString("h009"));
    final static public OrigamiException H010 = new OrigamiException(Dictionary.getString("h010"));
    final static public OrigamiException H011 = new OrigamiException(Dictionary.getString("h011"));
    final static public OrigamiException H012 = new OrigamiException(Dictionary.getString("h012"));
    final static public OrigamiException H013 = new OrigamiException(Dictionary.getString("h013"));
}
