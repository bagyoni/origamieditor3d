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

/**
 * This is the first patch to the {@link Origami} class. It resolves a serious
 * bug in the {@link Origami#cutPolygon(double[], double[], int) cutPolygon}
 * method that allowed adjacent polygons to overlap in the paper space and, in
 * turn, get torn apart in the origami space.
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiGen2 extends Origami {

    public OrigamiGen2(PaperType papertype) {
        super(papertype);
    }

    public OrigamiGen2(ArrayList<double[]> corners) throws Exception {
        super(corners);
    }

    public OrigamiGen2(Origami origami) throws Exception {
        super(origami);
    }

    @Override
    public int generation() {
        return 2;
    }

    @Override
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
                            vertices.get(polygons.get(polygonIndex).get(j)))
                            && !Geometry.point_on_plane(ppoint, pnormal,
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

    @Override
    @SuppressWarnings("unchecked")
    public OrigamiGen2 clone() {

        OrigamiGen2 copy = new OrigamiGen2(papertype);
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
