// This file is part of Origami Editor 3D.
// Copyright (C) 2013 Bágyoni Attila <bagyoni.attila@gmail.com>
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

import origamieditor3d.origami.OrigamiTracker;
import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Origami;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * @author @author Attila Bágyoni <bagyoni.attila@gmail.com>
 */
public class OrigamiPanel extends JPanel implements BasicEditing {

    private static final long serialVersionUID = 1L;

    public OrigamiPanel() {

        super();
        ready_to_paint = false;
        PanelCamera = new Camera(0, 0, 1);
        linerOn = false;
        tracker_x = null;
        tracker_y = null;
        trackerOn = false;
        liner_triangle = new double[3][];
        liner_triangle[0] = null;
        liner_triangle[1] = null;
        liner_triangle[2] = null;
        liner_grab_index = 0;
        alignment_point = null;
        alignment_radius = 0;
        paper_front_color = 0xFFFFFF;
        neusisOn = false;
        previewOn = false;
        displaymode = DisplayMode.SIMA;
        beacons = null;
    }
    private Origami PanelOrigami;
    protected Camera PanelCamera;
    private boolean ready_to_paint;
    private Integer liner_x1, liner_y1, liner_x2, liner_y2;
    private boolean linerOn;
    private Integer tracker_x, tracker_y;
    private boolean trackerOn;
    private double[] tracker_im;
    final private double[][] liner_triangle;
    private int liner_grab_index;
    private int[] alignment_point;
    private int alignment_radius;
    private int paper_front_color;
    private boolean neusisOn;
    private boolean previewOn;
    private DisplayMode displaymode;
    private double[][] beacons;
    private Integer protractor_angle;

    public enum DisplayMode {

        SIMA, UV, SEMMI
    }

    @Override
    public boolean isTracked() {
        return trackerOn;
    }

    @Override
    public void grabLinerAt(int vertIndex) {
        liner_grab_index = vertIndex;
    }

    @Override
    public void update(Origami origami) {

        PanelOrigami = origami;
        if (displaymode == DisplayMode.UV) {
            PanelCamera.updateBuffer(PanelOrigami);
        }
        ready_to_paint = true;
    }

    public void linerOn(int x1, int y1, int x2, int y2) {

        liner_x1 = x1;
        liner_y1 = y1;
        liner_x2 = x2;
        liner_y2 = y2;
        linerOn = true;
    }

    public void linerOff() {
        linerOn = false;
    }

    public void neusisOn() {
        neusisOn = true;
    }

    public void neusisOff() {
        neusisOn = false;
    }

    public void previewOn() {
        previewOn = true;
    }

    public void previewOff() {
        previewOn = false;
    }

    @Override
    public void setTracker(Camera refkamera, int x, int y) {

        tracker_x = x;
        tracker_y = y;
        try {
            tracker_im = new OrigamiTracker(
                    PanelOrigami,
                    new double[]{
                        ((double) tracker_x - refkamera.xshift + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom()).projection0(refkamera.camera_pos)[0]) / refkamera.zoom(),
                        ((double) tracker_y - refkamera.yshift + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom()).projection0(refkamera.camera_pos)[1]) / refkamera.zoom()
                    }).trackPoint();
        } catch (Exception ex) {
        }
        trackerOn = true;
    }

    @Override
    public void tiltLinerTo(Camera refkamera, Integer... xy) {

        try {
            int x = xy[0];
            int y = xy[1];
            liner_triangle[liner_grab_index] = new OrigamiTracker(
                    PanelOrigami,
                    new double[]{
                        ((double) x - refkamera.xshift + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom()).projection0(refkamera.camera_pos)[0]) / refkamera.zoom(),
                        ((double) y - refkamera.yshift + new Camera(refkamera.xshift, refkamera.yshift, refkamera.zoom()).projection0(refkamera.camera_pos)[1]) / refkamera.zoom()
                    }).trackPoint();
        } catch (Exception ex) {
            liner_triangle[liner_grab_index] = null;
        }
        liner_grab_index++;
        liner_grab_index %= 3;
    }

    public void setAlignmentPoint(int... point) {
        alignment_point = point;
    }

    public void resetAlignmentPoint() {
        alignment_point = null;
    }

    public void setAlignmentRadius(int radiusSQ) {
        alignment_radius = (int) Math.max(Math.sqrt(radiusSQ), 5);
    }

    public void setBeacons(double[]... points2d) {
        beacons = new double[points2d.length][];
        for (int i = 0; i < points2d.length; i++) {
            try {
                beacons[i] = new OrigamiTracker(PanelOrigami, points2d[i]).trackPoint();
            } catch (Exception ex) {
                beacons = null;
                break;
            }
        }
    }

    public void resetBeacons() {
        beacons = null;
    }
    
    public void displayProtractor(int angle) {
        protractor_angle = angle;
    }
    
    public void hideProtractor() {
        protractor_angle = null;
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
        alignment_point = null;
        protractor_angle = null;
    }

    public void colorFront(int rgb) {
        paper_front_color = rgb;
    }

    public void setDisplaymode(DisplayMode value) {
        displaymode = value;
    }

    public DisplayMode displaymode() {
        return displaymode;
    }

    public void setTexture(java.awt.image.BufferedImage tex) {
        PanelCamera.texture = tex;
    }

    public boolean validateClickOnBeacon(int x, int y) {

        try {
            int dx = (int) (PanelCamera.projection(beacons[0])[0]) + PanelCamera.xshift - x;
            int dy = (int) (PanelCamera.projection(beacons[0])[1]) + PanelCamera.yshift - y;
            return dx * dx + dy * dy < alignment_radius * alignment_radius;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean validateBeaconOverlap() {

        try {
            double dx = (PanelCamera.projection(beacons[0])[0]) - (PanelCamera.projection(beacons[1])[0]);
            double dy = (PanelCamera.projection(beacons[0])[1]) - (PanelCamera.projection(beacons[1])[1]);
            return dx * dx + dy * dy < 50;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (ready_to_paint) {

            switch (displaymode) {

                case UV:
                    PanelCamera.drawTexture(g, this.getWidth(), this.getHeight());
                    break;

                case SIMA:
                    PanelCamera.drawFaces(g, paper_front_color, PanelOrigami);
                    PanelCamera.drawEdges(g, Color.black, PanelOrigami);
                    break;

                case SEMMI:
                    PanelCamera.drawEdges(g, Color.black, PanelOrigami);
                    break;
            }
        }
        if (alignment_point != null) {

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawOval(alignment_point[0] - alignment_radius, alignment_point[1] - alignment_radius, alignment_radius * 2, alignment_radius * 2);
            g2.setStroke(new java.awt.BasicStroke(1));
        }
        /*
        if (beacons != null) {
            g.setColor(Color.red);
            for (int i = 0; i < beacons.length; i++) {
                int x = (int) (PanelCamera.projection(beacons[i])[0]) + PanelCamera.xshift;
                int y = (int) (PanelCamera.projection(beacons[i])[1]) + PanelCamera.yshift;
                g.fillOval(x - alignment_radius, y - alignment_radius, alignment_radius * 2, alignment_radius * 2);
            }
        }*/

        g.setColor(Color.red);
        if (linerOn) {

            if (previewOn) {

                double pontX = ((double) liner_x2 - this.PanelCamera.xshift) / this.PanelCamera.zoom();
                double pontY = ((double) liner_y2 - this.PanelCamera.yshift) / this.PanelCamera.zoom();
                double pont1X = ((double) liner_x1 - this.PanelCamera.xshift) / this.PanelCamera.zoom();
                double pont1Y = ((double) liner_y1 - this.PanelCamera.yshift) / this.PanelCamera.zoom();

                double[] vonalzoNV = new double[]{
                    this.PanelCamera.axis_x[0] * (liner_y2 - liner_y1) + this.PanelCamera.axis_y[0] * (liner_x1 - liner_x2),
                    this.PanelCamera.axis_x[1] * (liner_y2 - liner_y1) + this.PanelCamera.axis_y[1] * (liner_x1 - liner_x2),
                    this.PanelCamera.axis_x[2] * (liner_y2 - liner_y1) + this.PanelCamera.axis_y[2] * (liner_x1 - liner_x2)
                };
                double[] vonalzoPT = new double[]{
                    this.PanelCamera.axis_x[0] / this.PanelCamera.zoom() * pontX + this.PanelCamera.axis_y[0] / this.PanelCamera.zoom() * pontY + this.PanelCamera.camera_pos[0],
                    this.PanelCamera.axis_x[1] / this.PanelCamera.zoom() * pontX + this.PanelCamera.axis_y[1] / this.PanelCamera.zoom() * pontY + this.PanelCamera.camera_pos[1],
                    this.PanelCamera.axis_x[2] / this.PanelCamera.zoom() * pontX + this.PanelCamera.axis_y[2] / this.PanelCamera.zoom() * pontY + this.PanelCamera.camera_pos[2]
                };
                double[] vonalzoPT1 = new double[]{
                    this.PanelCamera.axis_x[0] / this.PanelCamera.zoom() * pont1X + this.PanelCamera.axis_y[0] / this.PanelCamera.zoom() * pont1Y + this.PanelCamera.camera_pos[0],
                    this.PanelCamera.axis_x[1] / this.PanelCamera.zoom() * pont1X + this.PanelCamera.axis_y[1] / this.PanelCamera.zoom() * pont1Y + this.PanelCamera.camera_pos[1],
                    this.PanelCamera.axis_x[2] / this.PanelCamera.zoom() * pont1X + this.PanelCamera.axis_y[2] / this.PanelCamera.zoom() * pont1Y + this.PanelCamera.camera_pos[2]
                };
                if (neusisOn) {
                    vonalzoNV = Origami.vector(vonalzoPT, vonalzoPT1);
                }

                PanelCamera.drawPreview(g, Color.green, PanelOrigami, vonalzoPT, vonalzoNV);
            }

            g.setColor(Color.red);
            if (neusisOn) {
                int maxdim = Math.max(this.getWidth(), this.getHeight());
                g.drawLine(liner_x2 + maxdim * (liner_y1 - liner_y2), liner_y2 + maxdim * (liner_x2 - liner_x1),
                        liner_x2 - maxdim * (liner_y1 - liner_y2), liner_y2 - maxdim * (liner_x2 - liner_x1));
            } else {
                int maxdim = Math.max(this.getWidth(), this.getHeight());
                g.drawLine(liner_x1 + maxdim * (liner_x1 - liner_x2), liner_y1 + maxdim * (liner_y1 - liner_y2),
                        liner_x2 - maxdim * (liner_x1 - liner_x2), liner_y2 - maxdim * (liner_y1 - liner_y2));
            }
        }
        if (trackerOn) {
            int x = (int) (PanelCamera.projection(tracker_im)[0]) + PanelCamera.xshift;
            int y = (int) (PanelCamera.projection(tracker_im)[1]) + PanelCamera.yshift;
            g.drawLine(x - 5, y, x + 5, y);
            g.drawLine(x, y - 5, x, y + 5);
        }
        g.setColor(Color.magenta);
        if (liner_triangle[0] != null) {
            int x = (int) (PanelCamera.projection(liner_triangle[0])[0]) + PanelCamera.xshift;
            int y = (int) (PanelCamera.projection(liner_triangle[0])[1]) + PanelCamera.yshift;
            g.drawLine(x - 3, y + 3, x + 3, y - 3);
            g.drawLine(x - 3, y - 3, x + 3, y + 3);
        }
        if (liner_triangle[1] != null) {
            int x = (int) (PanelCamera.projection(liner_triangle[1])[0]) + PanelCamera.xshift;
            int y = (int) (PanelCamera.projection(liner_triangle[1])[1]) + PanelCamera.yshift;
            g.drawLine(x - 3, y + 3, x + 3, y - 3);
            g.drawLine(x - 3, y - 3, x + 3, y + 3);
        }
        if (liner_triangle[2] != null) {
            int x = (int) (PanelCamera.projection(liner_triangle[2])[0]) + PanelCamera.xshift;
            int y = (int) (PanelCamera.projection(liner_triangle[2])[1]) + PanelCamera.yshift;
            g.drawLine(x - 3, y + 3, x + 3, y - 3);
            g.drawLine(x - 3, y - 3, x + 3, y + 3);
        }
        if (protractor_angle != null) {
            drawProtractor(g, protractor_angle);
        }
    }
    
    private void drawProtractor(Graphics g, int angle) {
        
        angle -= 90;
        while (angle < 0) {
            angle += 360;
        }
        
        int width = getWidth();
        int height = getHeight();
        int diam = Math.min(width, height)/2;
        g.setColor(new Color(255, 255, 255, 170));
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.red);
        g.drawLine(
                (int)(width/2 + Math.cos(angle*Math.PI/180)*diam/2),
                (int)(height/2 + Math.sin(angle*Math.PI/180)*diam/2),
                (int)(width/2),
                (int)(height/2)
        );
        
        g.setColor(Color.black);
        g.drawOval((width-diam)/2, (height-diam)/2, diam, diam);
        
        for (int i=0; i<360; i+=5) {
            
            int notch = i % 180 == 0 ? diam/6 : i % 90 == 0 ? diam/8 : i % 45 == 0 ? diam/10 : diam/14;
            g.drawLine(
                    (int)(width/2 + Math.cos(i*Math.PI/180)*diam/2),
                    (int)(height/2 + Math.sin(i*Math.PI/180)*diam/2),
                    (int)(width/2 + Math.cos(i*Math.PI/180)*(diam/2-notch)),
                    (int)(height/2 + Math.sin(i*Math.PI/180)*(diam/2-notch))
            );
            
        }
        g.setFont(g.getFont().deriveFont(12.0f));
        g.drawString("0°", width/2-5, (height-diam)/2-5);
        g.drawString("90°", (width+diam)/2+5, height/2+5);
        g.drawString("180°", width/2-10, (height+diam)/2+15);
        g.drawString("-90°", (width-diam)/2-30, height/2+5);

        if (angle % 90 != 0) {
            g.drawString(
                    (angle < 90 ? angle + 90 : angle - 270) + "°", 
                    (int)(width/2 + Math.cos(angle*Math.PI/180)*diam/2 + (angle > 90 && angle < 270 ? -30 : 10)),
                    (int)(height/2 + Math.sin(angle*Math.PI/180)*diam/2) + (angle < 180 ? 15 : -5));
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
