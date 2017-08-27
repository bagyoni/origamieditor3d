package origamieditor3d.ui.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import origamieditor3d.graphics.Camera;
import origamieditor3d.origami.Geometry;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class PaperPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private Integer tracker_x, tracker_y;
	private double[] tracker_im;

	private boolean trackerOn;
	private double[] liner_point, liner_normal;
	private Integer[][] liner_triangle;
	private int liner_grab_index;

	public Integer tracker_x() {
		return tracker_x;
	}

	public Integer tracker_y() {
		return tracker_y;
	}

	@Override
	public boolean isTracked() {
		return trackerOn;
	}

	public Integer[][] linerTriangle() {
		return liner_triangle;
	}

	@Override
	public void grabTriangleAt(int vertIndex) {
		liner_grab_index = vertIndex;
	}

	public PaperPanel() {

		super();
		PanelCamera = new Camera(0, 0, 1);
		ready_to_paint = false;
		tracker_x = null;
		tracker_y = null;
		trackerOn = false;
		liner_triangle = new Integer[3][];
		liner_triangle[0] = null;
		liner_triangle[1] = null;
		liner_triangle[2] = null;
		liner_grab_index = 0;
		liner_point = null;
		liner_normal = null;
		rulerMode = RulerMode.Normal;
	}

	@Override
	public void setTracker(Camera refkamera, int x, int y) {

		tracker_x = x;
		tracker_y = y;
		tracker_im = new double[] {
				((double) tracker_x - refkamera.getXShift()
						+ new Camera(refkamera.getXShift(), refkamera.getYShift(), refkamera.getZoom())
								.projection0(refkamera.getCamPosition())[0])
						/ refkamera.getZoom(),
				((double) tracker_y - refkamera.getYShift()
						+ new Camera(refkamera.getXShift(), refkamera.getYShift(), refkamera.getZoom())
								.projection0(refkamera.getCamPosition())[1])
						/ refkamera.getZoom(),
				0 };
		trackerOn = true;
	}

	@Override
	public void resetTracker() {

		tracker_x = null;
		tracker_y = null;
		trackerOn = false;
	}

	@Override
	public void rulerOn(Camera refcam, int x1, int y1, int x2, int y2) {

		double pontX = ((double) x2 - refcam.getXShift()) / refcam.getZoom();
		double pontY = ((double) y2 - refcam.getYShift()) / refcam.getZoom();
		double pont1X = ((double) x1 - refcam.getXShift()) / refcam.getZoom();
		double pont1Y = ((double) y1 - refcam.getYShift()) / refcam.getZoom();

		double[] vonalzoNV = new double[] { refcam.getXScale()[0] * (y2 - y1) + refcam.getYScale()[0] * (x1 - x2),
				refcam.getXScale()[1] * (y2 - y1) + refcam.getYScale()[1] * (x1 - x2),
				refcam.getXScale()[2] * (y2 - y1) + refcam.getYScale()[2] * (x1 - x2) };
		double[] vonalzoPT = new double[] {
				refcam.getXScale()[0] / refcam.getZoom() * pontX + refcam.getYScale()[0] / refcam.getZoom() * pontY
						+ refcam.getCamPosition()[0],
				refcam.getXScale()[1] / refcam.getZoom() * pontX + refcam.getYScale()[1] / refcam.getZoom() * pontY
						+ refcam.getCamPosition()[1],
				refcam.getXScale()[2] / refcam.getZoom() * pontX + refcam.getYScale()[2] / refcam.getZoom() * pontY
						+ refcam.getCamPosition()[2] };
		double[] vonalzoPT1 = new double[] {
				refcam.getXScale()[0] / refcam.getZoom() * pont1X + refcam.getYScale()[0] / refcam.getZoom() * pont1Y
						+ refcam.getCamPosition()[0],
				refcam.getXScale()[1] / refcam.getZoom() * pont1X + refcam.getYScale()[1] / refcam.getZoom() * pont1Y
						+ refcam.getCamPosition()[1],
				refcam.getXScale()[2] / refcam.getZoom() * pont1X + refcam.getYScale()[2] / refcam.getZoom() * pont1Y
						+ refcam.getCamPosition()[2] };
		if (rulerMode == RulerMode.Neusis) {
			vonalzoNV = Geometry.vectorDiff(vonalzoPT, vonalzoPT1);
		}
		liner_point = vonalzoPT;
		liner_normal = vonalzoNV;
	}

	@Override
	public void rulerOff() {
		liner_point = (liner_normal = null);
	}

	@Override
	public void tiltTriangleTo(Camera refkamera, Integer... xy) {

		liner_triangle[liner_grab_index] = xy;
		liner_grab_index++;
		liner_grab_index %= 3;

		if (liner_triangle[0] != null && liner_triangle[1] != null && liner_triangle[2] != null) {

			try {

				double[] pt1 = PanelOrigami
						.find3dImageOf(
								new double[] {
										((double) liner_triangle[0][0] - PanelCamera.getXShift()
												+ PanelCamera.projection0(PanelCamera.getCamPosition())[0])
												/ PanelCamera.getZoom(),
										((double) liner_triangle[0][1] - PanelCamera.getYShift()
												+ PanelCamera.projection0(PanelCamera.getCamPosition())[1])
												/ PanelCamera.getZoom() }),
						pt2 = PanelOrigami.find3dImageOf(new double[] {
								((double) liner_triangle[1][0] - PanelCamera.getXShift()
										+ PanelCamera.projection0(PanelCamera.getCamPosition())[0])
										/ PanelCamera.getZoom(),
								((double) liner_triangle[1][1] - PanelCamera.getYShift()
										+ PanelCamera.projection0(PanelCamera.getCamPosition())[1])
										/ PanelCamera.getZoom() }),
						pt3 = PanelOrigami.find3dImageOf(new double[] {
								((double) liner_triangle[2][0] - PanelCamera.getXShift()
										+ PanelCamera.projection0(PanelCamera.getCamPosition())[0])
										/ PanelCamera.getZoom(),
								((double) liner_triangle[2][1] - PanelCamera.getYShift()
										+ PanelCamera.projection0(PanelCamera.getCamPosition())[1])
										/ PanelCamera.getZoom() });

				if (rulerMode == RulerMode.Planethrough) {
					if (Geometry.vectorLength(Geometry.crossProduct(Geometry.vectorDiff(pt2, pt1),
							Geometry.vectorDiff(pt3, pt1))) != 0d) {

						liner_point = pt1;
						liner_normal = Geometry.crossProduct(Geometry.vectorDiff(pt2, pt1),
								Geometry.vectorDiff(pt3, pt1));
					}
					else {
						rulerOff();
					}
				}
				else if (rulerMode == RulerMode.Angle_bisector) {
					liner_point = pt2;
					liner_normal = Geometry.vectorDiff(Geometry.length_to_100(Geometry.vectorDiff(pt1, pt2)),
							Geometry.length_to_100(Geometry.vectorDiff(pt3, pt2)));
					if (Geometry.vectorLength(liner_normal) == 0.) {
						rulerOff();
					}
				}
			}
			catch (Exception ex) {
			}
		}
	}

	@Override
	public void resetTriangle() {

		liner_grab_index = 0;
		liner_triangle[0] = null;
		liner_triangle[1] = null;
		liner_triangle[2] = null;
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
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (ready_to_paint) {
			try {
				PanelCamera.drawCreasePattern(g, Color.black, PanelOrigami);
			}
			catch (Exception ex) {
			}
		}
		g.setColor(Color.red);
		if (trackerOn) {
			int x = (int) (PanelCamera.projection(tracker_im)[0]) + PanelCamera.getXShift();
			int y = (int) (PanelCamera.projection(tracker_im)[1]) + PanelCamera.getYShift();
			g.drawLine(x - 5, y, x + 5, y);
			g.drawLine(x, y - 5, x, y + 5);
		}

		if (liner_point != null) {
			PanelCamera.draw2dFoldingLine(g, Color.red, liner_point, liner_normal, PanelOrigami);
		}

		g.setColor(Color.magenta);
		((Graphics2D) g).setStroke(new BasicStroke(2));

		if (liner_triangle[0] != null) {
			g.drawLine(liner_triangle[0][0] - 3, liner_triangle[0][1] - 3, liner_triangle[0][0] + 3,
					liner_triangle[0][1] + 3);
			g.drawLine(liner_triangle[0][0] - 3, liner_triangle[0][1] + 3, liner_triangle[0][0] + 3,
					liner_triangle[0][1] - 3);
		}
		if (liner_triangle[1] != null) {
			g.drawLine(liner_triangle[1][0] - 3, liner_triangle[1][1] - 3, liner_triangle[1][0] + 3,
					liner_triangle[1][1] + 3);
			g.drawLine(liner_triangle[1][0] - 3, liner_triangle[1][1] + 3, liner_triangle[1][0] + 3,
					liner_triangle[1][1] - 3);
		}
		if (liner_triangle[2] != null) {
			g.drawLine(liner_triangle[2][0] - 3, liner_triangle[2][1] - 3, liner_triangle[2][0] + 3,
					liner_triangle[2][1] + 3);
			g.drawLine(liner_triangle[2][0] - 3, liner_triangle[2][1] + 3, liner_triangle[2][0] + 3,
					liner_triangle[2][1] - 3);
		}
	}

	@Override
	public Point getToolTipLocation(MouseEvent e) {

		Point pt = e.getPoint();
		pt.y += Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).height / 2;
		pt.x += Toolkit.getDefaultToolkit().getBestCursorSize(10, 10).width / 2;
		return pt;
	}
}
