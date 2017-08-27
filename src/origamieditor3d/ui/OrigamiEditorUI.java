package origamieditor3d.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import origamieditor3d.graphics.Camera;
import origamieditor3d.io.OrigamiIO;
import origamieditor3d.origami.OrigamiGen1;
import origamieditor3d.resources.BaseModels;
import origamieditor3d.resources.Constants;
import origamieditor3d.resources.Dictionary;
import origamieditor3d.resources.ExampleModels;
import origamieditor3d.resources.Instructor;
import origamieditor3d.script.OrigamiScriptTerminal;
import origamieditor3d.script.OrigamiScriptTerminalV1;
import origamieditor3d.script.OrigamiScripter;
import origamieditor3d.ui.panel.OrigamiPanel;
import origamieditor3d.ui.panel.Panel;
import origamieditor3d.ui.panel.PaperPanel;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiEditorUI extends JFrame {

	final static private long serialVersionUID = 1L;

	final private OrigamiScriptTerminalV1 terminal1;
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
	private BufferedImage tex;

	private boolean saved;
	private boolean changeListenerShutUp;

	private int snap2, snap3, snap4;
	private boolean targetOn;

	private JFrame ui_options;
	private JPopupMenu ui_foldingops;

	private enum ControlState {

		STANDBY, RULER1, RULER2, RULER_ROT, TRI0, TRI1, TRI2, TRI3, TRI_ROT, PLANETHRU, ANGLE_BISECT
	}

	public OrigamiEditorUI() {

		setIconImage(
				Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("res/icon.png")));

		// Initialize terminal and dialog manager
		terminal1 = new OrigamiScriptTerminalV1(OrigamiScriptTerminal.AccessMode.USER);
		dialogManager1 = new DialogManager(this);

		// Look for an update
		dialogManager1.lookForUpdate();

		// Initialize UI
		initComponents();
		initViewOptionsWindow();
		initFoldingOptionsMenu();
		relabel();
		Instructor.getString("asdasd");
		setTitle("Origami Editor 3D");
		setLocationRelativeTo(null);

		// Redirect standard output to the terminal log
		OutputStream sysout = new OutputStream() {

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			@Override
			public void write(int ch) {

				bytes.write(ch);
				if (ch == 10) {

					terminal_log.append(bytes.toString());
					bytes.reset();
				}
			}
		};
		System.setOut(new PrintStream(sysout));

		// Load base model entries into the menu
		final BaseModels bases = new BaseModels();
		final ArrayList<String> basenames = bases.names();
		for (int i = 0; i < basenames.size(); i++) {

			final int ind = i;
			final JMenuItem baseitem = new JMenuItem(Dictionary.getString(basenames.get(i)));
			baseitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {

					if (!saved) {
						if (!dialogManager1.canICloseFile()) {
							return;
						}
					}
					filepath = null;
					try (java.io.InputStream fis = bases.getFile(basenames.get(ind))) {

						ArrayList<Byte> bytesb = new ArrayList<>();
						int fisbyte;
						while ((fisbyte = fis.read()) != -1) {
							bytesb.add((byte) fisbyte);
						}
						byte[] bytes = new byte[bytesb.size()];
						for (int i = 0; i < bytesb.size(); i++) {
							bytes[i] = bytesb.get(i);
						}

						terminal1.TerminalOrigami = OrigamiIO.read_gen2(new ByteArrayInputStream(bytes), null);
						terminal1.clearHistory();

						oPanel1.update(terminal1.TerminalOrigami);
						pPanel1.update(terminal1.TerminalOrigami);

						oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
						oPanel1.resetZoom();
						pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
						pPanel1.resetZoom();
						if (alwaysInMiddle) {
							oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
						}
						oPanel1.randomizeFrontColor();
						oPanel1.panelCamera().setOrthogonalView(0);
						rotation_angle = 0;
						defaultify();
						saved = true;
						setTitle("Origami Editor 3D");
					}
					catch (Exception ex) {
						JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("h005"));
					}
					foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
					changeListenerShutUp = true;
					timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
					changeListenerShutUp = false;
					timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
				}
			});
			ui_file_new_bases.add(baseitem);
		}

		// Load example model entries into the menu
		ui_file_example.getPopupMenu().setLayout(new GridLayout(0, 2));
		final ExampleModels examples = new ExampleModels();
		final ArrayList<String> modnames = examples.names();
		for (int i = 0; i < modnames.size(); i++) {
			final int ind = i;
			final JMenuItem modelitem = new JMenuItem(Dictionary.getString(modnames.get(i)));
			modelitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {

					if (!saved) {
						if (!dialogManager1.canICloseFile()) {
							return;
						}
					}
					filepath = null;
					try (java.io.InputStream fis = examples.getFile(modnames.get(ind))) {

						ArrayList<Byte> bytesb = new ArrayList<>();
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

						terminal1.TerminalOrigami = OrigamiIO.read_gen2(new ByteArrayInputStream(bytes), rgb);
						terminal1.clearHistory();

						oPanel1.setFrontColor(rgb[0] * 0x10000 + rgb[1] * 0x100 + rgb[2]);

						oPanel1.update(terminal1.TerminalOrigami);
						pPanel1.update(terminal1.TerminalOrigami);

						oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
						oPanel1.resetZoom();
						pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
						pPanel1.resetZoom();
						if (alwaysInMiddle) {
							oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
						}
						oPanel1.panelCamera().setOrthogonalView(0);
						rotation_angle = 0;
						defaultify();
						saved = true;
						setTitle("Origami Editor 3D");
					}
					catch (Exception ex) {
						JOptionPane.showMessageDialog(OrigamiEditorUI.this, Dictionary.getString("h005"));
					}
					foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
					changeListenerShutUp = true;
					timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
					changeListenerShutUp = false;
					timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
				}
			});
			ui_file_example.add(modelitem);
		}

		// Confirmation dialog on closing
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
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
		oPanel1.panelCamera().setXShift(oPanel1.getWidth() / 2);
		oPanel1.panelCamera().setYShift(oPanel1.getHeight() / 2);
		pPanel1.panelCamera().setXShift(pPanel1.getWidth() / 2);
		pPanel1.panelCamera().setYShift(pPanel1.getHeight() / 2);
		try {
			terminal1.executeWithTimeout("version 1");
			terminal1.executeWithTimeout(OrigamiScripter.paper("square"));
			terminal1.executeWithTimeout(OrigamiScripter._new());
		}
		catch (Exception exc) {
		}

		oPanel1.update(terminal1.TerminalOrigami);
		pPanel1.update(terminal1.TerminalOrigami);

		oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
		oPanel1.resetZoom();
		pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
		pPanel1.resetZoom();

		ui_panels.setDividerLocation(0.5);
		ui_panels.setResizeWeight(0.5);
		oPanel1.repaint();
		pPanel1.repaint();

		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(1000);
		oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
		pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));

		oPanel1.setMinimumSize(new Dimension(100, 100));
		pPanel1.setMinimumSize(new Dimension(100, 100));
		this.setMinimumSize(new Dimension(200, 200));

		filepath = null;
		tex = null;
		save_paper_color = true;

		// Load language entries into menu
		final ResourceBundle locales = ResourceBundle.getBundle("locales");
		Set<String> locnames = locales.keySet();
		for (final String locname : locnames) {

			final JMenuItem locitem = new JMenuItem(locname);
			locitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						terminal1.executeWithTimeout(locales.getString(locname));
						relabel();
					}
					catch (Exception ex) {
					}
				}
			});
			ui_help_lang.add(locitem);
		}

		// Disable tooltips
		ToolTipManager.sharedInstance().setEnabled(false);
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

		oPanel1.setBorder(BorderFactory.createTitledBorder(Dictionary.getString("ui.3dview")));
		pPanel1.setBorder(BorderFactory.createTitledBorder(Dictionary.getString("ui.crease")));
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

		jTabbedPane1 = new JTabbedPane();
		ui_editor = new JSplitPane();
		ui_panels = new JSplitPane();
		oPanel1 = new OrigamiPanel();
		pPanel1 = new PaperPanel();
		ui_editor_notimeline = new JSplitPane();
		ui_toolbars = new JSplitPane();
		ui_rightbar = new JToolBar();
		ui_select = new JToggleButton();
		ui_plane = new JToggleButton();
		ui_angle = new JToggleButton();
		ui_leftbar = new JToolBar();
		ui_snap = new JLabel();
		ui_snap_separator = new Separator();
		ui_snap_1 = new JToggleButton();
		ui_snap_2 = new JToggleButton();
		ui_snap_3 = new JToggleButton();
		ui_snap_4 = new JToggleButton();
		jSplitPane2 = new JSplitPane();
		jScrollPane3 = new JScrollPane();
		terminal_log = new JTextArea();
		jTextField1 = new JTextField();
		jMenuBar1 = new JMenuBar();
		ui_file = new JMenu();
		ui_file_new = new JMenu();
		ui_file_new_square = new JMenuItem();
		ui_file_new_a4 = new JMenuItem();
		ui_file_new_hexagonal = new JMenuItem();
		ui_file_new_dollar = new JMenuItem();
		ui_file_new_bases = new JMenu();
		ui_file_example = new JMenu();
		jSeparator1 = new javax.swing.JPopupMenu.Separator();
		ui_file_open = new JMenuItem();
		ui_file_save = new JMenuItem();
		ui_file_saveas = new JMenuItem();
		ui_file_export = new JMenu();
		ui_file_export_topdf = new JMenuItem();
		ui_file_export_toopenctm = new JMenuItem();
		ui_file_export_togif = new JMenu();
		ui_file_export_togif_revolving = new JMenuItem();
		ui_file_export_togif_folding = new JMenuItem();
		ui_file_export_toself = new JMenuItem();
		ui_file_export_crease = new JMenuItem();
		jSeparator6 = new javax.swing.JPopupMenu.Separator();
		ui_file_properties = new JMenuItem();
		ui_edit = new JMenu();
		ui_edit_undo = new JMenuItem();
		ui_edit_redo = new JMenuItem();
		jSeparator2 = new javax.swing.JPopupMenu.Separator();
		ui_edit_plane = new JMenuItem();
		ui_edit_angle = new JMenuItem();
		jSeparator3 = new javax.swing.JPopupMenu.Separator();
		ui_edit_neusis = new JCheckBoxMenuItem();
		ui_view = new JMenu();
		ui_view_paper = new JMenu();
		ui_view_paper_image = new JCheckBoxMenuItem();
		ui_view_paper_gradient = new JCheckBoxMenuItem();
		ui_view_paper_plain = new JCheckBoxMenuItem();
		ui_view_paper_none = new JCheckBoxMenuItem();
		ui_view_use = new JCheckBoxMenuItem();
		ui_view_show = new JCheckBoxMenuItem();
		jSeparator5 = new javax.swing.JPopupMenu.Separator();
		ui_view_zoom = new JCheckBoxMenuItem();
		ui_view_best = new JCheckBoxMenuItem();
		jSeparator4 = new javax.swing.JPopupMenu.Separator();
		ui_help_show = new JCheckBoxMenuItem();
		ui_view_timeline = new JCheckBoxMenuItem();
		ui_view_options = new JMenuItem();
		ui_help = new JMenu();
		ui_help_user = new JMenuItem();
		ui_help_lang = new JMenu();
		ui_help_about = new JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new GridLayout(1, 0));

		ui_editor.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		ui_editor.setDividerSize(0);
		ui_editor.setResizeWeight(1.0);
		ui_editor.setEnabled(false);
		ui_editor.setPreferredSize(new Dimension(802, 459));
		ui_editor.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
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
		ui_panels.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				ui_panelsComponentResized(evt);
			}
		});

		oPanel1.setBackground(new Color(255, 255, 255));
		oPanel1.setBorder(BorderFactory.createTitledBorder("3D View"));
		oPanel1.setPreferredSize(new Dimension(400, 400));
		oPanel1.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent evt) {
				oPanel1MouseMoved(evt);
			}

			@Override
			public void mouseDragged(MouseEvent evt) {
				oPanel1MouseDragged(evt);
			}
		});
		oPanel1.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent evt) {
				oPanel1MouseWheelMoved(evt);
			}
		});
		oPanel1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				oPanel1MousePressed(evt);
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				oPanel1MouseClicked(evt);
			}
		});
		oPanel1.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				oPanel1ComponentResized(evt);
			}
		});
		ui_panels.setLeftComponent(oPanel1);

		pPanel1.setBackground(java.awt.Color.white);
		pPanel1.setBorder(BorderFactory.createTitledBorder("Crease Pattern"));
		pPanel1.setPreferredSize(new Dimension(400, 400));
		pPanel1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				pPanel1MouseClicked(evt);
			}
		});
		pPanel1.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				pPanel1ComponentResized(evt);
			}
		});
		ui_panels.setRightComponent(pPanel1);

		ui_editor_notimeline.setTopComponent(ui_panels);

		ui_toolbars.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
		ui_toolbars.setDividerLocation(0.5);
		ui_toolbars.setDividerSize(0);
		ui_toolbars.setResizeWeight(0.5);
		ui_toolbars.setMinimumSize(new Dimension(142, 35));
		ui_toolbars.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				ui_toolbarsComponentResized(evt);
			}
		});

		// initialize right toolbar
		ui_rightbar.setBorder(BorderFactory.createEtchedBorder());
		ui_rightbar.setFloatable(false);
		ui_rightbar.setEnabled(false);
		ui_rightbar.setMaximumSize(new Dimension(32767, 30));
		ui_rightbar.setMinimumSize(new Dimension(30, 27));
		ui_rightbar.setPreferredSize(new Dimension(800, 30));
		ui_rightbar.setLayout(new GridLayout(1, 3));

		ui_select.setIcon(new ImageIcon(getClass().getResource("/res/target.png"))); // NOI18N
		ui_select.setSelected(true);
		targetOn = true;
		ui_select.setText("Select");
		ui_select.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		ui_select.setFocusable(false);
		ui_select.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		ui_select.setIconTextGap(2);
		ui_select.setMaximumSize(new Dimension(69, 33));
		ui_select.setMinimumSize(new Dimension(69, 33));
		ui_select.setPreferredSize(new Dimension(69, 33));
		ui_select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_selectActionPerformed(evt);
			}
		});
		ui_rightbar.add(ui_select);

		ui_plane.setIcon(new ImageIcon(getClass().getResource("/res/planethrough.png"))); // NOI18N
		ui_plane.setSelected(false);
		ui_plane.setText("Through 3");
		ui_plane.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		ui_plane.setFocusable(false);
		ui_plane.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		ui_plane.setIconTextGap(2);
		ui_plane.setMaximumSize(new Dimension(126, 33));
		ui_plane.setMinimumSize(new Dimension(126, 33));
		ui_plane.setPreferredSize(new Dimension(126, 33));
		ui_plane.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_planeActionPerformed(evt);
			}
		});
		ui_rightbar.add(ui_plane);

		ui_angle.setIcon(new ImageIcon(getClass().getResource("/res/angle-bisector.png"))); // NOI18N
		ui_angle.setSelected(false);
		ui_angle.setText("Angle bisector");
		ui_angle.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		ui_angle.setFocusable(false);
		ui_angle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		ui_angle.setIconTextGap(2);
		ui_angle.setMaximumSize(new Dimension(133, 33));
		ui_angle.setMinimumSize(new Dimension(133, 33));
		ui_angle.setPreferredSize(new Dimension(133, 33));
		ui_angle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_angleActionPerformed(evt);
			}
		});
		ui_rightbar.add(ui_angle);

		ui_toolbars.setRightComponent(ui_rightbar);

		// initialize snap options
		ui_leftbar.setBorder(BorderFactory.createEtchedBorder());
		ui_leftbar.setFloatable(false);
		ui_leftbar.setRollover(true);
		ui_leftbar.setEnabled(false);
		ui_leftbar.setMaximumSize(new Dimension(175, 30));
		ui_leftbar.setMinimumSize(new Dimension(175, 30));
		ui_leftbar.setPreferredSize(new Dimension(175, 33));

		ui_snap.setText("Snap fineness");
		ui_leftbar.add(ui_snap);

		ui_snap_separator.setForeground(new Color(238, 238, 238));
		ui_snap_separator.setToolTipText("");
		ui_snap_separator.setEnabled(false);
		ui_snap_separator.setSeparatorSize(new Dimension(20, 0));
		ui_leftbar.add(ui_snap_separator);

		ui_snap_1.setSelected(true);
		ui_snap_1.setText("1");
		ui_snap_1.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		ui_snap_1.setFocusable(false);
		ui_snap_1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		ui_snap_1.setMaximumSize(new Dimension(33, 33));
		ui_snap_1.setMinimumSize(new Dimension(33, 33));
		ui_snap_1.setPreferredSize(new Dimension(33, 33));
		ui_snap_1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		ui_snap_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_snap_1ActionPerformed(evt);
			}
		});
		ui_leftbar.add(ui_snap_1);

		ui_snap_2.setSelected(true);
		ui_snap_2.setText("2");
		ui_snap_2.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		ui_snap_2.setFocusable(false);
		ui_snap_2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		ui_snap_2.setMaximumSize(new Dimension(33, 33));
		ui_snap_2.setMinimumSize(new Dimension(33, 33));
		ui_snap_2.setPreferredSize(new Dimension(33, 33));
		ui_snap_2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		ui_snap_2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_snap_2ActionPerformed(evt);
			}
		});
		ui_leftbar.add(ui_snap_2);

		ui_snap_3.setText("3");
		ui_snap_3.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		ui_snap_3.setFocusable(false);
		ui_snap_3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		ui_snap_3.setMaximumSize(new Dimension(33, 33));
		ui_snap_3.setMinimumSize(new Dimension(33, 33));
		ui_snap_3.setPreferredSize(new Dimension(33, 33));
		ui_snap_3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		ui_snap_3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_snap_3ActionPerformed(evt);
			}
		});
		ui_leftbar.add(ui_snap_3);

		ui_snap_4.setText("4");
		ui_snap_4.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		ui_snap_4.setFocusable(false);
		ui_snap_4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		ui_snap_4.setMaximumSize(new Dimension(33, 33));
		ui_snap_4.setMinimumSize(new Dimension(33, 33));
		ui_snap_4.setPreferredSize(new Dimension(33, 33));
		ui_snap_4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		ui_snap_4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
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
		ui_timeline = new JPanel();
		ui_timeline.setLayout(new BorderLayout());
		ui_timeline.setMinimumSize(new Dimension(142, 30));
		ui_timeline.setBorder(BorderFactory.createEtchedBorder());
		ui_timeline_label = new JLabel("Timeline: ");
		ui_timeline.add(ui_timeline_label, java.awt.BorderLayout.WEST);

		timeSlider = new JSlider();
		timeSlider.setMinimum(0);
		timeSlider.setMaximum(0);
		timeSlider.setValue(0);
		foldNumber = 0;
		timeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
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
		terminal_log.setForeground(new Color(102, 102, 102));
		terminal_log.setRows(5);
		terminal_log.setDisabledTextColor(new Color(102, 102, 102));
		jScrollPane3.setViewportView(terminal_log);

		jSplitPane2.setTopComponent(jScrollPane3);

		jTextField1.setMaximumSize(new Dimension(2147483647, 20));
		jTextField1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				jTextField1KeyTyped(evt);
			}
		});
		jSplitPane2.setRightComponent(jTextField1);

		jTabbedPane1.addTab("Scripting", jSplitPane2);

		getContentPane().add(jTabbedPane1);

		ui_file.setText("File");

		ui_file_new.setIcon(UIManager.getIcon("FileView.fileIcon"));
		ui_file_new.setText("New");

		ui_file_new_square.setText("Square origami");
		ui_file_new_square.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_new_squareActionPerformed(evt);
			}
		});
		ui_file_new.add(ui_file_new_square);

		ui_file_new_a4.setText("A4 origami");
		ui_file_new_a4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_new_a4ActionPerformed(evt);
			}
		});
		ui_file_new.add(ui_file_new_a4);

		ui_file_new_hexagonal.setText("Hexagonal origami");
		ui_file_new_hexagonal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_new_hexagonalActionPerformed(evt);
			}
		});
		ui_file_new.add(ui_file_new_hexagonal);

		ui_file_new_dollar.setText("Dollar bill origami");
		ui_file_new_dollar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
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
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
		ui_file_open.setIcon(UIManager.getIcon("FileView.directoryIcon"));
		ui_file_open.setText("Open...");
		ui_file_open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_openActionPerformed(evt);
			}
		});
		ui_file.add(ui_file_open);

		ui_file_save.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
		ui_file_save.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		ui_file_save.setText("Save");
		ui_file_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_saveActionPerformed(evt);
			}
		});
		ui_file.add(ui_file_save);

		ui_file_saveas.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
				java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
		ui_file_saveas.setText("Save As...");
		ui_file_saveas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_saveasActionPerformed(evt);
			}
		});
		ui_file.add(ui_file_saveas);

		ui_file_export.setText("Export");

		ui_file_export_topdf.setText("To PDF...");
		ui_file_export_topdf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_export_topdfActionPerformed(evt);
			}
		});
		ui_file_export.add(ui_file_export_topdf);

		ui_file_export_toopenctm.setText("To OpenCTM 3D File...");
		ui_file_export_toopenctm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_export_toopenctmActionPerformed(evt);
			}
		});
		ui_file_export.add(ui_file_export_toopenctm);

		ui_file_export_togif.setText("To GIF");

		ui_file_export_togif_revolving.setText("Revolving animation...");
		ui_file_export_togif_revolving.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_export_togif_revolvingActionPerformed(evt);
			}
		});
		ui_file_export_togif.add(ui_file_export_togif_revolving);

		ui_file_export_togif_folding.setText("Folding process...");
		ui_file_export_togif_folding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_export_togif_foldingActionPerformed(evt);
			}
		});
		ui_file_export_togif.add(ui_file_export_togif_folding);

		ui_file_export.add(ui_file_export_togif);

		ui_file_export_toself.setText("To self-displaying ORI...");
		ui_file_export_toself.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_export_toselfActionPerformed(evt);
			}
		});
		ui_file_export.add(ui_file_export_toself);

		ui_file_export_crease.setText("Crease pattern to PNG...");
		ui_file_export_crease.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_export_creaseActionPerformed(evt);
			}
		});
		ui_file_export.add(ui_file_export_crease);

		ui_file.add(ui_file_export);
		ui_file.add(jSeparator6);

		ui_file_properties.setText("Properties");
		ui_file_properties.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_file_propertiesActionPerformed(evt);
			}
		});
		ui_file.add(ui_file_properties);

		jMenuBar1.add(ui_file);

		ui_edit.setText("Edit");

		ui_edit_undo.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
		ui_edit_undo.setText("Undo");
		ui_edit_undo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_edit_undoActionPerformed(evt);
			}
		});
		ui_edit.add(ui_edit_undo);

		ui_edit_redo.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
		ui_edit_redo.setText("Redo");
		ui_edit_redo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_edit_redoActionPerformed(evt);
			}
		});
		ui_edit.add(ui_edit_redo);
		ui_edit.add(jSeparator2);

		ui_edit_plane.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
		ui_edit_plane.setText("Plane through 3 points");
		ui_edit_plane.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_edit_planeActionPerformed(evt);
			}
		});
		ui_edit.add(ui_edit_plane);

		ui_edit_angle.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
		ui_edit_angle.setText("Angle bisector");
		ui_edit_angle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_edit_angleActionPerformed(evt);
			}
		});
		ui_edit.add(ui_edit_angle);
		ui_edit.add(jSeparator3);

		ui_edit_neusis.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
		ui_edit_neusis.setText("Neusis Mode");
		ui_edit_neusis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_edit_neusisActionPerformed(evt);
			}
		});
		ui_edit.add(ui_edit_neusis);

		jMenuBar1.add(ui_edit);

		ui_view.setText("View");

		ui_view_paper.setText("Paper texture");

		ui_view_paper_image.setText("Image");
		ui_view_paper_image.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_paper_imageActionPerformed(evt);
			}
		});
		ui_view_paper.add(ui_view_paper_image);

		ui_view_paper_gradient.setSelected(true);
		ui_view_paper_gradient.setText("Gradient");
		ui_view_paper_gradient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_paper_gradientActionPerformed(evt);
			}
		});
		ui_view_paper.add(ui_view_paper_gradient);

		ui_view_paper_plain.setText("Plain");
		ui_view_paper_plain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_paper_plainActionPerformed(evt);
			}
		});
		ui_view_paper.add(ui_view_paper_plain);

		ui_view_paper_none.setText("None");
		ui_view_paper_none.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_paper_noneActionPerformed(evt);
			}
		});
		ui_view_paper.add(ui_view_paper_none);

		ui_view.add(ui_view_paper);

		ui_view_use.setSelected(true);
		ui_view_use.setText("Use anti-aliasing");
		ui_view_use.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_useActionPerformed(evt);
			}
		});
		ui_view.add(ui_view_use);

		ui_view.add(jSeparator5);

		ui_view_show.setAccelerator(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
		ui_view_show.setText("Show preview");
		ui_view_show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_showActionPerformed(evt);
			}
		});
		ui_view.add(ui_view_show);

		ui_view_zoom.setSelected(true);
		ui_view_zoom.setText("Zoom on scroll");
		ui_view_zoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_zoomActionPerformed(evt);
			}
		});
		ui_view.add(ui_view_zoom);

		ui_view_best.setSelected(true);
		ui_view_best.setText("Always in the middle");
		ui_view_best.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_bestActionPerformed(evt);
			}
		});
		ui_view.add(ui_view_best);
		ui_view.add(jSeparator4);

		ui_view_timeline.setSelected(true);
		ui_view_timeline.setText("Timeline");
		ui_view_timeline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_timelineActionPerformed(evt);
			}
		});
		ui_view.add(ui_view_timeline);

		ui_view_options.setText("Options");
		ui_view_options.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_view_optionsActionPerformed(evt);
			}
		});
		ui_view.add(ui_view_options);

		jMenuBar1.add(ui_view);

		ui_help.setText("Help");

		ui_help_user.setText("User Guide");
		ui_help_user.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_help_userActionPerformed(evt);
			}
		});
		ui_help.add(ui_help_user);

		separator = new JSeparator();
		ui_help.add(separator);

		ui_help_lang.setText("Language");
		ui_help.add(ui_help_lang);

		ui_help_show.setText("Show tooltips");
		ui_help_show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_help_showActionPerformed(evt);
			}
		});
		ui_help.add(ui_help_show);

		separator_1 = new JSeparator();
		ui_help.add(separator_1);

		ui_help_about.setText("About");
		ui_help_about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
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
		final JColorChooser paletta = new JColorChooser(
				new Color(oPanel1.getFrontColor()));
		ui_options = new JFrame(Dictionary.getString("ui.view.options"));
		ui_options.setIconImage(getIconImage());
		GridBagConstraints c;
		ui_options.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
		ui_options.getContentPane().setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = java.awt.GridBagConstraints.NORTH;
		ui_options.getContentPane().add(new JLabel(Dictionary.getString("ui.view.options.snap")), c);
		final JSlider snapRadiusSlider = new JSlider();
		snapRadiusSlider.setMinimum(5);
		snapRadiusSlider.setMaximum(20);
		snapRadiusSlider.setValue((int) Math.sqrt(alignment_radius));
		snapRadiusSlider.setLabelTable(snapRadiusSlider.createStandardLabels(15));
		snapRadiusSlider.setPaintLabels(true);
		c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = java.awt.GridBagConstraints.CENTER;
		ui_options.getContentPane().add(snapRadiusSlider, c);
		c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		JLabel cimke2 = new JLabel(Dictionary.getString("ui.view.options.paper"));
		ui_options.getContentPane().add(cimke2, c);
		paletta.setPreviewPanel(new JPanel());
		c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 2;
		ui_options.getContentPane().add(paletta, c);
		final JCheckBox savecolor = new JCheckBox(Dictionary.getString("ui.view.options.save"));
		savecolor.setSelected(true);
		save_paper_color = true;
		savecolor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				save_paper_color = savecolor.isSelected();
			}
		});
		c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 4;
		ui_options.getContentPane().add(savecolor, c);
		final JButton ok = new JButton();
		ok.setText("OK");
		ok.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {

				alignment_radius = snapRadiusSlider.getValue() * snapRadiusSlider.getValue();
				oPanel1.setFrontColor(paletta.getColor().getRGB());
				oPanel1.repaint();
				ui_options.dispose();
			}
		});
		c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		ui_options.getContentPane().add(ok, c);
		ui_options.setMinimumSize(new Dimension(paletta.getMinimumSize().width,
				paletta.getMinimumSize().height + snapRadiusSlider.getMinimumSize().height + ok.getMinimumSize().height
						+ cimke2.getMinimumSize().height + 30));
		ui_options.setResizable(false);
		ui_options.setLocationRelativeTo(null);
		ui_options.pack();
	}

	private void initFoldingOptionsMenu() {

		// initialize folding options popup menu
		ui_foldingops = new JPopupMenu();
		final JMenuItem reflect = new JMenuItem(Dictionary.getString("ui.foldingops.reflect"));
		reflect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_foldingops_reflect_actionPerformed(evt);
			}
		});
		final JMenuItem rotate = new JMenuItem(Dictionary.getString("ui.foldingops.rotate"));
		rotate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_foldingops_rotate_actionPerformed(evt);
			}
		});
		final JMenuItem cut = new JMenuItem(Dictionary.getString("ui.foldingops.cut"));
		cut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ui_foldingops_cut_actionPerformed(evt);
			}
		});
		ui_foldingops.add(reflect);
		ui_foldingops.add(rotate);
		ui_foldingops.add(cut);
	}

	private void oPanel1MousePressed(MouseEvent evt) {// GEN-FIRST:event_oPanel1MousePressed

		mouseDragX = evt.getX();
		mouseDragY = evt.getY();
	}// GEN-LAST:event_oPanel1MousePressed

	//
	// DRAG / HÚZÁS
	//
	private void oPanel1MouseDragged(MouseEvent evt) {// GEN-FIRST:event_oPanel1MouseDragged

		if (EditorState == ControlState.STANDBY || EditorState == ControlState.TRI0 || EditorState == ControlState.TRI1
				|| EditorState == ControlState.TRI2 || EditorState == ControlState.TRI3) {

			oPanel1.resetAlignmentPoint();
			oPanel1.panelCamera().rotate((mouseDragX - evt.getX()) / (float) oPanel1.panelCamera().getZoom() / 2,
					(evt.getY() - mouseDragY) / (float) oPanel1.panelCamera().getZoom() / 2);
			oPanel1.repaint();
			mouseDragX = evt.getX();
			mouseDragY = evt.getY();
		}
	}// GEN-LAST:event_oPanel1MouseDragged

	//
	// RESIZE / ÁTMÉRETEZÉS
	//
	private void oPanel1ComponentResized(ComponentEvent evt) {// GEN-FIRST:event_oPanel1ComponentResized

		oPanel1.resetZoom();
		oPanel1.panelCamera().setXShift(oPanel1.getWidth() / 2);
		oPanel1.panelCamera().setYShift(oPanel1.getHeight() / 2);
	}// GEN-LAST:event_oPanel1ComponentResized

	private void ui_editorComponentResized(ComponentEvent evt) {// GEN-FIRST:event_ui_editorComponentResized

	}// GEN-LAST:event_ui_editorComponentResized

	//
	// TERMINAL
	//
	private void jTextField1KeyTyped(KeyEvent evt) {// GEN-FIRST:event_jTextField1KeyTyped

		if (evt.getKeyChar() == (char) 10) {
			if (!"log".equals(OrigamiScriptTerminalV1.obfuscate(jTextField1.getText()))) {

				try {

					terminal1.executeWithTimeout(jTextField1.getText());

					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);

					oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
					if (alwaysInMiddle) {
						oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
					}
					oPanel1.resetZoom();
					pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
					pPanel1.resetZoom();

					System.out.println(OrigamiScriptTerminalV1.obfuscate(jTextField1.getText()));
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
				for (String sor : terminal1.getHistory()) {
					System.out.println(sor);
				}
			}
			foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
			changeListenerShutUp = true;
			timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
			changeListenerShutUp = false;
			timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
			saved = false;
		}
	}// GEN-LAST:event_jTextField1KeyTyped

	//
	// RESIZE / ÁTMÉRETEZÉS
	//
	private void pPanel1ComponentResized(ComponentEvent evt) {// GEN-FIRST:event_pPanel1ComponentResized

		pPanel1.resetZoom();
		pPanel1.panelCamera().setXShift(pPanel1.getWidth() / 2);
		pPanel1.panelCamera().setYShift(pPanel1.getHeight() / 2);
	}// GEN-LAST:event_pPanel1ComponentResized

	//
	// EDITING (2D) / SZERKESZTÉS (2D)
	//
	private void pPanel1MouseClicked(MouseEvent evt) {// GEN-FIRST:event_pPanel1MouseClicked

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
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), (Integer[]) null);
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
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), (Integer[]) null);
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
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), (Integer[]) null);
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

			pPanel1.setTracker(pPanel1.panelCamera(), evt.getX(), evt.getY());
			oPanel1.setTracker(pPanel1.panelCamera(), evt.getX(), evt.getY());
			pPanel1.repaint();
			oPanel1.repaint();
			pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.unselect"));

		}
		else if (EditorState == ControlState.TRI0) {

			if (alignOn) {
				int[] ig = flatSnap(evt.getX(), evt.getY(), alignment_radius);
				pPanel1.tiltTriangleTo(null, ig[0], ig[1]);
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), ig[0], ig[1]);
			}
			else {
				pPanel1.tiltTriangleTo(null, evt.getX(), evt.getY());
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), evt.getX(), evt.getY());
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
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), ig[0], ig[1]);
			}
			else {
				pPanel1.tiltTriangleTo(null, evt.getX(), evt.getY());
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), evt.getX(), evt.getY());
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
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), ig[0], ig[1]);
			}
			else {
				pPanel1.tiltTriangleTo(null, evt.getX(), evt.getY());
				oPanel1.tiltTriangleTo(pPanel1.panelCamera(), evt.getX(), evt.getY());
			}
			pPanel1.repaint();
			oPanel1.repaint();
			EditorState = ControlState.TRI3;
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_pPanel1MouseClicked

	//
	// EDITING (3D) / SZERKESZTÉS (3D)
	//
	private void oPanel1MouseClicked(MouseEvent evt) {// GEN-FIRST:event_oPanel1MouseClicked

		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {

			if (EditorState == ControlState.STANDBY || EditorState == ControlState.TRI0
					|| EditorState == ControlState.TRI1 || EditorState == ControlState.TRI2
					|| EditorState == ControlState.TRI3) {

				oPanel1.resetZoom();
				oPanel1.panelCamera().nextOrthogonalView();
				oPanel1.repaint();
			}
			else if (EditorState == ControlState.RULER_ROT) {

				try {

					double[] rulerPT = oPanel1.getRulerPoint();
					double[] rulerNV = oPanel1.getRulerNormalvector();
					terminal1.executeWithTimeout(OrigamiScripter.plane(rulerPT, rulerNV));

					if (pPanel1.isTracked()) {

						double[] targ = pPanel1.panelCamera().deprojection((double) pPanel1.tracker_x(),
								(double) pPanel1.tracker_y());

						terminal1.executeWithTimeout(OrigamiScripter.target(targ));
					}
					terminal1.executeWithTimeout(OrigamiScripter.angle(rotation_angle));
					terminal1.executeWithTimeout(OrigamiScripter.rotate());
					oPanel1.update(terminal1.TerminalOrigami);
				}
				catch (Exception ex) {

					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);
					JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
							javax.swing.JOptionPane.ERROR_MESSAGE);
				}
				finally {

					if (alwaysInMiddle) {
						oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
					}
					rotation_angle = 0;
					oPanel1.hideProtractor();
					defaultify();
					saved = false;
				}
			}
			else if (EditorState == ControlState.TRI_ROT) {

				double[] p1 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[0][0],
						(double) pPanel1.linerTriangle()[0][1]);
				double[] p2 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[1][0],
						(double) pPanel1.linerTriangle()[1][1]);
				double[] p3 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[2][0],
						(double) pPanel1.linerTriangle()[2][1]);

				try {
					if (SecondaryState == ControlState.PLANETHRU) {
						terminal1.executeWithTimeout(OrigamiScripter.planethrough(p1, p2, p3));
					}
					if (SecondaryState == ControlState.ANGLE_BISECT) {
						terminal1.executeWithTimeout(OrigamiScripter.angle_bisector(p1, p2, p3));
					}
					if (pPanel1.isTracked()) {

						double[] targ = pPanel1.panelCamera().deprojection((double) pPanel1.tracker_x(),
								(double) pPanel1.tracker_y());

						terminal1.executeWithTimeout(OrigamiScripter.target(targ));
					}
					terminal1.executeWithTimeout(OrigamiScripter.angle(rotation_angle));
					terminal1.executeWithTimeout(OrigamiScripter.rotate());
					oPanel1.update(terminal1.TerminalOrigami);
				}
				catch (Exception ex) {

					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);
					JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
							javax.swing.JOptionPane.ERROR_MESSAGE);
				}
				finally {

					if (alwaysInMiddle) {
						oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
					}
					rotation_angle = 0;
					oPanel1.hideProtractor();
					defaultify();
					saved = false;
				}
			}
			else {
				if (alwaysInMiddle) {
					oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
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
			ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(oPanel1, 0,
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
				oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
			}
			defaultify();
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
		if (alignOn) {
			oPanel1.resetAlignmentPoint();
		}
	}// GEN-LAST:event_oPanel1MouseClicked

	private void ui_foldingops_reflect_actionPerformed(ActionEvent evt) {

		if (EditorState == ControlState.RULER2) {

			try {
				double[] rulerPT = oPanel1.getRulerPoint();
				double[] rulerNV = oPanel1.getRulerNormalvector();
				terminal1.executeWithTimeout(OrigamiScripter.plane(rulerPT, rulerNV));

				if (pPanel1.isTracked()) {

					double[] targ = pPanel1.panelCamera().deprojection((double) pPanel1.tracker_x(),
							(double) pPanel1.tracker_y());

					terminal1.executeWithTimeout(OrigamiScripter.target(targ));
				}
				terminal1.executeWithTimeout(OrigamiScripter.reflect());
				oPanel1.update(terminal1.TerminalOrigami);
			}
			catch (Exception ex) {

				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally {

				if (alwaysInMiddle) {
					oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
				}
				defaultify();
				saved = false;
			}
		}
		else if (EditorState == ControlState.TRI3) {

			double[] p1 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[0][0],
					(double) pPanel1.linerTriangle()[0][1]);
			double[] p2 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[1][0],
					(double) pPanel1.linerTriangle()[1][1]);
			double[] p3 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[2][0],
					(double) pPanel1.linerTriangle()[2][1]);

			try {
				if (SecondaryState == ControlState.PLANETHRU) {
					terminal1.executeWithTimeout(OrigamiScripter.planethrough(p1, p2, p3));
				}
				if (SecondaryState == ControlState.ANGLE_BISECT) {
					terminal1.executeWithTimeout(OrigamiScripter.angle_bisector(p1, p2, p3));
				}
				if (pPanel1.isTracked()) {

					double[] targ = pPanel1.panelCamera().deprojection((double) pPanel1.tracker_x(),
							(double) pPanel1.tracker_y());

					terminal1.executeWithTimeout(OrigamiScripter.target(targ));
				}
				terminal1.executeWithTimeout(OrigamiScripter.reflect());
				oPanel1.update(terminal1.TerminalOrigami);
			}
			catch (Exception ex) {

				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally {

				if (alwaysInMiddle) {
					oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
				}
				defaultify();
				saved = false;
			}
		}

		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
		if (alignOn) {
			oPanel1.resetAlignmentPoint();
		}
	}

	private void ui_foldingops_rotate_actionPerformed(ActionEvent evt) {

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

	private void ui_foldingops_cut_actionPerformed(ActionEvent evt) {

		if (EditorState == ControlState.RULER2) {

			try {
				double[] rulerPT = oPanel1.getRulerPoint();
				double[] rulerNV = oPanel1.getRulerNormalvector();
				terminal1.executeWithTimeout(OrigamiScripter.plane(rulerPT, rulerNV));

				if (pPanel1.isTracked()) {

					double[] targ = pPanel1.panelCamera().deprojection((double) pPanel1.tracker_x(),
							(double) pPanel1.tracker_y());

					terminal1.executeWithTimeout(OrigamiScripter.target(targ));
				}
				terminal1.executeWithTimeout(OrigamiScripter.cut());
				oPanel1.update(terminal1.TerminalOrigami);
			}
			catch (Exception ex) {

				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally {

				if (alwaysInMiddle) {
					oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
				}
				defaultify();
				saved = false;
			}
		}
		else if (EditorState == ControlState.TRI3) {

			double[] p1 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[0][0],
					(double) pPanel1.linerTriangle()[0][1]);
			double[] p2 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[1][0],
					(double) pPanel1.linerTriangle()[1][1]);
			double[] p3 = pPanel1.panelCamera().deprojection((double) pPanel1.linerTriangle()[2][0],
					(double) pPanel1.linerTriangle()[2][1]);

			try {
				if (SecondaryState == ControlState.PLANETHRU) {
					terminal1.executeWithTimeout(OrigamiScripter.planethrough(p1, p2, p3));
				}
				if (SecondaryState == ControlState.ANGLE_BISECT) {
					terminal1.executeWithTimeout(OrigamiScripter.angle_bisector(p1, p2, p3));
				}
				if (pPanel1.isTracked()) {

					double[] targ = pPanel1.panelCamera().deprojection((double) pPanel1.tracker_x(),
							(double) pPanel1.tracker_y());

					terminal1.executeWithTimeout(OrigamiScripter.target(targ));
				}
				terminal1.executeWithTimeout(OrigamiScripter.cut());
				oPanel1.update(terminal1.TerminalOrigami);
			}
			catch (Exception ex) {

				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally {

				if (alwaysInMiddle) {
					oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
				}
				defaultify();
				saved = false;
			}
		}

		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
		if (alignOn) {
			oPanel1.resetAlignmentPoint();
		}
	}

	//
	// MOUSE MOVEMENT OVER 3D VIEW / EGÉRMOZGÁS A 3D NÉZET FELETT
	//
	private void oPanel1MouseMoved(MouseEvent evt) {// GEN-FIRST:event_oPanel1MouseMoved

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
			pPanel1.rulerOn(oPanel1.panelCamera(), ruler1X, ruler1Y, ruler2X, ruler2Y);
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
	private void ui_file_export_topdfActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_export_topdfActionPerformed

		final String fpath = dialogManager1.getSaveFilePath("pdf");
		if (fpath != null) {

			if (!new File(fpath).getName().matches("[\\w\\.]+")) {
				JOptionPane.showMessageDialog(null, Dictionary.getString("noword"),
						Dictionary.getString("warning"), javax.swing.JOptionPane.WARNING_MESSAGE);
			}

			try {

				final JDialog exporting = new JDialog(this);
				exporting.setUndecorated(true);
				JLabel loadmsg = new JLabel(Dictionary.getString("message.info.exporting"));
				loadmsg.setForeground(Color.RED);
				exporting.getContentPane().setLayout(new BorderLayout());
				exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
				exporting.getContentPane().setBackground(Color.YELLOW);
				exporting.pack();
				exporting.setResizable(false);
				exporting.setLocationRelativeTo(null);
				exporting.setVisible(true);

				final Exception[] unreportedException = { null };

				new SwingWorker<Void, Integer>() {

					@Override
					protected Void doInBackground() {

						try {

							terminal1.executeWithTimeout(
									OrigamiScripter.title(new File(fpath).getName().replace(".pdf", "")));
							terminal1.executeWithTimeout(OrigamiScripter.filename(fpath));
							terminal1.executeWithTimeout(OrigamiScripter.export_autopdf(), OrigamiScriptTerminal.AccessMode.USER);

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
						JOptionPane.showMessageDialog(OrigamiEditorUI.this,
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
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_export_topdfActionPerformed

	//
	// UNDO / VISSZAVONÁS
	//
	private void ui_edit_undoActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_edit_undoActionPerformed

		try {
			terminal1.executeWithTimeout(OrigamiScripter.undo());
		}
		catch (Exception exc) {
		}

		oPanel1.update(terminal1.TerminalOrigami);
		pPanel1.update(terminal1.TerminalOrigami);

		oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
		oPanel1.resetZoom();
		pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
		pPanel1.resetZoom();
		if (alwaysInMiddle) {
			oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
		}
		defaultify();
		rotation_angle = 0;
		saved = false;
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_edit_undoActionPerformed

	//
	// SCROLL OVER 3D VIEW / GÖRGETÉS A 3D NÉZET FELETT
	//
	private void oPanel1MouseWheelMoved(MouseWheelEvent evt) {// GEN-FIRST:event_oPanel1MouseWheelMoved

		if (EditorState != ControlState.RULER1 && EditorState != ControlState.RULER2
				&& EditorState != ControlState.RULER_ROT && EditorState != ControlState.TRI_ROT) {

			if (zoomOnScroll && oPanel1.panelCamera().getZoom() - 0.1 * evt.getWheelRotation() <= Camera.maximal_zoom
					&& oPanel1.panelCamera().getZoom() - 0.1 * evt.getWheelRotation() >= 0.8
							* Math.min(oPanel1.getWidth(), oPanel1.getHeight())
							/ terminal1.TerminalOrigami.circumscribedSquareSize()) {
				oPanel1.panelCamera().setZoom(oPanel1.panelCamera().getZoom() - 0.1 * evt.getWheelRotation());
			}
			oPanel1.repaint();
		}

	}// GEN-LAST:event_oPanel1MouseWheelMoved

	//
	// SAVE AS / MENTÉS MÁSKÉNT
	//
	private void ui_file_saveasActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_saveasActionPerformed

		String fpath = dialogManager1.getSaveFilePath("ori");

		if (fpath != null) {

			try {

				if (save_paper_color) {
					terminal1.executeWithTimeout(OrigamiScripter.color(oPanel1.getFrontColor()));
				}
				else {
					terminal1.executeWithTimeout(OrigamiScripter.uncolor());
				}
				terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_ori(),
						OrigamiScriptTerminal.AccessMode.USER);
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				defaultify();
				rotation_angle = 0;
				filepath = fpath;
				setTitle(new File(fpath).getName() + " - Origami Editor 3D");
				saved = true;
			}
			catch (Exception ex) {
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_saveasActionPerformed

	//
	// CTM EXPORT
	//
	private void ui_file_export_toopenctmActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_export_toopenctmActionPerformed

		String fpath = dialogManager1.getSaveFilePath("ctm");

		if (fpath != null) {

			if (!new File(fpath).getName().matches("[\\w\\.]+")) {
				JOptionPane.showMessageDialog(null, Dictionary.getString("noword"),
						Dictionary.getString("warning"), javax.swing.JOptionPane.WARNING_MESSAGE);
			}

			try {
				terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_ctm(),
						OrigamiScriptTerminal.AccessMode.USER);
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				pPanel1.reset();
				defaultify();
				rotation_angle = 0;
			}
			catch (Exception ex) {
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_export_toopenctmActionPerformed

	//
	// OPEN / MEGNYITÁS
	//
	private void ui_file_openActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_openActionPerformed

		if (!saved) {
			if (!dialogManager1.canICloseFile()) {
				return;
			}
		}

		String fpath = dialogManager1.getOpenFilePath("ori", "txt");

		if (fpath != null) {

			if (fpath.endsWith(".ori")) {

				try {
					terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.open(),
							OrigamiScriptTerminal.AccessMode.USER);
					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);

					oPanel1.setFrontColor(terminal1.getPaperColor());
					oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
					oPanel1.resetZoom();
					pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
					pPanel1.resetZoom();
					if (alwaysInMiddle) {
						oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
					}
					oPanel1.panelCamera().setOrthogonalView(0);
					rotation_angle = 0;
					defaultify();
					filepath = fpath;
					setTitle(new File(fpath).getName() + " - Origami Editor 3D");
					saved = true;
				}
				catch (Exception ex) {
					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);
					JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
							javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (fpath.endsWith(".txt")) {

				try {
					terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.load(),
							OrigamiScriptTerminal.AccessMode.USER);

					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);

					oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
					oPanel1.resetZoom();
					pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
					pPanel1.resetZoom();
					if (alwaysInMiddle) {
						oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
					}
					oPanel1.panelCamera().setOrthogonalView(0);
					rotation_angle = 0;
					defaultify();
					setTitle(new File(fpath) + " - Origami Editor 3D");
					saved = true;
				}
				catch (Exception ex) {
					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
							javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_file_openActionPerformed

	private void ui_edit_planeActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_edit_planeActionPerformed
		ui_planeActionPerformed(evt);
	}// GEN-LAST:event_ui_edit_planeActionPerformed

	//
	// REDO / MÉGIS
	//
	private void ui_edit_redoActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_edit_redoActionPerformed

		try {

			if (!terminal1.getHistory().isEmpty()) {
				terminal1.executeWithTimeout(OrigamiScripter.redo());
			}
		}
		catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		if (alwaysInMiddle) {
			oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
		}
		oPanel1.update(terminal1.TerminalOrigami);
		pPanel1.update(terminal1.TerminalOrigami);
		defaultify();
		rotation_angle = 0;
		saved = false;
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_edit_redoActionPerformed

	//
	// NEW HEXAGONAL PAPER / ÚJ PAPÍR (HATSZÖG)
	//
	private void ui_file_new_hexagonalActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_new_hexagonalActionPerformed

		if (!saved) {
			if (!dialogManager1.canICloseFile()) {
				return;
			}
		}
		try {

			filepath = null;
			terminal1.executeWithTimeout(OrigamiScripter.paper("hexagon"));
			terminal1.executeWithTimeout(OrigamiScripter._new());

			oPanel1.update(terminal1.TerminalOrigami);
			pPanel1.update(terminal1.TerminalOrigami);

			oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			oPanel1.resetZoom();
			pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			pPanel1.resetZoom();

			oPanel1.randomizeFrontColor();

			defaultify();
			setTitle("Origami Editor 3D");
			saved = true;
		}
		catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_file_new_hexagonalActionPerformed

	//
	// NEW SQUARE PAPER / ÚJ PAPÍR (NÉGYZET)
	//
	private void ui_file_new_squareActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_new_squareActionPerformed

		if (!saved) {
			if (!dialogManager1.canICloseFile()) {
				return;
			}
		}
		try {

			filepath = null;
			terminal1.executeWithTimeout(OrigamiScripter.paper("square"));
			terminal1.executeWithTimeout(OrigamiScripter._new());

			oPanel1.update(terminal1.TerminalOrigami);
			pPanel1.update(terminal1.TerminalOrigami);

			oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			oPanel1.resetZoom();
			pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			pPanel1.resetZoom();

			oPanel1.randomizeFrontColor();

			defaultify();
			setTitle("Origami Editor 3D");
			saved = true;
		}
		catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_file_new_squareActionPerformed

	//
	// NEW A4 PAPER / ÚJ PAPÍR (A4)
	//
	private void ui_file_new_a4ActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_new_a4ActionPerformed

		if (!saved) {
			if (!dialogManager1.canICloseFile()) {
				return;
			}
		}
		try {

			filepath = null;
			terminal1.executeWithTimeout(OrigamiScripter.paper("a4"));
			terminal1.executeWithTimeout(OrigamiScripter._new());

			oPanel1.update(terminal1.TerminalOrigami);
			pPanel1.update(terminal1.TerminalOrigami);

			oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			oPanel1.resetZoom();
			pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			pPanel1.resetZoom();

			oPanel1.randomizeFrontColor();

			defaultify();
			setTitle("Origami Editor 3D");
			saved = true;
		}
		catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_file_new_a4ActionPerformed

	//
	// NEW USD PAPER / ÚJ PAPÍR (EGYDOLLÁROS)
	//
	private void ui_file_new_dollarActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_new_dollarActionPerformed

		if (!saved) {
			if (!dialogManager1.canICloseFile()) {
				return;
			}
		}
		try {

			filepath = null;
			terminal1.executeWithTimeout(OrigamiScripter.paper("usd"));
			terminal1.executeWithTimeout(OrigamiScripter._new());

			oPanel1.update(terminal1.TerminalOrigami);
			pPanel1.update(terminal1.TerminalOrigami);

			oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			oPanel1.resetZoom();
			pPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			pPanel1.resetZoom();

			oPanel1.randomizeFrontColor();

			defaultify();
			setTitle("Origami Editor 3D");
			saved = true;
		}
		catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		foldNumber = terminal1.TerminalOrigami.getHistoryPointer();
		changeListenerShutUp = true;
		timeSlider.setMaximum(terminal1.TerminalOrigami.getHistory().size());
		changeListenerShutUp = false;
		timeSlider.setValue(terminal1.TerminalOrigami.getHistoryPointer());
	}// GEN-LAST:event_ui_file_new_dollarActionPerformed

	//
	// OPTIONS MENU / BEÁLLÍTÁSOK MENÜ
	//
	private void ui_view_optionsActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_optionsActionPerformed
		ui_options.setVisible(true);
	}// GEN-LAST:event_ui_view_optionsActionPerformed

	private void ui_edit_angleActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_edit_angleActionPerformed
		ui_angleActionPerformed(evt);
	}// GEN-LAST:event_ui_edit_angleActionPerformed

	//
	// PLAIN PAPER / SIMA PAPÍR
	//
	private void ui_view_paper_plainActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_plainActionPerformed

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
	private void ui_view_zoomActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_zoomActionPerformed
		zoomOnScroll = ui_view_zoom.isSelected();
	}// GEN-LAST:event_ui_view_zoomActionPerformed

	//
	// ABOUT / NÉVJEGY
	//
	private void ui_help_aboutActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_help_aboutActionPerformed

		final JEditorPane html = new JEditorPane("text/html",
				"<html><body>" + "Origami Editor 3D Version " + Constants.VERSION + " <br>"
						+ "Copyright © 2014 Bágyoni-Szabó Attila (ba-sz-at@users.sourceforge.net) <br>" + "<br>"
						+ "Origami Editor 3D is licensed under the GNU General Public License version 3. <br>"
						+ "<a href=\"/res/LICENSE.txt\">Click here for more information.</a> <br>" + "<br>"
						+ "Some of the origami models bundled with this program are copyrighted works. <br>"
						+ "The usage of such models within this program serves a purely demonstrational/<br>"
						+ "educational purpose, and therefore should be considered 'fair use' by all means.<br>"
						+ "</body></html>");

		html.setEditable(false);
		html.setHighlighter(null);
		html.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				if (evt.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
					Scanner inf = new Scanner(
							OrigamiEditorUI.this.getClass().getResourceAsStream(evt.getDescription()), "UTF-8");
					String text = "";
					while (inf.hasNextLine()) {
						text += inf.nextLine() + (char) 10;
					}
					inf.close();
					terminal_log.setText(text);
					terminal_log.setCaretPosition(0);
					jTabbedPane1.setSelectedIndex(1);
					SwingUtilities.getWindowAncestor(html).dispose();
				}
			}
		});
		JOptionPane.showMessageDialog(this, html);
	}// GEN-LAST:event_ui_help_aboutActionPerformed

	///
	/// SAVE / MENTÉS
	///
	private void ui_file_saveActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_saveActionPerformed

		if (filepath == null) {

			String fpath = dialogManager1.getSaveFilePath("ori");

			if (fpath != null) {

				try {

					if (save_paper_color) {
						terminal1.executeWithTimeout(OrigamiScripter.color(oPanel1.getFrontColor()));
					}
					else {
						terminal1.executeWithTimeout(OrigamiScripter.uncolor());
					}
					terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_ori(),
							OrigamiScriptTerminal.AccessMode.USER);
					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);
					defaultify();
					rotation_angle = 0;
					filepath = fpath;
					setTitle(new File(fpath).getName() + " - Origami Editor 3D");
					saved = true;
				}
				catch (Exception ex) {
					oPanel1.update(terminal1.TerminalOrigami);
					pPanel1.update(terminal1.TerminalOrigami);
					JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
							javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else {

			try {

				if (save_paper_color) {
					terminal1.executeWithTimeout(OrigamiScripter.color(oPanel1.getFrontColor()));
				}
				else {
					terminal1.executeWithTimeout(OrigamiScripter.uncolor());
				}
				terminal1.executeWithTimeout(OrigamiScripter.filename(filepath) + OrigamiScripter.export_ori(),
						OrigamiScriptTerminal.AccessMode.USER);
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				defaultify();
				rotation_angle = 0;
				saved = true;
			}
			catch (Exception ex) {
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_saveActionPerformed

	//
	// BEST FIT / LEGJOBB ILLESZKEDÉS
	//
	private void ui_view_bestActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_bestActionPerformed

		alwaysInMiddle = ui_view_best.isSelected();
		if (alwaysInMiddle) {
			oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
			oPanel1.rulerOff();
			pPanel1.rulerOff();
			oPanel1.repaint();
			pPanel1.repaint();
		}
		else {
			oPanel1.panelCamera().unadjust(terminal1.TerminalOrigami);
			oPanel1.rulerOff();
			pPanel1.rulerOff();
			oPanel1.repaint();
			pPanel1.repaint();
		}
	}// GEN-LAST:event_ui_view_bestActionPerformed

	//
	// NEUSIS MODE / NEUSZISZ MÓD
	//
	private void ui_edit_neusisActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_edit_neusisActionPerformed

		if (ui_edit_neusis.isSelected()) {

			oPanel1.setRulerMode(Panel.RulerMode.Neusis);
			pPanel1.setRulerMode(Panel.RulerMode.Neusis);
			neusisOn = true;
			ui_view_show.setSelected(true);
			oPanel1.previewOn();
		}
		else {

			oPanel1.setRulerMode(Panel.RulerMode.Normal);
			pPanel1.setRulerMode(Panel.RulerMode.Normal);
			neusisOn = false;
			ui_view_show.setSelected(false);
			oPanel1.previewOff();
		}
		oPanel1.repaint();
	}// GEN-LAST:event_ui_edit_neusisActionPerformed

	//
	// PREVIEW / ELŐNÉZET
	//
	private void ui_view_showActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_showActionPerformed

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
	private void ui_view_useActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_showActionPerformed

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
	private void ui_view_paper_imageActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_imageActionPerformed

		ui_view_paper_image.setSelected(false);
		String fpath = null;

		if (tex == null) {

			if (JOptionPane.showOptionDialog(this, Dictionary.getString("loadtex"),
					Dictionary.getString("notex"), javax.swing.JOptionPane.YES_NO_OPTION,
					javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, null) == 0) {

				fpath = dialogManager1.getOpenImagePath();
			}

		}
		else if (oPanel1.displaymode() == OrigamiPanel.DisplayMode.UV) {

			if (JOptionPane.showOptionDialog(this, Dictionary.getString("loadtex"),
					Dictionary.getString("havetex"), javax.swing.JOptionPane.YES_NO_OPTION,
					javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, null) == 0) {

				fpath = dialogManager1.getOpenImagePath();
			}
		}

		if (fpath != null) {

			try {

				terminal1.executeWithTimeout(OrigamiScripter.filename(fpath));
				terminal1.executeWithTimeout(OrigamiScripter.load_texture());
				tex = terminal1.getPaperTexture();
				if (tex.getHeight() < (int) terminal1.TerminalOrigami.paperHeight()
						|| tex.getWidth() < (int) terminal1.TerminalOrigami.paperWidth()) {

					String uzenet = Dictionary.getString("message.warning.smalltex",
							(int) terminal1.TerminalOrigami.paperWidth(),
							(int) terminal1.TerminalOrigami.paperHeight());
					JOptionPane.showMessageDialog(this, uzenet, Dictionary.getString("message.warning"),
							javax.swing.JOptionPane.WARNING_MESSAGE);
				}
				else if (tex.getHeight() > (int) terminal1.TerminalOrigami.paperHeight()
						|| tex.getWidth() > (int) terminal1.TerminalOrigami.paperWidth()) {

					String uzenet = Dictionary.getString("message.warning.largetex",
							(int) terminal1.TerminalOrigami.paperWidth(),
							(int) terminal1.TerminalOrigami.paperHeight());
					JOptionPane.showMessageDialog(this, uzenet, Dictionary.getString("message.warning"),
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
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
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
	private void ui_view_paper_noneActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_noneActionPerformed

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
	private void ui_view_timelineActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_timelineActionPerformed

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
	private void timeSliderStateChanged(ChangeEvent evt) {

		if (foldNumber == timeSlider.getValue() || changeListenerShutUp) {
			return;
		}
		if (terminal1.TerminalOrigami.getHistory().size() < 100) {
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
			oPanel1.panelCamera().adjust(terminal1.TerminalOrigami);
		}
		defaultify();
	}

	//
	// USER GUIDE / FELHASZNÁLÓI KÉZIKÖNYV
	//
	private void ui_help_userActionPerformed(ActionEvent evt) {

		try {
			Scanner inf = new Scanner(new URL(Constants.INFO_LINK).openStream());
			String line;
			while (!(line = inf.nextLine()).startsWith("userguide_link"))
				;
			inf.close();
			String url = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
			if (Desktop.isDesktopSupported() ? Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) : false) {
				Desktop.getDesktop().browse(new URI(url));
			}
			else {
				JTextArea copyable = new JTextArea(Dictionary.getString("browser-fail", url));
				copyable.setEditable(false);
				JOptionPane.showMessageDialog(this, copyable, Dictionary.getString("error"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception ex) {
			try {
				if (Desktop.isDesktopSupported() ? Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) : false) {
					Desktop.getDesktop().browse(new URI(Constants.USER_GUIDE_LINK));
				}
				else {
					JTextArea copyable = new JTextArea(
							Dictionary.getString("browser-fail", Constants.USER_GUIDE_LINK));
					copyable.setEditable(false);
					JOptionPane.showMessageDialog(this, copyable, Dictionary.getString("error"),
							javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (Exception ex2) {
				JTextArea copyable = new JTextArea(
						Dictionary.getString("connect-fail", Constants.USER_GUIDE_LINK));
				copyable.setEditable(false);
				JOptionPane.showMessageDialog(this, copyable, Dictionary.getString("error"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	//
	// SHOW TOOLTIPS / EGÉRFELIRATOK MUTATÁSA
	//
	private void ui_help_showActionPerformed(ActionEvent evt) {
		ToolTipManager.sharedInstance().setEnabled(ui_help_show.isSelected());
	}

	//
	// GRADIENT PAPER / ÁRNYALT PAPÍR
	//
	private void ui_view_paper_gradientActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_view_paper_gradientActionPerformed

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
	private void ui_file_export_togif_revolvingActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_export_togif_revolvingActionPerformed

		final String fpath = dialogManager1.getSaveFilePath("gif");

		if (fpath != null) {

			try {

				final JDialog exporting = new JDialog(this);
				exporting.setUndecorated(true);
				JLabel loadmsg = new JLabel(Dictionary.getString("message.info.exporting"));
				loadmsg.setForeground(Color.RED);
				exporting.getContentPane().setLayout(new BorderLayout());
				exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
				exporting.getContentPane().setBackground(Color.YELLOW);
				exporting.pack();
				exporting.setResizable(false);
				exporting.setLocationRelativeTo(null);
				exporting.setVisible(true);

				final Exception[] unreportedException = { null };

				new SwingWorker<Void, Integer>() {

					@Override
					protected Void doInBackground() {

						try {

							terminal1.executeWithTimeout(OrigamiScripter.camera(oPanel1.panelCamera().getCamDirection(),
									oPanel1.panelCamera().getXScale(), oPanel1.panelCamera().getYScale()));
							terminal1.executeWithTimeout(OrigamiScripter.color(oPanel1.getFrontColor()));
							terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_revolving_gif(),
									OrigamiScriptTerminal.AccessMode.USER);

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
						JOptionPane.showMessageDialog(OrigamiEditorUI.this,
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
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_export_togif_revolvingActionPerformed

	//
	// FOLDING GIF EXPORT
	//
	private void ui_file_export_togif_foldingActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_export_togif_foldingActionPerformed

		final String fpath = dialogManager1.getSaveFilePath("gif");

		if (fpath != null) {

			try {

				final JDialog exporting = new JDialog(this);
				exporting.setUndecorated(true);
				JLabel loadmsg = new JLabel(Dictionary.getString("message.info.exporting"));
				loadmsg.setForeground(Color.RED);
				exporting.getContentPane().setLayout(new BorderLayout());
				exporting.getContentPane().add(loadmsg, java.awt.BorderLayout.CENTER);
				exporting.getContentPane().setBackground(Color.YELLOW);
				exporting.pack();
				exporting.setResizable(false);
				exporting.setLocationRelativeTo(null);
				exporting.setVisible(true);

				final Exception[] unreportedException = { null };

				new SwingWorker<Void, Integer>() {

					@Override
					protected Void doInBackground() {

						try {

							terminal1.executeWithTimeout(OrigamiScripter.camera(oPanel1.panelCamera().getCamDirection(),
									oPanel1.panelCamera().getXScale(), oPanel1.panelCamera().getYScale()));
							terminal1.executeWithTimeout(OrigamiScripter.color(oPanel1.getFrontColor()));
							terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_gif(),
									OrigamiScriptTerminal.AccessMode.USER);

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
						JOptionPane.showMessageDialog(OrigamiEditorUI.this,
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
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_export_togif_foldingActionPerformed

	//
	// PNG EXPORT
	//
	private void ui_file_export_creaseActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_export_creaseActionPerformed

		String fpath = dialogManager1.getSaveFilePath("png");

		if (fpath != null) {

			try {
				terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_png(),
						OrigamiScriptTerminal.AccessMode.USER);
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				defaultify();
				rotation_angle = 0;
			}
			catch (Exception ex) {
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_export_creaseActionPerformed

	private void ui_panelsComponentResized(ComponentEvent evt) {// GEN-FIRST:event_ui_panelsComponentResized
		ui_panels.setDividerLocation(0.5);
	}// GEN-LAST:event_ui_panelsComponentResized

	private void ui_toolbarsComponentResized(ComponentEvent evt) {// GEN-FIRST:event_ui_toolbarsComponentResized
		ui_toolbars.setDividerLocation(0.5);
	}// GEN-LAST:event_ui_toolbarsComponentResized

	//
	// SELECT
	//
	private void ui_selectActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_selectActionPerformed

		ui_select.setSelected(true);
		ui_plane.setSelected(false);
		ui_angle.setSelected(false);
		targetOn = true;
		pPanel1.setRulerMode(neusisOn ? Panel.RulerMode.Neusis : Panel.RulerMode.Normal);
		pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.select"));
		oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.standby"));
		oPanel1.repaint();
		pPanel1.repaint();
	}// GEN-LAST:event_ui_selectActionPerformed

	//
	// PLANE THROUGH 3 POINTS / 3 PONTOS ILLESZTÉS
	//
	private void ui_planeActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_planeActionPerformed

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
		pPanel1.setRulerMode(Panel.RulerMode.Planethrough);
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
	private void ui_angleActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_angleActionPerformed

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
		pPanel1.setRulerMode(Panel.RulerMode.Angle_bisector);
		pPanel1.setToolTipText(Dictionary.getString("tooltip.ppanel.3points1"));
		oPanel1.setToolTipText(Dictionary.getString("tooltip.opanel.3points"));

		ui_select.setSelected(false);
		ui_plane.setSelected(false);
		ui_angle.setSelected(true);
		targetOn = false;
	}// GEN-LAST:event_ui_angleActionPerformed

	private void ui_snap_1ActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_snap_1ActionPerformed

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

	private void ui_snap_2ActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_snap_2ActionPerformed

		if (alignOn) {
			snap2 = ui_snap_2.isSelected() ? 2 : 1;
		}
		else {
			ui_snap_2.setSelected(false);
		}
	}// GEN-LAST:event_ui_snap_2ActionPerformed

	private void ui_snap_3ActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_snap_3ActionPerformed

		if (alignOn) {
			snap3 = ui_snap_3.isSelected() ? 3 : 1;
		}
		else {
			ui_snap_3.setSelected(false);
		}
	}// GEN-LAST:event_ui_snap_3ActionPerformed

	private void ui_snap_4ActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_snap_4ActionPerformed

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
	private void ui_file_propertiesActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_propertiesActionPerformed

		final JDialog properties = new JDialog(this, Dictionary.getString("ui.file.properties"));
		properties.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = java.awt.GridBagConstraints.NORTH;
		properties.getContentPane().add(new JLabel(Dictionary.getString("origami-version")), c);
		c.gridy = 1;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridy = 2;
		properties.getContentPane().add(new JLabel(Dictionary.getString("origami-papertype")), c);
		c.gridy = 3;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridy = 4;
		properties.getContentPane().add(new JLabel(Dictionary.getString("origami-steps")), c);
		c.gridy = 5;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridy = 6;
		properties.getContentPane().add(new JLabel(Dictionary.getString("origami-difficulty")), c);
		c.gridy = 7;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridx = 1;
		c.gridy = 0;
		properties.getContentPane().add(new JLabel("Generation " + terminal1.TerminalOrigami.generation()),
				c);
		c.gridy = 1;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridy = 2;
		properties.getContentPane().add(
				new JLabel(Dictionary.getString(terminal1.TerminalOrigami.getPaperType().toString())), c);
		c.gridy = 3;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridy = 4;
		properties.getContentPane()
				.add(new JLabel(Integer.toString(terminal1.TerminalOrigami.getHistory().size())), c);
		c.gridy = 5;
		properties.getContentPane().add(new JLabel(" "), c);
		c.gridy = 6;
		final JLabel diflabel = new JLabel(Dictionary.getString("calculating..."));
		properties.getContentPane().add(diflabel, c);
		c.gridy = 7;
		properties.getContentPane().add(new JLabel(" "), c);

		properties.setResizable(false);
		properties.pack();
		properties.setLocationRelativeTo(this);

		final Runnable calcDifficulty = new Runnable() {

			@Override
			public void run() {

				int dif = OrigamiGen1.difficultyLevel(terminal1.TerminalOrigami.difficulty());
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
		};

		final ExecutorService exec = Executors.newSingleThreadExecutor();
		
		properties.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent evt) {

				exec.shutdownNow();
				super.windowClosed(evt);
			}
		});

		properties.setVisible(true);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				exec.execute(calcDifficulty);
			}
		});
	}// GEN-LAST:event_ui_file_propertiesActionPerformed

	//
	// JAR EXPORT
	//
	private void ui_file_export_toselfActionPerformed(ActionEvent evt) {// GEN-FIRST:event_ui_file_export_toselfActionPerformed

		String fpath = dialogManager1.getSaveFilePath("ori.jar");

		if (fpath != null) {

			try {
				if (save_paper_color) {
					terminal1.executeWithTimeout(OrigamiScripter.color(oPanel1.getFrontColor()));
				}
				else {
					terminal1.executeWithTimeout(OrigamiScripter.uncolor());
				}
				terminal1.executeWithTimeout(OrigamiScripter.filename(fpath) + OrigamiScripter.export_jar(),
						OrigamiScriptTerminal.AccessMode.USER);
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				defaultify();
				rotation_angle = 0;
			}
			catch (Exception ex) {
				oPanel1.update(terminal1.TerminalOrigami);
				pPanel1.update(terminal1.TerminalOrigami);
				JOptionPane.showMessageDialog(this, ex.getMessage().replace('/', (char) 10), "Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}// GEN-LAST:event_ui_file_export_toselfActionPerformed

	private boolean snap1(int radius) {

		int v1ujX = -1;
		int v1ujY = -1;

		for (int[] osztohely : oPanel1.panelCamera().alignmentPoints(terminal1.TerminalOrigami, snap2, snap3, snap4)) {

			if ((ruler1X - oPanel1.panelCamera().getXShift() - osztohely[0])
					* (ruler1X - oPanel1.panelCamera().getXShift() - osztohely[0])
					+ (ruler1Y - oPanel1.panelCamera().getYShift() - osztohely[1])
							* (ruler1Y - oPanel1.panelCamera().getYShift() - osztohely[1]) < radius) {

				v1ujX = osztohely[0] + oPanel1.panelCamera().getXShift();
				v1ujY = osztohely[1] + oPanel1.panelCamera().getYShift();
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

		for (int[] osztohely : oPanel1.panelCamera().alignmentPoints(terminal1.TerminalOrigami, snap2, snap3, snap4)) {

			if ((ruler2X - oPanel1.panelCamera().getXShift() - osztohely[0])
					* (ruler2X - oPanel1.panelCamera().getXShift() - osztohely[0])
					+ (ruler2Y - oPanel1.panelCamera().getYShift() - osztohely[1])
							* (ruler2Y - oPanel1.panelCamera().getYShift() - osztohely[1]) < sugar) {

				v2ujX = osztohely[0] + oPanel1.panelCamera().getXShift();
				v2ujY = osztohely[1] + oPanel1.panelCamera().getYShift();
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

		for (int[] osztohely : pPanel1.panelCamera().alignmentPoints2d(terminal1.TerminalOrigami)) {

			if ((x - pPanel1.panelCamera().getXShift() - osztohely[0])
					* (x - pPanel1.panelCamera().getXShift() - osztohely[0])
					+ (y - pPanel1.panelCamera().getYShift() - osztohely[1])
							* (y - pPanel1.panelCamera().getYShift() - osztohely[1]) < radius) {

				ujX = osztohely[0] + pPanel1.panelCamera().getXShift();
				ujY = osztohely[1] + pPanel1.panelCamera().getYShift();
				break;
			}
		}
		if (ujX != -1) {
			return new int[] { ujX, ujY };
		}
		return new int[] { x, y };
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JMenuBar jMenuBar1;
	private JScrollPane jScrollPane3;
	private javax.swing.JPopupMenu.Separator jSeparator1;
	private javax.swing.JPopupMenu.Separator jSeparator2;
	private javax.swing.JPopupMenu.Separator jSeparator3;
	private javax.swing.JPopupMenu.Separator jSeparator4;
	private javax.swing.JPopupMenu.Separator jSeparator5;
	private javax.swing.JPopupMenu.Separator jSeparator6;
	private JSplitPane jSplitPane2;
	private JTabbedPane jTabbedPane1;
	private JTextArea terminal_log;
	private JTextField jTextField1;
	private OrigamiPanel oPanel1;
	private PaperPanel pPanel1;
	private JToggleButton ui_angle;
	private JMenu ui_edit;
	private JMenuItem ui_edit_angle;
	private JCheckBoxMenuItem ui_edit_neusis;
	private JMenuItem ui_edit_plane;
	private JMenuItem ui_edit_redo;
	private JMenuItem ui_edit_undo;
	private JSplitPane ui_editor;
	private JMenu ui_file;
	private JMenu ui_file_export;
	private JMenuItem ui_file_export_crease;
	private JMenu ui_file_export_togif;
	private JMenuItem ui_file_export_togif_folding;
	private JMenuItem ui_file_export_togif_revolving;
	private JMenuItem ui_file_export_toopenctm;
	private JMenuItem ui_file_export_topdf;
	private JMenuItem ui_file_export_toself;
	private JMenu ui_file_new;
	private JMenuItem ui_file_new_a4;
	private JMenu ui_file_new_bases;
	private JMenuItem ui_file_new_dollar;
	private JMenuItem ui_file_new_hexagonal;
	private JMenuItem ui_file_new_square;
	private JMenuItem ui_file_open;
	private JMenuItem ui_file_properties;
	private JMenu ui_file_example;
	private JMenuItem ui_file_save;
	private JMenuItem ui_file_saveas;
	private JMenu ui_help;
	private JMenuItem ui_help_user;
	private JMenuItem ui_help_about;
	private JMenu ui_help_lang;
	private JCheckBoxMenuItem ui_help_show;
	private JToolBar ui_leftbar;
	private JSplitPane ui_panels;
	private JToggleButton ui_plane;
	private JToolBar ui_rightbar;
	private JToggleButton ui_select;
	private JToggleButton ui_snap_1;
	private JToggleButton ui_snap_2;
	private JToggleButton ui_snap_3;
	private JToggleButton ui_snap_4;
	private JLabel ui_snap;
	private Separator ui_snap_separator;
	private JSplitPane ui_editor_notimeline;
	private JSplitPane ui_toolbars;
	private JPanel ui_timeline;
	private JLabel ui_timeline_label;
	private JSlider timeSlider;
	private JMenu ui_view;
	private JMenu ui_view_paper;
	private JCheckBoxMenuItem ui_view_paper_gradient;
	private JCheckBoxMenuItem ui_view_paper_image;
	private JCheckBoxMenuItem ui_view_paper_none;
	private JCheckBoxMenuItem ui_view_paper_plain;
	private JCheckBoxMenuItem ui_view_use;
	private JCheckBoxMenuItem ui_view_show;
	private JCheckBoxMenuItem ui_view_zoom;
	private JCheckBoxMenuItem ui_view_best;
	private JCheckBoxMenuItem ui_view_timeline;
	private JMenuItem ui_view_options;
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
