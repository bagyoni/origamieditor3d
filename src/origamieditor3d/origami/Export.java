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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Metódusokat nyújt az {@linkplain Origami} objektumok PDF és OpenCTM
 * formátumba exportálásához.
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public class Export {

    final static public int page_width = 595;
    final static public int page_height = 842;
    final static public int figure_frame = 200;

    static public int exportCTM(Origami origami, String filename, java.awt.image.BufferedImage texture) {

        try {

            Camera kamera = new Camera(0, 0, 1);
            kamera.adjust(origami);

            int haromszogek_hossz = 0;
            for (int i = 0; i < origami.polygons_size(); i++) {

                if (origami.isNonDegenerate(i)) {
                    haromszogek_hossz += origami.polygons().get(i).size() - 2;
                }
            }

            ArrayList<Byte> bajtlista = new ArrayList<>();
            int uj_int;

            //OCTM
            uj_int = 0x4d54434f;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //5. verzió
            uj_int = 0x00000005;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //RAW tömörítés
            uj_int = 0x00574152;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Pontok száma
            uj_int = origami.vertices_size();
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Háromszögek száma
            uj_int = haromszogek_hossz;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //UV térképek száma
            uj_int = texture == null ? 0 : 1;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Attibrútumtérképek száma
            uj_int = 0x00000000;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Csúcsonkénti merôlegesek nincsenek
            uj_int = 0x00000000;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Reklám
            uj_int = 0x00000020;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            uj_int = 0x43726561;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x74656420;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x77697468;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x204f7269;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x67616d69;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x20456469;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x746f7220;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            uj_int = 0x33442e20;
            bajtlista.add((byte) (uj_int >>> 24));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int));

            //INDX
            uj_int = 0x58444e49;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Háromszögek
            for (int i = 0; i < origami.polygons_size(); i++) {

                if (origami.isNonDegenerate(i)) {

                    for (int ii = 1; ii < origami.polygons().get(i).size() - 1; ii++) {

                        uj_int = origami.polygons().get(i).get(0);
                        bajtlista.add((byte) (uj_int));
                        bajtlista.add((byte) (uj_int >>> 8));
                        bajtlista.add((byte) (uj_int >>> 16));
                        bajtlista.add((byte) (uj_int >>> 24));

                        uj_int = origami.polygons().get(i).get(ii);
                        bajtlista.add((byte) (uj_int));
                        bajtlista.add((byte) (uj_int >>> 8));
                        bajtlista.add((byte) (uj_int >>> 16));
                        bajtlista.add((byte) (uj_int >>> 24));

                        uj_int = origami.polygons().get(i).get(ii + 1);
                        bajtlista.add((byte) (uj_int));
                        bajtlista.add((byte) (uj_int >>> 8));
                        bajtlista.add((byte) (uj_int >>> 16));
                        bajtlista.add((byte) (uj_int >>> 24));
                    }
                }
            }

            //VERT
            uj_int = 0x54524556;
            bajtlista.add((byte) (uj_int));
            bajtlista.add((byte) (uj_int >>> 8));
            bajtlista.add((byte) (uj_int >>> 16));
            bajtlista.add((byte) (uj_int >>> 24));

            //Csúcsok
            for (int i = 0; i < origami.vertices_size(); i++) {

                uj_int = Float.floatToIntBits((float) origami.vertices().get(i)[0] - (float) kamera.camera_pos[0]);
                bajtlista.add((byte) (uj_int));
                bajtlista.add((byte) (uj_int >>> 8));
                bajtlista.add((byte) (uj_int >>> 16));
                bajtlista.add((byte) (uj_int >>> 24));

                uj_int = Float.floatToIntBits((float) origami.vertices().get(i)[1] - (float) kamera.camera_pos[1]);
                bajtlista.add((byte) (uj_int));
                bajtlista.add((byte) (uj_int >>> 8));
                bajtlista.add((byte) (uj_int >>> 16));
                bajtlista.add((byte) (uj_int >>> 24));

                uj_int = Float.floatToIntBits((float) origami.vertices().get(i)[2] - (float) kamera.camera_pos[2]);
                bajtlista.add((byte) (uj_int));
                bajtlista.add((byte) (uj_int >>> 8));
                bajtlista.add((byte) (uj_int >>> 16));
                bajtlista.add((byte) (uj_int >>> 24));
            }

            if (texture != null) {

                uj_int = 0x43584554;
                bajtlista.add((byte) (uj_int));
                bajtlista.add((byte) (uj_int >>> 8));
                bajtlista.add((byte) (uj_int >>> 16));
                bajtlista.add((byte) (uj_int >>> 24));

                bajtlista.add((byte) 5);
                bajtlista.add((byte) 0);
                bajtlista.add((byte) 0);
                bajtlista.add((byte) 0);
                bajtlista.add((byte) 'P');
                bajtlista.add((byte) 'a');
                bajtlista.add((byte) 'p');
                bajtlista.add((byte) 'e');
                bajtlista.add((byte) 'r');

                long u = 0;
                File teximg = new File(filename + "-texture.png");

                while (teximg.exists()) {

                    teximg = new File(filename + "-texture" + u + ".png");
                    u++;
                }

                javax.imageio.ImageIO.write(texture, "png", teximg);

                bajtlista.add((byte) teximg.getName().length());
                bajtlista.add((byte) 0);
                bajtlista.add((byte) 0);
                bajtlista.add((byte) 0);
                for (int i = 0; i < teximg.getName().length(); i++) {
                    bajtlista.add((byte) teximg.getName().charAt(i));
                }

                //the UV mapping is defined by the vertices in the paper space
                for (int i = 0; i < origami.vertices_size(); i++) {

                    uj_int = Float.floatToIntBits((float) (origami.vertices2d().get(i)[0] / origami.paperWidth()));
                    bajtlista.add((byte) (uj_int));
                    bajtlista.add((byte) (uj_int >>> 8));
                    bajtlista.add((byte) (uj_int >>> 16));
                    bajtlista.add((byte) (uj_int >>> 24));

                    uj_int = Float.floatToIntBits((float) (1 - origami.vertices2d().get(i)[1] / origami.paperHeight()));
                    bajtlista.add((byte) (uj_int));
                    bajtlista.add((byte) (uj_int >>> 8));
                    bajtlista.add((byte) (uj_int >>> 16));
                    bajtlista.add((byte) (uj_int >>> 24));
                }
            }

            byte[] bajtok = new byte[bajtlista.size()];
            for (int i = 0; i < bajtlista.size(); i++) {

                bajtok[i] = bajtlista.get(i);
            }

            File ctm = new File(filename);
            if (ctm.exists()) {
                ctm.delete();
            }

            FileOutputStream str = new FileOutputStream(ctm);

            str.write(bajtok);
            str.close();
            kamera.unadjust(origami);

            return 1;

        } catch (IOException exc) {

            return 0;
        }
    }

    static public int exportPDF(Origami origami, String filename, String title) {

        try {

            Origami origami1 = origami.clone();
            File pdf = new File(filename);
            if (pdf.exists()) {
                pdf.delete();
            }
            FileOutputStream str = new FileOutputStream(pdf);

            //Itt tároljuk az objektumok offszeteit
            ArrayList<Integer> Offszetek = new ArrayList<>();
            Offszetek.add(0);
            int bajtszam = 0;

            //Megszámoljuk, hány mûvelet nem lesz külön feltüntetve
            int ures_muveletek = 0;
            ArrayList<Integer> UresIndexek = new ArrayList<>();

            for (int i = 0; i < origami1.history().size(); i++) {

                if (origami1.history().get(i)[0] == 2.0) {

                    if (i < origami1.history().size() - 1) {

                        if (origami1.history().get(i + 1)[0] == 2.0
                                && origami1.history().get(i + 1)[1] == origami1.history().get(i)[1]
                                && origami1.history().get(i + 1)[2] == origami1.history().get(i)[2]
                                && origami1.history().get(i + 1)[3] == origami1.history().get(i)[3]
                                && origami1.history().get(i + 1)[4] == origami1.history().get(i)[4]
                                && origami1.history().get(i + 1)[5] == origami1.history().get(i)[5]
                                && origami1.history().get(i + 1)[6] == origami1.history().get(i)[6]) {
                            ures_muveletek++;
                            UresIndexek.add(i);
                        }
                    }
                } else if (origami1.history().get(i)[0] == 4.0) {

                    if (i < origami1.history().size() - 1) {

                        if (origami1.history().get(i + 1)[0] == 4.0
                                && origami1.history().get(i + 1)[1] == origami1.history().get(i)[1]
                                && origami1.history().get(i + 1)[2] == origami1.history().get(i)[2]
                                && origami1.history().get(i + 1)[3] == origami1.history().get(i)[3]
                                && origami1.history().get(i + 1)[4] == origami1.history().get(i)[4]
                                && origami1.history().get(i + 1)[5] == origami1.history().get(i)[5]
                                && origami1.history().get(i + 1)[6] == origami1.history().get(i)[6]
                                && origami1.history().get(i + 1)[8] == origami1.history().get(i)[8]) {
                            ures_muveletek++;
                            UresIndexek.add(i);
                        }
                    }
                }
            }

            int forgatasok = 2;
            //Azok a lépések, amikhez szemszögváltás kell
            ArrayList<Integer> ForgatasIndexek = new ArrayList<>();
            ForgatasIndexek.add(0);
            //A szemszögváltások függôleges forgásszögei
            ArrayList<Integer> ForgatasSzogek = new ArrayList<>();
            ForgatasSzogek.add(0);

            //Méretezés és elôigazítás
            Camera kamera = new Camera(0, 0, 0.5);
            kamera.nextOrthogonalView();

            //Felmérjük az olyan lépések számát, amikhez szemszögváltás kell.
            for (int i = 1; i < origami1.history().size(); i++) {

                double[] regiVaszonNV = kamera.camera_dir;

                kamera.camera_dir = Origami.vector_product(new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]},
                        new double[]{0, 1, 0});

                if (Origami.vector_length(kamera.camera_dir) < .00000001) {
                    kamera.camera_dir = new double[]{0, 0, 1};
                }

                kamera.camera_dir = new double[]{kamera.camera_dir[0] / Origami.vector_length(kamera.camera_dir),
                    kamera.camera_dir[1] / Origami.vector_length(kamera.camera_dir),
                    kamera.camera_dir[2] / Origami.vector_length(kamera.camera_dir)};

                if (Origami.vector_length(Origami.vector_product(regiVaszonNV, kamera.camera_dir)) > .00000001 && !UresIndexek.contains(i - 1)) {

                    forgatasok++;
                    ForgatasIndexek.add(i);
                    double cos = Origami.scalar_product(regiVaszonNV, kamera.camera_dir) / Origami.vector_length(regiVaszonNV) / Origami.vector_length(kamera.camera_dir);
                    ForgatasSzogek.add((int) (Math.acos(cos >= -1 && cos <= 1 ? cos : 1) / Math.PI * 180));
                }
            }
            ForgatasIndexek.add(origami1.history().size());

            //Egy oldalon 6 cella van (papírmérettôl függetlenül)
            int cellak_szama = origami1.history().size() + forgatasok - ures_muveletek + 2;

            //Fejléc
            String fajl = "";
            fajl += "%PDF-1.3";
            fajl += (char) 10;
            fajl += (char) 10;

            //Katalógus
            Offszetek.add(fajl.length());
            fajl += "1 0 obj";
            fajl += (char) 10;
            fajl += "<< /Type /Catalog";
            fajl += (char) 10;
            fajl += " /Pages 2 0 R";
            fajl += (char) 10;
            fajl += ">>";
            fajl += (char) 10;
            fajl += "endobj";
            fajl += (char) 10;
            fajl += (char) 10;

            //Kötet
            Offszetek.add(fajl.length());
            fajl += "2 0 obj";
            fajl += (char) 10;
            fajl += "<< /Type /Pages";
            fajl += (char) 10;
            fajl += "/Kids [";
            fajl += "3 0 R";

            //Az oldalak száma a cellák számának hatoda felfelé kerekítve
            for (int i = 1; i < (int) Math.ceil((double) cellak_szama / 6); i++) {

                fajl += " " + Integer.toString(i + 3) + " 0 R";
            }
            fajl += "]";
            fajl += (char) 10;
            fajl += "/Count " + Integer.toString((int) Math.ceil((double) cellak_szama / 6));
            fajl += (char) 10;
            fajl += "/MediaBox [0 0 " + Integer.toString(page_width) + " " + Integer.toString(page_height) + "]";
            fajl += (char) 10;
            fajl += ">>";
            fajl += (char) 10;
            fajl += "endobj";
            fajl += (char) 10;
            fajl += (char) 10;

            //Oldalak
            for (int i = 0; i < (int) Math.ceil((double) cellak_szama / 6); i++) {

                Offszetek.add(fajl.length());
                fajl += "" + Integer.toString(i + 3) + " 0 obj";
                fajl += (char) 10;
                fajl += "<< /Type /Page";
                fajl += (char) 10;
                fajl += "/Parent 2 0 R";
                fajl += (char) 10;
                fajl += "/Resources";
                fajl += (char) 10;
                fajl += "<< /Font";
                fajl += (char) 10;
                fajl += "<< /F1";
                fajl += (char) 10;
                fajl += "<< /Type /Font";
                fajl += (char) 10;
                fajl += "/Subtype /Type1";
                fajl += (char) 10;
                fajl += "/BaseFont /Courier";
                fajl += (char) 10;
                fajl += ">>";
                fajl += (char) 10;
                fajl += ">>";
                fajl += (char) 10;
                fajl += ">>";
                fajl += (char) 10;
                fajl += "/Contents[";

                //Egy oldalon általánosan 6 kép és 6 szöveg objektum van
                //A fájltest elsô felében a képek, a másodikban a szövegek vannak
                for (int ii = (int) Math.ceil((double) cellak_szama / 6) + i * 6;
                        ii < (cellak_szama < (i + 1) * 6
                                ? (int) Math.ceil((double) cellak_szama / 6) + cellak_szama
                                : (int) Math.ceil((double) cellak_szama / 6) + (i + 1) * 6);
                        ii++) {
                    if (ii != (int) Math.ceil((double) cellak_szama / 6) + i * 6) {
                        fajl += " ";
                    }
                    fajl += Integer.toString(ii + 3) + " 0 R";
                    fajl += " " + Integer.toString(ii + cellak_szama + 3) + " 0 R";
                }
                fajl += "]";
                fajl += (char) 10;
                fajl += ">>";
                fajl += (char) 10;
                fajl += "endobj";
                fajl += (char) 10;
                fajl += (char) 10;
            }

            //A cím a megadott fájlnév
            Offszetek.add(fajl.length());
            String stream;
            stream = "BT";
            stream += (char) 10;
            stream += "/F1 18 Tf";
            stream += (char) 10;
            stream += "100 800 Td";
            stream += (char) 10;
            stream += "(" + title + Cookbook.PDF_TITLE + " Tj";
            stream += (char) 10;
            stream += "ET";
            stream += (char) 10;
            fajl += Integer.toString((int) Math.ceil((double) cellak_szama / 6) + 3) + " 0 obj";
            fajl += (char) 10;
            fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
            fajl += (char) 10;
            fajl += "stream";
            fajl += (char) 10;
            fajl += stream;
            fajl += "endstream";
            fajl += (char) 10;
            fajl += "endobj";
            fajl += (char) 10;
            fajl += (char) 10;

            //A cím alatti két üres cellában van helyünk a reklámozásra
            Offszetek.add(fajl.length());
            stream = "BT";
            stream += (char) 10;
            stream += "/F1 12 Tf";
            stream += (char) 10;
            stream += Integer.toString((int) (page_width - 2 * figure_frame) / 4) + " 760 Td";
            stream += (char) 10;
            stream += "14 TL";
            stream += (char) 10;
            stream += Cookbook.PDF_DISCLAIMER;
            stream += (char) 10;
            stream += "ET";
            stream += (char) 10;
            fajl += Integer.toString((int) Math.ceil((double) cellak_szama / 6) + 4) + " 0 obj";
            fajl += (char) 10;
            fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
            fajl += (char) 10;
            fajl += "stream";
            fajl += (char) 10;
            fajl += stream;
            fajl += "endstream";
            fajl += (char) 10;
            fajl += "endobj";
            fajl += (char) 10;
            fajl += (char) 10;
            str.write(fajl.getBytes(Charset.forName("UTF-8")));
            bajtszam += fajl.length();
            fajl = "";

            //Ez már élesben megy
            origami1.reset();
            Double maxdim = origami1.circumscribedSquareSize();
            if (maxdim == .0) {
                maxdim = 1.;
            }

            kamera = new Camera(0, 0, figure_frame / maxdim);
            kamera.nextOrthogonalView();
            kamera.unadjust(origami1);

            //Az objektum indexe, ahol épp tartunk
            int objindex = (int) Math.ceil((double) cellak_szama / 6) + 5;

            //Ábrák
            for (int i = 0; i <= origami1.history().size(); i++) {

                while (UresIndexek.contains(i - 1)) {

                    origami1.execute(i - 1, 1);
                    i++;
                }

                origami1.execute(i - 1, 1);

                int x = 0, y = 0;
                String kep;

                if (ForgatasIndexek.contains(i)) {

                    switch ((objindex - (int) Math.ceil((double) cellak_szama / 6)) % 6) {

                        case 0:
                            x = page_width / 4 * 3;
                            y = page_height / 6 * 3 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 1:
                            x = page_width / 4 * 1;
                            y = page_height / 6 * 1 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 2:
                            x = page_width / 4 * 3;
                            y = page_height / 6 * 1 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 3:
                            x = page_width / 4 * 1;
                            y = page_height / 6 * 5 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 4:
                            x = page_width / 4 * 3;
                            y = page_height / 6 * 5 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 5:
                            x = page_width / 4 * 1;
                            y = page_height / 6 * 3 + (page_height / 3 - figure_frame) / 4;
                            break;

                        default:
                            break;
                    }

                    kep = kamera.drawFaces(x, y, origami1) + kamera.drawEdges(x, y, origami1);

                    Offszetek.add(bajtszam);
                    stream = "q";
                    stream += " ";
                    stream += kep;
                    stream += "Q";
                    stream += (char) 10;
                    fajl += Integer.toString(objindex) + " 0 obj";
                    fajl += (char) 10;
                    fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
                    fajl += (char) 10;
                    fajl += "stream";
                    fajl += (char) 10;
                    fajl += stream;
                    fajl += "endstream";
                    fajl += (char) 10;
                    fajl += "endobj";
                    fajl += (char) 10;
                    fajl += (char) 10;
                    objindex++;
                    str.write(fajl.getBytes(Charset.forName("UTF-8")));
                    bajtszam += fajl.length();
                    fajl = "";
                }

                if (i < origami1.history().size()) {

                    double[] regiVaszonNV = kamera.camera_dir;

                    kamera.camera_dir = Origami.vector_product(new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]},
                            new double[]{0, 1, 0});

                    if (Origami.scalar_product(kamera.camera_dir, kamera.camera_dir) < 0.00000001) {
                        kamera.camera_dir = new double[]{0, 0, 1};
                    }

                    kamera.camera_dir = new double[]{kamera.camera_dir[0] / Origami.vector_length(kamera.camera_dir),
                        kamera.camera_dir[1] / Origami.vector_length(kamera.camera_dir),
                        kamera.camera_dir[2] / Origami.vector_length(kamera.camera_dir)};

                    kamera.axis_y = new double[]{0, 1, 0};
                    kamera.axis_x = Origami.vector_product(kamera.camera_dir, kamera.axis_y);

                    kamera.axis_x = new double[]{kamera.axis_x[0] / Origami.vector_length(kamera.axis_x) * kamera.zoom(),
                        kamera.axis_x[1] / Origami.vector_length(kamera.axis_x) * kamera.zoom(),
                        kamera.axis_x[2] / Origami.vector_length(kamera.axis_x) * kamera.zoom()};

                    kamera.axis_y = new double[]{kamera.axis_y[0] / Origami.vector_length(kamera.axis_y) * kamera.zoom(),
                        kamera.axis_y[1] / Origami.vector_length(kamera.axis_y) * kamera.zoom(),
                        kamera.axis_y[2] / Origami.vector_length(kamera.axis_y) * kamera.zoom()};

                    if (Origami.scalar_product(regiVaszonNV, kamera.camera_dir) < 0 && !ForgatasIndexek.contains(i)) {

                        kamera.camera_dir = Origami.vector(Origami.nullvektor, kamera.camera_dir);
                        kamera.axis_x = Origami.vector(Origami.nullvektor, kamera.axis_x);
                    }

                    switch ((objindex - (int) Math.ceil((double) cellak_szama / 6)) % 6) {

                        case 0:
                            x = page_width / 4 * 3;
                            y = page_height / 6 * 3 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 1:
                            x = page_width / 4 * 1;
                            y = page_height / 6 * 1 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 2:
                            x = page_width / 4 * 3;
                            y = page_height / 6 * 1 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 3:
                            x = page_width / 4 * 1;
                            y = page_height / 6 * 5 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 4:
                            x = page_width / 4 * 3;
                            y = page_height / 6 * 5 + (page_height / 3 - figure_frame) / 4;
                            break;

                        case 5:
                            x = page_width / 4 * 1;
                            y = page_height / 6 * 3 + (page_height / 3 - figure_frame) / 4;
                            break;

                        default:
                            break;
                    }

                    double[] sikpont;
                    double[] siknv;

                    kamera.adjust(origami1);

                    switch ((int) origami1.history().get(i)[0]) {

                        case 1:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawFaces(x, y, origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        case 2:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawFaces(x, y, origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        case 3:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawSelection(x, y, sikpont, siknv, (int) origami1.history().get(i)[7], origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        case 4:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawSelection(x, y, sikpont, siknv, (int) origami1.history().get(i)[8], origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        case 5:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawFaces(x, y, origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        case 6:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawFaces(x, y, origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        case 7:
                            sikpont = new double[]{origami1.history().get(i)[1], origami1.history().get(i)[2], origami1.history().get(i)[3]};
                            siknv = new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]};
                            kep = kamera.drawSelection(x, y, sikpont, siknv, (int) origami1.history().get(i)[7], origami1) + kamera.drawEdges(x, y, origami1) + kamera.pfdLiner(x, y, sikpont, siknv);
                            break;

                        default:
                            kep = kamera.drawFaces(x, y, origami1) + kamera.drawEdges(x, y, origami1);
                            break;
                    }

                    Offszetek.add(bajtszam);
                    stream = "q";
                    stream += " ";
                    stream += kep;
                    stream += "Q";
                    stream += (char) 10;
                    fajl += Integer.toString(objindex) + " 0 obj";
                    fajl += (char) 10;
                    fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
                    fajl += (char) 10;
                    fajl += "stream";
                    fajl += (char) 10;
                    fajl += stream;
                    fajl += "endstream";
                    fajl += (char) 10;
                    fajl += "endobj";
                    fajl += (char) 10;
                    fajl += (char) 10;
                    objindex++;
                    str.write(fajl.getBytes(Charset.forName("UTF-8")));
                    bajtszam += fajl.length();
                    fajl = "";
                }
            }

            Offszetek.add(bajtszam);
            stream = "BT";
            stream += (char) 10;
            stream += "/F1 12 Tf";
            stream += (char) 10;
            stream += Integer.toString((int) (page_width - 2 * figure_frame) / 4) + " "
                    + Integer.toString(736 - Cookbook.PDF_DISCLAIMER.length() * 14 + Cookbook.PDF_DISCLAIMER.replace(") '", ") ").length() * 14) + " Td";
            stream += (char) 10;
            stream += Cookbook.PDF_PAPERTYPE + origami1.papertype().toString() + ") Tj";
            stream += (char) 10;
            stream += "ET";
            stream += (char) 10;
            fajl += Integer.toString(objindex) + " 0 obj";
            fajl += (char) 10;
            fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
            fajl += (char) 10;
            fajl += "stream";
            fajl += (char) 10;
            fajl += stream;
            fajl += "endstream";
            fajl += (char) 10;
            fajl += "endobj";
            fajl += (char) 10;
            fajl += (char) 10;
            objindex++;
            str.write(fajl.getBytes(Charset.forName("UTF-8")));
            bajtszam += fajl.length();
            fajl = "";

            Offszetek.add(bajtszam);
            stream = "BT";
            stream += (char) 10;
            stream += "/F1 12 Tf";
            stream += (char) 10;
            stream += Integer.toString((int) (page_width - 2 * figure_frame) / 4) + " "
                    + Integer.toString(722 - Cookbook.PDF_DISCLAIMER.length() * 14 + Cookbook.PDF_DISCLAIMER.replace(") '", ") ").length() * 14) + " Td";
            stream += (char) 10;
            stream += Cookbook.PDF_STEPS + Integer.toString(cellak_szama - 2) + ") Tj";
            stream += (char) 10;
            stream += "ET";
            stream += (char) 10;
            fajl += Integer.toString(objindex) + " 0 obj";
            fajl += (char) 10;
            fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
            fajl += (char) 10;
            fajl += "stream";
            fajl += (char) 10;
            fajl += stream;
            fajl += "endstream";
            fajl += (char) 10;
            fajl += "endobj";
            fajl += (char) 10;
            fajl += (char) 10;
            objindex++;
            str.write(fajl.getBytes(Charset.forName("UTF-8")));
            bajtszam += fajl.length();
            fajl = "";

            int sorszam = 1;

            //Szövegek
            for (int i = 0; i <= origami1.history().size(); i++) {

                while (UresIndexek.contains(i)) {
                    i++;
                }

                String utasitas = "";
                String koo = "";

                double[] siknv;

                if (ForgatasIndexek.contains(i)) {

                    if (i == origami1.history().size()) {

                        utasitas = "(" + Integer.toString(sorszam) + ". " + Cookbook.PDF_OUTRO;
                        sorszam++;
                    } else if (i == 0) {

                        utasitas = "(" + Integer.toString(sorszam) + ". ";
                        switch (origami1.papertype()) {

                            case A4:
                                utasitas += Cookbook.PDF_INTRO_A4;
                                break;
                            case Square:
                                utasitas += Cookbook.PDF_INTRO_SQUARE;
                                break;
                            case Hexagon:
                                utasitas += Cookbook.PDF_INTRO_HEX;
                                break;
                            case Dollar:
                                utasitas += Cookbook.PDF_INTRO_DOLLAR;
                                break;
                            case Custom:
                                if (origami1.corners().size() == 3) {
                                    utasitas += Cookbook.PDF_INTRO_TRIANGLE;
                                } else if (origami1.corners().size() == 4) {
                                    utasitas += Cookbook.PDF_INTRO_QUAD;
                                } else {
                                    utasitas += Cookbook.PDF_INTRO_POLYGON;
                                }
                                break;
                            default:
                                break;
                        }
                        sorszam++;
                    } else {

                        utasitas = "(" + Integer.toString(sorszam) + ". "
                                + Cookbook.PDF_TURN + Integer.toString(ForgatasSzogek.get(ForgatasIndexek.indexOf(i)))
                                + Cookbook.PDF_TURN_ANGLE;
                        sorszam++;
                    }

                    switch ((sorszam + 1) % 6) {

                        case 1:
                            koo = Integer.toString(page_width / 2 * 0 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 2 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 2:
                            koo = Integer.toString(page_width / 2 * 1 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 2 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 3:
                            koo = Integer.toString(page_width / 2 * 0 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 1 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 4:
                            koo = Integer.toString(page_width / 2 * 1 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 1 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 5:
                            koo = Integer.toString(page_width / 2 * 0 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 0 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 0:
                            koo = Integer.toString(page_width / 2 * 1 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 0 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        default:
                            break;
                    }

                    Offszetek.add(bajtszam);
                    stream = "BT";
                    stream += (char) 10;
                    stream += "/F1 10 Tf";
                    stream += (char) 10;
                    stream += koo + " Td";
                    stream += (char) 10;
                    stream += "12 TL";
                    stream += (char) 10;
                    stream += utasitas;
                    stream += (char) 10;
                    stream += "ET";
                    stream += (char) 10;
                    fajl += Integer.toString(objindex + sorszam - 2) + " 0 obj";
                    fajl += (char) 10;
                    fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
                    fajl += (char) 10;
                    fajl += "stream";
                    fajl += (char) 10;
                    fajl += stream;
                    fajl += "endstream";
                    fajl += (char) 10;
                    fajl += "endobj";
                    fajl += (char) 10;
                    fajl += (char) 10;
                    str.write(fajl.getBytes(Charset.forName("UTF-8")));
                    bajtszam += fajl.length();
                    fajl = "";
                }

                if (i < origami1.history().size()) {

                    double[] regiVaszonNV = kamera.camera_dir;

                    kamera.camera_dir = Origami.vector_product(new double[]{origami1.history().get(i)[4], origami1.history().get(i)[5], origami1.history().get(i)[6]},
                            new double[]{0, 1, 0});

                    if (Origami.scalar_product(kamera.camera_dir, kamera.camera_dir) < 0.00000001) {
                        kamera.camera_dir = new double[]{0, 0, 1};
                    }

                    kamera.camera_dir = new double[]{kamera.camera_dir[0] / Origami.vector_length(kamera.camera_dir),
                        kamera.camera_dir[1] / Origami.vector_length(kamera.camera_dir),
                        kamera.camera_dir[2] / Origami.vector_length(kamera.camera_dir)};

                    kamera.axis_y = new double[]{0, 1, 0};
                    kamera.axis_x = Origami.vector_product(kamera.camera_dir, kamera.axis_y);

                    kamera.axis_x = new double[]{kamera.axis_x[0] / Origami.vector_length(kamera.axis_x) * kamera.zoom(),
                        kamera.axis_x[1] / Origami.vector_length(kamera.axis_x) * kamera.zoom(),
                        kamera.axis_x[2] / Origami.vector_length(kamera.axis_x) * kamera.zoom()};

                    kamera.axis_y = new double[]{kamera.axis_y[0] / Origami.vector_length(kamera.axis_y) * kamera.zoom(),
                        kamera.axis_y[1] / Origami.vector_length(kamera.axis_y) * kamera.zoom(),
                        kamera.axis_y[2] / Origami.vector_length(kamera.axis_y) * kamera.zoom()};

                    if (Origami.scalar_product(regiVaszonNV, kamera.camera_dir) < 0 && !ForgatasIndexek.contains(i)) {

                        kamera.camera_dir = Origami.vector(Origami.nullvektor, kamera.camera_dir);
                        kamera.axis_x = Origami.vector(Origami.nullvektor, kamera.axis_x);
                    }

                    switch ((int) origami1.history().get(i)[0]) {

                        case 1:
                            siknv = new double[]{origami1.history().get(i)[4],
                                origami1.history().get(i)[5],
                                origami1.history().get(i)[6]};
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            switch (kamera.pdfLinerDir(siknv)) {

                                case Camera.PDF_NORTH:
                                    utasitas += Cookbook.PDF_REFLECT_NORTH;
                                    break;
                                case Camera.PDF_EAST:
                                    utasitas += Cookbook.PDF_REFLECT_EAST;
                                    break;
                                case Camera.PDF_SOUTH:
                                    utasitas += Cookbook.PDF_REFLECT_SOUTH;
                                    break;
                                case Camera.PDF_WEST:
                                    utasitas += Cookbook.PDF_REFLECT_WEST;
                                    break;
                                default:
                                    break;
                            }
                            sorszam++;
                            break;

                        case 2:
                            siknv = new double[]{origami1.history().get(i)[4],
                                origami1.history().get(i)[5],
                                origami1.history().get(i)[6]};
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            switch (kamera.pdfLinerDir(siknv)) {

                                case Camera.PDF_NORTH:
                                    utasitas += Cookbook.PDF_ROTATE_NORTH;
                                    break;
                                case Camera.PDF_EAST:
                                    utasitas += Cookbook.PDF_ROTATE_EAST;
                                    break;
                                case Camera.PDF_SOUTH:
                                    utasitas += Cookbook.PDF_ROTATE_SOUTH;
                                    break;
                                case Camera.PDF_WEST:
                                    utasitas += Cookbook.PDF_ROTATE_WEST;
                                    break;
                                default:
                                    break;
                            }
                            int szog = 0;
                            int j = i - 1;
                            while (UresIndexek.contains(j)) {

                                if (origami1.history().get(j)[0] == 2.0) {

                                    szog += (int) origami1.history().get(j)[7];
                                }
                                j--;
                            }
                            utasitas += Integer.toString(szog + (int) origami1.history().get(i)[7])
                                    + Cookbook.PDF_ROTATE_ANGLE;
                            sorszam++;
                            break;

                        case 3:
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            utasitas += Cookbook.PDF_REFLECT_TARGET;
                            sorszam++;
                            break;

                        case 4:
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            utasitas += Cookbook.PDF_ROTATE_TARGET;
                            int szog1 = 0;
                            int j1 = i - 1;
                            while (UresIndexek.contains(j1)) {

                                if (origami1.history().get(j1)[0] == 4.0) {

                                    szog1 += (int) origami1.history().get(j1)[7];
                                }
                                j1--;
                            }
                            utasitas += Integer.toString(szog1 + (int) origami1.history().get(i)[7])
                                    + Cookbook.PDF_ROTATE_ANGLE;
                            sorszam++;
                            break;

                        case 5:
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            utasitas += Cookbook.PDF_CREASE + Integer.toString(sorszam + 1) + Cookbook.PDF_CREASE_STEP;
                            sorszam++;
                            break;

                        case 6:
                            siknv = new double[]{origami1.history().get(i)[4],
                                origami1.history().get(i)[5],
                                origami1.history().get(i)[6]};
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            switch (kamera.pdfLinerDir(siknv)) {

                                case Camera.PDF_NORTH:
                                    utasitas += Cookbook.PDF_CUT_NORTH;
                                    break;
                                case Camera.PDF_EAST:
                                    utasitas += Cookbook.PDF_CUT_EAST;
                                    break;
                                case Camera.PDF_SOUTH:
                                    utasitas += Cookbook.PDF_CUT_SOUTH;
                                    break;
                                case Camera.PDF_WEST:
                                    utasitas += Cookbook.PDF_CUT_WEST;
                                    break;
                                default:
                                    break;
                            }
                            sorszam++;
                            break;

                        case 7:
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            utasitas += Cookbook.PDF_CUT_TARGET;
                            sorszam++;
                            break;

                        default:
                            utasitas = "(" + Integer.toString(sorszam) + ". ";
                            utasitas += "???) ' ";
                            sorszam++;
                            break;
                    }

                    switch ((sorszam + 1) % 6) {

                        case 1:
                            koo = Integer.toString(page_width / 2 * 0 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 2 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 2:
                            koo = Integer.toString(page_width / 2 * 1 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 2 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 3:
                            koo = Integer.toString(page_width / 2 * 0 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 1 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 4:
                            koo = Integer.toString(page_width / 2 * 1 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 1 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 5:
                            koo = Integer.toString(page_width / 2 * 0 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 0 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        case 0:
                            koo = Integer.toString(page_width / 2 * 1 + (page_width / 2 - figure_frame) / 3);
                            koo += " ";
                            koo += Integer.toString(page_height / 3 * 0 + (page_height / 3 - figure_frame) / 2 + (page_height / 3 - figure_frame) / 4);
                            break;
                        default:
                            break;
                    }

                    Offszetek.add(bajtszam);
                    stream = "BT";
                    stream += (char) 10;
                    stream += "/F1 10 Tf";
                    stream += (char) 10;
                    stream += koo + " Td";
                    stream += (char) 10;
                    stream += "12 TL";
                    stream += (char) 10;
                    stream += utasitas;
                    stream += (char) 10;
                    stream += "ET";
                    stream += (char) 10;
                    fajl += Integer.toString(objindex + sorszam - 2) + " 0 obj";
                    fajl += (char) 10;
                    fajl += "<< /Length " + Integer.toString(stream.length()) + " >>";
                    fajl += (char) 10;
                    fajl += "stream";
                    fajl += (char) 10;
                    fajl += stream;
                    fajl += "endstream";
                    fajl += (char) 10;
                    fajl += "endobj";
                    fajl += (char) 10;
                    fajl += (char) 10;
                    str.write(fajl.getBytes(Charset.forName("UTF-8")));
                    bajtszam += fajl.length();
                    fajl = "";
                }
            }

            int xroffszet = bajtszam;

            fajl += "xref";
            fajl += (char) 10;
            fajl += "0 " + Integer.toString(Offszetek.size());
            fajl += (char) 10;
            fajl += "0000000000 65535 f ";
            fajl += (char) 10;

            for (int i = 1; i < Offszetek.size(); i++) {

                for (int ii = 0; ii < 10 - Integer.toString(Offszetek.get(i)).length(); ii++) {
                    fajl += "0";
                }
                fajl += Integer.toString(Offszetek.get(i));
                fajl += " 00000 n ";
                fajl += (char) 10;
            }

            fajl += "trailer";
            fajl += (char) 10;
            fajl += "<< /Root 1 0 R";
            fajl += (char) 10;
            fajl += "/Size " + Integer.toString(Offszetek.size());
            fajl += (char) 10;
            fajl += ">>";
            fajl += (char) 10;
            fajl += "startxref";
            fajl += (char) 10;
            fajl += Integer.toString(xroffszet);
            fajl += (char) 10;
            fajl += "%%EOF";

            str.write(fajl.getBytes(Charset.forName("UTF-8")));
            str.close();
            return 1;

        } catch (Exception exc) {

            return 0;
        }
    }

    static public int exportGIF(Origami origami, Camera refcam, int color, int width, int height, String filename) {

        try (FileOutputStream fos = new FileOutputStream(filename)) {

            fos.write('G');
            fos.write('I');
            fos.write('F');
            fos.write('8');
            fos.write('9');
            fos.write('a');

            fos.write((byte) width);
            fos.write((byte) (width >>> 8));
            fos.write((byte) height);
            fos.write((byte) (height >>> 8));
            fos.write(0b10010110);
            fos.write(0);
            fos.write(0);

            for (int r = 1; r <= 5; r++) {
                for (int g = 1; g <= 5; g++) {
                    for (int b = 1; b <= 5; b++) {

                        fos.write(r * 51);
                        fos.write(g * 51);
                        fos.write(b * 51);
                    }
                }
            }
            for (int i = 0; i < 9; i++) {
                fos.write(0);
            }

            fos.write(0x21);
            fos.write(0xFF);
            fos.write(0x0B);
            fos.write('N');
            fos.write('E');
            fos.write('T');
            fos.write('S');
            fos.write('C');
            fos.write('A');
            fos.write('P');
            fos.write('E');
            fos.write('2');
            fos.write('.');
            fos.write('0');
            fos.write(0x03);
            fos.write(0x01);
            fos.write(0x00);
            fos.write(0x00);
            fos.write(0x00);

            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D gimg = img.createGraphics();
            gimg.setBackground(java.awt.Color.WHITE);
            Origami origami1 = origami.clone();
            Camera cam = new Camera(width / 2, height / 2, 1);
            cam.camera_dir = refcam.camera_dir.clone();
            cam.axis_x = refcam.axis_x.clone();
            cam.axis_y = refcam.axis_y.clone();
            cam.setZoom(0.8 * Math.min(width, height) / origami1.circumscribedSquareSize());
            int steps = origami1.history_pointer();
            origami1.undo(steps);
            boolean last = false;
            while (origami1.history_pointer() < steps || (last = !last && origami1.history_pointer() == steps)) {

                gimg.clearRect(0, 0, width, height);
                cam.adjust(origami1);
                cam.drawFaces(gimg, color, origami1);
                cam.drawEdges(gimg, java.awt.Color.black, origami1);
                origami1.redo();
                fos.write(0x21);
                fos.write(0xF9);
                fos.write(0x04);
                fos.write(0x04);
                fos.write(0x64); //delay time
                fos.write(0x00);
                fos.write(0x00);
                fos.write(0x00);

                fos.write(0x2C);
                fos.write(0x00);
                fos.write(0x00);
                fos.write(0x00);
                fos.write(0x00);
                fos.write((byte) width);
                fos.write((byte) (width >>> 8));
                fos.write((byte) height);
                fos.write((byte) (height >>> 8));
                fos.write(0x00);

                fos.write(0x07);

                byte[] bimg = new byte[width * height];
                for (int y = 0; y < height; y++) {

                    fos.write(width / 2 + 1);
                    fos.write(0x80);
                    for (int x = 0; x < width / 2; x++) {

                        int rgb = img.getRGB(x, y) & 0xFFFFFF;
                        int b = rgb % 0x100;
                        int g = (rgb >>> 8) % 0x100;
                        int r = rgb >>> 16;

                        fos.write((((r * 5) / 256) * 25 + ((g * 5) / 256) * 5 + (b * 5) / 256));
                    }
                    fos.write(width - width / 2 + 1);
                    fos.write(0x80);
                    for (int x = width / 2; x < width; x++) {

                        int rgb = img.getRGB(x, y) & 0xFFFFFF;
                        int b = rgb % 0x100;
                        int g = (rgb >>> 8) % 0x100;
                        int r = rgb >>> 16;

                        fos.write((((r * 5) / 256) * 25 + ((g * 5) / 256) * 5 + (b * 5) / 256));
                    }
                }
                fos.write(0x01);
                fos.write(0x81);
                fos.write(0);
            }

            fos.write(0x3B);
            fos.close();
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    static public int exportRevolvingGIF(Origami origami, Camera refcam, int color, int width, int height, String filename) {

        try (FileOutputStream fos = new FileOutputStream(filename)) {

            fos.write('G');
            fos.write('I');
            fos.write('F');
            fos.write('8');
            fos.write('9');
            fos.write('a');

            fos.write((byte) width);
            fos.write((byte) (width >>> 8));
            fos.write((byte) height);
            fos.write((byte) (height >>> 8));
            fos.write(0b10010110);
            fos.write(0);
            fos.write(0);

            for (int r = 1; r <= 5; r++) {
                for (int g = 1; g <= 5; g++) {
                    for (int b = 1; b <= 5; b++) {

                        fos.write(r * 51);
                        fos.write(g * 51);
                        fos.write(b * 51);
                    }
                }
            }
            for (int i = 0; i < 9; i++) {
                fos.write(0);
            }

            fos.write(0x21);
            fos.write(0xFF);
            fos.write(0x0B);
            fos.write('N');
            fos.write('E');
            fos.write('T');
            fos.write('S');
            fos.write('C');
            fos.write('A');
            fos.write('P');
            fos.write('E');
            fos.write('2');
            fos.write('.');
            fos.write('0');
            fos.write(0x03);
            fos.write(0x01);
            fos.write(0x00);
            fos.write(0x00);
            fos.write(0x00);

            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D gimg = img.createGraphics();
            gimg.setBackground(java.awt.Color.WHITE);
            Origami origami1 = origami.clone();
            Camera cam = new Camera(width / 2, height / 2, 1);
            cam.camera_dir = refcam.camera_dir.clone();
            cam.axis_x = refcam.axis_x.clone();
            cam.axis_y = refcam.axis_y.clone();
            cam.setZoom(0.8 * Math.min(width, height) / origami1.circumscribedSquareSize());
            cam.adjust(origami1);

            for (int i = 0; i < 72; i++) {

                gimg.clearRect(0, 0, width, height);
                cam.drawFaces(gimg, color, origami1);
                cam.drawEdges(gimg, java.awt.Color.black, origami1);
                cam.rotate(10, 0);

                fos.write(0x21);
                fos.write(0xF9);
                fos.write(0x04);
                fos.write(0x04);
                fos.write(0x05); //delay time
                fos.write(0x00);
                fos.write(0x00);
                fos.write(0x00);

                fos.write(0x2C);
                fos.write(0x00);
                fos.write(0x00);
                fos.write(0x00);
                fos.write(0x00);
                fos.write((byte) width);
                fos.write((byte) (width >>> 8));
                fos.write((byte) height);
                fos.write((byte) (height >>> 8));
                fos.write(0x00);

                fos.write(0x07);

                byte[] bimg = new byte[width * height];
                for (int y = 0; y < height; y++) {

                    fos.write(width / 2 + 1);
                    fos.write(0x80);
                    for (int x = 0; x < width / 2; x++) {

                        int rgb = img.getRGB(x, y) & 0xFFFFFF;
                        int b = rgb % 0x100;
                        int g = (rgb >>> 8) % 0x100;
                        int r = rgb >>> 16;

                        fos.write((((r * 5) / 256) * 25 + ((g * 5) / 256) * 5 + (b * 5) / 256));
                    }
                    fos.write(width - width / 2 + 1);
                    fos.write(0x80);
                    for (int x = width / 2; x < width; x++) {

                        int rgb = img.getRGB(x, y) & 0xFFFFFF;
                        int b = rgb % 0x100;
                        int g = (rgb >>> 8) % 0x100;
                        int r = rgb >>> 16;

                        fos.write((((r * 5) / 256) * 25 + ((g * 5) / 256) * 5 + (b * 5) / 256));
                    }
                }
                fos.write(0x01);
                fos.write(0x81);
                fos.write(0);
            }

            fos.write(0x3B);
            fos.close();
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    static public int exportPNG(Origami origami, String filename) {

        try {

            File png = new File(filename);
            if (png.exists()) {
                png.delete();
            }
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage((int) origami.paperWidth(), (int) origami.paperHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setBackground(java.awt.Color.WHITE);
            g.clearRect(0, 0, (int) origami.paperWidth(), (int) origami.paperHeight());
            new Camera((int) origami.paperWidth() / 2, (int) origami.paperHeight() / 2, 1).drawCreasePattern(g, Color.BLACK, origami);

            if (javax.imageio.ImageIO.write(img, "png", png)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception ex) {
            return 0;
        }
    }

    static public int exportJAR(Origami origami, String filename) {

        try {

            File finalJar = new File(filename);
            if (finalJar.exists()) {
                finalJar.delete();
            }
            long ordinal = 1;
            File tempJar;
            while((tempJar = new File(finalJar.getParentFile(), ordinal+".jar")).exists()
                    || tempJar.equals(finalJar)) {
                ordinal++;
            }
            ordinal = 1;
            File tempOri;
            while((tempOri = new File(finalJar.getParentFile(), ordinal+".ori")).exists()
                    || tempOri.equals(finalJar)) {
                ordinal++;
            }
            
            java.io.InputStream is = new Export().getClass().getResourceAsStream("/res/OrigamiDisplay.jar");
            java.io.OutputStream os = new java.io.FileOutputStream(tempJar);
            
            int nextbyte;
            while ((nextbyte = is.read()) != -1) {
                os.write(nextbyte);
            }
            
            is.close();
            os.close();
            
            OrigamiIO.write_gen2(origami, tempOri.getPath());
            
            java.util.zip.ZipFile jar = new java.util.zip.ZipFile(tempJar);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(finalJar);
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);
            java.util.zip.ZipEntry next;

            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                if (!(next = entries.nextElement()).isDirectory()) {

                    zos.putNextEntry(next);
                    is = jar.getInputStream(next);
                    
                    while ((nextbyte = is.read()) != -1) {
                        zos.write(nextbyte);
                    }
                    zos.closeEntry();
                    is.close();
                }
            }
            
            next = new java.util.zip.ZipEntry("o");
            zos.putNextEntry(next);
            is = new java.io.FileInputStream(tempOri);
            while ((nextbyte = is.read()) != -1) {
                zos.write(nextbyte);
            }
            
            zos.closeEntry();
            zos.close();
            fos.close();
            
            tempOri.delete();
            tempJar.delete();
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }
}
