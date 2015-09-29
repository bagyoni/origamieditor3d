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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public class OrigamiIO {

    static public void write_gen2(Origami origami, String filename) throws Exception {

        if (!(origami instanceof OrigamiGen2)) {
            write_gen1(origami, filename);
            return;
        }
        try {

            File ori = new File(filename + "~");
            if (ori.exists()) {
                ori.delete();
            }
            FileOutputStream str = new FileOutputStream(ori);

            //OE3D
            str.write(0x4f);
            str.write(0x45);
            str.write(0x33);
            str.write(0x44);

            //version 3, compressed
            str.write(3);
            str.write(0x63);
            //paper type
            str.write((int) origami.papertype().toChar());
            //corners
            if (origami.papertype() == Origami.PaperType.Custom) {

                str.write(origami.corners().size());
                for (int i = 0; i < origami.corners().size(); i++) {

                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]));

                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]));
                }
            } else {
                str.write(0);
            }

            //command blocks
            for (int i = 0; i < origami.history_pointer(); i++) {
                for (int ii=0; ii<16; ii++) {
                    str.write(origami.history_stream().get(i)[ii]);
                }
            }

            //EOF
            str.write(0x0A);
            str.write(0x45);
            str.write(0x4f);
            str.write(0x46);

            str.close();
            origamieditor3d.compression.LZW.compress(new File(filename + "~"), new File(filename));
            ori.delete();

        } catch (Exception ex) {
            throw OrigamiException.H005;
        }
    }

    static public void write_gen1(Origami origami, String filename) throws Exception {

        try {

            File ori = new File(filename + "~");
            if (ori.exists()) {
                ori.delete();
            }
            FileOutputStream str = new FileOutputStream(ori);

            //OE3D
            str.write(0x4f);
            str.write(0x45);
            str.write(0x33);
            str.write(0x44);

            //version 2, compressed
            str.write(2);
            str.write(0x63);
            //paper type
            str.write((int) origami.papertype().toChar());
            //corners
            if (origami.papertype() == Origami.PaperType.Custom) {

                str.write(origami.corners().size());
                for (int i = 0; i < origami.corners().size(); i++) {

                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[0]));

                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.corners().get(i)[1]));
                }
            } else {
                str.write(0);
            }

            //command blocks
            for (int i = 0; i < origami.history_pointer(); i++) {
                for (int ii=0; ii<16; ii++) {
                    str.write(origami.history_stream().get(i)[ii]);
                }
            }

            //EOF
            str.write(0x0A);
            str.write(0x45);
            str.write(0x4f);
            str.write(0x46);

            str.close();
            origamieditor3d.compression.LZW.compress(new File(filename + "~"), new File(filename));
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
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) {

                str.close();
                throw OrigamiException.H005;
            } else {

                int fejlec2 = str.read();
                fejlec2 <<= 8;
                fejlec2 += str.read();

                if (fejlec2 != 0x0363) {

                    str.close();
                    return read_gen1(ori);
                } else {

                    int papir = str.read();

                    if (Origami.PaperType.forChar((char) papir) != Origami.PaperType.Custom) {

                        origami = new OrigamiGen2(Origami.PaperType.forChar((char) papir));
                        str.read();
                    } else {

                        ArrayList<double[]> sarkok = new ArrayList<>(Arrays.asList(new double[][]{}));
                        int sarokszam = str.read();

                        for (int i = 0; i < sarokszam; i++) {

                            int Xint = str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            float X = Float.intBitsToFloat(Xint);

                            int Yint = str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            float Y = Float.intBitsToFloat(Yint);

                            sarkok.add(new double[]{(double) X, (double) Y});
                        }

                        origami = new OrigamiGen2(sarkok);
                    }

                    int[] block = new int[16];
                    int i=-1;

                    block[++i] = str.read();
                    block[++i] = str.read();
                    block[++i] = str.read();
                    block[++i] = str.read();
                    int header = (((((block[0] << 8) + block[1]) << 8) + block[2]) << 8) + block[3];
                    while (header != 0x0A454f46) {

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();

                        origami.addCommand(block.clone());
                        i = -1;

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        header = (((((block[0] << 8) + block[1]) << 8) + block[2]) << 8) + block[3];
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
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) {

                str.close();
                throw OrigamiException.H005;
            } else {

                int fejlec2 = str.read();
                fejlec2 <<= 8;
                fejlec2 += str.read();

                if (fejlec2 != 0x0263) {

                    str.close();
                    throw OrigamiException.H005;
                } else {

                    int papir = str.read();

                    if (Origami.PaperType.forChar((char) papir) != Origami.PaperType.Custom) {

                        origami = new Origami(Origami.PaperType.forChar((char) papir));
                        str.read();
                    } else {

                        ArrayList<double[]> sarkok = new ArrayList<>(Arrays.asList(new double[][]{}));
                        int sarokszam = str.read();

                        for (int i = 0; i < sarokszam; i++) {

                            int Xint = str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            float X = Float.intBitsToFloat(Xint);

                            int Yint = str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            float Y = Float.intBitsToFloat(Yint);

                            sarkok.add(new double[]{(double) X, (double) Y});
                        }

                        origami = new Origami(sarkok);
                    }

                    int[] block = new int[16];
                    int i=-1;

                    block[++i] = str.read();
                    block[++i] = str.read();
                    block[++i] = str.read();
                    block[++i] = str.read();
                    int header = (((((block[0] << 8) + block[1]) << 8) + block[2]) << 8) + block[3];
                    while (header != 0x0A454f46) {

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();

                        origami.addCommand(block.clone());
                        i = -1;

                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        block[++i] = str.read();
                        header = (((((block[0] << 8) + block[1]) << 8) + block[2]) << 8) + block[3];
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
}
