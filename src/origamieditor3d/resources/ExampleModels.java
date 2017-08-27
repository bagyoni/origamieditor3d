package origamieditor3d.resources;

import java.util.Scanner;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class ExampleModels {

    public InputStream getFile(String name) {
        return getClass().getResourceAsStream("/res/models/"+name);
    }

    public ArrayList<String> names() {

        ArrayList<String> namelist = new ArrayList<>();
        Scanner sc = new Scanner(getClass().getResourceAsStream("/res/models/examples.list"));
        while(sc.hasNextLine()) {
            namelist.add(sc.nextLine());
        }
        sc.close();
        return namelist;
    }
}
