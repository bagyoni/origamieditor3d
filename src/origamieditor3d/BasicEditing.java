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
package origamieditor3d;

import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Origami;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public interface BasicEditing {

    public enum RulerMode {
        Normal, Neusis, Planethrough, Angle_bisector
    }

    public void update(Origami origami);
    public void reset();

    public boolean isTracked();
    public void setTracker(Camera refcam, int x, int y);
    public void resetTracker();

    public void setRulerMode(RulerMode mode);
    public void rulerOn(Camera refcam, int x1, int y1, int x2, int y2);
    public void rulerOff();

    public void tiltTriangleTo(Camera refcam, Integer... xy);
    public void resetTriangle();
    public void grabTriangleAt(int vertIndex);
}
