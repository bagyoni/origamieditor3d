package origamieditor3d.script;

public class OrigamiScripter {
    
    private static String vector2d(double... xy) {
        return "[" + xy[0] + " " + xy[1] + "] ";
    }
    
    private static String vector3d(double... xyz) {
        return "[" + xyz[0] + " " + xyz[1] + " " + xyz[2] + "] ";
    }
    
    private static String vector4d(double... xyzw) {
        return "[" + xyzw[0] + " " + xyzw[1] + " " + xyzw[2] + " " + xyzw[3] + "] ";
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
    
    public static String paper(String ptype) {
        return "paper [" + ptype + "] ";
    }
    
    public static String paper(double left, double bottom, double right, double top) {
        return "paper " + vector4d(left, bottom, right, top);
    }
    
    public static String corner(double... xy) {
        return "corner " + vector2d(xy);
    }
    
    public static String target(double... xy) {
        return "target " + vector2d(xy);
    }
    
    public static String angle(int ang) {
        return "angle " + ang + " ";
    }
    
    public static String plane(double[] point, double[] normal) {
        return "plane " + vector(point) + vector3d(normal);
    }
    
    public static String planepoint(double... point) {
        return "planepoint " + vector(point);
    }
    
    public static String planenormal(double... normal) {
        return "planenormal " + vector3d(normal);
    }
    
    public static String planethrough(double[] p1, double[] p2, double[] p3) {
        return "planethrough " + vector(p1) + vector(p2) + vector(p3);
    }
    
    public static String angle_bisector(double[] p1, double[] p2, double[] p3) {
        return "angle-bisector " + vector(p1) + vector(p2) + vector(p3);
    }
    
    public static String camera(double[] dir, double[] axx, double[] axy) {
        return "camera " + vector3d(dir) + vector3d(axx) + vector3d(axy);
    }
    
    public static String filename(String path) {
        return "filename [" + path + "] ";
    }
    
    public static String title(String title) {
        return "title [" + title + "] ";
    }
    
    public static String color(int rgb) {
        return "color " + rgb + " ";
    }
    
    public static String _new() {
        return "new ";
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
    
    public static String undo() {
        return "undo ";
    }
    
    public static String redo() {
        return "redo ";
    }
    
    public static String open() {
        return "open ";
    }
    
    public static String load() {
        return "load ";
    }
    
    public static String load_texture() {
        return "load-texture ";
    }
    
    public static String unload_texture() {
        return "unload-texture ";
    }
    
    public static String uncolor() {
        return "uncolor ";
    }
    
    public static String export_ori() {
        return "export-ori ";
    }
    
    public static String export_ctm() {
        return "export-ctm ";
    }
    
    public static String export_autopdf() {
        return "export-autopdf ";
    }
    
    public static String export_gif() {
        return "export-gif ";
    }
    
    public static String export_revolving_gif() {
        return "export-revolving-gif ";
    }
    
    public static String export_png() {
        return "export-png ";
    }
    
    public static String export_jar() {
        return "export-jar ";
    }
}
