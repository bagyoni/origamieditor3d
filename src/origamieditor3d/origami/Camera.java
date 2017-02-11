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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 * @see Origami
 */
public class Camera {

    final static public int paper_back_color = 0xF7D6A6;
    final static public int paper_front_color = 0x000097;
    final static public int maximal_zoom = 4;

    public Camera(int x, int y, double zoom) {

        camera_pos = default_camera_pos.clone();
        camera_dir = default_camera_dir.clone();
        axis_x = default_axis_x.clone();
        axis_y = default_axis_y.clone();
        xshift = x;
        yshift = y;
        this.zoom = zoom;
    }

    protected double[] camera_pos;
    protected double[] camera_dir;
    protected double[] axis_x;
    protected double[] axis_y;
    static private double[] default_camera_pos = {200, 200, 0};
    static private double[] default_camera_dir = {0, 0, 1};
    static private double[] default_axis_x = {1, 0, 0};
    static private double[] default_axis_y = {0, 1, 0};
    public int xshift = 230;
    public int yshift = 230;
    private double zoom = 1.0;
    private double[][] space_buffer;
    public java.awt.image.BufferedImage texture;

    public double[] camera_pos() {
        return camera_pos;
    }

    public double[] camera_dir() {
        return camera_dir;
    }

    public double[] axis_x() {
        return Geometry.scalar_multip(axis_x, zoom);
    }

    public double[] axis_y() {
        return Geometry.scalar_multip(axis_y, zoom);
    }

    public double zoom() {
        return zoom;
    }

    public void setZoom(double value) {
        zoom = value;
    }

    protected byte orientation = 0;

    public double[] projection0(double[] point) {

        double[] basepoint = Geometry.line_plane_intersection(
                point, camera_dir, camera_pos, Geometry.scalar_multip(camera_dir, zoom));

        double[] img = {basepoint[0] * axis_x[0] * zoom + basepoint[1] * axis_x[1] * zoom + basepoint[2] * axis_x[2] * zoom,
            basepoint[0] * axis_y[0] * zoom + basepoint[1] * axis_y[1] * zoom + basepoint[2] * axis_y[2] * zoom};
        return img;
    }

    public double[] projection(double[] point) {

        double[] img = {projection0(point)[0] - projection0(camera_pos)[0],
                projection0(point)[1] - projection0(camera_pos)[1]};
        return img;
    }

    public void rotate(float x, float y) {

        double sinX = Math.sin(x * Math.PI / 180);
        double cosX = Math.cos(x * Math.PI / 180);

        camera_dir = Geometry.rotation(camera_dir, Geometry.nullvector, axis_y, sinX, cosX);
        axis_x = Geometry.length_to_1(Geometry.rotation(axis_x, Geometry.nullvector, axis_y, sinX, cosX));

        double sinY = Math.sin(y * Math.PI / 180);
        double cosY = Math.cos(y * Math.PI / 180);

        camera_dir = Geometry.rotation(camera_dir, Geometry.nullvector, axis_x, sinY, cosY);
        axis_y = Geometry.length_to_1(Geometry.rotation(axis_y, Geometry.nullvector, axis_x, sinY, cosY));
    }

    public java.util.List<int[]> alignmentPoints(Origami origami, int... denoms) {

        java.util.List<int[]> nsectors = new ArrayList<>();
        for (int i = 0; i < origami.vertices_size(); i++) {

            nsectors.add(new int[]{(int) projection(origami.vertices().get(i))[0],
                (int) projection(origami.vertices().get(i))[1]});
        }
        for (int n : denoms) {
            for (int i = 0; i < origami.polygons_size(); i++) {
                if (origami.isNonDegenerate(i)) {
                    for (int ii = 0; ii < origami.polygons().get(i).size() - 1; ii++) {

                        double[] p1 = origami.vertices().get(origami.polygons().get(i).get(ii));
                        double[] p2 = origami.vertices().get(origami.polygons().get(i).get(ii + 1));
                        for (int j = 1; j < n; j++) {

                            double[] nsect = new double[]{
                                (p1[0] * j + p2[0] * (n - j)) / n,
                                (p1[1] * j + p2[1] * (n - j)) / n,
                                (p1[2] * j + p2[2] * (n - j)) / n
                            };
                            nsectors.add(new int[]{(int) projection(nsect)[0], (int) projection(nsect)[1]});
                        }
                    }

                    double[] last1 = origami.vertices().get(origami.polygons().get(i).get(origami.polygons().get(i).size() - 1));
                    double[] last2 = origami.vertices().get(origami.polygons().get(i).get(0));
                    for (int j = 1; j < n; j++) {

                        double[] nsect = new double[]{
                            (last1[0] * j + last2[0] * (n - j)) / n,
                            (last1[1] * j + last2[1] * (n - j)) / n,
                            (last1[2] * j + last2[2] * (n - j)) / n
                        };
                        nsectors.add(new int[]{(int) projection(nsect)[0], (int) projection(nsect)[1]});
                    }
                }
            }
        }

        return nsectors;
    }

    public java.util.List<int[]> alignmentPoints2d(Origami origami) {

        java.util.List<int[]> vissza = new ArrayList<>();
        for (int i = 0; i < origami.vertices_size(); i++) {

            vissza.add(new int[]{(int) projection(origami.vertices2d().get(i))[0],
                (int) projection(origami.vertices2d().get(i))[1]});
        }

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (origami.isNonDegenerate(i)) {

                for (int ii = 0; ii < origami.polygons().get(i).size() - 1; ii++) {

                    double[] pont1 = origami.vertices2d().get(origami.polygons().get(i).get(ii));
                    double[] pont2 = origami.vertices2d().get(origami.polygons().get(i).get(ii + 1));
                    double[] felezo = Geometry.midpoint(pont1, pont2);

                    vissza.add(new int[]{(int) projection(felezo)[0], (int) projection(felezo)[1]});
                }

                double[] Upont1 = origami.vertices2d().get(origami.polygons().get(i).get(origami.polygons().get(i).size() - 1));
                double[] Upont2 = origami.vertices2d().get(origami.polygons().get(i).get(0));
                double[] Ufelezo = Geometry.midpoint(Upont1, Upont2);

                vissza.add(new int[]{(int) projection(Ufelezo)[0], (int) projection(Ufelezo)[1]});
            }
        }

        return vissza;
    }

    public double circumscribedSquareSize(Origami origami) {

        Double t, b, l, r;
        t = (b = (l = (r = null)));
        for (double[] vert : origami.vertices()) {

            double[] proj = projection(vert);
            t = t == null ? proj[1] : proj[1] < t ? proj[1] : t;
            b = b == null ? proj[1] : proj[1] > b ? proj[1] : b;
            l = l == null ? proj[0] : proj[0] < l ? proj[0] : t;
            r = r == null ? proj[0] : proj[0] > r ? proj[0] : r;
        }
        return 2 * Math.max(Math.abs(t), Math.max(Math.abs(b), Math.max(Math.abs(l), Math.abs(r))));
    }

    public void drawEdges(Graphics canvas, Color color, Origami origami) {

        canvas.setColor(color);

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                Polygon edges = new Polygon();

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    edges.addPoint((short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + yshift);
                }
                canvas.drawPolygon(edges);
            }
        }
    }

    public String drawEdges(int x, int y, Origami origami) {

        String edges = "1 w ";

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                edges += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                edges += " ";
                edges += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                edges += " m ";

                for (int ii = 1; ii < origami.polygons().get(i).size(); ii++) {
                    edges += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + x);
                    edges += " ";
                    edges += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + y);
                    edges += " l ";
                }
                edges += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                edges += " ";
                edges += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                edges += " l S ";
            }
        }

        return edges;
    }

    public void drawPreview(Graphics canvas, Color color, Origami origami, double[] ppoint, double[] pnormal) {

        double[] cp_tmp = camera_pos;
        double[] cd_tmp = camera_dir;
        double[] x_tmp = axis_x;
        double[] y_tmp = axis_y;

        camera_pos = Geometry.reflection(camera_pos, ppoint, pnormal);
        camera_dir = Geometry.reflection(camera_dir, Geometry.nullvector, pnormal);
        axis_x = Geometry.reflection(axis_x, Geometry.nullvector, pnormal);
        axis_y = Geometry.reflection(axis_y, Geometry.nullvector, pnormal);

        drawEdges(canvas, color, origami);

        camera_pos = cp_tmp;
        camera_dir = cd_tmp;
        axis_x = x_tmp;
        axis_y = y_tmp;
    }

    public String drawSelection(int x, int y, double[] ppoint, double[] pnormal, int polygonIndex, Origami origami) {

        String selection = "0.8 0.8 0.8 rg ";

        ArrayList<Integer> kijeloles = origami.polygonSelect(ppoint, pnormal, polygonIndex);
        for (int i : kijeloles) {

            if (isDrawable(i, origami)) {

                selection += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                selection += " ";
                selection += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                selection += " m ";

                for (int ii = 1; ii < origami.polygons().get(i).size(); ii++) {
                    selection += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + x);
                    selection += " ";
                    selection += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + y);
                    selection += " l ";
                }
                selection += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                selection += " ";
                selection += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                selection += " l f ";
            }
        }

        return selection;
    }

    public boolean isDrawable(int polygonIndex, Origami origami) {
        return origami.isStrictlyNonDegenerate(polygonIndex);
    }

    public boolean isDrawable(int polygonIndex, Origami origami, int... ref) {

        if (origami.polygons().get(polygonIndex).size() > 2) {

            double maxarea = 0;
            for (int i = 0; i < origami.polygons().get(polygonIndex).size(); i++) {

                int pont1ind = origami.polygons().get(polygonIndex).get(i);
                int pont0ind = origami.polygons().get(polygonIndex).get((i + 1) % origami.polygons().get(polygonIndex).size());
                int pont2ind = origami.polygons().get(polygonIndex).get((i + 2) % origami.polygons().get(polygonIndex).size());
                double area = Geometry.vector_length(Geometry.vector_product(Geometry.vector(origami.vertices().get(pont1ind), origami.vertices().get(pont0ind)),
                        Geometry.vector(origami.vertices().get(pont2ind), origami.vertices().get(pont0ind))));
                if (area > maxarea) {
                    maxarea = area;
                    ref[1] = pont1ind;
                    ref[2] = pont2ind;
                    ref[0] = pont0ind;
                }
            }
            if (maxarea > 1) {
                return true;
            }
        }
        return false;
    }

    public void drawGradient(Graphics canvas, int rgb, Origami origami) {

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                double[] fnormal = Geometry.vector_product(Geometry.vector(origami.vertices().get(origami.polygons().get(i).get(0)),
                        origami.vertices().get(origami.polygons().get(i).get(1))),
                        Geometry.vector(origami.vertices().get(origami.polygons().get(i).get(0)),
                                origami.vertices().get(origami.polygons().get(i).get(2))));

                double nv_len = Geometry.vector_length(fnormal);
                if (nv_len != 0) {
                    fnormal[0] = fnormal[0] / nv_len;
                    fnormal[1] = fnormal[1] / nv_len;
                    fnormal[2] = fnormal[2] / nv_len;
                }

                double alpha = 1 - Math.abs(Geometry.scalar_product(camera_dir, fnormal));
                int color = Geometry.scalar_product(camera_dir, fnormal) > 0 ? (rgb & 0xFFFFFF) : paper_back_color;

                Polygon path = new Polygon();

                double[] close = null, far = null;

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    path.addPoint((short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + yshift);

                    double sc = Geometry.scalar_product(origami.vertices().get(origami.polygons().get(i).get(ii)), camera_dir);
                    if (close == null ? true : sc > Geometry.scalar_product(close, camera_dir)) {
                        close = origami.vertices().get(origami.polygons().get(i).get(ii));
                    }
                    if (far == null ? true : sc < Geometry.scalar_product(far, camera_dir)) {
                        far = origami.vertices().get(origami.polygons().get(i).get(ii));
                    }
                }

                double[] grad_dir = Geometry.vector_product(fnormal, Geometry.vector_product(fnormal, camera_dir));
                close = Geometry.line_plane_intersection(far, grad_dir, close, camera_dir);

                double dclose = Geometry.scalar_product(Geometry.vector(close, camera_pos), camera_dir)
                        / Math.max(origami.circumscribedSquareSize() * Math.sqrt(2) / 2, 1);
                double dfar = Geometry.scalar_product(Geometry.vector(far, camera_pos), camera_dir)
                        / Math.max(origami.circumscribedSquareSize() * Math.sqrt(2) / 2, 1);
                float[] hsb = Color.RGBtoHSB((color >>> 16) % 0x100, (color >>> 8) % 0x100, color % 0x100, null);

                int rgb1 = Color.HSBtoRGB(hsb[0], Math.max(Math.min((float) (.5 - dclose * .5), 1f), 0f), 1f) & 0xFFFFFF;
                int rgb2 = Color.HSBtoRGB(hsb[0], Math.max(Math.min((float) (.5 - dfar * .5), 1f), 0f), hsb[2]) & 0xFFFFFF;

                Color c1, c2;
                try {
                    c1 = new Color((rgb1 >>> 16) % 0x100, (rgb1 >>> 8) % 0x100, rgb1 % 0x100, (int) (alpha * 64) + 100);
                } catch (Exception exc) {
                    c1 = new Color((rgb1 >>> 16) % 0x100, (rgb1 >>> 8) % 0x100, rgb1 % 0x100, 188);
                }
                try {
                    c2 = new Color((rgb2 >>> 16) % 0x100, (rgb2 >>> 8) % 0x100, rgb2 % 0x100, (int) (alpha * 64) + 100);
                } catch (Exception exc) {
                    c2 = new Color((rgb2 >>> 16) % 0x100, (rgb2 >>> 8) % 0x100, rgb2 % 0x100, 188);
                }
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        (float) projection(close)[0] + xshift,
                        (float) projection(close)[1] + yshift,
                        c1,
                        (float) projection(far)[0] + xshift,
                        (float) projection(far)[1] + yshift,
                        c2
                );
                ((java.awt.Graphics2D) canvas).setPaint((gp));

                canvas.fillPolygon(path);
            }
        }
    }

    public void drawFaces(Graphics canvas, int rgb, Origami origami) {

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                double[] fnormal = Geometry.vector_product(
                        Geometry.vector(origami.vertices().get(origami.polygons().get(i).get(0)),
                                origami.vertices().get(origami.polygons().get(i).get(1))),
                        Geometry.vector(origami.vertices().get(origami.polygons().get(i).get(0)),
                                origami.vertices().get(origami.polygons().get(i).get(2))));

                double nv_len = Geometry.vector_length(fnormal);
                if (nv_len != 0) {
                    fnormal[0] = fnormal[0] / nv_len;
                    fnormal[1] = fnormal[1] / nv_len;
                    fnormal[2] = fnormal[2] / nv_len;
                }

                double alpha = 1 - Math.abs(Geometry.scalar_product(camera_dir, fnormal));
                int color = Geometry.scalar_product(camera_dir, fnormal) > 0 ? (rgb & 0xFFFFFF) : paper_back_color;

                try {
                    canvas.setColor(
                            new Color((color >>> 16) % 0x100, (color >>> 8) % 0x100, color % 0x100, (int) (alpha * 128) + 80));
                } catch (Exception exc) {
                    canvas.setColor(new Color((color >>> 16) % 0x100, (color >>> 8) % 0x100, color % 0x100, 188));
                }

                Polygon path = new Polygon();

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    path.addPoint((short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + yshift);
                }

                canvas.fillPolygon(path);
            }
        }
    }

    public String drawFaces(int x, int y, Origami origami) {

        String out = "0.8 0.8 0.8 rg ";

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                out += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                out += " ";
                out += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                out += " m ";

                for (int ii = 1; ii < origami.polygons().get(i).size(); ii++) {
                    out += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + x);
                    out += " ";
                    out += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + y);
                    out += " l ";
                }
                out += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                out += " ";
                out += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                out += " l f ";
            }
        }

        return out;
    }

    public void drawCreasePattern(Graphics canvas, Color color, Origami origami) {

        canvas.setColor(color);

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                Polygon ut = new Polygon();

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    ut.addPoint((short) (projection(origami.vertices2d().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices2d().get(origami.polygons().get(i).get(ii)))[1]) + yshift);
                }
                canvas.drawPolygon(ut);
            }
        }
    }

    public void drawFoldingLine(Graphics canvas, Color color, double[] ppoint, double[] pnormal, Origami origami) {

        canvas.setColor(color);
        java.util.ArrayList<double[]> line = origami.foldingLine(ppoint, pnormal);
        for (int i = 0; i < line.size(); i += 2) {
            canvas.drawLine(
                    (short) (projection(line.get(i))[0] + xshift),
                    (short) (projection(line.get(i))[1] + yshift),
                    (short) (projection(line.get(i + 1))[0] + xshift),
                    (short) (projection(line.get(i + 1))[1] + yshift));
        }
    }

    public void draw2dFoldingLine(Graphics canvas, Color color, double[] ppoint, double[] pnormal, Origami origami) {

        canvas.setColor(color);
        java.util.ArrayList<double[]> line = origami.foldingLine2d(ppoint, pnormal);
        for (int i = 0; i < line.size(); i += 2) {
            canvas.drawLine(
                    (short) (projection(line.get(i))[0] + xshift),
                    (short) (projection(line.get(i))[1] + yshift),
                    (short) (projection(line.get(i + 1))[0] + xshift),
                    (short) (projection(line.get(i + 1))[1] + yshift));
        }
    }

    public String pfdLiner(int x, int y, double[] ppoint, double[] pnormal) {

        String out = "0.4 0.4 0.4 RG [5 5] 0 d ";
        double[] pnormal_2D = projection0(pnormal);
        double[] ppoint_2D = projection(ppoint);
        boolean lineto = false;
        double bound = 100;

        if (pdfLinerDir(pnormal) == 'J' || pdfLinerDir(pnormal) == 'B') {

            double[] pdir_2D = new double[]{-pnormal_2D[1] / pnormal_2D[0], 1};

            if (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) <= bound
                    && ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) >= -bound) {

                out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1])) + x);
                out += " ";
                out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[1])) + y);
                out += " m ";
                lineto = true;
            }

            if (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) <= bound
                    && ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) >= -bound) {

                out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1])) + x);
                out += " ";
                out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[1])) + y);
                if (lineto) {

                    out += " l ";
                    lineto = false;
                } else {

                    out += " m ";
                    lineto = true;
                }
            }

            if (lineto) {

                pdir_2D = new double[]{1, -pnormal_2D[0] / pnormal_2D[1]};

                if (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) <= bound
                        && ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) >= -bound) {

                    out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[0])) + x);
                    out += " ";
                    out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0])) + y);
                    out += " l ";
                }

                if (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) <= bound
                        && ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) >= -bound) {

                    out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[0])) + x);
                    out += " ";
                    out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0])) + y);
                    out += " l ";
                }
            }
        } else {

            double[] pdir_2D = new double[]{1, -pnormal_2D[0] / pnormal_2D[1]};

            if (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) <= bound
                    && ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) >= -bound) {

                out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[0])) + x);
                out += " ";
                out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0])) + y);
                out += " m ";
                lineto = true;
            }

            if (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) <= bound
                    && ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) >= -bound) {

                out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[0])) + x);
                out += " ";
                out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0])) + y);
                if (lineto) {

                    out += " l ";
                    lineto = false;
                } else {

                    out += " m ";
                    lineto = true;
                }
            }

            if (lineto) {

                pdir_2D = new double[]{-pnormal_2D[1] / pnormal_2D[0], 1};

                if (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) <= bound
                        && ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) >= -bound) {

                    out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1])) + x);
                    out += " ";
                    out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[1])) + y);
                    out += " l ";
                }

                if (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) <= bound
                        && ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) >= -bound) {

                    out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1])) + x);
                    out += " ";
                    out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[1])) + y);
                    out += " l ";
                }
            }
        }

        out += "S [ ] 0 d 0.0 0.0 0.0 RG ";
        return out;
    }

    final static public int PDF_NORTH = 'F';
    final static public int PDF_SOUTH = 'L';
    final static public int PDF_WEST = 'B';
    final static public int PDF_EAST = 'J';

    public int pdfLinerDir(double[] pnormal) {

        double[] pnormal_2D = projection0(pnormal);

        if (pnormal_2D[0] < pnormal_2D[1]) {

            if (pnormal_2D[0] < -pnormal_2D[1]) {

                return PDF_WEST;
            } else {

                return PDF_NORTH;
            }
        } else {

            if (pnormal_2D[0] < -pnormal_2D[1]) {

                return PDF_SOUTH;
            } else {

                return PDF_EAST;
            }
        }
    }

    public java.util.ArrayList<int[]> centers(Origami origami) {

        java.util.ArrayList<int[]> ret = new java.util.ArrayList<>(java.util.Arrays.asList(new int[][]{}));
        for (int i = 0; i < origami.polygons_size(); i++) {
            ret.add(new int[]{(short) (projection(origami.polygonCenter(i))[0]) + xshift,
                    (short) (projection(origami.polygonCenter(i))[1]) + yshift});
        }
        return ret;
    }

    public int polygonSelect(int cursor_x, int cursor_y, Origami origami) {

        java.util.ArrayList<int[]> centers = centers(origami);

        int min = Integer.MAX_VALUE;
        int min_point = -1;
        for (int i = 0; i < origami.polygons_size(); i++) {

            if (origami.isNonDegenerate(i)) {

                int[] kpoint = centers.get(i);
                int dist_sq = (kpoint[0] - cursor_x) * (kpoint[0] - cursor_x)
                        + (kpoint[1] - cursor_y) * (kpoint[1] - cursor_y);

                if (dist_sq < min) {
                    min = dist_sq;
                    min_point = i;
                }
            }
        }
        return min_point;
    }

    public void adjust(Origami origami) {

        Double a, f, b, j, h, e;
        f = (a = (j = (b = (e = (h = null)))));
        for (int i = 0; i < origami.vertices_size(); i++) {

            if (b == null || origami.vertices().get(i)[0] < b) {
                b = origami.vertices().get(i)[0];
            }
            if (j == null || origami.vertices().get(i)[0] > j) {
                j = origami.vertices().get(i)[0];
            }
            if (a == null || origami.vertices().get(i)[1] < a) {
                a = origami.vertices().get(i)[1];
            }
            if (f == null || origami.vertices().get(i)[1] > f) {
                f = origami.vertices().get(i)[1];
            }
            if (h == null || origami.vertices().get(i)[2] < h) {
                h = origami.vertices().get(i)[2];
            }
            if (e == null || origami.vertices().get(i)[2] > e) {
                e = origami.vertices().get(i)[2];
            }
        }

        if (origami.vertices_size() > 0) {
            camera_pos = new double[]{(b + j) / 2, (a + f) / 2, (h + e) / 2};
        }
    }

    public void unadjust(Origami origami) {

        double[] center = new double[]{0.0, 0.0, 0.0};
        for (double[] pont : origami.corners()) {
            center = new double[]{center[0] + pont[0], center[1] + pont[1], 0};
        }

        center = new double[]{center[0] / origami.corners().size(), center[1] / origami.corners().size(), 0};
        camera_pos = center;
    }

    public void setOrthogonalView(int orientation) {

        switch (orientation) {

            case 0:
                camera_dir[0] = 0;
                camera_dir[1] = 0;
                camera_dir[2] = 1;
                axis_x[0] = 1;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 1;
                axis_y[2] = 0;
                break;
            case 1:
                camera_dir[0] = 0;
                camera_dir[1] = 1;
                camera_dir[2] = 0;
                axis_x[0] = 1;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 0;
                axis_y[2] = -1;
                break;
            case 2:
                camera_dir[0] = -1;
                camera_dir[1] = 0;
                camera_dir[2] = 0;
                axis_x[0] = 0;
                axis_x[1] = 0;
                axis_x[2] = 1;
                axis_y[0] = 0;
                axis_y[1] = 1;
                axis_y[2] = 0;
                break;
            default:
                camera_dir[0] = 0;
                camera_dir[1] = 0;
                camera_dir[2] = 1;
                axis_x[0] = 1;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 1;
                axis_y[2] = 0;
                break;
        }

        this.orientation = (byte) orientation;
    }

    public void nextOrthogonalView() {

        switch (orientation) {

            case 0:
                camera_dir[0] = 0;
                camera_dir[1] = 0;
                camera_dir[2] = 1;
                axis_x[0] = 1;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 1;
                axis_y[2] = 0;
                break;
            case 1:
                camera_dir[0] = 0;
                camera_dir[1] = 1;
                camera_dir[2] = 0;
                axis_x[0] = 1;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 0;
                axis_y[2] = -1;
                break;
            case 2:
                camera_dir[0] = -1;
                camera_dir[1] = 0;
                camera_dir[2] = 0;
                axis_x[0] = 0;
                axis_x[1] = 0;
                axis_x[2] = 1;
                axis_y[0] = 0;
                axis_y[1] = 1;
                axis_y[2] = 0;
                break;
            default:
                camera_dir[0] = 0;
                camera_dir[1] = 0;
                camera_dir[2] = 1;
                axis_x[0] = 1;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 1;
                axis_y[2] = 0;
                break;
        }

        orientation = (byte) ((orientation + 1) % 3);
    }

    public void setTexture(java.awt.image.BufferedImage texture) throws Exception {

        if (texture.getColorModel().hasAlpha()) {
            throw OrigamiException.H013;
        }
        this.texture = texture;
    }

    public void updateBuffer(Origami origami) {

        java.awt.image.BufferedImage map = new java.awt.image.BufferedImage(
                texture.getWidth(), texture.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D canvas = map.createGraphics();
        canvas.setBackground(Color.WHITE);
        canvas.clearRect(0, 0, texture.getWidth(), texture.getHeight());
        int[][] skeleton = new int[origami.polygons_size()][];

        for (int i = 0; i < origami.polygons_size(); i++) {

            int[] triangle = new int[3];
            if (isDrawable(i, origami, triangle)) {

                skeleton[i] = triangle;

                Polygon path = new Polygon();
                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    path.addPoint((short) (new Camera(0, 0, 1d).projection(origami.vertices2d().get(origami.polygons().get(i).get(ii)))[0]) + 200,
                            (short) (new Camera(0, 0, 1d).projection(origami.vertices2d().get(origami.polygons().get(i).get(ii)))[1]) + 200);
                }
                canvas.setColor(new Color(i));
                canvas.fillPolygon(path);
            }
        }

        int[] raw = ((java.awt.image.DataBufferInt) map.getRaster().getDataBuffer()).getData();

        int len = texture.getHeight() * texture.getWidth();
        int width = texture.getWidth();
        space_buffer = new double[len][];

        for (int i = 0; i < len; i++) {

            int color = raw[i] & 0xFFFFFF;
            if (color != 0xFFFFFF) {

                try {
                    double x_1 = origami.vertices2d().get(skeleton[color][1])[0] - origami.vertices2d().get(skeleton[color][0])[0];
                    double x_2 = origami.vertices2d().get(skeleton[color][1])[1] - origami.vertices2d().get(skeleton[color][0])[1];
                    double y_1 = origami.vertices2d().get(skeleton[color][2])[0] - origami.vertices2d().get(skeleton[color][0])[0];
                    double y_2 = origami.vertices2d().get(skeleton[color][2])[1] - origami.vertices2d().get(skeleton[color][0])[1];
                    double a_1 = (double) (i % width) - origami.vertices2d().get(skeleton[color][0])[0];
                    double a_2 = (double) i / width - origami.vertices2d().get(skeleton[color][0])[1];

                    double lambda1 = (a_1 * y_2 - a_2 * y_1) / (x_1 * y_2 - x_2 * y_1);
                    double lambda2 = (a_1 * x_2 - a_2 * x_1) / (y_1 * x_2 - y_2 * x_1);
                    double[] v3d1 = Geometry.vector(origami.vertices.get(skeleton[color][1]), origami.vertices.get(skeleton[color][0]));
                    double[] v3d2 = Geometry.vector(origami.vertices.get(skeleton[color][0]), origami.vertices.get(skeleton[color][2]));
                    space_buffer[i] = Geometry.vector(
                            Geometry.vector(Geometry.scalar_multip(v3d1, lambda1), Geometry.scalar_multip(v3d2, lambda2)),
                            Geometry.scalar_multip(origami.vertices.get(skeleton[color][0]), -1));
                } catch (Exception ex) {
                }
            }
        }

        new Camera(200, 200, 1d).drawCreasePattern(texture.createGraphics(), Color.BLACK, origami);
    }

    public void drawTexture(Graphics canvas, int w, int h) {

        byte[] raw = ((java.awt.image.DataBufferByte) texture.getRaster().getDataBuffer()).getData();
        Double[][] depth_buffer = new Double[w][h];
        java.awt.image.BufferedImage ret = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D bleach = ret.createGraphics();
        bleach.setBackground(Color.WHITE);
        bleach.clearRect(0, 0, w, h);

        for (int i = 0; i < raw.length; i += 3) {

            int szin = (raw[i] & 0xFF) + ((raw[i + 1] & 0xFF) << 8) + ((raw[i + 2] & 0xFF) << 16);
            kiserlet:
            try {
                double[] point = space_buffer[i / 3];
                if (point == null) {
                    break kiserlet;
                }
                double tmp = zoom;
                zoom = 1;
                double[] proj = projection(point);
                zoom = tmp;
                short projX = (short) (proj[0] + xshift);
                short projY = (short) (proj[1] + yshift);
                if (projX >= 0 && projX < depth_buffer.length && projY >= 0 && projY < depth_buffer[0].length) {
                    if (depth_buffer[projX][projY] == null
                            || Geometry.scalar_product(point, camera_dir) > depth_buffer[projX][projY]) {

                        depth_buffer[projX][projY] = Geometry.scalar_product(point, camera_dir);
                        ret.setRGB(projX, projY, szin);
                    }
                }
            } catch (Exception ex) {
            }
        }
        canvas.drawImage(ret,
                (int)((1-zoom) * ret.getWidth() / 2), (int)((1-zoom) * ret.getHeight() / 2),
                (int)(ret.getWidth() * zoom), (int)(ret.getHeight() * zoom),
                null);
    }
}
