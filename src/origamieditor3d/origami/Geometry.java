package origamieditor3d.origami;


public class Geometry {

    final static public double[] nullvector = new double[]{0, 0, 0};

    static public boolean plane_between_points(double[] ppoint, double[] pnormal, double[] A, double[] B) {

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        boolean inner = false, outer = false;

        if ((A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst) / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) > 0.00000001) {
            inner = true;
        }
        if ((A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst) / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) < -0.00000001) {
            outer = true;
        }
        if ((B[0] * pnormal[0] + B[1] * pnormal[1] + B[2] * pnormal[2] - konst) / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) > 0.00000001) {
            inner = true;
        }
        if ((B[0] * pnormal[0] + B[1] * pnormal[1] + B[2] * pnormal[2] - konst) / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) < -0.00000001) {
            outer = true;
        }

        return inner && outer;
    }

    static public boolean point_on_plane(double[] ppoint, double[] pnormal, double[] A) {

        double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
        return (Math.abs(A[0] * pnormal[0] + A[1] * pnormal[1] + A[2] * pnormal[2] - konst) / Math.sqrt(Math.max(Geometry.scalar_product(pnormal, pnormal), 1)) < 1);
    }

    static public double[] vector_product(double[] v1, double[] v2) {
        return new double[]{(v1[1] * v2[2] - v1[2] * v2[1]), (v1[2] * v2[0] - v1[0] * v2[2]), (v1[0] * v2[1] - v1[1] * v2[0])};
    }

    static public double scalar_product(double[] v1, double[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    static public double[] scalar_multip(double[] v, double lambda) {
        return new double[]{v[0] * lambda, v[1] * lambda, v[2] * lambda};
    }

    static public double[] vector(double[] A, double[] B) {

        if (A.length == 3 && B.length == 3) {
            return new double[]{A[0] - B[0], A[1] - B[1], A[2] - B[2]};
        }

        return new double[]{A[0] - B[0], A[1] - B[1], 0};
    }

    static public double[] sum(double[] A, double[] B) {

        if (A.length == 3 && B.length == 3) {
            return new double[]{A[0] + B[0], A[1] + B[1], A[2] + B[2]};
        }

        return new double[]{A[0] + B[0], A[1] + B[1], 0};
    }

    static public double[] midpoint(double[] A, double[] B) {
        return new double[]{(A[0] + B[0]) / 2, (A[1] + B[1]) / 2, (A[2] + B[2]) / 2};
    }

    static public double vector_length(double[] v) {

        double length = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return length;
    }

    static public double[] length_to_1(double[] v) {

        double length = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return new double[]{v[0] / length, v[1] / length, v[2] / length};
    }

    static public double[] length_to_100(double[] v) {

        double length = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) * 0.01;
        return new double[]{v[0] / length, v[1] / length, v[2] / length};
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

    static public double[] line_plane_intersection(double[] lpoint, double[] ldir, double[] ppoint, double[] pnormal) {

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

        return new double[]{X + t * U, Y + t * V, Z + t * W};
    }

    static public double[] reflection(double[] v, double[] ppoint, double[] pnormal) {

        double[] basepoint = line_plane_intersection(v, pnormal, ppoint, pnormal);
        return sum(basepoint, vector(basepoint, v));
    }

    static public double[] rotation(double[] v, double[] lpoint, double[] ldir, double sinphi, double cosphi) {

        double Cx = ldir[0] / vector_length(ldir);
        double Cy = ldir[1] / vector_length(ldir);
        double Cz = ldir[2] / vector_length(ldir);

        double X = v[0] - lpoint[0];
        double Y = v[1] - lpoint[1];
        double Z = v[2] - lpoint[2];

        double imgX = X * (cosphi + Cx * Cx * (1 - cosphi))
                + Y * (Cx * Cy * (1 - cosphi) - Cz * sinphi)
                + Z * (Cx * Cz * (1 - cosphi) + Cy * sinphi);
        double imgY = X * (Cy * Cx * (1 - cosphi) + Cz * sinphi)
                + Y * (cosphi + Cy * Cy * (1 - cosphi))
                + Z * (Cy * Cz * (1 - cosphi) - Cx * sinphi);
        double imgZ = X * (Cz * Cx * (1 - cosphi) - Cy * sinphi)
                + Y * (Cz * Cy * (1 - cosphi) + Cx * sinphi)
                + Z * (cosphi + Cz * Cz * (1 - cosphi));

        double[] img = new double[]{
            imgX + lpoint[0],
            imgY + lpoint[1],
            imgZ + lpoint[2]
        };

        return img;
    }
}
