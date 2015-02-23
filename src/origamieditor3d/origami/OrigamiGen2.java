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
 * This is the first patch to the {@link Origami} class.
 * It resolves a serious bug in the
 * {@link Origami#cutPolygon(double[], double[], int) cutPolygon} method that
 * allowed contiguous polygons to overlap in the paper space and, as a
 * consequence, be torn apart in the origami space.
 * 
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiGen2 extends Origami {

    public OrigamiGen2(PaperType papertype) {
        super(papertype);
    }

    public OrigamiGen2(PaperType papertype, ArrayList<double[]> history) {
    	super(papertype, history);
    }

    public OrigamiGen2(ArrayList<double[]> corners) throws Exception {
    	super(corners);
    }

    public OrigamiGen2(ArrayList<double[]> corners, ArrayList<double[]> history) throws Exception {
    	super(corners, history);
    }
    
    @Override
    public int generation() {
        return 2;
    }
    
    @Override
    protected boolean cutPolygon(double[] ppoint, double[] pnormal, int polygonIndex) {

        if (isCut(ppoint, pnormal, polygonIndex)) {

            ArrayList<Integer> ujsokszog1 = new ArrayList<>();
            ArrayList<Integer> ujsokszog2 = new ArrayList<>();

            for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

                int j = (i +1) % polygons.get(polygonIndex).size();
                if (point_on_plane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)))) {

                    ujsokszog1.add(polygons.get(polygonIndex).get(i));
                    ujsokszog2.add(polygons.get(polygonIndex).get(i));
                } else {

                    if (scalar_product(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) > scalar_product(ppoint, pnormal)) {
                        ujsokszog1.add(polygons.get(polygonIndex).get(i));
                    } else {
                        ujsokszog2.add(polygons.get(polygonIndex).get(i));
                    }

                    if (plane_between_points(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)), vertices.get(polygons.get(polygonIndex).get(j))) && !point_on_plane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(j)))) {

                        freshcut:
                        {
                            for (int[] szakasz : cutpolygon_nodes) {
                                if (szakasz[0] == polygons.get(polygonIndex).get(i) && szakasz[1] == polygons.get(polygonIndex).get(j)) {
                                    ujsokszog1.add(szakasz[2]);
                                    ujsokszog2.add(szakasz[2]);
                                    break freshcut;
                                } else if (szakasz[0] == polygons.get(polygonIndex).get(j) && szakasz[1] == polygons.get(polygonIndex).get(i)) {
                                    ujsokszog1.add(szakasz[2]);
                                    ujsokszog2.add(szakasz[2]);
                                    break freshcut;
                                }
                            }
                            double D = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

                            double[] iranyvek = vector(vertices.get(polygons.get(polygonIndex).get(i)), vertices.get(polygons.get(polygonIndex).get(j)));
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

                            double suly1 = vector_length(vector(metszet, vertices.get(polygons.get(polygonIndex).get(j))));
                            double suly2 = vector_length(vector(metszet, vertices.get(polygons.get(polygonIndex).get(i))));
                            add2dVertex(new double[]{
                                (vertices2d.get(polygons.get(polygonIndex).get(i))[0] * suly1 + vertices2d.get(polygons.get(polygonIndex).get(j))[0] * suly2) / (suly1 + suly2),
                                (vertices2d.get(polygons.get(polygonIndex).get(i))[1] * suly1 + vertices2d.get(polygons.get(polygonIndex).get(j))[1] * suly2) / (suly1 + suly2),
                                0
                            });

                            ujsokszog1.add(vertices_size - 1);
                            ujsokszog2.add(vertices_size - 1);
                            cutpolygon_nodes.add(new int[]{polygons.get(polygonIndex).get(i), polygons.get(polygonIndex).get(j), vertices_size - 1});
                        }
                    }
                }
            }

            cutpolygon_pairs.add(new int[]{polygonIndex, polygons.size()});
            last_cut_polygons.add(polygons.get(polygonIndex));
            polygons.set(polygonIndex, ujsokszog1);
            addPolygon(ujsokszog2);
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
        copy.history_pointer = history_pointer;
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
