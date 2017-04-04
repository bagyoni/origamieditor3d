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
package origamieditor3d.panel;

import javax.swing.JPanel;

import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Origami;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public abstract class Panel extends JPanel {

    private static final long serialVersionUID = 1L;

    public enum RulerMode {
        Normal, Neusis, Planethrough, Angle_bisector
    }

    protected Origami PanelOrigami;
    protected Camera PanelCamera;
    protected RulerMode rulerMode;
    
    protected Integer tracker_x, tracker_y;
    protected boolean trackerOn;
    protected boolean ready_to_paint;
    
    public abstract void reset();

    public abstract boolean isTracked();
    public abstract void setTracker(Camera refcam, int x, int y);
    public abstract void resetTracker();

    public abstract void rulerOn(Camera refcam, int x1, int y1, int x2, int y2);
    public abstract void rulerOff();

    public abstract void tiltTriangleTo(Camera refcam, Integer... xy);
    public abstract void resetTriangle();
    public abstract void grabTriangleAt(int vertIndex);
    
    public Camera panelCamera() {
        return PanelCamera;
    }
    
    public void update(Origami origami) {

        PanelOrigami = origami;
        ready_to_paint = true;
    }
    
    public void setRulerMode(RulerMode mode) {
        rulerMode = mode;
    }
    
    public void resetZoom() {
        if (PanelOrigami.circumscribedSquareSize() > 0) {
            PanelCamera.setZoom(0.8 * Math.min(getWidth(), getHeight())
                    / PanelOrigami.circumscribedSquareSize());
        }
    }
}
