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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a three-dimensional rigid origami model consisting of convex
 * polygonal faces. Provides methods for manipulating the model with various
 * types of transformations.
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public class Origami {

    /**
     * Indicates that a method is a user-level folding operation.
     */
    private @interface FoldingOperation {

        /**
         *
         * @return
         */
        int value();
    }

    /**
     * Creates a new origami model. <br>
     * The new model will be initialized with an empty {@link #history()
     * histroy}, a {@link #papertype() papertype} of the specified
     * {@link PaperType}, and will be {@link #reset() reset} immediately
     * afterwards.
     *
     * @param papertype
     *            The {@link #papertype() papertype} of the new instance.
     */
    public Origami(PaperType papertype) {

        vertices = (vertices2d = new ArrayList<>());
        vertices_size = 0;
        polygons = new ArrayList<>();
        polygons_size = 0;
        history = new ArrayList<>();
        history_stream = new ArrayList<>();
        history_pointer = 0;
        this.papertype = papertype;
        reset();
    }

    /**
     * Creates a new origami model. <br>
     * The new model will be initialized with an empty {@link #history()
     * history}, a {@link #papertype() papertype} of {@link PaperType#Custom}, a
     * {@link #corners() corners} list that is the
     * {@link Geometry#ccwWindingOrder(ArrayList) ccwWindingOrder} of the specified
     * {@link ArrayList}, and will be {@link #reset() reset} immediately
     * afterwards.
     *
     * @param corners
     *            The {@link #corners() corners} list of the new instance in an
     *            arbitrary order.
     * @throws Exception
     *             if {@code !isConvex(ccwWindingOrder(corners))}
     */
    public Origami(ArrayList<double[]> corners) throws Exception {

        vertices = (vertices2d = new ArrayList<>());
        vertices_size = 0;
        polygons = new ArrayList<>();
        polygons_size = 0;
        history = new ArrayList<>();
        history_stream = new ArrayList<>();
        history_pointer = 0;
        papertype = PaperType.Custom;
        this.corners = Geometry.ccwWindingOrder(corners);
        if (!Geometry.isConvex(this.corners)) {
            throw new Exception("Varatlan konkav sokszog/Unexpected concave polygon");
        }
        reset();
    }

    @SuppressWarnings("unchecked")
    public Origami(Origami origami) {

        papertype = origami.papertype;
        corners = origami.corners;
        history = (ArrayList<double[]>) origami.history.clone();
        history_stream = (ArrayList<int[]>) origami.history_stream.clone();
        reset();
    }

    public int generation() {
        return 1;
    }

    protected ArrayList<double[]> vertices = new ArrayList<>();

    /**
     * Returns a list of all the vertices in this origami, regardless of whether
     * they are used in any of the {@link #polygons() polygons} or not. The
     * elements in this list are {@code double[]}s, each one representing the
     * coordinates of a vertex in the origami space, i. e. the 3-dimensional
     * space where this origami is edited. <br>
     * In this list, every vertex has a corresponding preimage in the
     * {@link #vertices2d() vertices2d} list that has the same index.
     *
     * @return An {@link ArrayList} representing the vertices of this origami in
     *         the origami space.
     */
    public ArrayList<double[]> vertices() {
        return vertices;
    }

    protected int vertices_size = 0;

    /**
     * Returns the number of vertices in this origami, which is expected to be
     * the {@link ArrayList#size() size} of the {@link #vertices() vertices} and
     * the {@link #vertices2d() vertices2d} list of this origami at the same
     * time.
     *
     * @return The number of vertices in this origami.
     */
    public int vertices_size() {
        return vertices_size;
    }

    protected ArrayList<ArrayList<Integer>> polygons = new ArrayList<>();

    /**
     * Returns a list of all the polygons in this origami. Each element in this
     * list is an {@link ArrayList} that stores the vertices of one of this
     * origami's polygons in the form of indices pointing into both the
     * {@link #vertices() vertices} and the {@link #vertices2d() vertices2d}
     * list. In each polygon, the vertices are arranged in a counter-clockwise
     * winding order. <br>
     * The polygons themselves do not have any particular order.
     *
     * @return As described above.
     */
    public ArrayList<ArrayList<Integer>> polygons() {
        return polygons;
    }

    protected int polygons_size = 0;

    /**
     * @return The number of polygons in this origami.
     */
    public int polygons_size() {
        return polygons_size;
    }

    protected ArrayList<double[]> history = new ArrayList<>();

    /**
     * Whenever a FoldingOperation is called on the origami, its identifier and
     * its parameters are all merged into a single array and added to this
     * list at the index corresponding to the {@link #history_pointer()}.
     * All elements above this index will be removed from the list.
     *
     * @return As described above.
     */
    public ArrayList<double[]> history() {
        return history;
    }

    protected int history_pointer;

    public int history_pointer() {
        return history_pointer;
    }

    protected ArrayList<int[]> history_stream = new ArrayList<>();

    public ArrayList<int[]> history_stream() {
        return history_stream;
    }

    /**
     * Enumerates the preset paper types of the {@link Origami} class.
     */
    public enum PaperType {

        /**
         * An Origami of this {@link #papertype() papertype} will have a single
         * polygon with vertices {0, 0, 0}, (424.3, 0, 0} {424.3, 300, 0} and
         * {300, 0, 0} when {@link #reset() reset}.
         */
        A4('A'),
        /**
         * An Origami of this {@link #papertype() papertype} will have a single
         * polygon with vertices {0, 0, 0}, (400, 0, 0} {400, 400, 0} and {0,
         * 400, 0} when {@link #reset() reset}.
         */
        Square('N'),
        /**
         * An Origami of this {@link #papertype() papertype} will have a single
         * polygon with vertices {300, 346.41, 0}, {400, 173.205, 0}, {300, 0,
         * 0}, {100, 0, 0}, {0, 173.205, 0} and {100, 346.41, 0} when
         * {@link #reset() reset}.
         */
        Hexagon('H'),
        /**
         * An Origami of this {@link #papertype() papertype} will have a single
         * polygon with vertices {0, 0, 0}, {400, 0, 0}, {400, 170, 0} and {0,
         * 170, 0} when {@link #reset() reset}.
         */
        Dollar('D'),
        /**
         * An Origami of this {@link #papertype() papertype} will have a single
         * polygon with vertices {0, 0, 0}, {400, 0, 0}, {400, 181.82, 0} and
         * {0, 181.82, 0} when {@link #reset() reset}.
         */
        Forint('F'),
        /**
         * An Origami of this {@link #papertype() papertype} will have a single
         * polygon with the vertices obtained from the
         * {@link Geometry#ccwWindingOrder(ArrayList) ccwWindingOrder} of its
         * {@link #corners() corners} when {@link #reset() reset}.
         */
        Custom('E');

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
         * @return The assigned unique {@code char} value of this PaperType.
         */
        public char toChar() {

            return ID;
        }

        /**
         * Returns the PaperType the specified {@code char} value is assigned
         * to.
         *
         * @param c
         *            The {@code char} value of the desired PaperType.
         * @return The desired PaperType.
         */
        static public PaperType forChar(char c) {

            return allid.get(c);
        }

        @Override
        public String toString() throws NullPointerException {

            if (super.equals(A4)) {

                return "A4";
            }
            else if (super.equals(Square)) {

                return "Square";
            }
            else if (super.equals(Hexagon)) {

                return "Regular hexagon";
            }
            else if (super.equals(Dollar)) {

                return "Dollar bill";
            }
            else if (super.equals(Forint)) {

                return "Forint bill";
            }
            else if (super.equals(Custom)) {

                return "Custom";
            }
            else {

                throw new NullPointerException();
            }
        }
    }

    protected PaperType papertype = PaperType.Square;

    /**
     * Returns the paper type of this origami. The value of this field will be
     * taken into account by the {@link #reset() reset} method.
     *
     * @return As described above.
     * @see PaperType
     */
    public PaperType papertype() {
        return papertype;
    }

    protected ArrayList<double[]> corners = new ArrayList<>();

    /**
     * Returns a copy of the original {@link #vertices() vertices} list of this
     * origami, as it was initialized after the last {@link #reset() reset}
     * call. If {@code (papertype() == PaperType.Custom)}, the {@link #reset()
     * reset} method will initialize the {@link #vertices() vertices} and the
     * {@link #vertices2d() vertices2d} lists as copies of this list.
     *
     * @return As described above.
     */
    public ArrayList<double[]> corners() {
        return corners;
    }

    protected ArrayList<double[]> vertices2d = new ArrayList<>();

    /**
     * Returns a list of all the vertices in this origami, regardless of whether
     * they are used in any of the {@link #polygons() polygons} or not. The
     * elements in this list are {@code double[]}s, each one representing the
     * coordinates of a vertex in the paper space, i. e. the 2-dimensional space
     * where the vertices of this origami would be if it were unfolded. <br>
     * In this list, every vertex has a corresponding image in the
     * {@link #vertices() vertices} list that has the same index.
     *
     * @return An {@link ArrayList} representing the vertices of this origami in
     *         the paper space.
     */
    public ArrayList<double[]> vertices2d() {
        return vertices2d;
    }

    protected ArrayList<Integer> border = new ArrayList<>();

    public ArrayList<Integer> border() {
        return border;
    }

    /**
     * Adds a new vertex to the end of the {@link #vertices() vertices} list of
     * this origami.
     *
     * @param point
     *            The 3-dimensional coordinates of the new vertex in the origami
     *            space as {@code double}s.
     */
    protected void addVertex(double... point) {

        vertices.add(point);
        vertices_size++;
    }

    /**
     * Adds a new vertex to the end of the {@link #vertices2d() vertices2d} list
     * of this origami.
     *
     * @param point
     *            The 2-dimensional coordinates of the new vertex in the paper
     *            space as {@code double}s.
     */
    protected void add2dVertex(double... point) {
        vertices2d.add(point);
    }

    /**
     * Adds a new polygon to the end of the {@link #polygons() polygons} list of
     * this origami. <br>
     * When adding a new polygon, {@code isConvex(polygon)} is expected (but not
     * checked) to be {@code true}.
     *
     * @param polygon
     *            An {@link ArrayList} that contains zero-based indices pointing
     *            into the {@link #vertices() vertices} and the
     *            {@link #vertices2d() vertices2d} list of this origami in a
     *            counter-clockwise winding order.
     */
    protected void addPolygon(ArrayList<Integer> polygon) {

        polygons.add(polygon);
        polygons_size++;
    }

    /**
     * Removes the polygon from this origami's {@link #polygons() polygons} at
     * the specified index.
     *
     * @param polygonIndex
     *            The zero-based index at which the polygon to remove is located
     *            in the {@link #polygons() polygons} list.
     */
    protected void removePolygon(int polygonIndex) {

        polygons.remove(polygonIndex);
        polygons_size--;
    }

    protected void addCommand(int commandID, double[] ppoint, double[] pnormal, int polygonIndex, int phi) {

        int[] cblock = commandBlock(commandID, ppoint, pnormal, polygonIndex, phi);
        addCommand(cblock);
    }

    protected void addCommand(int[] cblock) {

        int i = -1;

        int header = cblock[++i];
        header <<= 8;
        header += cblock[++i];
        header <<= 8;
        header += cblock[++i];
        header <<= 8;
        header += cblock[++i];

        short Xint, Yint, Zint;
        int Xfrac, Yfrac, Zfrac;

        Xint = (short) cblock[++i];
        Xint <<= 8;
        Xint += cblock[++i];
        Xfrac = cblock[++i];
        Xfrac <<= 8;
        Xfrac += cblock[++i];
        double X = Xint + Math.signum(Xint) * (double) Xfrac / 256 / 256;

        Yint = (short) cblock[++i];
        Yint <<= 8;
        Yint += cblock[++i];
        Yfrac = cblock[++i];
        Yfrac <<= 8;
        Yfrac += cblock[++i];
        double Y = Yint + Math.signum(Yint) * (double) Yfrac / 256 / 256;

        Zint = (short) cblock[++i];
        Zint <<= 8;
        Zint += cblock[++i];
        Zfrac = cblock[++i];
        Zfrac <<= 8;
        Zfrac += cblock[++i];
        double Z = Zint + Math.signum(Zint) * (double) Zfrac / 256 / 256;

        double[] ppoint = new double[3];
        double[] pnormal = new double[3];
        ppoint[0] = (double) X + Origins[(((header >>> 24) % 32) - ((header >>> 24) % 8)) / 8][0];
        ppoint[1] = (double) Y + Origins[(((header >>> 24) % 32) - ((header >>> 24) % 8)) / 8][1];
        ppoint[2] = (double) Z + Origins[(((header >>> 24) % 32) - ((header >>> 24) % 8)) / 8][2];
        pnormal[0] = X;
        pnormal[1] = Y;
        pnormal[2] = Z;

        // choosing the appropriate half space
        if (((header >>> 24) - ((header >>> 24) % 32)) / 32 == 1) {
            pnormal = new double[] { -pnormal[0], -pnormal[1], -pnormal[2] };
        }

        double[] command;
        if ((header >>> 24) % 8 == 1) {

            // reflection fold
            command = new double[7];
            command[0] = 1;
        }
        else if ((header >>> 24) % 8 == 2) {

            // positive rot. fold
            command = new double[8];
            command[0] = 2;
            command[7] = (header >>> 16) % 256;
        }
        else if ((header >>> 24) % 8 == 3) {

            // negative rot. fold
            command = new double[8];
            command[0] = 2;
            command[7] = -(header >>> 16) % 256;
        }
        else if ((header >>> 24) % 8 == 4) {

            // partial reflection fold
            command = new double[8];
            command[0] = 3;
            command[7] = (header % 65536);
        }
        else if ((header >>> 24) % 8 == 5) {

            // positive partial rot. fold
            command = new double[9];
            command[0] = 4;
            command[7] = (header >>> 16) % 256;
            command[8] = (header % 65536);
        }
        else if ((header >>> 24) % 8 == 6) {

            // negative partial rot. fold
            command = new double[9];
            command[0] = 4;
            command[7] = -(header >>> 16) % 256;
            command[8] = (header % 65536);
        }
        else if ((header >>> 24) % 8 == 7) {

            // crease
            command = new double[7];
            command[0] = 5;
        }
        else if (header % 65536 == 65535) {

            // cut
            command = new double[7];
            command[0] = 6;
        }
        else {

            // partial cut
            command = new double[8];
            command[0] = 7;
            command[7] = (header % 65536);
        }

        command[1] = ppoint[0];
        command[2] = ppoint[1];
        command[3] = ppoint[2];
        command[4] = pnormal[0];
        command[5] = pnormal[1];
        command[6] = pnormal[2];

        history.add(command);
        history_stream.add(cblock);
    }

    protected int[] commandBlock(int foid, double[] ppoint, double[] pnormal, int polygonIndex, int phi) {

        double max_d = -1;
        int used_origin = 0;
        int used_hemispace = 0;
        double[] pjoint = new double[] { 0, 0, 0 };

        for (int ii = 0; ii < Origins.length; ii++) {

            double[] basepoint = Geometry.line_plane_intersection(Origins[ii], pnormal, ppoint, pnormal);
            if (Geometry.vector_length(Geometry.vector(basepoint, Origins[ii])) > max_d) {

                pjoint = Geometry.vector(basepoint, Origins[ii]);
                max_d = Geometry.vector_length(pjoint);
                used_origin = ii;
            }
        }

        // inner: 1, outer: 0
        if (Geometry.scalar_product(pnormal, pjoint) < 0) {
            used_hemispace = 1;
        }

        int command = 0;
        int poly_indx = 65535;

        switch (foid) {

            case 1:
                command = 1;
                break;

            case 2:
                while (phi < 0) {
                    phi += 360;
                }
                phi %= 360;
                if (phi <= 180) {
                    command = 2;
                }
                else {

                    command = 3;
                    phi = 360 - phi;
                }
                break;

            case 3:
                command = 4;
                poly_indx = polygonIndex;
                break;

            case 4:
                while (phi < 0) {
                    phi += 360;
                }
                phi %= 360;
                if (phi <= 180) {
                    command = 5;
                }
                else {

                    command = 6;
                    phi = 360 - phi;
                }
                poly_indx = polygonIndex;
                break;

            case 5:
                command = 7;
                break;

            case 6:
                command = 0;
                break;

            case 7:
                command = 0;
                poly_indx = polygonIndex;
                break;
        }

        int Xe = (int) pjoint[0];
        int Ye = (int) pjoint[1];
        int Ze = (int) pjoint[2];

        int Xt = (int) Math.round((Math.abs(pjoint[0] - Xe)) * 256 * 256);
        int Yt = (int) Math.round((Math.abs(pjoint[1] - Ye)) * 256 * 256);
        int Zt = (int) Math.round((Math.abs(pjoint[2] - Ze)) * 256 * 256);

        int[] cblock = {

                // header
                (0xFF & (used_hemispace * 32 + used_origin * 8 + command)), (0xFF & (phi)), (0xFF & (poly_indx >>> 8)),
                (0xFF & (poly_indx)),

                // body
                (0xFF & (Xe >>> 8)), (0xFF & (Xe)), (0xFF & (Xt >>> 8)), (0xFF & (Xt)),

                (0xFF & (Ye >>> 8)), (0xFF & (Ye)), (0xFF & (Yt >>> 8)), (0xFF & (Yt)),

                (0xFF & (Ze >>> 8)), (0xFF & (Ze)), (0xFF & (Zt >>> 8)), (0xFF & (Zt)) };
        return cblock;
    }

    /**
     * Checks if the polygon at the specified index in the {@link #polygons()
     * polygons} list is at least one-dimensional, i. e. it has two vertices
     * with a positive distance between them.
     *
     * @param polygonIndex
     *            The zero-based index at which the polygon to check is located
     *            in the {@link #polygons() polygons} list.
     * @return {@code false} iff the specified polygon is zero-dimensional.
     */
    public boolean isNonDegenerate(int polygonIndex) {

        if (polygons.get(polygonIndex).size() > 1) {
            for (int p : polygons.get(polygonIndex)) {
                if (Geometry.vector_length(
                        Geometry.vector(vertices.get(p), vertices.get(polygons.get(polygonIndex).get(0)))) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the polygon at the specified index in the {@link #polygons()
     * polygons} list has a positive area.
     *
     * @param polygonIndex
     *            The zero-based index at which the polygon to check is located
     *            in the {@link #polygons() polygons} list.
     * @return {@code false} iff the specified polygon is zero- or one-dimensional.
     */
    public boolean isStrictlyNonDegenerate(int polygonIndex) {

        if (polygons.get(polygonIndex).size() > 2) {

            for (int point1ind : polygons().get(polygonIndex)) {
                for (int point2ind : polygons().get(polygonIndex)) {

                    if (Geometry.vector_length(Geometry.vector_product(
                            Geometry.vector(vertices().get(point1ind),
                                    vertices().get(polygons().get(polygonIndex).get(0))),
                            Geometry.vector(vertices().get(point2ind),
                                    vertices().get(polygons().get(polygonIndex).get(0))))) > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean isCut(double[] ppoint, double[] pnormal, int polygonIndex) {

        if (isNonDegenerate(polygonIndex)) {

            boolean inner = false, outer = false;
            for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {
                if (Geometry.scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) / Math.sqrt(Math
                        .max(Geometry.scalar_product(pnormal, pnormal), 1)) > Geometry.scalar_product(ppoint, pnormal)
                                / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) + 0.00000001) {
                    inner = true;
                }
                else if (Geometry.scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal)
                        / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) < Geometry.scalar_product(
                                ppoint, pnormal) / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1))
                                - 0.00000001) {
                    outer = true;
                }
                if (inner && outer) {
                    return true;
                }
            }
        }
        return false;
    }

    protected ArrayList<int[]> cutpolygon_nodes = new ArrayList<>();
    protected ArrayList<int[]> cutpolygon_pairs = new ArrayList<>();
    protected ArrayList<ArrayList<Integer>> last_cut_polygons = new ArrayList<>();

    /**
     * Removes the specified polygon from the {@link #polygons() polygons} list,
     * and adds one or two new polygons into it. One of new polygons is obtained
     * by intersecting the old polygon with the specified closed half-space, and
     * the other one by intersecting it with the closure of that half-space's
     * complement. If one of these polygons is empty, only the other one will be
     * added. <br>
     * If two new polygons have been generated, their common vertices will point
     * to the same object in the {@link #vertices() vertices} list, thus
     * becoming 'inseparable'.
     *
     * @param ppoint
     *            An array containing the coordinates of a boundary point of the
     *            half-space.
     * @param pnormal
     *            An array containing the coordinates of the normal vector of
     *            the half-space.
     * @param polygonIndex
     *            The zero-based index at which the polygon to split is located
     *            in the {@link #polygons() polygons} list.
     * @return {@code true} iff the polygon has been divided in two.
     */
    protected boolean cutPolygon(double[] ppoint, double[] pnormal, int polygonIndex) {

        if (isCut(ppoint, pnormal, polygonIndex)) {

            ArrayList<Integer> newpoly1 = new ArrayList<>();
            ArrayList<Integer> newpoly2 = new ArrayList<>();

            for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

                int j = (i + 1) % polygons.get(polygonIndex).size();
                if (Geometry.point_on_plane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)))) {

                    newpoly1.add(polygons.get(polygonIndex).get(i));
                    newpoly2.add(polygons.get(polygonIndex).get(i));
                }
                else {

                    if (Geometry.scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) > Geometry
                            .scalar_product(ppoint, pnormal)) {
                        newpoly1.add(polygons.get(polygonIndex).get(i));
                    }
                    else {
                        newpoly2.add(polygons.get(polygonIndex).get(i));
                    }

                    if (Geometry.plane_between_points(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)),
                            vertices.get(polygons.get(polygonIndex).get(j)))) {

                        freshcut: {
                            for (int[] szakasz : cutpolygon_nodes) {
                                if (szakasz[0] == polygons.get(polygonIndex).get(i)
                                        && szakasz[1] == polygons.get(polygonIndex).get(j)) {
                                    newpoly1.add(szakasz[2]);
                                    newpoly2.add(szakasz[2]);
                                    break freshcut;
                                }
                                else if (szakasz[0] == polygons.get(polygonIndex).get(j)
                                        && szakasz[1] == polygons.get(polygonIndex).get(i)) {
                                    newpoly1.add(szakasz[2]);
                                    newpoly2.add(szakasz[2]);
                                    break freshcut;
                                }
                            }

                            double[] dirvec = Geometry.vector(vertices.get(polygons.get(polygonIndex).get(i)),
                                    vertices.get(polygons.get(polygonIndex).get(j)));
                            double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

                            double[] meet = Geometry.line_plane_intersection(ipoint, dirvec, ppoint, pnormal);
                            addVertex(meet);

                            double weight1 = Geometry.vector_length(
                                    Geometry.vector(meet, vertices.get(polygons.get(polygonIndex).get(j))));
                            double weight2 = Geometry.vector_length(
                                    Geometry.vector(meet, vertices.get(polygons.get(polygonIndex).get(i))));
                            add2dVertex(new double[] {
                                    (vertices2d.get(polygons.get(polygonIndex).get(i))[0] * weight1
                                            + vertices2d.get(polygons.get(polygonIndex).get(j))[0] * weight2)
                                            / (weight1 + weight2),
                                    (vertices2d.get(polygons.get(polygonIndex).get(i))[1] * weight1
                                            + vertices2d.get(polygons.get(polygonIndex).get(j))[1] * weight2)
                                            / (weight1 + weight2),
                                    0 });

                            newpoly1.add(vertices_size - 1);
                            newpoly2.add(vertices_size - 1);
                            cutpolygon_nodes.add(new int[] { polygons.get(polygonIndex).get(i),
                                    polygons.get(polygonIndex).get(j), vertices_size - 1 });

                            for (int ii = 0; ii < border.size(); ii++) {
                                if (border.get(ii).equals(polygons.get(polygonIndex).get(i))) {
                                    if (border.get((ii + 1) % border.size())
                                            .equals(polygons.get(polygonIndex).get(j))) {

                                        border.add(ii + 1, vertices_size - 1);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            cutpolygon_pairs.add(new int[] { polygonIndex, polygons.size() });
            last_cut_polygons.add(polygons.get(polygonIndex));
            polygons.set(polygonIndex, newpoly1);
            addPolygon(newpoly2);
            return true;
        }
        return false;
    }

    public ArrayList<Integer> polygonSelect(double[] ppoint, double[] pnormal, int polygonIndex) {

        ArrayList<Integer> selection = new ArrayList<>();
        selection.add(polygonIndex);
        for (int i = 0; i < selection.size(); i++) {

            int elem = selection.get(i);
            for (int ii = 0; ii < polygons_size; ii++) {

                if (!selection.contains(ii)) {

                    for (int e_point : polygons.get(elem)) {

                        if (polygons.get(ii).contains(e_point)) {
                            if (!Geometry.point_on_plane(ppoint, pnormal, vertices.get(e_point))) {
                                selection.add(ii);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return selection;
    }

    public double[] polygonCenter(int polygonIndex) {

        double[] res = new double[] { Double.NaN, Double.NaN, Double.NaN };
        if (isNonDegenerate(polygonIndex)) {

            res = new double[] { 0d, 0d, 0d };
            for (int ind : polygons.get(polygonIndex)) {
                res = new double[] { res[0] + vertices.get(ind)[0], res[1] + vertices.get(ind)[1],
                        res[2] + vertices.get(ind)[2] };
            }

            java.util.Random eltolas = new java.util.Random(polygonIndex);
            res = new double[] { res[0] / polygons.get(polygonIndex).size() + eltolas.nextDouble() * 10 - 5,
                    res[1] / polygons.get(polygonIndex).size() + eltolas.nextDouble() * 10 - 5,
                    res[2] / polygons.get(polygonIndex).size() + eltolas.nextDouble() * 10 - 5 };
        }
        return res;
    }

    /**
     * Performs a {@link #cutPolygon(double[], double[], int) cutPolygon} with
     * the specified plane and every polygon's index in this origami's
     * {@link #polygons() polygon list}, and reflects some of the
     * {@link #vertices() vertices} over the plane. The vertices located on the
     * same side of the plane as where the specified normal vector is pointing
     * to will be the ones reflected over the plane.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     */
    protected void internalReflectionFold(double[] ppoint, double[] pnormal) {

        shrink();

        cutpolygon_nodes = new ArrayList<>();
        cutpolygon_pairs = new ArrayList<>();
        last_cut_polygons = new ArrayList<>();

        int facenum = polygons_size;
        for (int i = 0; i < facenum; i++) {

            if (isNonDegenerate(i)) {
                cutPolygon(ppoint, pnormal, i);
            }
        }

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        for (int i = 0; i < vertices_size; i++) {

            double[] ipoint = vertices.get(i);
            if (ipoint[0] * pnormal[0] + ipoint[1] * pnormal[1] + ipoint[2] * pnormal[2] - konst > 0) {

                double[] img = Geometry.reflection(ipoint, ppoint, pnormal);
                vertices.set(i, img);
            }
        }
    }

    /**
     * Passes the arguments in the same order to the
     * {@link Origami#polygonSelect(double[], double[], int) polygonSelect}
     * method, and reflects every {@link #polygons() polygon} listed therein
     * over the specified plane. <br>
     * Reunites previously {@link Origami#cutPolygon(double[], double[], int)
     * split} polygons if possible.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     * @param polygonIndex
     *            The index of the polygon to include in
     *            {@link Origami#polygonSelect(double[], double[], int)
     *            polygonSelect}.
     */
    protected void internalReflectionFold(double[] ppoint, double[] pnormal, int polygonIndex) {

        ArrayList<Integer> selection = polygonSelect(ppoint, pnormal, polygonIndex);

        for (int i = 0; i < vertices_size; i++) {

            for (int elem : selection) {

                if (polygons.get(elem).contains(i)) {

                    double[] ipoint = vertices.get(i);
                    double[] img = Geometry.reflection(ipoint, ppoint, pnormal);
                    vertices.set(i, img);
                    break;
                }
            }
        }

        for (int i = 0; i < cutpolygon_pairs.size(); i++) {

            if (!(selection.contains(cutpolygon_pairs.get(i)[0]) || selection.contains(cutpolygon_pairs.get(i)[1]))) {
                polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
            }
        }

        for (int[] pair : cutpolygon_pairs) {

            if (!(selection.contains(pair[0]) || selection.contains(pair[1]))) {
                polygons.set(pair[1], new ArrayList<Integer>());
            }
        }

        cutpolygon_pairs = new ArrayList<>();
        last_cut_polygons = new ArrayList<>();

        shrink(polygonIndex);
    }

    /**
     * Performs a {@link #cutPolygon(double[], double[], int) cutPolygon} with
     * the specified plane and every polygon's index in this origami's
     * {@link #polygons() polygon list}, and if the intersection of the plane
     * and the origami is a non-degenerate line, rotates some of the
     * {@link #vertices() vertices} around that line by the specified angle.
     * <br>
     * In this case, every vertex located on the same side of the plane as where
     * the specified normal vector is pointing to will be rotated around the
     * line. As there is no exclusive 'clockwise' direction in a 3-dimensional
     * space, the rotation's direction will be decided on a whim.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     * @param phi
     *            The angle of the rotation.
     * @return 0 if the rotation has been performed; 1 if it has not.
     */
    protected int internalRotationFold(double[] ppoint, double[] pnormal, int phi) {

        shrink();

        cutpolygon_nodes = new ArrayList<>();
        cutpolygon_pairs = new ArrayList<>();
        last_cut_polygons = new ArrayList<>();

        int facenum = polygons_size;
        for (int i = 0; i < facenum; i++) {

            if (isNonDegenerate(i)) {
                cutPolygon(ppoint, pnormal, i);
            }
        }

        ArrayList<Integer> foldingpoints = new ArrayList<>();
        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

        for (int i = 0; i < vertices_size; i++) {

            double[] ipoint = vertices.get(i);
            if (Geometry.point_on_plane(ppoint, pnormal, ipoint)) {
                foldingpoints.add(i);
            }
        }

        boolean collin = false;
        int farpoint = -1;
        double dist_max = -1;

        if (foldingpoints.size() >= 2) {

            for (int fp : foldingpoints) {

                if (Geometry.vector_length(Geometry.vector(vertices.get(fp), vertices.get(foldingpoints.get(0)))) > 0) {

                    collin = true;
                    if (Geometry.vector_length(
                            Geometry.vector(vertices.get(fp), vertices.get(foldingpoints.get(0)))) > dist_max) {

                        farpoint = fp;
                        dist_max = Geometry
                                .vector_length(Geometry.vector(vertices.get(fp), vertices.get(foldingpoints.get(0))));
                    }
                }
            }
        }

        for (int i = 1; i < foldingpoints.size() && i != farpoint; i++) {

            if (Geometry.vector_length(Geometry.vector_product(
                    Geometry.vector(vertices.get(foldingpoints.get(0)), vertices.get(foldingpoints.get(i))),
                    Geometry.vector(vertices.get(farpoint), vertices.get(foldingpoints.get(i))))) > Geometry
                            .vector_length(
                                    Geometry.vector(vertices.get(foldingpoints.get(0)), vertices.get(farpoint)))) {

                collin = false;
                break;
            }
        }

        if (collin) {

            double[] dirvec = Geometry.vector(vertices.get(foldingpoints.get(0)), vertices.get(farpoint));
            double sinphi = Math.sin((double) phi * Math.PI / 180);
            double cosphi = Math.cos((double) phi * Math.PI / 180);

            for (int i = 0; i < vertices_size; i++) {

                double[] ipoint = vertices.get(i);
                if (ipoint[0] * pnormal[0] + ipoint[1] * pnormal[1] + ipoint[2] * pnormal[2] - konst > 0) {

                    double[] img = Geometry.rotation(ipoint, vertices.get(foldingpoints.get(0)), dirvec, sinphi,
                            cosphi);
                    vertices.set(i, img);
                }
            }
            return 0;

        }
        else {
            return 1;
        }
    }

    /**
     * Passes the arguments in the same order to the
     * {@link Origami#polygonSelect(double[], double[], int) polygonSelect}
     * method, and if the resulting family of {@link #polygons() polygons}
     * intersects the specified plane in a non-degenerate line, rotates these
     * polygons by the specified angle around the line. As there is no exclusive
     * 'clockwise' direction in a 3-dimensional space, the rotation's direction
     * will be decided on a whim. <br>
     * Reunites previously {@link Origami#cutPolygon(double[], double[], int)
     * split} polygons if possible.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     * @param polygonIndex
     *            The index of the polygon to include in
     *            {@link Origami#polygonSelect(double[], double[], int)
     *            polygonSelect}.
     */
    protected void internalRotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex) {

        ArrayList<Integer> selection = polygonSelect(ppoint, pnormal, polygonIndex);
        ArrayList<Integer> foldingpoints = new ArrayList<>();

        for (int i = 0; i < vertices_size; i++) {

            double[] ipont = vertices.get(i);
            if (Geometry.point_on_plane(ppoint, pnormal, ipont)) {

                for (int elem : selection) {

                    if (polygons.get(elem).contains(i)) {

                        foldingpoints.add(i);
                        break;
                    }
                }
            }
        }

        boolean collin = false;
        int farpoint = -1;
        double dist_max = -1;

        if (foldingpoints.size() >= 2) {

            for (int hp : foldingpoints) {

                if (Geometry.vector_length(Geometry.vector(vertices.get(hp), vertices.get(foldingpoints.get(0)))) > 0) {

                    collin = true;
                    if (Geometry.vector_length(
                            Geometry.vector(vertices.get(hp), vertices.get(foldingpoints.get(0)))) > dist_max) {

                        farpoint = hp;
                        dist_max = Geometry
                                .vector_length(Geometry.vector(vertices.get(hp), vertices.get(foldingpoints.get(0))));
                    }
                }
            }
        }

        for (int i = 1; i < foldingpoints.size() && i != farpoint; i++) {

            if (Geometry.vector_length(Geometry.vector_product(
                    Geometry.vector(vertices.get(foldingpoints.get(0)), vertices.get(foldingpoints.get(i))),
                    Geometry.vector(vertices.get(farpoint), vertices.get(foldingpoints.get(i))))) > Geometry
                            .vector_length(
                                    Geometry.vector(vertices.get(foldingpoints.get(0)), vertices.get(farpoint)))) {

                collin = false;
                break;
            }
        }

        if (collin) {

            double[] dirvec = Geometry.vector(vertices.get(foldingpoints.get(0)), vertices.get(farpoint));
            double sinphi = Math.sin((double) phi * Math.PI / 180);
            double cosphi = Math.cos((double) phi * Math.PI / 180);

            for (int i = 0; i < vertices_size; i++) {
                for (int tag : selection) {

                    if (polygons.get(tag).contains(i)) {

                        double[] ipoint = vertices.get(i);
                        double[] img = Geometry.rotation(ipoint, vertices.get(foldingpoints.get(0)), dirvec, sinphi,
                                cosphi);
                        vertices.set(i, img);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < cutpolygon_pairs.size(); i++) {

            if (!(selection.contains(cutpolygon_pairs.get(i)[0]) || selection.contains(cutpolygon_pairs.get(i)[1]))) {
                polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
            }

        }

        for (int[] pair : cutpolygon_pairs) {

            if (!(selection.contains(pair[0]) || selection.contains(pair[1]))) {
                polygons.set(pair[1], new ArrayList<Integer>());
            }
        }

        cutpolygon_pairs = new ArrayList<>();
        last_cut_polygons = new ArrayList<>();

        shrink(polygonIndex);
    }

    /**
     * Performs a {@link #cutPolygon(double[], double[], int) cutPolygon} with
     * the specified plane and every polygon's index in this origami's
     * {@link #polygons() polygon list}, and deletes every polygon that is on
     * the same side of the plane as where the specified normal vector is
     * pointing to.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     */
    protected void internalMutilation(double[] ppoint, double[] pnormal) {

        shrink();

        cutpolygon_nodes = new ArrayList<>();
        cutpolygon_pairs = new ArrayList<>();
        last_cut_polygons = new ArrayList<>();
        int pnum = polygons_size;

        for (int i = 0; i < pnum; i++) {

            if (isNonDegenerate(i)) {
                cutPolygon(ppoint, pnormal, i);
            }
        }

        double konst = Geometry.scalar_product(ppoint, pnormal);
        for (int i = 0; i < polygons_size; i++) {
            for (int vert : polygons.get(i)) {

                if (Geometry.scalar_product(vertices.get(vert), pnormal) > konst
                        && !Geometry.point_on_plane(ppoint, pnormal, vertices.get(vert))) {

                    polygons.set(i, new ArrayList<Integer>());
                    break;
                }
            }
        }
    }

    /**
     * Passes the arguments in the same order to the
     * {@link Origami#polygonSelect(double[], double[], int) polygonSelect}
     * method, and deletes every {@link #polygons() polygon} listed therein.
     * <br>
     * Reunites previously {@link Origami#cutPolygon(double[], double[], int)
     * split} polygons if possible.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     * @param polygonIndex
     *            The index of the polygon to include in
     *            {@link Origami#polygonSelect(double[], double[], int)
     *            polygonSelect}.
     */
    protected void internalMutilation(double[] ppoint, double[] pnormal, int polygonIndex) {

        ArrayList<Integer> selection = polygonSelect(ppoint, pnormal, polygonIndex);
        double konst = Geometry.scalar_product(ppoint, pnormal);
        for (int i : selection) {

            ArrayList<Integer> poly = polygons.get(i);
            // this is just double-checking; the code should work without it
            for (int vert : poly) {
                if (Geometry.scalar_product(vertices.get(vert), pnormal) > konst) {

                    polygons.set(i, new ArrayList<Integer>());
                    break;
                }
            }
        }

        for (int i = 0; i < cutpolygon_pairs.size(); i++) {

            if (!(selection.contains(cutpolygon_pairs.get(i)[0]) || selection.contains(cutpolygon_pairs.get(i)[1]))) {
                polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
            }
        }

        for (int[] pair : cutpolygon_pairs) {

            if (!(selection.contains(pair[0]) || selection.contains(pair[1]))) {
                polygons.set(pair[1], new ArrayList<Integer>());
            }
        }

        cutpolygon_pairs = new ArrayList<>();
        last_cut_polygons = new ArrayList<>();

        shrink(polygonIndex);
    }

    /**
     * Logs its own {@link Origami#FoldingOperator FoldingOperator} identifier
     * and the given parameters by calling
     * {@link #addCommand(int, double[], double[], int, int) addCommand}. Calls
     * {@link #internalReflectionFold(double[], double[]) its internal variant}
     * with the same parameters.
     * 
     * @param ppoint
     * @param pnormal
     */
    @FoldingOperation(1)
    public void reflectionFold(double[] ppoint, double[] pnormal) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(1, ppoint, pnormal, 0, 0);
        execute(history_pointer, 1);
        history_pointer++;
    }

    @FoldingOperation(3)
    public void reflectionFold(double[] ppoint, double[] pnormal, int polygonIndex) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(3, ppoint, pnormal, polygonIndex, 0);
        execute(history_pointer, 1);
        history_pointer++;
    }

    @FoldingOperation(2)
    public void rotationFold(double[] ppoint, double[] pnormal, int phi) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(2, ppoint, pnormal, 0, phi);
        execute(history_pointer, 1);
        history_pointer++;
    }

    @FoldingOperation(4)
    public void rotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(4, ppoint, pnormal, polygonIndex, phi);
        execute(history_pointer, 1);
        history_pointer++;
    }

    @FoldingOperation(5)
    public void crease(double[] ppoint, double[] pnormal) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(5, ppoint, pnormal, 0, 0);
        execute(history_pointer, 1);
        history_pointer++;
    }

    @FoldingOperation(6)
    public void mutilation(double[] ppoint, double[] pnormal) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(6, ppoint, pnormal, 0, 0);
        execute(history_pointer, 1);
        history_pointer++;
    }

    @FoldingOperation(7)
    public void mutilation(double[] ppoint, double[] pnormal, int polygonIndex) {

        history.subList(history_pointer, history.size()).clear();
        history_stream.subList(history_pointer, history_stream.size()).clear();
        addCommand(7, ppoint, pnormal, polygonIndex, 0);
        execute(history_pointer, 1);
        history_pointer++;
    }

    /**
     * Initializes the {@link #corners() corners}, {@link #vertices() vertices},
     * {@link #vertices2d() vertices2d} and {@link #polygons() polygons} lists
     * of this origami depending on the value of its {@link #papertype()
     * papertype}. The {@link #corners() corners}, {@link #vertices() vertices},
     * and {@link #vertices2d() vertices2d} lists will always have the same
     * initial vertices in the same order.
     */
    @SuppressWarnings("unchecked")
    final public void reset() {

        if (papertype == PaperType.A4) {

            vertices_size = 0;
            vertices.clear();
            addVertex(0, 0, 0);
            addVertex(424.3, 0, 0);
            addVertex(424.3, 300, 0);
            addVertex(0, 300, 0);
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
            addVertex(0, 0, 0);
            addVertex(400, 0, 0);
            addVertex(400, 400, 0);
            addVertex(0, 400, 0);
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
            addVertex(300, 346.41, 0);
            addVertex(400, 173.205, 0);
            addVertex(300, 0, 0);
            addVertex(100, 0, 0);
            addVertex(0, 173.205, 0);
            addVertex(100, 346.41, 0);
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
            addVertex(0, 0, 0);
            addVertex(400, 0, 0);
            addVertex(400, 170, 0);
            addVertex(0, 170, 0);
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
            addVertex(0, 0, 0);
            addVertex(400, 0, 0);
            addVertex(400, 181.82, 0);
            addVertex(0, 181.82, 0);
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
                addVertex(pont[0], pont[1], 0);
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

        border = (ArrayList<Integer>) polygons.get(0).clone();
    }

    /**
     * Executes every {@link Origami#FoldingOperation FoldingOperation} stored
     * in this origami's {@link #history() history}. Does not call {@link
     * #reset() reset}.
     */
    public void execute() {

        for (int i = 0; i < history_pointer; i++) {

            double[] parancs = history.get(i);
            if (parancs[0] == 1) {
                internalReflectionFold(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] });
            }
            else if (parancs[0] == 2) {
                internalRotationFold(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]));
            }
            else if (parancs[0] == 3) {
                internalReflectionFold(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]));
            }
            else if (parancs[0] == 4) {
                internalRotationFold(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]), (int) (parancs[8]));
            }
            else if (parancs[0] == 5) {
                internalRotationFold(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] }, 0);
            }
            else if (parancs[0] == 6) {
                internalMutilation(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] });
            }
            else if (parancs[0] == 7) {
                internalMutilation(new double[] { parancs[1], parancs[2], parancs[3] },
                        new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]));
            }
        }
    }

    /**
     * Executes the specified number of steps stored in the {@link #history()
     * history} of this origami starting from the specified index.
     *
     * @param index The index of the first element in the in the {@link
     * #history() history} to execute.
     * @param steps The number of {@link Origami#FoldingOperation
     * FoldingOperations} to execute.
     */
    public void execute(int index, int steps) {

        if (index + steps <= history.size()) {
            for (int i = index; i < index + steps && i >= 0; i++) {
                double[] parancs = history.get(i);

                if (parancs[0] == 1) {
                    internalReflectionFold(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] });
                }
                else if (parancs[0] == 2) {
                    internalRotationFold(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]));
                }
                else if (parancs[0] == 3) {
                    internalReflectionFold(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]));
                }
                else if (parancs[0] == 4) {
                    internalRotationFold(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]),
                            (int) (parancs[8]));
                }
                else if (parancs[0] == 5) {
                    internalRotationFold(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] }, 0);
                }
                else if (parancs[0] == 6) {
                    internalMutilation(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] });
                }
                else if (parancs[0] == 7) {
                    internalMutilation(new double[] { parancs[1], parancs[2], parancs[3] },
                            new double[] { parancs[4], parancs[5], parancs[6] }, (int) (parancs[7]));
                }
            }
        }
    }

    /**
     * Restores the state of this origami to the one before the last
     * {@link FoldingOperation folding operation} was executed. For this method
     * to work, the user should never use any method with a protected signature
     * in this class.
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
     * Equivalent to calling {@link #undo() undo} {@code steps} times, except it
     * will not do anything if {@code steps > history_pointer}.
     *
     * @param steps
     *            The number of steps to undo.
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
            while (history.get(history_pointer - 1)[0] == 5d) {
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
     * Pushes the empty polygons of this origami as close to the end of the
     * {@link #polygons() polygons} list as possible without moving the polygon
     * at the specified index.
     *
     * @param polygonIndex
     *            The zero-based index at which the polygon in the
     *            {@link #polygons() polygons} list should be kept in place.
     * @since 2013-09-04
     */
    protected void shrink(int polygonIndex) {

        ArrayList<Integer> tmp = polygons.get(polygonIndex);
        removePolygon(polygonIndex);
        for (int i = 0; i < polygons_size; i++) {
            if (polygons.get(i) == new ArrayList<Integer>() || polygons.get(i).isEmpty()) {

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
     * Removes every empty list from the {@link #polygons() polygons} of this
     * origami.
     *
     * @since 2013-09-04
     */
    protected void shrink() {

        for (int i = 0; i < polygons_size; i++) {
            if (polygons.get(i) == new ArrayList<Integer>() || polygons.get(i).isEmpty()) {

                removePolygon(i);
                i--;
            }
        }
    }

    /**
     * Returns the size of the smallest orthogonal square all the
     * {@link #corners() corners} of this origami can fit in.
     *
     * @return As described above.
     * @since 2013-10-31
     */
    public double circumscribedSquareSize() {
        return Math.max(paperWidth(), paperHeight());
    }

    /**
     * Returns the difference of the largest and the smallest first coordinate
     * occurring within the {@link #corners() corners} list.
     *
     * @return As described above.
     */
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

    /**
     * Returns the difference of the largest and the smallest second coordinate
     * occuring within the {@link #corners() corners} list.
     *
     * @return As described above.
     */
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

    final static private double[][] Origins = new double[][] {

            new double[] { 0, 0, 0 }, new double[] { 400, 0, 0 }, new double[] { 0, 400, 0 },
            new double[] { 0, 0, 400 } };

    /**
     * Emulates the plane equation compression done by the
     * {@link #commandBlock(int, double[], double[], int, int) commandBlock}
     * method. Used for previewing.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     * @return An array containing the 3-dimensional coordinates of a point the
     *         compressed plane goes through. Can be arbitrarily far from
     *         {@code ppoint}.
     * @see OrigamiIO#write_gen2(Origami, String)
     */
    static protected double[] planarPointRound(double[] ppoint, double[] pnormal) {

        double dist_max = -1;
        int used_origin = 0;
        double[] planeptnv = new double[] { 0, 0, 0 };

        for (int ii = 0; ii < Origins.length; ii++) {

            double[] basepoint = Geometry.line_plane_intersection(Origins[ii], pnormal, ppoint, pnormal);
            if (Geometry.vector_length(Geometry.vector(basepoint, Origins[ii])) > dist_max) {

                planeptnv = Geometry.vector(basepoint, Origins[ii]);
                dist_max = Geometry.vector_length(planeptnv);
                used_origin = ii;
            }
        }
        int Xe = (int) planeptnv[0];
        int Ye = (int) planeptnv[1];
        int Ze = (int) planeptnv[2];
        int Xt = (int) Math.round((Math.abs(planeptnv[0] - Xe)) * 256 * 256);
        int Yt = (int) Math.round((Math.abs(planeptnv[1] - Ye)) * 256 * 256);
        int Zt = (int) Math.round((Math.abs(planeptnv[2] - Ze)) * 256 * 256);
        return new double[] { (double) Xe + Math.signum(Xe) * Xt / 256 / 256 + Origins[used_origin][0],
                (double) Ye + Math.signum(Ye) * Yt / 256 / 256 + Origins[used_origin][1],
                (double) Ze + Math.signum(Ze) * Zt / 256 / 256 + Origins[used_origin][2] };
    }

    /**
     * Emulates the plane equation compression done by the
     * {@link #commandBlock(int, double[], double[], int, int) commandBlock}
     * method. Used for previewing.
     *
     * @param ppoint
     *            An array containing the 3-dimensional coordinates of a point
     *            the plane goes through as {@code double}s.
     * @param pnormal
     *            An array containing the 3-dimensional coordinates the plane's
     *            normal vector as {@code double}s.
     * @return An array containing the 3-dimensional coordinates of the
     *         compressed plane's normal vector.
     * @see OrigamiIO#write_gen2(Origami, String)
     */
    static protected double[] normalvectorRound(double[] ppoint, double[] pnormal) {

        double dist_max = -1;
        double[] planeptnv = new double[] { 0, 0, 0 };

        for (double[] origo : Origins) {

            double[] basepoint = Geometry.line_plane_intersection(origo, pnormal, ppoint, pnormal);
            if (Geometry.vector_length(Geometry.vector(basepoint, origo)) > dist_max) {
                planeptnv = Geometry.vector(basepoint, origo);
                dist_max = Geometry.vector_length(planeptnv);
            }
        }
        double sgn = 1;
        if (Geometry.scalar_product(pnormal, planeptnv) < 0) {
            sgn = -1;
        }
        int Xe = (int) planeptnv[0];
        int Ye = (int) planeptnv[1];
        int Ze = (int) planeptnv[2];
        int Xt = (int) Math.round((Math.abs(planeptnv[0] - Xe)) * 256 * 256);
        int Yt = (int) Math.round((Math.abs(planeptnv[1] - Ye)) * 256 * 256);
        int Zt = (int) Math.round((Math.abs(planeptnv[2] - Ze)) * 256 * 256);
        return new double[] { sgn * ((double) Xe + Math.signum(Xe) * Xt / 256 / 256),
                sgn * ((double) Ye + Math.signum(Ye) * Yt / 256 / 256),
                sgn * ((double) Ze + Math.signum(Ze) * Zt / 256 / 256) };
    }

    public ArrayList<double[]> foldingLine(double[] ppoint, double[] pnormal) {

        double[] ppoint1 = planarPointRound(ppoint, pnormal);
        double[] pnormal1 = normalvectorRound(ppoint, pnormal);
        ArrayList<double[]> line = new ArrayList<>();
        for (int polygonIndex = 0; polygonIndex < polygons_size; polygonIndex++) {

            if (isCut(ppoint1, pnormal1, polygonIndex)) {

                double[] start = null, end = null;
                for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

                    int j = (i + 1) % polygons.get(polygonIndex).size();
                    if (Geometry.point_on_plane(ppoint1, pnormal1, vertices.get(polygons.get(polygonIndex).get(i)))) {

                        end = start;
                        start = vertices.get(polygons.get(polygonIndex).get(i));
                    }
                    else {

                        if (Geometry.plane_between_points(ppoint1, pnormal1,
                                vertices.get(polygons.get(polygonIndex).get(i)),
                                vertices.get(polygons.get(polygonIndex).get(j)))
                                && !Geometry.point_on_plane(ppoint, pnormal,
                                        vertices.get(polygons.get(polygonIndex).get(j)))) {

                            double[] dirvec = Geometry.vector(vertices.get(polygons.get(polygonIndex).get(i)),
                                    vertices.get(polygons.get(polygonIndex).get(j)));
                            double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

                            end = start;
                            start = Geometry.line_plane_intersection(ipoint, dirvec, ppoint1, pnormal1);
                        }
                    }
                }
                if (start != null && end != null) {

                    line.add(start);
                    line.add(end);
                }
            }
        }
        return line;
    }

    public ArrayList<double[]> foldingLine2d(double[] ppoint, double[] pnormal) {

        double[] ppoint1 = planarPointRound(ppoint, pnormal);
        double[] pnormal1 = normalvectorRound(ppoint, pnormal);
        ArrayList<double[]> line = new ArrayList<>();
        for (int polygonIndex = 0; polygonIndex < polygons_size; polygonIndex++) {

            if (isCut(ppoint1, pnormal1, polygonIndex)) {

                double[] start = null, end = null;
                for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

                    int j = (i + 1) % polygons.get(polygonIndex).size();
                    if (Geometry.point_on_plane(ppoint1, pnormal1, vertices.get(polygons.get(polygonIndex).get(i)))) {

                        end = start;
                        start = vertices2d.get(polygons.get(polygonIndex).get(i));
                    }
                    else {

                        if (Geometry.plane_between_points(ppoint1, pnormal1,
                                vertices.get(polygons.get(polygonIndex).get(i)),
                                vertices.get(polygons.get(polygonIndex).get(j)))
                                && !Geometry.point_on_plane(ppoint, pnormal,
                                        vertices.get(polygons.get(polygonIndex).get(j)))) {

                            double[] dirvec = Geometry.vector(vertices.get(polygons.get(polygonIndex).get(i)),
                                    vertices.get(polygons.get(polygonIndex).get(j)));
                            double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

                            double[] meet = Geometry.line_plane_intersection(ipoint, dirvec, ppoint, pnormal);

                            double weight1 = Geometry.vector_length(
                                    Geometry.vector(meet, vertices.get(polygons.get(polygonIndex).get(j))));
                            double weight2 = Geometry.vector_length(
                                    Geometry.vector(meet, vertices.get(polygons.get(polygonIndex).get(i))));
                            end = start;
                            start = new double[] {
                                    (vertices2d.get(polygons.get(polygonIndex).get(i))[0] * weight1
                                            + vertices2d.get(polygons.get(polygonIndex).get(j))[0] * weight2)
                                            / (weight1 + weight2),
                                    (vertices2d.get(polygons.get(polygonIndex).get(i))[1] * weight1
                                            + vertices2d.get(polygons.get(polygonIndex).get(j))[1] * weight2)
                                            / (weight1 + weight2),
                                    0 };
                        }
                    }
                }
                if (start != null && end != null) {

                    line.add(start);
                    line.add(end);
                }
            }
        }
        return line;
    }

    public int foldType(double[] ppoint, double[] pnormal) {

        ArrayList<Integer> lines = new ArrayList<>();
        ArrayList<Integer> line_pols = new ArrayList<>();
        for (int i = 0; i < polygons_size; i++) {
            for (int vert : polygons.get(i)) {

                if (Geometry.point_on_plane(ppoint, pnormal, vertices.get(vert))) {

                    if (!lines.contains(vert)) {
                        lines.add(vert);
                    }
                    if (isStrictlyNonDegenerate(i)) {
                        line_pols.add(i);
                    }
                }
            }
        }
        int components = -1;
        while (!line_pols.isEmpty()) {

            line_pols.removeAll(polygonSelect(ppoint, pnormal, line_pols.get(0)));
            components++;
        }

        if (lines.size() < 2) {
            return 0;
        }

        if (components == 1) {

            boolean collin = false;
            int farpoint = -1;
            double dist_max = -1;

            for (int hp : lines) {
                if (Geometry.vector_length(Geometry.vector(vertices.get(hp), vertices.get(lines.get(0)))) > 0) {
                    collin = true;
                    if (Geometry
                            .vector_length(Geometry.vector(vertices.get(hp), vertices.get(lines.get(0)))) > dist_max) {
                        
                        farpoint = hp;
                        dist_max = Geometry
                                .vector_length(Geometry.vector(vertices.get(hp), vertices.get(lines.get(0))));
                    }
                }
            }

            if (collin) {
                for (int ii = 1; ii < lines.size() && ii != farpoint; ii++) {

                    if (Geometry.vector_length(Geometry.vector_product(
                            Geometry.vector(vertices.get(lines.get(0)), vertices.get(lines.get(ii))),
                            Geometry.vector(vertices.get(farpoint), vertices.get(lines.get(ii))))) > Geometry
                                    .vector_length(
                                            Geometry.vector(vertices.get(lines.get(0)), vertices.get(farpoint)))) {

                        collin = false;
                        break;
                    }
                }
            }

            for (int i = 0; i < lines.size(); i += 2) {

                if (border.contains(lines.get(i))) {

                    if (lines.size() == 2) {
                        return -1;
                    }
                    if (collin) {
                        return -2;
                    }
                    return -3;
                }
            }
            if (collin) {
                return -4;
            }
            return -5;
        }

        return components;
    }

    public int foldType(double[] ppoint, double[] pnormal, int polygonIndex) {

        ArrayList<Integer> line = new ArrayList<>();
        for (int spoly : polygonSelect(ppoint, pnormal, polygonIndex)) {
            for (int vert : polygons.get(spoly)) {

                if (Geometry.point_on_plane(ppoint, pnormal, vertices.get(vert))) {
                    line.add(vert);
                }
            }
        }
        if (line.size() < 2) {
            return 0;
        }

        boolean collin = false;
        int farpoint = -1;
        double dist_max = -1;

        for (int fp : line) {
            if (Geometry.vector_length(Geometry.vector(vertices.get(fp), vertices.get(line.get(0)))) > 0) {
                collin = true;
                if (Geometry.vector_length(Geometry.vector(vertices.get(fp), vertices.get(line.get(0)))) > dist_max) {
                    farpoint = fp;
                    dist_max = Geometry.vector_length(Geometry.vector(vertices.get(fp), vertices.get(line.get(0))));
                }
            }
        }

        if (collin) {
            for (int ii = 1; ii < line.size() && ii != farpoint; ii++) {

                if (Geometry.vector_length(
                        Geometry.vector_product(Geometry.vector(vertices.get(line.get(0)), vertices.get(line.get(ii))),
                                Geometry.vector(vertices.get(farpoint), vertices.get(line.get(ii))))) > Geometry
                                        .vector_length(
                                                Geometry.vector(vertices.get(line.get(0)), vertices.get(farpoint)))) {

                    collin = false;
                    break;
                }
            }
        }

        for (int i = 0; i < line.size(); i += 2) {

            if (border.contains(line.get(i))) {

                if (line.size() == 2) {
                    return -1;
                }
                if (collin) {
                    return -2;
                }
                return -3;
            }
        }
        if (collin) {
            return -4;
        }
        return -5;
    }

    @SuppressWarnings("unchecked")
    public int complexity(int step) {

        Origami origami = clone();
        if (step > origami.history_pointer) {
            return 0;
        }
        if (origami.history.get(step)[0] == 1) {

            origami.undo(origami.history_pointer - step);
            origami.redo(1);
            ArrayList<int[]> pairs = (ArrayList<int[]>) origami.cutpolygon_pairs.clone();
            origami.undo(1);
            int maxcompl = 0;

            while (!pairs.isEmpty()) {

                ArrayList<int[]> pairs_local = new ArrayList<>();
                pairs_local.add(pairs.remove(0));
                for (int i = 0; i < pairs_local.size(); i++) {
                    for (int ii = 0; ii < pairs.size(); ii++) {

                        for (int vert : origami.polygons.get(pairs.get(ii)[0])) {
                            if (origami.polygons.get(pairs_local.get(i)[0]).contains(vert)) {

                                pairs_local.add(pairs.remove(ii));
                                ii--;
                                break;
                            }
                        }
                    }
                }
                if (pairs_local.size() - 1 > maxcompl) {
                    maxcompl = pairs_local.size() - 1;
                }
            }

            return maxcompl;
        }
        if (origami.history.get(step)[0] == 3) {

            origami.undo(origami.history_pointer - step + 1);
            origami.redo(1);

            double[] point = new double[] { origami.history().get(step)[1], origami.history().get(step)[2],
                    origami.history().get(step)[3] };
            double[] normal = new double[] { origami.history().get(step)[4], origami.history().get(step)[5],
                    origami.history().get(step)[6] };
            int index = (int) origami.history().get(step)[7];

            ArrayList<int[]> pairs = (ArrayList<int[]>) origami.cutpolygon_pairs.clone();
            ArrayList<Integer> selection = origami.polygonSelect(point, normal, index);
            int compl = 0;

            for (int[] pair : pairs) {
                if (selection.contains(pair[0]) || selection.contains(pair[1])) {
                    compl++;
                }
            }

            return compl > 0 ? compl - 1 : 0;
        }
        return 0;
    }

    public int difficulty() {

        Origami origami = clone();
        origami.redoAll();

        int sum = 0;
        for (int i = 0; i < origami.history().size(); i++) {

            sum += origami.complexity(i);
        }
        return sum;
    }

    static public int difficultyLevel(int difficulty) {

        if (difficulty == 0) {
            return 0;
        }
        if (difficulty <= 50) {
            return 1;
        }
        if (difficulty <= 100) {
            return 2;
        }
        if (difficulty <= 200) {
            return 3;
        }
        if (difficulty <= 400) {
            return 4;
        }
        if (difficulty <= 800) {
            return 5;
        }
        return 6;
    }
    
    /**
     * Returns the index of the polygon in the {@link #polygons() polygons}
     * list that contains a specific point on the paper given in 2D (paper
     * space) coordinates. Returns -1 if every polygon in this origami is
     * {@link #isStrictlyNonDegenerate(int) degenerate}.
     * 
     * @param point2d The 2D coordinates of the point.
     * @return The index of the polygon containing the point.
     * @since 2017-02-19
     */
    public int findPolygonContaining(double... point2d) {
        
        //find the closest edge to point2d
        int[] closest_segment = new int[] { -1, -1 };
        double min_dist = -1;
        
        for (int i=0; i<polygons_size; i++) {
            
            if (isStrictlyNonDegenerate(i)) {
                
                ArrayList<Integer> poly = polygons.get(i);
                for (int ii=0; ii<poly.size() - 1; ii++) {
                    
                    double[] s1 = vertices2d.get(poly.get(ii));
                    double[] s2 = vertices2d.get(poly.get(ii+1));
                    double dist = Geometry.point_segment_distance(point2d, s1, s2);

                    if (dist < min_dist || min_dist == -1) {
                        
                        closest_segment[0] = poly.get(ii);
                        closest_segment[1] = poly.get(ii+1);
                        min_dist = dist;
                    }
                }
                
                double[] s1 = vertices2d.get(poly.get(poly.size() - 1));
                double[] s2 = vertices2d.get(poly.get(0));
                double dist = Geometry.point_segment_distance(point2d, s1, s2);

                if (dist < min_dist || min_dist == -1) {
                    
                    closest_segment[0] = poly.get(poly.size() - 1);
                    closest_segment[1] = poly.get(0);
                    min_dist = dist;
                }
            }
        }
        
        //there are no more than two polygons where this edge can belong
        int closest_poly1 = -1, closest_poly2 = -1;
        for (int i=0; i<polygons_size; i++) {
            if (isStrictlyNonDegenerate(i)) {
                ArrayList<Integer> poly = polygons.get(i);
                for (int ii=0; ii<poly.size() - 1; ii++) {
                    if (closest_segment[0] == poly.get(ii) && closest_segment[1] == poly.get(ii+1)
                            || closest_segment[1] == poly.get(ii) && closest_segment[0] == poly.get(ii+1)) {
                        if (closest_poly1 == -1) {
                            closest_poly1 = i;
                        }
                        else {
                            closest_poly2 = i;
                        }
                    }
                }
                if (closest_segment[0] == poly.get(poly.size() - 1) && closest_segment[1] == poly.get(0)
                        || closest_segment[1] == poly.get(poly.size() - 1) && closest_segment[0] == poly.get(0)) {
                    if (closest_poly1 == -1) {
                        closest_poly1 = i;
                    }
                    else {
                        closest_poly2 = i;
                    }
                }
            }
        }
        
        //one of these polygons is the polygon containing point2d
        if (closest_poly2 == -1) {
            return closest_poly1;
        }
        ArrayList<Integer> testpoly = polygons.get(closest_poly1);
        ArrayList<double[]> testpoints = new ArrayList<double[]>();
        for (int i=0; i<testpoly.size(); i++) {
            testpoints.add(vertices2d.get(testpoly.get(i)));
        }
        testpoints.add(point2d);
        
        if (Geometry.isConvex(Geometry.ccwWindingOrder(testpoints))) {
            return closest_poly2;
        }
        return closest_poly1;
    }
    
    /**
     * Determines where a given point on the paper ends up in the 3D space,
     * in the current state of this origami. For example, if an element of the
     * {@link #vertices2d() vertices2d} list is given, the corresponding
     * element of the {@link #vertices() vertices} list should be returned,
     * unless the origami is malformed.
     * 
     * @param point2d The 2D (paper space) coordinates of the point.
     * @return The 3D (origami space) coordinates of the same point.
     * @since 2017-02-19
     */
    public double[] find3dImageOf(double... point2d) {
        
        int poly_ind = findPolygonContaining(point2d);
        ArrayList<Integer> image_poly = polygons.get(poly_ind);
        double[] orig = vertices().get(image_poly.get(0));
        double[] orig2d = vertices2d().get(image_poly.get(0));
        
        for (int point1ind : image_poly) {
            for (int point2ind : image_poly) {

                double[] base1 = Geometry.vector(vertices().get(point1ind), orig);
                double[] base2 = Geometry.vector(vertices().get(point2ind), orig);
                
                if (Geometry.vector_length(Geometry.vector_product(base1, base2)) > 0) {
                    
                    base1 = Geometry.length_to_1(base1);
                    base2 = Geometry.length_to_1(base2);
                    
                    double[] base1_2d = Geometry.vector(vertices2d().get(point1ind), orig2d);
                    double[] base2_2d = Geometry.vector(vertices2d().get(point2ind), orig2d);
                    base1_2d = Geometry.length_to_1(base1_2d);
                    base2_2d = Geometry.length_to_1(base2_2d);
                    
                    double det = base1_2d[0]*base2_2d[1] - base1_2d[1]*base2_2d[0];

                    double coord1 = Geometry.scalar_product(Geometry.vector(point2d, orig2d),
                            new double[] {base2_2d[1], -base2_2d[0], 0})/det;
                    double coord2 = Geometry.scalar_product(Geometry.vector(point2d, orig2d),
                            new double[] {-base1_2d[1], base1_2d[0], 0})/det;
                    
                    double[] img = Geometry.sum(orig,
                            Geometry.sum(Geometry.scalar_multip(base1, coord1),
                                         Geometry.scalar_multip(base2, coord2)));
                    return img;
                    
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Origami clone() {

        Origami copy = new Origami(papertype);
        copy.corners = (ArrayList<double[]>) corners.clone();
        copy.history = (ArrayList<double[]>) history.clone();
        copy.history_stream = (ArrayList<int[]>) history_stream.clone();
        copy.history_pointer = history_pointer;
        copy.vertices_size = vertices_size;
        copy.vertices = (ArrayList<double[]>) vertices.clone();
        copy.vertices2d = (ArrayList<double[]>) vertices2d.clone();
        copy.polygons_size = polygons_size;
        copy.polygons = (ArrayList<ArrayList<Integer>>) polygons.clone();
        copy.last_cut_polygons = (ArrayList<ArrayList<Integer>>) last_cut_polygons.clone();
        copy.cutpolygon_nodes = (ArrayList<int[]>) cutpolygon_nodes.clone();
        copy.cutpolygon_pairs = (ArrayList<int[]>) cutpolygon_pairs.clone();
        copy.border = (ArrayList<Integer>) border.clone();
        return copy;
    }
}
