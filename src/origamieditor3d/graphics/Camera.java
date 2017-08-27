package origamieditor3d.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import origamieditor3d.origami.Geometry;
import origamieditor3d.origami.OrigamiGen1;
import origamieditor3d.origami.OrigamiException;
import origamieditor3d.origami.Origami;
import java.awt.image.DataBufferInt;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.image.DataBufferByte;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 * @see OrigamiGen1
 */
public class Camera {

	final static public int paper_back_color = 0xD8D2B0;
	final static public int maximal_zoom = 4;
	final static public double[] default_camera_pos = { 200, 200, 0 };
	final static public double[] default_camera_dir = { 0, 0, 1 };
	final static public double[] default_axis_x = { 1, 0, 0 };
	final static public double[] default_axis_y = { 0, 1, 0 };

	public Camera(int x, int y, double zoom) {

		camPosition = default_camera_pos.clone();
		camDirection = default_camera_dir.clone();
		xAxis = default_axis_x.clone();
		yAxis = default_axis_y.clone();
		xShift = x;
		yShift = y;
		this.zoom = zoom;
	}

	private double[] camPosition;
	private double[] camDirection;
	private double[] xAxis;
	private double[] yAxis;
	private int xShift = 230;
	private int yShift = 230;
	private double zoom = 1.0;
	private double[][] spaceBuffer;
	private BufferedImage texture;
	private byte orientation = 0;

	public double[] projection0(double[] point) {

		double[] basepoint = Geometry.linePlaneIntersection(point, camDirection, camPosition,
				Geometry.scalarMultiple(camDirection, zoom));

		double[] img = {
				basepoint[0] * xAxis[0] * zoom + basepoint[1] * xAxis[1] * zoom + basepoint[2] * xAxis[2] * zoom,
				basepoint[0] * yAxis[0] * zoom + basepoint[1] * yAxis[1] * zoom + basepoint[2] * yAxis[2] * zoom };
		return img;
	}

	public double[] projection(double[] point) {

		double[] img = { projection0(point)[0] - projection0(camPosition)[0],
				projection0(point)[1] - projection0(camPosition)[1] };
		return img;
	}

	public double[] deprojection(double... xy) {

		double X = (xy[0] - xShift + new Camera(xShift, yShift, zoom).projection0(camPosition)[0]) / zoom;

		double Y = (xy[1] - yShift + new Camera(xShift, yShift, zoom).projection0(camPosition)[1]) / zoom;

		return new double[] { X, Y };
	}

	public void rotate(float x, float y) {

		double sinX = Math.sin(x * Math.PI / 180);
		double cosX = Math.cos(x * Math.PI / 180);

		camDirection = Geometry
				.normalizeVector(Geometry.rotation(camDirection, Geometry.nullvector, yAxis, sinX, cosX));
		xAxis = Geometry.normalizeVector(Geometry.rotation(xAxis, Geometry.nullvector, yAxis, sinX, cosX));

		double sinY = Math.sin(y * Math.PI / 180);
		double cosY = Math.cos(y * Math.PI / 180);

		camDirection = Geometry
				.normalizeVector(Geometry.rotation(camDirection, Geometry.nullvector, xAxis, sinY, cosY));
		yAxis = Geometry.normalizeVector(Geometry.rotation(yAxis, Geometry.nullvector, xAxis, sinY, cosY));
	}

	public List<int[]> alignmentPoints(Origami origami, int... denoms) {

		List<int[]> nsectors = new ArrayList<>();

		for (int i = 0; i < origami.getPolygonsSize(); i++) {
			if (origami.isNonDegenerate(i)) {
				for (int ii = 0; ii < origami.getPolygons().get(i).size(); ii++) {

					double[] vert = origami.getVertices().get(origami.getPolygons().get(i).get(ii));
					nsectors.add(new int[] { (int) projection(vert)[0], (int) projection(vert)[1] });
				}
			}
		}
		for (int n : denoms) {
			for (int i = 0; i < origami.getPolygonsSize(); i++) {
				if (origami.isNonDegenerate(i)) {
					for (int ii = 0; ii < origami.getPolygons().get(i).size() - 1; ii++) {

						double[] p1 = origami.getVertices().get(origami.getPolygons().get(i).get(ii));
						double[] p2 = origami.getVertices().get(origami.getPolygons().get(i).get(ii + 1));
						for (int j = 1; j < n; j++) {

							double[] nsect = new double[] { (p1[0] * j + p2[0] * (n - j)) / n,
									(p1[1] * j + p2[1] * (n - j)) / n, (p1[2] * j + p2[2] * (n - j)) / n };
							nsectors.add(new int[] { (int) projection(nsect)[0], (int) projection(nsect)[1] });
						}
					}

					double[] last1 = origami.getVertices()
							.get(origami.getPolygons().get(i).get(origami.getPolygons().get(i).size() - 1));
					double[] last2 = origami.getVertices().get(origami.getPolygons().get(i).get(0));
					for (int j = 1; j < n; j++) {

						double[] nsect = new double[] { (last1[0] * j + last2[0] * (n - j)) / n,
								(last1[1] * j + last2[1] * (n - j)) / n, (last1[2] * j + last2[2] * (n - j)) / n };
						nsectors.add(new int[] { (int) projection(nsect)[0], (int) projection(nsect)[1] });
					}
				}
			}
		}

		return nsectors;
	}

	public List<int[]> alignmentPoints2d(Origami origami) {

		List<int[]> vissza = new ArrayList<>();
		for (int i = 0; i < origami.getVerticesSize(); i++) {

			vissza.add(new int[] { (int) projection(origami.getVertices2d().get(i))[0],
					(int) projection(origami.getVertices2d().get(i))[1] });
		}

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (origami.isNonDegenerate(i)) {

				for (int ii = 0; ii < origami.getPolygons().get(i).size() - 1; ii++) {

					double[] pont1 = origami.getVertices2d().get(origami.getPolygons().get(i).get(ii));
					double[] pont2 = origami.getVertices2d().get(origami.getPolygons().get(i).get(ii + 1));
					double[] felezo = Geometry.midpoint(pont1, pont2);

					vissza.add(new int[] { (int) projection(felezo)[0], (int) projection(felezo)[1] });
				}

				double[] Upont1 = origami.getVertices2d()
						.get(origami.getPolygons().get(i).get(origami.getPolygons().get(i).size() - 1));
				double[] Upont2 = origami.getVertices2d().get(origami.getPolygons().get(i).get(0));
				double[] Ufelezo = Geometry.midpoint(Upont1, Upont2);

				vissza.add(new int[] { (int) projection(Ufelezo)[0], (int) projection(Ufelezo)[1] });
			}
		}

		return vissza;
	}

	public double circumscribedSquareSize(Origami origami) {

		Double t, b, l, r;
		t = (b = (l = (r = null)));
		for (double[] vert : origami.getVertices()) {

			double[] proj = projection(vert);
			t = t == null ? proj[1] : proj[1] < t ? proj[1] : t;
			b = b == null ? proj[1] : proj[1] > b ? proj[1] : b;
			l = l == null ? proj[0] : proj[0] < l ? proj[0] : t;
			r = r == null ? proj[0] : proj[0] > r ? proj[0] : r;
		}
		return 2 * Math.max(Math.abs(t), Math.max(Math.abs(b), Math.max(Math.abs(l), Math.abs(r))));
	}

	public void drawEdges(Graphics canvas, Color color, Origami origami) {

		canvas.setColor(color);

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (isDrawable(i, origami)) {

				Polygon edges = new Polygon();

				for (int ii = 0; ii < origami.getPolygons().get(i).size(); ii++) {

					edges.addPoint(
							(short) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[0])
									+ xShift,
							(short) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[1])
									+ yShift);
				}
				canvas.drawPolygon(edges);
			}
		}
	}

	public String drawEdges(int x, int y, Origami origami) {

		String edges = "1 w ";

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (isDrawable(i, origami)) {

				edges += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[0]) + x);
				edges += " ";
				edges += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[1]) + y);
				edges += " m ";

				for (int ii = 1; ii < origami.getPolygons().get(i).size(); ii++) {
					edges += Integer.toString(
							(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[0]) + x);
					edges += " ";
					edges += Integer.toString(
							(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[1]) + y);
					edges += " l ";
				}
				edges += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[0]) + x);
				edges += " ";
				edges += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[1]) + y);
				edges += " l S ";
			}
		}

		return edges;
	}

	public void drawPreview(Graphics canvas, Color color, Origami origami, double[] ppoint, double[] pnormal) {

		double[] cp_tmp = camPosition;
		double[] cd_tmp = camDirection;
		double[] x_tmp = xAxis;
		double[] y_tmp = yAxis;

		camPosition = Geometry.reflection(camPosition, ppoint, pnormal);
		camDirection = Geometry.reflection(camDirection, Geometry.nullvector, pnormal);
		xAxis = Geometry.reflection(xAxis, Geometry.nullvector, pnormal);
		yAxis = Geometry.reflection(yAxis, Geometry.nullvector, pnormal);

		drawEdges(canvas, color, origami);

		camPosition = cp_tmp;
		camDirection = cd_tmp;
		xAxis = x_tmp;
		yAxis = y_tmp;
	}

	public String drawSelection(int x, int y, double[] ppoint, double[] pnormal, int polygonIndex, Origami origami) {

		String selection = "0.8 0.8 0.8 rg ";

		List<Integer> kijeloles = origami.polygonSelect(ppoint, pnormal, polygonIndex);
		for (int i : kijeloles) {

			if (isDrawable(i, origami)) {

				selection += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[0]) + x);
				selection += " ";
				selection += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[1]) + y);
				selection += " m ";

				for (int ii = 1; ii < origami.getPolygons().get(i).size(); ii++) {
					selection += Integer.toString(
							(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[0]) + x);
					selection += " ";
					selection += Integer.toString(
							(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[1]) + y);
					selection += " l ";
				}
				selection += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[0]) + x);
				selection += " ";
				selection += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[1]) + y);
				selection += " l f ";
			}
		}

		return selection;
	}

	public boolean isDrawable(int polygonIndex, Origami origami) {
		return origami.isStrictlyNonDegenerate(polygonIndex);
	}

	public boolean isDrawable(int polygonIndex, Origami origami, int... ref) {

		if (origami.getPolygons().get(polygonIndex).size() > 2) {

			double maxarea = 0;
			for (int i = 0; i < origami.getPolygons().get(polygonIndex).size(); i++) {

				int pont1ind = origami.getPolygons().get(polygonIndex).get(i);
				int pont0ind = origami.getPolygons().get(polygonIndex)
						.get((i + 1) % origami.getPolygons().get(polygonIndex).size());
				int pont2ind = origami.getPolygons().get(polygonIndex)
						.get((i + 2) % origami.getPolygons().get(polygonIndex).size());
				double area = Geometry.vectorLength(Geometry.crossProduct(
						Geometry.vectorDiff(origami.getVertices().get(pont1ind), origami.getVertices().get(pont0ind)),
						Geometry.vectorDiff(origami.getVertices().get(pont2ind), origami.getVertices().get(pont0ind))));
				if (area > maxarea) {
					maxarea = area;
					ref[1] = pont1ind;
					ref[2] = pont2ind;
					ref[0] = pont0ind;
				}
			}
			if (maxarea > 1) {
				return true;
			}
		}
		return false;
	}

	public void drawGradient(Graphics canvas, int rgb, Origami origami) {

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (isDrawable(i, origami)) {

				double[] fnormal = Geometry.crossProduct(
						Geometry.vectorDiff(origami.getVertices().get(origami.getPolygons().get(i).get(0)),
								origami.getVertices().get(origami.getPolygons().get(i).get(1))),
						Geometry.vectorDiff(origami.getVertices().get(origami.getPolygons().get(i).get(0)),
								origami.getVertices().get(origami.getPolygons().get(i).get(2))));

				double nv_len = Geometry.vectorLength(fnormal);
				if (nv_len != 0) {
					fnormal[0] = fnormal[0] / nv_len;
					fnormal[1] = fnormal[1] / nv_len;
					fnormal[2] = fnormal[2] / nv_len;
				}

				double alpha = 1 - Math.abs(Geometry.scalarProduct(camDirection, fnormal));
				int color = Geometry.scalarProduct(camDirection, fnormal) > 0 ? (rgb & 0xFFFFFF) : paper_back_color;

				Polygon path = new Polygon();

				double[] close = null, far = null;

				for (int ii = 0; ii < origami.getPolygons().get(i).size(); ii++) {

					path.addPoint(
							(short) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[0])
									+ xShift,
							(short) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[1])
									+ yShift);

					double sc = Geometry.scalarProduct(origami.getVertices().get(origami.getPolygons().get(i).get(ii)),
							camDirection);
					if (close == null ? true : sc > Geometry.scalarProduct(close, camDirection)) {
						close = origami.getVertices().get(origami.getPolygons().get(i).get(ii));
					}
					if (far == null ? true : sc < Geometry.scalarProduct(far, camDirection)) {
						far = origami.getVertices().get(origami.getPolygons().get(i).get(ii));
					}
				}

				double[] grad_dir = Geometry.crossProduct(fnormal, Geometry.crossProduct(fnormal, camDirection));
				close = Geometry.linePlaneIntersection(far, grad_dir, close, camDirection);

				double dclose = Geometry.scalarProduct(Geometry.vectorDiff(close, camPosition), camDirection)
						/ Math.max(origami.circumscribedSquareSize() * Math.sqrt(2) / 2, 1);
				double dfar = Geometry.scalarProduct(Geometry.vectorDiff(far, camPosition), camDirection)
						/ Math.max(origami.circumscribedSquareSize() * Math.sqrt(2) / 2, 1);
				float[] hsb = Color.RGBtoHSB((color >>> 16) % 0x100, (color >>> 8) % 0x100, color % 0x100, null);

				int rgb1 = Color.HSBtoRGB(hsb[0], Math.max(Math.min((float) (.5 - dclose * .5), 1f), 0f), 1f)
						& 0xFFFFFF;
				int rgb2 = Color.HSBtoRGB(hsb[0], Math.max(Math.min((float) (.5 - dfar * .5), 1f), 0f), hsb[2])
						& 0xFFFFFF;

				Color c1, c2;
				try {
					c1 = new Color((rgb1 >>> 16) % 0x100, (rgb1 >>> 8) % 0x100, rgb1 % 0x100, (int) (alpha * 64) + 100);
				}
				catch (Exception exc) {
					c1 = new Color((rgb1 >>> 16) % 0x100, (rgb1 >>> 8) % 0x100, rgb1 % 0x100, 188);
				}
				try {
					c2 = new Color((rgb2 >>> 16) % 0x100, (rgb2 >>> 8) % 0x100, rgb2 % 0x100, (int) (alpha * 64) + 100);
				}
				catch (Exception exc) {
					c2 = new Color((rgb2 >>> 16) % 0x100, (rgb2 >>> 8) % 0x100, rgb2 % 0x100, 188);
				}
				GradientPaint gp = new GradientPaint((float) projection(close)[0] + xShift,
						(float) projection(close)[1] + yShift, c1, (float) projection(far)[0] + xShift,
						(float) projection(far)[1] + yShift, c2);
				((Graphics2D) canvas).setPaint((gp));

				canvas.fillPolygon(path);
			}
		}
	}

	public void drawFaces(Graphics canvas, int rgb, Origami origami) {

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (isDrawable(i, origami)) {

				double[] fnormal = Geometry.crossProduct(
						Geometry.vectorDiff(origami.getVertices().get(origami.getPolygons().get(i).get(0)),
								origami.getVertices().get(origami.getPolygons().get(i).get(1))),
						Geometry.vectorDiff(origami.getVertices().get(origami.getPolygons().get(i).get(0)),
								origami.getVertices().get(origami.getPolygons().get(i).get(2))));

				double nv_len = Geometry.vectorLength(fnormal);
				if (nv_len != 0) {
					fnormal[0] = fnormal[0] / nv_len;
					fnormal[1] = fnormal[1] / nv_len;
					fnormal[2] = fnormal[2] / nv_len;
				}

				double alpha = 1 - Math.abs(Geometry.scalarProduct(camDirection, fnormal));
				int color = Geometry.scalarProduct(camDirection, fnormal) > 0 ? (rgb & 0xFFFFFF) : paper_back_color;

				try {
					canvas.setColor(new Color((color >>> 16) % 0x100, (color >>> 8) % 0x100, color % 0x100,
							(int) (alpha * 128) + 80));
				}
				catch (Exception exc) {
					canvas.setColor(new Color((color >>> 16) % 0x100, (color >>> 8) % 0x100, color % 0x100, 188));
				}

				Polygon path = new Polygon();

				for (int ii = 0; ii < origami.getPolygons().get(i).size(); ii++) {

					path.addPoint(
							(short) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[0])
									+ xShift,
							(short) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[1])
									+ yShift);
				}

				canvas.fillPolygon(path);
			}
		}
	}

	public String drawFaces(int x, int y, Origami origami) {

		String out = "0.8 0.8 0.8 rg ";

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (isDrawable(i, origami)) {

				out += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[0]) + x);
				out += " ";
				out += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[1]) + y);
				out += " m ";

				for (int ii = 1; ii < origami.getPolygons().get(i).size(); ii++) {
					out += Integer.toString(
							(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[0]) + x);
					out += " ";
					out += Integer.toString(
							(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(ii)))[1]) + y);
					out += " l ";
				}
				out += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[0]) + x);
				out += " ";
				out += Integer.toString(
						(int) (projection(origami.getVertices().get(origami.getPolygons().get(i).get(0)))[1]) + y);
				out += " l f ";
			}
		}

		return out;
	}

	public void drawCreasePattern(Graphics canvas, Color color, Origami origami) {

		canvas.setColor(color);

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			if (isDrawable(i, origami)) {

				Polygon ut = new Polygon();

				for (int ii = 0; ii < origami.getPolygons().get(i).size(); ii++) {

					ut.addPoint(
							(short) (projection(origami.getVertices2d().get(origami.getPolygons().get(i).get(ii)))[0])
									+ xShift,
							(short) (projection(origami.getVertices2d().get(origami.getPolygons().get(i).get(ii)))[1])
									+ yShift);
				}
				canvas.drawPolygon(ut);
			}
		}
	}

	public void drawFoldingLine(Graphics canvas, Color color, double[] ppoint, double[] pnormal, Origami origami) {

		canvas.setColor(color);
		List<double[]> line = origami.foldingLine(ppoint, pnormal);
		for (int i = 0; i < line.size(); i += 2) {
			canvas.drawLine((short) (projection(line.get(i))[0] + xShift),
					(short) (projection(line.get(i))[1] + yShift), (short) (projection(line.get(i + 1))[0] + xShift),
					(short) (projection(line.get(i + 1))[1] + yShift));
		}
	}

	public void draw2dFoldingLine(Graphics canvas, Color color, double[] ppoint, double[] pnormal, Origami origami) {

		canvas.setColor(color);
		List<double[]> line = origami.foldingLine2d(ppoint, pnormal);
		for (int i = 0; i < line.size(); i += 2) {
			canvas.drawLine((short) (projection(line.get(i))[0] + xShift),
					(short) (projection(line.get(i))[1] + yShift), (short) (projection(line.get(i + 1))[0] + xShift),
					(short) (projection(line.get(i + 1))[1] + yShift));
		}
	}

	public String pfdLiner(int x, int y, double[] ppoint, double[] pnormal) {

		String out = "0.4 0.4 0.4 RG [5 5] 0 d ";
		double[] pnormal_2D = projection0(pnormal);
		double[] ppoint_2D = projection(ppoint);
		boolean lineto = false;
		double bound = 100;

		if (pdfLinerDir(pnormal) == 'J' || pdfLinerDir(pnormal) == 'B') {

			double[] pdir_2D = new double[] { -pnormal_2D[1] / pnormal_2D[0], 1 };

			if (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) <= bound
					&& ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) >= -bound) {

				out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1])) + x);
				out += " ";
				out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[1])) + y);
				out += " m ";
				lineto = true;
			}

			if (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) <= bound
					&& ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) >= -bound) {

				out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1])) + x);
				out += " ";
				out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[1])) + y);
				if (lineto) {

					out += " l ";
					lineto = false;
				}
				else {

					out += " m ";
					lineto = true;
				}
			}

			if (lineto) {

				pdir_2D = new double[] { 1, -pnormal_2D[0] / pnormal_2D[1] };

				if (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) <= bound
						&& ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) >= -bound) {

					out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[0])) + x);
					out += " ";
					out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0])) + y);
					out += " l ";
				}

				if (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) <= bound
						&& ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) >= -bound) {

					out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[0])) + x);
					out += " ";
					out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0])) + y);
					out += " l ";
				}
			}
		}
		else {

			double[] pdir_2D = new double[] { 1, -pnormal_2D[0] / pnormal_2D[1] };

			if (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) <= bound
					&& ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0]) >= -bound) {

				out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[0])) + x);
				out += " ";
				out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[0])) + y);
				out += " m ";
				lineto = true;
			}

			if (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) <= bound
					&& ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0]) >= -bound) {

				out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[0])) + x);
				out += " ";
				out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[0])) + y);
				if (lineto) {

					out += " l ";
					lineto = false;
				}
				else {

					out += " m ";
					lineto = true;
				}
			}

			if (lineto) {

				pdir_2D = new double[] { -pnormal_2D[1] / pnormal_2D[0], 1 };

				if (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) <= bound
						&& ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1]) >= -bound) {

					out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (bound - ppoint_2D[1])) + x);
					out += " ";
					out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (bound - ppoint_2D[1])) + y);
					out += " l ";
				}

				if (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) <= bound
						&& ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1]) >= -bound) {

					out += Integer.toString((int) (ppoint_2D[0] + pdir_2D[0] * (-bound - ppoint_2D[1])) + x);
					out += " ";
					out += Integer.toString((int) (ppoint_2D[1] + pdir_2D[1] * (-bound - ppoint_2D[1])) + y);
					out += " l ";
				}
			}
		}

		out += "S [ ] 0 d 0.0 0.0 0.0 RG ";
		return out;
	}

	final static public int PDF_NORTH = 'F';
	final static public int PDF_SOUTH = 'L';
	final static public int PDF_WEST = 'B';
	final static public int PDF_EAST = 'J';

	public int pdfLinerDir(double[] pnormal) {

		double[] pnormal_2D = projection0(pnormal);

		if (pnormal_2D[0] < pnormal_2D[1]) {

			if (pnormal_2D[0] < -pnormal_2D[1]) {

				return PDF_WEST;
			}
			else {

				return PDF_NORTH;
			}
		}
		else {

			if (pnormal_2D[0] < -pnormal_2D[1]) {

				return PDF_SOUTH;
			}
			else {

				return PDF_EAST;
			}
		}
	}

	public void adjust(Origami origami) {

		Double a, f, b, j, h, e;
		f = (a = (j = (b = (e = (h = null)))));
		for (int i = 0; i < origami.getVerticesSize(); i++) {

			if (b == null || origami.getVertices().get(i)[0] < b) {
				b = origami.getVertices().get(i)[0];
			}
			if (j == null || origami.getVertices().get(i)[0] > j) {
				j = origami.getVertices().get(i)[0];
			}
			if (a == null || origami.getVertices().get(i)[1] < a) {
				a = origami.getVertices().get(i)[1];
			}
			if (f == null || origami.getVertices().get(i)[1] > f) {
				f = origami.getVertices().get(i)[1];
			}
			if (h == null || origami.getVertices().get(i)[2] < h) {
				h = origami.getVertices().get(i)[2];
			}
			if (e == null || origami.getVertices().get(i)[2] > e) {
				e = origami.getVertices().get(i)[2];
			}
		}

		if (origami.getVerticesSize() > 0) {
			camPosition = new double[] { (b + j) / 2, (a + f) / 2, (h + e) / 2 };
		}
	}

	public void unadjust(Origami origami) {

		double[] center = new double[] { 0.0, 0.0, 0.0 };
		for (double[] pont : origami.getCorners()) {
			center = new double[] { center[0] + pont[0], center[1] + pont[1], 0 };
		}

		center = new double[] { center[0] / origami.getCorners().size(), center[1] / origami.getCorners().size(), 0 };
		camPosition = center;
	}

	public void setOrthogonalView(int orientation) {

		switch (orientation) {

		case 0:
			camDirection[0] = 0;
			camDirection[1] = 0;
			camDirection[2] = 1;
			xAxis[0] = 1;
			xAxis[1] = 0;
			xAxis[2] = 0;
			yAxis[0] = 0;
			yAxis[1] = 1;
			yAxis[2] = 0;
			break;
		case 1:
			camDirection[0] = 0;
			camDirection[1] = 1;
			camDirection[2] = 0;
			xAxis[0] = 1;
			xAxis[1] = 0;
			xAxis[2] = 0;
			yAxis[0] = 0;
			yAxis[1] = 0;
			yAxis[2] = -1;
			break;
		case 2:
			camDirection[0] = -1;
			camDirection[1] = 0;
			camDirection[2] = 0;
			xAxis[0] = 0;
			xAxis[1] = 0;
			xAxis[2] = 1;
			yAxis[0] = 0;
			yAxis[1] = 1;
			yAxis[2] = 0;
			break;
		default:
			camDirection[0] = 0;
			camDirection[1] = 0;
			camDirection[2] = 1;
			xAxis[0] = 1;
			xAxis[1] = 0;
			xAxis[2] = 0;
			yAxis[0] = 0;
			yAxis[1] = 1;
			yAxis[2] = 0;
			break;
		}

		this.orientation = (byte) orientation;
	}

	public void nextOrthogonalView() {

		switch (orientation) {

		case 0:
			camDirection[0] = 0;
			camDirection[1] = 0;
			camDirection[2] = 1;
			xAxis[0] = 1;
			xAxis[1] = 0;
			xAxis[2] = 0;
			yAxis[0] = 0;
			yAxis[1] = 1;
			yAxis[2] = 0;
			break;
		case 1:
			camDirection[0] = 0;
			camDirection[1] = 1;
			camDirection[2] = 0;
			xAxis[0] = 1;
			xAxis[1] = 0;
			xAxis[2] = 0;
			yAxis[0] = 0;
			yAxis[1] = 0;
			yAxis[2] = -1;
			break;
		case 2:
			camDirection[0] = -1;
			camDirection[1] = 0;
			camDirection[2] = 0;
			xAxis[0] = 0;
			xAxis[1] = 0;
			xAxis[2] = 1;
			yAxis[0] = 0;
			yAxis[1] = 1;
			yAxis[2] = 0;
			break;
		default:
			camDirection[0] = 0;
			camDirection[1] = 0;
			camDirection[2] = 1;
			xAxis[0] = 1;
			xAxis[1] = 0;
			xAxis[2] = 0;
			yAxis[0] = 0;
			yAxis[1] = 1;
			yAxis[2] = 0;
			break;
		}

		orientation = (byte) ((orientation + 1) % 3);
	}

	public void setTexture(BufferedImage texture) throws Exception {

		if (texture.getColorModel().hasAlpha()) {
			throw OrigamiException.H013;
		}
		this.texture = texture;
	}

	public void updateBuffer(Origami origami) {

		BufferedImage map = new BufferedImage(texture.getWidth(), texture.getHeight(),
				java.awt.image.BufferedImage.TYPE_INT_RGB);
		Graphics2D canvas = map.createGraphics();
		canvas.setBackground(Color.WHITE);
		canvas.clearRect(0, 0, texture.getWidth(), texture.getHeight());
		int[][] skeleton = new int[origami.getPolygonsSize()][];

		for (int i = 0; i < origami.getPolygonsSize(); i++) {

			int[] triangle = new int[3];
			if (isDrawable(i, origami, triangle)) {

				skeleton[i] = triangle;

				Polygon path = new Polygon();
				for (int ii = 0; ii < origami.getPolygons().get(i).size(); ii++) {

					path.addPoint(
							(short) (new Camera(0, 0, 1d)
									.projection(origami.getVertices2d().get(origami.getPolygons().get(i).get(ii)))[0])
									+ 200,
							(short) (new Camera(0, 0, 1d)
									.projection(origami.getVertices2d().get(origami.getPolygons().get(i).get(ii)))[1])
									+ 200);
				}
				canvas.setColor(new Color(i));
				canvas.fillPolygon(path);
			}
		}

		int[] raw = ((DataBufferInt) map.getRaster().getDataBuffer()).getData();

		int len = texture.getHeight() * texture.getWidth();
		int width = texture.getWidth();
		spaceBuffer = new double[len][];

		for (int i = 0; i < len; i++) {

			int color = raw[i] & 0xFFFFFF;
			if (color != 0xFFFFFF) {

				try {
					double x_1 = origami.getVertices2d().get(skeleton[color][1])[0]
							- origami.getVertices2d().get(skeleton[color][0])[0];
					double x_2 = origami.getVertices2d().get(skeleton[color][1])[1]
							- origami.getVertices2d().get(skeleton[color][0])[1];
					double y_1 = origami.getVertices2d().get(skeleton[color][2])[0]
							- origami.getVertices2d().get(skeleton[color][0])[0];
					double y_2 = origami.getVertices2d().get(skeleton[color][2])[1]
							- origami.getVertices2d().get(skeleton[color][0])[1];
					double a_1 = (double) (i % width) - origami.getVertices2d().get(skeleton[color][0])[0];
					double a_2 = (double) i / width - origami.getVertices2d().get(skeleton[color][0])[1];

					double lambda1 = (a_1 * y_2 - a_2 * y_1) / (x_1 * y_2 - x_2 * y_1);
					double lambda2 = (a_1 * x_2 - a_2 * x_1) / (y_1 * x_2 - y_2 * x_1);
					double[] v3d1 = Geometry.vectorDiff(origami.getVertices().get(skeleton[color][1]),
							origami.getVertices().get(skeleton[color][0]));
					double[] v3d2 = Geometry.vectorDiff(origami.getVertices().get(skeleton[color][0]),
							origami.getVertices().get(skeleton[color][2]));
					spaceBuffer[i] = Geometry.vectorDiff(
							Geometry.vectorDiff(Geometry.scalarMultiple(v3d1, lambda1),
									Geometry.scalarMultiple(v3d2, lambda2)),
							Geometry.scalarMultiple(origami.getVertices().get(skeleton[color][0]), -1));
				}
				catch (Exception ex) {
				}
			}
		}

		new Camera(200, 200, 1d).drawCreasePattern(texture.createGraphics(), Color.BLACK, origami);
	}

	public void drawTexture(Graphics canvas, int w, int h) {

		byte[] raw = ((DataBufferByte) texture.getRaster().getDataBuffer()).getData();
		Double[][] depth_buffer = new Double[w][h];
		BufferedImage ret = new BufferedImage(w, h,
				java.awt.image.BufferedImage.TYPE_INT_RGB);
		Graphics2D bleach = ret.createGraphics();
		bleach.setBackground(Color.WHITE);
		bleach.clearRect(0, 0, w, h);

		for (int i = 0; i < raw.length; i += 3) {

			int szin = (raw[i] & 0xFF) + ((raw[i + 1] & 0xFF) << 8) + ((raw[i + 2] & 0xFF) << 16);
			kiserlet: try {
				double[] point = spaceBuffer[i / 3];
				if (point == null) {
					break kiserlet;
				}
				double tmp = zoom;
				zoom = 1;
				double[] proj = projection(point);
				zoom = tmp;
				short projX = (short) (proj[0] + xShift);
				short projY = (short) (proj[1] + yShift);
				if (projX >= 0 && projX < depth_buffer.length && projY >= 0 && projY < depth_buffer[0].length) {
					if (depth_buffer[projX][projY] == null
							|| Geometry.scalarProduct(point, camDirection) > depth_buffer[projX][projY]) {

						depth_buffer[projX][projY] = Geometry.scalarProduct(point, camDirection);
						ret.setRGB(projX, projY, szin);
					}
				}
			}
			catch (Exception ex) {
			}
		}
		canvas.drawImage(ret, (int) ((1 - zoom) * ret.getWidth() / 2), (int) ((1 - zoom) * ret.getHeight() / 2),
				(int) (ret.getWidth() * zoom), (int) (ret.getHeight() * zoom), null);
	}

	public double[] getCamPosition() {
		return camPosition;
	}

	public void setCamPosition(double[] camPosition) {
		this.camPosition = camPosition;
	}

	public double[] getCamDirection() {
		return camDirection;
	}

	public void setCamDirection(double[] camDirection) {
		this.camDirection = camDirection;
	}

	public double[] getXAxis() {
		return xAxis;
	}

	public void setXAxis(double[] xAxis) {
		this.xAxis = xAxis;
	}

	public double[] getYAxis() {
		return yAxis;
	}

	public void setYAxis(double[] yAxis) {
		this.yAxis = yAxis;
	}

	public double[] getXScale() {
		return Geometry.scalarMultiple(xAxis, zoom);
	}

	public double[] getYScale() {
		return Geometry.scalarMultiple(yAxis, zoom);
	}

	public int getXShift() {
		return xShift;
	}

	public void setXShift(int xShift) {
		this.xShift = xShift;
	}

	public int getYShift() {
		return yShift;
	}

	public void setYShift(int yShift) {
		this.yShift = yShift;
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double value) {
		zoom = value;
	}
}
