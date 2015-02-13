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
package origamieditor3d;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import origamieditor3d.origami.Camera;
import origamieditor3d.origami.OrigamiScriptTerminal;
import origamieditor3d.origami.Origami;
import origamieditor3d.origami.OrigamiIO;
import origamieditor3d.resources.Dictionary;
import origamieditor3d.resources.BaseFolds;
import origamieditor3d.resources.Models;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiEditorUI extends javax.swing.JFrame {

    final static private long serialVersionUID = 1L;
    final static private String Version = "1.2.1";
    private Integer mouseX, mouseY;
    private int scroll_angle;
    private Integer liner1X, liner1Y, liner2X, liner2Y;
    private OrigamiScriptTerminal terminal;
    private ControlState EditorState, SecondaryState;
    private boolean alignOn;
    private int alignment_radius;
    private boolean zoomOnScroll;
    private boolean alwaysInMiddle;
    private boolean neusisOn;
    private int foldNumber;
    final private String oPanel1_tip1 = Dictionary.getString("tooltip1");
    final private String oPanel1_tip2 = Dictionary.getString("tooltip2");
    final private String oPanel1_tip3 = Dictionary.getString("tooltip3");
    final private String pPanel1_tip1 = Dictionary.getString("tooltip4");
    final private String pPanel1_tip2 = Dictionary.getString("tooltip5");
    final private String pPanel1_tip3 = Dictionary.getString("tooltip6");
    final private String pPanel1_tip4 = Dictionary.getString("tooltip7");
    final private String pPanel1_tip5 = Dictionary.getString("tooltip8");
    private String fajlnev;
    final private javax.swing.JFrame beallitasok;
    final private javax.swing.JFrame timeline;
    private javax.swing.JFileChooser mentes;
    private javax.swing.JFileChooser megnyitas;
    private javax.swing.JFileChooser ctm_export;
    private javax.swing.JFileChooser pdf_export;
    private javax.swing.JFileChooser gif_export;
    private javax.swing.JFileChooser png_export;
    private javax.swing.JFileChooser textura_megnyitas;
    private java.awt.image.BufferedImage tex;
    private boolean saved;
    final private javax.swing.JSlider timeSlider;
    private boolean changeListenerShutUp;
    final javax.swing.JPopupMenu foldingops;
    private int snap2, snap3, snap4;

    private enum ControlState {

        KESZENLET, LOCKED, VONALZO1, VONALZO2, SZOG,
        ILLESZTES0, ILLESZTES1, ILLESZTES2, ILLESZTES3, ILLESZTES_SZOG,
        AFFIN_ALTER, SZOGFELEZO
    }

    @SuppressWarnings("empty-statement")
    public OrigamiEditorUI() {

        setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("res/icon.png")));

        try {
            java.util.Scanner inf = new java.util.Scanner(
                    new java.net.URL("http://origamieditor3d.sourceforge.net/info.txt").openStream());
            String line;
            while (!(line = inf.nextLine().replace(" ", "")).startsWith("latest_version="));
            String ver = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            if (!Version.equals(ver)) {
                Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
                if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("update"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[0]) == javax.swing.JOptionPane.YES_OPTION) {
                    inf.reset();
                    while (!(line = inf.nextLine().replace(" ", "")).startsWith("download_link="));
                    Desktop.getDesktop().browse(new java.net.URI(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""))));
                    System.exit(0);
                }
            }
        } catch (Exception ex) {
        }

        initComponents();

        final BaseFolds bases = new BaseFolds();
        final java.util.ArrayList<String> basenames = bases.names();

        for (int i = 0; i < basenames.size(); i++) {
            final int ind = i;
            final javax.swing.JMenuItem baseitem = new javax.swing.JMenuItem(Dictionary.getString(basenames.get(i)));
            baseitem.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    if (!saved) {
                        Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
                        if (javax.swing.JOptionPane.showOptionDialog(OrigamiEditorUI.this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    fajlnev = null;
                    try (java.io.InputStream fis = bases.getFile(basenames.get(ind))) {

                        java.util.ArrayList<Byte> bytesb = new java.util.ArrayList<>();
                        int fisbyte;
                        while ((fisbyte = fis.read()) != -1) {
                            bytesb.add((byte) fisbyte);
                        }
                        byte[] bytes = new byte[bytesb.size()];
                        for (int i = 0; i < bytesb.size(); i++) {
                            bytes[i] = bytesb.get(i);
                        }

                        terminal.TerminalOrigami = OrigamiIO.read_gen2(new java.io.ByteArrayInputStream(bytes));
                        terminal.historyReset();

                        oPanel1.update(terminal.TerminalOrigami);
                        oPanel1.linerOff();
                        oPanel1.reset();
                        pPanel1.update(terminal.TerminalOrigami);
                        pPanel1.reset();
                        oPanel1.setToolTipText(oPanel1_tip1);
                        pPanel1.setToolTipText(pPanel1_tip1);
                        oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                        if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                        }
                        pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                        if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                            pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                        }
                        if (alwaysInMiddle) {
                            oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                        }
                        oPanel1.PanelCamera.setOrthogonalView(0);
                        EditorState = ControlState.KESZENLET;
                        scroll_angle = 0;
                        oPanel1.repaint();
                        pPanel1.repaint();
                        oPanel1.setToolTipText(oPanel1_tip1);
                        pPanel1.setToolTipText(pPanel1_tip1);
                        saved = true;
                    } catch (Exception ex) {
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("h005"));
                    }
                    foldNumber = terminal.TerminalOrigami.history_pointer();
                    changeListenerShutUp = true;
                    timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
                    changeListenerShutUp = false;
                    timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
                }
            });
            ui_file_new_bases.add(baseitem);
        }
        final Models models = new Models();
        final java.util.ArrayList<String> modnames = models.names();
        for (int i = 0; i < modnames.size(); i++) {
            final int ind = i;
            final javax.swing.JMenuItem modelitem = new javax.swing.JMenuItem(Dictionary.getString(modnames.get(i)));
            modelitem.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    if (!saved) {
                        Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
                        if (javax.swing.JOptionPane.showOptionDialog(OrigamiEditorUI.this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    fajlnev = null;
                    try (java.io.InputStream fis = models.getFile(modnames.get(ind))) {

                        java.util.ArrayList<Byte> bytesb = new java.util.ArrayList<>();
                        int fisbyte;
                        while ((fisbyte = fis.read()) != -1) {
                            bytesb.add((byte) fisbyte);
                        }
                        byte[] bytes = new byte[bytesb.size()];
                        for (int i = 0; i < bytesb.size(); i++) {
                            bytes[i] = bytesb.get(i);
                        }

                        terminal.TerminalOrigami = OrigamiIO.read_gen2(new java.io.ByteArrayInputStream(bytes));
                        terminal.historyReset();

                        oPanel1.update(terminal.TerminalOrigami);
                        oPanel1.linerOff();
                        oPanel1.reset();
                        pPanel1.update(terminal.TerminalOrigami);
                        pPanel1.reset();
                        oPanel1.setToolTipText(oPanel1_tip1);
                        pPanel1.setToolTipText(pPanel1_tip1);
                        oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                        if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                        }
                        pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                        if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                            pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                        }
                        if (alwaysInMiddle) {
                            oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                        }
                        oPanel1.PanelCamera.setOrthogonalView(0);
                        EditorState = ControlState.KESZENLET;
                        scroll_angle = 0;
                        oPanel1.repaint();
                        pPanel1.repaint();
                        oPanel1.setToolTipText(oPanel1_tip1);
                        pPanel1.setToolTipText(pPanel1_tip1);
                        saved = true;
                    } catch (Exception ex) {
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("h005"));
                    }
                    foldNumber = terminal.TerminalOrigami.history_pointer();
                    changeListenerShutUp = true;
                    timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
                    changeListenerShutUp = false;
                    timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
                }
            });
            ui_file_sample.add(modelitem);
        }

        ui_file.setText(Dictionary.getString("file"));
        ui_file_new.setText(Dictionary.getString("new"));
        ui_file_new_square.setText(Dictionary.getString("newsquare"));
        ui_file_new_a4.setText(Dictionary.getString("newa4"));
        ui_file_new_hexagonal.setText(Dictionary.getString("newhex"));
        ui_file_new_dollar.setText(Dictionary.getString("newdollar"));
        ui_file_new_bases.setText(Dictionary.getString("bases"));
        ui_file_sample.setText(Dictionary.getString("samples"));
        ui_file_open.setText(Dictionary.getString("open"));
        ui_file_save.setText(Dictionary.getString("save"));
        ui_file_saveas.setText(Dictionary.getString("saveas"));
        ui_file_export.setText(Dictionary.getString("export"));
        ui_file_export_topdf.setText(Dictionary.getString("exportpdf"));
        ui_file_export_toopenctm.setText(Dictionary.getString("exportctm"));
        ui_file_export_togif.setText(Dictionary.getString("exportgif"));
        ui_file_export_togif_revolving.setText(Dictionary.getString("revolving-gif"));
        ui_file_export_togif_folding.setText(Dictionary.getString("folding-gif"));
        ui_file_export_crease.setText(Dictionary.getString("exportpng"));
        ui_edit.setText(Dictionary.getString("edit"));
        ui_edit_undo.setText(Dictionary.getString("undo"));
        ui_edit_redo.setText(Dictionary.getString("redo"));
        ui_edit_plane.setText(Dictionary.getString("planethrough"));
        ui_edit_angle.setText(Dictionary.getString("anglebisector"));
        ui_edit_neusis.setText(Dictionary.getString("neusis"));
        ui_edit_snap.setText(Dictionary.getString("alignment"));
        ui_edit_snap_1.setText(Dictionary.getString("alignment1"));
        ui_edit_snap_2.setText(Dictionary.getString("alignment2"));
        ui_edit_snap_3.setText(Dictionary.getString("alignment3"));
        ui_edit_snap_4.setText(Dictionary.getString("alignment4"));
        ui_view.setText(Dictionary.getString("view"));
        ui_view_paper.setText(Dictionary.getString("papertex"));
        ui_view_paper_image.setText(Dictionary.getString("teximage"));
        ui_view_paper_gradient.setText(Dictionary.getString("texgradient"));
        ui_view_paper_plain.setText(Dictionary.getString("texplain"));
        ui_view_paper_none.setText(Dictionary.getString("texnone"));
        ui_view_show.setText(Dictionary.getString("showprev"));
        ui_view_zoom.setText(Dictionary.getString("zoomonscroll"));
        ui_view_best.setText(Dictionary.getString("bestfit"));
        ui_view_options.setText(Dictionary.getString("options"));
        ui_view_timeline.setText(Dictionary.getString("timeline"));
        ui_help.setText(Dictionary.getString("help"));
        ui_help_about.setText(Dictionary.getString("about"));
        ui_tutorials.setText(Dictionary.getString("tutorials"));
        ui_tutorials_internet.setText(Dictionary.getString("tutorial_videos"));
        jTabbedPane1.setTitleAt(0, Dictionary.getString("editor"));
        jTabbedPane1.setTitleAt(1, Dictionary.getString("scripting"));
        oPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(Dictionary.getString("3dview")));
        pPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(Dictionary.getString("creasepat")));

        setTitle("Origami Editor 3D");
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!saved) {
                    Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
                    if (javax.swing.JOptionPane.showOptionDialog(OrigamiEditorUI.this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                beallitasok.dispose();
                timeline.dispose();
                OrigamiEditorUI.this.dispose();
                System.exit(0);
            }
        });
        saved = true;
        mouseX = null;
        mouseY = null;
        scroll_angle = 0;
        liner1X = null;
        liner1Y = null;
        liner2X = null;
        liner2Y = null;
        EditorState = (SecondaryState = ControlState.KESZENLET);
        alignment_radius = 100;
        zoomOnScroll = true;
        alwaysInMiddle = true;
        neusisOn = false;
        terminal = new OrigamiScriptTerminal(OrigamiScriptTerminal.Access.USER);
        oPanel1.setFrontColor(Camera.paper_front_color);
        oPanel1.PanelCamera.xshift = oPanel1.getWidth() / 2;
        oPanel1.PanelCamera.yshift = oPanel1.getHeight() / 2;
        pPanel1.PanelCamera.xshift = pPanel1.getWidth() / 2;
        pPanel1.PanelCamera.yshift = pPanel1.getHeight() / 2;
        try {

            terminal.execute("version 1");
            terminal.execute("paper square new");
        } catch (Exception exc) {
        }
        oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
        pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());

        jSplitPane1.setDividerLocation(0.5);
        jSplitPane1.setResizeWeight(0.5);

        oPanel1.update(terminal.TerminalOrigami);
        oPanel1.repaint();

        pPanel1.update(terminal.TerminalOrigami);
        pPanel1.repaint();

        javax.swing.ToolTipManager.sharedInstance().setInitialDelay(0);
        javax.swing.ToolTipManager.sharedInstance().setDismissDelay(1000);
        oPanel1.setToolTipText(oPanel1_tip1);
        pPanel1.setToolTipText(pPanel1_tip1);

        oPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        pPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        this.setMinimumSize(new java.awt.Dimension(200, 200));

        fajlnev = null;

        //color chooser init
        final javax.swing.JColorChooser paletta = new javax.swing.JColorChooser(new java.awt.Color(Camera.paper_front_color));
        //options init
        beallitasok = new javax.swing.JFrame(Dictionary.getString("options"));
        beallitasok.setIconImage(getIconImage());
        java.awt.GridBagConstraints c;
        beallitasok.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        beallitasok.getContentPane().setLayout(new java.awt.GridBagLayout());
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.NORTH;
        beallitasok.getContentPane().add(new javax.swing.JLabel(Dictionary.getString("radius")), c);
        final javax.swing.JSlider igazitasCsuszka = new javax.swing.JSlider();
        igazitasCsuszka.setMinimum(5);
        igazitasCsuszka.setMaximum(20);
        igazitasCsuszka.setValue((int) Math.sqrt(alignment_radius));
        igazitasCsuszka.setLabelTable(igazitasCsuszka.createStandardLabels(15));
        igazitasCsuszka.setPaintLabels(true);
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        beallitasok.getContentPane().add(igazitasCsuszka, c);
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        javax.swing.JLabel cimke2 = new javax.swing.JLabel(Dictionary.getString("papercolor"));
        beallitasok.getContentPane().add(cimke2, c);
        paletta.setPreviewPanel(new javax.swing.JPanel());
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 2;
        beallitasok.getContentPane().add(paletta, c);
        javax.swing.JButton ok = new javax.swing.JButton();
        ok.setText("OK");
        ok.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                alignment_radius = igazitasCsuszka.getValue() * igazitasCsuszka.getValue();
                oPanel1.setFrontColor(paletta.getColor().getRGB());
                oPanel1.repaint();
                beallitasok.dispose();
            }
        });
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        beallitasok.getContentPane().add(ok, c);
        beallitasok.setMinimumSize(
                new java.awt.Dimension(
                        paletta.getMinimumSize().width,
                        paletta.getMinimumSize().height + igazitasCsuszka.getMinimumSize().height + ok.getMinimumSize().height + cimke2.getMinimumSize().height + 30));
        beallitasok.setResizable(false);
        beallitasok.setLocationRelativeTo(null);
        beallitasok.pack();

        try {
            //texture dialog init
            textura_megnyitas = new javax.swing.JFileChooser();
            textura_megnyitas.setAcceptAllFileFilterUsed(false);
            textura_megnyitas.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("img"), javax.imageio.ImageIO.getReaderFormatNames()));
            //open dialog init
            megnyitas = new javax.swing.JFileChooser();
            megnyitas.setAcceptAllFileFilterUsed(false);
            megnyitas.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("ori"), "ori"));
            megnyitas.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("txt"), "txt"));
            //save dialog init
            mentes = new javax.swing.JFileChooser();
            mentes.setAcceptAllFileFilterUsed(false);
            mentes.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("ori"), "ori"));
            //ctm dialog init
            ctm_export = new javax.swing.JFileChooser();
            ctm_export.setAcceptAllFileFilterUsed(false);
            ctm_export.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("ctm"), "ctm"));
            //pdf dialog init
            pdf_export = new javax.swing.JFileChooser();
            pdf_export.setAcceptAllFileFilterUsed(false);
            pdf_export.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("pdf"), "pdf"));
            //gif dialog init
            gif_export = new javax.swing.JFileChooser();
            gif_export.setAcceptAllFileFilterUsed(false);
            gif_export.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("gif"), "gif"));
            //png dialog init
            png_export = new javax.swing.JFileChooser();
            png_export.setAcceptAllFileFilterUsed(false);
            png_export.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Dictionary.getString("png"), "png"));
            
        } catch (Exception ex) {
            ui_file_open.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_save.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_saveas.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_view_paper_image.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_export_toopenctm.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_export_topdf.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_export_togif_revolving.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_export_togif_folding.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
            ui_file_export_crease.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("sandbox"));
                }
            });
        }
        //texture init
        tex = null;
        //timeline init
        timeline = new javax.swing.JFrame(Dictionary.getString("timeline"));
        timeline.setIconImage(getIconImage());
        timeline.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        timeSlider = new javax.swing.JSlider();
        timeSlider.setMinimum(0);
        timeSlider.setMaximum(0);
        timeSlider.setValue(0);
        foldNumber = 0;
        timeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                if (foldNumber == timeSlider.getValue() || changeListenerShutUp) {
                    return;
                }
                EditorState = ControlState.LOCKED;

                if (terminal.TerminalOrigami.history().size() < 100) {
                    if (foldNumber < timeSlider.getValue()) {
                        terminal.TerminalOrigami.redo(timeSlider.getValue() - foldNumber);
                    } else {
                        terminal.TerminalOrigami.undo(foldNumber - timeSlider.getValue());
                    }
                    foldNumber = timeSlider.getValue();
                    oPanel1.update(terminal.TerminalOrigami);
                } else { // Stop eating the CPU when it gets too complex
                    if (!timeSlider.getValueIsAdjusting()) {
                        if (foldNumber < timeSlider.getValue()) {
                            terminal.TerminalOrigami.redo(timeSlider.getValue() - foldNumber);
                        } else {
                            terminal.TerminalOrigami.undo(foldNumber - timeSlider.getValue());
                        }
                        foldNumber = timeSlider.getValue();
                        oPanel1.update(terminal.TerminalOrigami);
                    }
                }
                if (terminal.TerminalOrigami.history().size() == timeSlider.getValue()) {
                    EditorState = ControlState.KESZENLET;
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                oPanel1.linerOff();
                oPanel1.reset();
                pPanel1.reset();
                oPanel1.repaint();
                pPanel1.repaint();
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.setToolTipText(pPanel1_tip1);
            }
        });
        timeline.getContentPane().add(timeSlider);
        timeline.setSize(getWidth(), 50);
        timeline.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                timeline.setSize(timeline.getWidth(), 50);
                super.componentResized(e);
            }
        });
        timeline.addWindowStateListener(new java.awt.event.WindowStateListener() {
            @Override
            public void windowStateChanged(java.awt.event.WindowEvent e) {
                if (e.getNewState() == MAXIMIZED_BOTH) {
                    timeline.setExtendedState(NORMAL);
                }
            }
        });
        timeline.setLocation(getLocation().x, getLocation().y + getHeight());
        timeline.setVisible(true);
        changeListenerShutUp = false;
        foldingops = new javax.swing.JPopupMenu();
        final javax.swing.JMenuItem reflect = new javax.swing.JMenuItem(Dictionary.getString("reflect"));
        reflect.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldingops_reflect_actionPerformed(evt);
            }
        });
        final javax.swing.JMenuItem rotate = new javax.swing.JMenuItem(Dictionary.getString("rotate"));
        rotate.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldingops_rotate_actionPerformed(evt);
            }
        });
        final javax.swing.JMenuItem cut = new javax.swing.JMenuItem(Dictionary.getString("cut"));
        cut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldingops_cut_actionPerformed(evt);
            }
        });
        foldingops.add(reflect);
        foldingops.add(rotate);
        foldingops.add(cut);

        alignOn = ui_edit_snap_1.isSelected();
        if (ui_edit_snap_2.isSelected()) {
            snap2 = 2;
        } else {
            snap2 = 1;
        }
        if (ui_edit_snap_3.isSelected()) {
            snap3 = 3;
        } else {
            snap3 = 1;
        }
        if (ui_edit_snap_4.isSelected()) {
            snap4 = 4;
        } else {
            snap4 = 1;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        oPanel1 = new origamieditor3d.OrigamiPanel();
        pPanel1 = new origamieditor3d.PaperPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        ui_file = new javax.swing.JMenu();
        ui_file_new = new javax.swing.JMenu();
        ui_file_new_square = new javax.swing.JMenuItem();
        ui_file_new_a4 = new javax.swing.JMenuItem();
        ui_file_new_hexagonal = new javax.swing.JMenuItem();
        ui_file_new_dollar = new javax.swing.JMenuItem();
        ui_file_new_bases = new javax.swing.JMenu();
        ui_file_sample = new javax.swing.JMenu();
        ui_file_open = new javax.swing.JMenuItem();
        ui_file_save = new javax.swing.JMenuItem();
        ui_file_saveas = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        ui_file_export = new javax.swing.JMenu();
        ui_file_export_topdf = new javax.swing.JMenuItem();
        ui_file_export_toopenctm = new javax.swing.JMenuItem();
        ui_file_export_togif = new javax.swing.JMenu();
        ui_file_export_togif_revolving = new javax.swing.JMenuItem();
        ui_file_export_togif_folding = new javax.swing.JMenuItem();
        ui_file_export_crease = new javax.swing.JMenuItem();
        ui_edit = new javax.swing.JMenu();
        ui_edit_undo = new javax.swing.JMenuItem();
        ui_edit_redo = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        ui_edit_plane = new javax.swing.JMenuItem();
        ui_edit_angle = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        ui_edit_snap = new javax.swing.JMenu();
        ui_edit_snap_1 = new javax.swing.JCheckBoxMenuItem();
        ui_edit_snap_2 = new javax.swing.JCheckBoxMenuItem();
        ui_edit_snap_3 = new javax.swing.JCheckBoxMenuItem();
        ui_edit_snap_4 = new javax.swing.JCheckBoxMenuItem();
        ui_edit_neusis = new javax.swing.JCheckBoxMenuItem();
        ui_view = new javax.swing.JMenu();
        ui_view_paper = new javax.swing.JMenu();
        ui_view_paper_image = new javax.swing.JCheckBoxMenuItem();
        ui_view_paper_gradient = new javax.swing.JCheckBoxMenuItem();
        ui_view_paper_plain = new javax.swing.JCheckBoxMenuItem();
        ui_view_paper_none = new javax.swing.JCheckBoxMenuItem();
        ui_view_show = new javax.swing.JCheckBoxMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        ui_view_zoom = new javax.swing.JCheckBoxMenuItem();
        ui_view_best = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        ui_view_options = new javax.swing.JMenuItem();
        ui_view_timeline = new javax.swing.JMenuItem();
        ui_help = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        ui_help_about = new javax.swing.JMenuItem();
        ui_tutorials = new javax.swing.JMenu();
        ui_tutorials_internet = new javax.swing.JMenuItem();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setDividerSize(0);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setEnabled(false);
        jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jSplitPane1ComponentResized(evt);
            }
        });

        oPanel1.setBackground(new java.awt.Color(255, 255, 255));
        oPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("3D View"));
        oPanel1.setPreferredSize(new java.awt.Dimension(400, 400));
        oPanel1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                oPanel1MouseWheelMoved(evt);
            }
        });
        oPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                oPanel1MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                oPanel1MousePressed(evt);
            }
        });
        oPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                oPanel1ComponentResized(evt);
            }
        });
        oPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                oPanel1MouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                oPanel1MouseMoved(evt);
            }
        });
        jSplitPane1.setLeftComponent(oPanel1);

        pPanel1.setBackground(java.awt.Color.white);
        pPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Crease Pattern"));
        pPanel1.setPreferredSize(new java.awt.Dimension(400, 400));
        pPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pPanel1MouseClicked(evt);
            }
        });
        pPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                pPanel1ComponentResized(evt);
            }
        });
        jSplitPane1.setRightComponent(pPanel1);

        jTabbedPane1.addTab("Editor", jSplitPane1);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setEnabled(false);

        jTextArea3.setEditable(false);
        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jTextArea3.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jScrollPane3.setViewportView(jTextArea3);

        jSplitPane2.setTopComponent(jScrollPane3);

        jTextField1.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });
        jSplitPane2.setRightComponent(jTextField1);

        jTabbedPane1.addTab("Scripting", jSplitPane2);

        getContentPane().add(jTabbedPane1);

        ui_file.setText("File");

        ui_file_new.setIcon(javax.swing.UIManager.getIcon("FileView.fileIcon"));
        ui_file_new.setText("New");

        ui_file_new_square.setText("Square origami");
        ui_file_new_square.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_squareActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_square);

        ui_file_new_a4.setText("A4 origami");
        ui_file_new_a4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_a4ActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_a4);

        ui_file_new_hexagonal.setText("Hexagonal origami");
        ui_file_new_hexagonal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_hexagonalActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_hexagonal);

        ui_file_new_dollar.setText("Dollar bill origami");
        ui_file_new_dollar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_dollarActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_dollar);

        ui_file_new_bases.setText("Bases");
        ui_file_new.add(ui_file_new_bases);

        ui_file.add(ui_file_new);

        ui_file_sample.setText("Sample figures");
        ui_file.add(ui_file_sample);

        ui_file_open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        ui_file_open.setIcon(javax.swing.UIManager.getIcon("FileView.directoryIcon"));
        ui_file_open.setText("Open...");
        ui_file_open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_openActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_open);

        ui_file_save.setIcon(javax.swing.UIManager.getIcon("FileView.floppyDriveIcon"));
        ui_file_save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        ui_file_save.setText("Save");
        ui_file_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_saveActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_save);

        ui_file_saveas.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        ui_file_saveas.setText("Save As...");
        ui_file_saveas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_saveasActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_saveas);
        ui_file.add(jSeparator1);

        ui_file_export.setText("Export");

        ui_file_export_topdf.setText("To PDF...");
        ui_file_export_topdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_topdfActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_topdf);

        ui_file_export_toopenctm.setText("To OpenCTM 3D File...");
        ui_file_export_toopenctm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_toopenctmActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_toopenctm);

        ui_file_export_togif.setText("To GIF");

        ui_file_export_togif_revolving.setText("Revolving animation...");
        ui_file_export_togif_revolving.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_togif_revolvingActionPerformed(evt);
            }
        });
        ui_file_export_togif.add(ui_file_export_togif_revolving);

        ui_file_export_togif_folding.setText("Folding process...");
        ui_file_export_togif_folding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_togif_foldingActionPerformed(evt);
            }
        });
        ui_file_export_togif.add(ui_file_export_togif_folding);

        ui_file_export.add(ui_file_export_togif);

        ui_file_export_crease.setText("Crease pattern to PNG...");
        ui_file_export_crease.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_creaseActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_crease);

        ui_file.add(ui_file_export);

        jMenuBar1.add(ui_file);

        ui_edit.setText("Edit");

        ui_edit_undo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_undo.setText("Undo");
        ui_edit_undo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_undoActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_undo);

        ui_edit_redo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_redo.setText("Redo");
        ui_edit_redo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_redoActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_redo);
        ui_edit.add(jSeparator2);

        ui_edit_plane.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_plane.setText("Plane through 3 points");
        ui_edit_plane.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_planeActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_plane);

        ui_edit_angle.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_angle.setText("Angle bisector");
        ui_edit_angle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_angleActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_angle);
        ui_edit.add(jSeparator3);

        ui_edit_snap.setText("Snap to");

        ui_edit_snap_1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_snap_1.setSelected(true);
        ui_edit_snap_1.setText("vertices");
        ui_edit_snap_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_snap_1ActionPerformed(evt);
            }
        });
        ui_edit_snap.add(ui_edit_snap_1);

        ui_edit_snap_2.setSelected(true);
        ui_edit_snap_2.setText("midpoints");
        ui_edit_snap_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_snap_2ActionPerformed(evt);
            }
        });
        ui_edit_snap.add(ui_edit_snap_2);

        ui_edit_snap_3.setText("trisection points");
        ui_edit_snap_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_snap_3ActionPerformed(evt);
            }
        });
        ui_edit_snap.add(ui_edit_snap_3);

        ui_edit_snap_4.setText("quadrisection points");
        ui_edit_snap_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_snap_4ActionPerformed(evt);
            }
        });
        ui_edit_snap.add(ui_edit_snap_4);

        ui_edit.add(ui_edit_snap);

        ui_edit_neusis.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_neusis.setText("Neusis Mode");
        ui_edit_neusis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_neusisActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_neusis);

        jMenuBar1.add(ui_edit);

        ui_view.setText("View");

        ui_view_paper.setText("Paper texture");

        ui_view_paper_image.setText("Image");
        ui_view_paper_image.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_imageActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_image);

        ui_view_paper_gradient.setSelected(true);
        ui_view_paper_gradient.setText("Gradient");
        ui_view_paper_gradient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_gradientActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_gradient);

        ui_view_paper_plain.setText("Plain");
        ui_view_paper_plain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_plainActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_plain);

        ui_view_paper_none.setText("None");
        ui_view_paper_none.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_noneActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_none);

        ui_view.add(ui_view_paper);

        ui_view_show.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        ui_view_show.setText("Show preview");
        ui_view_show.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_showActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_show);
        ui_view.add(jSeparator5);

        ui_view_zoom.setSelected(true);
        ui_view_zoom.setText("Zoom on scroll");
        ui_view_zoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_zoomActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_zoom);

        ui_view_best.setSelected(true);
        ui_view_best.setText("Always in the middle");
        ui_view_best.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_bestActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_best);
        ui_view.add(jSeparator4);

        ui_view_options.setText("Options");
        ui_view_options.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_optionsActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_options);

        ui_view_timeline.setText("Timeline");
        ui_view_timeline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_timelineActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_timeline);

        jMenuBar1.add(ui_view);

        ui_help.setText("Help");

        jMenuItem1.setText("The OrigamiScript documentation");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        ui_help.add(jMenuItem1);

        ui_help_about.setText("About");
        ui_help_about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_help_aboutActionPerformed(evt);
            }
        });
        ui_help.add(ui_help_about);

        jMenuBar1.add(ui_help);

        ui_tutorials.setText("Tutorials");

        ui_tutorials_internet.setText("On the Internet");
        ui_tutorials_internet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_tutorials_internetActionPerformed(evt);
            }
        });
        ui_tutorials.add(ui_tutorials_internet);

        jMenuBar1.add(ui_tutorials);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void oPanel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oPanel1MousePressed

        mouseX = evt.getX();
        mouseY = evt.getY();
    }//GEN-LAST:event_oPanel1MousePressed

    //
    //  DRAG / HÚZÁS
    //
    private void oPanel1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oPanel1MouseDragged

        if (EditorState == ControlState.KESZENLET
                || EditorState == ControlState.ILLESZTES0
                || EditorState == ControlState.ILLESZTES1
                || EditorState == ControlState.ILLESZTES2
                || EditorState == ControlState.ILLESZTES3
                || EditorState == ControlState.LOCKED) {

            oPanel1.resetAlignmentPoint();
            oPanel1.PanelCamera.rotate((mouseX - evt.getX()) / (float) oPanel1.PanelCamera.zoom() / 2, (evt.getY() - mouseY) / (float) oPanel1.PanelCamera.zoom() / 2);
            oPanel1.repaint();
            mouseX = evt.getX();
            mouseY = evt.getY();
        }
    }//GEN-LAST:event_oPanel1MouseDragged

    //
    //  RESIZE / ÁTMÉRETEZÉS
    //
    private void oPanel1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_oPanel1ComponentResized

        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
        oPanel1.PanelCamera.xshift = oPanel1.getWidth() / 2;
        oPanel1.PanelCamera.yshift = oPanel1.getHeight() / 2;
    }//GEN-LAST:event_oPanel1ComponentResized

    private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jSplitPane1ComponentResized

        jSplitPane1.setDividerLocation(0.5);
    }//GEN-LAST:event_jSplitPane1ComponentResized

    //
    //  TERMINAL
    //
    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped

        if (evt.getKeyChar() == (char) 10) {
            if (!"log".equals(OrigamiScriptTerminal.obfuscate(jTextField1.getText()))) {

                try {

                    terminal.execute(jTextField1.getText());

                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);

                    oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                    }
                    if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                    }
                    pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                    if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                    }

                    jTextArea3.setText(jTextArea3.getText() + OrigamiScriptTerminal.obfuscate(jTextField1.getText()) + (char) 10);
                    jTextField1.setText(null);
                } catch (Exception exc) {

                    jTextArea3.setText(jTextArea3.getText() + exc.getMessage() + (char) 10);
                    jTextField1.setText(null);
                }
            } else {

                jTextField1.setText(null);
                jTextArea3.setText("");
                for (String sor : terminal.history()) {
                    jTextArea3.setText(jTextArea3.getText() + (char) 10 + sor);
                }
            }
            foldNumber = terminal.TerminalOrigami.history_pointer();
            changeListenerShutUp = true;
            timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
            changeListenerShutUp = false;
            timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
            saved = false;
        }
    }//GEN-LAST:event_jTextField1KeyTyped

    //
    //  RESIZE / ÁTMÉRETEZÉS
    //
    private void pPanel1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pPanel1ComponentResized

        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
        pPanel1.PanelCamera.xshift = pPanel1.getWidth() / 2;
        pPanel1.PanelCamera.yshift = pPanel1.getHeight() / 2;
    }//GEN-LAST:event_pPanel1ComponentResized

    //
    //  EDITING (2D) / SZERKESZTÉS (2D)
    //
    private void pPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pPanel1MouseClicked

        if (evt.getButton() != java.awt.event.MouseEvent.BUTTON1) {

            if (EditorState == ControlState.ILLESZTES1) {

                pPanel1.grabLinerAt(0);
                pPanel1.tiltLinerTo(null, (Integer[]) null);
                pPanel1.grabLinerAt(0);
                oPanel1.grabLinerAt(0);
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, (Integer[]) null);
                oPanel1.grabLinerAt(0);
                EditorState = ControlState.ILLESZTES0;
                pPanel1.setToolTipText(pPanel1_tip3);
                pPanel1.repaint();
                oPanel1.repaint();
            } else if (EditorState == ControlState.ILLESZTES2) {

                pPanel1.grabLinerAt(1);
                pPanel1.tiltLinerTo(null, (Integer[]) null);
                pPanel1.grabLinerAt(1);
                oPanel1.grabLinerAt(1);
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, (Integer[]) null);
                oPanel1.grabLinerAt(1);
                EditorState = ControlState.ILLESZTES1;
                pPanel1.setToolTipText(pPanel1_tip4);
                pPanel1.repaint();
                oPanel1.repaint();
            } else if (EditorState == ControlState.ILLESZTES3) {

                pPanel1.grabLinerAt(2);
                pPanel1.tiltLinerTo(null, (Integer[]) null);
                pPanel1.grabLinerAt(2);
                oPanel1.grabLinerAt(2);
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, (Integer[]) null);
                oPanel1.grabLinerAt(2);
                EditorState = ControlState.ILLESZTES2;
                pPanel1.setToolTipText(pPanel1_tip5);
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.repaint();
                oPanel1.repaint();
            } else {
                pPanel1.reset();
                oPanel1.reset();
                pPanel1.repaint();
                oPanel1.repaint();
                pPanel1.setToolTipText(pPanel1_tip1);
                oPanel1.setToolTipText(oPanel1_tip1);
                EditorState = ControlState.KESZENLET;
            }
        } else if (EditorState == ControlState.ILLESZTES0) {

            if (alignOn) {
                int[] ig = LaposIgazitas(evt.getX(), evt.getY(), alignment_radius);
                pPanel1.tiltLinerTo(null, ig[0], ig[1]);
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, ig[0], ig[1]);
            } else {
                pPanel1.tiltLinerTo(null, evt.getX(), evt.getY());
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, evt.getX(), evt.getY());
            }
            pPanel1.repaint();
            oPanel1.repaint();
            pPanel1.setToolTipText(pPanel1_tip4);
            EditorState = ControlState.ILLESZTES1;
        } else if (EditorState == ControlState.ILLESZTES1) {

            if (alignOn) {
                int[] ig = LaposIgazitas(evt.getX(), evt.getY(), alignment_radius);
                pPanel1.tiltLinerTo(null, ig[0], ig[1]);
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, ig[0], ig[1]);
            } else {
                pPanel1.tiltLinerTo(null, evt.getX(), evt.getY());
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, evt.getX(), evt.getY());
            }
            pPanel1.repaint();
            oPanel1.repaint();
            pPanel1.setToolTipText(pPanel1_tip5);
            EditorState = ControlState.ILLESZTES2;
        } else if (EditorState == ControlState.ILLESZTES2 || EditorState == ControlState.ILLESZTES3) {

            if (alignOn) {
                int[] ig = LaposIgazitas(evt.getX(), evt.getY(), alignment_radius);
                pPanel1.tiltLinerTo(null, ig[0], ig[1]);
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, ig[0], ig[1]);
            } else {
                pPanel1.tiltLinerTo(null, evt.getX(), evt.getY());
                oPanel1.tiltLinerTo(pPanel1.PanelCamera, evt.getX(), evt.getY());
            }
            pPanel1.repaint();
            oPanel1.repaint();
            EditorState = ControlState.ILLESZTES3;
        } else {

            pPanel1.setTracker(pPanel1.PanelCamera, evt.getX(), evt.getY());
            oPanel1.setTracker(pPanel1.PanelCamera, evt.getX(), evt.getY());
            pPanel1.repaint();
            oPanel1.repaint();
            pPanel1.setToolTipText(pPanel1_tip2);
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_pPanel1MouseClicked

    //
    //  EDITING (3D) / SZERKESZTÉS (3D)
    //
    private void oPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oPanel1MouseClicked

        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {

            if (EditorState == ControlState.KESZENLET
                    || EditorState == ControlState.ILLESZTES0
                    || EditorState == ControlState.ILLESZTES1
                    || EditorState == ControlState.ILLESZTES2
                    || EditorState == ControlState.ILLESZTES3) {

                if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                    oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                }
                oPanel1.PanelCamera.nextOrthogonalView();
                oPanel1.repaint();
            } else if (EditorState == ControlState.SZOG) {

                double pontX = ((double) liner2X - oPanel1.PanelCamera.xshift) / oPanel1.PanelCamera.zoom();
                double pontY = ((double) liner2Y - oPanel1.PanelCamera.yshift) / oPanel1.PanelCamera.zoom();
                double pont1X = ((double) liner1X - oPanel1.PanelCamera.xshift) / oPanel1.PanelCamera.zoom();
                double pont1Y = ((double) liner1Y - oPanel1.PanelCamera.yshift) / oPanel1.PanelCamera.zoom();

                double[] vonalzoNV = new double[]{
                    oPanel1.PanelCamera.axis_x[0] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[0] * (liner1X - liner2X),
                    oPanel1.PanelCamera.axis_x[1] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[1] * (liner1X - liner2X),
                    oPanel1.PanelCamera.axis_x[2] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[2] * (liner1X - liner2X)
                };
                double[] vonalzoPT = new double[]{
                    oPanel1.PanelCamera.axis_x[0] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[0] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[0],
                    oPanel1.PanelCamera.axis_x[1] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[1] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[1],
                    oPanel1.PanelCamera.axis_x[2] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[2] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[2]
                };
                double[] vonalzoPT1 = new double[]{
                    oPanel1.PanelCamera.axis_x[0] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[0] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[0],
                    oPanel1.PanelCamera.axis_x[1] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[1] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[1],
                    oPanel1.PanelCamera.axis_x[2] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[2] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[2]
                };
                if (neusisOn) {
                    vonalzoNV = Origami.vector(vonalzoPT, vonalzoPT1);
                }
                if (Origami.scalar_product(oPanel1.PanelCamera.camera_pos, vonalzoNV) - Origami.scalar_product(vonalzoPT, vonalzoNV) > 0) {
                    vonalzoNV = new double[]{-vonalzoNV[0], -vonalzoNV[1], -vonalzoNV[2]};
                }

                if (pPanel1.isTracked()) {

                    try {
                        double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                                + new Camera(
                                        pPanel1.PanelCamera.xshift,
                                        pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom();

                        double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                                + new Camera(
                                        pPanel1.PanelCamera.xshift,
                                        pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom();

                        terminal.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] [" + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10
                                + "target [" + magX + " " + magY + "]" + (char) 10
                                + "angle " + scroll_angle + (char) 10
                                + "rotate");
                        oPanel1.update(terminal.TerminalOrigami);
                    } catch (Exception ex) {
                        oPanel1.update(terminal.TerminalOrigami);
                        pPanel1.update(terminal.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                    }
                    pPanel1.reset();
                    oPanel1.reset();
                    pPanel1.setToolTipText(pPanel1_tip1);
                } else {

                    try {
                        terminal.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] [" + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10
                                + "angle " + scroll_angle + (char) 10
                                + "rotate");
                        oPanel1.update(terminal.TerminalOrigami);
                    } catch (Exception ex) {
                        oPanel1.update(terminal.TerminalOrigami);
                        pPanel1.update(terminal.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
                EditorState = ControlState.KESZENLET;
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                oPanel1.setToolTipText(oPanel1_tip1);
                scroll_angle = 0;
                oPanel1.linerOff();
                oPanel1.hideProtractor();
                oPanel1.repaint();
                pPanel1.repaint();
                saved = false;
            } else if (EditorState == ControlState.ILLESZTES_SZOG) {

                String pszo = "";
                if (SecondaryState == ControlState.AFFIN_ALTER) {
                    pszo = "planethrough";
                }
                if (SecondaryState == ControlState.SZOGFELEZO) {
                    pszo = "angle-bisector";
                }
                if (pPanel1.isTracked()) {

                    try {
                        double magX = ((double) pPanel1.tracker_x() - oPanel1.PanelCamera.xshift
                                + new Camera(
                                        oPanel1.PanelCamera.xshift,
                                        oPanel1.PanelCamera.yshift,
                                        oPanel1.PanelCamera.zoom()).projection0(oPanel1.PanelCamera.camera_pos)[0]) / oPanel1.PanelCamera.zoom();

                        double magY = ((double) pPanel1.tracker_y() - oPanel1.PanelCamera.yshift
                                + new Camera(
                                        oPanel1.PanelCamera.xshift,
                                        oPanel1.PanelCamera.yshift,
                                        oPanel1.PanelCamera.zoom()).projection0(oPanel1.PanelCamera.camera_pos)[1]) / oPanel1.PanelCamera.zoom();

                        terminal.execute(
                                pszo + " ["
                                + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                                + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                                + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                                + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                                + "]" + (char) 10
                                + "target [" + magX + " " + magY + "]" + (char) 10
                                + "angle " + scroll_angle + (char) 10
                                + "rotate");
                        oPanel1.update(terminal.TerminalOrigami);
                    } catch (Exception ex) {
                        oPanel1.update(terminal.TerminalOrigami);
                        pPanel1.update(terminal.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                    }
                    pPanel1.reset();
                    oPanel1.reset();
                    pPanel1.setToolTipText(pPanel1_tip1);
                } else {

                    try {
                        terminal.execute(
                                pszo + " ["
                                + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                                + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                                + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                                + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                                + "]" + (char) 10
                                + "angle " + scroll_angle + (char) 10
                                + "rotate");
                        oPanel1.update(terminal.TerminalOrigami);
                    } catch (Exception ex) {
                        oPanel1.update(terminal.TerminalOrigami);
                        pPanel1.update(terminal.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
                EditorState = ControlState.KESZENLET;
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                oPanel1.linerOff();
                pPanel1.reset();
                oPanel1.reset();
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.setToolTipText(pPanel1_tip1);
                pPanel1.repaint();
                oPanel1.repaint();
                saved = false;
            } else {
                EditorState = ControlState.KESZENLET;
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                oPanel1.linerOff();
                oPanel1.reset();
                pPanel1.reset();
                oPanel1.repaint();
                pPanel1.repaint();
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.setToolTipText(pPanel1_tip1);
            }
        } else if (EditorState == ControlState.KESZENLET) {

            liner1X = evt.getX();
            liner1Y = evt.getY();
            if (alignOn) {
                Igazit1(alignment_radius);
            }
            EditorState = ControlState.VONALZO1;
            oPanel1.setToolTipText(oPanel1_tip2);
            javax.swing.ToolTipManager.sharedInstance().mouseMoved(
                    new java.awt.event.MouseEvent(
                            oPanel1, 0, System.currentTimeMillis(), 0, evt.getX(), evt.getY(), 0, false));
        } else if (EditorState == ControlState.VONALZO1) {

            liner2X = evt.getX();
            liner2Y = evt.getY();
            if (alignOn) {
                Igazit2(alignment_radius);
            }
            EditorState = ControlState.VONALZO2;
            oPanel1.setToolTipText(null);

            foldingops.show(oPanel1, evt.getX(), evt.getY());

        } else if (EditorState == ControlState.VONALZO2 || EditorState == ControlState.ILLESZTES3) {

            foldingops.show(oPanel1, evt.getX(), evt.getY());
        } else {
            EditorState = ControlState.KESZENLET;
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
            }
            oPanel1.linerOff();
            oPanel1.reset();
            pPanel1.reset();
            oPanel1.repaint();
            pPanel1.repaint();
            oPanel1.setToolTipText(oPanel1_tip1);
            pPanel1.setToolTipText(pPanel1_tip1);
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
        if (alignOn) {
            oPanel1.resetAlignmentPoint();
        }
    }//GEN-LAST:event_oPanel1MouseClicked

    private void foldingops_reflect_actionPerformed(java.awt.event.ActionEvent evt) {

        if (EditorState == ControlState.VONALZO2) {

            double pontX = ((double) liner2X - oPanel1.PanelCamera.xshift) / oPanel1.PanelCamera.zoom();
            double pontY = ((double) liner2Y - oPanel1.PanelCamera.yshift) / oPanel1.PanelCamera.zoom();
            double pont1X = ((double) liner1X - oPanel1.PanelCamera.xshift) / oPanel1.PanelCamera.zoom();
            double pont1Y = ((double) liner1Y - oPanel1.PanelCamera.yshift) / oPanel1.PanelCamera.zoom();

            double[] vonalzoNV = new double[]{
                oPanel1.PanelCamera.axis_x[0] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[0] * (liner1X - liner2X),
                oPanel1.PanelCamera.axis_x[1] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[1] * (liner1X - liner2X),
                oPanel1.PanelCamera.axis_x[2] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[2] * (liner1X - liner2X)
            };
            double[] vonalzoPT = new double[]{
                oPanel1.PanelCamera.axis_x[0] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[0] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[0],
                oPanel1.PanelCamera.axis_x[1] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[1] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[1],
                oPanel1.PanelCamera.axis_x[2] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[2] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[2]
            };
            double[] vonalzoPT1 = new double[]{
                oPanel1.PanelCamera.axis_x[0] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[0] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[0],
                oPanel1.PanelCamera.axis_x[1] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[1] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[1],
                oPanel1.PanelCamera.axis_x[2] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[2] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[2]
            };
            if (neusisOn) {
                vonalzoNV = Origami.vector(vonalzoPT, vonalzoPT1);
            }
            if (Origami.scalar_product(oPanel1.PanelCamera.camera_pos, vonalzoNV) - Origami.scalar_product(vonalzoPT, vonalzoNV) > 0) {
                vonalzoNV = new double[]{-vonalzoNV[0], -vonalzoNV[1], -vonalzoNV[2]};
            }

            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                            + new Camera(
                                    pPanel1.PanelCamera.xshift,
                                    pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                            + new Camera(
                                    pPanel1.PanelCamera.xshift,
                                    pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom();

                    terminal.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] [" + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10
                            + "target [" + magX + " " + magY + "]" + (char) 10
                            + "reflect");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception exc) {
                    jTextArea3.setText(jTextArea3.getText() + (char) 10 + exc);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                pPanel1.reset();
                oPanel1.reset();
                pPanel1.setToolTipText(pPanel1_tip1);
            } else {

                try {
                    terminal.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] [" + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10
                            + "reflect");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            EditorState = ControlState.KESZENLET;
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
            }
            oPanel1.setToolTipText(oPanel1_tip1);
            oPanel1.linerOff();
            oPanel1.repaint();
            pPanel1.repaint();
            saved = false;
        } else if (EditorState == ControlState.ILLESZTES3) {

            String pszo = "";
            if (SecondaryState == ControlState.AFFIN_ALTER) {
                pszo = "planethrough";
            }
            if (SecondaryState == ControlState.SZOGFELEZO) {
                pszo = "angle-bisector";
            }
            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - oPanel1.PanelCamera.xshift
                            + new Camera(
                                    oPanel1.PanelCamera.xshift,
                                    oPanel1.PanelCamera.yshift,
                                    oPanel1.PanelCamera.zoom()).projection0(oPanel1.PanelCamera.camera_pos)[0]) / oPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - oPanel1.PanelCamera.yshift
                            + new Camera(
                                    oPanel1.PanelCamera.xshift,
                                    oPanel1.PanelCamera.yshift,
                                    oPanel1.PanelCamera.zoom()).projection0(oPanel1.PanelCamera.camera_pos)[1]) / oPanel1.PanelCamera.zoom();

                    terminal.execute(
                            pszo + " ["
                            + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10
                            + "target [" + magX + " " + magY + "]" + (char) 10
                            + "reflect");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                pPanel1.reset();
                oPanel1.reset();
                pPanel1.setToolTipText(pPanel1_tip1);
            } else {

                try {
                    terminal.execute(
                            pszo + " ["
                            + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10
                            + "reflect");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            EditorState = ControlState.KESZENLET;
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
            }
            oPanel1.linerOff();
            pPanel1.reset();
            oPanel1.reset();
            oPanel1.setToolTipText(oPanel1_tip1);
            pPanel1.setToolTipText(pPanel1_tip1);
            pPanel1.repaint();
            oPanel1.repaint();
            saved = false;
        }

        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
        if (alignOn) {
            oPanel1.resetAlignmentPoint();
        }
    }

    private void foldingops_rotate_actionPerformed(java.awt.event.ActionEvent evt) {

        if (EditorState == ControlState.VONALZO2) {

            EditorState = ControlState.SZOG;
            scroll_angle = 0;
            oPanel1.displayProtractor(scroll_angle);
            oPanel1.repaint();
        } else if (EditorState == ControlState.ILLESZTES3) {

            EditorState = ControlState.ILLESZTES_SZOG;
            scroll_angle = 0;
            oPanel1.displayProtractor(scroll_angle);
            oPanel1.repaint();
        }
    }

    private void foldingops_cut_actionPerformed(java.awt.event.ActionEvent evt) {

        if (EditorState == ControlState.VONALZO2) {

            double pontX = ((double) liner2X - oPanel1.PanelCamera.xshift) / oPanel1.PanelCamera.zoom();
            double pontY = ((double) liner2Y - oPanel1.PanelCamera.yshift) / oPanel1.PanelCamera.zoom();
            double pont1X = ((double) liner1X - oPanel1.PanelCamera.xshift) / oPanel1.PanelCamera.zoom();
            double pont1Y = ((double) liner1Y - oPanel1.PanelCamera.yshift) / oPanel1.PanelCamera.zoom();

            double[] vonalzoNV = new double[]{
                oPanel1.PanelCamera.axis_x[0] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[0] * (liner1X - liner2X),
                oPanel1.PanelCamera.axis_x[1] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[1] * (liner1X - liner2X),
                oPanel1.PanelCamera.axis_x[2] * (liner2Y - liner1Y) + oPanel1.PanelCamera.axis_y[2] * (liner1X - liner2X)
            };
            double[] vonalzoPT = new double[]{
                oPanel1.PanelCamera.axis_x[0] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[0] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[0],
                oPanel1.PanelCamera.axis_x[1] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[1] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[1],
                oPanel1.PanelCamera.axis_x[2] / oPanel1.PanelCamera.zoom() * pontX + oPanel1.PanelCamera.axis_y[2] / oPanel1.PanelCamera.zoom() * pontY + oPanel1.PanelCamera.camera_pos[2]
            };
            double[] vonalzoPT1 = new double[]{
                oPanel1.PanelCamera.axis_x[0] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[0] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[0],
                oPanel1.PanelCamera.axis_x[1] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[1] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[1],
                oPanel1.PanelCamera.axis_x[2] / oPanel1.PanelCamera.zoom() * pont1X + oPanel1.PanelCamera.axis_y[2] / oPanel1.PanelCamera.zoom() * pont1Y + oPanel1.PanelCamera.camera_pos[2]
            };
            if (neusisOn) {
                vonalzoNV = Origami.vector(vonalzoPT, vonalzoPT1);
            }
            if (Origami.scalar_product(oPanel1.PanelCamera.camera_pos, vonalzoNV) - Origami.scalar_product(vonalzoPT, vonalzoNV) > 0) {
                vonalzoNV = new double[]{-vonalzoNV[0], -vonalzoNV[1], -vonalzoNV[2]};
            }

            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                            + new Camera(
                                    pPanel1.PanelCamera.xshift,
                                    pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                            + new Camera(
                                    pPanel1.PanelCamera.xshift,
                                    pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom();

                    terminal.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] [" + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10
                            + "target [" + magX + " " + magY + "]" + (char) 10
                            + "cut");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception exc) {
                    jTextArea3.setText(jTextArea3.getText() + (char) 10 + exc);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                pPanel1.reset();
                oPanel1.reset();
                pPanel1.setToolTipText(pPanel1_tip1);
            } else {

                try {
                    terminal.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] [" + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10
                            + "cut");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            EditorState = ControlState.KESZENLET;
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
            }
            oPanel1.setToolTipText(oPanel1_tip1);
            oPanel1.linerOff();
            oPanel1.repaint();
            pPanel1.repaint();
            saved = false;
        } else if (EditorState == ControlState.ILLESZTES3) {

            String pszo = "";
            if (SecondaryState == ControlState.AFFIN_ALTER) {
                pszo = "planethrough";
            }
            if (SecondaryState == ControlState.SZOGFELEZO) {
                pszo = "angle-bisector";
            }
            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - oPanel1.PanelCamera.xshift
                            + new Camera(
                                    oPanel1.PanelCamera.xshift,
                                    oPanel1.PanelCamera.yshift,
                                    oPanel1.PanelCamera.zoom()).projection0(oPanel1.PanelCamera.camera_pos)[0]) / oPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - oPanel1.PanelCamera.yshift
                            + new Camera(
                                    oPanel1.PanelCamera.xshift,
                                    oPanel1.PanelCamera.yshift,
                                    oPanel1.PanelCamera.zoom()).projection0(oPanel1.PanelCamera.camera_pos)[1]) / oPanel1.PanelCamera.zoom();

                    terminal.execute(
                            pszo + " ["
                            + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10
                            + "target [" + magX + " " + magY + "]" + (char) 10
                            + "cut");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                }
                pPanel1.reset();
                oPanel1.reset();
                pPanel1.setToolTipText(pPanel1_tip1);
            } else {

                try {
                    terminal.execute(
                            pszo + " ["
                            + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[0]) / pPanel1.PanelCamera.zoom()) + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift, pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos)[1]) / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10
                            + "cut");
                    oPanel1.update(terminal.TerminalOrigami);
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            EditorState = ControlState.KESZENLET;
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
            }
            oPanel1.linerOff();
            pPanel1.reset();
            oPanel1.reset();
            oPanel1.setToolTipText(oPanel1_tip1);
            pPanel1.setToolTipText(pPanel1_tip1);
            pPanel1.repaint();
            oPanel1.repaint();
            saved = false;
        }

        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
        if (alignOn) {
            oPanel1.resetAlignmentPoint();
        }
    }

    //
    //  MOUSE MOVEMENT OVER 3D VIEW / EGÉRMOZGÁS A 3D NÉZET FELETT
    //
    private void oPanel1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oPanel1MouseMoved

        if (EditorState == ControlState.KESZENLET && alignOn) {

            liner1X = evt.getX();
            liner1Y = evt.getY();
            if (Igazit1(alignment_radius)) {
                oPanel1.setAlignmentPoint(liner1X, liner1Y);
                oPanel1.setAlignmentRadius(alignment_radius);
            } else {
                oPanel1.resetAlignmentPoint();
            }
        } else if (EditorState == ControlState.VONALZO1) {

            liner2X = evt.getX();
            liner2Y = evt.getY();
            if (alignOn) {
                if (Igazit2(alignment_radius)) {
                    oPanel1.setAlignmentPoint(liner2X, liner2Y);
                    oPanel1.setAlignmentRadius(alignment_radius);
                } else {
                    oPanel1.resetAlignmentPoint();
                }
            }
            oPanel1.linerOn(liner1X, liner1Y, liner2X, liner2Y);
        } else if (EditorState == ControlState.SZOG || EditorState == ControlState.ILLESZTES_SZOG) {

            if (evt.getX() != oPanel1.getWidth() / 2 || evt.getY() != oPanel1.getHeight() / 2) {

                double r = Math.max(Math.sqrt((evt.getX() - oPanel1.getWidth() / 2) * (evt.getX() - oPanel1.getWidth() / 2) + (evt.getY() - oPanel1.getHeight() / 2) * (evt.getY() - oPanel1.getHeight() / 2)), 1);
                scroll_angle = evt.getX() > oPanel1.getWidth() / 2
                        ? (int) (Math.acos((oPanel1.getHeight() / 2 - evt.getY()) / r) * 180. / Math.PI)
                        : -(int) (Math.acos((oPanel1.getHeight() / 2 - evt.getY()) / r) * 180. / Math.PI);
                oPanel1.displayProtractor(scroll_angle);
            }
        }
        oPanel1.repaint();
    }//GEN-LAST:event_oPanel1MouseMoved

    //
    //  PDF EXPORT
    //
    private void ui_file_export_topdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_export_topdfActionPerformed

        if (pdf_export.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            try {

                final javax.swing.JDialog exporting = new javax.swing.JDialog(this);
                exporting.setUndecorated(true);
                javax.swing.JLabel loadmsg = new javax.swing.JLabel(Dictionary.getString("exporting..."));
                loadmsg.setForeground(Color.RED);
                exporting.setLayout(new java.awt.BorderLayout());
                exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
                exporting.getContentPane().setBackground(Color.YELLOW);
                exporting.pack();
                exporting.setResizable(false);
                exporting.setLocationRelativeTo(null);
                exporting.setVisible(true);

                final Exception[] unreportedException = {null};

                new javax.swing.SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() {

                        try {

                            terminal.execute("title [" + pdf_export.getSelectedFile().getName().replace(".pdf", "")
                                    + "] filename [" + pdf_export.getSelectedFile().getPath()
                                    + (pdf_export.getSelectedFile().getPath().endsWith(".pdf") ? "] export-autopdf" : ".pdf] export-autopdf"), OrigamiScriptTerminal.Access.ROOT);
                            oPanel1.update(terminal.TerminalOrigami);
                            oPanel1.linerOff();
                            oPanel1.reset();
                            pPanel1.update(terminal.TerminalOrigami);
                            pPanel1.reset();
                            oPanel1.setToolTipText(oPanel1_tip1);
                            pPanel1.setToolTipText(pPanel1_tip1);
                            EditorState = ControlState.KESZENLET;
                            scroll_angle = 0;
                        } catch (Exception ex) {

                            unreportedException[0] = ex;
                            exporting.setVisible(false);
                            exporting.dispose();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {

                        exporting.setVisible(false);
                        exporting.dispose();
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("finished"), "Message", javax.swing.JOptionPane.PLAIN_MESSAGE);
                    }
                }.execute();

                if (unreportedException[0] != null) {
                    throw unreportedException[0];
                }
            } catch (Exception ex) {
                oPanel1.update(terminal.TerminalOrigami);
                pPanel1.update(terminal.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ui_file_export_topdfActionPerformed

    //
    //  UNDO / VISSZAVONÁS
    //
    private void ui_edit_undoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_undoActionPerformed

        if (EditorState == ControlState.LOCKED) {
            return;
        }
        try {
            terminal.execute("undo");
        } catch (Exception exc) {
        }

        oPanel1.update(terminal.TerminalOrigami);
        oPanel1.linerOff();
        oPanel1.reset();
        pPanel1.update(terminal.TerminalOrigami);
        pPanel1.reset();
        oPanel1.setToolTipText(oPanel1_tip1);
        pPanel1.setToolTipText(pPanel1_tip1);

        oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
        if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
        }
        pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
        if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
            pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
        }
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
        }
        oPanel1.repaint();
        pPanel1.repaint();
        EditorState = ControlState.KESZENLET;
        scroll_angle = 0;
        saved = false;
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_edit_undoActionPerformed

    //
    //  SCROLL OVER 3D VIEW / GÖRGETÉS A 3D NÉZET FELETT
    //
    private void oPanel1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_oPanel1MouseWheelMoved

        if (EditorState != ControlState.VONALZO1
                && EditorState != ControlState.VONALZO2
                && EditorState != ControlState.SZOG
                && EditorState != ControlState.ILLESZTES_SZOG) {

            if (zoomOnScroll
                    && oPanel1.PanelCamera.zoom() - 0.1 * evt.getWheelRotation() <= Camera.maximal_zoom
                    && oPanel1.PanelCamera.zoom() - 0.1 * evt.getWheelRotation() >= 0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize()) {
                oPanel1.PanelCamera.setZoom(oPanel1.PanelCamera.zoom() - 0.1 * evt.getWheelRotation());
            }
            oPanel1.repaint();
        }

    }//GEN-LAST:event_oPanel1MouseWheelMoved

    //
    //  SAVE AS / MENTÉS MÁSKÉNT
    //
    private void ui_file_saveasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_saveasActionPerformed

        if (mentes.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            if (new java.io.File(mentes.getSelectedFile().getPath().endsWith(".ori")
                    ? mentes.getSelectedFile().getPath()
                    : mentes.getSelectedFile().getPath() + ".ori").exists()) {
                if (javax.swing.JOptionPane.showConfirmDialog(null, Dictionary.getString("overwrite"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.NO_OPTION) {
                    ui_file_saveasActionPerformed(evt);
                    return;
                }
            }

            if (mentes.getFileFilter() == mentes.getChoosableFileFilters()[0]) {

                try {
                    terminal.execute("filename [" + mentes.getSelectedFile().getPath()
                            + (mentes.getSelectedFile().getPath().endsWith(".ori") ? "] export-ori" : ".ori] export-ori"), OrigamiScriptTerminal.Access.ROOT);
                    oPanel1.update(terminal.TerminalOrigami);
                    oPanel1.linerOff();
                    oPanel1.reset();
                    pPanel1.update(terminal.TerminalOrigami);
                    pPanel1.reset();
                    oPanel1.setToolTipText(oPanel1_tip1);
                    pPanel1.setToolTipText(pPanel1_tip1);
                    EditorState = ControlState.KESZENLET;
                    scroll_angle = 0;
                    fajlnev = mentes.getSelectedFile().getPath();
                    setTitle(mentes.getSelectedFile().getName() + " - Origami Editor 3D");
                    saved = true;
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_ui_file_saveasActionPerformed

    //
    //  CTM EXPORT
    //
    private void ui_file_export_toopenctmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_export_toopenctmActionPerformed

        if (ctm_export.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            try {
                terminal.execute("filename [" + ctm_export.getSelectedFile().getPath()
                        + (ctm_export.getSelectedFile().getPath().endsWith(".ctm") ? "] export-ctm" : ".ctm] export-ctm"), OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal.TerminalOrigami);
                oPanel1.linerOff();
                oPanel1.reset();
                pPanel1.update(terminal.TerminalOrigami);
                pPanel1.reset();
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.setToolTipText(pPanel1_tip1);
                EditorState = ControlState.KESZENLET;
                scroll_angle = 0;
            } catch (Exception ex) {
                oPanel1.update(terminal.TerminalOrigami);
                pPanel1.update(terminal.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ui_file_export_toopenctmActionPerformed

    //
    //  OPEN / MEGNYITÁS
    //
    private void ui_file_openActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_openActionPerformed

        if (!saved) {
            Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (megnyitas.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            if (megnyitas.getFileFilter() == megnyitas.getChoosableFileFilters()[0]) {

                try {
                    terminal.execute("filename [" + megnyitas.getSelectedFile().getPath() + "] open", OrigamiScriptTerminal.Access.ROOT);
                    oPanel1.update(terminal.TerminalOrigami);
                    oPanel1.linerOff();
                    oPanel1.reset();
                    pPanel1.update(terminal.TerminalOrigami);
                    pPanel1.reset();
                    oPanel1.setToolTipText(oPanel1_tip1);
                    pPanel1.setToolTipText(pPanel1_tip1);
                    oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                    if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                    }
                    pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                    if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                    }
                    oPanel1.PanelCamera.setOrthogonalView(0);
                    EditorState = ControlState.KESZENLET;
                    scroll_angle = 0;
                    oPanel1.repaint();
                    pPanel1.repaint();
                    oPanel1.setToolTipText(oPanel1_tip1);
                    pPanel1.setToolTipText(pPanel1_tip1);
                    fajlnev = megnyitas.getSelectedFile().getPath();
                    setTitle(megnyitas.getSelectedFile().getName() + " - Origami Editor 3D");
                    saved = true;
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else if (megnyitas.getFileFilter() == megnyitas.getChoosableFileFilters()[1]) {

                try {
                    terminal.execute("filename [" + megnyitas.getSelectedFile().getPath() + "] load", OrigamiScriptTerminal.Access.ROOT);
                    oPanel1.update(terminal.TerminalOrigami);
                    oPanel1.linerOff();
                    oPanel1.reset();
                    pPanel1.update(terminal.TerminalOrigami);
                    pPanel1.reset();
                    oPanel1.setToolTipText(oPanel1_tip1);
                    pPanel1.setToolTipText(pPanel1_tip1);
                    oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                    if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                    }
                    pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
                    if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
                    }
                    oPanel1.PanelCamera.setOrthogonalView(0);
                    EditorState = ControlState.KESZENLET;
                    scroll_angle = 0;
                    oPanel1.repaint();
                    pPanel1.repaint();
                    oPanel1.setToolTipText(oPanel1_tip1);
                    pPanel1.setToolTipText(pPanel1_tip1);
                    setTitle(megnyitas.getSelectedFile().getName() + " - Origami Editor 3D");
                    saved = true;
                } catch (Exception ex) {
                    oPanel1.update(terminal.TerminalOrigami);
                    pPanel1.update(terminal.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_file_openActionPerformed

    //
    //  PLANE THROUGH 3 POINTS / 3 PONTOS ILLESZTÉS
    //
    private void ui_edit_planeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_planeActionPerformed

        if (EditorState == ControlState.KESZENLET || EditorState == ControlState.ILLESZTES0) {

            EditorState = ControlState.ILLESZTES0;
            SecondaryState = ControlState.AFFIN_ALTER;
            pPanel1.setToolTipText(pPanel1_tip3);
            oPanel1.setToolTipText(oPanel1_tip3);
        }
    }//GEN-LAST:event_ui_edit_planeActionPerformed

    //
    //  REDO / MÉGIS
    //
    private void ui_edit_redoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_redoActionPerformed

        if (EditorState == ControlState.LOCKED) {
            return;
        }
        try {

            if (!terminal.history().isEmpty()) {
                terminal.execute("redo");
            }
        } catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
        }
        oPanel1.update(terminal.TerminalOrigami);
        oPanel1.linerOff();
        oPanel1.reset();
        pPanel1.update(terminal.TerminalOrigami);
        pPanel1.reset();
        oPanel1.setToolTipText(oPanel1_tip1);
        pPanel1.setToolTipText(pPanel1_tip1);
        EditorState = ControlState.KESZENLET;
        scroll_angle = 0;
        oPanel1.repaint();
        pPanel1.repaint();
        saved = false;
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_edit_redoActionPerformed

    //
    //  SNAP TO VERTICES / IGAZÍTÁS CSÚCSOKHOZ
    //
    private void ui_edit_snap_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_snap_1ActionPerformed

        if (ui_edit_snap_1.isSelected()) {

            alignOn = true;
            ui_edit_snap_2.setEnabled(true);
            ui_edit_snap_3.setEnabled(true);
            ui_edit_snap_4.setEnabled(true);
        } else {

            alignOn = false;
            ui_edit_snap_2.setEnabled(false);
            ui_edit_snap_3.setEnabled(false);
            ui_edit_snap_4.setEnabled(false);
        }
    }//GEN-LAST:event_ui_edit_snap_1ActionPerformed

    //
    //  NEW HEXAGONAL PAPER / ÚJ PAPÍR (HATSZÖG)
    //
    private void ui_file_new_hexagonalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_new_hexagonalActionPerformed

        if (!saved) {
            Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {

            fajlnev = null;
            terminal.execute("paper hexagon new");

            oPanel1.update(terminal.TerminalOrigami);
            pPanel1.update(terminal.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            oPanel1.repaint();
            pPanel1.repaint();
            setTitle("Origami Editor 3D");
            saved = true;
        } catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_file_new_hexagonalActionPerformed

    //
    //  NEW SQUARE PAPER / ÚJ PAPÍR (NÉGYZET)
    //
    private void ui_file_new_squareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_new_squareActionPerformed

        if (!saved) {
            Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {

            fajlnev = null;
            terminal.execute("paper square new");

            oPanel1.update(terminal.TerminalOrigami);
            pPanel1.update(terminal.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            oPanel1.repaint();
            pPanel1.repaint();
            setTitle("Origami Editor 3D");
            saved = true;
        } catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_file_new_squareActionPerformed

    //
    //  NEW A4 PAPER / ÚJ PAPÍR (A4)
    //
    private void ui_file_new_a4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_new_a4ActionPerformed

        if (!saved) {
            Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {

            fajlnev = null;
            terminal.execute("paper a4 new");

            oPanel1.update(terminal.TerminalOrigami);
            pPanel1.update(terminal.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            oPanel1.repaint();
            pPanel1.repaint();
            setTitle("Origami Editor 3D");
            saved = true;
        } catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_file_new_a4ActionPerformed

    //
    //  NEW USD PAPER / ÚJ PAPÍR (EGYDOLLÁROS)
    //
    private void ui_file_new_dollarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_new_dollarActionPerformed

        if (!saved) {
            Object[] options = {Dictionary.getString("yes"), Dictionary.getString("no")};
            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("nosave"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {

            fajlnev = null;
            terminal.execute("paper usd new");

            oPanel1.update(terminal.TerminalOrigami);
            pPanel1.update(terminal.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            if (terminal.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight()) / terminal.TerminalOrigami.circumscribedSquareSize());
            }
            oPanel1.repaint();
            pPanel1.repaint();
            setTitle("Origami Editor 3D");
            saved = true;
        } catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal.TerminalOrigami.history_pointer());
    }//GEN-LAST:event_ui_file_new_dollarActionPerformed

    //
    //  OPTIONS MENU / BEÁLLÍTÁSOK MENÜ
    //
    private void ui_view_optionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_optionsActionPerformed
        beallitasok.setVisible(true);
    }//GEN-LAST:event_ui_view_optionsActionPerformed

    //
    //  ANGLE BISECTOR / SZÖGFELEZŐ
    //
    private void ui_edit_angleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_angleActionPerformed

        if (EditorState == ControlState.KESZENLET || EditorState == ControlState.ILLESZTES0) {

            EditorState = ControlState.ILLESZTES0;
            SecondaryState = ControlState.SZOGFELEZO;
            pPanel1.setToolTipText(pPanel1_tip3);
            oPanel1.setToolTipText(oPanel1_tip3);
        }
    }//GEN-LAST:event_ui_edit_angleActionPerformed

    //
    //  PLAIN PAPER / SIMA PAPÍR
    //
    private void ui_view_paper_plainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_paper_plainActionPerformed

        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.SIMA);
        ui_view_paper_plain.setSelected(true);
        ui_view_paper_image.setSelected(false);
        ui_view_paper_none.setSelected(false);
        ui_view_paper_gradient.setSelected(false);
        oPanel1.repaint();
    }//GEN-LAST:event_ui_view_paper_plainActionPerformed

    //
    //  ZOOM ON SCROLL / GÖRGETÉSRE NAGYÍT
    //
    private void ui_view_zoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_zoomActionPerformed

        zoomOnScroll = ui_view_zoom.isSelected();
    }//GEN-LAST:event_ui_view_zoomActionPerformed

    //
    //  ABOUT / NÉVJEGY
    //
    private void ui_help_aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_help_aboutActionPerformed

        final javax.swing.JEditorPane html = new javax.swing.JEditorPane("text/html", "<html><body>"
                + "Origami Editor 3D Version " + Version + " <br>"
                + "Copyright © 2014 Bágyoni-Szabó Attila (ba-sz-at@users.sourceforge.net) <br>"
                + "<br>"
                + "Origami Editor 3D is licensed under the GNU General Public License version 3. <br>"
                + "<a href=\"/res/LICENSE.txt\">Click here for more information.</a>"
                + "</body></html>");

        html.setEditable(false);
        html.setHighlighter(null);
        html.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                if (evt.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
                    java.util.Scanner inf = new java.util.Scanner(OrigamiEditorUI.this.getClass().getResourceAsStream(evt.getDescription()), "UTF-8");
                    String text = "";
                    while (inf.hasNextLine()) {
                        text += inf.nextLine() + (char) 10;
                    }
                    jTextArea3.setText(text);
                    jTextArea3.setCaretPosition(0);
                    jTabbedPane1.setSelectedIndex(1);
                    javax.swing.SwingUtilities.getWindowAncestor(html).dispose();
                }
            }
        });
        javax.swing.JOptionPane.showMessageDialog(this, html);
    }//GEN-LAST:event_ui_help_aboutActionPerformed

    ///
    /// SAVE / MENTÉS
    ///
    private void ui_file_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_saveActionPerformed

        if (fajlnev == null) {
            if (mentes.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

                if (new java.io.File(mentes.getSelectedFile().getPath().endsWith(".ori")
                        ? mentes.getSelectedFile().getPath()
                        : mentes.getSelectedFile().getPath() + ".ori").exists()) {
                    if (javax.swing.JOptionPane.showConfirmDialog(null, Dictionary.getString("overwrite"), Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.NO_OPTION) {
                        ui_file_saveActionPerformed(evt);
                        return;
                    }
                }

                if (mentes.getFileFilter() == mentes.getChoosableFileFilters()[0]) {

                    try {
                        terminal.execute("filename [" + mentes.getSelectedFile().getPath()
                                + (mentes.getSelectedFile().getPath().endsWith(".ori") ? "] export-ori" : ".ori] export-ori"), OrigamiScriptTerminal.Access.ROOT);
                        oPanel1.update(terminal.TerminalOrigami);
                        oPanel1.linerOff();
                        oPanel1.reset();
                        pPanel1.update(terminal.TerminalOrigami);
                        pPanel1.reset();
                        oPanel1.setToolTipText(oPanel1_tip1);
                        pPanel1.setToolTipText(pPanel1_tip1);
                        EditorState = ControlState.KESZENLET;
                        scroll_angle = 0;
                        fajlnev = mentes.getSelectedFile().getPath();
                        setTitle(mentes.getSelectedFile().getName() + " - Origami Editor 3D");
                        saved = true;
                    } catch (Exception ex) {
                        oPanel1.update(terminal.TerminalOrigami);
                        pPanel1.update(terminal.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            try {
                terminal.execute("filename [" + fajlnev + (fajlnev.endsWith(".ori") ? "] export-ori" : ".ori] export-ori"), OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal.TerminalOrigami);
                oPanel1.linerOff();
                oPanel1.reset();
                pPanel1.update(terminal.TerminalOrigami);
                pPanel1.reset();
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.setToolTipText(pPanel1_tip1);
                EditorState = ControlState.KESZENLET;
                scroll_angle = 0;
                saved = true;
            } catch (Exception ex) {
                oPanel1.update(terminal.TerminalOrigami);
                pPanel1.update(terminal.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ui_file_saveActionPerformed

    //
    // ALWAYS IN THE MIDDLE / MINDIG KÖZÉPEN
    //
    private void ui_view_bestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_bestActionPerformed

        alwaysInMiddle = !alwaysInMiddle;
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal.TerminalOrigami);
            oPanel1.linerOff();
            oPanel1.repaint();
        } else {
            oPanel1.PanelCamera.unadjust(terminal.TerminalOrigami);
            oPanel1.linerOff();
            oPanel1.repaint();
        }
    }//GEN-LAST:event_ui_view_bestActionPerformed

    //
    // NEUSIS MODE / NEUSZISZ MÓD
    //
    private void ui_edit_neusisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_neusisActionPerformed

        if (ui_edit_neusis.isSelected()) {

            oPanel1.neusisOn();
            neusisOn = true;
            ui_view_show.setSelected(true);
            oPanel1.previewOn();
        } else {

            oPanel1.neusisOff();
            neusisOn = false;
            ui_view_show.setSelected(false);
            oPanel1.previewOff();
        }
        oPanel1.repaint();
    }//GEN-LAST:event_ui_edit_neusisActionPerformed

    //
    // PREVIEW / ELŐNÉZET
    //
    private void ui_view_showActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_showActionPerformed

        if (ui_view_show.isSelected()) {
            oPanel1.previewOn();
        } else {
            oPanel1.previewOff();
        }
    }//GEN-LAST:event_ui_view_showActionPerformed

    //
    // UV PAPER / UV PAPÍR
    //
    private void ui_view_paper_imageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_paper_imageActionPerformed

        ui_view_paper_image.setSelected(false);
        if (tex == null) {

            if (javax.swing.JOptionPane.showOptionDialog(
                    this,
                    Dictionary.getString("loadtex"),
                    Dictionary.getString("notex"),
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null) == 0) {

                if (textura_megnyitas.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

                    try {
                        terminal.execute("filename [" + textura_megnyitas.getSelectedFile().getPath() + "] load-texture");
                        tex = terminal.paper_texture();
                        if (tex.getHeight() < (int) terminal.TerminalOrigami.paperHeight() || tex.getWidth() < (int) terminal.TerminalOrigami.paperWidth()) {

                            String uzenet = Dictionary.getString("smalltex") + (char) 10
                                    + java.text.MessageFormat.format(Dictionary.getString("idealtex"), terminal.TerminalOrigami.paperWidth(), terminal.TerminalOrigami.paperHeight());
                            javax.swing.JOptionPane.showMessageDialog(this, uzenet, "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
                        } else if (tex.getHeight() > (int) terminal.TerminalOrigami.paperHeight() || tex.getWidth() > (int) terminal.TerminalOrigami.paperWidth()) {

                            String uzenet = Dictionary.getString("largetex") + (char) 10
                                    + java.text.MessageFormat.format(Dictionary.getString("idealtex"), terminal.TerminalOrigami.paperWidth(), terminal.TerminalOrigami.paperHeight());
                            javax.swing.JOptionPane.showMessageDialog(this, uzenet, "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
                        }
                        oPanel1.setTexture(tex);
                        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.UV);
                        ui_view_paper_image.setSelected(true);
                        ui_view_paper_plain.setSelected(false);
                        ui_view_paper_none.setSelected(false);
                        ui_view_paper_gradient.setSelected(false);
                        oPanel1.update(terminal.TerminalOrigami);
                        oPanel1.repaint();

                    } catch (Exception ex) {
                        tex = null;
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else if (oPanel1.displaymode() == OrigamiPanel.DisplayMode.UV) {

            if (javax.swing.JOptionPane.showOptionDialog(
                    this,
                    Dictionary.getString("loadtex"),
                    Dictionary.getString("havetex"),
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null) == 0) {

                if (textura_megnyitas.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

                    try {
                        terminal.execute("filename [" + textura_megnyitas.getSelectedFile().getPath() + "] load-texture");
                        tex = terminal.paper_texture();
                        if (tex.getHeight() < (int) terminal.TerminalOrigami.paperHeight() || tex.getWidth() < (int) terminal.TerminalOrigami.paperWidth()) {

                            String uzenet = Dictionary.getString("smalltex") + (char) 10
                                    + java.text.MessageFormat.format(Dictionary.getString("idealtex"), terminal.TerminalOrigami.paperWidth(), terminal.TerminalOrigami.paperHeight());
                            javax.swing.JOptionPane.showMessageDialog(this, uzenet, "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
                        } else if (tex.getHeight() > (int) terminal.TerminalOrigami.paperHeight() || tex.getWidth() > (int) terminal.TerminalOrigami.paperWidth()) {

                            String uzenet = Dictionary.getString("largetex") + (char) 10
                                    + java.text.MessageFormat.format(Dictionary.getString("idealtex"), terminal.TerminalOrigami.paperWidth(), terminal.TerminalOrigami.paperHeight());
                            javax.swing.JOptionPane.showMessageDialog(this, uzenet, "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
                        }
                        oPanel1.setTexture(tex);
                        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.UV);
                        ui_view_paper_image.setSelected(true);
                        ui_view_paper_plain.setSelected(false);
                        ui_view_paper_none.setSelected(false);
                        ui_view_paper_gradient.setSelected(false);
                        oPanel1.update(terminal.TerminalOrigami);
                        oPanel1.repaint();
                    } catch (Exception ex) {
                        tex = null;
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {

            oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.UV);
            ui_view_paper_image.setSelected(true);
            ui_view_paper_plain.setSelected(false);
            ui_view_paper_none.setSelected(false);
            ui_view_paper_gradient.setSelected(false);
            oPanel1.update(terminal.TerminalOrigami);
            oPanel1.repaint();
        }
    }//GEN-LAST:event_ui_view_paper_imageActionPerformed

    //
    //  EMPTY PAPER / ÜRES PAPÍR
    //
    private void ui_view_paper_noneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_paper_noneActionPerformed

        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.SEMMI);
        ui_view_paper_none.setSelected(true);
        ui_view_paper_image.setSelected(false);
        ui_view_paper_plain.setSelected(false);
        ui_view_paper_gradient.setSelected(false);
        oPanel1.repaint();
    }//GEN-LAST:event_ui_view_paper_noneActionPerformed

    //
    //  TIMELINE / IDŐVONAL
    //
    private void ui_view_timelineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_timelineActionPerformed
        timeline.setVisible(true);
    }//GEN-LAST:event_ui_view_timelineActionPerformed

    //
    //  TUTORIALS / TUTORIALOK
    //
    @SuppressWarnings("empty-statement")
    private void ui_tutorials_internetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_tutorials_internetActionPerformed

        try {
            java.util.Scanner inf = new java.util.Scanner(
                    new java.net.URL("http://origamieditor3d.sourceforge.net/info.txt").openStream());
            String line;
            while (!(line = inf.nextLine()).startsWith("tutorials_link"));
            String url = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception ex) {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://www.youtube.com/watch?v=tHWfKmynoEc&list=PL6Ycz5VMP2kJk71uuJB9r2VqLZhUnnobX"));
            } catch (Exception ex2) {
                javax.swing.JOptionPane.showMessageDialog(this, Dictionary.getString("tutorials-fail"));
            }
        }
    }//GEN-LAST:event_ui_tutorials_internetActionPerformed

    //
    //  DOCUMENTATION / DOKUMENTÁCIÓ
    //
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

        try {

            java.io.InputStream is = getClass().getResourceAsStream("/res/osdoc_en.html");
            java.io.File tmp = new java.io.File("osdoc_en.html");
            long ind = 0;
            while (tmp.exists()) {
                tmp = new java.io.File("osdoc_en" + ind + ".html");
                ++ind;
            }
            tmp.deleteOnExit();
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
            int b;
            while ((b = is.read()) != -1) {
                fos.write(b);
            }
            fos.close();
            Desktop.getDesktop().browse(tmp.toURI());
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    //
    //  GRADIENT PAPER / ÁRNYALT PAPÍR
    //
    private void ui_view_paper_gradientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_view_paper_gradientActionPerformed

        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.GRADIENT);
        ui_view_paper_gradient.setSelected(true);
        ui_view_paper_none.setSelected(false);
        ui_view_paper_image.setSelected(false);
        ui_view_paper_plain.setSelected(false);

        oPanel1.repaint();
    }//GEN-LAST:event_ui_view_paper_gradientActionPerformed

    //
    //  SNAP TO MIDPOINTS / IGAZÍTÁS SZAKASZFELEZŐKHÖZ
    //
    private void ui_edit_snap_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_snap_2ActionPerformed

        if (ui_edit_snap_2.isSelected()) {
            snap2 = 2;
        } else {
            snap2 = 1;
        }
    }//GEN-LAST:event_ui_edit_snap_2ActionPerformed

    //
    //  SNAP TO TRISECTION POINTS / IGAZÍTÁS HARMADOLÓPONTOKHOZ
    //
    private void ui_edit_snap_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_snap_3ActionPerformed

        if (ui_edit_snap_3.isSelected()) {
            snap3 = 3;
        } else {
            snap3 = 1;
        }
    }//GEN-LAST:event_ui_edit_snap_3ActionPerformed

    //
    //  SNAP TO QUADRISECTION POINTS / IGAZÍTÁS NEGYEDELŐPONTOKHOZ
    //
    private void ui_edit_snap_4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_edit_snap_4ActionPerformed

        if (ui_edit_snap_4.isSelected()) {
            snap4 = 4;
        } else {
            snap4 = 1;
        }
    }//GEN-LAST:event_ui_edit_snap_4ActionPerformed

    private void ui_file_export_togif_revolvingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_export_togif_revolvingActionPerformed

        if (gif_export.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            try {

                final javax.swing.JDialog exporting = new javax.swing.JDialog(this);
                exporting.setUndecorated(true);
                javax.swing.JLabel loadmsg = new javax.swing.JLabel(Dictionary.getString("exporting..."));
                loadmsg.setForeground(Color.RED);
                exporting.setLayout(new java.awt.BorderLayout());
                exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
                exporting.getContentPane().setBackground(Color.YELLOW);
                exporting.pack();
                exporting.setResizable(false);
                exporting.setLocationRelativeTo(null);
                exporting.setVisible(true);

                final Exception[] unreportedException = {null};

                new javax.swing.SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() {

                        try {

                            terminal.execute("camera [" + oPanel1.PanelCamera.camera_dir[0] + " " + oPanel1.PanelCamera.camera_dir[1] + " " + oPanel1.PanelCamera.camera_dir[2] + "] "
                                    + "[" + oPanel1.PanelCamera.axis_x[0] + " " + oPanel1.PanelCamera.axis_x[1] + " " + oPanel1.PanelCamera.axis_x[2] + "] "
                                    + "[" + oPanel1.PanelCamera.axis_y[0] + " " + oPanel1.PanelCamera.axis_y[1] + " " + oPanel1.PanelCamera.axis_y[2] + "] "
                                    + "color " + oPanel1.getFrontColor() + " "
                                    + "filename [" + gif_export.getSelectedFile().getPath()
                                    + (gif_export.getSelectedFile().getPath().endsWith(".gif") ? "] export-revolving-gif" : ".gif] export-revolving-gif"),
                                    OrigamiScriptTerminal.Access.ROOT);
                            oPanel1.update(terminal.TerminalOrigami);
                            oPanel1.linerOff();
                            oPanel1.reset();
                            pPanel1.update(terminal.TerminalOrigami);
                            pPanel1.reset();
                            oPanel1.setToolTipText(oPanel1_tip1);
                            pPanel1.setToolTipText(pPanel1_tip1);
                            EditorState = ControlState.KESZENLET;
                            scroll_angle = 0;
                        } catch (Exception ex) {

                            unreportedException[0] = ex;
                            exporting.setVisible(false);
                            exporting.dispose();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {

                        exporting.setVisible(false);
                        exporting.dispose();
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("finished"), "Message", javax.swing.JOptionPane.PLAIN_MESSAGE);
                    }
                }.execute();

                if (unreportedException[0] != null) {
                    throw unreportedException[0];
                }
            } catch (Exception ex) {

                oPanel1.update(terminal.TerminalOrigami);
                pPanel1.update(terminal.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ui_file_export_togif_revolvingActionPerformed

    private void ui_file_export_togif_foldingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_export_togif_foldingActionPerformed

        if (gif_export.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            try {

                final javax.swing.JDialog exporting = new javax.swing.JDialog(this);
                exporting.setUndecorated(true);
                javax.swing.JLabel loadmsg = new javax.swing.JLabel(Dictionary.getString("exporting..."));
                loadmsg.setForeground(Color.RED);
                exporting.setLayout(new java.awt.BorderLayout());
                exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
                exporting.getContentPane().setBackground(Color.YELLOW);
                exporting.pack();
                exporting.setResizable(false);
                exporting.setLocationRelativeTo(null);
                exporting.setVisible(true);

                final Exception[] unreportedException = {null};

                new javax.swing.SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() {

                        try {

                            terminal.execute("camera [" + oPanel1.PanelCamera.camera_dir[0] + " " + oPanel1.PanelCamera.camera_dir[1] + " " + oPanel1.PanelCamera.camera_dir[2] + "] "
                                    + "[" + oPanel1.PanelCamera.axis_x[0] + " " + oPanel1.PanelCamera.axis_x[1] + " " + oPanel1.PanelCamera.axis_x[2] + "] "
                                    + "[" + oPanel1.PanelCamera.axis_y[0] + " " + oPanel1.PanelCamera.axis_y[1] + " " + oPanel1.PanelCamera.axis_y[2] + "] "
                                    + "color " + oPanel1.getFrontColor() + " "
                                    + "filename [" + gif_export.getSelectedFile().getPath()
                                    + (gif_export.getSelectedFile().getPath().endsWith(".gif") ? "] export-gif" : ".gif] export-gif"),
                                    OrigamiScriptTerminal.Access.ROOT);
                            oPanel1.update(terminal.TerminalOrigami);
                            oPanel1.linerOff();
                            oPanel1.reset();
                            pPanel1.update(terminal.TerminalOrigami);
                            pPanel1.reset();
                            oPanel1.setToolTipText(oPanel1_tip1);
                            pPanel1.setToolTipText(pPanel1_tip1);
                            EditorState = ControlState.KESZENLET;
                            scroll_angle = 0;
                        } catch (Exception ex) {

                            unreportedException[0] = ex;
                            exporting.setVisible(false);
                            exporting.dispose();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {

                        exporting.setVisible(false);
                        exporting.dispose();
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("finished"), "Message", javax.swing.JOptionPane.PLAIN_MESSAGE);
                    }
                }.execute();

                if (unreportedException[0] != null) {
                    throw unreportedException[0];
                }
            } catch (Exception ex) {
                oPanel1.update(terminal.TerminalOrigami);
                pPanel1.update(terminal.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ui_file_export_togif_foldingActionPerformed

    private void ui_file_export_creaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ui_file_export_creaseActionPerformed
        
        if (png_export.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {

            try {
                terminal.execute("filename [" + png_export.getSelectedFile().getPath()
                        + (png_export.getSelectedFile().getPath().endsWith(".png") ? "] export-png" : ".png] export-png"),
                        OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal.TerminalOrigami);
                oPanel1.linerOff();
                oPanel1.reset();
                pPanel1.update(terminal.TerminalOrigami);
                pPanel1.reset();
                oPanel1.setToolTipText(oPanel1_tip1);
                pPanel1.setToolTipText(pPanel1_tip1);
                EditorState = ControlState.KESZENLET;
                scroll_angle = 0;
            } catch (Exception ex) {
                oPanel1.update(terminal.TerminalOrigami);
                pPanel1.update(terminal.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ui_file_export_creaseActionPerformed

    /**
     * @param args the command line arguments
     */
    static public void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OrigamiEditorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OrigamiEditorUI().setVisible(true);
            }
        });
    }

    private boolean Igazit1(int sugar) {

        int v1ujX = -1;
        int v1ujY = -1;

        for (int[] osztohely : oPanel1.PanelCamera.alignmentPoints(terminal.TerminalOrigami, snap2, snap3, snap4)) {

            if ((liner1X - oPanel1.PanelCamera.xshift - osztohely[0])
                    * (liner1X - oPanel1.PanelCamera.xshift - osztohely[0])
                    + (liner1Y - oPanel1.PanelCamera.yshift - osztohely[1])
                    * (liner1Y - oPanel1.PanelCamera.yshift - osztohely[1]) < sugar) {

                v1ujX = osztohely[0] + oPanel1.PanelCamera.xshift;
                v1ujY = osztohely[1] + oPanel1.PanelCamera.yshift;
                break;
            }
        }
        if (v1ujX != -1) {
            liner1X = v1ujX;
            liner1Y = v1ujY;
            return true;
        }
        return false;
    }

    private boolean Igazit2(int sugar) {

        int v2ujX = -1;
        int v2ujY = -1;

        for (int[] osztohely : oPanel1.PanelCamera.alignmentPoints(terminal.TerminalOrigami, snap2, snap3, snap4)) {

            if ((liner2X - oPanel1.PanelCamera.xshift - osztohely[0])
                    * (liner2X - oPanel1.PanelCamera.xshift - osztohely[0])
                    + (liner2Y - oPanel1.PanelCamera.yshift - osztohely[1])
                    * (liner2Y - oPanel1.PanelCamera.yshift - osztohely[1]) < sugar) {

                v2ujX = osztohely[0] + oPanel1.PanelCamera.xshift;
                v2ujY = osztohely[1] + oPanel1.PanelCamera.yshift;
                break;
            }
        }
        if (v2ujX != -1) {
            liner2X = v2ujX;
            liner2Y = v2ujY;
            return true;
        }
        return false;
    }

    private int[] LaposIgazitas(int x, int y, int sugar) {

        int ujX = -1;
        int ujY = -1;

        for (int[] osztohely : pPanel1.PanelCamera.alignmentPoints2d(terminal.TerminalOrigami)) {

            if ((x - pPanel1.PanelCamera.xshift - osztohely[0])
                    * (x - pPanel1.PanelCamera.xshift - osztohely[0])
                    + (y - pPanel1.PanelCamera.yshift - osztohely[1])
                    * (y - pPanel1.PanelCamera.yshift - osztohely[1]) < sugar) {

                ujX = osztohely[0] + pPanel1.PanelCamera.xshift;
                ujY = osztohely[1] + pPanel1.PanelCamera.yshift;
                break;
            }
        }
        if (ujX != -1) {
            return new int[]{ujX, ujY};
        }
        return new int[]{x, y};
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField jTextField1;
    private origamieditor3d.OrigamiPanel oPanel1;
    private origamieditor3d.PaperPanel pPanel1;
    private javax.swing.JMenu ui_edit;
    private javax.swing.JMenuItem ui_edit_angle;
    private javax.swing.JCheckBoxMenuItem ui_edit_neusis;
    private javax.swing.JMenuItem ui_edit_plane;
    private javax.swing.JMenuItem ui_edit_redo;
    private javax.swing.JMenu ui_edit_snap;
    private javax.swing.JCheckBoxMenuItem ui_edit_snap_1;
    private javax.swing.JCheckBoxMenuItem ui_edit_snap_2;
    private javax.swing.JCheckBoxMenuItem ui_edit_snap_3;
    private javax.swing.JCheckBoxMenuItem ui_edit_snap_4;
    private javax.swing.JMenuItem ui_edit_undo;
    private javax.swing.JMenu ui_file;
    private javax.swing.JMenu ui_file_export;
    private javax.swing.JMenuItem ui_file_export_crease;
    private javax.swing.JMenu ui_file_export_togif;
    private javax.swing.JMenuItem ui_file_export_togif_folding;
    private javax.swing.JMenuItem ui_file_export_togif_revolving;
    private javax.swing.JMenuItem ui_file_export_toopenctm;
    private javax.swing.JMenuItem ui_file_export_topdf;
    private javax.swing.JMenu ui_file_new;
    private javax.swing.JMenuItem ui_file_new_a4;
    private javax.swing.JMenu ui_file_new_bases;
    private javax.swing.JMenuItem ui_file_new_dollar;
    private javax.swing.JMenuItem ui_file_new_hexagonal;
    private javax.swing.JMenuItem ui_file_new_square;
    private javax.swing.JMenuItem ui_file_open;
    private javax.swing.JMenu ui_file_sample;
    private javax.swing.JMenuItem ui_file_save;
    private javax.swing.JMenuItem ui_file_saveas;
    private javax.swing.JMenu ui_help;
    private javax.swing.JMenuItem ui_help_about;
    private javax.swing.JMenu ui_tutorials;
    private javax.swing.JMenuItem ui_tutorials_internet;
    private javax.swing.JMenu ui_view;
    private javax.swing.JCheckBoxMenuItem ui_view_best;
    private javax.swing.JMenuItem ui_view_options;
    private javax.swing.JMenu ui_view_paper;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_gradient;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_image;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_none;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_plain;
    private javax.swing.JCheckBoxMenuItem ui_view_show;
    private javax.swing.JMenuItem ui_view_timeline;
    private javax.swing.JCheckBoxMenuItem ui_view_zoom;
    // End of variables declaration//GEN-END:variables

    public OrigamiPanel oPanel1() {
        return oPanel1;
    }

    public PaperPanel pPanel1() {
        return pPanel1;
    }
}
