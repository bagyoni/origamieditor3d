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
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents a three-dimensional rigid origami model consisting
 * of convex polygonal faces. Provides methods to manipulate the
 * model by intersecting it with a given plane, and then
 * transforming one or more connected parts of the paper that are
 * bound by this plane, by reflection or rotation.
 *
 * @author Attila Bágyoni <bagyoni.attila@gmail.com>
 * @since 2013-01-14
 */
public class Origami {

    /**
     * Létrehozza az {@linkplain Origami} osztály egy új példányát üres
     * {@link #history} listával és a paraméterként megadott papírmérettel.
     *
     * @param papertype	Az új objektum {@link #papertype()}-e.
     */
    public Origami(PaperType papertype) {

        vertices = (vertices2d = new ArrayList<>(Arrays.asList(new double[][]{})));
        vertices_size = 0;
        polygons = new ArrayList<>();
        polygons_size = 0;
        history = new ArrayList<>(Arrays.asList(new double[][]{}));
        history_pointer = 0;
        this.papertype = papertype;
        reset();
    }

    /**
     * Létrehozza az {@linkplain Origami} osztály egy új példányát a
     * paraméterként megadott {@link #history} listával és papírmérettel.
     *
     * @param papir	Az új példány {@link #papertype()}-e.
     */
    public Origami(PaperType papertype, ArrayList<double[]> history) {

        vertices = (vertices2d = new ArrayList<>(Arrays.asList(new double[][]{})));
        vertices_size = 0;
        polygons = new ArrayList<>();
        polygons_size = 0;
        this.history = history;
        history_pointer = this.history.size();
        this.papertype = papertype;
        reset();
    }

    /**
     * Létrehozza az {@linkplain Origami} osztály egy új példányát üres
     * {@link #history} listával, a paraméterként megadott lista
     * {@link #ccwWindingOrder(java.util.ArrayList)}-ának megfelelő
     * {@link #corners()} listával és {@link PaperType.Custom} értékű
     * {@link #papir()} mezővel.
     *
     */
    public Origami(ArrayList<double[]> corners) throws Exception {

        vertices = (vertices2d = new ArrayList<>(Arrays.asList(new double[][]{})));
        vertices_size = 0;
        polygons = new ArrayList<>();
        polygons_size = 0;
        history = new ArrayList<>(Arrays.asList(new double[][]{}));
        history_pointer = 0;
        papertype = PaperType.Custom;
        this.corners = ccwWindingOrder(corners);
        if (!isConvex(this.corners)) {
            throw new Exception("Varatlan konkav sokszog/Unexpected concave polygon");
        }
        reset();
    }

    /**
     * Létrehozza az {@linkplain Origami} osztály egy új példányát a
     * paraméterként megadott {@link #history} listával, a paraméterként
     * megadott lista {@link #ccwWindingOrder(java.util.ArrayList)}-ának
     * megfelelő {@link #corners()} listával és {@link PaperType.Custom} értékű
     * {@link #papir()} mezővel.
     *
     */
    public Origami(ArrayList<double[]> corners, ArrayList<double[]> history) throws Exception {

        vertices = (vertices2d = new ArrayList<>(Arrays.asList(new double[][]{})));
        vertices_size = 0;
        polygons = new ArrayList<>();
        polygons_size = 0;
        this.history = history;
        history_pointer = this.history.size();
        papertype = PaperType.Custom;
        this.corners = ccwWindingOrder(corners);
        if (!isConvex(this.corners)) {
            throw new Exception("Varatlan konkav sokszog/Unexpected concave polygon");
        }
        reset();
    }

    protected ArrayList<double[]> vertices = new ArrayList<>(Arrays.asList(new double[][]{}));

    /**
     * @return Az origami modell csúcsait reprezentáló lista. Minden eleme egy,
     * az origami egy csúcsának térbeli derékszögû koordinátáiból álló tömb.
     */
    public ArrayList<double[]> vertices() {
        return vertices;
    }

    protected int vertices_size = 0;

    /**
     * @return A {@link #vertices} lista elemszáma. Kívülrôl érkezô hívás esetén
     * mindig egyenlô {@link #vertices}{@code .size()}-zal.
     */
    public int vertices_size() {
        return vertices_size;
    }

    protected ArrayList<ArrayList<Integer>> polygons = new ArrayList<>();

    /**
     * @return Az origami modell lapjait reprezentáló lista. Minden eleme egy,
     * az {@linkplain Origami} egy lapját alkotó csúcsok {@link vertices()}-beli
     * indexeit pozitív vagy negatív körüljárás szerint felsoroló lista.
     * <p> Az {@linkplain Origami} osztály által nyújtott eljárások garantálják,
     * hogy minden lap egy konvex sokszög (akárcsak a valódi origamiban), amit
     * bizonyos privát eljárások ki is használnak.
     */
    public ArrayList<ArrayList<Integer>> polygons() {
        return polygons;
    }

    protected int polygons_size = 0;

    public int polygons_size() {
        return polygons_size;
    }

    protected ArrayList<double[]> history = new ArrayList<>(Arrays.asList(new double[][]{}));

    public ArrayList<double[]> history() {
        return history;
    }

    protected int history_pointer;
    public int history_pointer() {
    	return history_pointer;
    }

    /**
     * A(z) {@linkplain Origami#reset()} metódusban elérhetô papírméretek
     * megkülönböztetésére szolgál.
     * <p> Lehetséges értékei: {@link #A4}, {@link #Square}, {@link #Hexagon}, {@link #Dollar},
     * {@link #Forint}.
     */
    public enum PaperType {

        A4('A'), Square('N'), Hexagon('H'), Dollar('D'), Forint('F'), Custom('E');
        final private char ID;
        final static private HashMap<Character, PaperType> allid = new HashMap<>();

        static {

            for (PaperType p : PaperType.values()) {

                allid.put(p.ID, p);
            }
        }

        private PaperType(final char c) {

            ID = c;
        }

        /**
         * Visszaadja a papírméretet egyértelműen azonosító karaktert.
         *
         * @return	A papírmérethez rendelt karakter (általában a papírméret
         * nevének elsô karaktere).
         */
        public char toChar() {

            return ID;
        }

        /**
         * Visszaadja a paraméterként megadott karakter által reprezentált
         * papírméretet.
         *
         * @param c	A papírmérethez rendelt karakter (általában a papírméret
         * nevének elsô karaktere).
         * @return	A paraméter által reprezentált enum konstans.
         */
        static public PaperType forChar(char c) {

            return allid.get(c);
        }

        /**
         * Visszaadja this enum konstans közérthetô leírását felhasználói
         * olvasásra.
         */
        @Override
        public String toString() throws NullPointerException {

            if (super.equals(A4)) {

                return "A4";
            } else if (super.equals(Square)) {

                return "Square";
            } else if (super.equals(Hexagon)) {

                return "Regular hexagon";
            } else if (super.equals(Dollar)) {

                return "Dollar bill";
            } else if (super.equals(Forint)) {

                return "Forint bill";
            } else if (super.equals(Custom)) {

                return "Custom";
            } else {

                throw new NullPointerException();
            }
        }
    }
    private PaperType papertype = PaperType.Square;

    public PaperType papertype() {
        return papertype;
    }
    protected ArrayList<double[]> corners = new ArrayList<>(Arrays.asList(new double[][]{}));

    public ArrayList<double[]> corners() {
        return corners;
    }
    protected ArrayList<double[]> vertices2d = new ArrayList<>(Arrays.asList(new double[][]{}));

    public ArrayList<double[]> vertices_2d() {
        return vertices2d;
    }

    protected void addVertex(double[] point) {
        vertices.add(point);
        vertices_size++;
    }

    protected void add2dVertex(double[] point) {
        vertices2d.add(point);
    }

    protected void addPolygon(ArrayList<Integer> polygon) {
        polygons.add(polygon);
        polygons_size++;
    }

    protected void removePolygon(int polygonIndex) {
        polygons.remove(polygonIndex);
        polygons_size--;
    }

    static public boolean plane_between_points(double[] ppoint, double[] pnormal, double[] A, double[] B) {

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        boolean innen = false, tul = false;

        if ((A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) > 0.00000001) {
            innen = true;
        }
        if ((A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) < -0.00000001) {
            tul = true;
        }
        if ((B[0] * pnormal[0] + B[1] * pnormal[1] + B[2] * pnormal[2] - konst) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) > 0.00000001) {
            innen = true;
        }
        if ((B[0] * pnormal[0] + B[1] * pnormal[1] + B[2] * pnormal[2] - konst) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) < -0.00000001) {
            tul = true;
        }

        return innen && tul;
    }

    static public boolean point_on_plane(double[] ppoint, double[] pnormal, double[] A) {

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        return (Math.abs(A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) < 1);
    }
    final static public double[] nullvektor = new double[]{0, 0, 0};

    static public double[] vector_product(double[] v1, double[] v2) {

        return new double[]{(v1[1] * v2[2] - v1[2] * v2[1]), (v1[2] * v2[0] - v1[0] * v2[2]), (v1[0] * v2[1] - v1[1] * v2[0])};
    }

    static public double scalar_product(double[] v1, double[] v2) {

        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    static public double[] scalar_multip(double[] v, double lambda) {

        return new double[]{v[0]*lambda, v[1]*lambda, v[2]*lambda};
    }

    static public double[] vector(double[] A, double[] B) {

        if (A.length == 3 && B.length == 3) {

            return new double[]{A[0] - B[0], A[1] - B[1], A[2] - B[2]};
        }

        return new double[]{A[0] - B[0], A[1] - B[1], 0};
    }

    static public double[] midpoint(double[] A, double[] B) {

        return new double[]{(A[0] + B[0]) / 2, (A[1] + B[1]) / 2, (A[2] + B[2]) / 2};
    }

    static public double[] length_to_100(double[] v) {

        double hossz = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) * 0.01;
        return new double[]{v[0] / hossz, v[1] / hossz, v[2] / hossz};
    }

    static public double vector_length(double[] v) {

        double hossz = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return hossz;
    }

    static public double angle(double[] v1, double[] v2) {

        if (vector_length(v1) > 0 && vector_length(v2) > 0) {

            double arg1 = Math.acos(v1[0] / vector_length(v1));
            if (v1[1] < 0) {
                arg1 = 2 * Math.PI - arg1;
            }
            double arg2 = Math.acos(v2[0] / vector_length(v2));
            if (v2[1] < 0) {
                arg2 = 2 * Math.PI - arg2;
            }

            double szog = arg1 - arg2;
            while (szog < 0) {
                szog += 2 * Math.PI;
            }
            while (szog > 2 * Math.PI) {
                szog -= 2 * Math.PI;
            }

            return szog;

        }
        return 0;
    }

    public boolean isNonDegenerate(int polygonIndex) {

        if (polygons.get(polygonIndex).size() > 1) {
            for (int pontazon : polygons.get(polygonIndex)) {
                if (vector_length(vector(vertices.get(pontazon), vertices.get(polygons.get(polygonIndex).get(0)))) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isCut(double[] ppoint, double[] pnormal, int polygonIndex) {

        if (isNonDegenerate(polygonIndex)) {

            boolean egyik = false, masik = false;
            for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {
                if (scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) > scalar_product(ppoint, pnormal) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) + 0.00000001) {
                    egyik = true;
                } else if (scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) < scalar_product(ppoint, pnormal) / Math.sqrt(Math.max(scalar_product(pnormal, pnormal), 1)) - 0.00000001) {
                    masik = true;
                }
                if (egyik && masik) {
                    return true;
                }
            }
        }
        return false;
    }

    protected ArrayList<int[]> cutpolygon_nodes = new ArrayList<>(Arrays.asList(new int[][]{}));
    protected ArrayList<int[]> cutpolygon_pairs = new ArrayList<>(Arrays.asList(new int[][]{}));
    protected ArrayList<ArrayList<Integer>> last_cut_polygons = new ArrayList<>();

    protected boolean cutPolygon(double[] ppoint, double[] pnormal, int polygonIndex) {

        if (isNonDegenerate(polygonIndex)) {

            ArrayList<Integer> ujsokszog1 = new ArrayList<>();
            ArrayList<Integer> ujsokszog2 = new ArrayList<>();

            for (int i = 0; i < polygons.get(polygonIndex).size() - 1; i++) {

                if (point_on_plane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)))) {

                    ujsokszog1.add(polygons.get(polygonIndex).get(i));
                    ujsokszog2.add(polygons.get(polygonIndex).get(i));
                } else {

                    if (scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) > scalar_product(ppoint, pnormal)) {
                        ujsokszog1.add(polygons.get(polygonIndex).get(i));
                    } else {
                        ujsokszog2.add(polygons.get(polygonIndex).get(i));
                    }

                    if (plane_between_points(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)), vertices.get(polygons.get(polygonIndex).get(i + 1)))) {

                        freshcut:
                        {
                            for (int[] szakasz : cutpolygon_nodes) {
                                if (szakasz[0] == polygons.get(polygonIndex).get(i) && szakasz[1] == polygons.get(polygonIndex).get(i + 1)) {
                                    ujsokszog1.add(szakasz[2]);
                                    ujsokszog2.add(szakasz[2]);
                                    break freshcut;
                                } else if (szakasz[0] == polygons.get(polygonIndex).get(i + 1) && szakasz[1] == polygons.get(polygonIndex).get(i)) {
                                    ujsokszog1.add(szakasz[2]);
                                    ujsokszog2.add(szakasz[2]);
                                    break freshcut;
                                }
                            }
                            double D = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

                            double[] iranyvek = vector(vertices.get(polygons.get(polygonIndex).get(i)), vertices.get(polygons.get(polygonIndex).get(i + 1)));
                            double X = vertices.get(polygons.get(polygonIndex).get(i))[0];
                            double Y = vertices.get(polygons.get(polygonIndex).get(i))[1];
                            double Z = vertices.get(polygons.get(polygonIndex).get(i))[2];
                            double U = iranyvek[0];
                            double V = iranyvek[1];
                            double W = iranyvek[2];
                            double A = pnormal[0];
                            double B = pnormal[1];
                            double C = pnormal[2];
                            double t = -(A * X + B * Y + C * Z - D) / (A * U + B * V + C * W);

                            double[] metszet = new double[]{X + t * U, Y + t * V, Z + t * W};
                            addVertex(metszet);

                            double suly1 = vector_length(vector(metszet, vertices.get(polygons.get(polygonIndex).get(i + 1))));
                            double suly2 = vector_length(vector(metszet, vertices.get(polygons.get(polygonIndex).get(i))));
                            add2dVertex(new double[]{
                                (vertices2d.get(polygons.get(polygonIndex).get(i))[0] * suly1 + vertices2d.get(polygons.get(polygonIndex).get(i + 1))[0] * suly2) / (suly1 + suly2),
                                (vertices2d.get(polygons.get(polygonIndex).get(i))[1] * suly1 + vertices2d.get(polygons.get(polygonIndex).get(i + 1))[1] * suly2) / (suly1 + suly2),
                                0
                            });

                            ujsokszog1.add(vertices_size - 1);
                            ujsokszog2.add(vertices_size - 1);
                            cutpolygon_nodes.add(new int[]{polygons.get(polygonIndex).get(i), polygons.get(polygonIndex).get(i + 1), vertices_size - 1});
                        }
                    }
                }
            }

            if (point_on_plane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1)))) {

                ujsokszog1.add(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1));
                ujsokszog2.add(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1));
            } else {

                if (scalar_product(vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1)), pnormal) > scalar_product(ppoint, pnormal)) {
                    ujsokszog1.add(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1));
                } else {
                    ujsokszog2.add(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1));
                }

                if (plane_between_points(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1)), vertices.get(polygons.get(polygonIndex).get(0)))) {

                    freshcut:
                    {
                        for (int[] szakasz : cutpolygon_nodes) {
                            if (szakasz[0] == polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1) && szakasz[1] == polygons.get(polygonIndex).get(0)) {
                                ujsokszog1.add(szakasz[2]);
                                ujsokszog2.add(szakasz[2]);
                                break freshcut;
                            } else if (szakasz[0] == polygons.get(polygonIndex).get(0) && szakasz[1] == polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1)) {
                                ujsokszog1.add(szakasz[2]);
                                ujsokszog2.add(szakasz[2]);
                                break freshcut;
                            }
                        }
                        double D = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

                        double[] iranyvek = vector(vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1)), vertices.get(polygons.get(polygonIndex).get(0)));
                        double X = vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1))[0];
                        double Y = vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1))[1];
                        double Z = vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1))[2];
                        double U = iranyvek[0];
                        double V = iranyvek[1];
                        double W = iranyvek[2];
                        double A = pnormal[0];
                        double B = pnormal[1];
                        double C = pnormal[2];
                        double t = -(A * X + B * Y + C * Z - D) / (A * U + B * V + C * W);

                        double[] metszet = new double[]{X + t * U, Y + t * V, Z + t * W};
                        addVertex(metszet);

                        double suly1 = vector_length(vector(metszet, vertices.get(polygons.get(polygonIndex).get(0))));
                        double suly2 = vector_length(vector(metszet, vertices.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1))));
                        add2dVertex(new double[]{
                            (vertices2d.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1))[0] * suly1 + vertices2d.get(polygons.get(polygonIndex).get(0))[0] * suly2) / (suly1 + suly2),
                            (vertices2d.get(polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1))[1] * suly1 + vertices2d.get(polygons.get(polygonIndex).get(0))[1] * suly2) / (suly1 + suly2),
                            0
                        });

                        ujsokszog1.add(vertices_size - 1);
                        ujsokszog2.add(vertices_size - 1);
                        cutpolygon_nodes.add(new int[]{polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1), polygons.get(polygonIndex).get(0), vertices_size - 1});
                    }
                }
            }
            if (isCut(ppoint, pnormal, polygonIndex)) {

                cutpolygon_pairs.add(new int[]{polygonIndex, polygons.size()});
                last_cut_polygons.add(polygons.get(polygonIndex));
                polygons.set(polygonIndex, ujsokszog1);
                addPolygon(ujsokszog2);
                return true;
            }
            return false;
        }
        return false;
    }

    public ArrayList<Integer> polygonSelect(double[] ppoint, double[] pnormal, int polygonIndex) {

        ArrayList<Integer> kijeloles = new ArrayList<>(Arrays.asList(new Integer[]{}));
        kijeloles.add(polygonIndex);
        for (int i = 0; i < kijeloles.size(); i++) {

            int tag = kijeloles.get(i);
            for (int ii = 0; ii < polygons_size; ii++) {

                if (!kijeloles.contains(ii)) {

                    for (int tagpont : polygons.get(tag)) {

                        if (polygons.get(ii).contains(tagpont)) {
                            if (!point_on_plane(ppoint, pnormal, vertices.get(tagpont))) {
                                kijeloles.add(ii);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return kijeloles;
    }

    public double[] polygonCenter(int polygonIndex) {

        double[] vissza = new double[]{Double.NaN, Double.NaN, Double.NaN};
        if (isNonDegenerate(polygonIndex)) {

            vissza = new double[]{0d, 0d, 0d};
            for (int ind : polygons.get(polygonIndex)) {
                vissza = new double[]{vissza[0] + vertices.get(ind)[0], vissza[1] + vertices.get(ind)[1], vissza[2] + vertices.get(ind)[2]};
            }

            java.util.Random eltolas = new java.util.Random(polygonIndex);
            vissza = new double[]{vissza[0] / polygons.get(polygonIndex).size() + eltolas.nextDouble() * 10 - 5, vissza[1] / polygons.get(polygonIndex).size() + eltolas.nextDouble() * 10 - 5, vissza[2] / polygons.get(polygonIndex).size() + eltolas.nextDouble() * 10 - 5};
        }
        return vissza;
    }

    //HAJT, HORPASZT
    protected void internalReflectionFold(double[] ppoint, double[] pnormal) {

        shrink();

        cutpolygon_nodes = new ArrayList<>(Arrays.asList(new int[][]{}));
        cutpolygon_pairs = new ArrayList<>(Arrays.asList(new int[][]{}));
        last_cut_polygons = new ArrayList<>();
        int lapszam = polygons_size;
        for (int i = 0; i < lapszam; i++) {
            if (isNonDegenerate(i)) {
                cutPolygon(ppoint, pnormal, i);
            }
        }

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        for (int i = 0; i < vertices_size; i++) {

            double[] ipont = vertices.get(i);
            if (ipont[0] * pnormal[0] + ipont[1] * pnormal[1] + ipont[2] * pnormal[2] - konst > 0) {

                double[] iranyvek = pnormal;
                double X = ipont[0];
                double Y = ipont[1];
                double Z = ipont[2];
                double U = iranyvek[0];
                double V = iranyvek[1];
                double W = iranyvek[2];
                double A = pnormal[0];
                double B = pnormal[1];
                double C = pnormal[2];
                double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);

                double[] talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
                double[] kep = new double[]{talppont[0] + vector(talppont, ipont)[0], talppont[1] + vector(talppont, ipont)[1], talppont[2] + vector(talppont, ipont)[2]};
                vertices.set(i, kep);
            }
        }
    }

    protected int internalRotationFold(double[] ppoint, double[] pnormal, int phi) {

        shrink();

        cutpolygon_nodes = new ArrayList<>(Arrays.asList(new int[][]{}));
        cutpolygon_pairs = new ArrayList<>(Arrays.asList(new int[][]{}));
        last_cut_polygons = new ArrayList<>();
        int lapszam = polygons_size;
        for (int i = 0; i < lapszam; i++) {
            if (isNonDegenerate(i)) {
                cutPolygon(ppoint, pnormal, i);
            }
        }

        ArrayList<Integer> hajtopontok = new ArrayList<>(Arrays.asList(new Integer[]{}));
        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

        for (int i = 0; i < vertices_size; i++) {

            double[] ipont = vertices.get(i);
            if (point_on_plane(ppoint, pnormal, ipont)) {
                hajtopontok.add(i);
            }
        }

        boolean collin = false;
        int maspont = -1;
        double tavolsagmax = -1;

        if (hajtopontok.size() >= 2) {
            for (int hp : hajtopontok) {
                if (vector_length(vector(vertices.get(hp), vertices.get(hajtopontok.get(0)))) > 0) {
                    collin = true;
                    if (vector_length(vector(vertices.get(hp), vertices.get(hajtopontok.get(0)))) > tavolsagmax) {
                        maspont = hp;
                        tavolsagmax = vector_length(vector(vertices.get(hp), vertices.get(hajtopontok.get(0))));
                    }
                }
            }
        }

        for (int i = 1; i < hajtopontok.size() && i != maspont; i++) {

            if (vector_length(vector_product(vector(vertices.get(hajtopontok.get(0)), vertices.get(hajtopontok.get(i))), vector(vertices.get(maspont), vertices.get(hajtopontok.get(i))))) > vector_length(vector(vertices.get(hajtopontok.get(0)), vertices.get(maspont)))) {
            	collin = false;
                break;
            }
        }

        if (collin) {

            double[] iranyvek = vector(vertices.get(hajtopontok.get(0)), vertices.get(maspont));
            double Cx = iranyvek[0] / Math.pow(iranyvek[0] * iranyvek[0] + iranyvek[1] * iranyvek[1] + iranyvek[2] * iranyvek[2], 0.5);
            double Cy = iranyvek[1] / Math.pow(iranyvek[0] * iranyvek[0] + iranyvek[1] * iranyvek[1] + iranyvek[2] * iranyvek[2], 0.5);
            double Cz = iranyvek[2] / Math.pow(iranyvek[0] * iranyvek[0] + iranyvek[1] * iranyvek[1] + iranyvek[2] * iranyvek[2], 0.5);
            double sinphi = Math.sin((double) phi * Math.PI / 180);
            double cosphi = Math.cos((double) phi * Math.PI / 180);

            for (int i = 0; i < vertices_size; i++) {

                double[] ipont = vertices.get(i);
                if (ipont[0] * pnormal[0] + ipont[1] * pnormal[1] + ipont[2] * pnormal[2] - konst > 0) {

                    double X = ipont[0] - vertices.get(hajtopontok.get(0))[0];
                    double Y = ipont[1] - vertices.get(hajtopontok.get(0))[1];
                    double Z = ipont[2] - vertices.get(hajtopontok.get(0))[2];

                    double kepX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi) + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
                    double kepY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi)) + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
                    double kepZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi) + Z * (cosphi + Cz * Cz * (1 - cosphi));

                    double[] kep = new double[]{kepX + vertices.get(hajtopontok.get(0))[0], kepY + vertices.get(hajtopontok.get(0))[1], kepZ + vertices.get(hajtopontok.get(0))[2]};
                    vertices.set(i, kep);
                }
            }
            return 0;
        } else if (phi != 0) {
            undo(1);
            return 1;
        } else {
            return 1;
        }
    }

    protected void internalReflectionFold(double[] ppoint, double[] pnormal, int polygonIndex) {

        ArrayList<Integer> kijeloles = polygonSelect(ppoint, pnormal, polygonIndex);

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        for (int i = 0; i < vertices_size; i++) {

            for (int tag : kijeloles) {

                if (polygons.get(tag).contains(i)) {

                    double[] ipont = vertices.get(i);

                    double[] iranyvek = pnormal;
                    double X = ipont[0];
                    double Y = ipont[1];
                    double Z = ipont[2];
                    double U = iranyvek[0];
                    double V = iranyvek[1];
                    double W = iranyvek[2];
                    double A = pnormal[0];
                    double B = pnormal[1];
                    double C = pnormal[2];
                    double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);

                    double[] talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
                    double[] kep = new double[]{talppont[0] + vector(talppont, ipont)[0], talppont[1] + vector(talppont, ipont)[1], talppont[2] + vector(talppont, ipont)[2]};
                    vertices.set(i, kep);
                    break;
                }
            }
        }

        for (int i = 0; i < cutpolygon_pairs.size(); i++) {

            if (!(kijeloles.contains(cutpolygon_pairs.get(i)[0]) || kijeloles.contains(cutpolygon_pairs.get(i)[1]))) {
                polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
            }
        }

        for (int[] par : cutpolygon_pairs) {

            if (!(kijeloles.contains(par[0]) || kijeloles.contains(par[1]))) {
                polygons.set(par[1], new ArrayList<Integer>());
            }
        }

        cutpolygon_pairs = new ArrayList<>(Arrays.asList(new int[][]{}));
        last_cut_polygons = new ArrayList<>();

        shrink(polygonIndex);
    }

    protected void internalRotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex) {

        ArrayList<Integer> kijeloles = polygonSelect(ppoint, pnormal, polygonIndex);

        ArrayList<Integer> hajtopontok = new ArrayList<>(Arrays.asList(new Integer[]{}));

        for (int i = 0; i < vertices_size; i++) {

            double[] ipont = vertices.get(i);

            if (point_on_plane(ppoint, pnormal, ipont)) {
                for (int tag : kijeloles) {
                    if (polygons.get(tag).contains(i)) {
                        hajtopontok.add(i);
                        break;
                    }
                }
            }
        }

        boolean collin = false;
        int maspont = -1;
        double tavolsagmax = -1;

        if (hajtopontok.size() >= 2) {
            for (int hp : hajtopontok) {
                if (vector_length(vector(vertices.get(hp), vertices.get(hajtopontok.get(0)))) > 0) {
                    collin = true;
                    if (vector_length(vector(vertices.get(hp), vertices.get(hajtopontok.get(0)))) > tavolsagmax) {
                        maspont = hp;
                        tavolsagmax = vector_length(vector(vertices.get(hp), vertices.get(hajtopontok.get(0))));
                    }
                }
            }
        }

        for (int i = 1; i < hajtopontok.size() && i != maspont; i++) {

            if (vector_length(vector_product(vector(vertices.get(hajtopontok.get(0)), vertices.get(hajtopontok.get(i))), vector(vertices.get(maspont), vertices.get(hajtopontok.get(i))))) > vector_length(vector(vertices.get(hajtopontok.get(0)), vertices.get(maspont)))) {
            	collin = false;
                break;
            }
        }

        if (collin) {

            double[] iranyvek = vector(vertices.get(hajtopontok.get(0)), vertices.get(maspont));
            double Cx = iranyvek[0] / Math.pow(iranyvek[0] * iranyvek[0] + iranyvek[1] * iranyvek[1] + iranyvek[2] * iranyvek[2], 0.5);
            double Cy = iranyvek[1] / Math.pow(iranyvek[0] * iranyvek[0] + iranyvek[1] * iranyvek[1] + iranyvek[2] * iranyvek[2], 0.5);
            double Cz = iranyvek[2] / Math.pow(iranyvek[0] * iranyvek[0] + iranyvek[1] * iranyvek[1] + iranyvek[2] * iranyvek[2], 0.5);
            double sinphi = Math.sin((double) phi * Math.PI / 180);
            double cosphi = Math.cos((double) phi * Math.PI / 180);

            for (int i = 0; i < vertices_size; i++) {

                for (int tag : kijeloles) {

                    if (polygons.get(tag).contains(i)) {

                        double[] ipont = vertices.get(i);

                        double X = ipont[0] - vertices.get(hajtopontok.get(0))[0];
                        double Y = ipont[1] - vertices.get(hajtopontok.get(0))[1];
                        double Z = ipont[2] - vertices.get(hajtopontok.get(0))[2];

                        double kepX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi) + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
                        double kepY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi)) + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
                        double kepZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi) + Z * (cosphi + Cz * Cz * (1 - cosphi));

                        double[] kep = new double[]{kepX + vertices.get(hajtopontok.get(0))[0], kepY + vertices.get(hajtopontok.get(0))[1], kepZ + vertices.get(hajtopontok.get(0))[2]};
                        vertices.set(i, kep);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < cutpolygon_pairs.size(); i++) {

            if (!(kijeloles.contains(cutpolygon_pairs.get(i)[0]) || kijeloles.contains(cutpolygon_pairs.get(i)[1]))) {
                polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
            }

        }

        for (int[] par : cutpolygon_pairs) {

            if (!(kijeloles.contains(par[0]) || kijeloles.contains(par[1]))) {

                polygons.set(par[1], new ArrayList<Integer>());
            }
        }

        cutpolygon_pairs = new ArrayList<>(Arrays.asList(new int[][]{}));
        last_cut_polygons = new ArrayList<>();

        shrink(polygonIndex);
    }

    public void reflectionFold(double[] ppoint, double[] pnormal) {

        //naplózás
        history.subList(history_pointer, history.size()).clear();
        history.add(new double[]{
            1,
            ppoint[0],
            ppoint[1],
            ppoint[2],
            pnormal[0],
            pnormal[1],
            pnormal[2]
        });
        history_pointer++;
        //horpasztás
        internalReflectionFold(ppoint, pnormal);
    }

    public int rotationFold(double[] ppoint, double[] pnormal, int phi) {

        //naplózás
        history.subList(history_pointer, history.size()).clear();
        history.add(new double[]{
            2,
            ppoint[0],
            ppoint[1],
            ppoint[2],
            pnormal[0],
            pnormal[1],
            pnormal[2],
            (double) phi
        });
        history_pointer++;
        //hajtás
        return internalRotationFold(ppoint, pnormal, phi);
    }

    public void reflectionFold(double[] ppoint, double[] pnormal, int polygonIndex) {

        //naplózás
        history.subList(history_pointer, history.size()).clear();
        history.add(new double[]{
            3,
            ppoint[0],
            ppoint[1],
            ppoint[2],
            pnormal[0],
            pnormal[1],
            pnormal[2],
            (double) polygonIndex
        });
        history_pointer++;
        //horpasztás
        internalReflectionFold(ppoint, pnormal, polygonIndex);
    }

    public void rotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex) {

        //naplózás
        history.subList(history_pointer, history.size()).clear();
        history.add(new double[]{
            4,
            ppoint[0],
            ppoint[1],
            ppoint[2],
            pnormal[0],
            pnormal[1],
            pnormal[2],
            (double) phi,
            (double) polygonIndex
        });
        history_pointer++;
        //hajtás
        internalRotationFold(ppoint, pnormal, phi, polygonIndex);
    }

    public void crease(double[] ppoint, double[] pnormal) {

        //naplózás
        history.subList(history_pointer, history.size()).clear();
        history.add(new double[]{
            5,
            ppoint[0],
            ppoint[1],
            ppoint[2],
            pnormal[0],
            pnormal[1],
            pnormal[2]
        });
        history_pointer++;
        //"hajtás"
        internalRotationFold(ppoint, pnormal, 0);
    }

    /**
     * {@link #papertype()} értékétôl függôen újrainicializálja this origami
     * {@link #corners()}, {@link #vertices()} és {@link #polygons()} listáit.
     */
    @SuppressWarnings("unchecked")
    public final void reset() {

        if (papertype == PaperType.A4) {

            vertices_size = 0;
            vertices.clear();
            addVertex(new double[]{0, 0, 0});
            addVertex(new double[]{424.3, 0, 0});
            addVertex(new double[]{424.3, 300, 0});
            addVertex(new double[]{0, 300, 0});
            vertices2d = (ArrayList<double[]>) vertices.clone();
            polygons_size = 0;
            polygons.clear();
            ArrayList<Integer> sokszog0 = new ArrayList<>();
            sokszog0.add(0);
            sokszog0.add(1);
            sokszog0.add(2);
            sokszog0.add(3);
            addPolygon(sokszog0);
            corners = (ArrayList<double[]>) vertices.clone();
        }

        if (papertype == PaperType.Square) {

            vertices_size = 0;
            vertices.clear();
            addVertex(new double[]{0, 0, 0});
            addVertex(new double[]{400, 0, 0});
            addVertex(new double[]{400, 400, 0});
            addVertex(new double[]{0, 400, 0});
            vertices2d = (ArrayList<double[]>) vertices.clone();
            polygons_size = 0;
            polygons.clear();
            ArrayList<Integer> sokszog0 = new ArrayList<>();
            sokszog0.add(0);
            sokszog0.add(1);
            sokszog0.add(2);
            sokszog0.add(3);
            addPolygon(sokszog0);
            corners = (ArrayList<double[]>) vertices.clone();
        }

        if (papertype == PaperType.Hexagon) {

            vertices_size = 0;
            vertices.clear();
            addVertex(new double[]{300, 346.41, 0});
            addVertex(new double[]{400, 173.205, 0});
            addVertex(new double[]{300, 0, 0});
            addVertex(new double[]{100, 0, 0});
            addVertex(new double[]{0, 173.205, 0});
            addVertex(new double[]{100, 346.41, 0});
            vertices2d = (ArrayList<double[]>) vertices.clone();
            polygons_size = 0;
            polygons.clear();
            ArrayList<Integer> sokszog0 = new ArrayList<>();
            sokszog0.add(5);
            sokszog0.add(4);
            sokszog0.add(3);
            sokszog0.add(2);
            sokszog0.add(1);
            sokszog0.add(0);
            addPolygon(sokszog0);
            corners = (ArrayList<double[]>) vertices.clone();
        }

        if (papertype == PaperType.Dollar) {

            vertices_size = 0;
            vertices.clear();
            addVertex(new double[]{0, 0, 0});
            addVertex(new double[]{400, 0, 0});
            addVertex(new double[]{400, 170, 0});
            addVertex(new double[]{0, 170, 0});
            vertices2d = (ArrayList<double[]>) vertices.clone();
            polygons_size = 0;
            polygons.clear();
            ArrayList<Integer> sokszog0 = new ArrayList<>();
            sokszog0.add(0);
            sokszog0.add(1);
            sokszog0.add(2);
            sokszog0.add(3);
            addPolygon(sokszog0);
            corners = (ArrayList<double[]>) vertices.clone();
        }

        if (papertype == PaperType.Forint) {

            vertices_size = 0;
            vertices.clear();
            addVertex(new double[]{0, 0, 0});
            addVertex(new double[]{400, 0, 0});
            addVertex(new double[]{400, 181.82, 0});
            addVertex(new double[]{0, 181.82, 0});
            vertices2d = (ArrayList<double[]>) vertices.clone();
            polygons_size = 0;
            polygons.clear();
            ArrayList<Integer> sokszog0 = new ArrayList<>();
            sokszog0.add(0);
            sokszog0.add(1);
            sokszog0.add(2);
            sokszog0.add(3);
            addPolygon(sokszog0);
            corners = (ArrayList<double[]>) vertices.clone();
        }

        if (papertype == PaperType.Custom) {

            vertices_size = 0;
            vertices.clear();
            for (double[] pont : corners) {
                addVertex(new double[]{pont[0], pont[1], 0});
            }
            vertices2d = (ArrayList<double[]>) vertices.clone();

            polygons_size = 0;
            polygons.clear();
            ArrayList<Integer> sokszog0 = new ArrayList<>();
            for (int i = 0; i < vertices_size; i++) {
                sokszog0.add(i);
            }
            addPolygon(sokszog0);
        }
    }

    /**
     * Végrehajtja a this origami {@link #history}-jában szereplô összes
     * mûveletet index szerint növekvô sorrendben.
     * <p> Mivel a mûveleteket this origami aktuális állapotán végzi el,
     * általában egy {@linkplain #reset()} hívás elôzi meg.
     */
    public void execute() {

        for (int i=0; i<history_pointer; i++) {

            double[] parancs = history.get(i);
            if (parancs[0] == 1) {
                internalReflectionFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]});
            } else if (parancs[0] == 2) {
                internalRotationFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, (int) (parancs[7]));
            } else if (parancs[0] == 3) {
                internalReflectionFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, (int) (parancs[7]));
            } else if (parancs[0] == 4) {
                internalRotationFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, (int) (parancs[7]), (int) (parancs[8]));
            } else if (parancs[0] == 5) {
                internalRotationFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, 0);
            }
        }
    }

    /**
     * Végrehajt a megadott indextôl kezdve a megadott darabszámú, this origami
     * {@link #history}-jában egymást követô mûveletet this origamin index
     * szerint növekvô sorrendben.
     *
     * @param index Az elsô végrehajtandó mûvelet {@link history}-beli 0-alapú
     * indexe. Negatív érték esetén csak a mûveletek nemnegatív indexû része
     * lesz elvégezve.
     * @param steps A végrehajtandó mûveletek száma.
     * @throws Exception if (index+db > history.size())
     */
    public void execute(int index, int steps) throws Exception {
        if (index + steps <= history.size()) {

            for (int i = index; i < index + steps && i >= 0; i++) {
                double[] parancs = history.get(i);

                if (parancs[0] == 1) {
                    internalReflectionFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]});
                } else if (parancs[0] == 2) {
                    internalRotationFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, (int) (parancs[7]));
                } else if (parancs[0] == 3) {
                    internalReflectionFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, (int) (parancs[7]));
                } else if (parancs[0] == 4) {
                    internalRotationFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, (int) (parancs[7]), (int) (parancs[8]));
                } else if (parancs[0] == 5) {
                    internalRotationFold(new double[]{parancs[1], parancs[2], parancs[3]}, new double[]{parancs[4], parancs[5], parancs[6]}, 0);
                }
            }
        } else {

            throw new Exception("Parameter must be smaller than history.size()");
        }
    }

    /**
     * Visszavonja az utoljára végrehajtott mûveletet this origamiban azáltal,
     * hogy törli {@link #history} utolsó elemét, majd meghívja a
     * {@linkplain #reset()} és {@linkplain #execute()} metódusokat.
     *
     * @since 2013-09-05
     */
    public void undo() {

        if (history_pointer > 0) {
            history_pointer--;
            while (0 < history_pointer ? history.get(history_pointer - 1)[0] == 5d : false) {
                history_pointer--;
            }
            reset();
            execute();
        }
    }

    /**
     * undo a paraméternek megfelelô számú mûveletet this origamiban
     * azáltal, hogy törli {@link #history} annyi utolsó elemét, majd meghívja a
     * {@linkplain #reset()} és {@linkplain #execute()} metódusokat.
     *
     * @param steps A visszavonandó mûveletek száma.
     * @since 2013-09-05
     */
    public void undo(int steps) {

        if (history_pointer >= steps) {
            history_pointer -= steps;
            reset();
            execute();
        }
    }

    public void redo() {

        if (history.size() > history_pointer) {

            history_pointer++;
            while(history.get(history_pointer - 1)[0] == 5d) {
                history_pointer++;
            }
            reset();
            execute();
        }
    }
    
    public void redo(int steps) {
    	
    	if (history_pointer + steps <= history.size()) {
            history_pointer += steps;
            reset();
            execute();
        }
    }

    public void redoAll() {

        if (history.size() > history_pointer) {

            history_pointer = history.size();
            reset();
            execute();
        }
    }

    /**
     * {@link #polygons()} üres listái helyére a felettük lévô nemüreseket
     * csúsztatja le this origamiban, miközben a paraméternek megfelelô indexû
     * sokszöget a helyén hagyja.
     *
     * @param polygonIndex A helyben hagyni kívánt sokszög 0 alapú indexe.
     * @since 2013-09-04
     */
    private void shrink(int polygonIndex) {

        ArrayList<Integer> tmp = polygons.get(polygonIndex);
        removePolygon(polygonIndex);
        for (int i = 0; i < polygons_size; i++) {
            if (polygons.get(i) == new ArrayList<Integer>()
                    || polygons.get(i).isEmpty()) {

                removePolygon(i);
                i--;
            }
        }

        while (polygonIndex > polygons_size) {

            addPolygon(new ArrayList<Integer>());
        }
        polygons.add(polygonIndex, tmp);
        polygons_size++;
    }

    /**
     * Eltávolítja {@link #polygons()} összes üres sokszögét this origamiban.
     *
     * @since 2013-09-04
     */
    private void shrink() {

        for (int i = 0; i < polygons_size; i++) {
            if (polygons.get(i) == new ArrayList<Integer>()
                    || polygons.get(i).isEmpty()) {

                removePolygon(i);
                i--;
            }
        }
    }

    /**
     * A bemeneti listában megadott pontokat a súlypontjukból nézve pozitív
     * körüljárás szerint rendezi. NEM ellenőrzi, hogy a vertices konvex sokszöget
     * határoznak-e meg.
     *
     * @param polygon
     * @return
     * @since 2013-10-11
     */
    static private ArrayList<double[]> ccwWindingOrder(ArrayList<double[]> polygon) {

        ArrayList<double[]> rend = new ArrayList<>(Arrays.asList(new double[][]{}));
        ArrayList<Double> szogek = new ArrayList<>();
        szogek.add(0d);

        if (polygon.size() > 0) {

            double[] kozeppont = new double[]{0, 0};

            for (double[] pont : polygon) {

                kozeppont = new double[]{
                    kozeppont[0] + pont[0] / polygon.size(),
                    kozeppont[1] + pont[1] / polygon.size()
                };
            }

            for (int i = 1; i < polygon.size(); i++) {

                szogek.add(angle(vector(polygon.get(i), kozeppont),
                        vector(polygon.get(0), kozeppont)));
            }

            while (rend.size() < polygon.size()) {

                double minszog = -1.0;
                double[] minhely = nullvektor;
                int mindex = -1;

                for (int i = 0; i < szogek.size(); i++) {

                    if ((szogek.get(i) < minszog || minszog == -1.0) && szogek.get(i) != -1.0) {

                        minszog = szogek.get(i);
                        minhely = polygon.get(i);
                        mindex = i;
                    }
                }

                rend.add(new double[]{minhely[0], minhely[1]});
                szogek.set(mindex, -1.0);
            }
        }
        return rend;
    }

    /**
     *
     * @param polygon
     * @return
     * @since 2013-10-12
     */
    static private boolean isConvex(ArrayList<double[]> polygon) {

        if (polygon.size() > 3) {

            if (angle(vector(polygon.get(polygon.size() - 1), polygon.get(0)),
                    vector(polygon.get(1), polygon.get(0)))
                    > Math.PI) {
                return false;
            }

            for (int i = 1; i < polygon.size() - 1; i++) {

                if (angle(vector(polygon.get(i - 1), polygon.get(i)),
                        vector(polygon.get(i + 1), polygon.get(i)))
                        > Math.PI) {
                    return false;
                }
            }

            if (angle(vector(polygon.get(polygon.size() - 2), polygon.get(polygon.size() - 1)),
                    vector(polygon.get(0), polygon.get(polygon.size() - 1)))
                    > Math.PI) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return @since 2013-10-31
     */
    public double circumscribedSquareSize() {
        return Math.max(paperWidth(), paperHeight());
    }

    public double paperWidth() {

        Double jobb = null, bal = null;
        for (double[] pont : corners) {

            bal = bal == null ? pont[0] : (bal > pont[0] ? pont[0] : bal);
            jobb = jobb == null ? pont[0] : (jobb < pont[0] ? pont[0] : jobb);
        }
        if (jobb == null) {
            jobb = 0d;
        }
        if (bal == null) {
            bal = 0d;
        }
        return jobb - bal;
    }

    public double paperHeight() {

        Double also = null, felso = null;
        for (double[] pont : corners) {

            also = also == null ? pont[1] : (also > pont[1] ? pont[1] : also);
            felso = felso == null ? pont[1] : (felso < pont[1] ? pont[1] : felso);
        }
        if (also == null) {
            also = 0d;
        }
        if (felso == null) {
            felso = 0d;
        }
        return felso - also;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Origami clone() {

        Origami copy = new Origami(papertype);
        copy.corners = (ArrayList<double[]>) corners.clone();
        copy.history = (ArrayList<double[]>) history.clone();
        copy.vertices_size = vertices_size;
        copy.vertices = (ArrayList<double[]>) vertices.clone();
        copy.vertices2d = (ArrayList<double[]>) vertices2d.clone();
        copy.polygons_size = polygons_size;
        copy.polygons = (ArrayList<ArrayList<Integer>>) polygons.clone();
        copy.last_cut_polygons = (ArrayList<ArrayList<Integer>>) last_cut_polygons.clone();
        copy.cutpolygon_nodes = (ArrayList<int[]>) cutpolygon_nodes.clone();
        copy.cutpolygon_pairs = (ArrayList<int[]>) cutpolygon_pairs.clone();
        return copy;
    }
}