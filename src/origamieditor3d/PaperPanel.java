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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Geometry;
import origamieditor3d.origami.Origami;
import origamieditor3d.origami.OrigamiTracker;

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
    private double[] liner_point, liner_normal;
    private Integer[][] liner_triangle;
    private int liner_grab_index;
    private LinerMode linerMode;

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
    public void grabTriangleAt(int vertIndex) {
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
        liner_point = null;
        liner_normal = null;
        linerMode = LinerMode.Normal;
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
        tracker_im = new double[]{
            ((double) tracker_x - refkamera.xshift
                    + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom())
                    .projection0(refkamera.camera_pos())[0]) / refkamera.zoom(),
            ((double) tracker_y - refkamera.yshift
                    + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom())
                    .projection0(refkamera.camera_pos())[1]) / refkamera.zoom(),
            0};
        trackerOn = true;
    }

    @Override
    public void resetTracker() {

        tracker_x = null;
        tracker_y = null;
        trackerOn = false;
    }

    @Override
    public void setLinerMode(LinerMode mode) {
        linerMode = mode;
    }

    @Override
    public void linerOn(Camera refcam, int x1, int y1, int x2, int y2) {

        double pontX = ((double) x2 - refcam.xshift) / refcam.zoom();
        double pontY = ((double) y2 - refcam.yshift) / refcam.zoom();
        double pont1X = ((double) x1 - refcam.xshift) / refcam.zoom();
        double pont1Y = ((double) y1 - refcam.yshift) / refcam.zoom();

        double[] vonalzoNV = new double[]{
            refcam.axis_x()[0] * (y2 - y1) + refcam.axis_y()[0] * (x1 - x2),
            refcam.axis_x()[1] * (y2 - y1) + refcam.axis_y()[1] * (x1 - x2),
            refcam.axis_x()[2] * (y2 - y1) + refcam.axis_y()[2] * (x1 - x2)
        };
        double[] vonalzoPT = new double[]{
            refcam.axis_x()[0] / refcam.zoom() * pontX + refcam.axis_y()[0] / refcam.zoom() * pontY + refcam.camera_pos()[0],
            refcam.axis_x()[1] / refcam.zoom() * pontX + refcam.axis_y()[1] / refcam.zoom() * pontY + refcam.camera_pos()[1],
            refcam.axis_x()[2] / refcam.zoom() * pontX + refcam.axis_y()[2] / refcam.zoom() * pontY + refcam.camera_pos()[2]
        };
        double[] vonalzoPT1 = new double[]{
            refcam.axis_x()[0] / refcam.zoom() * pont1X + refcam.axis_y()[0] / refcam.zoom() * pont1Y + refcam.camera_pos()[0],
            refcam.axis_x()[1] / refcam.zoom() * pont1X + refcam.axis_y()[1] / refcam.zoom() * pont1Y + refcam.camera_pos()[1],
            refcam.axis_x()[2] / refcam.zoom() * pont1X + refcam.axis_y()[2] / refcam.zoom() * pont1Y + refcam.camera_pos()[2]
        };
        if (linerMode == LinerMode.Neusis) {
            vonalzoNV = Geometry.vector(vonalzoPT, vonalzoPT1);
        }
        liner_point = vonalzoPT;
        liner_normal = vonalzoNV;
    }

    @Override
    public void linerOff() {
        liner_point = (liner_normal = null);
    }

    @Override
    public void tiltTriangleTo(Camera refkamera, Integer... xy) {

        liner_triangle[liner_grab_index] = xy;
        liner_grab_index++;
        liner_grab_index %= 3;

        if (liner_triangle[0] != null && liner_triangle[1] != null && liner_triangle[2] != null) {

            try {

                double[] pt1 = new OrigamiTracker(
                        PanelOrigami,
                        new double[]{
                            ((double) liner_triangle[0][0] - PanelCamera.xshift + PanelCamera.projection0(PanelCamera.camera_pos())[0]) / PanelCamera.zoom(),
                            ((double) liner_triangle[0][1] - PanelCamera.yshift + PanelCamera.projection0(PanelCamera.camera_pos())[1]) / PanelCamera.zoom()
                        }).trackPoint(),
                        pt2 = new OrigamiTracker(
                                PanelOrigami,
                                new double[]{
                                    ((double) liner_triangle[1][0] - PanelCamera.xshift + PanelCamera.projection0(PanelCamera.camera_pos())[0]) / PanelCamera.zoom(),
                                    ((double) liner_triangle[1][1] - PanelCamera.yshift + PanelCamera.projection0(PanelCamera.camera_pos())[1]) / PanelCamera.zoom()
                                }).trackPoint(),
                        pt3 = new OrigamiTracker(
                                PanelOrigami,
                                new double[]{
                                    ((double) liner_triangle[2][0] - PanelCamera.xshift + PanelCamera.projection0(PanelCamera.camera_pos())[0]) / PanelCamera.zoom(),
                                    ((double) liner_triangle[2][1] - PanelCamera.yshift + PanelCamera.projection0(PanelCamera.camera_pos())[1]) / PanelCamera.zoom()
                                }).trackPoint();

                if (linerMode == LinerMode.Planethrough) {
                    if (Geometry.vector_length(Geometry.vector_product(
                            Geometry.vector(pt2, pt1), Geometry.vector(pt3, pt1))) != 0d) {

                        liner_point = pt1;
                        liner_normal = Geometry.vector_product(Geometry.vector(pt2, pt1),
                                Geometry.vector(pt3, pt1));
                    } else {
                        linerOff();
                    }
                } else if (linerMode == LinerMode.Angle_bisector) {
                    liner_point = pt2;
                    liner_normal = Geometry.vector(
                            Geometry.length_to_100(Geometry.vector(pt1, pt2)),
                            Geometry.length_to_100(Geometry.vector(pt3, pt2)));
                    if (Geometry.vector_length(liner_normal) == 0.) {
                        linerOff();
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void resetTriangle() {

        liner_grab_index = 0;
        liner_triangle[0] = null;
        liner_triangle[1] = null;
        liner_triangle[2] = null;
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
        if (ready_to_paint) {
            try {
                PanelCamera.drawCreasePattern(g, Color.black, PanelOrigami);
            } catch (Exception ex) {
            }
        }
        g.setColor(Color.red);
        if (trackerOn) {
            int x = (int) (PanelCamera.projection(tracker_im)[0]) + PanelCamera.xshift;
            int y = (int) (PanelCamera.projection(tracker_im)[1]) + PanelCamera.yshift;
            g.drawLine(x - 5, y, x + 5, y);
            g.drawLine(x, y - 5, x, y + 5);
        }

        if (liner_point != null) {
            PanelCamera.draw2dFoldingLine(g, Color.red, liner_point, liner_normal, PanelOrigami);
        }

        g.setColor(Color.magenta);
        ((java.awt.Graphics2D)g).setStroke(new java.awt.BasicStroke(2));

        if (liner_triangle[0] != null) {
            g.drawLine(liner_triangle[0][0] - 3, liner_triangle[0][1] - 3, liner_triangle[0][0] + 3, liner_triangle[0][1] + 3);
            g.drawLine(liner_triangle[0][0] - 3, liner_triangle[0][1] + 3, liner_triangle[0][0] + 3, liner_triangle[0][1] - 3);
        }
        if (liner_triangle[1] != null) {
            g.drawLine(liner_triangle[1][0] - 3, liner_triangle[1][1] - 3, liner_triangle[1][0] + 3, liner_triangle[1][1] + 3);
            g.drawLine(liner_triangle[1][0] - 3, liner_triangle[1][1] + 3, liner_triangle[1][0] + 3, liner_triangle[1][1] - 3);
        }
        if (liner_triangle[2] != null) {
            g.drawLine(liner_triangle[2][0] - 3, liner_triangle[2][1] - 3, liner_triangle[2][0] + 3, liner_triangle[2][1] + 3);
            g.drawLine(liner_triangle[2][0] - 3, liner_triangle[2][1] + 3, liner_triangle[2][0] + 3, liner_triangle[2][1] - 3);
        }
    }

    @Override
    public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent e) {

        java.awt.Point pt = e.getPoint();
        pt.y += java.awt.Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).height / 2;
        pt.x += java.awt.Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).width / 2;
        return pt;
    }
}
