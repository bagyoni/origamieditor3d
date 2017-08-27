package origamieditor3d.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class BaseModels {

    public InputStream getFile(String name) {
        return getClass().getResourceAsStream("/res/models/"+name);
    }

    public ArrayList<String> names() {

        ArrayList<String> namelist = new ArrayList<>();
        Scanner sc = new Scanner(getClass().getResourceAsStream("/res/models/bases.list"));
        while(sc.hasNextLine()) {
            namelist.add(sc.nextLine());
        }
        sc.close();
        return namelist;
    }
}
