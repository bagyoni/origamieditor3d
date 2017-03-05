// This file is part of Origami Editor 3D.
// Copyright (C) 2013-2017 Bágyoni Attila <ba-sz-at@users.sourceforge.net>
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

import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Origami;
import origamieditor3d.origami.OrigamiIO;
import origamieditor3d.origami.OrigamiScriptTerminal;
import origamieditor3d.resources.BaseModels;
import origamieditor3d.resources.Dictionary;
import origamieditor3d.resources.Instructor;
import origamieditor3d.resources.ExampleModels;
import javax.swing.JSeparator;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiEditorUI extends javax.swing.JFrame {

    final static private long serialVersionUID = 1L;
    
    final private OrigamiScriptTerminal terminal1;
    final private DialogManager dialogManager1;
    
    private Integer mouseDragX, mouseDragY;
    private int rotation_angle;
    private Integer ruler1X, ruler1Y, ruler2X, ruler2Y;
    
    private ControlState EditorState, SecondaryState;
    private boolean alignOn;
    private int alignment_radius;
    private boolean zoomOnScroll;
    private boolean alwaysInMiddle;
    private boolean neusisOn;
    private int foldNumber;
    private String filepath;
    
    private boolean save_paper_color;
    private java.awt.image.BufferedImage tex;
    
    private boolean saved;
    private boolean changeListenerShutUp;
    
    private int snap2, snap3, snap4;
    private boolean targetOn;
    
    private javax.swing.JFrame ui_options;
    private javax.swing.JPopupMenu ui_foldingops;

    private enum ControlState {

        STANDBY, RULER1, RULER2, RULER_ROT, TRI0, TRI1, TRI2, TRI3, TRI_ROT, PLANETHRU, ANGLE_BISECT
    }

    public OrigamiEditorUI() {

        setIconImage(
                java.awt.Toolkit.getDefaultToolkit()
                .getImage(getClass().getClassLoader().getResource("res/icon.png")));

        //Initialize terminal and dialog manager
        terminal1 = new OrigamiScriptTerminal(OrigamiScriptTerminal.Access.USER);
        dialogManager1 = new DialogManager(this);
        
        // Look for an update
        dialogManager1.lookForUpdate();
        
        //Initialize UI
        initComponents();
        initViewOptionsWindow();
        initFoldingOptionsMenu();
        relabel();
        Instructor.getString("asdasd");
        setTitle("Origami Editor 3D");
        setLocationRelativeTo(null);

        // Redirect standard output to the terminal log
        java.io.OutputStream sysout = new java.io.OutputStream() {

            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();

            @Override
            public void write(int ch) {

                bytes.write(ch);
                if (ch == 10) {

                    terminal_log.append(bytes.toString());
                    bytes.reset();
                }
            }
        };
        System.setOut(new java.io.PrintStream(sysout));

        // Load base model entries into the menu
        final BaseModels bases = new BaseModels();
        final java.util.ArrayList<String> basenames = bases.names();
        for (int i = 0; i < basenames.size(); i++) {

            final int ind = i;
            final javax.swing.JMenuItem baseitem = new javax.swing.JMenuItem(Dictionary.getString(basenames.get(i)));
            baseitem.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    if (!saved) {
                        if (!dialogManager1.canICloseFile()) {
                            return;
                        }
                    }
                    filepath = null;
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

                        terminal1.TerminalOrigami = OrigamiIO.read_gen2(new java.io.ByteArrayInputStream(bytes), null);
                        terminal1.historyReset();

                        oPanel1.update(terminal1.TerminalOrigami);
                        pPanel1.update(terminal1.TerminalOrigami);

                        oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                                    / terminal1.TerminalOrigami.circumscribedSquareSize());
                        }
                        pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                            pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                                    / terminal1.TerminalOrigami.circumscribedSquareSize());
                        }
                        if (alwaysInMiddle) {
                            oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                        }
                        oPanel1.randomizeFrontColor();
                        oPanel1.PanelCamera.setOrthogonalView(0);
                        rotation_angle = 0;
                        defaultify();
                        saved = true;
                        setTitle("Origami Editor 3D");
                    }
                    catch (Exception ex) {
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("h005"));
                    }
                    foldNumber = terminal1.TerminalOrigami.history_pointer();
                    changeListenerShutUp = true;
                    timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
                    changeListenerShutUp = false;
                    timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
                }
            });
            ui_file_new_bases.add(baseitem);
        }
        
        // Load example model entries into the menu
        ui_file_example.getPopupMenu().setLayout(new java.awt.GridLayout(0, 2));
        final ExampleModels examples = new ExampleModels();
        final java.util.ArrayList<String> modnames = examples.names();
        for (int i = 0; i < modnames.size(); i++) {
            final int ind = i;
            final javax.swing.JMenuItem modelitem = new javax.swing.JMenuItem(Dictionary.getString(modnames.get(i)));
            modelitem.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    if (!saved) {
                        if (!dialogManager1.canICloseFile()) {
                            return;
                        }
                    }
                    filepath = null;
                    try (java.io.InputStream fis = examples.getFile(modnames.get(ind))) {

                        java.util.ArrayList<Byte> bytesb = new java.util.ArrayList<>();
                        int fisbyte;
                        while ((fisbyte = fis.read()) != -1) {
                            bytesb.add((byte) fisbyte);
                        }
                        byte[] bytes = new byte[bytesb.size()];
                        for (int i = 0; i < bytesb.size(); i++) {
                            bytes[i] = bytesb.get(i);
                        }

                        int papercolor = oPanel1.getFrontColor();
                        int[] rgb = { (papercolor >>> 16) & 0xFF, (papercolor >>> 8) & 0xFF, papercolor & 0xFF };
                        
                        terminal1.TerminalOrigami = OrigamiIO.read_gen2(new java.io.ByteArrayInputStream(bytes), rgb);
                        terminal1.historyReset();
                        
                        oPanel1.setFrontColor(rgb[0]*0x10000 + rgb[1]*0x100 + rgb[2]);
                        
                        oPanel1.update(terminal1.TerminalOrigami);
                        pPanel1.update(terminal1.TerminalOrigami);

                        oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                                    / terminal1.TerminalOrigami.circumscribedSquareSize());
                        }
                        pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                            pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                                    / terminal1.TerminalOrigami.circumscribedSquareSize());
                        }
                        if (alwaysInMiddle) {
                            oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                        }
                        oPanel1.PanelCamera.setOrthogonalView(0);
                        rotation_angle = 0;
                        defaultify();
                        saved = true;
                        setTitle("Origami Editor 3D");
                    }
                    catch (Exception ex) {
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("h005"));
                    }
                    foldNumber = terminal1.TerminalOrigami.history_pointer();
                    changeListenerShutUp = true;
                    timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
                    changeListenerShutUp = false;
                    timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
                }
            });
            ui_file_example.add(modelitem);
        }

        // Confirmation dialog on closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!saved) {
                    if (!dialogManager1.canICloseFile()) {
                        return;
                    }
                }
                ui_options.dispose();
                OrigamiEditorUI.this.dispose();
                System.exit(0);
            }
        });

        saved = true;
        mouseDragX = null;
        mouseDragY = null;
        rotation_angle = 0;
        ruler1X = null;
        ruler1Y = null;
        ruler2X = null;
        ruler2Y = null;
        EditorState = (SecondaryState = ControlState.STANDBY);
        alignment_radius = 100;
        zoomOnScroll = true;
        alwaysInMiddle = true;
        neusisOn = false;
        
        oPanel1.antialiasOn();
        oPanel1.randomizeFrontColor();
        oPanel1.PanelCamera.xshift = oPanel1.getWidth() / 2;
        oPanel1.PanelCamera.yshift = oPanel1.getHeight() / 2;
        pPanel1.PanelCamera.xshift = pPanel1.getWidth() / 2;
        pPanel1.PanelCamera.yshift = pPanel1.getHeight() / 2;
        try {

            terminal1.execute("version 1");
            terminal1.execute("paper square new");
        }
        catch (Exception exc) {
        }
        oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                / terminal1.TerminalOrigami.circumscribedSquareSize());
        pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                / terminal1.TerminalOrigami.circumscribedSquareSize());

        ui_panels.setDividerLocation(0.5);
        ui_panels.setResizeWeight(0.5);

        oPanel1.update(terminal1.TerminalOrigami);
        oPanel1.repaint();

        pPanel1.update(terminal1.TerminalOrigami);
        pPanel1.repaint();

        javax.swing.ToolTipManager.sharedInstance().setInitialDelay(0);
        javax.swing.ToolTipManager.sharedInstance().setDismissDelay(1000);
        oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
        pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));

        oPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        pPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        this.setMinimumSize(new java.awt.Dimension(200, 200));

        filepath = null;
        tex = null;
        save_paper_color = true;

        // Load language entries into menu
        final java.util.ResourceBundle locales = java.util.ResourceBundle.getBundle("locales");
        java.util.Set<String> locnames = locales.keySet();
        for (final String locname : locnames) {

            final javax.swing.JMenuItem locitem = new javax.swing.JMenuItem(locname);
            locitem.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        terminal1.execute(locales.getString(locname));
                        relabel();
                    }
                    catch (Exception ex) {
                    }
                }
            });
            ui_help_lang.add(locitem);
        }
        
        //Disable tooltips
        javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
    }

    private void relabel() {

        ui_file.setText(Dictionary.getString("ui.file"));
        ui_file_new.setText(Dictionary.getString("ui.file.new"));
        ui_file_new_square.setText(Dictionary.getString("ui.file.new.square"));
        ui_file_new_a4.setText(Dictionary.getString("ui.file.new.a4"));
        ui_file_new_hexagonal.setText(Dictionary.getString("ui.file.new.hexagonal"));
        ui_file_new_dollar.setText(Dictionary.getString("ui.file.new.dollar"));
        ui_file_new_bases.setText(Dictionary.getString("ui.file.new.bases"));
        ui_file_example.setText(Dictionary.getString("ui.file.example"));
        ui_file_open.setText(Dictionary.getString("ui.file.open"));
        ui_file_save.setText(Dictionary.getString("ui.file.save"));
        ui_file_saveas.setText(Dictionary.getString("ui.file.saveas"));
        ui_file_export.setText(Dictionary.getString("ui.file.export"));
        ui_file_export_topdf.setText(Dictionary.getString("ui.file.export.topdf"));
        ui_file_export_toopenctm.setText(Dictionary.getString("ui.file.export.toopenctm"));
        ui_file_export_togif.setText(Dictionary.getString("ui.file.export.togif"));
        ui_file_export_togif_revolving.setText(Dictionary.getString("ui.file.export.togif.revolving"));
        ui_file_export_togif_folding.setText(Dictionary.getString("ui.file.export.togif.folding"));
        ui_file_export_toself.setText(Dictionary.getString("ui.file.export.tostandalone"));
        ui_file_export_crease.setText(Dictionary.getString("ui.file.export.crease"));
        ui_file_properties.setText(Dictionary.getString("ui.file.properties"));
        
        ui_edit.setText(Dictionary.getString("ui.edit"));
        ui_edit_undo.setText(Dictionary.getString("ui.edit.undo"));
        ui_edit_redo.setText(Dictionary.getString("ui.edit.redo"));
        ui_edit_plane.setText(Dictionary.getString("ui.edit.plane"));
        ui_edit_angle.setText(Dictionary.getString("ui.edit.angle"));
        ui_edit_neusis.setText(Dictionary.getString("ui.edit.neusis"));
        
        ui_view.setText(Dictionary.getString("ui.view"));
        ui_view_paper.setText(Dictionary.getString("ui.view.paper"));
        ui_view_paper_image.setText(Dictionary.getString("ui.view.paper.image"));
        ui_view_paper_gradient.setText(Dictionary.getString("ui.view.paper.gradient"));
        ui_view_paper_plain.setText(Dictionary.getString("ui.view.paper.plain"));
        ui_view_paper_none.setText(Dictionary.getString("ui.view.paper.none"));
        ui_view_use.setText(Dictionary.getString("ui.view.use"));
        ui_view_show.setText(Dictionary.getString("ui.view.show"));
        ui_view_zoom.setText(Dictionary.getString("ui.view.zoom"));
        ui_view_best.setText(Dictionary.getString("ui.view.best"));
        ui_view_timeline.setText(Dictionary.getString("ui.view.timeline"));
        ui_view_options.setText(Dictionary.getString("ui.view.options"));
        
        ui_help.setText(Dictionary.getString("ui.help"));
        ui_help_user.setText(Dictionary.getString("ui.help.user"));
        ui_help_show.setText(Dictionary.getString("ui.help.show"));
        ui_help_lang.setText(Dictionary.getString("ui.help.language"));
        ui_help_about.setText(Dictionary.getString("ui.help.about"));
        
        jTabbedPane1.setTitleAt(0, Dictionary.getString("ui.editor"));
        jTabbedPane1.setTitleAt(1, Dictionary.getString("ui.scripting"));
        
        oPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(Dictionary.getString("ui.3dview")));
        pPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(Dictionary.getString("ui.crease")));
        ui_select.setText(Dictionary.getString("ui.select"));
        ui_plane.setText(Dictionary.getString("ui.through"));
        ui_angle.setText(Dictionary.getString("ui.angle"));
        
        ui_snap.setText(Dictionary.getString("ui.snap"));
        ui_snap_1.setToolTipText(Dictionary.getString("tooltip.snap.1"));
        ui_snap_2.setToolTipText(Dictionary.getString("tooltip.snap.2"));
        ui_snap_3.setToolTipText(Dictionary.getString("tooltip.snap.3"));
        ui_snap_4.setToolTipText(Dictionary.getString("tooltip.snap.4"));
        
        ui_timeline_label.setText(Dictionary.getString("ui.timeline"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        ui_editor = new javax.swing.JSplitPane();
        ui_panels = new javax.swing.JSplitPane();
        oPanel1 = new origamieditor3d.OrigamiPanel();
        pPanel1 = new origamieditor3d.PaperPanel();
        ui_editor_notimeline = new javax.swing.JSplitPane();
        ui_toolbars = new javax.swing.JSplitPane();
        ui_rightbar = new javax.swing.JToolBar();
        ui_select = new javax.swing.JToggleButton();
        ui_plane = new javax.swing.JToggleButton();
        ui_angle = new javax.swing.JToggleButton();
        ui_leftbar = new javax.swing.JToolBar();
        ui_snap = new javax.swing.JLabel();
        ui_snap_separator = new javax.swing.JToolBar.Separator();
        ui_snap_1 = new javax.swing.JToggleButton();
        ui_snap_2 = new javax.swing.JToggleButton();
        ui_snap_3 = new javax.swing.JToggleButton();
        ui_snap_4 = new javax.swing.JToggleButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        terminal_log = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        ui_file = new javax.swing.JMenu();
        ui_file_new = new javax.swing.JMenu();
        ui_file_new_square = new javax.swing.JMenuItem();
        ui_file_new_a4 = new javax.swing.JMenuItem();
        ui_file_new_hexagonal = new javax.swing.JMenuItem();
        ui_file_new_dollar = new javax.swing.JMenuItem();
        ui_file_new_bases = new javax.swing.JMenu();
        ui_file_example = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        ui_file_open = new javax.swing.JMenuItem();
        ui_file_save = new javax.swing.JMenuItem();
        ui_file_saveas = new javax.swing.JMenuItem();
        ui_file_export = new javax.swing.JMenu();
        ui_file_export_topdf = new javax.swing.JMenuItem();
        ui_file_export_toopenctm = new javax.swing.JMenuItem();
        ui_file_export_togif = new javax.swing.JMenu();
        ui_file_export_togif_revolving = new javax.swing.JMenuItem();
        ui_file_export_togif_folding = new javax.swing.JMenuItem();
        ui_file_export_toself = new javax.swing.JMenuItem();
        ui_file_export_crease = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        ui_file_properties = new javax.swing.JMenuItem();
        ui_edit = new javax.swing.JMenu();
        ui_edit_undo = new javax.swing.JMenuItem();
        ui_edit_redo = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        ui_edit_plane = new javax.swing.JMenuItem();
        ui_edit_angle = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        ui_edit_neusis = new javax.swing.JCheckBoxMenuItem();
        ui_view = new javax.swing.JMenu();
        ui_view_paper = new javax.swing.JMenu();
        ui_view_paper_image = new javax.swing.JCheckBoxMenuItem();
        ui_view_paper_gradient = new javax.swing.JCheckBoxMenuItem();
        ui_view_paper_plain = new javax.swing.JCheckBoxMenuItem();
        ui_view_paper_none = new javax.swing.JCheckBoxMenuItem();
        ui_view_use = new javax.swing.JCheckBoxMenuItem();
        ui_view_show = new javax.swing.JCheckBoxMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        ui_view_zoom = new javax.swing.JCheckBoxMenuItem();
        ui_view_best = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        ui_help_show = new javax.swing.JCheckBoxMenuItem();
        ui_view_timeline = new javax.swing.JCheckBoxMenuItem();
        ui_view_options = new javax.swing.JMenuItem();
        ui_help = new javax.swing.JMenu();
        ui_help_user = new javax.swing.JMenuItem();
        ui_help_lang = new javax.swing.JMenu();
        ui_help_about = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        ui_editor.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        ui_editor.setDividerSize(0);
        ui_editor.setResizeWeight(1.0);
        ui_editor.setEnabled(false);
        ui_editor.setPreferredSize(new java.awt.Dimension(802, 459));
        ui_editor.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ui_editorComponentResized(evt);
            }
        });

        ui_editor_notimeline.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        ui_editor_notimeline.setDividerSize(0);
        ui_editor_notimeline.setResizeWeight(1);
        ui_editor_notimeline.setEnabled(false);
        
        ui_panels.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
        ui_panels.setDividerLocation(0.5);
        ui_panels.setDividerSize(0);
        ui_panels.setResizeWeight(0.5);
        ui_panels.setEnabled(false);
        ui_panels.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ui_panelsComponentResized(evt);
            }
        });

        oPanel1.setBackground(new java.awt.Color(255, 255, 255));
        oPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("3D View"));
        oPanel1.setPreferredSize(new java.awt.Dimension(400, 400));
        oPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                oPanel1MouseMoved(evt);
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                oPanel1MouseDragged(evt);
            }
        });
        oPanel1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                oPanel1MouseWheelMoved(evt);
            }
        });
        oPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                oPanel1MousePressed(evt);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                oPanel1MouseClicked(evt);
            }
        });
        oPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                oPanel1ComponentResized(evt);
            }
        });
        ui_panels.setLeftComponent(oPanel1);

        pPanel1.setBackground(java.awt.Color.white);
        pPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Crease Pattern"));
        pPanel1.setPreferredSize(new java.awt.Dimension(400, 400));
        pPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pPanel1MouseClicked(evt);
            }
        });
        pPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                pPanel1ComponentResized(evt);
            }
        });
        ui_panels.setRightComponent(pPanel1);

        ui_editor_notimeline.setTopComponent(ui_panels);
        
        ui_toolbars.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
        ui_toolbars.setDividerLocation(0.5);
        ui_toolbars.setDividerSize(0);
        ui_toolbars.setResizeWeight(0.5);
        ui_toolbars.setMinimumSize(new java.awt.Dimension(142, 35));
        ui_toolbars.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ui_toolbarsComponentResized(evt);
            }
        });

        //initialize right toolbar
        ui_rightbar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ui_rightbar.setFloatable(false);
        ui_rightbar.setEnabled(false);
        ui_rightbar.setMaximumSize(new java.awt.Dimension(32767, 30));
        ui_rightbar.setMinimumSize(new java.awt.Dimension(30, 27));
        ui_rightbar.setPreferredSize(new java.awt.Dimension(800, 30));
        ui_rightbar.setLayout(new java.awt.GridLayout(1, 3));

        ui_select.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/target.png"))); // NOI18N
        ui_select.setSelected(true);
        targetOn = true;
        ui_select.setText("Select");
        ui_select.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        ui_select.setFocusable(false);
        ui_select.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ui_select.setIconTextGap(2);
        ui_select.setMaximumSize(new java.awt.Dimension(69, 33));
        ui_select.setMinimumSize(new java.awt.Dimension(69, 33));
        ui_select.setPreferredSize(new java.awt.Dimension(69, 33));
        ui_select.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_selectActionPerformed(evt);
            }
        });
        ui_rightbar.add(ui_select);

        ui_plane.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/planethrough.png"))); // NOI18N
        ui_plane.setSelected(false);
        ui_plane.setText("Through 3");
        ui_plane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        ui_plane.setFocusable(false);
        ui_plane.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ui_plane.setIconTextGap(2);
        ui_plane.setMaximumSize(new java.awt.Dimension(126, 33));
        ui_plane.setMinimumSize(new java.awt.Dimension(126, 33));
        ui_plane.setPreferredSize(new java.awt.Dimension(126, 33));
        ui_plane.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_planeActionPerformed(evt);
            }
        });
        ui_rightbar.add(ui_plane);

        ui_angle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/angle-bisector.png"))); // NOI18N
        ui_angle.setSelected(false);
        ui_angle.setText("Angle bisector");
        ui_angle.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        ui_angle.setFocusable(false);
        ui_angle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ui_angle.setIconTextGap(2);
        ui_angle.setMaximumSize(new java.awt.Dimension(133, 33));
        ui_angle.setMinimumSize(new java.awt.Dimension(133, 33));
        ui_angle.setPreferredSize(new java.awt.Dimension(133, 33));
        ui_angle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_angleActionPerformed(evt);
            }
        });
        ui_rightbar.add(ui_angle);

        ui_toolbars.setRightComponent(ui_rightbar);

        //initialize snap options
        ui_leftbar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ui_leftbar.setFloatable(false);
        ui_leftbar.setRollover(true);
        ui_leftbar.setEnabled(false);
        ui_leftbar.setMaximumSize(new java.awt.Dimension(175, 30));
        ui_leftbar.setMinimumSize(new java.awt.Dimension(175, 30));
        ui_leftbar.setPreferredSize(new java.awt.Dimension(175, 33));

        ui_snap.setText("Snap fineness");
        ui_leftbar.add(ui_snap);

        ui_snap_separator.setForeground(new java.awt.Color(238, 238, 238));
        ui_snap_separator.setToolTipText("");
        ui_snap_separator.setEnabled(false);
        ui_snap_separator.setSeparatorSize(new java.awt.Dimension(20, 0));
        ui_leftbar.add(ui_snap_separator);

        ui_snap_1.setSelected(true);
        ui_snap_1.setText("1");
        ui_snap_1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ui_snap_1.setFocusable(false);
        ui_snap_1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ui_snap_1.setMaximumSize(new java.awt.Dimension(33, 33));
        ui_snap_1.setMinimumSize(new java.awt.Dimension(33, 33));
        ui_snap_1.setPreferredSize(new java.awt.Dimension(33, 33));
        ui_snap_1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ui_snap_1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_snap_1ActionPerformed(evt);
            }
        });
        ui_leftbar.add(ui_snap_1);

        ui_snap_2.setSelected(true);
        ui_snap_2.setText("2");
        ui_snap_2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ui_snap_2.setFocusable(false);
        ui_snap_2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ui_snap_2.setMaximumSize(new java.awt.Dimension(33, 33));
        ui_snap_2.setMinimumSize(new java.awt.Dimension(33, 33));
        ui_snap_2.setPreferredSize(new java.awt.Dimension(33, 33));
        ui_snap_2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ui_snap_2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_snap_2ActionPerformed(evt);
            }
        });
        ui_leftbar.add(ui_snap_2);

        ui_snap_3.setText("3");
        ui_snap_3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ui_snap_3.setFocusable(false);
        ui_snap_3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ui_snap_3.setMaximumSize(new java.awt.Dimension(33, 33));
        ui_snap_3.setMinimumSize(new java.awt.Dimension(33, 33));
        ui_snap_3.setPreferredSize(new java.awt.Dimension(33, 33));
        ui_snap_3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ui_snap_3.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_snap_3ActionPerformed(evt);
            }
        });
        ui_leftbar.add(ui_snap_3);

        ui_snap_4.setText("4");
        ui_snap_4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ui_snap_4.setFocusable(false);
        ui_snap_4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ui_snap_4.setMaximumSize(new java.awt.Dimension(33, 33));
        ui_snap_4.setMinimumSize(new java.awt.Dimension(33, 33));
        ui_snap_4.setPreferredSize(new java.awt.Dimension(33, 33));
        ui_snap_4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ui_snap_4.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_snap_4ActionPerformed(evt);
            }
        });
        ui_leftbar.add(ui_snap_4);
        
        // initialize snap fineness
        alignOn = ui_snap_1.isSelected();
        if (ui_snap_2.isSelected()) {
            snap2 = 2;
        }
        else {
            snap2 = 1;
        }
        if (ui_snap_3.isSelected()) {
            snap3 = 3;
        }
        else {
            snap3 = 1;
        }
        if (ui_snap_4.isSelected()) {
            snap4 = 4;
        }
        else {
            snap4 = 1;
        }

        ui_toolbars.setLeftComponent(ui_leftbar);
        
        ui_editor_notimeline.setBottomComponent(ui_toolbars);
        
        ui_editor.setTopComponent(ui_editor_notimeline);
        
        // initialize timeline
        ui_timeline = new javax.swing.JPanel();
        ui_timeline.setLayout(new java.awt.BorderLayout());
        ui_timeline.setMinimumSize(new java.awt.Dimension(142, 30));
        ui_timeline.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ui_timeline_label = new javax.swing.JLabel("Timeline: ");
        ui_timeline.add(ui_timeline_label, java.awt.BorderLayout.WEST);
        
        timeSlider = new javax.swing.JSlider();
        timeSlider.setMinimum(0);
        timeSlider.setMaximum(0);
        timeSlider.setValue(0);
        foldNumber = 0;
        timeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                timeSliderStateChanged(e);
            }
        });
        ui_timeline.add(timeSlider, java.awt.BorderLayout.CENTER);
        changeListenerShutUp = false;
        
        ui_editor.setBottomComponent(ui_timeline);

        jTabbedPane1.addTab("Editor", ui_editor);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setEnabled(false);

        terminal_log.setEditable(false);
        terminal_log.setColumns(20);
        terminal_log.setForeground(new java.awt.Color(102, 102, 102));
        terminal_log.setRows(5);
        terminal_log.setDisabledTextColor(new java.awt.Color(102, 102, 102));
        jScrollPane3.setViewportView(terminal_log);

        jSplitPane2.setTopComponent(jScrollPane3);

        jTextField1.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
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
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_squareActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_square);

        ui_file_new_a4.setText("A4 origami");
        ui_file_new_a4.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_a4ActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_a4);

        ui_file_new_hexagonal.setText("Hexagonal origami");
        ui_file_new_hexagonal.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_hexagonalActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_hexagonal);

        ui_file_new_dollar.setText("Dollar bill origami");
        ui_file_new_dollar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_new_dollarActionPerformed(evt);
            }
        });
        ui_file_new.add(ui_file_new_dollar);

        ui_file_new_bases.setText("Bases");
        ui_file_new.add(ui_file_new_bases);

        ui_file.add(ui_file_new);

        ui_file_example.setText("Sample figures");
        ui_file.add(ui_file_example);
        ui_file.add(jSeparator1);

        ui_file_open.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        ui_file_open.setIcon(javax.swing.UIManager.getIcon("FileView.directoryIcon"));
        ui_file_open.setText("Open...");
        ui_file_open.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_openActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_open);

        ui_file_save.setIcon(javax.swing.UIManager.getIcon("FileView.floppyDriveIcon"));
        ui_file_save.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        ui_file_save.setText("Save");
        ui_file_save.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_saveActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_save);

        ui_file_saveas.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        ui_file_saveas.setText("Save As...");
        ui_file_saveas.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_saveasActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_saveas);

        ui_file_export.setText("Export");

        ui_file_export_topdf.setText("To PDF...");
        ui_file_export_topdf.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_topdfActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_topdf);

        ui_file_export_toopenctm.setText("To OpenCTM 3D File...");
        ui_file_export_toopenctm.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_toopenctmActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_toopenctm);

        ui_file_export_togif.setText("To GIF");

        ui_file_export_togif_revolving.setText("Revolving animation...");
        ui_file_export_togif_revolving.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_togif_revolvingActionPerformed(evt);
            }
        });
        ui_file_export_togif.add(ui_file_export_togif_revolving);

        ui_file_export_togif_folding.setText("Folding process...");
        ui_file_export_togif_folding.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_togif_foldingActionPerformed(evt);
            }
        });
        ui_file_export_togif.add(ui_file_export_togif_folding);

        ui_file_export.add(ui_file_export_togif);

        ui_file_export_toself.setText("To self-displaying ORI...");
        ui_file_export_toself.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_toselfActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_toself);

        ui_file_export_crease.setText("Crease pattern to PNG...");
        ui_file_export_crease.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_export_creaseActionPerformed(evt);
            }
        });
        ui_file_export.add(ui_file_export_crease);

        ui_file.add(ui_file_export);
        ui_file.add(jSeparator6);

        ui_file_properties.setText("Properties");
        ui_file_properties.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_file_propertiesActionPerformed(evt);
            }
        });
        ui_file.add(ui_file_properties);

        jMenuBar1.add(ui_file);

        ui_edit.setText("Edit");

        ui_edit_undo.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_undo.setText("Undo");
        ui_edit_undo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_undoActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_undo);

        ui_edit_redo.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_redo.setText("Redo");
        ui_edit_redo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_redoActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_redo);
        ui_edit.add(jSeparator2);

        ui_edit_plane.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_plane.setText("Plane through 3 points");
        ui_edit_plane.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_planeActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_plane);

        ui_edit_angle.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_angle.setText("Angle bisector");
        ui_edit_angle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_edit_angleActionPerformed(evt);
            }
        });
        ui_edit.add(ui_edit_angle);
        ui_edit.add(jSeparator3);

        ui_edit_neusis.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        ui_edit_neusis.setText("Neusis Mode");
        ui_edit_neusis.addActionListener(new java.awt.event.ActionListener() {
            @Override
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
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_imageActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_image);

        ui_view_paper_gradient.setSelected(true);
        ui_view_paper_gradient.setText("Gradient");
        ui_view_paper_gradient.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_gradientActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_gradient);

        ui_view_paper_plain.setText("Plain");
        ui_view_paper_plain.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_plainActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_plain);

        ui_view_paper_none.setText("None");
        ui_view_paper_none.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_paper_noneActionPerformed(evt);
            }
        });
        ui_view_paper.add(ui_view_paper_none);

        ui_view.add(ui_view_paper);

        ui_view_use.setSelected(true);
        ui_view_use.setText("Use anti-aliasing");
        ui_view_use.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_useActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_use);
        
        ui_view.add(jSeparator5);
        
        ui_view_show.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        ui_view_show.setText("Show preview");
        ui_view_show.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_showActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_show);

        ui_view_zoom.setSelected(true);
        ui_view_zoom.setText("Zoom on scroll");
        ui_view_zoom.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_zoomActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_zoom);

        ui_view_best.setSelected(true);
        ui_view_best.setText("Always in the middle");
        ui_view_best.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_bestActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_best);
        ui_view.add(jSeparator4);

        ui_view_timeline.setSelected(true);
        ui_view_timeline.setText("Timeline");
        ui_view_timeline.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_timelineActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_timeline);
        
        ui_view_options.setText("Options");
        ui_view_options.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_view_optionsActionPerformed(evt);
            }
        });
        ui_view.add(ui_view_options);

        jMenuBar1.add(ui_view);

        ui_help.setText("Help");

        ui_help_user.setText("User Guide");
        ui_help_user.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_help_userActionPerformed(evt);
            }
        });
        ui_help.add(ui_help_user);
        
        separator = new JSeparator();
        ui_help.add(separator);
        
        ui_help_lang.setText("Language");
        ui_help.add(ui_help_lang);
        
        ui_help_show.setText("Show tooltips");
        ui_help_show.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_help_showActionPerformed(evt);
            }
        });
        ui_help.add(ui_help_show);

        separator_1 = new JSeparator();
        ui_help.add(separator_1);
        
        ui_help_about.setText("About");
        ui_help_about.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_help_aboutActionPerformed(evt);
            }
        });
        
        ui_help.add(ui_help_about);

        jMenuBar1.add(ui_help);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initViewOptionsWindow() {
        
        // initialize options menu
        final javax.swing.JColorChooser paletta = new javax.swing.JColorChooser(
                new java.awt.Color(oPanel1.getFrontColor()));
        ui_options = new javax.swing.JFrame(Dictionary.getString("ui.view.options"));
        ui_options.setIconImage(getIconImage());
        java.awt.GridBagConstraints c;
        ui_options.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        ui_options.getContentPane().setLayout(new java.awt.GridBagLayout());
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.NORTH;
        ui_options.getContentPane().add(new javax.swing.JLabel(Dictionary.getString("ui.view.options.snap")), c);
        final javax.swing.JSlider snapRadiusSlider = new javax.swing.JSlider();
        snapRadiusSlider.setMinimum(5);
        snapRadiusSlider.setMaximum(20);
        snapRadiusSlider.setValue((int) Math.sqrt(alignment_radius));
        snapRadiusSlider.setLabelTable(snapRadiusSlider.createStandardLabels(15));
        snapRadiusSlider.setPaintLabels(true);
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        ui_options.getContentPane().add(snapRadiusSlider, c);
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        javax.swing.JLabel cimke2 = new javax.swing.JLabel(Dictionary.getString("ui.view.options.paper"));
        ui_options.getContentPane().add(cimke2, c);
        paletta.setPreviewPanel(new javax.swing.JPanel());
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 2;
        ui_options.getContentPane().add(paletta, c);
        final javax.swing.JCheckBox savecolor = new javax.swing.JCheckBox(Dictionary.getString("ui.view.options.save"));
        savecolor.setSelected(true);
        save_paper_color = true;
        savecolor.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_paper_color = savecolor.isSelected();
            }
        });
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 4;
        ui_options.getContentPane().add(savecolor, c);
        final javax.swing.JButton ok = new javax.swing.JButton();
        ok.setText("OK");
        ok.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                alignment_radius = snapRadiusSlider.getValue() * snapRadiusSlider.getValue();
                oPanel1.setFrontColor(paletta.getColor().getRGB());
                oPanel1.repaint();
                ui_options.dispose();
            }
        });
        c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        ui_options.getContentPane().add(ok, c);
        ui_options.setMinimumSize(new java.awt.Dimension(paletta.getMinimumSize().width,
                paletta.getMinimumSize().height + snapRadiusSlider.getMinimumSize().height + ok.getMinimumSize().height
                        + cimke2.getMinimumSize().height + 30));
        ui_options.setResizable(false);
        ui_options.setLocationRelativeTo(null);
        ui_options.pack();
    }
    
    private void initFoldingOptionsMenu() {
        
        // initialize folding options popup menu
        ui_foldingops = new javax.swing.JPopupMenu();
        final javax.swing.JMenuItem reflect = new javax.swing.JMenuItem(Dictionary.getString("ui.foldingops.reflect"));
        reflect.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_foldingops_reflect_actionPerformed(evt);
            }
        });
        final javax.swing.JMenuItem rotate = new javax.swing.JMenuItem(Dictionary.getString("ui.foldingops.rotate"));
        rotate.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_foldingops_rotate_actionPerformed(evt);
            }
        });
        final javax.swing.JMenuItem cut = new javax.swing.JMenuItem(Dictionary.getString("ui.foldingops.cut"));
        cut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ui_foldingops_cut_actionPerformed(evt);
            }
        });
        ui_foldingops.add(reflect);
        ui_foldingops.add(rotate);
        ui_foldingops.add(cut);
    }
    
    private void oPanel1MousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_oPanel1MousePressed

        mouseDragX = evt.getX();
        mouseDragY = evt.getY();
    }// GEN-LAST:event_oPanel1MousePressed

    //
    // DRAG / HÚZÁS
    //
    private void oPanel1MouseDragged(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_oPanel1MouseDragged

        if (EditorState == ControlState.STANDBY || EditorState == ControlState.TRI0 || EditorState == ControlState.TRI1
                || EditorState == ControlState.TRI2 || EditorState == ControlState.TRI3) {

            oPanel1.resetAlignmentPoint();
            oPanel1.PanelCamera.rotate((mouseDragX - evt.getX()) / (float) oPanel1.PanelCamera.zoom() / 2,
                    (evt.getY() - mouseDragY) / (float) oPanel1.PanelCamera.zoom() / 2);
            oPanel1.repaint();
            mouseDragX = evt.getX();
            mouseDragY = evt.getY();
        }
    }// GEN-LAST:event_oPanel1MouseDragged

    //
    // RESIZE / ÁTMÉRETEZÉS
    //
    private void oPanel1ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_oPanel1ComponentResized

        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                    / terminal1.TerminalOrigami.circumscribedSquareSize());
        }
        oPanel1.PanelCamera.xshift = oPanel1.getWidth() / 2;
        oPanel1.PanelCamera.yshift = oPanel1.getHeight() / 2;
    }// GEN-LAST:event_oPanel1ComponentResized

    private void ui_editorComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_ui_editorComponentResized

    }// GEN-LAST:event_ui_editorComponentResized

    //
    // TERMINAL
    //
    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jTextField1KeyTyped

        if (evt.getKeyChar() == (char) 10) {
            if (!"log".equals(OrigamiScriptTerminal.obfuscate(jTextField1.getText()))) {

                try {

                    terminal1.execute(jTextField1.getText());

                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);

                    oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                    }
                    if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                                / terminal1.TerminalOrigami.circumscribedSquareSize());
                    }
                    pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                    if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                                / terminal1.TerminalOrigami.circumscribedSquareSize());
                    }

                    System.out.println(OrigamiScriptTerminal.obfuscate(jTextField1.getText()));
                    jTextField1.setText(null);
                }
                catch (Exception exc) {

                    System.out.println(exc.getMessage());
                    jTextField1.setText(null);
                }
            }
            else {

                jTextField1.setText(null);
                terminal_log.setText("");
                for (String sor : terminal1.history()) {
                    System.out.println(sor);
                }
            }
            foldNumber = terminal1.TerminalOrigami.history_pointer();
            changeListenerShutUp = true;
            timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
            changeListenerShutUp = false;
            timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
            saved = false;
        }
    }// GEN-LAST:event_jTextField1KeyTyped

    //
    // RESIZE / ÁTMÉRETEZÉS
    //
    private void pPanel1ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_pPanel1ComponentResized

        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                / terminal1.TerminalOrigami.circumscribedSquareSize());
        pPanel1.PanelCamera.xshift = pPanel1.getWidth() / 2;
        pPanel1.PanelCamera.yshift = pPanel1.getHeight() / 2;
    }// GEN-LAST:event_pPanel1ComponentResized

    //
    // EDITING (2D) / SZERKESZTÉS (2D)
    //
    private void pPanel1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_pPanel1MouseClicked

        if (evt.getButton() != java.awt.event.MouseEvent.BUTTON1) {

            if (targetOn) {

                pPanel1.resetTracker();
                oPanel1.resetTracker();
                pPanel1.repaint();
                oPanel1.repaint();
                pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
                oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
                EditorState = ControlState.STANDBY;
            }
            else if (EditorState == ControlState.TRI1) {

                pPanel1.grabTriangleAt(0);
                pPanel1.tiltTriangleTo(null, (Integer[]) null);
                pPanel1.grabTriangleAt(0);
                oPanel1.grabTriangleAt(0);
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, (Integer[]) null);
                oPanel1.grabTriangleAt(0);
                EditorState = ControlState.TRI0;
                pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points1"));
                pPanel1.repaint();
                oPanel1.repaint();
            }
            else if (EditorState == ControlState.TRI2) {

                pPanel1.grabTriangleAt(1);
                pPanel1.tiltTriangleTo(null, (Integer[]) null);
                pPanel1.grabTriangleAt(1);
                oPanel1.grabTriangleAt(1);
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, (Integer[]) null);
                oPanel1.grabTriangleAt(1);
                EditorState = ControlState.TRI1;
                pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points2"));
                pPanel1.repaint();
                oPanel1.repaint();
            }
            else if (EditorState == ControlState.TRI3) {

                pPanel1.grabTriangleAt(2);
                pPanel1.tiltTriangleTo(null, (Integer[]) null);
                pPanel1.grabTriangleAt(2);
                oPanel1.grabTriangleAt(2);
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, (Integer[]) null);
                oPanel1.grabTriangleAt(2);
                EditorState = ControlState.TRI2;
                pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points3"));
                pPanel1.repaint();
                oPanel1.repaint();
            }
            else {
                pPanel1.reset();
                oPanel1.reset();
                pPanel1.repaint();
                oPanel1.repaint();
                pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
                oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
                EditorState = ControlState.STANDBY;
            }

        }
        else if (targetOn) {

            pPanel1.setTracker(pPanel1.PanelCamera, evt.getX(), evt.getY());
            oPanel1.setTracker(pPanel1.PanelCamera, evt.getX(), evt.getY());
            pPanel1.repaint();
            oPanel1.repaint();
            pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.unselect"));

        }
        else if (EditorState == ControlState.TRI0) {

            if (alignOn) {
                int[] ig = flatSnap(evt.getX(), evt.getY(), alignment_radius);
                pPanel1.tiltTriangleTo(null, ig[0], ig[1]);
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, ig[0], ig[1]);
            }
            else {
                pPanel1.tiltTriangleTo(null, evt.getX(), evt.getY());
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, evt.getX(), evt.getY());
            }
            pPanel1.repaint();
            oPanel1.repaint();
            pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points2"));
            EditorState = ControlState.TRI1;

        }
        else if (EditorState == ControlState.TRI1) {

            if (alignOn) {
                int[] ig = flatSnap(evt.getX(), evt.getY(), alignment_radius);
                pPanel1.tiltTriangleTo(null, ig[0], ig[1]);
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, ig[0], ig[1]);
            }
            else {
                pPanel1.tiltTriangleTo(null, evt.getX(), evt.getY());
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, evt.getX(), evt.getY());
            }
            pPanel1.repaint();
            oPanel1.repaint();
            pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points3"));
            EditorState = ControlState.TRI2;
        }
        else if (EditorState == ControlState.TRI2 || EditorState == ControlState.TRI3) {

            if (alignOn) {
                int[] ig = flatSnap(evt.getX(), evt.getY(), alignment_radius);
                pPanel1.tiltTriangleTo(null, ig[0], ig[1]);
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, ig[0], ig[1]);
            }
            else {
                pPanel1.tiltTriangleTo(null, evt.getX(), evt.getY());
                oPanel1.tiltTriangleTo(pPanel1.PanelCamera, evt.getX(), evt.getY());
            }
            pPanel1.repaint();
            oPanel1.repaint();
            EditorState = ControlState.TRI3;
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_pPanel1MouseClicked

    //
    // EDITING (3D) / SZERKESZTÉS (3D)
    //
    private void oPanel1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_oPanel1MouseClicked

        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {

            if (EditorState == ControlState.STANDBY || EditorState == ControlState.TRI0
                    || EditorState == ControlState.TRI1 || EditorState == ControlState.TRI2
                    || EditorState == ControlState.TRI3) {

                if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                    oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                            / terminal1.TerminalOrigami.circumscribedSquareSize());
                }
                oPanel1.PanelCamera.nextOrthogonalView();
                oPanel1.repaint();
            }
            else if (EditorState == ControlState.RULER_ROT) {

                double[] vonalzoNV = oPanel1.getRulerNormalvector();
                double[] vonalzoPT = oPanel1.getRulerPoint();
                
                if (pPanel1.isTracked()) {

                    try {
                        double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                                + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                                / pPanel1.PanelCamera.zoom();

                        double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                                + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[1])
                                / pPanel1.PanelCamera.zoom();

                        terminal1.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] ["
                                + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10 + "target ["
                                + magX + " " + magY + "]" + (char) 10 + "angle " + rotation_angle + (char) 10 + "rotate");
                        oPanel1.update(terminal1.TerminalOrigami);
                    }
                    catch (Exception ex) {
                        oPanel1.update(terminal1.TerminalOrigami);
                        pPanel1.update(terminal1.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10),
                                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                    }
                    pPanel1.reset();
                    oPanel1.reset();
                    pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
                }
                else {

                    try {
                        terminal1.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] ["
                                + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10 + "angle "
                                + rotation_angle + (char) 10 + "rotate");
                        oPanel1.update(terminal1.TerminalOrigami);
                    }
                    catch (Exception ex) {
                        oPanel1.update(terminal1.TerminalOrigami);
                        pPanel1.update(terminal1.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10),
                                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
                rotation_angle = 0;
                oPanel1.hideProtractor();
                defaultify();
                saved = false;
            }
            else if (EditorState == ControlState.TRI_ROT) {

                String pszo = "";
                if (SecondaryState == ControlState.PLANETHRU) {
                    pszo = "planethrough";
                }
                if (SecondaryState == ControlState.ANGLE_BISECT) {
                    pszo = "angle-bisector";
                }
                if (pPanel1.isTracked()) {

                    try {
                        double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                                + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                                / pPanel1.PanelCamera.zoom();

                        double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                                + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[1])
                                / pPanel1.PanelCamera.zoom();

                        terminal1.execute(pszo + " [" + (((double) pPanel1.linerTriangle()[0][0]
                                - pPanel1.PanelCamera.xshift
                                + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                                / pPanel1.PanelCamera.zoom())
                                + " "
                                + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                        / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                        / pPanel1.PanelCamera.zoom())
                                + " "
                                + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                        / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                        / pPanel1.PanelCamera.zoom())
                                + " "
                                + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                        / pPanel1.PanelCamera.zoom())
                                + "]" + (char) 10 + "target [" + magX + " " + magY + "]" + (char) 10 + "angle "
                                + rotation_angle + (char) 10 + "rotate");
                        oPanel1.update(terminal1.TerminalOrigami);
                    }
                    catch (Exception ex) {
                        oPanel1.update(terminal1.TerminalOrigami);
                        pPanel1.update(terminal1.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10),
                                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                    }
                    pPanel1.reset();
                    oPanel1.reset();
                    pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
                }
                else {
                    try {
                        terminal1.execute(pszo + " [" + (((double) pPanel1.linerTriangle()[0][0]
                                - pPanel1.PanelCamera.xshift
                                + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                        pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                                / pPanel1.PanelCamera.zoom())
                                + " "
                                + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                        / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                        / pPanel1.PanelCamera.zoom())
                                + " "
                                + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                        / pPanel1.PanelCamera.zoom())
                                + "] ["
                                + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                        / pPanel1.PanelCamera.zoom())
                                + " "
                                + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift
                                        + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                                pPanel1.PanelCamera.zoom())
                                                        .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                        / pPanel1.PanelCamera.zoom())
                                + "]" + (char) 10 + "angle " + rotation_angle + (char) 10 + "rotate");
                        oPanel1.update(terminal1.TerminalOrigami);
                    }
                    catch (Exception ex) {
                        oPanel1.update(terminal1.TerminalOrigami);
                        pPanel1.update(terminal1.TerminalOrigami);
                        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10),
                                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
                defaultify();
                saved = false;
            }
            else {
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
                defaultify();
            }
        }
        else if (EditorState == ControlState.STANDBY) {

            ruler1X = evt.getX();
            ruler1Y = evt.getY();
            if (alignOn) {
                snap1(alignment_radius);
            }
            EditorState = ControlState.RULER1;
            oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.liner1"));
            javax.swing.ToolTipManager.sharedInstance().mouseMoved(new java.awt.event.MouseEvent(oPanel1, 0,
                    System.currentTimeMillis(), 0, evt.getX(), evt.getY(), 0, false));
        }
        else if (EditorState == ControlState.RULER1) {

            ruler2X = evt.getX();
            ruler2Y = evt.getY();
            if (alignOn) {
                snap2(alignment_radius);
            }
            EditorState = ControlState.RULER2;
            oPanel1.setToolTipText(null);

            ui_foldingops.show(oPanel1, evt.getX(), evt.getY());

        }
        else if (EditorState == ControlState.RULER2 || EditorState == ControlState.TRI3) {
            ui_foldingops.show(oPanel1, evt.getX(), evt.getY());
        }
        else {
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
            }
            defaultify();
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
        if (alignOn) {
            oPanel1.resetAlignmentPoint();
        }
    }// GEN-LAST:event_oPanel1MouseClicked

    private void ui_foldingops_reflect_actionPerformed(java.awt.event.ActionEvent evt) {

        if (EditorState == ControlState.RULER2) {

            double[] vonalzoNV = oPanel1.getRulerNormalvector();
            double[] vonalzoPT = oPanel1.getRulerPoint();

            if (pPanel1.isTracked()) {
                try {
                    double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[1])
                            / pPanel1.PanelCamera.zoom();

                    terminal1.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] ["
                            + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10 + "target ["
                            + magX + " " + magY + "]" + (char) 10 + "reflect");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception exc) {
                    terminal_log.setText(terminal_log.getText() + (char) 10 + exc);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
            }
            else {

                try {
                    terminal1.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] ["
                            + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10 + "reflect");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
            }
            defaultify();
            saved = false;
        }
        else if (EditorState == ControlState.TRI3) {

            String pszo = "";
            if (SecondaryState == ControlState.PLANETHRU) {
                pszo = "planethrough";
            }
            if (SecondaryState == ControlState.ANGLE_BISECT) {
                pszo = "angle-bisector";
            }
            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[1])
                            / pPanel1.PanelCamera.zoom();

                    terminal1.execute(pszo + " [" + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10 + "target [" + magX + " " + magY + "]" + (char) 10 + "reflect");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
            }
            else {

                try {
                    terminal1.execute(pszo + " [" + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10 + "reflect");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
            }
            defaultify();
            saved = false;
        }

        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
        if (alignOn) {
            oPanel1.resetAlignmentPoint();
        }
    }

    private void ui_foldingops_rotate_actionPerformed(java.awt.event.ActionEvent evt) {

        if (EditorState == ControlState.RULER2) {

            EditorState = ControlState.RULER_ROT;
            rotation_angle = 0;
            oPanel1.displayProtractor(rotation_angle);
            oPanel1.repaint();
        }
        else if (EditorState == ControlState.TRI3) {

            EditorState = ControlState.TRI_ROT;
            rotation_angle = 0;
            oPanel1.displayProtractor(rotation_angle);
            oPanel1.repaint();
        }
    }

    private void ui_foldingops_cut_actionPerformed(java.awt.event.ActionEvent evt) {

        if (EditorState == ControlState.RULER2) {

            double[] vonalzoNV = oPanel1.getRulerNormalvector();
            double[] vonalzoPT = oPanel1.getRulerPoint();

            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[1])
                            / pPanel1.PanelCamera.zoom();

                    terminal1.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] ["
                            + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10 + "target ["
                            + magX + " " + magY + "]" + (char) 10 + "cut");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception exc) {
                    terminal_log.setText(terminal_log.getText() + (char) 10 + exc);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
            }
            else {

                try {
                    terminal1.execute("plane [" + vonalzoPT[0] + " " + vonalzoPT[1] + " " + vonalzoPT[2] + "] ["
                            + vonalzoNV[0] + " " + vonalzoNV[1] + " " + vonalzoNV[2] + "]" + (char) 10 + "cut");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
            }
            defaultify();
            saved = false;
        }
        else if (EditorState == ControlState.TRI3) {

            String pszo = "";
            if (SecondaryState == ControlState.PLANETHRU) {
                pszo = "planethrough";
            }
            if (SecondaryState == ControlState.ANGLE_BISECT) {
                pszo = "angle-bisector";
            }
            if (pPanel1.isTracked()) {

                try {
                    double magX = ((double) pPanel1.tracker_x() - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom();

                    double magY = ((double) pPanel1.tracker_y() - pPanel1.PanelCamera.yshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[1])
                            / pPanel1.PanelCamera.zoom();

                    terminal1.execute(pszo + " [" + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10 + "target [" + magX + " " + magY + "]" + (char) 10 + "cut");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                if (alwaysInMiddle) {
                    oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                }
            }
            else {

                try {
                    terminal1.execute(pszo + " [" + (((double) pPanel1.linerTriangle()[0][0] - pPanel1.PanelCamera.xshift
                            + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                    pPanel1.PanelCamera.zoom()).projection0(pPanel1.PanelCamera.camera_pos())[0])
                            / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[0][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[1][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[1][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "] ["
                            + (((double) pPanel1.linerTriangle()[2][0] - pPanel1.PanelCamera.xshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[0])
                                    / pPanel1.PanelCamera.zoom())
                            + " "
                            + (((double) pPanel1.linerTriangle()[2][1] - pPanel1.PanelCamera.yshift
                                    + new Camera(pPanel1.PanelCamera.xshift, pPanel1.PanelCamera.yshift,
                                            pPanel1.PanelCamera.zoom())
                                                    .projection0(pPanel1.PanelCamera.camera_pos())[1])
                                    / pPanel1.PanelCamera.zoom())
                            + "]" + (char) 10 + "cut");
                    oPanel1.update(terminal1.TerminalOrigami);
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            EditorState = ControlState.STANDBY;
            if (alwaysInMiddle) {
                oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
            }
            defaultify();
            saved = false;
        }

        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
        if (alignOn) {
            oPanel1.resetAlignmentPoint();
        }
    }

    //
    // MOUSE MOVEMENT OVER 3D VIEW / EGÉRMOZGÁS A 3D NÉZET FELETT
    //
    private void oPanel1MouseMoved(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_oPanel1MouseMoved

        if (EditorState == ControlState.STANDBY && alignOn) {

            ruler1X = evt.getX();
            ruler1Y = evt.getY();
            if (snap1(alignment_radius)) {
                oPanel1.setAlignmentPoint(ruler1X, ruler1Y);
            }
            else {
                oPanel1.resetAlignmentPoint();
            }
        }
        else if (EditorState == ControlState.RULER1) {

            ruler2X = evt.getX();
            ruler2Y = evt.getY();
            if (alignOn) {
                if (snap2(alignment_radius)) {
                    oPanel1.setAlignmentPoint(ruler2X, ruler2Y);
                }
                else {
                    oPanel1.resetAlignmentPoint();
                }
            }
            oPanel1.rulerOn(null, ruler1X, ruler1Y, ruler2X, ruler2Y);
            pPanel1.rulerOn(oPanel1.PanelCamera, ruler1X, ruler1Y, ruler2X, ruler2Y);
            pPanel1.repaint();
        }
        else if (EditorState == ControlState.RULER_ROT || EditorState == ControlState.TRI_ROT) {

            if (evt.getX() != oPanel1.getWidth() / 2 || evt.getY() != oPanel1.getHeight() / 2) {

                double r = Math
                        .max(Math.sqrt((evt.getX() - oPanel1.getWidth() / 2) * (evt.getX() - oPanel1.getWidth() / 2)
                                + (evt.getY() - oPanel1.getHeight() / 2) * (evt.getY() - oPanel1.getHeight() / 2)), 1);
                rotation_angle = evt.getX() > oPanel1.getWidth() / 2
                        ? (int) (Math.acos((oPanel1.getHeight() / 2 - evt.getY()) / r) * 180. / Math.PI)
                        : -(int) (Math.acos((oPanel1.getHeight() / 2 - evt.getY()) / r) * 180. / Math.PI);
                oPanel1.displayProtractor(rotation_angle);
            }
        }
        oPanel1.repaint();
    }// GEN-LAST:event_oPanel1MouseMoved

    //
    // PDF EXPORT
    //
    private void ui_file_export_topdfActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_export_topdfActionPerformed

        final String fpath = dialogManager1.getSaveFilePath("pdf");
        if (fpath != null) {

            if (!new java.io.File(fpath).getName().matches("[\\w\\.]+")) {
                javax.swing.JOptionPane.showMessageDialog(null, Dictionary.getString("noword"),
                        Dictionary.getString("warning"), javax.swing.JOptionPane.WARNING_MESSAGE);
            }

            try {

                final javax.swing.JDialog exporting = new javax.swing.JDialog(this);
                exporting.setUndecorated(true);
                javax.swing.JLabel loadmsg = new javax.swing.JLabel(Dictionary.getString("message.info.exporting"));
                loadmsg.setForeground(Color.RED);
                exporting.getContentPane().setLayout(new java.awt.BorderLayout());
                exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
                exporting.getContentPane().setBackground(Color.YELLOW);
                exporting.pack();
                exporting.setResizable(false);
                exporting.setLocationRelativeTo(null);
                exporting.setVisible(true);

                final Exception[] unreportedException = { null };

                new javax.swing.SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() {

                        try {

                            terminal1.execute(
                                    "title [" + new java.io.File(fpath).getName().replace(".pdf", "")
                                    + "] filename [" + fpath + "] export-autopdf",
                                    OrigamiScriptTerminal.Access.ROOT);
                            oPanel1.update(terminal1.TerminalOrigami);
                            pPanel1.update(terminal1.TerminalOrigami);
                            defaultify();
                            rotation_angle = 0;
                        }
                        catch (Exception ex) {

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
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this,
                                Dictionary.getString("message.info.export_finished"), "Message",
                                javax.swing.JOptionPane.PLAIN_MESSAGE);
                    }
                }.execute();

                if (unreportedException[0] != null) {
                    throw unreportedException[0];
                }
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_export_topdfActionPerformed

    //
    // UNDO / VISSZAVONÁS
    //
    private void ui_edit_undoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_edit_undoActionPerformed

        try {
            terminal1.execute("undo");
        }
        catch (Exception exc) {
        }

        oPanel1.update(terminal1.TerminalOrigami);
        pPanel1.update(terminal1.TerminalOrigami);

        oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
            oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                    / terminal1.TerminalOrigami.circumscribedSquareSize());
        }
        pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
        if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
            pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                    / terminal1.TerminalOrigami.circumscribedSquareSize());
        }
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
        }
        defaultify();
        rotation_angle = 0;
        saved = false;
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_edit_undoActionPerformed

    //
    // SCROLL OVER 3D VIEW / GÖRGETÉS A 3D NÉZET FELETT
    //
    private void oPanel1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_oPanel1MouseWheelMoved

        if (EditorState != ControlState.RULER1 && EditorState != ControlState.RULER2
                && EditorState != ControlState.RULER_ROT && EditorState != ControlState.TRI_ROT) {

            if (zoomOnScroll && oPanel1.PanelCamera.zoom() - 0.1 * evt.getWheelRotation() <= Camera.maximal_zoom
                    && oPanel1.PanelCamera.zoom() - 0.1 * evt.getWheelRotation() >= 0.8
                            * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                            / terminal1.TerminalOrigami.circumscribedSquareSize()) {
                oPanel1.PanelCamera.setZoom(oPanel1.PanelCamera.zoom() - 0.1 * evt.getWheelRotation());
            }
            oPanel1.repaint();
        }

    }// GEN-LAST:event_oPanel1MouseWheelMoved

    //
    // SAVE AS / MENTÉS MÁSKÉNT
    //
    private void ui_file_saveasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_saveasActionPerformed

        String fpath = dialogManager1.getSaveFilePath("ori");
        
        if (fpath != null) {
            
            try {
                
                if (save_paper_color) {
                    terminal1.execute("color " + String.valueOf(oPanel1.getFrontColor()));
                }
                else {
                    terminal1.execute("uncolor");
                }
                terminal1.execute("filename [" + fpath + "] export-ori",
                        OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                defaultify();
                rotation_angle = 0;
                filepath = fpath;
                setTitle(new java.io.File(fpath).getName() + " - Origami Editor 3D");
                saved = true;
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_saveasActionPerformed

    //
    // CTM EXPORT
    //
    private void ui_file_export_toopenctmActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_export_toopenctmActionPerformed

        String fpath = dialogManager1.getSaveFilePath("ctm");
        
        if (fpath != null) {
            
            if (!new java.io.File(fpath).getName().matches("[\\w\\.]+")) {
                javax.swing.JOptionPane.showMessageDialog(null, Dictionary.getString("noword"),
                        Dictionary.getString("warning"), javax.swing.JOptionPane.WARNING_MESSAGE);
            }

            try {
                terminal1.execute("filename [" + fpath + "] export-ctm",
                        OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                pPanel1.reset();
                defaultify();
                rotation_angle = 0;
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_export_toopenctmActionPerformed

    //
    // OPEN / MEGNYITÁS
    //
    private void ui_file_openActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_openActionPerformed

        if (!saved) {
            if (!dialogManager1.canICloseFile()) {
                return;
            }
        }
        
        String fpath = dialogManager1.getOpenFilePath("ori", "txt");

        if (fpath != null) {

            if (fpath.endsWith(".ori")) {

                try {
                    terminal1.execute("filename [" + fpath + "] open",
                            OrigamiScriptTerminal.Access.ROOT);
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);

                    oPanel1.setFrontColor(terminal1.paper_color());
                    oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                    if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                                / terminal1.TerminalOrigami.circumscribedSquareSize());
                    }
                    pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                    if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                                / terminal1.TerminalOrigami.circumscribedSquareSize());
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                    }
                    oPanel1.PanelCamera.setOrthogonalView(0);
                    rotation_angle = 0;
                    defaultify();
                    filepath = fpath;
                    setTitle(new java.io.File(fpath).getName() + " - Origami Editor 3D");
                    saved = true;
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (fpath.endsWith(".txt")) {

                try {
                    terminal1.execute("filename [" + fpath + "] load",
                            OrigamiScriptTerminal.Access.ROOT);
                    
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);

                    oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                    if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                        oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                                / terminal1.TerminalOrigami.circumscribedSquareSize());
                    }
                    pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
                    if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                        pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                                / terminal1.TerminalOrigami.circumscribedSquareSize());
                    }
                    if (alwaysInMiddle) {
                        oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
                    }
                    oPanel1.PanelCamera.setOrthogonalView(0);
                    rotation_angle = 0;
                    defaultify();
                    setTitle(new java.io.File(fpath) + " - Origami Editor 3D");
                    saved = true;
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_file_openActionPerformed

    private void ui_edit_planeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_edit_planeActionPerformed
        ui_planeActionPerformed(evt);
    }// GEN-LAST:event_ui_edit_planeActionPerformed

    //
    // REDO / MÉGIS
    //
    private void ui_edit_redoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_edit_redoActionPerformed

        try {

            if (!terminal1.history().isEmpty()) {
                terminal1.execute("redo");
            }
        }
        catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
        }
        oPanel1.update(terminal1.TerminalOrigami);
        pPanel1.update(terminal1.TerminalOrigami);
        defaultify();
        rotation_angle = 0;
        saved = false;
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_edit_redoActionPerformed

    //
    // NEW HEXAGONAL PAPER / ÚJ PAPÍR (HATSZÖG)
    //
    private void ui_file_new_hexagonalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_new_hexagonalActionPerformed

        if (!saved) {
            if (!dialogManager1.canICloseFile()) {
                return;
            }
        }
        try {

            filepath = null;
            terminal1.execute("paper hexagon new");

            oPanel1.update(terminal1.TerminalOrigami);
            pPanel1.update(terminal1.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            
            oPanel1.randomizeFrontColor();

            defaultify();
            setTitle("Origami Editor 3D");
            saved = true;
        }
        catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_file_new_hexagonalActionPerformed

    //
    // NEW SQUARE PAPER / ÚJ PAPÍR (NÉGYZET)
    //
    private void ui_file_new_squareActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_new_squareActionPerformed

        if (!saved) {
            if (!dialogManager1.canICloseFile()) {
                return;
            }
        }
        try {

            filepath = null;
            terminal1.execute("paper square new");

            oPanel1.update(terminal1.TerminalOrigami);
            pPanel1.update(terminal1.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            
            oPanel1.randomizeFrontColor();
            
            defaultify();
            setTitle("Origami Editor 3D");
            saved = true;
        }
        catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_file_new_squareActionPerformed

    //
    // NEW A4 PAPER / ÚJ PAPÍR (A4)
    //
    private void ui_file_new_a4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_new_a4ActionPerformed

        if (!saved) {
            if (!dialogManager1.canICloseFile()) {
                return;
            }
        }
        try {

            filepath = null;
            terminal1.execute("paper a4 new");

            oPanel1.update(terminal1.TerminalOrigami);
            pPanel1.update(terminal1.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            
            oPanel1.randomizeFrontColor();
            
            defaultify();
            setTitle("Origami Editor 3D");
            saved = true;
        }
        catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_file_new_a4ActionPerformed

    //
    // NEW USD PAPER / ÚJ PAPÍR (EGYDOLLÁROS)
    //
    private void ui_file_new_dollarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_new_dollarActionPerformed

        if (!saved) {
            if (!dialogManager1.canICloseFile()) {
                return;
            }
        }
        try {

            filepath = null;
            terminal1.execute("paper usd new");

            oPanel1.update(terminal1.TerminalOrigami);
            pPanel1.update(terminal1.TerminalOrigami);

            oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                oPanel1.PanelCamera.setZoom(0.8 * Math.min(oPanel1.getWidth(), oPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            pPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            if (terminal1.TerminalOrigami.circumscribedSquareSize() > 0) {
                pPanel1.PanelCamera.setZoom(0.8 * Math.min(pPanel1.getWidth(), pPanel1.getHeight())
                        / terminal1.TerminalOrigami.circumscribedSquareSize());
            }
            
            oPanel1.randomizeFrontColor();
            
            defaultify();
            setTitle("Origami Editor 3D");
            saved = true;
        }
        catch (Exception ex) {

            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        foldNumber = terminal1.TerminalOrigami.history_pointer();
        changeListenerShutUp = true;
        timeSlider.setMaximum(terminal1.TerminalOrigami.history().size());
        changeListenerShutUp = false;
        timeSlider.setValue(terminal1.TerminalOrigami.history_pointer());
    }// GEN-LAST:event_ui_file_new_dollarActionPerformed

    //
    // OPTIONS MENU / BEÁLLÍTÁSOK MENÜ
    //
    private void ui_view_optionsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_optionsActionPerformed
        ui_options.setVisible(true);
    }// GEN-LAST:event_ui_view_optionsActionPerformed

    private void ui_edit_angleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_edit_angleActionPerformed
        ui_angleActionPerformed(evt);
    }// GEN-LAST:event_ui_edit_angleActionPerformed

    //
    // PLAIN PAPER / SIMA PAPÍR
    //
    private void ui_view_paper_plainActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_plainActionPerformed

        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.PLAIN);
        ui_view_paper_plain.setSelected(true);
        ui_view_paper_image.setSelected(false);
        ui_view_paper_none.setSelected(false);
        ui_view_paper_gradient.setSelected(false);
        oPanel1.repaint();
    }// GEN-LAST:event_ui_view_paper_plainActionPerformed

    //
    // ZOOM ON SCROLL / GÖRGETÉSRE NAGYÍT
    //
    private void ui_view_zoomActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_zoomActionPerformed
        zoomOnScroll = ui_view_zoom.isSelected();
    }// GEN-LAST:event_ui_view_zoomActionPerformed

    //
    // ABOUT / NÉVJEGY
    //
    private void ui_help_aboutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_help_aboutActionPerformed

        final javax.swing.JEditorPane html = new javax.swing.JEditorPane("text/html",
                "<html><body>" + "Origami Editor 3D Version " + Constants.Version + " <br>"
                        + "Copyright © 2014 Bágyoni-Szabó Attila (ba-sz-at@users.sourceforge.net) <br>" + "<br>"
                        + "Origami Editor 3D is licensed under the GNU General Public License version 3. <br>"
                        + "<a href=\"/res/LICENSE.txt\">Click here for more information.</a> <br>" + "<br>"
                        + "Some of the origami models bundled with this program are copyrighted works. <br>"
                        + "The usage of such models within this program serves a purely demonstrational/<br>"
                        + "educational purpose, and therefore should be considered 'fair use' by all means.<br>"
                        + "</body></html>");

        html.setEditable(false);
        html.setHighlighter(null);
        html.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                if (evt.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
                    java.util.Scanner inf = new java.util.Scanner(
                            OrigamiEditorUI.this.getClass().getResourceAsStream(evt.getDescription()), "UTF-8");
                    String text = "";
                    while (inf.hasNextLine()) {
                        text += inf.nextLine() + (char) 10;
                    }
                    inf.close();
                    terminal_log.setText(text);
                    terminal_log.setCaretPosition(0);
                    jTabbedPane1.setSelectedIndex(1);
                    javax.swing.SwingUtilities.getWindowAncestor(html).dispose();
                }
            }
        });
        javax.swing.JOptionPane.showMessageDialog(this, html);
    }// GEN-LAST:event_ui_help_aboutActionPerformed

    ///
    /// SAVE / MENTÉS
    ///
    private void ui_file_saveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_saveActionPerformed

        if (filepath == null) {

            String fpath = dialogManager1.getSaveFilePath("ori");

            if (fpath != null) {

                try {
                    
                    if (save_paper_color) {
                        terminal1.execute("color " + String.valueOf(oPanel1.getFrontColor()));
                    }
                    else {
                        terminal1.execute("uncolor");
                    }
                    terminal1.execute("filename [" + fpath + "] export-ori",
                            OrigamiScriptTerminal.Access.ROOT);
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    defaultify();
                    rotation_angle = 0;
                    filepath = fpath;
                    setTitle(new java.io.File(fpath).getName() + " - Origami Editor 3D");
                    saved = true;
                }
                catch (Exception ex) {
                    oPanel1.update(terminal1.TerminalOrigami);
                    pPanel1.update(terminal1.TerminalOrigami);
                    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10),
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else {
            try {
                
                if (save_paper_color) {
                    terminal1.execute("color " + String.valueOf(oPanel1.getFrontColor()));
                }
                else {
                    terminal1.execute("uncolor");
                }
                terminal1.execute("filename [" + filepath + "] export-ori",
                        OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                defaultify();
                rotation_angle = 0;
                saved = true;
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_saveActionPerformed

    //
    // BEST FIT / LEGJOBB ILLESZKEDÉS
    //
    private void ui_view_bestActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_bestActionPerformed

        alwaysInMiddle = ui_view_best.isSelected();
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
            oPanel1.rulerOff();
            pPanel1.rulerOff();
            oPanel1.repaint();
            pPanel1.repaint();
        }
        else {
            oPanel1.PanelCamera.unadjust(terminal1.TerminalOrigami);
            oPanel1.rulerOff();
            pPanel1.rulerOff();
            oPanel1.repaint();
            pPanel1.repaint();
        }
    }// GEN-LAST:event_ui_view_bestActionPerformed

    //
    // NEUSIS MODE / NEUSZISZ MÓD
    //
    private void ui_edit_neusisActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_edit_neusisActionPerformed

        if (ui_edit_neusis.isSelected()) {

            oPanel1.setRulerMode(BasicEditing.RulerMode.Neusis);
            pPanel1.setRulerMode(BasicEditing.RulerMode.Neusis);
            neusisOn = true;
            ui_view_show.setSelected(true);
            oPanel1.previewOn();
        }
        else {

            oPanel1.setRulerMode(BasicEditing.RulerMode.Normal);
            pPanel1.setRulerMode(BasicEditing.RulerMode.Normal);
            neusisOn = false;
            ui_view_show.setSelected(false);
            oPanel1.previewOff();
        }
        oPanel1.repaint();
    }// GEN-LAST:event_ui_edit_neusisActionPerformed

    //
    // PREVIEW / ELŐNÉZET
    //
    private void ui_view_showActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_showActionPerformed

        if (ui_view_show.isSelected()) {
            oPanel1.previewOn();
        }
        else {
            oPanel1.previewOff();
        }
    }// GEN-LAST:event_ui_view_showActionPerformed

    //
    // ANTIALIASING / SIMA ÉLEK
    //
    private void ui_view_useActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_showActionPerformed

        if (ui_view_use.isSelected()) {
            oPanel1.antialiasOn();
        }
        else {
            oPanel1.antialiasOff();
        }
    }
    
    //
    // UV PAPER / UV PAPÍR
    //
    private void ui_view_paper_imageActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_imageActionPerformed

        ui_view_paper_image.setSelected(false);
        String fpath = null;
        
        if (tex == null) {

            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("loadtex"),
                    Dictionary.getString("notex"), javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, null) == 0) {

                fpath = dialogManager1.getOpenImagePath();
            }
            
        }
        else if (oPanel1.displaymode() == OrigamiPanel.DisplayMode.UV) {

            if (javax.swing.JOptionPane.showOptionDialog(this, Dictionary.getString("loadtex"),
                    Dictionary.getString("havetex"), javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, null) == 0) {

                fpath = dialogManager1.getOpenImagePath();
            }
        }
        
        if (fpath != null) {

            try {
                terminal1.execute("filename [" + fpath + "] load-texture");
                tex = terminal1.paper_texture();
                if (tex.getHeight() < (int) terminal1.TerminalOrigami.paperHeight()
                        || tex.getWidth() < (int) terminal1.TerminalOrigami.paperWidth()) {

                    String uzenet = Dictionary.getString("message.warning.smalltex",
                                    (int)terminal1.TerminalOrigami.paperWidth(),
                                    (int)terminal1.TerminalOrigami.paperHeight());
                    javax.swing.JOptionPane.showMessageDialog(this, uzenet,
                            Dictionary.getString("message.warning"),
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                }
                else if (tex.getHeight() > (int) terminal1.TerminalOrigami.paperHeight()
                        || tex.getWidth() > (int) terminal1.TerminalOrigami.paperWidth()) {

                    String uzenet = Dictionary.getString("message.warning.largetex",
                                    (int)terminal1.TerminalOrigami.paperWidth(),
                                    (int)terminal1.TerminalOrigami.paperHeight());
                    javax.swing.JOptionPane.showMessageDialog(this, uzenet,
                            Dictionary.getString("message.warning"),
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                }
                oPanel1.setTexture(tex);
                oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.UV);
                ui_view_paper_image.setSelected(true);
                ui_view_paper_plain.setSelected(false);
                ui_view_paper_none.setSelected(false);
                ui_view_paper_gradient.setSelected(false);
                oPanel1.update(terminal1.TerminalOrigami);
                oPanel1.repaint();

            }
            catch (Exception ex) {
                tex = null;
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10),
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
        else {

            oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.UV);
            ui_view_paper_image.setSelected(true);
            ui_view_paper_plain.setSelected(false);
            ui_view_paper_none.setSelected(false);
            ui_view_paper_gradient.setSelected(false);
            oPanel1.update(terminal1.TerminalOrigami);
            oPanel1.repaint();
        }
    }// GEN-LAST:event_ui_view_paper_imageActionPerformed

    //
    // EMPTY PAPER / ÜRES PAPÍR
    //
    private void ui_view_paper_noneActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_noneActionPerformed

        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.WIREFRAME);
        ui_view_paper_none.setSelected(true);
        ui_view_paper_image.setSelected(false);
        ui_view_paper_plain.setSelected(false);
        ui_view_paper_gradient.setSelected(false);
        oPanel1.repaint();
    }// GEN-LAST:event_ui_view_paper_noneActionPerformed

    //
    // TIMELINE / IDŐVONAL
    //
    private void ui_view_timelineActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_timelineActionPerformed
        
        if (!ui_view_timeline.isSelected()) {
            ui_editor.remove(ui_timeline);
        }
        else {
            ui_editor.add(ui_timeline);
        }
    }// GEN-LAST:event_ui_view_timelineActionPerformed

    //
    // TIMELINE SLIDER / IDŐVONAL CSÚSZKA
    //
    private void timeSliderStateChanged(javax.swing.event.ChangeEvent evt) {
        
        if (foldNumber == timeSlider.getValue() || changeListenerShutUp) {
            return;
        }
        if (terminal1.TerminalOrigami.history().size() < 100) {
            if (foldNumber < timeSlider.getValue()) {
                terminal1.TerminalOrigami.redo(timeSlider.getValue() - foldNumber);
            }
            else {
                terminal1.TerminalOrigami.undo(foldNumber - timeSlider.getValue());
            }
            foldNumber = timeSlider.getValue();
            oPanel1.update(terminal1.TerminalOrigami);
        }
        else { // Stop eating the CPU when it gets too complex
            if (!timeSlider.getValueIsAdjusting()) {
                if (foldNumber < timeSlider.getValue()) {
                    terminal1.TerminalOrigami.redo(timeSlider.getValue() - foldNumber);
                }
                else {
                    terminal1.TerminalOrigami.undo(foldNumber - timeSlider.getValue());
                }
                foldNumber = timeSlider.getValue();
                oPanel1.update(terminal1.TerminalOrigami);
            }
        }
        if (alwaysInMiddle) {
            oPanel1.PanelCamera.adjust(terminal1.TerminalOrigami);
        }
        defaultify();
    }
    
    //
    // USER GUIDE / FELHASZNÁLÓI KÉZIKÖNYV
    //
    private void ui_help_userActionPerformed(java.awt.event.ActionEvent evt) {
        
        try {
            java.util.Scanner inf = new java.util.Scanner(
                    new java.net.URL(Constants.InfoLink).openStream());
            String line;
            while (!(line = inf.nextLine()).startsWith("userguide_link"))
                ;
            inf.close();
            String url = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            if (Desktop.isDesktopSupported() ? Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) : false) {
                Desktop.getDesktop().browse(new java.net.URI(url));
            }
            else {
                javax.swing.JTextArea copyable = new javax.swing.JTextArea(Dictionary.getString("browser-fail", url));
                copyable.setEditable(false);
                javax.swing.JOptionPane.showMessageDialog(this, copyable, Dictionary.getString("error"), javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception ex) {
            try {
                if (Desktop.isDesktopSupported() ? Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) : false) {
                    Desktop.getDesktop().browse(new java.net.URI(Constants.UserguideLink));
                }
                else {
                    javax.swing.JTextArea copyable = new javax.swing.JTextArea(Dictionary.getString("browser-fail", Constants.UserguideLink));
                    copyable.setEditable(false);
                    javax.swing.JOptionPane.showMessageDialog(this, copyable, Dictionary.getString("error"), javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (Exception ex2) {
                javax.swing.JTextArea copyable = new javax.swing.JTextArea(Dictionary.getString("connect-fail", Constants.UserguideLink));
                copyable.setEditable(false);
                javax.swing.JOptionPane.showMessageDialog(this, copyable, Dictionary.getString("error"), javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    //
    // SHOW TOOLTIPS / EGÉRFELIRATOK MUTATÁSA
    //
    private void ui_help_showActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.ToolTipManager.sharedInstance().setEnabled(ui_help_show.isSelected());
    }

    //
    // GRADIENT PAPER / ÁRNYALT PAPÍR
    //
    private void ui_view_paper_gradientActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_gradientActionPerformed

        oPanel1.setDisplaymode(OrigamiPanel.DisplayMode.GRADIENT);
        ui_view_paper_gradient.setSelected(true);
        ui_view_paper_none.setSelected(false);
        ui_view_paper_image.setSelected(false);
        ui_view_paper_plain.setSelected(false);

        oPanel1.repaint();
    }// GEN-LAST:event_ui_view_paper_gradientActionPerformed

    //
    // REVOLVING GIF EXPORT
    //
    private void ui_file_export_togif_revolvingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_export_togif_revolvingActionPerformed

        final String fpath = dialogManager1.getSaveFilePath("gif");

        if (fpath != null) {
            
            try {

                final javax.swing.JDialog exporting = new javax.swing.JDialog(this);
                exporting.setUndecorated(true);
                javax.swing.JLabel loadmsg = new javax.swing.JLabel(Dictionary.getString("message.info.exporting"));
                loadmsg.setForeground(Color.RED);
                exporting.getContentPane().setLayout(new java.awt.BorderLayout());
                exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
                exporting.getContentPane().setBackground(Color.YELLOW);
                exporting.pack();
                exporting.setResizable(false);
                exporting.setLocationRelativeTo(null);
                exporting.setVisible(true);

                final Exception[] unreportedException = { null };

                new javax.swing.SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() {

                        try {

                            terminal1.execute(
                                    "camera [" + oPanel1.PanelCamera.camera_dir()[0] + " "
                                            + oPanel1.PanelCamera.camera_dir()[1] + " "
                                            + oPanel1.PanelCamera.camera_dir()[2] + "] ["
                                            + oPanel1.PanelCamera.axis_x()[0] + " " + oPanel1.PanelCamera.axis_x()[1]
                                            + " " + oPanel1.PanelCamera.axis_x()[2] + "] ["
                                            + oPanel1.PanelCamera.axis_y()[0] + " " + oPanel1.PanelCamera.axis_y()[1]
                                            + " " + oPanel1.PanelCamera.axis_y()[2] + "] color "
                                            + oPanel1.getFrontColor() + " filename ["
                                            + fpath + "] export-revolving-gif",
                                    OrigamiScriptTerminal.Access.ROOT);
                            oPanel1.update(terminal1.TerminalOrigami);
                            pPanel1.update(terminal1.TerminalOrigami);
                            defaultify();
                            rotation_angle = 0;
                        }
                        catch (Exception ex) {

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
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this,
                                Dictionary.getString("message.info.export_finished"), "Message",
                                javax.swing.JOptionPane.PLAIN_MESSAGE);
                    }
                }.execute();

                if (unreportedException[0] != null) {
                    throw unreportedException[0];
                }
            }
            catch (Exception ex) {

                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_export_togif_revolvingActionPerformed

    //
    // FOLDING GIF EXPORT
    //
    private void ui_file_export_togif_foldingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_export_togif_foldingActionPerformed

        final String fpath = dialogManager1.getSaveFilePath("gif");
        
        if (fpath != null) {
            
            try {

                final javax.swing.JDialog exporting = new javax.swing.JDialog(this);
                exporting.setUndecorated(true);
                javax.swing.JLabel loadmsg = new javax.swing.JLabel(Dictionary.getString("message.info.exporting"));
                loadmsg.setForeground(Color.RED);
                exporting.getContentPane().setLayout(new java.awt.BorderLayout());
                exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
                exporting.getContentPane().setBackground(Color.YELLOW);
                exporting.pack();
                exporting.setResizable(false);
                exporting.setLocationRelativeTo(null);
                exporting.setVisible(true);

                final Exception[] unreportedException = { null };

                new javax.swing.SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() {

                        try {

                            terminal1.execute(
                                    "camera [" + oPanel1.PanelCamera.camera_dir()[0] + " "
                                            + oPanel1.PanelCamera.camera_dir()[1] + " "
                                            + oPanel1.PanelCamera.camera_dir()[2] + "] ["
                                            + oPanel1.PanelCamera.axis_x()[0] + " " + oPanel1.PanelCamera.axis_x()[1]
                                            + " " + oPanel1.PanelCamera.axis_x()[2] + "] ["
                                            + oPanel1.PanelCamera.axis_y()[0] + " " + oPanel1.PanelCamera.axis_y()[1]
                                            + " " + oPanel1.PanelCamera.axis_y()[2] + "] color "
                                            + oPanel1.getFrontColor() + " filename ["
                                            + fpath + "] export-gif",
                                    OrigamiScriptTerminal.Access.ROOT);
                            oPanel1.update(terminal1.TerminalOrigami);
                            pPanel1.update(terminal1.TerminalOrigami);
                            defaultify();
                            rotation_angle = 0;
                        }
                        catch (Exception ex) {

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
                        javax.swing.JOptionPane.showMessageDialog(OrigamiEditorUI.this,
                                Dictionary.getString("message.info.export_finished"), "Message",
                                javax.swing.JOptionPane.PLAIN_MESSAGE);
                    }
                }.execute();

                if (unreportedException[0] != null) {
                    throw unreportedException[0];
                }
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_export_togif_foldingActionPerformed

    //
    // PNG EXPORT
    //
    private void ui_file_export_creaseActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_export_creaseActionPerformed

        String fpath = dialogManager1.getSaveFilePath("png");
        
        if (fpath != null) {
            
            try {
                terminal1.execute("filename [" + fpath + "] export-png",
                        OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                defaultify();
                rotation_angle = 0;
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_export_creaseActionPerformed

    private void ui_panelsComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_ui_panelsComponentResized
        ui_panels.setDividerLocation(0.5);
    }// GEN-LAST:event_ui_panelsComponentResized

    private void ui_toolbarsComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_ui_toolbarsComponentResized
        ui_toolbars.setDividerLocation(0.5);
    }// GEN-LAST:event_ui_toolbarsComponentResized

    //
    // SELECT
    //
    private void ui_selectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_selectActionPerformed

        ui_select.setSelected(true);
        ui_plane.setSelected(false);
        ui_angle.setSelected(false);
        targetOn = true;
        pPanel1.setRulerMode(neusisOn ? BasicEditing.RulerMode.Neusis : BasicEditing.RulerMode.Normal);
        pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
        oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
        oPanel1.repaint();
        pPanel1.repaint();
    }// GEN-LAST:event_ui_selectActionPerformed

    //
    // PLANE THROUGH 3 POINTS / 3 PONTOS ILLESZTÉS
    //
    private void ui_planeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_planeActionPerformed

        oPanel1.rulerOff();
        pPanel1.rulerOff();
        oPanel1.hideProtractor();
        rotation_angle = 0;
        oPanel1.resetTriangle();
        pPanel1.resetTriangle();
        oPanel1.repaint();
        pPanel1.repaint();

        EditorState = ControlState.TRI0;
        SecondaryState = ControlState.PLANETHRU;
        pPanel1.setRulerMode(BasicEditing.RulerMode.Planethrough);
        pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points1"));
        oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.3points"));

        ui_select.setSelected(false);
        ui_plane.setSelected(true);
        ui_angle.setSelected(false);
        targetOn = false;
    }// GEN-LAST:event_ui_planeActionPerformed

    //
    // ANGLE BISECTOR / SZÖGFELEZŐ
    //
    private void ui_angleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_angleActionPerformed

        oPanel1.rulerOff();
        pPanel1.rulerOff();
        oPanel1.hideProtractor();
        rotation_angle = 0;
        oPanel1.resetTriangle();
        pPanel1.resetTriangle();
        oPanel1.repaint();
        pPanel1.repaint();

        EditorState = ControlState.TRI0;
        SecondaryState = ControlState.ANGLE_BISECT;
        pPanel1.setRulerMode(BasicEditing.RulerMode.Angle_bisector);
        pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points1"));
        oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.3points"));

        ui_select.setSelected(false);
        ui_plane.setSelected(false);
        ui_angle.setSelected(true);
        targetOn = false;
    }// GEN-LAST:event_ui_angleActionPerformed

    private void ui_snap_1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_snap_1ActionPerformed

        if (ui_snap_1.isSelected()) {

            alignOn = true;
            ui_snap_2.setSelected(snap2 == 2);
            ui_snap_3.setSelected(snap3 == 3);
            ui_snap_4.setSelected(snap4 == 4);
        }
        else {

            alignOn = false;
            ui_snap_2.setSelected(false);
            ui_snap_3.setSelected(false);
            ui_snap_4.setSelected(false);
        }
    }// GEN-LAST:event_ui_snap_1ActionPerformed

    private void ui_snap_2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_snap_2ActionPerformed

        if (alignOn) {
            snap2 = ui_snap_2.isSelected() ? 2 : 1;
        }
        else {
            ui_snap_2.setSelected(false);
        }
    }// GEN-LAST:event_ui_snap_2ActionPerformed

    private void ui_snap_3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_snap_3ActionPerformed

        if (alignOn) {
            snap3 = ui_snap_3.isSelected() ? 3 : 1;
        }
        else {
            ui_snap_3.setSelected(false);
        }
    }// GEN-LAST:event_ui_snap_3ActionPerformed

    private void ui_snap_4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_snap_4ActionPerformed

        if (alignOn) {
            snap4 = ui_snap_4.isSelected() ? 4 : 1;
        }
        else {
            ui_snap_4.setSelected(false);
        }
    }// GEN-LAST:event_ui_snap_4ActionPerformed

    //
    // PROPERTIES
    //
    @SuppressWarnings("deprecation")
    private void ui_file_propertiesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_propertiesActionPerformed

        final javax.swing.JDialog properties = new javax.swing.JDialog(this, Dictionary.getString("properties"));
        properties.getContentPane().setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.NORTH;
        properties.getContentPane().add(new javax.swing.JLabel(Dictionary.getString("origami-version")), c);
        c.gridy = 1;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridy = 2;
        properties.getContentPane().add(new javax.swing.JLabel(Dictionary.getString("origami-papertype")), c);
        c.gridy = 3;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridy = 4;
        properties.getContentPane().add(new javax.swing.JLabel(Dictionary.getString("origami-steps")), c);
        c.gridy = 5;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridy = 6;
        properties.getContentPane().add(new javax.swing.JLabel(Dictionary.getString("origami-difficulty")), c);
        c.gridy = 7;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridx = 1;
        c.gridy = 0;
        properties.getContentPane().add(new javax.swing.JLabel("Generation " + terminal1.TerminalOrigami.generation()),
                c);
        c.gridy = 1;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridy = 2;
        properties.getContentPane()
                .add(new javax.swing.JLabel(Dictionary.getString(terminal1.TerminalOrigami.papertype().toString())), c);
        c.gridy = 3;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridy = 4;
        properties.getContentPane()
                .add(new javax.swing.JLabel(Integer.toString(terminal1.TerminalOrigami.history().size())), c);
        c.gridy = 5;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);
        c.gridy = 6;
        final javax.swing.JLabel diflabel = new javax.swing.JLabel(Dictionary.getString("calculating..."));
        properties.getContentPane().add(diflabel, c);
        c.gridy = 7;
        properties.getContentPane().add(new javax.swing.JLabel(" "), c);

        properties.setResizable(false);
        properties.pack();
        properties.setLocationRelativeTo(this);

        final Thread difth = new Thread(new Runnable() {

            @Override
            public void run() {

                int dif = Origami.difficultyLevel(terminal1.TerminalOrigami.difficulty());
                String difname = null;
                switch (dif) {
                    case 0:
                        difname = Dictionary.getString("level0");
                        break;
                    case 1:
                        difname = Dictionary.getString("level1");
                        break;
                    case 2:
                        difname = Dictionary.getString("level2");
                        break;
                    case 3:
                        difname = Dictionary.getString("level3");
                        break;
                    case 4:
                        difname = Dictionary.getString("level4");
                        break;
                    case 5:
                        difname = Dictionary.getString("level5");
                        break;
                    case 6:
                        difname = Dictionary.getString("level6");
                        break;
                }
                diflabel.setText(Dictionary.getString("level", dif, difname));
                properties.pack();
            }
        });

        properties.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {

                difth.stop();
                super.windowClosed(evt);
            }
        });

        properties.setVisible(true);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                difth.start();
            }
        });
    }// GEN-LAST:event_ui_file_propertiesActionPerformed

    //
    // JAR EXPORT
    //
    private void ui_file_export_toselfActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ui_file_export_toselfActionPerformed

        String fpath = dialogManager1.getSaveFilePath("ori.jar");
        
        if (fpath != null) {
            
            try {
                if (save_paper_color) {
                    terminal1.execute("color " + String.valueOf(oPanel1.getFrontColor()));
                }
                else {
                    terminal1.execute("uncolor");
                }
                terminal1.execute("filename [" + fpath + "] export-jar",
                        OrigamiScriptTerminal.Access.ROOT);
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                defaultify();
                rotation_angle = 0;
            }
            catch (Exception ex) {
                oPanel1.update(terminal1.TerminalOrigami);
                pPanel1.update(terminal1.TerminalOrigami);
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_ui_file_export_toselfActionPerformed

    private boolean snap1(int radius) {

        int v1ujX = -1;
        int v1ujY = -1;

        for (int[] osztohely : oPanel1.PanelCamera.alignmentPoints(terminal1.TerminalOrigami, snap2, snap3, snap4)) {

            if ((ruler1X - oPanel1.PanelCamera.xshift - osztohely[0])
                    * (ruler1X - oPanel1.PanelCamera.xshift - osztohely[0])
                    + (ruler1Y - oPanel1.PanelCamera.yshift - osztohely[1])
                            * (ruler1Y - oPanel1.PanelCamera.yshift - osztohely[1]) < radius) {

                v1ujX = osztohely[0] + oPanel1.PanelCamera.xshift;
                v1ujY = osztohely[1] + oPanel1.PanelCamera.yshift;
                break;
            }
        }
        if (v1ujX != -1) {
            ruler1X = v1ujX;
            ruler1Y = v1ujY;
            return true;
        }
        return false;
    }

    private boolean snap2(int sugar) {

        int v2ujX = -1;
        int v2ujY = -1;

        for (int[] osztohely : oPanel1.PanelCamera.alignmentPoints(terminal1.TerminalOrigami, snap2, snap3, snap4)) {

            if ((ruler2X - oPanel1.PanelCamera.xshift - osztohely[0])
                    * (ruler2X - oPanel1.PanelCamera.xshift - osztohely[0])
                    + (ruler2Y - oPanel1.PanelCamera.yshift - osztohely[1])
                            * (ruler2Y - oPanel1.PanelCamera.yshift - osztohely[1]) < sugar) {

                v2ujX = osztohely[0] + oPanel1.PanelCamera.xshift;
                v2ujY = osztohely[1] + oPanel1.PanelCamera.yshift;
                break;
            }
        }
        if (v2ujX != -1) {
            ruler2X = v2ujX;
            ruler2Y = v2ujY;
            return true;
        }
        return false;
    }

    private int[] flatSnap(int x, int y, int radius) {

        int ujX = -1;
        int ujY = -1;

        for (int[] osztohely : pPanel1.PanelCamera.alignmentPoints2d(terminal1.TerminalOrigami)) {

            if ((x - pPanel1.PanelCamera.xshift - osztohely[0]) * (x - pPanel1.PanelCamera.xshift - osztohely[0])
                    + (y - pPanel1.PanelCamera.yshift - osztohely[1])
                            * (y - pPanel1.PanelCamera.yshift - osztohely[1]) < radius) {

                ujX = osztohely[0] + pPanel1.PanelCamera.xshift;
                ujY = osztohely[1] + pPanel1.PanelCamera.yshift;
                break;
            }
        }
        if (ujX != -1) {
            return new int[] { ujX, ujY };
        }
        return new int[] { x, y };
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea terminal_log;
    private javax.swing.JTextField jTextField1;
    private origamieditor3d.OrigamiPanel oPanel1;
    private origamieditor3d.PaperPanel pPanel1;
    private javax.swing.JToggleButton ui_angle;
    private javax.swing.JMenu ui_edit;
    private javax.swing.JMenuItem ui_edit_angle;
    private javax.swing.JCheckBoxMenuItem ui_edit_neusis;
    private javax.swing.JMenuItem ui_edit_plane;
    private javax.swing.JMenuItem ui_edit_redo;
    private javax.swing.JMenuItem ui_edit_undo;
    private javax.swing.JSplitPane ui_editor;
    private javax.swing.JMenu ui_file;
    private javax.swing.JMenu ui_file_export;
    private javax.swing.JMenuItem ui_file_export_crease;
    private javax.swing.JMenu ui_file_export_togif;
    private javax.swing.JMenuItem ui_file_export_togif_folding;
    private javax.swing.JMenuItem ui_file_export_togif_revolving;
    private javax.swing.JMenuItem ui_file_export_toopenctm;
    private javax.swing.JMenuItem ui_file_export_topdf;
    private javax.swing.JMenuItem ui_file_export_toself;
    private javax.swing.JMenu ui_file_new;
    private javax.swing.JMenuItem ui_file_new_a4;
    private javax.swing.JMenu ui_file_new_bases;
    private javax.swing.JMenuItem ui_file_new_dollar;
    private javax.swing.JMenuItem ui_file_new_hexagonal;
    private javax.swing.JMenuItem ui_file_new_square;
    private javax.swing.JMenuItem ui_file_open;
    private javax.swing.JMenuItem ui_file_properties;
    private javax.swing.JMenu ui_file_example;
    private javax.swing.JMenuItem ui_file_save;
    private javax.swing.JMenuItem ui_file_saveas;
    private javax.swing.JMenu ui_help;
    private javax.swing.JMenuItem ui_help_user;
    private javax.swing.JMenuItem ui_help_about;
    private javax.swing.JMenu ui_help_lang;
    private javax.swing.JCheckBoxMenuItem ui_help_show;
    private javax.swing.JToolBar ui_leftbar;
    private javax.swing.JSplitPane ui_panels;
    private javax.swing.JToggleButton ui_plane;
    private javax.swing.JToolBar ui_rightbar;
    private javax.swing.JToggleButton ui_select;
    private javax.swing.JToggleButton ui_snap_1;
    private javax.swing.JToggleButton ui_snap_2;
    private javax.swing.JToggleButton ui_snap_3;
    private javax.swing.JToggleButton ui_snap_4;
    private javax.swing.JLabel ui_snap;
    private javax.swing.JToolBar.Separator ui_snap_separator;
    private javax.swing.JSplitPane ui_editor_notimeline;
    private javax.swing.JSplitPane ui_toolbars;
    private javax.swing.JPanel ui_timeline;
    private javax.swing.JLabel ui_timeline_label;
    private javax.swing.JSlider timeSlider;
    private javax.swing.JMenu ui_view;
    private javax.swing.JMenu ui_view_paper;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_gradient;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_image;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_none;
    private javax.swing.JCheckBoxMenuItem ui_view_paper_plain;
    private javax.swing.JCheckBoxMenuItem ui_view_use;
    private javax.swing.JCheckBoxMenuItem ui_view_show;
    private javax.swing.JCheckBoxMenuItem ui_view_zoom;
    private javax.swing.JCheckBoxMenuItem ui_view_best;
    private javax.swing.JCheckBoxMenuItem ui_view_timeline;
    private javax.swing.JMenuItem ui_view_options;
    private JSeparator separator;
    private JSeparator separator_1;
    // End of variables declaration//GEN-END:variables

    private void defaultify() {

        EditorState = (SecondaryState = ControlState.STANDBY);
        targetOn = true;
        ui_select.setSelected(true);
        ui_plane.setSelected(false);
        ui_angle.setSelected(false);
        oPanel1.rulerOff();
        pPanel1.rulerOff();
        oPanel1.reset();
        pPanel1.reset();
        oPanel1.repaint();
        pPanel1.repaint();
        oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
        pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
    }
}
