package origamieditor3d.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import origamieditor3d.graphics.Camera;
import origamieditor3d.io.Export;
import origamieditor3d.io.OrigamiIO;
import origamieditor3d.origami.Geometry;
import origamieditor3d.origami.Origami;
import origamieditor3d.origami.OrigamiException;
import origamieditor3d.origami.OrigamiGen1;
import origamieditor3d.origami.OrigamiGen2;
import origamieditor3d.resources.Dictionary;
import origamieditor3d.resources.Instructor;

/**
 * Represents an OrigamiScript engine. To learn more about OrigamiScript, see
 * http://origamieditor3d.sourceforge.net/osdoc_en.html
 * 
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-09-29
 */
public class OrigamiScriptTerminalV1 implements OrigamiScriptTerminal {

	public OrigamiScriptTerminalV1(AccessMode access) {

		this.accessMode = access;
		TerminalCamera = new Camera(0, 0, 1);
		corners = new ArrayList<>();
		papertype = Origami.PaperType.Custom;
		paperColor = 0x0000FF;
		paperTexture = null;
		history = new ArrayList<>();
		filename = null;
		ppoint = null;
		pnormal = null;
		tracker = null;
		phi = null;
		title = null;

		activePrompts = 0;
	}

	public OrigamiScriptTerminalV1(AccessMode access, String filename) {

		this(access);
		this.filename = filename;
		TerminalCamera = new Camera(0, 0, 1);
	}

	private int activePrompts;

	final private Integer version = 1;
	private AccessMode accessMode;
	//
	// eltárolt terminál mezők
	private List<String> history;

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#getHistory()
	 */
	@Override
	public List<String> getHistory() {
		return history;
	}

	private String filename;
	//
	// betöltött szerkesztési mezők
	private double[] ppoint;
	private double[] pnormal;
	private double[] tracker;
	private Integer phi;
	private ArrayList<double[]> corners;
	private OrigamiGen1.PaperType papertype;
	private String title;
	//
	// eltárolt szerkesztési mezők
	public Origami TerminalOrigami;
	public Camera TerminalCamera;
	private int paperColor;

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#getPaperColor()
	 */
	@Override
	public int getPaperColor() {
		return paperColor;
	}

	final static private int default_paper_color = 0x000097;
	private java.awt.image.BufferedImage paperTexture;

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#getPaperTexture()
	 */
	@Override
	public java.awt.image.BufferedImage getPaperTexture() {
		return paperTexture;
	}

	private void paramReset() {

		ppoint = null;
		pnormal = null;
		tracker = null;
		phi = null;
		title = null;
		corners = new ArrayList<>();
		papertype = Origami.PaperType.Custom;
	}

	private void totalReset() {

		paramReset();
		history.clear();

		TerminalOrigami.undo(TerminalOrigami.getHistory().size());
		TerminalCamera = new Camera(0, 0, 1);
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#clearHistory()
	 */
	@Override
	public void clearHistory() {
		history.clear();
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#getVersion()
	 */
	@Override
	public Integer getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#getAccessMode()
	 */
	@Override
	public AccessMode getAccessMode() {
		return accessMode;
	}

	

	private interface Command {
		void execute(String... args) throws Exception;
	}

	final private HashMap<String, Command> Commands = new HashMap<>();
	final private HashMap<String, Command> Params = new HashMap<>();

	{
		Params.put("plane", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				plane(args);
			}
		});

		Params.put("planethrough", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				planethrough(args);
			}
		});

		Params.put("angle-bisector", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				angle_bisector(args);
			}
		});

		Params.put("planepoint", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				planepoint(args);
			}
		});

		Params.put("planenormal", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				planenormal(args);
			}
		});

		Params.put("target", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				target(args);
			}
		});

		Params.put("angle", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				angle(args);
			}
		});

		Params.put("paper", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				paper(args);
			}
		});

		Params.put("corner", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				corner(args);
			}
		});

		Params.put("locale", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				locale(args);
			}
		});

		Params.put("filename", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				filename(args);
			}
		});

		Params.put("title", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				title(args);
			}
		});

		Params.put("camera", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				camera(args);
			}
		});

		Params.put("color", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				color(args);
			}
		});

		Params.put("uncolor", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				uncolor(args);
			}
		});
	}

	{
		Commands.put("new", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				NEW();
			}
		});

		Commands.put("rotate", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				ROTATE();
			}
		});

		Commands.put("reflect", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				REFLECT();
			}
		});

		Commands.put("cut", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				CUT();
			}
		});

		Commands.put("undo", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				UNDO();
			}
		});

		Commands.put("redo", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				REDO();
			}
		});

		Commands.put("diagnostics", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				DIAGNOSTICS();
			}
		});

		Commands.put("load", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				LOAD();
			}
		});

		Commands.put("open", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				OPEN();
			}
		});

		Commands.put("load-texture", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				LOAD_TEXTURE();
			}
		});

		Commands.put("unload-texture", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				UNLOAD_TEXTURE();
			}
		});

		Commands.put("export-ctm", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_CTM();
			}
		});

		Commands.put("export-autopdf", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_AUTOPDF();
			}
		});

		Commands.put("export-gif", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_GIF();
			}
		});

		Commands.put("export-jar", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_JAR();
			}
		});

		Commands.put("export-png", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_PNG();
			}
		});

		Commands.put("export-revolving-gif", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_REVOLVING_GIF();
			}
		});

		Commands.put("export-ori", new Command() {
			@Override
			public void execute(String... args) throws Exception {
				EXPORT_ORI();
			}
		});
	}

	/*
	 * EDITOR PARAMETERS
	 */

	private void plane(String... args) throws Exception {

		if (args.length == 2) {

			String[] pt = args[0].split(" ");
			String[] nv = args[1].split(" ");

			if (pt.length == 3 && nv.length == 3) {

				ppoint = new double[] { Double.parseDouble(pt[0]), Double.parseDouble(pt[1]),
						Double.parseDouble(pt[2]) };

				pnormal = new double[] { Double.parseDouble(nv[0]), Double.parseDouble(nv[1]),
						Double.parseDouble(nv[2]) };

			}
			else if (pt.length == 2 && nv.length == 3) {

				ppoint = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pt[0]), Double.parseDouble(pt[1]) });

				pnormal = new double[] { Double.parseDouble(nv[0]), Double.parseDouble(nv[1]),
						Double.parseDouble(nv[2]) };

			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void planethrough(String... args) throws Exception {

		if (args.length == 3) {

			String[] pont1 = args[0].split(" ");
			String[] pont2 = args[1].split(" ");
			String[] pont3 = args[2].split(" ");

			double[] pt1, pt2, pt3;

			if (pont1.length == 3) {

				pt1 = new double[] { Double.parseDouble(pont1[0]), Double.parseDouble(pont1[1]),
						Double.parseDouble(pont1[2]) };
			}
			else if (pont1.length == 2) {

				pt1 = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pont1[0]), Double.parseDouble(pont1[1]) });
			}
			else {
				throw OrigamiException.H007;
			}

			if (pont2.length == 3) {

				pt2 = new double[] { Double.parseDouble(pont2[0]), Double.parseDouble(pont2[1]),
						Double.parseDouble(pont2[2]) };
			}
			else if (pont2.length == 2) {

				pt2 = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pont2[0]), Double.parseDouble(pont2[1]) });
			}
			else {
				throw OrigamiException.H007;
			}

			if (pont3.length == 3) {

				pt3 = new double[] { Double.parseDouble(pont3[0]), Double.parseDouble(pont3[1]),
						Double.parseDouble(pont3[2]) };
			}
			else if (pont3.length == 2) {

				pt3 = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pont3[0]), Double.parseDouble(pont3[1]) });
			}
			else {
				throw OrigamiException.H007;
			}

			if (Geometry.vectorLength(
					Geometry.crossProduct(Geometry.vectorDiff(pt2, pt1), Geometry.vectorDiff(pt3, pt1))) != 0d) {

				ppoint = pt1;
				pnormal = Geometry.crossProduct(Geometry.vectorDiff(pt2, pt1), Geometry.vectorDiff(pt3, pt1));
			}
			else {
				throw OrigamiException.H008;
			}
		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void angle_bisector(String... args) throws Exception {

		if (args.length == 3) {

			String[] pont1 = args[0].split(" ");
			String[] pont2 = args[1].split(" ");
			String[] pont3 = args[2].split(" ");

			double[] pt1, pt2, pt3;

			if (pont1.length == 3) {

				pt1 = new double[] { Double.parseDouble(pont1[0]), Double.parseDouble(pont1[1]),
						Double.parseDouble(pont1[2]) };
			}
			else if (pont1.length == 2) {

				pt1 = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pont1[0]), Double.parseDouble(pont1[1]) });
			}
			else {
				throw OrigamiException.H007;
			}

			if (pont2.length == 3) {

				pt2 = new double[] { Double.parseDouble(pont2[0]), Double.parseDouble(pont2[1]),
						Double.parseDouble(pont2[2]) };
			}
			else if (pont2.length == 2) {

				pt2 = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pont2[0]), Double.parseDouble(pont2[1]) });
			}
			else {
				throw OrigamiException.H007;
			}

			if (pont3.length == 3) {

				pt3 = new double[] { Double.parseDouble(pont3[0]), Double.parseDouble(pont3[1]),
						Double.parseDouble(pont3[2]) };
			}
			else if (pont3.length == 2) {

				pt3 = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pont3[0]), Double.parseDouble(pont3[1]) });
			}
			else {
				throw OrigamiException.H007;
			}
			ppoint = pt2;
			pnormal = Geometry.vectorDiff(Geometry.length_to_100(Geometry.vectorDiff(pt1, pt2)),
					Geometry.length_to_100(Geometry.vectorDiff(pt3, pt2)));

			if (Geometry.vectorLength(pnormal) == 0.) {
				throw OrigamiException.H012;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void planepoint(String... args) throws Exception {

		if (args.length == 1) {

			String[] pt = args[0].split(" ");

			if (pt.length == 3) {

				ppoint = new double[] { Double.parseDouble(pt[0]), Double.parseDouble(pt[1]),
						Double.parseDouble(pt[2]) };

			}
			else if (pt.length == 2) {

				ppoint = TerminalOrigami
						.find3dImageOf(new double[] { Double.parseDouble(pt[0]), Double.parseDouble(pt[1]) });

			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void planenormal(String... args) throws Exception {

		if (args.length == 1) {

			String[] nv = args[1].split(" ");

			if (nv.length == 3) {

				pnormal = new double[] { Double.parseDouble(nv[0]), Double.parseDouble(nv[1]),
						Double.parseDouble(nv[2]) };

			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void target(String... args) throws Exception {

		if (args.length == 1) {

			String[] maghely = args[0].split(" ");

			if (maghely.length == 2) {

				tracker = new double[] { Double.parseDouble(maghely[0]), Double.parseDouble(maghely[1]) };
			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void angle(String... args) throws Exception {

		if (args.length == 1) {

			String[] szog = args[0].split(" ");

			if (szog.length == 1) {

				phi = Integer.parseInt(szog[0]);
			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void paper(String... args) throws Exception {

		if (args.length == 1) {

			String[] bajf = args[0].split(" ");

			if (bajf.length == 4) {

				corners = new ArrayList<>(Arrays.asList(new double[][] {}));
				corners.add(new double[] { Double.parseDouble(bajf[0]), Double.parseDouble(bajf[1]) });
				corners.add(new double[] { Double.parseDouble(bajf[0]), Double.parseDouble(bajf[3]) });
				corners.add(new double[] { Double.parseDouble(bajf[2]), Double.parseDouble(bajf[1]) });
				corners.add(new double[] { Double.parseDouble(bajf[2]), Double.parseDouble(bajf[3]) });

			}
			else if (bajf.length == 1) {

				switch (bajf[0]) {

				case "square":
					papertype = Origami.PaperType.Square;
					break;
				case "a4":
					papertype = Origami.PaperType.A4;
					break;
				case "hexagon":
					papertype = Origami.PaperType.Hexagon;
					break;
				case "usd":
					papertype = Origami.PaperType.Dollar;
					break;
				case "huf":
					papertype = Origami.PaperType.Forint;
					break;
				default:
					throw OrigamiException.H009;
				}
			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void corner(String... args) throws Exception {

		if (args.length == 1) {

			String[] koo = args[0].split(" ");

			if (koo.length == 2) {

				corners.add(new double[] { Double.parseDouble(koo[0]), Double.parseDouble(koo[1]) });
			}
			else {
				throw OrigamiException.H007;
			}

		}
		else {
			throw OrigamiException.H007;
		}
	}

	/*
	 * EDITOR COMMANDS
	 */

	private void NEW() throws Exception {

		clearHistory();
		if (papertype == Origami.PaperType.Custom) {

			try {
				TerminalOrigami = new OrigamiGen2(corners);
			}
			catch (Exception ex) {
				throw OrigamiException.H001;
			}
			paramReset();
		}
		else {

			TerminalOrigami = new OrigamiGen2(papertype);
			paramReset();
		}
	}

	private void ROTATE() throws Exception {

		if (ppoint != null && pnormal != null && phi != null && tracker == null) {
			TerminalOrigami.rotationFold(ppoint, pnormal, phi);

		}
		else if (ppoint != null && pnormal != null && phi != null && tracker != null) {

			TerminalOrigami.crease(ppoint, pnormal);

			int mag = TerminalOrigami.findPolygonContaining(tracker);

			TerminalOrigami.rotationFold(ppoint, pnormal, phi, mag);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void REFLECT() throws Exception {

		if (ppoint != null && pnormal != null && tracker == null) {

			TerminalOrigami.reflectionFold(ppoint, pnormal);
		}
		else if (ppoint != null && pnormal != null && tracker != null) {

			TerminalOrigami.crease(ppoint, pnormal);

			int mag = TerminalOrigami.findPolygonContaining(tracker);

			TerminalOrigami.reflectionFold(ppoint, pnormal, mag);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void CUT() throws Exception {

		if (ppoint != null && pnormal != null && tracker == null) {

			TerminalOrigami.mutilation(ppoint, pnormal);
		}
		else if (ppoint != null && pnormal != null && tracker != null) {

			TerminalOrigami.crease(ppoint, pnormal);

			int mag = TerminalOrigami.findPolygonContaining(tracker);

			TerminalOrigami.mutilation(ppoint, pnormal, mag);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void UNDO() throws Exception {

		if (TerminalOrigami.getHistory().size() > 0) {
			TerminalOrigami.undo();
		}
		else {
			undo(1);
		}
		paramReset();
	}

	private void REDO() throws Exception {

		TerminalOrigami.redo();
		paramReset();
	}

	/*
	 * TERMINAL PARAMETERS
	 */

	private void locale(String... args) throws Exception {

		if (args.length == 1) {

			String[] loc = args[0].split(" ");

			if (loc.length == 2) {
				Dictionary.setLocale(new java.util.Locale(loc[0], loc[1]));
				Instructor.setLocale(new java.util.Locale(loc[0], loc[1]));
			}
			else {
				throw OrigamiException.H007;
			}
		}
	}

	private void filename(String... args) throws Exception {

		if (args.length == 1) {

			filename = args[0];
		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void title(String... args) throws Exception {

		if (args.length == 1) {

			title = args[0];
		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void camera(String... args) throws Exception {

		if (args.length == 3) {

			String[] Dir = args[0].split(" ");
			String[] Xaxis = args[1].split(" ");
			String[] Yaxis = args[2].split(" ");

			double[] dir, xaxis, yaxis;
			if (Dir.length == 3) {
				dir = new double[] { Double.parseDouble(Dir[0]), Double.parseDouble(Dir[1]),
						Double.parseDouble(Dir[2]) };
			}
			else {
				throw OrigamiException.H007;
			}
			if (Xaxis.length == 3) {
				xaxis = new double[] { Double.parseDouble(Xaxis[0]), Double.parseDouble(Xaxis[1]),
						Double.parseDouble(Xaxis[2]) };
			}
			else {
				throw OrigamiException.H007;
			}
			if (Yaxis.length == 3) {
				yaxis = new double[] { Double.parseDouble(Yaxis[0]), Double.parseDouble(Yaxis[1]),
						Double.parseDouble(Yaxis[2]) };
			}
			else {
				throw OrigamiException.H007;
			}

			TerminalCamera.setCamDirection(dir);
			TerminalCamera.setXAxis(xaxis);
			TerminalCamera.setYAxis(yaxis);

		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void color(String... args) throws Exception {

		if (args.length == 1) {

			String[] Col = args[0].split(" ");

			if (Col.length == 1) {
				paperColor = Integer.parseInt(Col[0]) & 0xFFFFFF;
			}
			else if (Col.length == 3) {

				paperColor = ((Integer.parseInt(Col[0]) & 0xFF) << 16) + ((Integer.parseInt(Col[1]) & 0xFF) << 8)
						+ (Integer.parseInt(Col[2]) & 0xFF);
			}
			else {
				throw OrigamiException.H007;
			}
		}
		else {
			throw OrigamiException.H007;
		}
	}

	private void uncolor(String... args) throws Exception {

		if (args.length == 0) {
			paperColor = default_paper_color;
		}
		else {
			throw OrigamiException.H007;
		}
	}

	/*
	 * TERMINAL COMMANDS
	 */

	private void DIAGNOSTICS() throws Exception {

		Method[] methods = TerminalOrigami.getClass().getMethods();
		for (Method m : methods)  {
			if (m.getName().startsWith("get")) {
				System.out.println(m.getName() + " : " 
						+ printObject(m.invoke(TerminalOrigami)));
			}
		}
	}

	private void LOAD() throws Exception {

		if (accessMode == AccessMode.USER || accessMode == AccessMode.DEBUG) {

			clearHistory();
			history.add("version 1 filename [" + filename + "] load");
			if (filename != null) {

				try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

					String bajtok = "", sor;
					while ((sor = br.readLine()) != null) {
						bajtok += sor + (char) 10;
					}

					executeWithTimeout(bajtok, AccessMode.SCRIPT);
				}
			}
			else {
				throw OrigamiException.H010;
			}

			paramReset();
		}
		else {
			throw OrigamiException.H011;
		}
	}

	private void OPEN() throws Exception {

		if (accessMode == AccessMode.USER || accessMode == AccessMode.DEBUG) {

			clearHistory();
			history.add("version 1 filename [" + filename + "] open");
			if (filename != null) {

				java.util.ArrayList<Byte> bytesb = new java.util.ArrayList<>();
				java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(filename));
				int nxb;
				while ((nxb = fis.read()) != -1) {
					bytesb.add((byte) nxb);
				}
				fis.close();
				byte[] bytes = new byte[bytesb.size()];
				for (int i = 0; i < bytesb.size(); i++) {
					bytes[i] = bytesb.get(i);
				}
				paperColor = default_paper_color;
				int[] rgb = { (paperColor >>> 16) & 0xFF, (paperColor >>> 8) & 0xFF, paperColor & 0xFF };
				TerminalOrigami = OrigamiIO.read_gen2(new java.io.ByteArrayInputStream(bytes), rgb);
				paperColor = rgb[0] * 0x10000 + rgb[1] * 0x100 + rgb[2];
			}
			else {
				throw OrigamiException.H010;
			}
		}
		else {
			throw OrigamiException.H011;
		}
	}

	private void LOAD_TEXTURE() throws Exception {

		if (filename != null) {

			paperTexture = javax.imageio.ImageIO.read(new java.io.File(filename));
			if (paperTexture.getColorModel().hasAlpha()) {
				throw OrigamiException.H013;
			}
		}
		else {
			throw OrigamiException.H010;
		}
	}

	private void UNLOAD_TEXTURE() throws Exception {
		paperTexture = null;
	}

	private void EXPORT_CTM() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null) {
			Export.exportCTM(TerminalOrigami, filename, paperTexture);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void EXPORT_AUTOPDF() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null && title != null) {
			Export.exportPDF(TerminalOrigami, filename, title);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void EXPORT_GIF() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null) {
			Export.exportGIF(TerminalOrigami, TerminalCamera, paperColor, 250, 250, filename);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void EXPORT_REVOLVING_GIF() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null) {
			Export.exportRevolvingGIF(TerminalOrigami, TerminalCamera, paperColor, 250, 250, filename);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void EXPORT_JAR() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null) {
			int[] rgb = { (paperColor >>> 16) & 0xFF, (paperColor >>> 8) & 0xFF, paperColor & 0xFF };
			if (paperColor == default_paper_color) {
				rgb = null;
			}
			Export.exportJAR(TerminalOrigami, filename, rgb);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void EXPORT_PNG() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null) {
			Export.exportPNG(TerminalOrigami, filename);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	private void EXPORT_ORI() throws Exception {

		if (accessMode != AccessMode.USER && accessMode != AccessMode.DEBUG) {
			throw OrigamiException.H011;
		}
		if (filename != null) {
			int[] rgb = { (paperColor >>> 16) & 0xFF, (paperColor >>> 8) & 0xFF, paperColor & 0xFF };
			if (paperColor == default_paper_color) {
				rgb = null;
			}
			OrigamiIO.write_gen2(TerminalOrigami, filename, rgb);
		}
		else {
			throw OrigamiException.H010;
		}

		paramReset();
	}

	static public String obfuscate(String code) {

		// 1. lépés: kommentek eltávolítása
		String result = "";
		boolean phys = true;
		for (int i = 0; i < code.length(); i++) {

			if (code.charAt(i) == '{') {
				phys = false;
			}
			if (phys) {
				result += code.charAt(i);
			}
			if (code.charAt(i) == '}') {
				phys = true;
			}
		}

		// 2. lépés: hiányzó szóközök pótlása
		result = result.replace("[", " [");
		result = result.replace("]", "] ");

		// 3. lépés: tabulátorok és sortörések eltávolítása
		result = result.replace((char) 9, ' ');
		result = result.replace((char) 10, ' ');

		// 4. lépés: többszörös szóközök összevonása
		int tmp_hossz = -1;
		while (tmp_hossz != result.length()) {

			tmp_hossz = result.length();
			result = result.replace("  ", " ");
		}

		// 5. lépés: szögletes zárójelen belüli szóközök cseréje |-ra.
		boolean param = false;
		for (int i = 0; i < result.length() - 1; i++) {

			if (result.charAt(i) == ']') {
				param = false;
			}
			if (param && result.charAt(i) == ' ') {
				result = result.substring(0, i) + "|" + result.substring(i + 1);
			}
			if (result.charAt(i) == '[') {
				param = true;
			}
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#execute(java.lang.String)
	 */
	@Override
	public void execute(String code) throws Exception {

		history.add(code);
		code = obfuscate(code);
		final String[] szavak = code.split(" ");

		try {
			for (int i = 0; i < szavak.length; i++) {
				if (!(szavak[i].contains("[") || szavak[i].contains("]"))) {
					if (Commands.containsKey(szavak[i])) {
						Commands.get(szavak[i]).execute();
					}

					if (Params.containsKey(szavak[i])) {

						ArrayList<String> argumentumok = new ArrayList<>();
						for (int ii = i + 1; ii < szavak.length; ii++) {
							if (Commands.containsKey(szavak[ii]) || Params.containsKey(szavak[ii])) {
								break;
							}
							if (!szavak[ii].equals("")) {
								argumentumok.add(szavak[ii].replace("[", "").replace("]", "").replace("|", " "));
							}
						}

						String[] tombarg = new String[argumentumok.size()];
						for (int iii = 0; iii < argumentumok.size(); iii++) {
							tombarg[iii] = argumentumok.get(iii);
						}

						Params.get(szavak[i]).execute(tombarg);
					}
				}
			}
		}
		catch (Exception e) {

			if (!history.isEmpty()) {
				history.subList(history.size() - 1, history.size()).clear();
			}
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#executeWithTimeout(java.lang.String)
	 */
	@Override
	public void executeWithTimeout(final String code) throws Exception {

		final Stack<Exception> unreportedExceptions = new Stack<>();
		int timeoutSecs = 10;
		String timeout_notif = Dictionary.getString("timeout");

		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<?> future = exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					execute(code);
				}
				catch (Exception e) {
					unreportedExceptions.push(e);
				}
			}
		});

		while (true) {
			try {
				future.get(timeoutSecs, TimeUnit.SECONDS);
				break;
			}
			catch (TimeoutException e) {
				if (activePrompts > 0) {
					break;
				}
				Object[] options = { Dictionary.getString("timeout_stop"), Dictionary.getString("timeout_wait") };
				if (javax.swing.JOptionPane.showOptionDialog(null, timeout_notif, Dictionary.getString("question"),
						javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]) == javax.swing.JOptionPane.YES_OPTION) {
					exec.shutdownNow();
					break;
				}
				else {
					timeoutSecs = 30;
					timeout_notif = Dictionary.getString("+30timeout");
				}
			}
		}

		if (!unreportedExceptions.isEmpty()) {
			throw unreportedExceptions.pop();
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#execute(java.lang.String, origamieditor3d.script.OrigamiScriptTerminalV1.AccessMode)
	 */
	@Override
	public void execute(String code, AccessMode access) throws Exception {

		AccessMode tmp = this.accessMode;
		this.accessMode = access;
		try {
			execute(code);
		}
		catch (Exception ex) {
			this.accessMode = tmp;
			throw ex;
		}
		this.accessMode = tmp;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.script.OrigamiScriptTerminal#executeWithTimeout(java.lang.String, origamieditor3d.script.OrigamiScriptTerminalV1.AccessMode)
	 */
	@Override
	public void executeWithTimeout(String code, AccessMode access) throws Exception {

		AccessMode tmp = this.accessMode;
		this.accessMode = access;
		try {
			executeWithTimeout(code);
		}
		catch (Exception ex) {
			this.accessMode = tmp;
			throw ex;
		}
		this.accessMode = tmp;
	}

	private void executeAll() throws Exception {

		List<String> tmp = new ArrayList<>(history);
		totalReset();
		for (String p : tmp) {
			executeWithTimeout(p);
		}
	}

	private void undo(int steps) throws Exception {

		if (history.size() >= steps) {

			history.subList(history.size() - steps, history.size()).clear();
			AccessMode tmp = this.accessMode;
			this.accessMode = AccessMode.DEBUG;
			executeAll();
			this.accessMode = tmp;
		}
	}
	
	private String printObject(Object o) {
		
		if (o instanceof List) {
			List<?> l = (List<?>)o;
			String s = "[";
			for (int i=0; i<l.size(); i++) {
				s += printObject(l.get(i));
				s += i == l.size()-1 ? "" : ", ";
			}
			s += "]";
			return s;
		}
		if (o.getClass().isArray()) {
			String s = "[";
			for (int i=0; i<Array.getLength(o); i++) {
				s += printObject(Array.get(o, i));
				s += i == Array.getLength(o)-1 ? "" : ", ";
			}
			s += "]";
			return s;
		}
		return o.toString();
	}
}
