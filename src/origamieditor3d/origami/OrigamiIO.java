// This file is part of Origami Editor 3D.
// Copyright (C) 2013, 2014, 2015 Bágyoni Attila <bagyoni.attila@gmail.com>
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Metódusokat nyújt {@linkplain Global#Main} külsô fájlként való mentésére és
 * megnyitására.
 *
 * @author Attila Bágyoni <bagyoni.attila@gmail.com>
 * @since 2013-01-14
 * @see Global
 * @see Origami
 */
public class OrigamiIO {

    final static private double[][] Origins = new double[][]{
        new double[]{0, 0, 0},
        new double[]{400, 0, 0},
        new double[]{0, 400, 0},
        new double[]{0, 0, 400}
    };

    static public void write_gen2(Origami origami, String filename) throws Exception {
    	
    	if (!(origami instanceof OrigamiGen2)) {
    		write_gen1(origami, filename);
    		return;
    	}
    	try {

            File ori = new File(filename+"~");
            if (ori.exists()) {
                ori.delete();
            }
            FileOutputStream str = new FileOutputStream(ori);

            //OE3D
            str.write(0x4f);
            str.write(0x45);
            str.write(0x33);
            str.write(0x44);

            //3. verzió, tömörített
            str.write(3);
            str.write(0x63);
            //papírméret
            str.write((int) origami.papertype().toChar());
            //sarkok
            if (origami.papertype() == Origami.PaperType.Custom) {

                str.write(origami.corners().size());
                for (int i=0; i<origami.corners().size(); i++) {

                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]) >>> 24);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]) >>> 16);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]) >>> 8);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]));

                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]) >>> 24);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]) >>> 16);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]) >>> 8);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]));
                }
            } else {
                str.write(0);
            }

            //parancsblokkok
            for (int i = 0; i < origami.history_pointer(); i++) {

                int hajtasszog = 0;
                if (origami.history.get(i)[0] == 2.0) {

                    hajtasszog += (int) origami.history.get(i)[7];
                    while (i < origami.history.size() - 1) {

                        if (origami.history.get(i + 1)[0] == 2.0
                                && origami.history.get(i + 1)[1] == origami.history.get(i)[1]
                                && origami.history.get(i + 1)[2] == origami.history.get(i)[2]
                                && origami.history.get(i + 1)[3] == origami.history.get(i)[3]
                                && origami.history.get(i + 1)[4] == origami.history.get(i)[4]
                                && origami.history.get(i + 1)[5] == origami.history.get(i)[5]
                                && origami.history.get(i + 1)[6] == origami.history.get(i)[6]) {
                            i++;
                            hajtasszog += (int) origami.history.get(i)[7];
                        } else {
                            break;
                        }
                    }
                } else if (origami.history.get(i)[0] == 4.0) {

                    hajtasszog += (int) origami.history.get(i)[7];
                    while (i < origami.history.size() - 1) {

                        if (origami.history.get(i + 1)[0] == 4.0
                                && origami.history.get(i + 1)[1] == origami.history.get(i)[1]
                                && origami.history.get(i + 1)[2] == origami.history.get(i)[2]
                                && origami.history.get(i + 1)[3] == origami.history.get(i)[3]
                                && origami.history.get(i + 1)[4] == origami.history.get(i)[4]
                                && origami.history.get(i + 1)[5] == origami.history.get(i)[5]
                                && origami.history.get(i + 1)[6] == origami.history.get(i)[6]
                                && origami.history.get(i + 1)[8] == origami.history.get(i)[8]) {
                            i++;
                            hajtasszog += (int) origami.history.get(i)[7];
                        } else {
                            break;
                        }
                    }
                }

                double[] sikpont = new double[]{origami.history.get(i)[1], origami.history.get(i)[2], origami.history.get(i)[3]};
                double[] siknv = new double[]{origami.history.get(i)[4], origami.history.get(i)[5], origami.history.get(i)[6]};

                double max_tavolsag = -1;
                int hasznalt_origo = 0;
                int hasznalt_terfel = 0;
                double[] sikpontnv = new double[]{0, 0, 0};
                double konst = sikpont[0] * siknv[0] + sikpont[1] * siknv[1] + sikpont[2] * siknv[2];

                for (int ii = 0; ii < Origins.length; ii++) {

                    double[] iranyvek = siknv;
                    double X = Origins[ii][0];
                    double Y = Origins[ii][1];
                    double Z = Origins[ii][2];
                    double U = iranyvek[0];
                    double V = iranyvek[1];
                    double W = iranyvek[2];
                    double A = siknv[0];
                    double B = siknv[1];
                    double C = siknv[2];
                    double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);

                    double[] talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
                    if (Origami.vector_length(Origami.vector(talppont, Origins[ii])) > max_tavolsag) {

                        sikpontnv = Origami.vector(talppont, Origins[ii]);
                        max_tavolsag = Origami.vector_length(sikpontnv);
                        hasznalt_origo = ii;
                    }
                }

                //belsô: 1, külsô: 0
                if (Origami.scalar_product(siknv, sikpontnv) < 0) {
                    hasznalt_terfel = 1;
                }

                int parancsazon = 0;
                int mag = 65535;

                switch ((int) origami.history.get(i)[0]) {

                    case 1:
                        parancsazon = 1;
                        break;

                    case 2:
                        while (hajtasszog < 0) {
                            hajtasszog += 360;
                        }
                        hajtasszog %= 360;
                        if (hajtasszog <= 180) {
                            parancsazon = 2;
                        } else {

                            parancsazon = 3;
                            hajtasszog = 360 - hajtasszog;
                        }
                        break;

                    case 3:
                        parancsazon = 4;
                        mag = (int) origami.history.get(i)[7];
                        break;

                    case 4:
                        while (hajtasszog < 0) {
                            hajtasszog += 360;
                        }
                        hajtasszog %= 360;
                        if (hajtasszog <= 180) {
                            parancsazon = 5;
                        } else {

                            parancsazon = 6;
                            hajtasszog = 360 - hajtasszog;
                        }
                        mag = (int) origami.history.get(i)[8];
                        break;

                    case 5:
                        parancsazon = 7;
                        break;
                        
                    case 6:
                        parancsazon = 0;
                        break;
                        
                    case 7:
                        parancsazon = 0;
                        mag = (int) origami.history.get(i)[7];
                        break;
                }

                int Xe = (int) sikpontnv[0];
                int Ye = (int) sikpontnv[1];
                int Ze = (int) sikpontnv[2];

                int Xt = (int)Math.round((Math.abs(sikpontnv[0]-Xe))*256*256);
                int Yt = (int)Math.round((Math.abs(sikpontnv[1]-Ye))*256*256);
                int Zt = (int)Math.round((Math.abs(sikpontnv[2]-Ze))*256*256);

                //fejléc
                str.write(hasznalt_terfel * 32 + hasznalt_origo * 8 + parancsazon);
                str.write(hajtasszog);
                str.write(mag >>> 8);
                str.write(mag);

                //test
                str.write(Xe >>> 8);
                str.write(Xe);
                str.write(Xt >>> 8);
                str.write(Xt);

                str.write(Ye >>> 8);
                str.write(Ye);
                str.write(Yt >>> 8);
                str.write(Yt);

                str.write(Ze >>> 8);
                str.write(Ze);
                str.write(Zt >>> 8);
                str.write(Zt);
            }

            //EOF
            str.write(0x0A);
            str.write(0x45);
            str.write(0x4f);
            str.write(0x46);

            str.close();
            origamieditor3d.compression.LZW.compress(new File(filename+"~"), new File(filename));
            ori.delete();

        } catch (Exception ex) {

            throw OrigamiException.H002;
        }
    }
    
    static public void write_gen1(Origami origami, String filename) throws Exception {

        try {

            File ori = new File(filename+"~");
            if (ori.exists()) {
                ori.delete();
            }
            FileOutputStream str = new FileOutputStream(ori);

            //OE3D
            str.write(0x4f);
            str.write(0x45);
            str.write(0x33);
            str.write(0x44);

            //2. verzió, tömörített
            str.write(2);
            str.write(0x63);
            //papírméret
            str.write((int) origami.papertype().toChar());
            //sarkok
            if (origami.papertype() == Origami.PaperType.Custom) {

                str.write(origami.corners().size());
                for (int i=0; i<origami.corners().size(); i++) {

                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]) >>> 24);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]) >>> 16);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]) >>> 8);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[0]));

                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]) >>> 24);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]) >>> 16);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]) >>> 8);
                    str.write(Float.floatToIntBits((float)origami.corners().get(i)[1]));
                }
            } else {
                str.write(0);
            }

            //parancsblokkok
            for (int i = 0; i < origami.history_pointer(); i++) {

                int hajtasszog = 0;
                if (origami.history.get(i)[0] == 2.0) {

                    hajtasszog += (int) origami.history.get(i)[7];
                    while (i < origami.history.size() - 1) {

                        if (origami.history.get(i + 1)[0] == 2.0
                                && origami.history.get(i + 1)[1] == origami.history.get(i)[1]
                                && origami.history.get(i + 1)[2] == origami.history.get(i)[2]
                                && origami.history.get(i + 1)[3] == origami.history.get(i)[3]
                                && origami.history.get(i + 1)[4] == origami.history.get(i)[4]
                                && origami.history.get(i + 1)[5] == origami.history.get(i)[5]
                                && origami.history.get(i + 1)[6] == origami.history.get(i)[6]) {
                            i++;
                            hajtasszog += (int) origami.history.get(i)[7];
                        } else {
                            break;
                        }
                    }
                } else if (origami.history.get(i)[0] == 4.0) {

                    hajtasszog += (int) origami.history.get(i)[7];
                    while (i < origami.history.size() - 1) {

                        if (origami.history.get(i + 1)[0] == 4.0
                                && origami.history.get(i + 1)[1] == origami.history.get(i)[1]
                                && origami.history.get(i + 1)[2] == origami.history.get(i)[2]
                                && origami.history.get(i + 1)[3] == origami.history.get(i)[3]
                                && origami.history.get(i + 1)[4] == origami.history.get(i)[4]
                                && origami.history.get(i + 1)[5] == origami.history.get(i)[5]
                                && origami.history.get(i + 1)[6] == origami.history.get(i)[6]
                                && origami.history.get(i + 1)[8] == origami.history.get(i)[8]) {
                            i++;
                            hajtasszog += (int) origami.history.get(i)[7];
                        } else {
                            break;
                        }
                    }
                }

                double[] sikpont = new double[]{origami.history.get(i)[1], origami.history.get(i)[2], origami.history.get(i)[3]};
                double[] siknv = new double[]{origami.history.get(i)[4], origami.history.get(i)[5], origami.history.get(i)[6]};

                double max_tavolsag = -1;
                int hasznalt_origo = 0;
                int hasznalt_terfel = 0;
                double[] sikpontnv = new double[]{0, 0, 0};
                double konst = sikpont[0] * siknv[0] + sikpont[1] * siknv[1] + sikpont[2] * siknv[2];

                for (int ii = 0; ii < Origins.length; ii++) {

                    double[] iranyvek = siknv;
                    double X = Origins[ii][0];
                    double Y = Origins[ii][1];
                    double Z = Origins[ii][2];
                    double U = iranyvek[0];
                    double V = iranyvek[1];
                    double W = iranyvek[2];
                    double A = siknv[0];
                    double B = siknv[1];
                    double C = siknv[2];
                    double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);

                    double[] talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
                    if (Origami.vector_length(Origami.vector(talppont, Origins[ii])) > max_tavolsag) {

                        sikpontnv = Origami.vector(talppont, Origins[ii]);
                        max_tavolsag = Origami.vector_length(sikpontnv);
                        hasznalt_origo = ii;
                    }
                }

                //belsô: 1, külsô: 0
                if (Origami.scalar_product(siknv, sikpontnv) < 0) {
                    hasznalt_terfel = 1;
                }

                int parancsazon = 0;
                int mag = 65535;

                switch ((int) origami.history.get(i)[0]) {

                    case 1:
                        parancsazon = 1;
                        break;

                    case 2:
                        while (hajtasszog < 0) {
                            hajtasszog += 360;
                        }
                        hajtasszog %= 360;
                        if (hajtasszog <= 180) {
                            parancsazon = 2;
                        } else {

                            parancsazon = 3;
                            hajtasszog = 360 - hajtasszog;
                        }
                        break;

                    case 3:
                        parancsazon = 4;
                        mag = (int) origami.history.get(i)[7];
                        break;

                    case 4:
                        while (hajtasszog < 0) {
                            hajtasszog += 360;
                        }
                        hajtasszog %= 360;
                        if (hajtasszog <= 180) {
                            parancsazon = 5;
                        } else {

                            parancsazon = 6;
                            hajtasszog = 360 - hajtasszog;
                        }
                        mag = (int) origami.history.get(i)[8];
                        break;

                    case 5:
                        parancsazon = 7;
                        break;
                        
                    case 6:
                        parancsazon = 0;
                        break;
                        
                    case 7:
                        parancsazon = 0;
                        mag = (int) origami.history.get(i)[7];
                        break;
                }

                int Xe = (int) sikpontnv[0];
                int Ye = (int) sikpontnv[1];
                int Ze = (int) sikpontnv[2];

                int Xt = (int)Math.round((Math.abs(sikpontnv[0]-Xe))*256*256);
                int Yt = (int)Math.round((Math.abs(sikpontnv[1]-Ye))*256*256);
                int Zt = (int)Math.round((Math.abs(sikpontnv[2]-Ze))*256*256);

                //fejléc
                str.write(hasznalt_terfel * 32 + hasznalt_origo * 8 + parancsazon);
                str.write(hajtasszog);
                str.write(mag >>> 8);
                str.write(mag);

                //test
                str.write(Xe >>> 8);
                str.write(Xe);
                str.write(Xt >>> 8);
                str.write(Xt);

                str.write(Ye >>> 8);
                str.write(Ye);
                str.write(Yt >>> 8);
                str.write(Yt);

                str.write(Ze >>> 8);
                str.write(Ze);
                str.write(Zt >>> 8);
                str.write(Zt);
            }

            //EOF
            str.write(0x0A);
            str.write(0x45);
            str.write(0x4f);
            str.write(0x46);

            str.close();
            origamieditor3d.compression.LZW.compress(new File(filename+"~"), new File(filename));
            ori.delete();

        } catch (Exception ex) {

            throw OrigamiException.H002;
        }
    }

    static public Origami read_gen2(java.io.ByteArrayInputStream ori) throws Exception {

        try {

            OrigamiGen2 origami;
            ori.reset();
            java.io.InputStream str = origamieditor3d.compression.LZW.extract(ori);

            int fejlec1 = str.read();
            fejlec1 *= 256;
            fejlec1 += str.read();
            fejlec1 *= 256;
            fejlec1 += str.read();
            fejlec1 *= 256;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) {

                str.close();
                throw OrigamiException.H005;
            } else {

                int fejlec2 = str.read();
                fejlec2 *= 256;
                fejlec2 += str.read();

                if (fejlec2 != 0x0363) {

                    str.close();
                    return read_gen1(ori);
                } else {

                    int papir = str.read();

                    if (Origami.PaperType.forChar((char)papir) != Origami.PaperType.Custom) {

                        origami = new OrigamiGen2(Origami.PaperType.forChar((char) papir));
                        str.read();
                    }
                    else {

                        ArrayList<double[]> sarkok = new ArrayList<>(Arrays.asList(new double[][]{}));
                        int sarokszam = str.read();

                        for (int i=0; i<sarokszam; i++) {

                            int Xint = str.read();
                            Xint *= 256;
                            Xint += str.read();
                            Xint *= 256;
                            Xint += str.read();
                            Xint *= 256;
                            Xint += str.read();
                            float X = Float.intBitsToFloat(Xint);

                            int Yint = str.read();
                            Yint *= 256;
                            Yint += str.read();
                            Yint *= 256;
                            Yint += str.read();
                            Yint *= 256;
                            Yint += str.read();
                            float Y = Float.intBitsToFloat(Yint);

                            sarkok.add(new double[]{(double)X, (double)Y});
                        }

                        origami = new OrigamiGen2(sarkok);
                    }

                    int parancsfejlec = str.read();
                    parancsfejlec *= 256;
                    parancsfejlec += str.read();
                    parancsfejlec *= 256;
                    parancsfejlec += str.read();
                    parancsfejlec *= 256;
                    parancsfejlec += str.read();
                    while (parancsfejlec != 0x0A454f46) {

                        short Xint, Yint, Zint;
                        int Xfrac, Yfrac, Zfrac;

                        Xint = (short)str.read();
                        Xint *= 256;
                        Xint += str.read();
                        Xfrac = str.read();
                        Xfrac *= 256;
                        Xfrac += str.read();
                        double X = Xint+Math.signum(Xint)*(double)Xfrac/256/256;

                        Yint = (short)str.read();
                        Yint *= 256;
                        Yint += str.read();
                        Yfrac = str.read();
                        Yfrac *= 256;
                        Yfrac += str.read();
                        double Y = Yint+Math.signum(Yint)*(double)Yfrac/256/256;

                        Zint = (short)str.read();
                        Zint *= 256;
                        Zint += str.read();
                        Zfrac = str.read();
                        Zfrac *= 256;
                        Zfrac += str.read();
                        double Z = Zint+Math.signum(Zint)*(double)Zfrac/256/256;

                        double[] sikpont = new double[3];
                        double[] siknv = new double[3];
                        sikpont[0] = (double) X + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][0];
                        sikpont[1] = (double) Y + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][1];
                        sikpont[2] = (double) Z + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][2];
                        siknv[0] = X;
                        siknv[1] = Y;
                        siknv[2] = Z;

                        //térfélválasztás
                        if (((parancsfejlec >>> 24) - ((parancsfejlec >>> 24) % 32)) / 32 == 1) {

                            siknv = new double[]{-siknv[0], -siknv[1], -siknv[2]};
                        }

                        double[] parancs;
                        if ((parancsfejlec >>> 24) % 8 == 1) {

                            //ref. fold
                            parancs = new double[7];
                            parancs[0] = 1;
                        } else if ((parancsfejlec >>> 24) % 8 == 2) {

                            //positive rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 3) {

                            //negative rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = -(parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 4) {

                            //partial ref. fold
                            parancs = new double[8];
                            parancs[0] = 3;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 5) {

                            //positive partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 6) {

                            //negative partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (double)-(parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 7) {

                            //crease
                            parancs = new double[7];
                            parancs[0] = 5;
                        } else if (parancsfejlec % 65536 == 65535) {

                            //cut
                            parancs = new double[7];
                            parancs[0] = 6;
                        } else {

                            //partial cut
                            parancs = new double[8];
                            parancs[0] = 7;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        }

                        parancs[1] = sikpont[0];
                        parancs[2] = sikpont[1];
                        parancs[3] = sikpont[2];
                        parancs[4] = siknv[0];
                        parancs[5] = siknv[1];
                        parancs[6] = siknv[2];

                        origami.history.add(parancs);

                        parancsfejlec = str.read();
                        parancsfejlec *= 256;
                        parancsfejlec += str.read();
                        parancsfejlec *= 256;
                        parancsfejlec += str.read();
                        parancsfejlec *= 256;
                        parancsfejlec += str.read();
                    }
                    origami.redoAll();
                    str.close();
                    return origami;
                }
            }
        } catch (Exception ex) {

            throw OrigamiException.H005;
        }
    }
    
    static public Origami read_gen1(java.io.ByteArrayInputStream ori) throws Exception {

        try {

            Origami origami;
            ori.reset();
            java.io.InputStream str = origamieditor3d.compression.LZW.extract(ori);
            
            int fejlec1 = str.read();
            fejlec1 *= 256;
            fejlec1 += str.read();
            fejlec1 *= 256;
            fejlec1 += str.read();
            fejlec1 *= 256;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) {

                str.close();
                throw OrigamiException.H005;
            } else {

                int fejlec2 = str.read();
                fejlec2 *= 256;
                fejlec2 += str.read();

                if (fejlec2 != 0x0263) {

                    str.close();
                    throw OrigamiException.H005;
                } else {

                    int papir = str.read();

                    if (Origami.PaperType.forChar((char)papir) != Origami.PaperType.Custom) {

                        origami = new Origami(Origami.PaperType.forChar((char) papir));
                        str.read();
                    }
                    else {

                        ArrayList<double[]> sarkok = new ArrayList<>(Arrays.asList(new double[][]{}));
                        int sarokszam = str.read();

                        for (int i=0; i<sarokszam; i++) {

                            int Xint = str.read();
                            Xint *= 256;
                            Xint += str.read();
                            Xint *= 256;
                            Xint += str.read();
                            Xint *= 256;
                            Xint += str.read();
                            float X = Float.intBitsToFloat(Xint);

                            int Yint = str.read();
                            Yint *= 256;
                            Yint += str.read();
                            Yint *= 256;
                            Yint += str.read();
                            Yint *= 256;
                            Yint += str.read();
                            float Y = Float.intBitsToFloat(Yint);

                            sarkok.add(new double[]{(double)X, (double)Y});
                        }

                        origami = new Origami(sarkok);
                    }

                    int parancsfejlec = str.read();
                    parancsfejlec *= 256;
                    parancsfejlec += str.read();
                    parancsfejlec *= 256;
                    parancsfejlec += str.read();
                    parancsfejlec *= 256;
                    parancsfejlec += str.read();
                    while (parancsfejlec != 0x0A454f46) {

                        short Xint, Yint, Zint;
                        int Xfrac, Yfrac, Zfrac;

                        Xint = (short)str.read();
                        Xint *= 256;
                        Xint += str.read();
                        Xfrac = str.read();
                        Xfrac *= 256;
                        Xfrac += str.read();
                        double X = Xint+Math.signum(Xint)*(double)Xfrac/256/256;

                        Yint = (short)str.read();
                        Yint *= 256;
                        Yint += str.read();
                        Yfrac = str.read();
                        Yfrac *= 256;
                        Yfrac += str.read();
                        double Y = Yint+Math.signum(Yint)*(double)Yfrac/256/256;

                        Zint = (short)str.read();
                        Zint *= 256;
                        Zint += str.read();
                        Zfrac = str.read();
                        Zfrac *= 256;
                        Zfrac += str.read();
                        double Z = Zint+Math.signum(Zint)*(double)Zfrac/256/256;

                        double[] sikpont = new double[3];
                        double[] siknv = new double[3];
                        sikpont[0] = (double) X + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][0];
                        sikpont[1] = (double) Y + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][1];
                        sikpont[2] = (double) Z + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][2];
                        siknv[0] = X;
                        siknv[1] = Y;
                        siknv[2] = Z;

                        //térfélválasztás
                        if (((parancsfejlec >>> 24) - ((parancsfejlec >>> 24) % 32)) / 32 == 1) {

                            siknv = new double[]{-siknv[0], -siknv[1], -siknv[2]};
                        }

                        double[] parancs;
                        if ((parancsfejlec >>> 24) % 8 == 1) {

                            //ref. fold
                            parancs = new double[7];
                            parancs[0] = 1;
                        } else if ((parancsfejlec >>> 24) % 8 == 2) {

                            //positive rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 3) {

                            //negative rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = -(parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 4) {

                            //partial ref. fold
                            parancs = new double[8];
                            parancs[0] = 3;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 5) {

                            //positive partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 6) {

                            //negative partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (double)-(parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 7) {

                            //crease
                            parancs = new double[7];
                            parancs[0] = 5;
                        } else if (parancsfejlec % 65536 == 65535) {

                            //cut
                            parancs = new double[7];
                            parancs[0] = 6;
                        } else {

                            //partial cut
                            parancs = new double[8];
                            parancs[0] = 7;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        }

                        parancs[1] = sikpont[0];
                        parancs[2] = sikpont[1];
                        parancs[3] = sikpont[2];
                        parancs[4] = siknv[0];
                        parancs[5] = siknv[1];
                        parancs[6] = siknv[2];

                        origami.history.add(parancs);

                        parancsfejlec = str.read();
                        parancsfejlec *= 256;
                        parancsfejlec += str.read();
                        parancsfejlec *= 256;
                        parancsfejlec += str.read();
                        parancsfejlec *= 256;
                        parancsfejlec += str.read();
                    }
                    origami.redoAll();
                    str.close();
                    return origami;
                }
            }
        } catch (Exception ex) {

            throw OrigamiException.H005;
        }
    }

    /**
     * A paraméterként megadott sík egyenletének együtthatóit olymódon kerekíti
     * {@link float}-ra, hogy a {@linkplain Fajlkezelo#Ment(String)} eljárásban
     * használt tömörítés ne legyen veszteséges, majd visszaadja az így kapott
     * sík egy pontját.
     *
     * @param sikpont	A sík egy tetszôleges pontjának térbeli derékszögû
     * koordinátáit tartalmazó tömb.
     * @param siknv	A sík normálvektorának térbeli derékszögû koordinátáit
     * tartalmazó tömb.
     * @return	A kerekítéssel kapott sík egy pontjának térbeli derékszögû
     * koordinátáit tartalmazó tömb.
     * @see Fajlkezelo#Ment(String)
     */
    static public double[] planarPointRound(double[] sikpont, double[] siknv) {
        double max_tavolsag = -1;
        int hasznalt_origo = 0;
        double[] sikpontnv = new double[]{0, 0, 0};
        double konst = sikpont[0] * siknv[0] + sikpont[1] * siknv[1] + sikpont[2] * siknv[2];
        for (int ii = 0; ii < Origins.length; ii++) {
            double[] iranyvek = siknv;
            double X = Origins[ii][0];
            double Y = Origins[ii][1];
            double Z = Origins[ii][2];
            double U = iranyvek[0];
            double V = iranyvek[1];
            double W = iranyvek[2];
            double A = siknv[0];
            double B = siknv[1];
            double C = siknv[2];
            double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);
            double[] talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
            if (Origami.vector_length(Origami.vector(talppont, Origins[ii])) > max_tavolsag) {
                sikpontnv = Origami.vector(talppont, Origins[ii]);
                max_tavolsag = Origami.vector_length(sikpontnv);
                hasznalt_origo = ii;
            }
        }
        int Xe = (int) sikpontnv[0];
        int Ye = (int) sikpontnv[1];
        int Ze = (int) sikpontnv[2];
        int Xt = (int) Math.round((Math.abs(sikpontnv[0] - Xe)) * 256 * 256);
        int Yt = (int) Math.round((Math.abs(sikpontnv[1] - Ye)) * 256 * 256);
        int Zt = (int) Math.round((Math.abs(sikpontnv[2] - Ze)) * 256 * 256);
        return new double[]{(double) Xe + Math.signum(Xe) * Xt / 256 / 256 + Origins[hasznalt_origo][0], (double) Ye + Math.signum(Ye) * Yt / 256 / 256 + Origins[hasznalt_origo][1], (double) Ze + Math.signum(Ze) * Zt / 256 / 256 + Origins[hasznalt_origo][2]};
    }

    /**
     * A paraméterként megadott sík egyenletének együtthatóit olymódon kerekíti
     * {@link float}-ra, hogy a {@linkplain Fajlkezelo#Ment(String)} eljárásban
     * használt tömörítés ne legyen veszteséges, majd visszaadja az így kapott
     * sík normálvektorát.
     *
     * @param sikpont	A sík egy tetszôleges pontjának térbeli derékszögû
     * koordinátáit tartalmazó tömb.
     * @param siknv	A sík normálvektorának térbeli derékszögû koordinátáit
     * tartalmazó tömb.
     * @return	A kerekítéssel kapott sík normálvektorának térbeli derékszögû
     * koordinátáit tartalmazó tömb.
     * @see Fajlkezelo#Ment(String)
     */
    static public double[] normalvectorRound(double[] sikpont, double[] siknv) {
        double max_tavolsag = -1;
        double[] sikpontnv = new double[]{0, 0, 0};
        double konst = sikpont[0] * siknv[0] + sikpont[1] * siknv[1] + sikpont[2] * siknv[2];
        for (double[] origo : Origins) {
            double[] iranyvek = siknv;
            double X = origo[0];
            double Y = origo[1];
            double Z = origo[2];
            double U = iranyvek[0];
            double V = iranyvek[1];
            double W = iranyvek[2];
            double A = siknv[0];
            double B = siknv[1];
            double C = siknv[2];
            double t = -(A * X + B * Y + C * Z - konst) / (A * U + B * V + C * W);
            double[] talppont = new double[]{X + t * U, Y + t * V, Z + t * W};
            if (Origami.vector_length(Origami.vector(talppont, origo)) > max_tavolsag) {
                sikpontnv = Origami.vector(talppont, origo);
                max_tavolsag = Origami.vector_length(sikpontnv);
            }
        }
        double elojel = 1;
        if (Origami.scalar_product(siknv, sikpontnv) < 0) {
            elojel = -1;
        }
        int Xe = (int) sikpontnv[0];
        int Ye = (int) sikpontnv[1];
        int Ze = (int) sikpontnv[2];
        int Xt = (int) Math.round((Math.abs(sikpontnv[0] - Xe)) * 256 * 256);
        int Yt = (int) Math.round((Math.abs(sikpontnv[1] - Ye)) * 256 * 256);
        int Zt = (int) Math.round((Math.abs(sikpontnv[2] - Ze)) * 256 * 256);
        return new double[]{elojel * ((double) Xe + Math.signum(Xe) * Xt / 256 / 256), elojel * ((double) Ye + Math.signum(Ye) * Yt / 256 / 256), elojel * ((double) Ze + Math.signum(Ze) * Zt / 256 / 256)};
    }
}
