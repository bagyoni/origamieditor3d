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
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-10-04
 */
public class OrigamiTracker {

    private Origami trackedOrigami;

    @SuppressWarnings("unchecked")
    public OrigamiTracker(Origami origami, double[] tracker) throws Exception {

    	if (origami instanceof OrigamiGen2) {

    		trackedOrigami = new OrigamiGen2(origami){

    		    @Override
    		    protected boolean cutPolygon(double[] ppoint, double[] pnormal, int polygonIndex) {

    		        if (isNonDegenerate(polygonIndex)) {

    		            boolean tracker = false;
    		            if ((int) polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1) == trackerpont) {

    		                ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                sokszog0.remove(sokszog0.size() - 1);
    		                polygons.set(polygonIndex, sokszog0);
    		                tracker = true;
    		            }
    		            ArrayList<Integer> s0 = polygons.get(polygonIndex);

    		            boolean osztodik = super.cutPolygon(ppoint, pnormal, polygonIndex);

    		            if (osztodik) {

    		                if (tracker) {

    		                    if (Geometry.point_on_plane(ppoint, pnormal, vertices.get(trackerpont))) {

    		                        ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                        sokszog0.add(trackerpont);
    		                        polygons.set(polygonIndex, sokszog0);
    		                    } else {
    		                        if (Geometry.scalar_product(vertices.get(trackerpont), pnormal) > Geometry.scalar_product(ppoint, pnormal)) {

    		                            ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                            sokszog0.add(trackerpont);
    		                            polygons.set(polygonIndex, sokszog0);

    		                        } else {

    		                            ArrayList<Integer> sokszog0 = polygons.get(polygons_size - 1);
    		                            sokszog0.add(trackerpont);
    		                            polygons.set(polygons_size - 1, sokszog0);
    		                        }
    		                    }

    		                    s0.add(trackerpont);
    		                    last_cut_polygons.set(last_cut_polygons.size() - 1, s0);
    		                }
    		            } else {
    		                if (tracker) {

    		                    ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                    sokszog0.add(trackerpont);
    		                    polygons.set(polygonIndex, sokszog0);
    		                }
    		            }
    		            return osztodik;
    		        }
    		        return false;
    		    }
    		};
    	} else {

    		trackedOrigami = new Origami(origami){

    		    @Override
    		    protected boolean cutPolygon(double[] ppoint, double[] pnormal, int polygonIndex) {

    		        if (isNonDegenerate(polygonIndex)) {

    		            boolean tracker = false;
    		            if ((int) polygons.get(polygonIndex).get(polygons.get(polygonIndex).size() - 1) == trackerpont) {

    		                ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                sokszog0.remove(sokszog0.size() - 1);
    		                polygons.set(polygonIndex, sokszog0);
    		                tracker = true;
    		            }
    		            ArrayList<Integer> s0 = polygons.get(polygonIndex);

    		            boolean osztodik = super.cutPolygon(ppoint, pnormal, polygonIndex);

    		            if (osztodik) {

    		                if (tracker) {

    		                    if (Geometry.point_on_plane(ppoint, pnormal, vertices.get(trackerpont))) {

    		                        ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                        sokszog0.add(trackerpont);
    		                        polygons.set(polygonIndex, sokszog0);
    		                    } else {
    		                        if (Geometry.scalar_product(vertices.get(trackerpont), pnormal) > Geometry.scalar_product(ppoint, pnormal)) {

    		                            ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                            sokszog0.add(trackerpont);
    		                            polygons.set(polygonIndex, sokszog0);

    		                        } else {

    		                            ArrayList<Integer> sokszog0 = polygons.get(polygons_size - 1);
    		                            sokszog0.add(trackerpont);
    		                            polygons.set(polygons_size - 1, sokszog0);
    		                        }
    		                    }

    		                    s0.add(trackerpont);
    		                    last_cut_polygons.set(last_cut_polygons.size() - 1, s0);
    		                }
    		            } else {
    		                if (tracker) {

    		                    ArrayList<Integer> sokszog0 = polygons.get(polygonIndex);
    		                    sokszog0.add(trackerpont);
    		                    polygons.set(polygonIndex, sokszog0);
    		                }
    		            }
    		            return osztodik;
    		        }
    		        return false;
    		    }
    		};
    	}

        trackerpont = trackedOrigami.vertices_size;
        trackedOrigami.history_pointer = origami.history_pointer;
        trackedOrigami.addVertex(new double[]{tracker[0], tracker[1], 0});
        trackedOrigami.add2dVertex(new double[]{tracker[0], tracker[1], 0});
        ArrayList<Integer> sokszog0 = (ArrayList<Integer>) trackedOrigami.polygons.get(0).clone();
        sokszog0.add(trackerpont);
        trackedOrigami.polygons.set(0, sokszog0);
    }
    private int trackerpont;

    public int trackPolygon() {

    	trackedOrigami.execute();
        for (int i = 0; i < trackedOrigami.polygons_size; i++) {
            if ((int) trackedOrigami.polygons.get(i).get(trackedOrigami.polygons.get(i).size() - 1) == trackerpont) {
                return i;
            }
        }
        return -1;
    }

    public double[] trackPoint() {

    	trackedOrigami.execute();
        return trackedOrigami.vertices.get(trackerpont);
    }
}
