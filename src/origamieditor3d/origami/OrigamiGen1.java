package origamieditor3d.origami;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public class OrigamiGen1 implements Origami {

	/**
	 * Creates a new origami model. <br>
	 * The new model will be initialized with an empty {@link #getHistory()
	 * histroy}, a {@link #getPaperType() papertype} of the specified
	 * {@link PaperType}, and will be {@link #reset() reset} immediately afterwards.
	 *
	 * @param papertype
	 *            The {@link #getPaperType() papertype} of the new instance.
	 */
	public OrigamiGen1(PaperType papertype) {

		vertices = new ArrayList<>();
		vertices2d = new ArrayList<>();
		verticesSize = 0;
		polygons = new ArrayList<>();
		polygonsSize = 0;
		history = new ArrayList<>();
		historyStream = new ArrayList<>();
		historyPointer = 0;
		this.paperType = papertype;
		reset();
	}

	/**
	 * Creates a new origami model. <br>
	 * The new model will be initialized with an empty {@link #getHistory()
	 * history}, a {@link #getPaperType() papertype} of {@link PaperType#Custom}, a
	 * {@link #getCorners() corners} list that is the
	 * {@link Geometry#ccwWindingOrder(ArrayList) ccwWindingOrder} of the specified
	 * {@link ArrayList}, and will be {@link #reset() reset} immediately afterwards.
	 *
	 * @param corners
	 *            The {@link #getCorners() corners} list of the new instance in an
	 *            arbitrary order.
	 * @throws Exception
	 *             if {@code !isConvex(ccwWindingOrder(corners))}
	 */
	public OrigamiGen1(List<double[]> corners) throws Exception {

		vertices = new ArrayList<>();
		vertices2d = new ArrayList<>();
		verticesSize = 0;
		polygons = new ArrayList<>();
		polygonsSize = 0;
		history = new ArrayList<>();
		historyStream = new ArrayList<>();
		historyPointer = 0;
		paperType = PaperType.Custom;
		this.corners = Geometry.ccwWindingOrder(corners);
		if (!Geometry.isConvex(this.corners)) {
			throw new Exception("Varatlan konkav sokszog/Unexpected concave polygon");
		}
		reset();
	}

	public OrigamiGen1(OrigamiGen1 origami) {

		vertices = new ArrayList<>();
		vertices2d = new ArrayList<>();
		verticesSize = 0;
		polygons = new ArrayList<>();
		polygonsSize = 0;
		paperType = origami.paperType;
		corners = new ArrayList<double[]>(origami.corners);
		history = new ArrayList<FoldingAction>(origami.history);
		historyStream = new ArrayList<int[]>(origami.historyStream);
		historyPointer = origami.historyPointer;
		reset();
		execute();
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#generation()
	 */
	@Override
	public int generation() {
		return 1;
	}

	protected List<double[]> vertices;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getVertices()
	 */
	@Override
	public List<double[]> getVertices() {
		return vertices;
	}

	protected int verticesSize = 0;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getVerticesSize()
	 */
	@Override
	public int getVerticesSize() {
		return verticesSize;
	}

	protected List<List<Integer>> polygons;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getPolygons()
	 */
	@Override
	public List<List<Integer>> getPolygons() {
		return polygons;
	}

	protected int polygonsSize = 0;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getPolygonsSize()
	 */
	@Override
	public int getPolygonsSize() {
		return polygonsSize;
	}

	

	protected List<FoldingAction> history;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getHistory()
	 */
	@Override
	public List<FoldingAction> getHistory() {
		return history;
	}

	protected int historyPointer;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getHistoryPointer()
	 */
	@Override
	public int getHistoryPointer() {
		return historyPointer;
	}

	protected List<int[]> historyStream;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getHistoryStream()
	 */
	@Override
	public List<int[]> getHistoryStream() {
		return new ArrayList<int[]>(historyStream);
	}

	protected PaperType paperType;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getPaperType()
	 */
	@Override
	public PaperType getPaperType() {
		return paperType;
	}

	protected List<double[]> corners;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getCorners()
	 */
	@Override
	public List<double[]> getCorners() {
		return corners;
	}

	protected List<double[]> vertices2d;

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getVertices2d()
	 */
	@Override
	public List<double[]> getVertices2d() {
		return vertices2d;
	}

	protected List<Integer> border = new ArrayList<>();

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#getBorder()
	 */
	@Override
	public List<Integer> getBorder() {
		return border;
	}

	/**
	 * Adds a new vertex to the end of the {@link #getVertices() vertices} list of
	 * this origami.
	 *
	 * @param point
	 *            The 3-dimensional coordinates of the new vertex in the origami
	 *            space as {@code double}s.
	 */
	protected void addVertex(double... point) {

		vertices.add(point);
		verticesSize++;
	}

	/**
	 * Adds a new vertex to the end of the {@link #getVertices2d() vertices2d} list
	 * of this origami.
	 *
	 * @param point
	 *            The 2-dimensional coordinates of the new vertex in the paper space
	 *            as {@code double}s.
	 */
	protected void add2dVertex(double... point) {
		vertices2d.add(point);
	}

	/**
	 * Adds a new polygon to the end of the {@link #getPolygons() polygons} list of
	 * this origami. <br>
	 * When adding a new polygon, {@code isConvex(polygon)} is expected (but not
	 * checked) to be {@code true}.
	 *
	 * @param polygon
	 *            An {@link ArrayList} that contains zero-based indices pointing
	 *            into the {@link #getVertices() vertices} and the
	 *            {@link #getVertices2d() vertices2d} list of this origami in a
	 *            counter-clockwise winding order.
	 */
	protected void addPolygon(ArrayList<Integer> polygon) {

		polygons.add(polygon);
		polygonsSize++;
	}

	/**
	 * Removes the polygon from this origami's {@link #getPolygons() polygons} at
	 * the specified index.
	 *
	 * @param polygonIndex
	 *            The zero-based index at which the polygon to remove is located in
	 *            the {@link #getPolygons() polygons} list.
	 */
	protected void removePolygon(int polygonIndex) {

		polygons.remove(polygonIndex);
		polygonsSize--;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#addCommand(int, double[], double[], int, int)
	 */
	@Override
	public void addCommand(int commandID, double[] ppoint, double[] pnormal, int polygonIndex, int phi) {

		int[] cblock = commandBlock(commandID, ppoint, pnormal, polygonIndex, phi);
		addCommand(cblock);
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#addCommand(int[])
	 */
	@Override
	public void addCommand(int[] cblock) {

		int i = -1;

		int header = cblock[++i];
		header <<= 8;
		header += cblock[++i];
		header <<= 8;
		header += cblock[++i];
		header <<= 8;
		header += cblock[++i];

		short Xint, Yint, Zint;
		int Xfrac, Yfrac, Zfrac;

		Xint = (short) cblock[++i];
		Xint <<= 8;
		Xint += cblock[++i];
		Xfrac = cblock[++i];
		Xfrac <<= 8;
		Xfrac += cblock[++i];
		double X = Xint + Math.signum(Xint) * (double) Xfrac / 256 / 256;

		Yint = (short) cblock[++i];
		Yint <<= 8;
		Yint += cblock[++i];
		Yfrac = cblock[++i];
		Yfrac <<= 8;
		Yfrac += cblock[++i];
		double Y = Yint + Math.signum(Yint) * (double) Yfrac / 256 / 256;

		Zint = (short) cblock[++i];
		Zint <<= 8;
		Zint += cblock[++i];
		Zfrac = cblock[++i];
		Zfrac <<= 8;
		Zfrac += cblock[++i];
		double Z = Zint + Math.signum(Zint) * (double) Zfrac / 256 / 256;

		double[] ppoint = new double[3];
		double[] pnormal = new double[3];
		ppoint[0] = (double) X + Origins[(((header >>> 24) % 32) - ((header >>> 24) % 8)) / 8][0];
		ppoint[1] = (double) Y + Origins[(((header >>> 24) % 32) - ((header >>> 24) % 8)) / 8][1];
		ppoint[2] = (double) Z + Origins[(((header >>> 24) % 32) - ((header >>> 24) % 8)) / 8][2];
		pnormal[0] = X;
		pnormal[1] = Y;
		pnormal[2] = Z;

		// choosing the appropriate half space
		if (((header >>> 24) - ((header >>> 24) % 32)) / 32 == 1) {
			pnormal = new double[] { -pnormal[0], -pnormal[1], -pnormal[2] };
		}

		FoldingAction comnd;
		if ((header >>> 24) % 8 == 1) {

			// reflection fold
			comnd = new FoldingAction(FoldingAction.FOLD_REFLECTION, ppoint, pnormal, 0, 0);
		}
		else if ((header >>> 24) % 8 == 2) {

			// positive rot. fold
			int phi = (header >>> 16) % 256;
			comnd = new FoldingAction(FoldingAction.FOLD_ROTATION, ppoint, pnormal, 0, phi);
		}
		else if ((header >>> 24) % 8 == 3) {

			// negative rot. fold
			int phi = -(header >>> 16) % 256;
			comnd = new FoldingAction(FoldingAction.FOLD_ROTATION, ppoint, pnormal, 0, phi);
		}
		else if ((header >>> 24) % 8 == 4) {

			// partial reflection fold
			int polygonIndex = (header % 65536);
			comnd = new FoldingAction(FoldingAction.FOLD_REFLECTION_P, ppoint, pnormal, polygonIndex, 0);
		}
		else if ((header >>> 24) % 8 == 5) {

			// positive partial rot. fold
			int phi = (header >>> 16) % 256;
			int polygonIndex = (header % 65536);
			comnd = new FoldingAction(FoldingAction.FOLD_ROTATION_P, ppoint, pnormal, polygonIndex, phi);
		}
		else if ((header >>> 24) % 8 == 6) {

			// negative partial rot. fold
			int phi = -(header >>> 16) % 256;
			int polygonIndex = (header % 65536);
			comnd = new FoldingAction(FoldingAction.FOLD_ROTATION_P, ppoint, pnormal, polygonIndex, phi);
		}
		else if ((header >>> 24) % 8 == 7) {

			// crease
			comnd = new FoldingAction(FoldingAction.FOLD_CREASE, ppoint, pnormal, 0, 0);
		}
		else if (header % 65536 == 65535) {

			// cut
			comnd = new FoldingAction(FoldingAction.FOLD_MUTILATION, ppoint, pnormal, 0, 0);
		}
		else {

			// partial cut
			int polygonIndex = (header % 65536);
			comnd = new FoldingAction(FoldingAction.FOLD_MUTILATION_P, ppoint, pnormal, polygonIndex, 0);
		}

		history.add(comnd);
		historyStream.add(cblock);
	}

	/**
	 * Compresses the arguments into an array of bytes. For {@code ppoint} and
	 * {@code pnormal}, this compression is lossy.
	 * 
	 * @param foid
	 * @param ppoint
	 * @param pnormal
	 * @param polygonIndex
	 * @param phi
	 * @return
	 */
	protected int[] commandBlock(int foid, double[] ppoint, double[] pnormal, int polygonIndex, int phi) {

		double max_d = -1;
		int used_origin = 0;
		int used_hemispace = 0;
		double[] pjoint = new double[] { 0, 0, 0 };

		for (int ii = 0; ii < Origins.length; ii++) {

			double[] basepoint = Geometry.linePlaneIntersection(Origins[ii], pnormal, ppoint, pnormal);
			if (Geometry.vectorLength(Geometry.vectorDiff(basepoint, Origins[ii])) > max_d) {

				pjoint = Geometry.vectorDiff(basepoint, Origins[ii]);
				max_d = Geometry.vectorLength(pjoint);
				used_origin = ii;
			}
		}

		// inner: 1, outer: 0
		if (Geometry.scalarProduct(pnormal, pjoint) < 0) {
			used_hemispace = 1;
		}

		int command = 0;
		int poly_indx = 65535;

		switch (foid) {

		case 1:
			command = 1;
			break;

		case 2:
			while (phi < 0) {
				phi += 360;
			}
			phi %= 360;
			if (phi <= 180) {
				command = 2;
			}
			else {

				command = 3;
				phi = 360 - phi;
			}
			break;

		case 3:
			command = 4;
			poly_indx = polygonIndex;
			break;

		case 4:
			while (phi < 0) {
				phi += 360;
			}
			phi %= 360;
			if (phi <= 180) {
				command = 5;
			}
			else {

				command = 6;
				phi = 360 - phi;
			}
			poly_indx = polygonIndex;
			break;

		case 5:
			command = 7;
			break;

		case 6:
			command = 0;
			break;

		case 7:
			command = 0;
			poly_indx = polygonIndex;
			break;
		}

		int Xe = (int) pjoint[0];
		int Ye = (int) pjoint[1];
		int Ze = (int) pjoint[2];

		int Xt = (int) Math.round((Math.abs(pjoint[0] - Xe)) * 256 * 256);
		int Yt = (int) Math.round((Math.abs(pjoint[1] - Ye)) * 256 * 256);
		int Zt = (int) Math.round((Math.abs(pjoint[2] - Ze)) * 256 * 256);

		int[] cblock = {

				// header
				(0xFF & (used_hemispace * 32 + used_origin * 8 + command)), (0xFF & (phi)), (0xFF & (poly_indx >>> 8)),
				(0xFF & (poly_indx)),

				// body
				(0xFF & (Xe >>> 8)), (0xFF & (Xe)), (0xFF & (Xt >>> 8)), (0xFF & (Xt)),

				(0xFF & (Ye >>> 8)), (0xFF & (Ye)), (0xFF & (Yt >>> 8)), (0xFF & (Yt)),

				(0xFF & (Ze >>> 8)), (0xFF & (Ze)), (0xFF & (Zt >>> 8)), (0xFF & (Zt)) };
		return cblock;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#isNonDegenerate(int)
	 */
	@Override
	public boolean isNonDegenerate(int polygonIndex) {

		if (polygons.get(polygonIndex).size() > 1) {
			for (int p : polygons.get(polygonIndex)) {
				if (Geometry.vectorLength(
						Geometry.vectorDiff(vertices.get(p), vertices.get(polygons.get(polygonIndex).get(0)))) > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#isStrictlyNonDegenerate(int)
	 */
	@Override
	public boolean isStrictlyNonDegenerate(int polygonIndex) {

		if (polygons.get(polygonIndex).size() > 2) {

			for (int point1ind : polygons.get(polygonIndex)) {
				for (int point2ind : polygons.get(polygonIndex)) {

					if (Geometry.vectorLength(Geometry.crossProduct(
							Geometry.vectorDiff(vertices.get(point1ind),
									vertices.get(polygons.get(polygonIndex).get(0))),
							Geometry.vectorDiff(vertices.get(point2ind),
									vertices.get(polygons.get(polygonIndex).get(0))))) > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean isCut(double[] ppoint, double[] pnormal, int polygonIndex) {

		if (isNonDegenerate(polygonIndex)) {

			boolean inner = false, outer = false;
			for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {
				if (Geometry.scalarProduct(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) / Math.sqrt(
						Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) > Geometry.scalarProduct(ppoint, pnormal)
								/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) + 0.00000001) {
					inner = true;
				}
				else if (Geometry.scalarProduct(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) / Math.sqrt(
						Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) < Geometry.scalarProduct(ppoint, pnormal)
								/ Math.sqrt(Math.max(Geometry.scalarProduct(pnormal, pnormal), 1)) - 0.00000001) {
					outer = true;
				}
				if (inner && outer) {
					return true;
				}
			}
		}
		return false;
	}

	protected List<int[]> cutpolygon_nodes = new ArrayList<>();
	protected List<int[]> cutpolygon_pairs = new ArrayList<>();
	protected List<List<Integer>> last_cut_polygons = new ArrayList<>();

	/**
	 * Removes the specified polygon from the {@link #getPolygons() polygons} list,
	 * and adds one or two new polygons into it. One of new polygons is obtained by
	 * intersecting the old polygon with the specified closed half-space, and the
	 * other one by intersecting it with the closure of that half-space's
	 * complement. If one of these polygons is empty, only the other one will be
	 * added. <br>
	 * If two new polygons have been generated, their common vertices will point to
	 * the same object in the {@link #getVertices() vertices} list, thus becoming
	 * 'inseparable'.
	 *
	 * @param ppoint
	 *            An array containing the coordinates of a boundary point of the
	 *            half-space.
	 * @param pnormal
	 *            An array containing the coordinates of the normal vector of the
	 *            half-space.
	 * @param polygonIndex
	 *            The zero-based index at which the polygon to split is located in
	 *            the {@link #getPolygons() polygons} list.
	 * @return {@code true} iff the polygon has been divided in two.
	 */
	protected boolean cutPolygon(double[] ppoint, double[] pnormal, int polygonIndex) {

		if (isCut(ppoint, pnormal, polygonIndex)) {

			ArrayList<Integer> newpoly1 = new ArrayList<>();
			ArrayList<Integer> newpoly2 = new ArrayList<>();

			for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

				int j = (i + 1) % polygons.get(polygonIndex).size();
				if (Geometry.isPointOnPlane(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)))) {

					newpoly1.add(polygons.get(polygonIndex).get(i));
					newpoly2.add(polygons.get(polygonIndex).get(i));
				}
				else {

					if (Geometry.scalarProduct(vertices.get(polygons.get(polygonIndex).get(i)), pnormal) > Geometry
							.scalarProduct(ppoint, pnormal)) {
						newpoly1.add(polygons.get(polygonIndex).get(i));
					}
					else {
						newpoly2.add(polygons.get(polygonIndex).get(i));
					}

					if (Geometry.isPlaneBetweenPoints(ppoint, pnormal, vertices.get(polygons.get(polygonIndex).get(i)),
							vertices.get(polygons.get(polygonIndex).get(j)))) {

						freshcut: {
							for (int[] szakasz : cutpolygon_nodes) {
								if (szakasz[0] == polygons.get(polygonIndex).get(i)
										&& szakasz[1] == polygons.get(polygonIndex).get(j)) {
									newpoly1.add(szakasz[2]);
									newpoly2.add(szakasz[2]);
									break freshcut;
								}
								else if (szakasz[0] == polygons.get(polygonIndex).get(j)
										&& szakasz[1] == polygons.get(polygonIndex).get(i)) {
									newpoly1.add(szakasz[2]);
									newpoly2.add(szakasz[2]);
									break freshcut;
								}
							}

							double[] dirvec = Geometry.vectorDiff(vertices.get(polygons.get(polygonIndex).get(i)),
									vertices.get(polygons.get(polygonIndex).get(j)));
							double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

							double[] meet = Geometry.linePlaneIntersection(ipoint, dirvec, ppoint, pnormal);
							addVertex(meet);

							double weight1 = Geometry.vectorLength(
									Geometry.vectorDiff(meet, vertices.get(polygons.get(polygonIndex).get(j))));
							double weight2 = Geometry.vectorLength(
									Geometry.vectorDiff(meet, vertices.get(polygons.get(polygonIndex).get(i))));
							add2dVertex(new double[] {
									(vertices2d.get(polygons.get(polygonIndex).get(i))[0] * weight1
											+ vertices2d.get(polygons.get(polygonIndex).get(j))[0] * weight2)
											/ (weight1 + weight2),
									(vertices2d.get(polygons.get(polygonIndex).get(i))[1] * weight1
											+ vertices2d.get(polygons.get(polygonIndex).get(j))[1] * weight2)
											/ (weight1 + weight2),
									0 });

							newpoly1.add(verticesSize - 1);
							newpoly2.add(verticesSize - 1);
							cutpolygon_nodes.add(new int[] { polygons.get(polygonIndex).get(i),
									polygons.get(polygonIndex).get(j), verticesSize - 1 });

							for (int ii = 0; ii < border.size(); ii++) {
								if (border.get(ii).equals(polygons.get(polygonIndex).get(i))) {
									if (border.get((ii + 1) % border.size())
											.equals(polygons.get(polygonIndex).get(j))) {

										border.add(ii + 1, verticesSize - 1);
										break;
									}
								}
							}
						}
					}
				}
			}

			cutpolygon_pairs.add(new int[] { polygonIndex, polygons.size() });
			last_cut_polygons.add(polygons.get(polygonIndex));
			polygons.set(polygonIndex, newpoly1);
			addPolygon(newpoly2);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#polygonSelect(double[], double[], int)
	 */
	@Override
	public ArrayList<Integer> polygonSelect(double[] ppoint, double[] pnormal, int polygonIndex) {

		ArrayList<Integer> selection = new ArrayList<>();
		selection.add(polygonIndex);
		for (int i = 0; i < selection.size(); i++) {

			int elem = selection.get(i);
			for (int ii = 0; ii < polygonsSize; ii++) {

				if (!selection.contains(ii)) {

					for (int e_point : polygons.get(elem)) {

						if (polygons.get(ii).contains(e_point)) {
							if (!Geometry.isPointOnPlane(ppoint, pnormal, vertices.get(e_point))) {
								selection.add(ii);
								break;
							}
						}
					}
				}
			}
		}

		return selection;
	}

	/**
	 * Performs a {@link #cutPolygon(double[], double[], int) cutPolygon} with the
	 * specified plane and every polygon's index in this origami's
	 * {@link #getPolygons() polygon list}, and reflects some of the
	 * {@link #getVertices() vertices} over the plane. The vertices located on the
	 * same side of the plane as where the specified normal vector is pointing to
	 * will be the ones reflected over the plane.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 */
	protected void internalReflectionFold(double[] ppoint, double[] pnormal) {

		shrink();

		cutpolygon_nodes = new ArrayList<>();
		cutpolygon_pairs = new ArrayList<>();
		last_cut_polygons = new ArrayList<>();

		int facenum = polygonsSize;
		for (int i = 0; i < facenum; i++) {

			if (isNonDegenerate(i)) {
				cutPolygon(ppoint, pnormal, i);
			}
		}

		double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];
		for (int i = 0; i < verticesSize; i++) {

			double[] ipoint = vertices.get(i);
			if (ipoint[0] * pnormal[0] + ipoint[1] * pnormal[1] + ipoint[2] * pnormal[2] - konst > 0) {

				double[] img = Geometry.reflection(ipoint, ppoint, pnormal);
				vertices.set(i, img);
			}
		}
	}

	/**
	 * Passes the arguments in the same order to the
	 * {@link OrigamiGen1#polygonSelect(double[], double[], int) polygonSelect} method,
	 * and reflects every {@link #getPolygons() polygon} listed therein over the
	 * specified plane. <br>
	 * Reunites previously {@link OrigamiGen1#cutPolygon(double[], double[], int) split}
	 * polygons if possible.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 * @param polygonIndex
	 *            The index of the polygon to include in
	 *            {@link OrigamiGen1#polygonSelect(double[], double[], int)
	 *            polygonSelect}.
	 */
	protected void internalReflectionFold(double[] ppoint, double[] pnormal, int polygonIndex) {

		ArrayList<Integer> selection = polygonSelect(ppoint, pnormal, polygonIndex);

		for (int i = 0; i < verticesSize; i++) {

			for (int elem : selection) {

				if (polygons.get(elem).contains(i)) {

					double[] ipoint = vertices.get(i);
					double[] img = Geometry.reflection(ipoint, ppoint, pnormal);
					vertices.set(i, img);
					break;
				}
			}
		}

		for (int i = 0; i < cutpolygon_pairs.size(); i++) {

			if (!(selection.contains(cutpolygon_pairs.get(i)[0]) || selection.contains(cutpolygon_pairs.get(i)[1]))) {
				polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
			}
		}

		for (int[] pair : cutpolygon_pairs) {

			if (!(selection.contains(pair[0]) || selection.contains(pair[1]))) {
				polygons.set(pair[1], new ArrayList<Integer>());
			}
		}

		cutpolygon_pairs = new ArrayList<>();
		last_cut_polygons = new ArrayList<>();

		shrink(polygonIndex);
	}

	/**
	 * Performs a {@link #cutPolygon(double[], double[], int) cutPolygon} with the
	 * specified plane and every polygon's index in this origami's
	 * {@link #getPolygons() polygon list}, and if the intersection of the plane and
	 * the origami is a non-degenerate line, rotates some of the
	 * {@link #getVertices() vertices} around that line by the specified angle. <br>
	 * In this case, every vertex located on the same side of the plane as where the
	 * specified normal vector is pointing to will be rotated around the line. As
	 * there is no exclusive 'clockwise' direction in a 3-dimensional space, the
	 * rotation's direction will be decided on a whim.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 * @param phi
	 *            The angle of the rotation.
	 * @return 0 if the rotation has been performed; 1 if it has not.
	 */
	protected int internalRotationFold(double[] ppoint, double[] pnormal, int phi) {

		shrink();

		cutpolygon_nodes = new ArrayList<>();
		cutpolygon_pairs = new ArrayList<>();
		last_cut_polygons = new ArrayList<>();

		int facenum = polygonsSize;
		for (int i = 0; i < facenum; i++) {

			if (isNonDegenerate(i)) {
				cutPolygon(ppoint, pnormal, i);
			}
		}

		ArrayList<Integer> foldingpoints = new ArrayList<>();
		double konst = ppoint[0] * pnormal[0] + ppoint[1] * pnormal[1] + ppoint[2] * pnormal[2];

		for (int i = 0; i < verticesSize; i++) {

			double[] ipoint = vertices.get(i);
			if (Geometry.isPointOnPlane(ppoint, pnormal, ipoint)) {
				foldingpoints.add(i);
			}
		}

		boolean collin = false;
		int farpoint = -1;
		double dist_max = -1;

		if (foldingpoints.size() >= 2) {

			for (int fp : foldingpoints) {

				if (Geometry
						.vectorLength(Geometry.vectorDiff(vertices.get(fp), vertices.get(foldingpoints.get(0)))) > 0) {

					collin = true;
					if (Geometry.vectorLength(
							Geometry.vectorDiff(vertices.get(fp), vertices.get(foldingpoints.get(0)))) > dist_max) {

						farpoint = fp;
						dist_max = Geometry.vectorLength(
								Geometry.vectorDiff(vertices.get(fp), vertices.get(foldingpoints.get(0))));
					}
				}
			}
		}

		for (int i = 1; i < foldingpoints.size() && i != farpoint; i++) {

			if (Geometry.vectorLength(Geometry.crossProduct(
					Geometry.vectorDiff(vertices.get(foldingpoints.get(0)), vertices.get(foldingpoints.get(i))),
					Geometry.vectorDiff(vertices.get(farpoint), vertices.get(foldingpoints.get(i))))) > Geometry
							.vectorLength(
									Geometry.vectorDiff(vertices.get(foldingpoints.get(0)), vertices.get(farpoint)))) {

				collin = false;
				break;
			}
		}

		if (collin) {

			double[] dirvec = Geometry.vectorDiff(vertices.get(foldingpoints.get(0)), vertices.get(farpoint));
			double sinphi = Math.sin((double) phi * Math.PI / 180);
			double cosphi = Math.cos((double) phi * Math.PI / 180);

			for (int i = 0; i < verticesSize; i++) {

				double[] ipoint = vertices.get(i);
				if (ipoint[0] * pnormal[0] + ipoint[1] * pnormal[1] + ipoint[2] * pnormal[2] - konst > 0) {

					double[] img = Geometry.rotation(ipoint, vertices.get(foldingpoints.get(0)), dirvec, sinphi,
							cosphi);
					vertices.set(i, img);
				}
			}
			return 0;

		}
		else {
			return 1;
		}
	}

	/**
	 * Passes the arguments in the same order to the
	 * {@link OrigamiGen1#polygonSelect(double[], double[], int) polygonSelect} method,
	 * and if the resulting family of {@link #getPolygons() polygons} intersects the
	 * specified plane in a non-degenerate line, rotates these polygons by the
	 * specified angle around the line. As there is no exclusive 'clockwise'
	 * direction in a 3-dimensional space, the rotation's direction will be decided
	 * on a whim. <br>
	 * Reunites previously {@link OrigamiGen1#cutPolygon(double[], double[], int) split}
	 * polygons if possible.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 * @param polygonIndex
	 *            The index of the polygon to include in
	 *            {@link OrigamiGen1#polygonSelect(double[], double[], int)
	 *            polygonSelect}.
	 */
	protected void internalRotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex) {

		ArrayList<Integer> selection = polygonSelect(ppoint, pnormal, polygonIndex);
		ArrayList<Integer> foldingpoints = new ArrayList<>();

		for (int i = 0; i < verticesSize; i++) {

			double[] ipont = vertices.get(i);
			if (Geometry.isPointOnPlane(ppoint, pnormal, ipont)) {

				for (int elem : selection) {

					if (polygons.get(elem).contains(i)) {

						foldingpoints.add(i);
						break;
					}
				}
			}
		}

		boolean collin = false;
		int farpoint = -1;
		double dist_max = -1;

		if (foldingpoints.size() >= 2) {

			for (int hp : foldingpoints) {

				if (Geometry
						.vectorLength(Geometry.vectorDiff(vertices.get(hp), vertices.get(foldingpoints.get(0)))) > 0) {

					collin = true;
					if (Geometry.vectorLength(
							Geometry.vectorDiff(vertices.get(hp), vertices.get(foldingpoints.get(0)))) > dist_max) {

						farpoint = hp;
						dist_max = Geometry.vectorLength(
								Geometry.vectorDiff(vertices.get(hp), vertices.get(foldingpoints.get(0))));
					}
				}
			}
		}

		for (int i = 1; i < foldingpoints.size() && i != farpoint; i++) {

			if (Geometry.vectorLength(Geometry.crossProduct(
					Geometry.vectorDiff(vertices.get(foldingpoints.get(0)), vertices.get(foldingpoints.get(i))),
					Geometry.vectorDiff(vertices.get(farpoint), vertices.get(foldingpoints.get(i))))) > Geometry
							.vectorLength(
									Geometry.vectorDiff(vertices.get(foldingpoints.get(0)), vertices.get(farpoint)))) {

				collin = false;
				break;
			}
		}

		if (collin) {

			double[] dirvec = Geometry.vectorDiff(vertices.get(foldingpoints.get(0)), vertices.get(farpoint));
			double sinphi = Math.sin((double) phi * Math.PI / 180);
			double cosphi = Math.cos((double) phi * Math.PI / 180);

			for (int i = 0; i < verticesSize; i++) {
				for (int tag : selection) {

					if (polygons.get(tag).contains(i)) {

						double[] ipoint = vertices.get(i);
						double[] img = Geometry.rotation(ipoint, vertices.get(foldingpoints.get(0)), dirvec, sinphi,
								cosphi);
						vertices.set(i, img);
						break;
					}
				}
			}
		}

		for (int i = 0; i < cutpolygon_pairs.size(); i++) {

			if (!(selection.contains(cutpolygon_pairs.get(i)[0]) || selection.contains(cutpolygon_pairs.get(i)[1]))) {
				polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
			}

		}

		for (int[] pair : cutpolygon_pairs) {

			if (!(selection.contains(pair[0]) || selection.contains(pair[1]))) {
				polygons.set(pair[1], new ArrayList<Integer>());
			}
		}

		cutpolygon_pairs = new ArrayList<>();
		last_cut_polygons = new ArrayList<>();

		shrink(polygonIndex);
	}

	/**
	 * Performs a {@link #cutPolygon(double[], double[], int) cutPolygon} with the
	 * specified plane and every polygon's index in this origami's
	 * {@link #getPolygons() polygon list}, and deletes every polygon that is on the
	 * same side of the plane as where the specified normal vector is pointing to.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 */
	protected void internalMutilation(double[] ppoint, double[] pnormal) {

		shrink();

		cutpolygon_nodes = new ArrayList<>();
		cutpolygon_pairs = new ArrayList<>();
		last_cut_polygons = new ArrayList<>();
		int pnum = polygonsSize;

		for (int i = 0; i < pnum; i++) {

			if (isNonDegenerate(i)) {
				cutPolygon(ppoint, pnormal, i);
			}
		}

		double konst = Geometry.scalarProduct(ppoint, pnormal);
		for (int i = 0; i < polygonsSize; i++) {
			for (int vert : polygons.get(i)) {

				if (Geometry.scalarProduct(vertices.get(vert), pnormal) > konst
						&& !Geometry.isPointOnPlane(ppoint, pnormal, vertices.get(vert))) {

					polygons.set(i, new ArrayList<Integer>());
					break;
				}
			}
		}
	}

	/**
	 * Passes the arguments in the same order to the
	 * {@link OrigamiGen1#polygonSelect(double[], double[], int) polygonSelect} method,
	 * and deletes every {@link #getPolygons() polygon} listed therein. <br>
	 * Reunites previously {@link OrigamiGen1#cutPolygon(double[], double[], int) split}
	 * polygons if possible.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 * @param polygonIndex
	 *            The index of the polygon to include in
	 *            {@link OrigamiGen1#polygonSelect(double[], double[], int)
	 *            polygonSelect}.
	 */
	protected void internalMutilation(double[] ppoint, double[] pnormal, int polygonIndex) {

		List<Integer> selection = polygonSelect(ppoint, pnormal, polygonIndex);
		double konst = Geometry.scalarProduct(ppoint, pnormal);
		for (int i : selection) {

			List<Integer> poly = polygons.get(i);
			// this is just double-checking; the code should work without it
			for (int vert : poly) {
				if (Geometry.scalarProduct(vertices.get(vert), pnormal) > konst) {

					polygons.set(i, new ArrayList<Integer>());
					break;
				}
			}
		}

		for (int i = 0; i < cutpolygon_pairs.size(); i++) {

			if (!(selection.contains(cutpolygon_pairs.get(i)[0]) || selection.contains(cutpolygon_pairs.get(i)[1]))) {
				polygons.set(cutpolygon_pairs.get(i)[0], last_cut_polygons.get(i));
			}
		}

		for (int[] pair : cutpolygon_pairs) {

			if (!(selection.contains(pair[0]) || selection.contains(pair[1]))) {
				polygons.set(pair[1], new ArrayList<Integer>());
			}
		}

		cutpolygon_pairs = new ArrayList<>();
		last_cut_polygons = new ArrayList<>();

		shrink(polygonIndex);
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#reflectionFold(double[], double[])
	 */
	@Override
	public void reflectionFold(double[] ppoint, double[] pnormal) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_REFLECTION, ppoint, pnormal, 0, 0);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#reflectionFold(double[], double[], int)
	 */
	@Override
	public void reflectionFold(double[] ppoint, double[] pnormal, int polygonIndex) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_REFLECTION_P, ppoint, pnormal, polygonIndex, 0);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#rotationFold(double[], double[], int)
	 */
	@Override
	public void rotationFold(double[] ppoint, double[] pnormal, int phi) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_ROTATION, ppoint, pnormal, 0, phi);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#rotationFold(double[], double[], int, int)
	 */
	@Override
	public void rotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_ROTATION_P, ppoint, pnormal, polygonIndex, phi);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#crease(double[], double[])
	 */
	@Override
	public void crease(double[] ppoint, double[] pnormal) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_CREASE, ppoint, pnormal, 0, 0);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#mutilation(double[], double[])
	 */
	@Override
	public void mutilation(double[] ppoint, double[] pnormal) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_MUTILATION, ppoint, pnormal, 0, 0);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#mutilation(double[], double[], int)
	 */
	@Override
	public void mutilation(double[] ppoint, double[] pnormal, int polygonIndex) {

		history.subList(historyPointer, history.size()).clear();
		historyStream.subList(historyPointer, historyStream.size()).clear();
		addCommand(FoldingAction.FOLD_MUTILATION_P, ppoint, pnormal, polygonIndex, 0);
		execute(historyPointer, 1);
		historyPointer++;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#reset()
	 */
	@Override
	public final void reset() {

		if (paperType == PaperType.A4) {

			verticesSize = 0;
			vertices.clear();
			addVertex(0, 0, 0);
			addVertex(424.3, 0, 0);
			addVertex(424.3, 300, 0);
			addVertex(0, 300, 0);
			vertices2d = new ArrayList<double[]>(vertices);
			polygonsSize = 0;
			polygons.clear();
			ArrayList<Integer> sokszog0 = new ArrayList<>();
			sokszog0.add(0);
			sokszog0.add(1);
			sokszog0.add(2);
			sokszog0.add(3);
			addPolygon(sokszog0);
			corners = new ArrayList<double[]>(vertices);
		}

		if (paperType == PaperType.Square) {

			verticesSize = 0;
			vertices.clear();
			addVertex(0, 0, 0);
			addVertex(400, 0, 0);
			addVertex(400, 400, 0);
			addVertex(0, 400, 0);
			vertices2d = new ArrayList<double[]>(vertices);
			polygonsSize = 0;
			polygons.clear();
			ArrayList<Integer> sokszog0 = new ArrayList<>();
			sokszog0.add(0);
			sokszog0.add(1);
			sokszog0.add(2);
			sokszog0.add(3);
			addPolygon(sokszog0);
			corners = new ArrayList<double[]>(vertices);
		}

		if (paperType == PaperType.Hexagon) {

			verticesSize = 0;
			vertices.clear();
			addVertex(300, 346.41, 0);
			addVertex(400, 173.205, 0);
			addVertex(300, 0, 0);
			addVertex(100, 0, 0);
			addVertex(0, 173.205, 0);
			addVertex(100, 346.41, 0);
			vertices2d = new ArrayList<double[]>(vertices);
			polygonsSize = 0;
			polygons.clear();
			ArrayList<Integer> sokszog0 = new ArrayList<>();
			sokszog0.add(5);
			sokszog0.add(4);
			sokszog0.add(3);
			sokszog0.add(2);
			sokszog0.add(1);
			sokszog0.add(0);
			addPolygon(sokszog0);
			corners = new ArrayList<double[]>(vertices);
		}

		if (paperType == PaperType.Dollar) {

			verticesSize = 0;
			vertices.clear();
			addVertex(0, 0, 0);
			addVertex(400, 0, 0);
			addVertex(400, 170, 0);
			addVertex(0, 170, 0);
			vertices2d = new ArrayList<double[]>(vertices);
			polygonsSize = 0;
			polygons.clear();
			ArrayList<Integer> sokszog0 = new ArrayList<>();
			sokszog0.add(0);
			sokszog0.add(1);
			sokszog0.add(2);
			sokszog0.add(3);
			addPolygon(sokszog0);
			corners = new ArrayList<double[]>(vertices);
		}

		if (paperType == PaperType.Forint) {

			verticesSize = 0;
			vertices.clear();
			addVertex(0, 0, 0);
			addVertex(400, 0, 0);
			addVertex(400, 181.82, 0);
			addVertex(0, 181.82, 0);
			vertices2d = new ArrayList<double[]>(vertices);
			polygonsSize = 0;
			polygons.clear();
			ArrayList<Integer> sokszog0 = new ArrayList<>();
			sokszog0.add(0);
			sokszog0.add(1);
			sokszog0.add(2);
			sokszog0.add(3);
			addPolygon(sokszog0);
			corners = new ArrayList<double[]>(vertices);
		}

		if (paperType == PaperType.Custom) {

			verticesSize = 0;
			vertices.clear();
			for (double[] pont : corners) {
				addVertex(pont[0], pont[1], 0);
			}
			vertices2d = new ArrayList<double[]>(vertices);

			polygonsSize = 0;
			polygons.clear();
			ArrayList<Integer> sokszog0 = new ArrayList<>();
			for (int i = 0; i < verticesSize; i++) {
				sokszog0.add(i);
			}
			addPolygon(sokszog0);
		}

		border = new ArrayList<Integer>(polygons.get(0));
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#execute()
	 */
	@Override
	public void execute() {

		for (int i = 0; i < historyPointer; i++) {

			FoldingAction fa = history.get(i);
			fa.execute(this);
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#execute(int, int)
	 */
	@Override
	public void execute(int index, int steps) {

		if (index + steps <= history.size()) {
			for (int i = index; i < index + steps && i >= 0; i++) {

				FoldingAction fa = history.get(i);
				fa.execute(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#undo()
	 */
	@Override
	public void undo() {

		if (historyPointer > 0) {

			historyPointer--;
			while (0 < historyPointer ? history.get(historyPointer - 1).foldID == FoldingAction.FOLD_CREASE : false) {
				historyPointer--;
			}
			reset();
			execute();
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#undo(int)
	 */
	@Override
	public void undo(int steps) {

		if (historyPointer >= steps) {

			historyPointer -= steps;
			reset();
			execute();
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#redo()
	 */
	@Override
	public void redo() {

		if (history.size() > historyPointer) {

			historyPointer++;
			while (history.get(historyPointer - 1).foldID == FoldingAction.FOLD_CREASE) {
				historyPointer++;
			}
			reset();
			execute();
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#redo(int)
	 */
	@Override
	public void redo(int steps) {

		if (historyPointer + steps <= history.size()) {

			historyPointer += steps;
			reset();
			execute();
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#redoAll()
	 */
	@Override
	public void redoAll() {

		if (history.size() > historyPointer) {

			historyPointer = history.size();
			reset();
			execute();
		}
	}

	/**
	 * Pushes the empty polygons of this origami as close to the end of the
	 * {@link #getPolygons() polygons} list as possible without moving the polygon
	 * at the specified index.
	 *
	 * @param polygonIndex
	 *            The zero-based index at which the polygon in the
	 *            {@link #getPolygons() polygons} list should be kept in place.
	 * @since 2013-09-04
	 */
	protected void shrink(int polygonIndex) {

		List<Integer> tmp = polygons.get(polygonIndex);
		removePolygon(polygonIndex);
		for (int i = 0; i < polygonsSize; i++) {
			if (polygons.get(i) == new ArrayList<Integer>() || polygons.get(i).isEmpty()) {

				removePolygon(i);
				i--;
			}
		}

		while (polygonIndex > polygonsSize) {

			addPolygon(new ArrayList<Integer>());
		}
		polygons.add(polygonIndex, tmp);
		polygonsSize++;
	}

	/**
	 * Removes every empty list from the {@link #getPolygons() polygons} of this
	 * origami.
	 *
	 * @since 2013-09-04
	 */
	protected void shrink() {

		for (int i = 0; i < polygonsSize; i++) {
			if (polygons.get(i) == new ArrayList<Integer>() || polygons.get(i).isEmpty()) {

				removePolygon(i);
				i--;
			}
		}
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#circumscribedSquareSize()
	 */
	@Override
	public double circumscribedSquareSize() {
		return Math.max(paperWidth(), paperHeight());
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#paperWidth()
	 */
	@Override
	public double paperWidth() {

		Double jobb = null, bal = null;
		for (double[] pont : corners) {

			bal = bal == null ? pont[0] : (bal > pont[0] ? pont[0] : bal);
			jobb = jobb == null ? pont[0] : (jobb < pont[0] ? pont[0] : jobb);
		}
		if (jobb == null) {
			jobb = 0d;
		}
		if (bal == null) {
			bal = 0d;
		}
		return jobb - bal;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#paperHeight()
	 */
	@Override
	public double paperHeight() {

		Double also = null, felso = null;
		for (double[] pont : corners) {

			also = also == null ? pont[1] : (also > pont[1] ? pont[1] : also);
			felso = felso == null ? pont[1] : (felso < pont[1] ? pont[1] : felso);
		}
		if (also == null) {
			also = 0d;
		}
		if (felso == null) {
			felso = 0d;
		}
		return felso - also;
	}

	private static final double[][] Origins = new double[][] {

			new double[] { 0, 0, 0 }, new double[] { 400, 0, 0 }, new double[] { 0, 400, 0 },
			new double[] { 0, 0, 400 } };

	/**
	 * Emulates the plane equation compression done by the
	 * {@link #commandBlock(int, double[], double[], int, int) commandBlock} method.
	 * Used for previewing.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 * @return An array containing the 3-dimensional coordinates of a point the
	 *         compressed plane goes through. Can be arbitrarily far from
	 *         {@code ppoint}.
	 */
	protected static double[] planarPointRound(double[] ppoint, double[] pnormal) {

		double dist_max = -1;
		int used_origin = 0;
		double[] planeptnv = new double[] { 0, 0, 0 };

		for (int ii = 0; ii < Origins.length; ii++) {

			double[] basepoint = Geometry.linePlaneIntersection(Origins[ii], pnormal, ppoint, pnormal);
			if (Geometry.vectorLength(Geometry.vectorDiff(basepoint, Origins[ii])) > dist_max) {

				planeptnv = Geometry.vectorDiff(basepoint, Origins[ii]);
				dist_max = Geometry.vectorLength(planeptnv);
				used_origin = ii;
			}
		}
		int Xe = (int) planeptnv[0];
		int Ye = (int) planeptnv[1];
		int Ze = (int) planeptnv[2];
		int Xt = (int) Math.round((Math.abs(planeptnv[0] - Xe)) * 256 * 256);
		int Yt = (int) Math.round((Math.abs(planeptnv[1] - Ye)) * 256 * 256);
		int Zt = (int) Math.round((Math.abs(planeptnv[2] - Ze)) * 256 * 256);
		return new double[] { (double) Xe + Math.signum(Xe) * Xt / 256 / 256 + Origins[used_origin][0],
				(double) Ye + Math.signum(Ye) * Yt / 256 / 256 + Origins[used_origin][1],
				(double) Ze + Math.signum(Ze) * Zt / 256 / 256 + Origins[used_origin][2] };
	}

	/**
	 * Emulates the plane equation compression done by the
	 * {@link #commandBlock(int, double[], double[], int, int) commandBlock} method.
	 * Used for previewing.
	 *
	 * @param ppoint
	 *            An array containing the 3-dimensional coordinates of a point the
	 *            plane goes through as {@code double}s.
	 * @param pnormal
	 *            An array containing the 3-dimensional coordinates the plane's
	 *            normal vector as {@code double}s.
	 * @return An array containing the 3-dimensional coordinates of the compressed
	 *         plane's normal vector.
	 */
	protected static double[] normalvectorRound(double[] ppoint, double[] pnormal) {

		double dist_max = -1;
		double[] planeptnv = new double[] { 0, 0, 0 };

		for (double[] origo : Origins) {

			double[] basepoint = Geometry.linePlaneIntersection(origo, pnormal, ppoint, pnormal);
			if (Geometry.vectorLength(Geometry.vectorDiff(basepoint, origo)) > dist_max) {
				planeptnv = Geometry.vectorDiff(basepoint, origo);
				dist_max = Geometry.vectorLength(planeptnv);
			}
		}
		double sgn = 1;
		if (Geometry.scalarProduct(pnormal, planeptnv) < 0) {
			sgn = -1;
		}
		int Xe = (int) planeptnv[0];
		int Ye = (int) planeptnv[1];
		int Ze = (int) planeptnv[2];
		int Xt = (int) Math.round((Math.abs(planeptnv[0] - Xe)) * 256 * 256);
		int Yt = (int) Math.round((Math.abs(planeptnv[1] - Ye)) * 256 * 256);
		int Zt = (int) Math.round((Math.abs(planeptnv[2] - Ze)) * 256 * 256);
		return new double[] { sgn * ((double) Xe + Math.signum(Xe) * Xt / 256 / 256),
				sgn * ((double) Ye + Math.signum(Ye) * Yt / 256 / 256),
				sgn * ((double) Ze + Math.signum(Ze) * Zt / 256 / 256) };
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#foldingLine(double[], double[])
	 */
	@Override
	public ArrayList<double[]> foldingLine(double[] ppoint, double[] pnormal) {

		double[] ppoint1 = planarPointRound(ppoint, pnormal);
		double[] pnormal1 = normalvectorRound(ppoint, pnormal);
		ArrayList<double[]> line = new ArrayList<>();
		for (int polygonIndex = 0; polygonIndex < polygonsSize; polygonIndex++) {

			if (isNonDegenerate(polygonIndex)) {

				double[] start = null, end = null;
				for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

					int j = (i + 1) % polygons.get(polygonIndex).size();
					if (Geometry.isPointOnPlane(ppoint1, pnormal1, vertices.get(polygons.get(polygonIndex).get(i)))) {

						end = start;
						start = vertices.get(polygons.get(polygonIndex).get(i));
					}
					else {

						if (Geometry.isPlaneBetweenPoints(ppoint1, pnormal1,
								vertices.get(polygons.get(polygonIndex).get(i)),
								vertices.get(polygons.get(polygonIndex).get(j)))
								&& !Geometry.isPointOnPlane(ppoint, pnormal,
										vertices.get(polygons.get(polygonIndex).get(j)))) {

							double[] dirvec = Geometry.vectorDiff(vertices.get(polygons.get(polygonIndex).get(i)),
									vertices.get(polygons.get(polygonIndex).get(j)));
							double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

							end = start;
							start = Geometry.linePlaneIntersection(ipoint, dirvec, ppoint1, pnormal1);
						}
					}
				}
				if (start != null && end != null) {

					line.add(start);
					line.add(end);
				}
			}
		}
		return line;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#foldingLine2d(double[], double[])
	 */
	@Override
	public ArrayList<double[]> foldingLine2d(double[] ppoint, double[] pnormal) {

		double[] ppoint1 = planarPointRound(ppoint, pnormal);
		double[] pnormal1 = normalvectorRound(ppoint, pnormal);
		ArrayList<double[]> line = new ArrayList<>();
		for (int polygonIndex = 0; polygonIndex < polygonsSize; polygonIndex++) {

			if (isNonDegenerate(polygonIndex)) {

				double[] start = null, end = null;
				for (int i = 0; i < polygons.get(polygonIndex).size(); i++) {

					int j = (i + 1) % polygons.get(polygonIndex).size();
					if (Geometry.isPointOnPlane(ppoint1, pnormal1, vertices.get(polygons.get(polygonIndex).get(i)))) {

						end = start;
						start = vertices2d.get(polygons.get(polygonIndex).get(i));
					}
					else {

						if (Geometry.isPlaneBetweenPoints(ppoint1, pnormal1,
								vertices.get(polygons.get(polygonIndex).get(i)),
								vertices.get(polygons.get(polygonIndex).get(j)))
								&& !Geometry.isPointOnPlane(ppoint, pnormal,
										vertices.get(polygons.get(polygonIndex).get(j)))) {

							double[] dirvec = Geometry.vectorDiff(vertices.get(polygons.get(polygonIndex).get(i)),
									vertices.get(polygons.get(polygonIndex).get(j)));
							double[] ipoint = vertices.get(polygons.get(polygonIndex).get(i));

							double[] meet = Geometry.linePlaneIntersection(ipoint, dirvec, ppoint, pnormal);

							double weight1 = Geometry.vectorLength(
									Geometry.vectorDiff(meet, vertices.get(polygons.get(polygonIndex).get(j))));
							double weight2 = Geometry.vectorLength(
									Geometry.vectorDiff(meet, vertices.get(polygons.get(polygonIndex).get(i))));
							end = start;
							start = new double[] {
									(vertices2d.get(polygons.get(polygonIndex).get(i))[0] * weight1
											+ vertices2d.get(polygons.get(polygonIndex).get(j))[0] * weight2)
											/ (weight1 + weight2),
									(vertices2d.get(polygons.get(polygonIndex).get(i))[1] * weight1
											+ vertices2d.get(polygons.get(polygonIndex).get(j))[1] * weight2)
											/ (weight1 + weight2),
									0 };
						}
					}
				}
				if (start != null && end != null) {

					line.add(start);
					line.add(end);
				}
			}
		}
		return line;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#foldType(double[], double[])
	 */
	@Override
	public int foldType(double[] ppoint, double[] pnormal) {

		ArrayList<Integer> lines = new ArrayList<>();
		ArrayList<Integer> line_pols = new ArrayList<>();
		for (int i = 0; i < polygonsSize; i++) {
			for (int vert : polygons.get(i)) {

				if (Geometry.isPointOnPlane(ppoint, pnormal, vertices.get(vert))) {

					if (!lines.contains(vert)) {
						lines.add(vert);
					}
					if (isStrictlyNonDegenerate(i)) {
						line_pols.add(i);
					}
				}
			}
		}
		int components = -1;
		while (!line_pols.isEmpty()) {

			line_pols.removeAll(polygonSelect(ppoint, pnormal, line_pols.get(0)));
			components++;
		}

		if (lines.size() < 2) {
			return 0;
		}

		if (components == 1) {

			boolean collin = false;
			int farpoint = -1;
			double dist_max = -1;

			for (int hp : lines) {
				if (Geometry.vectorLength(Geometry.vectorDiff(vertices.get(hp), vertices.get(lines.get(0)))) > 0) {
					collin = true;
					if (Geometry.vectorLength(
							Geometry.vectorDiff(vertices.get(hp), vertices.get(lines.get(0)))) > dist_max) {

						farpoint = hp;
						dist_max = Geometry
								.vectorLength(Geometry.vectorDiff(vertices.get(hp), vertices.get(lines.get(0))));
					}
				}
			}

			if (collin) {
				for (int ii = 1; ii < lines.size() && ii != farpoint; ii++) {

					if (Geometry.vectorLength(Geometry.crossProduct(
							Geometry.vectorDiff(vertices.get(lines.get(0)), vertices.get(lines.get(ii))),
							Geometry.vectorDiff(vertices.get(farpoint), vertices.get(lines.get(ii))))) > Geometry
									.vectorLength(
											Geometry.vectorDiff(vertices.get(lines.get(0)), vertices.get(farpoint)))) {

						collin = false;
						break;
					}
				}
			}

			for (int i = 0; i < lines.size(); i += 2) {

				if (border.contains(lines.get(i))) {

					if (lines.size() == 2) {
						return -1;
					}
					if (collin) {
						return -2;
					}
					return -3;
				}
			}
			if (collin) {
				return -4;
			}
			return -5;
		}

		return components;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#foldType(double[], double[], int)
	 */
	@Override
	public int foldType(double[] ppoint, double[] pnormal, int polygonIndex) {

		ArrayList<Integer> line = new ArrayList<>();
		for (int spoly : polygonSelect(ppoint, pnormal, polygonIndex)) {
			for (int vert : polygons.get(spoly)) {

				if (Geometry.isPointOnPlane(ppoint, pnormal, vertices.get(vert))) {
					line.add(vert);
				}
			}
		}
		if (line.size() < 2) {
			return 0;
		}

		boolean collin = false;
		int farpoint = -1;
		double dist_max = -1;

		for (int fp : line) {
			if (Geometry.vectorLength(Geometry.vectorDiff(vertices.get(fp), vertices.get(line.get(0)))) > 0) {
				collin = true;
				if (Geometry
						.vectorLength(Geometry.vectorDiff(vertices.get(fp), vertices.get(line.get(0)))) > dist_max) {
					farpoint = fp;
					dist_max = Geometry.vectorLength(Geometry.vectorDiff(vertices.get(fp), vertices.get(line.get(0))));
				}
			}
		}

		if (collin) {
			for (int ii = 1; ii < line.size() && ii != farpoint; ii++) {

				if (Geometry.vectorLength(Geometry.crossProduct(
						Geometry.vectorDiff(vertices.get(line.get(0)), vertices.get(line.get(ii))),
						Geometry.vectorDiff(vertices.get(farpoint), vertices.get(line.get(ii))))) > Geometry
								.vectorLength(Geometry.vectorDiff(vertices.get(line.get(0)), vertices.get(farpoint)))) {

					collin = false;
					break;
				}
			}
		}

		for (int i = 0; i < line.size(); i += 2) {

			if (border.contains(line.get(i))) {

				if (line.size() == 2) {
					return -1;
				}
				if (collin) {
					return -2;
				}
				return -3;
			}
		}
		if (collin) {
			return -4;
		}
		return -5;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#complexity(int)
	 */
	@Override
	public int complexity(int step) {

		OrigamiGen1 origami = copy();
		if (step > origami.historyPointer) {
			return 0;
		}
		if (origami.history.get(step).foldID == FoldingAction.FOLD_REFLECTION) {

			origami.undo(origami.historyPointer - step);
			origami.redo(1);
			ArrayList<int[]> pairs = new ArrayList<int[]>(origami.cutpolygon_pairs);
			origami.undo(1);
			int maxcompl = 0;

			while (!pairs.isEmpty()) {

				ArrayList<int[]> pairs_local = new ArrayList<>();
				pairs_local.add(pairs.remove(0));
				for (int i = 0; i < pairs_local.size(); i++) {
					for (int ii = 0; ii < pairs.size(); ii++) {

						for (int vert : origami.polygons.get(pairs.get(ii)[0])) {
							if (origami.polygons.get(pairs_local.get(i)[0]).contains(vert)) {

								pairs_local.add(pairs.remove(ii));
								ii--;
								break;
							}
						}
					}
				}
				if (pairs_local.size() - 1 > maxcompl) {
					maxcompl = pairs_local.size() - 1;
				}
			}

			return maxcompl;
		}
		if (origami.history.get(step).foldID == FoldingAction.FOLD_REFLECTION_P) {

			origami.undo(origami.historyPointer - step + 1);
			origami.redo(1);

			double[] point = origami.history.get(step).ppoint;
			double[] normal = origami.history.get(step).pnormal;
			int index = origami.history.get(step).polygonIndex;

			ArrayList<int[]> pairs = new ArrayList<int[]>(origami.cutpolygon_pairs);
			ArrayList<Integer> selection = origami.polygonSelect(point, normal, index);
			int compl = 0;

			for (int[] pair : pairs) {
				if (selection.contains(pair[0]) || selection.contains(pair[1])) {
					compl++;
				}
			}

			return compl > 0 ? compl - 1 : 0;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#difficulty()
	 */
	@Override
	public int difficulty() {

		OrigamiGen1 origami = copy();
		origami.redoAll();

		int sum = 0;
		for (int i = 0; i < origami.history.size(); i++) {
			sum += origami.complexity(i);
		}
		return sum;
	}

	public static int difficultyLevel(int difficulty) {

		if (difficulty == 0) {
			return 0;
		}
		if (difficulty <= 50) {
			return 1;
		}
		if (difficulty <= 100) {
			return 2;
		}
		if (difficulty <= 200) {
			return 3;
		}
		if (difficulty <= 400) {
			return 4;
		}
		if (difficulty <= 800) {
			return 5;
		}
		return 6;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#findPolygonContaining(double)
	 */
	@Override
	public int findPolygonContaining(double... point2d) {

		// find the closest edge to point2d
		int[] closest_segment = new int[] { -1, -1 };
		double min_dist = -1;

		for (int i = 0; i < polygonsSize; i++) {

			if (isStrictlyNonDegenerate(i)) {

				List<Integer> poly = polygons.get(i);
				for (int ii = 0; ii < poly.size() - 1; ii++) {

					double[] s1 = vertices2d.get(poly.get(ii));
					double[] s2 = vertices2d.get(poly.get(ii + 1));
					double dist = Geometry.pointSegmentDistance(point2d, s1, s2);

					if (dist < min_dist || min_dist == -1) {

						closest_segment[0] = poly.get(ii);
						closest_segment[1] = poly.get(ii + 1);
						min_dist = dist;
					}
				}

				double[] s1 = vertices2d.get(poly.get(poly.size() - 1));
				double[] s2 = vertices2d.get(poly.get(0));
				double dist = Geometry.pointSegmentDistance(point2d, s1, s2);

				if (dist < min_dist || min_dist == -1) {

					closest_segment[0] = poly.get(poly.size() - 1);
					closest_segment[1] = poly.get(0);
					min_dist = dist;
				}
			}
		}

		// there are no more than two polygons where this edge can belong
		int closest_poly1 = -1, closest_poly2 = -1;
		for (int i = 0; i < polygonsSize; i++) {
			if (isStrictlyNonDegenerate(i)) {
				List<Integer> poly = polygons.get(i);
				for (int ii = 0; ii < poly.size() - 1; ii++) {
					if (closest_segment[0] == poly.get(ii) && closest_segment[1] == poly.get(ii + 1)
							|| closest_segment[1] == poly.get(ii) && closest_segment[0] == poly.get(ii + 1)) {
						if (closest_poly1 == -1) {
							closest_poly1 = i;
						}
						else {
							closest_poly2 = i;
						}
					}
				}
				if (closest_segment[0] == poly.get(poly.size() - 1) && closest_segment[1] == poly.get(0)
						|| closest_segment[1] == poly.get(poly.size() - 1) && closest_segment[0] == poly.get(0)) {
					if (closest_poly1 == -1) {
						closest_poly1 = i;
					}
					else {
						closest_poly2 = i;
					}
				}
			}
		}

		// one of these polygons is the polygon containing point2d
		if (closest_poly2 == -1) {
			return closest_poly1;
		}
		List<Integer> testpoly = polygons.get(closest_poly1);
		ArrayList<double[]> testpoints = new ArrayList<double[]>();
		for (int i = 0; i < testpoly.size(); i++) {
			testpoints.add(vertices2d.get(testpoly.get(i)));
		}
		if (Geometry.pointInsidePolygon(point2d, testpoints)) {
			return closest_poly1;
		}
		return closest_poly2;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#find3dImageOf(double)
	 */
	@Override
	public double[] find3dImageOf(double... point2d) {

		int poly_ind = findPolygonContaining(point2d);
		List<Integer> image_poly = polygons.get(poly_ind);
		double[] orig = vertices.get(image_poly.get(0));
		double[] orig2d = vertices2d.get(image_poly.get(0));

		for (int point1ind : image_poly) {
			for (int point2ind : image_poly) {

				double[] base1 = Geometry.vectorDiff(vertices.get(point1ind), orig);
				double[] base2 = Geometry.vectorDiff(vertices.get(point2ind), orig);

				if (Geometry.vectorLength(Geometry.crossProduct(base1, base2)) > 0) {

					base1 = Geometry.normalizeVector(base1);
					base2 = Geometry.normalizeVector(base2);

					double[] base1_2d = Geometry.vectorDiff(vertices2d.get(point1ind), orig2d);
					double[] base2_2d = Geometry.vectorDiff(vertices2d.get(point2ind), orig2d);
					base1_2d = Geometry.normalizeVector(base1_2d);
					base2_2d = Geometry.normalizeVector(base2_2d);

					double det = base1_2d[0] * base2_2d[1] - base1_2d[1] * base2_2d[0];

					double coord1 = Geometry.scalarProduct(Geometry.vectorDiff(point2d, orig2d),
							new double[] { base2_2d[1], -base2_2d[0], 0 }) / det;
					double coord2 = Geometry.scalarProduct(Geometry.vectorDiff(point2d, orig2d),
							new double[] { -base1_2d[1], base1_2d[0], 0 }) / det;

					double[] img = Geometry.vectorSum(orig, Geometry.vectorSum(Geometry.scalarMultiple(base1, coord1),
							Geometry.scalarMultiple(base2, coord2)));
					return img;

				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see origamieditor3d.origami.OrigamiI#copy()
	 */
	@Override
	public OrigamiGen1 copy() {

		OrigamiGen1 copy = new OrigamiGen1(this);
		return copy;
	}
}
