package origamieditor3d.resources;

/**
 *
 * @author bsza
 */
public class BaseFolds {
    
    public java.util.ArrayList<java.io.InputStream> files() {
        
        java.util.ArrayList<java.io.InputStream> filelist = new java.util.ArrayList<>();
        java.util.Scanner sc = new java.util.Scanner(getClass().getResourceAsStream("/res/bases/0-filelist"));
        while(sc.hasNextLine()) {
            filelist.add(getClass().getResourceAsStream("/res/bases/"+sc.nextLine()));
        }
        return filelist;
    }
    
    public java.util.ArrayList<String> names() {
        
        java.util.ArrayList<String> namelist = new java.util.ArrayList<>();
        java.util.Scanner sc = new java.util.Scanner(getClass().getResourceAsStream("/res/bases/0-filelist"));
        while(sc.hasNextLine()) {
            namelist.add(sc.nextLine());
        }
        return namelist;
    }
}
