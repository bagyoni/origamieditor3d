package origamieditor3d;

public class OrigamiScripter {
    
    private static String vector2d(double... xy) {
        return " [" + xy[0] + " " + xy[1] + "] ";
    }
    
    private static String vector3d(double... xyz) {
        return " [" + xyz[0] + " " + xyz[1] + " " + xyz[2] + "] ";
    }
    
    private static String vector(double... p) {
        
        if (p.length == 2) {
            return vector2d(p);
        }
        if (p.length == 3) {
            return vector3d(p);
        }
        return null;
    }
    
    public static String target(double... xy) {
        return "target" + vector2d(xy);
    }
    
    public static String angle(int ang) {
        return "angle " + ang + " ";
    }
    
    public static String plane(double[] point, double[] normal) {
        return "plane" + vector(point) + vector3d(normal);
    }
    
    public static String planepoint(double... point) {
        return "planepoint" + vector(point);
    }
    
    public static String planenormal(double... normal) {
        return "planenormal" + vector3d(normal);
    }
    
    public static String planethrough(double[] p1, double[] p2, double[] p3) {
        return "planethrough" + vector(p1) + vector(p2) + vector(p3);
    }
    
    public static String angle_bisector(double[] p1, double[] p2, double[] p3) {
        return "angle-bisector" + vector(p1) + vector(p2) + vector(p3);
    }
    
    public static String reflect() {
        return "reflect ";
    }
    
    public static String rotate() {
        return "rotate ";
    }
    
    public static String cut() {
        return "cut ";
    }
}
