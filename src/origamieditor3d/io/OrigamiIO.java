package origamieditor3d.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import origamieditor3d.origami.Origami;
import origamieditor3d.origami.Origami.PaperType;
import origamieditor3d.origami.OrigamiException;
import origamieditor3d.origami.OrigamiGen1;
import origamieditor3d.origami.OrigamiGen2;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public class OrigamiIO {

    static public void write_gen2(Origami origami, String filename, int[] rgb) throws Exception {

        if (!(origami instanceof OrigamiGen2)) {
            write_gen1(origami, filename);
            return;
        }
        try {

            final int rand = new Random().nextInt(100000) + 100000;
            File ori = new File(filename + String.valueOf(rand));
            if (ori.exists()) {
                ori.delete();
            }
            FileOutputStream str = new FileOutputStream(ori);

            //OE3D
            str.write(0x4f);
            str.write(0x45);
            str.write(0x33);
            str.write(0x44);

            //version 3
            str.write(3);
            //number of payloads
            if (rgb == null) {
                str.write(0x63); //compact
            }
            else {
                str.write(1); //1 payload
            }
            
            //paper type
            str.write((int) origami.getPaperType().toChar());
            //corners
            if (origami.getPaperType() == Origami.PaperType.Custom) {

                str.write(origami.getCorners().size());
                for (int i = 0; i < origami.getCorners().size(); i++) {

                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]));

                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]));
                }
            } else {
                str.write(0);
            }
            
            //paper color
            if (rgb != null) {
                str.write(0x43); //C
                str.write(0xFF & rgb[0]);
                str.write(0xFF & rgb[1]);
                str.write(0xFF & rgb[2]);
            }
            
            //command blocks
            for (int i = 0; i < origami.getHistoryPointer(); i++) {
                for (int ii=0; ii<16; ii++) {
                    str.write(origami.getHistoryStream().get(i)[ii]);
                }
            }

            //EOF
            str.write(0x0A);
            str.write(0x45);
            str.write(0x4f);
            str.write(0x46);

            str.close();
            LZW.compress(new File(filename + String.valueOf(rand)), new File(filename));
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
            str.write((int) origami.getPaperType().toChar());
            //corners
            if (origami.getPaperType() == Origami.PaperType.Custom) {

                str.write(origami.getCorners().size());
                for (int i = 0; i < origami.getCorners().size(); i++) {

                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[0]));

                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]) >>> 24);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]) >>> 16);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]) >>> 8);
                    str.write(Float.floatToIntBits((float) origami.getCorners().get(i)[1]));
                }
            } else {
                str.write(0);
            }

            //command blocks
            for (int i = 0; i < origami.getHistoryPointer(); i++) {
                for (int ii=0; ii<16; ii++) {
                    str.write(origami.getHistoryStream().get(i)[ii]);
                }
            }

            //EOF
            str.write(0x0A);
            str.write(0x45);
            str.write(0x4f);
            str.write(0x46);

            str.close();
            LZW.compress(new File(filename + "~"), new File(filename));
            ori.delete();

        } catch (Exception ex) {

            throw OrigamiException.H002;
        }
    }

    static public Origami read_gen2(ByteArrayInputStream ori, int[] rgb) throws Exception {

        try {

            Origami origami;
            ori.reset();
            InputStream str = LZW.extract(ori);

            //reading header
            int fejlec1 = str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) { //OE3D

                str.close();
                throw OrigamiException.H005;
            } else {

                int fejlec2 = str.read(); //version
                fejlec2 <<= 8;
                fejlec2 += str.read(); //number of payloads

                if ((fejlec2 & 0xFF00) != 0x0300) { //ver 3

                    str.close();
                    return read_gen1(ori);
                } else {

                    //paper type
                    int papir = str.read();
                    if (PaperType.forChar((char) papir) != Origami.PaperType.Custom) {

                        origami = new OrigamiGen2(PaperType.forChar((char) papir));
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
                    
                    if ((fejlec2 & 0xFF) != 0x63) {
                        for (int i=0; i<(fejlec2 & 0xFF); i++) {
                            
                            int payload_id = str.read();
                            if (payload_id == 0x43) { //paper color
                                if (rgb != null) {
                                    
                                    rgb[0] = str.read();
                                    rgb[1] = str.read();
                                    rgb[2] = str.read();
                                }
                                else {
                                    
                                    str.read();
                                    str.read();
                                    str.read();
                                }
                            }
                        }
                    }

                    //command blocks
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

    static public Origami read_gen1(ByteArrayInputStream ori) throws Exception {

        try {

            Origami origami;
            ori.reset();
            InputStream str = LZW.extract(ori);

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

                    if (PaperType.forChar((char) papir) != Origami.PaperType.Custom) {

                        origami = new OrigamiGen1(PaperType.forChar((char) papir));
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

                        origami = new OrigamiGen1(sarkok);
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
