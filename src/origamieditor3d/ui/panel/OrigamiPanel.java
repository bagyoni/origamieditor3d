package origamieditor3d.ui.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import origamieditor3d.graphics.Camera;
import origamieditor3d.origami.Geometry;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public OrigamiPanel() {

		super();
		ready_to_paint = false;
		PanelCamera = new Camera(0, 0, 1);
		linerOn = false;
		tracker_x = null;
		tracker_y = null;
		trackerOn = false;
		liner_triangle = new double[3][];
		liner_triangle[0] = null;
		liner_triangle[1] = null;
		liner_triangle[2] = null;
		liner_grab_index = 0;
		alignment_point = null;
		paper_front_color = 0xFFFFFF;
		previewOn = false;
		displaymode = DisplayMode.GRADIENT;
		rulerMode = RulerMode.Normal;
		antialiasOn = true;
	}

	final static private int[] random_front_colors = { 0x000097, 0x24A0DF, 0x397E79, // blue
			0xDF0000, // red
			0x00E371, 0x66E71D, 0x20CB07, // green
			0xFFFFCC, // yellow
			0xA840F4, 0xC40A86 }; // purple
	final private double[][] liner_triangle;
	private Integer ruler_x1, ruler_y1, ruler_x2, ruler_y2;
	private boolean linerOn;

	private double[] tracker_im;
	private int liner_grab_index;
	private int[] alignment_point;
	private int paper_front_color;
	private boolean previewOn;
	private DisplayMode displaymode;
	private Integer protractor_angle;
	private boolean antialiasOn;

	public enum DisplayMode {

		PLAIN, UV, WIREFRAME, GRADIENT
	}

	@Override
	public boolean isTracked() {
		return trackerOn;
	}

	@Override
	public void grabTriangleAt(int vertIndex) {
		liner_grab_index = vertIndex;
	}

	@Override
	public void rulerOn(Camera refcam, int x1, int y1, int x2, int y2) {

		ruler_x1 = x1;
		ruler_y1 = y1;
		ruler_x2 = x2;
		ruler_y2 = y2;
		linerOn = true;
	}

	@Override
	public void rulerOff() {
		linerOn = false;
	}

	public void previewOn() {
		previewOn = true;
	}

	public void previewOff() {
		previewOn = false;
	}

	@Override
	public void setTracker(Camera refkamera, int x, int y) {

		tracker_x = x;
		tracker_y = y;
		try {
			tracker_im = PanelOrigami
					.find3dImageOf(
							new double[] {
									((double) tracker_x - refkamera.getXShift()
											+ new Camera(refkamera.getXShift(), refkamera.getYShift(),
													refkamera.getZoom()).projection0(refkamera.getCamPosition())[0])
											/ refkamera.getZoom(),
									((double) tracker_y - refkamera.getYShift()
											+ new Camera(refkamera.getXShift(), refkamera.getYShift(),
													refkamera.getZoom()).projection0(refkamera.getCamPosition())[1])
											/ refkamera.getZoom() });
		}
		catch (Exception ex) {
		}
		trackerOn = true;
	}

	@Override
	public void resetTracker() {

		tracker_x = null;
		tracker_y = null;
		trackerOn = false;
	}

	@Override
	public void tiltTriangleTo(Camera refkamera, Integer... xy) {

		try {
			int x = xy[0];
			int y = xy[1];
			liner_triangle[liner_grab_index] = PanelOrigami
					.find3dImageOf(
							new double[] {
									((double) x - refkamera.getXShift()
											+ new Camera(refkamera.getXShift(), refkamera.getYShift(),
													refkamera.getZoom()).projection0(refkamera.getCamPosition())[0])
											/ refkamera.getZoom(),
									((double) y - refkamera.getYShift()
											+ new Camera(refkamera.getXShift(), refkamera.getYShift(),
													refkamera.getZoom()).projection0(refkamera.getCamPosition())[1])
											/ refkamera.getZoom() });
		}
		catch (Exception ex) {
			liner_triangle[liner_grab_index] = null;
		}
		liner_grab_index++;
		liner_grab_index %= 3;
	}

	@Override
	public void resetTriangle() {

		liner_grab_index = 0;
		liner_triangle[0] = null;
		liner_triangle[1] = null;
		liner_triangle[2] = null;
	}

	public void setAlignmentPoint(int... point) {
		alignment_point = point;
	}

	public void resetAlignmentPoint() {
		alignment_point = null;
	}

	public void displayProtractor(int angle) {
		protractor_angle = angle;
	}

	public void hideProtractor() {
		protractor_angle = null;
	}

	@Override
	public void reset() {

		tracker_x = null;
		tracker_y = null;
		liner_grab_index = 0;
		liner_triangle[0] = null;
		liner_triangle[1] = null;
		liner_triangle[2] = null;
		trackerOn = false;
		alignment_point = null;
		protractor_angle = null;
	}

	public void setFrontColor(int rgb) {
		paper_front_color = rgb;
	}

	public int getFrontColor() {
		return paper_front_color;
	}

	public void randomizeFrontColor() {
		paper_front_color = random_front_colors[new Random().nextInt(random_front_colors.length)];
	}

	public void setDisplaymode(DisplayMode value) {
		displaymode = value;
	}

	public DisplayMode displaymode() {
		return displaymode;
	}

	public void setTexture(BufferedImage tex) throws Exception {
		PanelCamera.setTexture(tex);
	}

	public void antialiasOn() {

		antialiasOn = true;
		repaint();
	}

	public void antialiasOff() {

		antialiasOn = false;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (ready_to_paint) {

			Graphics2D gx2d = (Graphics2D) g;

			switch (displaymode) {

			case UV:
				PanelCamera.drawTexture(g, this.getWidth(), this.getHeight());
				break;

			case GRADIENT:
				PanelCamera.drawGradient(g, paper_front_color, PanelOrigami);
				if (antialiasOn) {
					gx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
				PanelCamera.drawEdges(g, new Color(0, 0, 0, .5f), PanelOrigami);
				break;

			case PLAIN:
				PanelCamera.drawFaces(g, paper_front_color, PanelOrigami);
				if (antialiasOn) {
					gx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
				PanelCamera.drawEdges(g, new Color(0, 0, 0, .5f), PanelOrigami);
				break;

			case WIREFRAME:
				if (antialiasOn) {
					gx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
				PanelCamera.drawEdges(g, new Color(0, 0, 0, .5f), PanelOrigami);
				break;
			}

			gx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		if (alignment_point != null) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(2));
			g2.drawRect(alignment_point[0] - 3, alignment_point[1] - 3, 6, 6);
			g2.setStroke(new BasicStroke(1));
		}

		g.setColor(Color.red);
		if (linerOn) {

			if (previewOn) {

				double pontX = ((double) ruler_x2 - this.PanelCamera.getXShift()) / this.PanelCamera.getZoom();
				double pontY = ((double) ruler_y2 - this.PanelCamera.getYShift()) / this.PanelCamera.getZoom();
				double pont1X = ((double) ruler_x1 - this.PanelCamera.getXShift()) / this.PanelCamera.getZoom();
				double pont1Y = ((double) ruler_y1 - this.PanelCamera.getYShift()) / this.PanelCamera.getZoom();

				double[] vonalzoNV = new double[] {
						this.PanelCamera.getXScale()[0] * (ruler_y2 - ruler_y1)
								+ this.PanelCamera.getYScale()[0] * (ruler_x1 - ruler_x2),
						this.PanelCamera.getXScale()[1] * (ruler_y2 - ruler_y1)
								+ this.PanelCamera.getYScale()[1] * (ruler_x1 - ruler_x2),
						this.PanelCamera.getXScale()[2] * (ruler_y2 - ruler_y1)
								+ this.PanelCamera.getYScale()[2] * (ruler_x1 - ruler_x2) };
				double[] vonalzoPT = new double[] {
						this.PanelCamera.getXScale()[0] / this.PanelCamera.getZoom() * pontX
								+ this.PanelCamera.getYScale()[0] / this.PanelCamera.getZoom() * pontY
								+ this.PanelCamera.getCamPosition()[0],
						this.PanelCamera.getXScale()[1] / this.PanelCamera.getZoom() * pontX
								+ this.PanelCamera.getYScale()[1] / this.PanelCamera.getZoom() * pontY
								+ this.PanelCamera.getCamPosition()[1],
						this.PanelCamera.getXScale()[2] / this.PanelCamera.getZoom() * pontX
								+ this.PanelCamera.getYScale()[2] / this.PanelCamera.getZoom() * pontY
								+ this.PanelCamera.getCamPosition()[2] };
				double[] vonalzoPT1 = new double[] {
						this.PanelCamera.getXScale()[0] / this.PanelCamera.getZoom() * pont1X
								+ this.PanelCamera.getYScale()[0] / this.PanelCamera.getZoom() * pont1Y
								+ this.PanelCamera.getCamPosition()[0],
						this.PanelCamera.getXScale()[1] / this.PanelCamera.getZoom() * pont1X
								+ this.PanelCamera.getYScale()[1] / this.PanelCamera.getZoom() * pont1Y
								+ this.PanelCamera.getCamPosition()[1],
						this.PanelCamera.getXScale()[2] / this.PanelCamera.getZoom() * pont1X
								+ this.PanelCamera.getYScale()[2] / this.PanelCamera.getZoom() * pont1Y
								+ this.PanelCamera.getCamPosition()[2] };
				if (rulerMode == RulerMode.Neusis) {
					vonalzoNV = Geometry.vectorDiff(vonalzoPT, vonalzoPT1);
				}

				PanelCamera.drawPreview(g, Color.green, PanelOrigami, vonalzoPT, vonalzoNV);
			}

			g.setColor(Color.red);
			if (rulerMode == RulerMode.Neusis) {
				int maxdim = Math.max(this.getWidth(), this.getHeight())
						/ Math.max(Math.max(Math.abs(ruler_y1 - ruler_y2), Math.abs(ruler_x1 - ruler_x2)), 1) + 1;
				g.drawLine(ruler_x2 + maxdim * (ruler_y1 - ruler_y2), ruler_y2 + maxdim * (ruler_x2 - ruler_x1),
						ruler_x2 - maxdim * (ruler_y1 - ruler_y2), ruler_y2 - maxdim * (ruler_x2 - ruler_x1));
			}
			else {
				int maxdim = Math.max(this.getWidth(), this.getHeight())
						/ Math.max(Math.max(Math.abs(ruler_y1 - ruler_y2), Math.abs(ruler_x1 - ruler_x2)), 1) + 1;
				g.drawLine(ruler_x1 + maxdim * (ruler_x1 - ruler_x2), ruler_y1 + maxdim * (ruler_y1 - ruler_y2),
						ruler_x2 - maxdim * (ruler_x1 - ruler_x2), ruler_y2 - maxdim * (ruler_y1 - ruler_y2));
			}
		}
		if (trackerOn) {
			int x = (int) (PanelCamera.projection(tracker_im)[0]) + PanelCamera.getXShift();
			int y = (int) (PanelCamera.projection(tracker_im)[1]) + PanelCamera.getYShift();
			g.drawLine(x - 5, y, x + 5, y);
			g.drawLine(x, y - 5, x, y + 5);
		}

		g.setColor(Color.magenta);
		((Graphics2D) g).setStroke(new BasicStroke(2));

		if (liner_triangle[0] != null) {
			int x = (int) (PanelCamera.projection(liner_triangle[0])[0]) + PanelCamera.getXShift();
			int y = (int) (PanelCamera.projection(liner_triangle[0])[1]) + PanelCamera.getYShift();
			g.drawLine(x - 3, y + 3, x + 3, y - 3);
			g.drawLine(x - 3, y - 3, x + 3, y + 3);
		}
		if (liner_triangle[1] != null) {
			int x = (int) (PanelCamera.projection(liner_triangle[1])[0]) + PanelCamera.getXShift();
			int y = (int) (PanelCamera.projection(liner_triangle[1])[1]) + PanelCamera.getYShift();
			g.drawLine(x - 3, y + 3, x + 3, y - 3);
			g.drawLine(x - 3, y - 3, x + 3, y + 3);
		}
		if (liner_triangle[2] != null) {
			int x = (int) (PanelCamera.projection(liner_triangle[2])[0]) + PanelCamera.getXShift();
			int y = (int) (PanelCamera.projection(liner_triangle[2])[1]) + PanelCamera.getYShift();
			g.drawLine(x - 3, y + 3, x + 3, y - 3);
			g.drawLine(x - 3, y - 3, x + 3, y + 3);
		}
		((Graphics2D) g).setStroke(new BasicStroke(1));

		if (protractor_angle != null) {
			drawProtractor(g, protractor_angle);
		}
	}

	private void drawProtractor(Graphics g, int angle) {

		angle -= 90;
		while (angle < 0) {
			angle += 360;
		}

		int width = getWidth();
		int height = getHeight();
		int diam = Math.min(width, height) / 2;
		g.setColor(new Color(255, 255, 255, 170));
		g.fillRect(0, 0, width, height);

		g.setColor(Color.red);
		g.drawLine((int) (width / 2 + Math.cos(angle * Math.PI / 180) * diam / 2),
				(int) (height / 2 + Math.sin(angle * Math.PI / 180) * diam / 2), (int) (width / 2), (int) (height / 2));

		g.setColor(Color.black);
		g.drawOval((width - diam) / 2, (height - diam) / 2, diam, diam);

		for (int i = 0; i < 360; i += 5) {

			int notch = i % 180 == 0 ? diam / 6 : i % 90 == 0 ? diam / 8 : i % 45 == 0 ? diam / 10 : diam / 14;
			g.drawLine((int) (width / 2 + Math.cos(i * Math.PI / 180) * diam / 2),
					(int) (height / 2 + Math.sin(i * Math.PI / 180) * diam / 2),
					(int) (width / 2 + Math.cos(i * Math.PI / 180) * (diam / 2 - notch)),
					(int) (height / 2 + Math.sin(i * Math.PI / 180) * (diam / 2 - notch)));

		}
		g.setFont(g.getFont().deriveFont(12.0f));
		g.drawString("0°", width / 2 - 5, (height - diam) / 2 - 5);
		g.drawString("90°", (width + diam) / 2 + 5, height / 2 + 5);
		g.drawString("180°", width / 2 - 10, (height + diam) / 2 + 15);
		g.drawString("-90°", (width - diam) / 2 - 30, height / 2 + 5);

		if (angle % 90 != 0) {
			g.drawString((angle < 90 ? angle + 90 : angle - 270) + "°",
					(int) (width / 2 + Math.cos(angle * Math.PI / 180) * diam / 2
							+ (angle > 90 && angle < 270 ? -30 : 10)),
					(int) (height / 2 + Math.sin(angle * Math.PI / 180) * diam / 2) + (angle < 180 ? 15 : -5));
		}
	}

	@Override
	public Point getToolTipLocation(MouseEvent e) {

		Point pt = e.getPoint();
		pt.y += Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).height / 2;
		pt.x += Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).width / 2;
		return pt;
	}

	public double[] getRulerNormalvector() {

		if (linerOn && rulerMode == RulerMode.Normal) {

			double[] rulerNV = new double[] {
					PanelCamera.getXScale()[0] * (ruler_y2 - ruler_y1)
							+ PanelCamera.getYScale()[0] * (ruler_x1 - ruler_x2),
					PanelCamera.getXScale()[1] * (ruler_y2 - ruler_y1)
							+ PanelCamera.getYScale()[1] * (ruler_x1 - ruler_x2),
					PanelCamera.getXScale()[2] * (ruler_y2 - ruler_y1)
							+ PanelCamera.getYScale()[2] * (ruler_x1 - ruler_x2) };

			if (Geometry.scalarProduct(PanelCamera.getCamPosition(), rulerNV)
					- Geometry.scalarProduct(getRulerPoint(), rulerNV) > 0) {
				rulerNV = new double[] { -rulerNV[0], -rulerNV[1], -rulerNV[2] };
			}
			return rulerNV;
		}
		if (linerOn && rulerMode == RulerMode.Neusis) {

			double[] rulerPT = getRulerPoint();
			double[] rulerNV = Geometry.vectorDiff(rulerPT, getRulerPoint1());
			if (Geometry.scalarProduct(PanelCamera.getCamPosition(), rulerNV)
					- Geometry.scalarProduct(rulerPT, rulerNV) > 0) {
				rulerNV = new double[] { -rulerNV[0], -rulerNV[1], -rulerNV[2] };
			}
			return rulerNV;
		}
		return null;
	}

	public double[] getRulerPoint() {

		if (linerOn) {

			double pontX = ((double) ruler_x2 - PanelCamera.getXShift()) / PanelCamera.getZoom();
			double pontY = ((double) ruler_y2 - PanelCamera.getYShift()) / PanelCamera.getZoom();

			double[] rulerPT = new double[] { PanelCamera.getXScale()[0] / PanelCamera.getZoom() * pontX
					+ PanelCamera.getYScale()[0] / PanelCamera.getZoom() * pontY + PanelCamera.getCamPosition()[0],
					PanelCamera.getXScale()[1] / PanelCamera.getZoom() * pontX
							+ PanelCamera.getYScale()[1] / PanelCamera.getZoom() * pontY
							+ PanelCamera.getCamPosition()[1],
					PanelCamera.getXScale()[2] / PanelCamera.getZoom() * pontX
							+ PanelCamera.getYScale()[2] / PanelCamera.getZoom() * pontY
							+ PanelCamera.getCamPosition()[2] };

			return rulerPT;
		}
		return null;
	}

	private double[] getRulerPoint1() {

		if (linerOn) {

			double pont1X = ((double) ruler_x1 - PanelCamera.getXShift()) / PanelCamera.getZoom();
			double pont1Y = ((double) ruler_y1 - PanelCamera.getYShift()) / PanelCamera.getZoom();

			double[] rulerPT1 = new double[] { PanelCamera.getXScale()[0] / PanelCamera.getZoom() * pont1X
					+ PanelCamera.getYScale()[0] / PanelCamera.getZoom() * pont1Y + PanelCamera.getCamPosition()[0],
					PanelCamera.getXScale()[1] / PanelCamera.getZoom() * pont1X
							+ PanelCamera.getYScale()[1] / PanelCamera.getZoom() * pont1Y
							+ PanelCamera.getCamPosition()[1],
					PanelCamera.getXScale()[2] / PanelCamera.getZoom() * pont1X
							+ PanelCamera.getYScale()[2] / PanelCamera.getZoom() * pont1Y
							+ PanelCamera.getCamPosition()[2] };

			return rulerPT1;
		}
		return null;
	}

}
