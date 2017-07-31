package origamieditor3d.resources;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class ExampleModels {

    public java.io.InputStream getFile(String name) {
        return getClass().getResourceAsStream("/res/models/"+name);
    }

    public java.util.ArrayList<String> names() {

        java.util.ArrayList<String> namelist = new java.util.ArrayList<>();
        java.util.Scanner sc = new java.util.Scanner(getClass().getResourceAsStream("/res/models/examples.list"));
        while(sc.hasNextLine()) {
            namelist.add(sc.nextLine());
        }
        sc.close();
        return namelist;
    }
}
