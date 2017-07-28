package origamieditor3d.origami;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

	public static final double[] nullvector = new double[] { 0, 0, 0 };

	public static boolean isPlaneBetweenPoints(double[] ppoint, double[] pnormal, double[] A, double[] B) {

		double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
		boolean inner = false, outer = false;

		if ((A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst)
				/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) > 0.00000001) {
			inner = true;
		}
		if ((A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst)
				/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) < -0.00000001) {
			outer = true;
		}
		if ((B[0] * pnormal[0] + B[1] * pnormal[1] + B[2] * pnormal[2] - konst)
				/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) > 0.00000001) {
			inner = true;
		}
		if ((B[0] * pnormal[0] + B[1] * pnormal[1] + B[2] * pnormal[2] - konst)
				/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) < -0.00000001) {
			outer = true;
		}

		return inner && outer;
	}

	public static boolean isPointOnPlane(double[] ppoint, double[] pnormal, double[] A) {

		double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
		return (Math.abs(A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst)
				/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) < 1);
	}

	public static double[] crossProduct(double[] v1, double[] v2) {
		return new double[] { (v1[1] * v2[2] - v1[2] * v2[1]), (v1[2] * v2[0] - v1[0] * v2[2]),
				(v1[0] * v2[1] - v1[1] * v2[0]) };
	}

	public static double scalarProduct(double[] v1, double[] v2) {
		return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
	}

	public static double[] scalarMultiple(double[] v, double lambda) {
		return new double[] { v[0] * lambda, v[1] * lambda, v[2] * lambda };
	}

	public static double[] vectorDiff(double[] A, double[] B) {

		if (A.length == 3 && B.length == 3) {
			return new double[] { A[0] - B[0], A[1] - B[1], A[2] - B[2] };
		}

		return new double[] { A[0] - B[0], A[1] - B[1], 0 };
	}

	public static double[] vectorSum(double[] A, double[] B) {

		if (A.length == 3 && B.length == 3) {
			return new double[] { A[0] + B[0], A[1] + B[1], A[2] + B[2] };
		}

		return new double[] { A[0] + B[0], A[1] + B[1], 0 };
	}

	public static double[] midpoint(double[] A, double[] B) {
		return new double[] { (A[0] + B[0]) / 2, (A[1] + B[1]) / 2, (A[2] + B[2]) / 2 };
	}

	public static double vectorLength(double[] v) {

		double length = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
		return length;
	}

	public static double[] normalizeVector(double[] v) {

		double length = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
		return new double[] { v[0] / length, v[1] / length, v[2] / length };
	}

	public static double[] length_to_100(double[] v) {

		double length = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) * 0.01;
		return new double[] { v[0] / length, v[1] / length, v[2] / length };
	}

	public static double angle(double[] v1, double[] v2) {

		if (vectorLength(v1) > 0 && vectorLength(v2) > 0) {

			double arg1 = Math.acos(v1[0] / vectorLength(v1));
			if (v1[1] < 0) {
				arg1 = 2 * Math.PI - arg1;
			}
			double arg2 = Math.acos(v2[0] / vectorLength(v2));
			if (v2[1] < 0) {
				arg2 = 2 * Math.PI - arg2;
			}

			double angl = arg1 - arg2;
			while (angl < 0) {
				angl += 2 * Math.PI;
			}
			while (angl > 2 * Math.PI) {
				angl -= 2 * Math.PI;
			}

			return angl;

		}
		return 0;
	}

	public static double[] linePlaneIntersection(double[] lpoint, double[] ldir, double[] ppoint, double[] pnormal) {

		double D = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

		double X = lpoint[0];
		double Y = lpoint[1];
		double Z = lpoint[2];
		double U = ldir[0];
		double V = ldir[1];
		double W = ldir[2];
		double A = pnormal[0];
		double B = pnormal[1];
		double C = pnormal[2];
		double t = -(A * X + B * Y + C * Z - D) / (A * U + B * V + C * W);

		return new double[] { X + t * U, Y + t * V, Z + t * W };
	}

	public static double[] reflection(double[] v, double[] ppoint, double[] pnormal) {

		double[] basepoint = linePlaneIntersection(v, pnormal, ppoint, pnormal);
		return vectorSum(basepoint, vectorDiff(basepoint, v));
	}

	public static double[] rotation(double[] v, double[] lpoint, double[] ldir, double sinphi, double cosphi) {

		double Cx = ldir[0] / vectorLength(ldir);
		double Cy = ldir[1] / vectorLength(ldir);
		double Cz = ldir[2] / vectorLength(ldir);

		double X = v[0] - lpoint[0];
		double Y = v[1] - lpoint[1];
		double Z = v[2] - lpoint[2];

		double imgX = X * (cosphi + Cx * Cx * (1 - cosphi)) + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi)
				+ Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
		double imgY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi) + Y * (cosphi + Cy * Cy * (1 - cosphi))
				+ Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
		double imgZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi) + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi)
				+ Z * (cosphi + Cz * Cz * (1 - cosphi));

		double[] img = new double[] { imgX + lpoint[0], imgY + lpoint[1], imgZ + lpoint[2] };

		return img;
	}

	/**
	 * Arranges the planar points in the specified list in a counter-clockwise
	 * winding order as viewed from their center.
	 *
	 * @param polygon
	 *            A list containing the 2D coordinates of each point.
	 * @return An {@link ArrayList} containing the same elements as {@code
	 * polygon}, but in a counter-clockwise winding order.
	 * @since 2013-10-11
	 */
	public static ArrayList<double[]> ccwWindingOrder(List<double[]> polygon) {

		ArrayList<double[]> ordered = new ArrayList<>();
		ArrayList<Double> angles = new ArrayList<>();

		if (polygon.size() > 0) {

			double[] center = { 0, 0 };

			for (double[] vert : polygon) {
				center = new double[] { center[0] + vert[0] / polygon.size(), center[1] + vert[1] / polygon.size() };
			}

			for (int i = 0; i < polygon.size(); i++) {

				angles.add(angle(vectorDiff(polygon.get(i), center), new double[] { 1, 0, 0 }));
			}

			while (ordered.size() < polygon.size()) {

				Double minangle = null;
				double[] minpoint = nullvector;
				int mindex = -1;

				for (int i = 0; i < angles.size(); i++) {

					boolean smaller = (angles.get(i) != null) ? (minangle != null ? angles.get(i) < minangle : true)
							: false;
					if (smaller) {

						minangle = angles.get(i);
						minpoint = polygon.get(i);
						mindex = i;
					}
				}

				ordered.add(new double[] { minpoint[0], minpoint[1] });
				angles.set(mindex, null);
			}
		}
		return ordered;
	}

	/**
	 * Returns {@code true} iff the planar points in the specified list are the
	 * vertices of a convex polygon listed in a counter-clockwise winding order.
	 *
	 * @param polygon
	 *            A list containing the 2D coordinates of each point.
	 * @return As described above.
	 * @since 2013-10-12
	 */
	public static boolean isConvex(List<double[]> polygon) {

		if (polygon.size() > 3) {

			if (angle(vectorDiff(polygon.get(polygon.size() - 1), polygon.get(0)),
					vectorDiff(polygon.get(1), polygon.get(0))) > Math.PI) {
				return false;
			}

			for (int i = 1; i < polygon.size() - 1; i++) {

				if (angle(vectorDiff(polygon.get(i - 1), polygon.get(i)),
						vectorDiff(polygon.get(i + 1), polygon.get(i))) > Math.PI) {
					return false;
				}
			}

			if (angle(vectorDiff(polygon.get(polygon.size() - 2), polygon.get(polygon.size() - 1)),
					vectorDiff(polygon.get(0), polygon.get(polygon.size() - 1))) > Math.PI) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the distance between the specified point and line segment in 2D.
	 * 
	 * @param point
	 *            The 2D coordinates of the point.
	 * @param s1
	 *            The 2D coordinates of one endpoint of the segment.
	 * @param s2
	 *            The 2D coordinates of the other endpoint of the segment.
	 * @return As described above.
	 * @since 2017-02-19
	 */
	public static double pointSegmentDistance(double[] point, double[] s1, double[] s2) {

		double[] sdir = vectorDiff(s2, s1);
		if (vectorLength(sdir) < 0.00000001) {
			return vectorLength(vectorDiff(s1, point));
		}
		sdir = normalizeVector(sdir);

		double[] proj = vectorSum(s1, scalarMultiple(sdir, scalarProduct(sdir, vectorDiff(point, s1))));
		if (scalarProduct(vectorDiff(s1, proj), vectorDiff(s2, proj)) < 0) {
			return vectorLength(vectorDiff(proj, point));
		}
		else {
			double dist1 = vectorLength(vectorDiff(point, s1));
			double dist2 = vectorLength(vectorDiff(point, s2));
			return dist1 < dist2 ? dist1 : dist2;
		}
	}

	/**
	 * Determines whether the specified convex polygon contains the specified point.
	 * 
	 * @param point
	 *            The 2D coordinates of the point.
	 * @param polygon
	 *            A list containing the 2D coordinates of each point of the convex
	 *            polygon in a (counter-)clockwise winding order.
	 * @return true iff the point is in the polygon (including its border).
	 * @since 2017-03-04
	 */
	public static boolean pointInsidePolygon(double[] point, List<double[]> polygon) {

		if (polygon.size() > 2) {

			int sgn = 0;
			for (int i = 0; i < polygon.size() - 1; i++) {

				double cos = crossProduct(vectorDiff(polygon.get(i + 1), polygon.get(i)),
						vectorDiff(point, polygon.get(i)))[2];
				if (cos > 0) {
					if (sgn >= 0) {
						sgn = 1;
					}
					else {
						return false;
					}
				}
				if (cos < 0) {
					if (sgn <= 0) {
						sgn = -1;
					}
					else {
						return false;
					}
				}
			}
			double cos = crossProduct(vectorDiff(polygon.get(0), polygon.get(polygon.size() - 1)),
					vectorDiff(point, polygon.get(polygon.size() - 1)))[2];
			if (cos > 0) {
				if (sgn >= 0) {
					sgn = 1;
				}
				else {
					return false;
				}
			}
			if (cos < 0) {
				if (sgn <= 0) {
					sgn = -1;
				}
				else {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
