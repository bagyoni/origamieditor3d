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
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class PaperPanel extends JPanel implements BasicEditing {

    private static final long serialVersionUID = 1L;
    private Origami PanelOrigami;
    protected Camera PanelCamera;
    private Integer tracker_x, tracker_y;
    private double[] tracker_im;
    private boolean ready_to_paint;
    private boolean trackerOn;
    private Integer[][] liner_triangle;
    private int liner_grab_index;

    public Integer tracker_x() {
        return tracker_x;
    }
    public Integer tracker_y() {
        return tracker_y;
    }
    @Override
    public boolean isTracked() {
        return trackerOn;
    }
    public Integer[][] linerTriangle() {
        return liner_triangle;
    }
    @Override
    public void grabLinerAt(int vertIndex) {
        liner_grab_index = vertIndex;
    }

    public PaperPanel() {

        super();
        PanelCamera = new Camera(0, 0, 1);
        ready_to_paint = false;
        tracker_x = null;
        tracker_y = null;
        trackerOn = false;
        liner_triangle = new Integer[3][];
        liner_triangle[0] = null;
        liner_triangle[1] = null;
        liner_triangle[2] = null;
        liner_grab_index = 0;
    }

    @Override
    public void update(Origami origami) {

        PanelOrigami = origami;
        ready_to_paint = true;
    }

    @Override
    public void setTracker(Camera refkamera, int x, int y) {

        tracker_x = x;
        tracker_y = y;
        tracker_im = new double[] {
            ((double) tracker_x - refkamera.xshift + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom()).projection0(refkamera.camera_pos)[0]) / refkamera.zoom(),
            ((double) tracker_y - refkamera.yshift + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom()).projection0(refkamera.camera_pos)[1]) / refkamera.zoom(),
            0};
        trackerOn = true;
    }

    @Override
    public void tiltLinerTo(Camera refkamera, Integer... xy) {

        liner_triangle[liner_grab_index] = xy;
        liner_grab_index ++;
        liner_grab_index %= 3;
    }

    @Override
    public void reset() {

        tracker_x = null;
        tracker_y = null;
        liner_grab_index = 0;
        liner_triangle[0] = null;
        liner_triangle[1] = null;
        liner_triangle[2] = null;
        trackerOn = false;
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (ready_to_paint) try {
            PanelCamera.drawCreasePattern(g, Color.black, PanelOrigami);
        } catch (Exception ex) {}
        g.setColor(Color.red);
        if (trackerOn) {
            int x = (int) (PanelCamera.projection(tracker_im)[0]) + PanelCamera.xshift;
            int y = (int) (PanelCamera.projection(tracker_im)[1]) + PanelCamera.yshift;
            g.drawLine(x - 5, y, x + 5, y);
            g.drawLine(x, y - 5, x, y + 5);
        }
        g.setColor(Color.magenta);
        if (liner_triangle[0] != null) {
            g.drawLine(liner_triangle[0][0]-3, liner_triangle[0][1]-3, liner_triangle[0][0]+3, liner_triangle[0][1]+3);
            g.drawLine(liner_triangle[0][0]-3, liner_triangle[0][1]+3, liner_triangle[0][0]+3, liner_triangle[0][1]-3);
        }
        if (liner_triangle[1] != null) {
            g.drawLine(liner_triangle[1][0]-3, liner_triangle[1][1]-3, liner_triangle[1][0]+3, liner_triangle[1][1]+3);
            g.drawLine(liner_triangle[1][0]-3, liner_triangle[1][1]+3, liner_triangle[1][0]+3, liner_triangle[1][1]-3);
        }
        if (liner_triangle[2] != null) {
            g.drawLine(liner_triangle[2][0]-3, liner_triangle[2][1]-3, liner_triangle[2][0]+3, liner_triangle[2][1]+3);
            g.drawLine(liner_triangle[2][0]-3, liner_triangle[2][1]+3, liner_triangle[2][0]+3, liner_triangle[2][1]-3);
        }
    }

    @Override
    public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent e) {

        java.awt.Point pt = e.getPoint();
        pt.y += java.awt.Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).height/2;
        pt.x += java.awt.Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).width/2;
        return pt;
    }
}
