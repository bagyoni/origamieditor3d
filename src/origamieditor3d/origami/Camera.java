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
package origamieditor3d.origami;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * Metódusokat nyújt az {@linkplain Origami} {@linkplain Canvas} objektumra,
 * illetve PDF-adatfolyamba rajzolásához.
 *
 * @author Attila Bágyoni <bagyoni.attila@gmail.com>
 * @since 2013-01-14
 * @see Origami
 */
public class Camera {

    final static public int paper_back_color = 15720379;
    final static public int paper_front_color = 3388901;
    final static public int maximal_zoom = 4;

    public Camera(int x, int y, double zoom) {

        camera_pos = default_camera_pos.clone();
        camera_dir = default_camera_dir.clone();
        axis_x = default_axis_x.clone();
        axis_y = default_axis_y.clone();
        xshift = x;
        yshift = y;
        this.zoom = zoom;

        double[] xt = axis_x.clone();
        axis_x[0] = axis_x[0] / Origami.vector_length(xt) * zoom;
        axis_x[1] = axis_x[1] / Origami.vector_length(xt) * zoom;
        axis_x[2] = axis_x[2] / Origami.vector_length(xt) * zoom;

        double[] yt = axis_y.clone();
        axis_y[0] = axis_y[0] / Origami.vector_length(yt) * zoom;
        axis_y[1] = axis_y[1] / Origami.vector_length(yt) * zoom;
        axis_y[2] = axis_y[2] / Origami.vector_length(yt) * zoom;

        double[] zt = camera_dir.clone();
        camera_dir[0] = camera_dir[0] / Origami.vector_length(zt) * zoom;
        camera_dir[1] = camera_dir[1] / Origami.vector_length(zt) * zoom;
        camera_dir[2] = camera_dir[2] / Origami.vector_length(zt) * zoom;
    }
    public double[] camera_pos;
    public double[] camera_dir;
    public double[] axis_x;
    public double[] axis_y;
    static public double[] default_camera_pos = {200, 200, 0};
    static public double[] default_camera_dir = {0, 0, 1};
    static public double[] default_axis_x = {1, 0, 0};
    static public double[] default_axis_y = {0, 1, 0};
    public int xshift = 230;
    public int yshift = 230;
    private double zoom = 1.0;
    private double[][] space_buffer;
    public java.awt.image.BufferedImage texture;

    public double zoom() {

        return zoom;
    }

    public void setZoom(double value) {

        zoom = value;

        double[] xt = axis_x.clone();
        axis_x[0] = axis_x[0] / Origami.vector_length(xt) * zoom;
        axis_x[1] = axis_x[1] / Origami.vector_length(xt) * zoom;
        axis_x[2] = axis_x[2] / Origami.vector_length(xt) * zoom;

        double[] yt = axis_y.clone();
        axis_y[0] = axis_y[0] / Origami.vector_length(yt) * zoom;
        axis_y[1] = axis_y[1] / Origami.vector_length(yt) * zoom;
        axis_y[2] = axis_y[2] / Origami.vector_length(yt) * zoom;

        double[] zt = camera_dir.clone();
        camera_dir[0] = camera_dir[0] / Origami.vector_length(zt) * zoom;
        camera_dir[1] = camera_dir[1] / Origami.vector_length(zt) * zoom;
        camera_dir[2] = camera_dir[2] / Origami.vector_length(zt) * zoom;
    }
    protected byte orientation = 0;

    public double[] projection0(double[] point) {

        double konst = camera_pos[0] * camera_dir[0] + camera_pos[1] * camera_dir[1] + camera_pos[2] * camera_dir[2];

        double[] iranyvek = camera_dir;
        double X = point[0];
        double Y = point[1];
        double Z = point[2];
        double U = iranyvek[0];
        double V = iranyvek[1];
        double W = iranyvek[2];
        double A = camera_dir[0];
        double B = camera_dir[1];
        double C = camera_dir[2];
        double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);

        double[] talppont = {X + t * U, Y + t * V, Z + t * W};
        double[] vissza0 = {talppont[0] * axis_x[0] + talppont[1] * axis_x[1] + talppont[2] * axis_x[2],
            talppont[0] * axis_y[0] + talppont[1] * axis_y[1] + talppont[2] * axis_y[2]};
        return vissza0;
    }

    public double[] projection(double[] point) {
        double[] vissza0 = {projection0(point)[0] - projection0(camera_pos)[0], projection0(point)[1] - projection0(camera_pos)[1]};
        return vissza0;
    }

    public void rotate(float x, float y) {

        double sinX = Math.sin(x * Math.PI / 180);
        double cosX = Math.cos(x * Math.PI / 180);

        double Cx = axis_y[0];
        double Cy = axis_y[1];
        double Cz = axis_y[2];

        double X = camera_dir[0];
        double Y = camera_dir[1];
        double Z = camera_dir[2];

        double sinphi = sinX;
        double cosphi = cosX;

        double kepX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi) + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
        double kepY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi)) + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
        double kepZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi) + Z * (cosphi + Cz * Cz * (1 - cosphi));

        camera_dir[0] = kepX;
        camera_dir[1] = kepY;
        camera_dir[2] = kepZ;

        X = axis_x[0];
        Y = axis_x[1];
        Z = axis_x[2];

        kepX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi) + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
        kepY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi)) + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
        kepZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi) + Z * (cosphi + Cz * Cz * (1 - cosphi));

        axis_x[0] = kepX / Origami.vector_length(new double[]{kepX, kepY, kepZ}) * zoom;
        axis_x[1] = kepY / Origami.vector_length(new double[]{kepX, kepY, kepZ}) * zoom;
        axis_x[2] = kepZ / Origami.vector_length(new double[]{kepX, kepY, kepZ}) * zoom;

        double sinY = Math.sin(y * Math.PI / 180);
        double cosY = Math.cos(y * Math.PI / 180);

        Cx = axis_x[0];
        Cy = axis_x[1];
        Cz = axis_x[2];

        X = camera_dir[0];
        Y = camera_dir[1];
        Z = camera_dir[2];

        sinphi = sinY;
        cosphi = cosY;

        kepX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi) + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
        kepY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi)) + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
        kepZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi) + Z * (cosphi + Cz * Cz * (1 - cosphi));

        camera_dir[0] = kepX / Origami.vector_length(new double[]{kepX, kepY, kepZ});
        camera_dir[1] = kepY / Origami.vector_length(new double[]{kepX, kepY, kepZ});
        camera_dir[2] = kepZ / Origami.vector_length(new double[]{kepX, kepY, kepZ});

        X = axis_y[0];
        Y = axis_y[1];
        Z = axis_y[2];

        kepX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi) + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
        kepY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi)) + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
        kepZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi) + Z * (cosphi + Cz * Cz * (1 - cosphi));

        axis_y[0] = kepX / Origami.vector_length(new double[]{kepX, kepY, kepZ}) * zoom;
        axis_y[1] = kepY / Origami.vector_length(new double[]{kepX, kepY, kepZ}) * zoom;
        axis_y[2] = kepZ / Origami.vector_length(new double[]{kepX, kepY, kepZ}) * zoom;
    }

    public java.util.List<int[]> alignmentPoints(Origami origami) {

        java.util.List<int[]> vissza = new ArrayList<>();
        for (int i = 0; i < origami.vertices_size(); i++) {

            vissza.add(new int[]{(int) projection(origami.vertices().get(i))[0],
                (int) projection(origami.vertices().get(i))[1]});
        }

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (origami.isNonDegenerate(i)) {

                for (int ii = 0; ii < origami.polygons().get(i).size() - 1; ii++) {

                    double[] pont1 = origami.vertices().get(origami.polygons().get(i).get(ii));
                    double[] pont2 = origami.vertices().get(origami.polygons().get(i).get(ii + 1));
                    double[] felezo = Origami.midpoint(pont1, pont2);

                    vissza.add(new int[]{(int) projection(felezo)[0], (int) projection(felezo)[1]});
                }

                double[] Upont1 = origami.vertices().get(origami.polygons().get(i).get(origami.polygons().get(i).size() - 1));
                double[] Upont2 = origami.vertices().get(origami.polygons().get(i).get(0));
                double[] Ufelezo = Origami.midpoint(Upont1, Upont2);

                vissza.add(new int[]{(int) projection(Ufelezo)[0], (int) projection(Ufelezo)[1]});
            }
        }

        return vissza;
    }

    public java.util.List<int[]> alignmentPoints2d(Origami origami) {

        java.util.List<int[]> vissza = new ArrayList<>();
        for (int i = 0; i < origami.vertices_size(); i++) {

            vissza.add(new int[]{(int) projection(origami.vertices_2d().get(i))[0],
                (int) projection(origami.vertices_2d().get(i))[1]});
        }

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (origami.isNonDegenerate(i)) {

                for (int ii = 0; ii < origami.polygons().get(i).size() - 1; ii++) {

                    double[] pont1 = origami.vertices_2d().get(origami.polygons().get(i).get(ii));
                    double[] pont2 = origami.vertices_2d().get(origami.polygons().get(i).get(ii + 1));
                    double[] felezo = Origami.midpoint(pont1, pont2);

                    vissza.add(new int[]{(int) projection(felezo)[0], (int) projection(felezo)[1]});
                }

                double[] Upont1 = origami.vertices_2d().get(origami.polygons().get(i).get(origami.polygons().get(i).size() - 1));
                double[] Upont2 = origami.vertices_2d().get(origami.polygons().get(i).get(0));
                double[] Ufelezo = Origami.midpoint(Upont1, Upont2);

                vissza.add(new int[]{(int) projection(Ufelezo)[0], (int) projection(Ufelezo)[1]});
            }
        }

        return vissza;
    }

    public void drawEdges(Graphics canvas, Color color, Origami origami) {

        canvas.setColor(color);

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                Polygon ut = new Polygon();

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    ut.addPoint((short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + yshift);
                }
                canvas.drawPolygon(ut);
            }
        }
    }

    public String drawEdges(int x, int y, Origami origami) {

        String ki = "1 w ";

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                ki += " ";
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                ki += " m ";

                for (int ii = 1; ii < origami.polygons().get(i).size(); ii++) {
                    ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + x);
                    ki += " ";
                    ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + y);
                    ki += " l ";
                }
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                ki += " ";
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                ki += " l S ";
            }
        }

        return ki;
    }

    public void drawPreview(Graphics canvas, Color color, Origami origami, double[] ppoint, double[] pnormal) {

        double[] vpt = camera_pos;
        double[] vnv = camera_dir;
        double[] xt = axis_x;
        double[] yt = axis_y;

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        double[] talppont;
        double X, Y, Z, t;
        double[] iranyvek = pnormal;
        double U = iranyvek[0];
        double V = iranyvek[1];
        double W = iranyvek[2];
        double A = pnormal[0];
        double B = pnormal[1];
        double C = pnormal[2];

        X = camera_pos[0];
        Y = camera_pos[1];
        Z = camera_pos[2];
        t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);
        talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
        camera_pos = new double[]{
            talppont[0] + Origami.vector(talppont, camera_pos)[0],
            talppont[1] + Origami.vector(talppont, camera_pos)[1],
            talppont[2] + Origami.vector(talppont, camera_pos)[2]};

        X = camera_dir[0];
        Y = camera_dir[1];
        Z = camera_dir[2];
        t = -(A * X + B * Y + C * Z) / (A * U + B * V + C * W);
        talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
        camera_dir = new double[]{
            talppont[0] + Origami.vector(talppont, camera_dir)[0],
            talppont[1] + Origami.vector(talppont, camera_dir)[1],
            talppont[2] + Origami.vector(talppont, camera_dir)[2]};

        X = axis_x[0];
        Y = axis_x[1];
        Z = axis_x[2];
        t = -(A * X + B * Y + C * Z) / (A * U + B * V + C * W);
        talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
        axis_x = new double[]{
            talppont[0] + Origami.vector(talppont, axis_x)[0],
            talppont[1] + Origami.vector(talppont, axis_x)[1],
            talppont[2] + Origami.vector(talppont, axis_x)[2]};

        X = axis_y[0];
        Y = axis_y[1];
        Z = axis_y[2];
        t = -(A * X + B * Y + C * Z) / (A * U + B * V + C * W);
        talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
        axis_y = new double[]{
            talppont[0] + Origami.vector(talppont, axis_y)[0],
            talppont[1] + Origami.vector(talppont, axis_y)[1],
            talppont[2] + Origami.vector(talppont, axis_y)[2]};

        drawEdges(canvas, color, origami);
        camera_pos = vpt;
        camera_dir = vnv;
        axis_x = xt;
        axis_y = yt;
    }

    public String drawSelection(int x, int y, double[] ppoint, double[] pnormal, int polygonIndex, Origami origami) {

        String ki = "0.8 0.8 0.8 rg ";

        ArrayList<Integer> kijeloles = origami.polygonSelect(ppoint, pnormal, polygonIndex);
        for (int i : kijeloles) {

            if (isDrawable(i, origami)) {

                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                ki += " ";
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                ki += " m ";

                for (int ii = 1; ii < origami.polygons().get(i).size(); ii++) {
                    ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + x);
                    ki += " ";
                    ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + y);
                    ki += " l ";
                }
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                ki += " ";
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                ki += " l f ";
            }
        }

        return ki;
    }

    public boolean isDrawable(int polygonIndex, Origami origami) {

        if (origami.polygons().get(polygonIndex).size() > 2) {

            for (int pont1ind : origami.polygons().get(polygonIndex)) {
                for (int pont2ind : origami.polygons().get(polygonIndex)) {

                    if (Origami.vector_length(Origami.vector_product(Origami.vector(origami.vertices().get(pont1ind), origami.vertices().get(origami.polygons().get(polygonIndex).get(0))),
                            Origami.vector(origami.vertices().get(pont2ind), origami.vertices().get(origami.polygons().get(polygonIndex).get(0))))) > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isDrawable(int polygonIndex, Origami origami, int... ref) {

        if (origami.polygons().get(polygonIndex).size() > 2) {

            double maxter = 0;
            for (int i = 0; i < origami.polygons().get(polygonIndex).size(); i++) {

                int pont1ind = origami.polygons().get(polygonIndex).get(i);
                int pont0ind = origami.polygons().get(polygonIndex).get((i + 1) % origami.polygons().get(polygonIndex).size());
                int pont2ind = origami.polygons().get(polygonIndex).get((i + 2) % origami.polygons().get(polygonIndex).size());
                double ter = Origami.vector_length(Origami.vector_product(Origami.vector(origami.vertices().get(pont1ind), origami.vertices().get(pont0ind)),
                        Origami.vector(origami.vertices().get(pont2ind), origami.vertices().get(pont0ind))));
                if (ter > maxter) {
                    maxter = ter;
                    ref[1] = pont1ind;
                    ref[2] = pont2ind;
                    ref[0] = pont0ind;
                }
            }
            if (maxter > 1) {
                return true;
            }
        }
        return false;
    }

    public void drawFaces(Graphics canvas, int rgb, Origami origami) {

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                double[] normalvek = Origami.vector_product(Origami.vector(origami.vertices().get(origami.polygons().get(i).get(0)),
                        origami.vertices().get(origami.polygons().get(i).get(1))),
                        Origami.vector(origami.vertices().get(origami.polygons().get(i).get(0)),
                        origami.vertices().get(origami.polygons().get(i).get(2))));

                double nvhossz = Origami.vector_length(normalvek);
                if (nvhossz != 0) {
                    normalvek[0] = normalvek[0] / nvhossz;
                    normalvek[1] = normalvek[1] / nvhossz;
                    normalvek[2] = normalvek[2] / nvhossz;
                }

                double alfa = 1 - Math.abs(Origami.scalar_product(camera_dir, normalvek));
                int szin = Origami.scalar_product(camera_dir, normalvek) > 0 ? (rgb & 0xFFFFFF) : paper_back_color;

                try {
                    canvas.setColor(new Color((szin >>> 16) % 0x100, (szin >>> 8) % 0x100, szin % 0x100, (int) (alfa * 128) + 80));
                } catch (Exception exc) {
                    canvas.setColor(new Color((szin >>> 16) % 0x100, (szin >>> 8) % 0x100, szin % 0x100, 188));
                }

                Polygon ut = new Polygon();

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    ut.addPoint((short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + yshift);
                }

                canvas.fillPolygon(ut);
            }
        }
    }

    public String drawFaces(int x, int y, Origami origami) {

        String ki = "0.8 0.8 0.8 rg ";

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                ki += " ";
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                ki += " m ";

                for (int ii = 1; ii < origami.polygons().get(i).size(); ii++) {
                    ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[0]) + x);
                    ki += " ";
                    ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(ii)))[1]) + y);
                    ki += " l ";
                }
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[0]) + x);
                ki += " ";
                ki += Integer.toString((int) (projection(origami.vertices().get(origami.polygons().get(i).get(0)))[1]) + y);
                ki += " l f ";
            }
        }

        return ki;
    }

    public void drawCreasePattern(Graphics canvas, Color color, Origami origami) {

        canvas.setColor(color);

        for (int i = 0; i < origami.polygons_size(); i++) {

            if (isDrawable(i, origami)) {

                Polygon ut = new Polygon();

                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    ut.addPoint((short) (projection(origami.vertices_2d().get(origami.polygons().get(i).get(ii)))[0]) + xshift,
                            (short) (projection(origami.vertices_2d().get(origami.polygons().get(i).get(ii)))[1]) + yshift);
                }
                canvas.drawPolygon(ut);
            }
        }
    }

    public String pfdLiner(int x, int y, double[] ppoint, double[] pnormal) {

        String ki = "0.4 0.4 0.4 RG [5 5] 0 d ";
        double[] siknv_2D = projection0(pnormal);
        double[] sikpont_2D = projection(ppoint);
        boolean lineto = false;
        double hatar = 100;

        if (pdfLinerDir(pnormal) == 'J' || pdfLinerDir(pnormal) == 'B') {

            double[] sikiv_2D = new double[]{-siknv_2D[1] / siknv_2D[0], 1};

            if (sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[1]) <= hatar
                    && sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[1]) >= -hatar) {

                ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[1])) + x);
                ki += " ";
                ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[1])) + y);
                ki += " m ";
                lineto = true;
            }

            if (sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[1]) <= hatar
                    && sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[1]) >= -hatar) {

                ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[1])) + x);
                ki += " ";
                ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[1])) + y);
                if (lineto) {

                    ki += " l ";
                    lineto = false;
                } else {

                    ki += " m ";
                    lineto = true;
                }
            }

            if (lineto) {

                sikiv_2D = new double[]{1, -siknv_2D[0] / siknv_2D[1]};

                if (sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[0]) <= hatar
                        && sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[0]) >= -hatar) {

                    ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[0])) + x);
                    ki += " ";
                    ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[0])) + y);
                    ki += " l ";
                }

                if (sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[0]) <= hatar
                        && sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[0]) >= -hatar) {

                    ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[0])) + x);
                    ki += " ";
                    ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[0])) + y);
                    ki += " l ";
                }
            }
        } else {

            double[] sikiv_2D = new double[]{1, -siknv_2D[0] / siknv_2D[1]};

            if (sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[0]) <= hatar
                    && sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[0]) >= -hatar) {

                ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[0])) + x);
                ki += " ";
                ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[0])) + y);
                ki += " m ";
                lineto = true;
            }

            if (sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[0]) <= hatar
                    && sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[0]) >= -hatar) {

                ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[0])) + x);
                ki += " ";
                ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[0])) + y);
                if (lineto) {

                    ki += " l ";
                    lineto = false;
                } else {

                    ki += " m ";
                    lineto = true;
                }
            }

            if (lineto) {

                sikiv_2D = new double[]{-siknv_2D[1] / siknv_2D[0], 1};

                if (sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[1]) <= hatar
                        && sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[1]) >= -hatar) {

                    ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (hatar - sikpont_2D[1])) + x);
                    ki += " ";
                    ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (hatar - sikpont_2D[1])) + y);
                    ki += " l ";
                }

                if (sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[1]) <= hatar
                        && sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[1]) >= -hatar) {

                    ki += Integer.toString((int) (sikpont_2D[0] + sikiv_2D[0] * (-hatar - sikpont_2D[1])) + x);
                    ki += " ";
                    ki += Integer.toString((int) (sikpont_2D[1] + sikiv_2D[1] * (-hatar - sikpont_2D[1])) + y);
                    ki += " l ";
                }
            }
        }

        ki += "S [ ] 0 d 0.0 0.0 0.0 RG ";
        return ki;
    }

    final static public int PDF_NORTH = 'F';
    final static public int PDF_SOUTH = 'L';
    final static public int PDF_WEST = 'B';
    final static public int PDF_EAST = 'J';

    public int pdfLinerDir(double[] pnormal) {

        double[] siknv_2D = projection0(pnormal);

        if (siknv_2D[0] < siknv_2D[1]) {

            if (siknv_2D[0] < -siknv_2D[1]) {

                return PDF_WEST;
            } else {

                return PDF_NORTH;
            }
        } else {

            if (siknv_2D[0] < -siknv_2D[1]) {

                return PDF_SOUTH;
            } else {

                return PDF_EAST;
            }
        }
    }

    public java.util.ArrayList<int[]> centers(Origami origami) {

        java.util.ArrayList<int[]> vissza = new java.util.ArrayList<>(java.util.Arrays.asList(new int[][]{}));
        for (int i = 0; i < origami.polygons_size(); i++) {
            vissza.add(new int[]{(short) (projection(origami.polygonCenter(i))[0]) + xshift, (short) (projection(origami.polygonCenter(i))[1]) + yshift});
        }
        return vissza;
    }

    public int polygonSelect(int cursor_x, int cursor_y, Origami origami) {

        java.util.ArrayList<int[]> kozepek = centers(origami);

        int min = Integer.MAX_VALUE;
        int minhely = -1;
        for (int i = 0; i < origami.polygons_size(); i++) {

            if (origami.isNonDegenerate(i)) {

                int[] kpont = kozepek.get(i);
                int tavolsagnegyzet = (kpont[0] - cursor_x) * (kpont[0] - cursor_x) + (kpont[1] - cursor_y) * (kpont[1] - cursor_y);

                if (tavolsagnegyzet < min) {
                    min = tavolsagnegyzet;
                    minhely = i;
                }
            }
        }
        return minhely;
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

        double[] sulypont = new double[]{0.0, 0.0, 0.0};
        for (double[] pont : origami.corners()) {
            sulypont = new double[]{sulypont[0] + pont[0], sulypont[1] + pont[1], 0};
        }

        sulypont = new double[]{sulypont[0] / origami.corners().size(), sulypont[1] / origami.corners().size(), 0};
        camera_pos = sulypont;
    }

    public void setOrthogonalView() {

        switch (orientation) {

            case 0:
                camera_dir[0] = 0;
                camera_dir[1] = 0;
                camera_dir[2] = 1 * zoom;
                axis_x[0] = 1 * zoom;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 1 * zoom;
                axis_y[2] = 0;
                break;
            case 1:
                camera_dir[0] = 0;
                camera_dir[1] = 1 * zoom;
                camera_dir[2] = 0;
                axis_x[0] = 1 * zoom;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 0;
                axis_y[2] = -1 * zoom;
                break;
            case 2:
                camera_dir[0] = -1 * zoom;
                camera_dir[1] = 0;
                camera_dir[2] = 0;
                axis_x[0] = 0;
                axis_x[1] = 0;
                axis_x[2] = 1 * zoom;
                axis_y[0] = 0;
                axis_y[1] = 1 * zoom;
                axis_y[2] = 0;
                break;
            default:
                camera_dir[0] = 0;
                camera_dir[1] = 0;
                camera_dir[2] = 1 * zoom;
                axis_x[0] = 1 * zoom;
                axis_x[1] = 0;
                axis_x[2] = 0;
                axis_y[0] = 0;
                axis_y[1] = 1 * zoom;
                axis_y[2] = 0;
                break;
        }

        orientation = (byte) ((orientation + 1) % 3);
    }

    public void setTexture(java.awt.image.BufferedImage texture) {
        this.texture = texture;
    }

    public void updateBuffer(Origami origami) {

        java.awt.image.BufferedImage terkep = new java.awt.image.BufferedImage(texture.getWidth(), texture.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D canvas = terkep.createGraphics();
        canvas.setBackground(Color.WHITE);
        canvas.clearRect(0, 0, texture.getWidth(), texture.getHeight());
        int[][] vaz = new int[origami.polygons_size()][];

        for (int i = 0; i < origami.polygons_size(); i++) {

            int[] hsz = new int[3];
            if (isDrawable(i, origami, hsz)) {

                vaz[i] = hsz;

                Polygon ut = new Polygon();
                for (int ii = 0; ii < origami.polygons().get(i).size(); ii++) {

                    ut.addPoint((short) (new Camera(0, 0, 1d).projection(origami.vertices_2d().get(origami.polygons().get(i).get(ii)))[0]) + 200,
                            (short) (new Camera(0, 0, 1d).projection(origami.vertices_2d().get(origami.polygons().get(i).get(ii)))[1]) + 200);
                }
                canvas.setColor(new Color(i));
                canvas.fillPolygon(ut);
            }
        }

        int[] nyers = ((java.awt.image.DataBufferInt) terkep.getRaster().getDataBuffer()).getData();

        int len = texture.getHeight() * texture.getWidth();
        int sor = texture.getWidth();
        space_buffer = new double[len][];

        for (int i = 0; i < len; i++) {

            int szin = nyers[i] & 0xFFFFFF;
            if (szin != 0xFFFFFF) {

                try {
                    double x_1 = origami.vertices_2d().get(vaz[szin][1])[0] - origami.vertices_2d().get(vaz[szin][0])[0];
                    double x_2 = origami.vertices_2d().get(vaz[szin][1])[1] - origami.vertices_2d().get(vaz[szin][0])[1];
                    double y_1 = origami.vertices_2d().get(vaz[szin][2])[0] - origami.vertices_2d().get(vaz[szin][0])[0];
                    double y_2 = origami.vertices_2d().get(vaz[szin][2])[1] - origami.vertices_2d().get(vaz[szin][0])[1];
                    double a_1 = (double) (i % sor) - origami.vertices_2d().get(vaz[szin][0])[0];
                    double a_2 = (double) i / sor - origami.vertices_2d().get(vaz[szin][0])[1];

                    double lambda1 = (a_1 * y_2 - a_2 * y_1) / (x_1 * y_2 - x_2 * y_1);
                    double lambda2 = (a_1 * x_2 - a_2 * x_1) / (y_1 * x_2 - y_2 * x_1);
                    double[] v3d1 = Origami.vector(origami.vertices.get(vaz[szin][1]), origami.vertices.get(vaz[szin][0]));
                    double[] v3d2 = Origami.vector(origami.vertices.get(vaz[szin][0]), origami.vertices.get(vaz[szin][2]));
                    space_buffer[i] = Origami.vector(Origami.vector(Origami.scalar_multip(v3d1, lambda1), Origami.scalar_multip(v3d2, lambda2)), Origami.scalar_multip(origami.vertices.get(vaz[szin][0]), -1));
                } catch (Exception ex) {
                }
            }
        }

        new Camera(200, 200, 1d).drawCreasePattern(texture.createGraphics(), Color.BLACK, origami);
    }

    public void drawTexture(Graphics canvas, int w, int h) {

        byte[] nyers = ((java.awt.image.DataBufferByte) texture.getRaster().getDataBuffer()).getData();
        Double[][] depth_buffer = new Double[w][h];
        java.awt.image.BufferedImage ki = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D bleach = ki.createGraphics();
        bleach.setBackground(Color.WHITE);
        bleach.clearRect(0, 0, w, h);

        for (int i = 0; i < nyers.length; i += 3) {

            int szin = (nyers[i] & 0xFF) + ((nyers[i + 1] & 0xFF) << 8) + ((nyers[i + 2] & 0xFF) << 16);
            kiserlet:
            try {
                double[] pont = space_buffer[i / 3];
                if (pont == null) {
                    break kiserlet;
                }
                double[] vet = projection(pont);
                short vetX = (short) (vet[0] + xshift);
                short vetY = (short) (vet[1] + yshift);
                if (vetX >= 0 && vetX < depth_buffer.length && vetY >= 0 && vetY < depth_buffer[0].length) {
                    if (depth_buffer[vetX][vetY] == null || Origami.scalar_product(pont, camera_dir) > depth_buffer[vetX][vetY]) {

                        depth_buffer[vetX][vetY] = Origami.scalar_product(pont, camera_dir);
                        ki.setRGB(vetX, vetY, szin);
                    }
                }
            } catch (Exception ex) {
            }
        }
        canvas.drawImage(ki, 0, 0, null);
    }
}