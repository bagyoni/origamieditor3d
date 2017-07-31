package origamieditor3d.origami;

import java.util.ArrayList;

/**
 * This is the first patch to the {@link OrigamiGen1} class. It resolves a serious
 * bug in the {@link OrigamiGen1#cutPolygon(double[], double[], int) cutPolygon}
 * method that allowed adjacent polygons to overlap in the paper space and, in
 * turn, get torn apart in the origami space.
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiGen2 extends OrigamiGen1 {

    public OrigamiGen2(PaperType papertype) {
        super(papertype);
    }

    public OrigamiGen2(ArrayList<double[]> corners) throws Exception {
        super(corners);
    }

    public OrigamiGen2(OrigamiGen1 origami) {
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
                if (Geometry.isPointOnPlane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)))) {

                    newpoly1.add(polygons.get(polygonIndex).get(i));
                    newpoly2.add(polygons.get(polygonIndex).get(i));
                }
                else {

                    if (Geometry.scalarProduct(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) > Geometry
                            .scalarProduct(ppoint, pnormal)) {
                        newpoly1.add(polygons.get(polygonIndex).get(i));
                    }
                    else {
                        newpoly2.add(polygons.get(polygonIndex).get(i));
                    }

                    if (Geometry.isPlaneBetweenPoints(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)),
                            vertices.get(polygons.get(polygonIndex).get(j)))
                            && !Geometry.isPointOnPlane(ppoint, pnormal,
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
                            double[] dirvec = Geometry.vectorDiff(vertices.get(polygons.get(polygonIndex).get(i)),
                                    vertices.get(polygons.get(polygonIndex).get(j)));
                            double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

                            double[] meet = Geometry.linePlaneIntersection(ipoint, dirvec, ppoint, pnormal);
                            addVertex(meet);

                            double weight1 = Geometry.vectorLength(
                                    Geometry.vectorDiff(meet, vertices.get(polygons.get(polygonIndex).get(j))));
                            double weight2 = Geometry.vectorLength(
                                    Geometry.vectorDiff(meet, vertices.get(polygons.get(polygonIndex).get(i))));
                            add2dVertex(new double[] {
                                    (vertices2d.get(polygons.get(polygonIndex).get(i))[0] * weight1
                                            + vertices2d.get(polygons.get(polygonIndex).get(j))[0] * weight2)
                                            / (weight1 + weight2),
                                    (vertices2d.get(polygons.get(polygonIndex).get(i))[1] * weight1
                                            + vertices2d.get(polygons.get(polygonIndex).get(j))[1] * weight2)
                                            / (weight1 + weight2),
                                    0 });

                            newpoly1.add(verticesSize - 1);
                            newpoly2.add(verticesSize - 1);
                            cutpolygon_nodes.add(new int[] { polygons.get(polygonIndex).get(i),
                                    polygons.get(polygonIndex).get(j), verticesSize - 1 });

                            for (int ii = 0; ii < border.size(); ii++) {
                                if (border.get(ii).equals(polygons.get(polygonIndex).get(i))) {
                                    if (border.get((ii + 1) % border.size())
                                            .equals(polygons.get(polygonIndex).get(j))) {

                                        border.add(ii + 1, verticesSize - 1);
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
    public OrigamiGen2 copy() {

        OrigamiGen2 copy = new OrigamiGen2(this);
        return copy;
    }
}
