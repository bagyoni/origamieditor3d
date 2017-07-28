package origamieditor3d.origami;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a three-dimensional rigid origami model consisting of convex
 * polygonal faces. Provides methods for manipulating the model with various
 * types of transformations.
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public interface Origami {

	int generation();

	/**
	 * Returns a list of all the vertices in this origami, regardless of whether
	 * they are used in any of the {@link #getPolygons() polygons} or not. The
	 * elements in this list are {@code double[]}s, each one representing the
	 * coordinates of a vertex in the origami space, i. e. the 3-dimensional space
	 * where this origami is edited. <br>
	 * In this list, every vertex has a corresponding preimage in the
	 * {@link #getVertices2d() vertices2d} list that has the same index.
	 *
	 * @return An {@link List} representing the vertices of this origami in the
	 *         origami space.
	 */
	List<double[]> getVertices();

	/**
	 * Returns the number of vertices in this origami, which is expected to be the
	 * {@link List#size() size} of the {@link #getVertices() vertices} and the
	 * {@link #getVertices2d() vertices2d} list of this origami at the same time.
	 *
	 * @return The number of vertices in this origami.
	 */
	int getVerticesSize();

	/**
	 * Returns a list of all the polygons in this origami. Each element in this list
	 * is a list that stores the vertices of one of this origami's
	 * polygons in the form of indices pointing into both the {@link #getVertices()
	 * vertices} and the {@link #getVertices2d() vertices2d} list. In each polygon,
	 * the vertices are arranged in a counter-clockwise winding order. <br>
	 * The polygons themselves do not have any particular order.
	 *
	 * @return As described above.
	 */
	List<List<Integer>> getPolygons();

	/**
	 * @return The number of polygons in this origami.
	 */
	int getPolygonsSize();

	/**
	 * Whenever a FoldingOperation is called on the origami, its identifier and its
	 * parameters are all merged into a single array and added to this list at the
	 * index corresponding to the {@link #getHistoryPointer()}. All elements above
	 * this index will be removed from the list.
	 *
	 * @return As described above.
	 */
	List<FoldingAction> getHistory();

	int getHistoryPointer();

	List<int[]> getHistoryStream();

	/**
	 * Returns the paper type of this origami. The value of this field will be taken
	 * into account by the {@link #reset() reset} method.
	 *
	 * @return As described above.
	 * @see PaperType
	 */
	PaperType getPaperType();

	/**
	 * Returns a copy of the original {@link #getVertices() vertices} list of this
	 * origami, as it was initialized after the last {@link #reset() reset} call. If
	 * {@code (papertype() == PaperType.Custom)}, the {@link #reset() reset} method
	 * will initialize the {@link #getVertices() vertices} and the
	 * {@link #getVertices2d() vertices2d} lists as copies of this list.
	 *
	 * @return As described above.
	 */
	List<double[]> getCorners();

	/**
	 * Returns a list of all the vertices in this origami, regardless of whether
	 * they are used in any of the {@link #getPolygons() polygons} or not. The
	 * elements in this list are {@code double[]}s, each one representing the
	 * coordinates of a vertex in the paper space, i. e. the 2-dimensional space
	 * where the vertices of this origami would be if it were unfolded. <br>
	 * In this list, every vertex has a corresponding image in the
	 * {@link #getVertices() vertices} list that has the same index.
	 *
	 * @return An {@link List} representing the vertices of this origami in the
	 *         paper space.
	 */
	List<double[]> getVertices2d();

	List<Integer> getBorder();

	void addCommand(int commandID, double[] ppoint, double[] pnormal, int polygonIndex, int phi);

	void addCommand(int[] cblock);

	/**
	 * Checks if the polygon at the specified index in the {@link #getPolygons()
	 * polygons} list is at least one-dimensional, i. e. it has two vertices with a
	 * positive distance between them.
	 *
	 * @param polygonIndex
	 *            The zero-based index at which the polygon to check is located in
	 *            the {@link #getPolygons() polygons} list.
	 * @return {@code false} iff the specified polygon is zero-dimensional.
	 */
	boolean isNonDegenerate(int polygonIndex);

	/**
	 * Checks if the polygon at the specified index in the {@link #getPolygons()
	 * polygons} list has a positive area.
	 *
	 * @param polygonIndex
	 *            The zero-based index at which the polygon to check is located in
	 *            the {@link #getPolygons() polygons} list.
	 * @return {@code false} iff the specified polygon is zero- or one-dimensional.
	 */
	boolean isStrictlyNonDegenerate(int polygonIndex);

	List<Integer> polygonSelect(double[] ppoint, double[] pnormal, int polygonIndex);

	/**
	 * Logs its own {@link OrigamiGen1#FoldingOperator FoldingOperator} identifier and
	 * the given parameters by calling
	 * {@link #addCommand(int, double[], double[], int, int) addCommand}. Calls
	 * {@link #internalReflectionFold(double[], double[]) its internal variant} with
	 * the same parameters.
	 * 
	 * @param ppoint
	 * @param pnormal
	 */
	void reflectionFold(double[] ppoint, double[] pnormal);

	void reflectionFold(double[] ppoint, double[] pnormal, int polygonIndex);

	void rotationFold(double[] ppoint, double[] pnormal, int phi);

	void rotationFold(double[] ppoint, double[] pnormal, int phi, int polygonIndex);

	void crease(double[] ppoint, double[] pnormal);

	void mutilation(double[] ppoint, double[] pnormal);

	void mutilation(double[] ppoint, double[] pnormal, int polygonIndex);

	/**
	 * Initializes the {@link #getCorners() corners}, {@link #getVertices()
	 * vertices}, {@link #getVertices2d() vertices2d} and {@link #getPolygons()
	 * polygons} lists of this origami depending on the value of its
	 * {@link #getPaperType() papertype}. The {@link #getCorners() corners},
	 * {@link #getVertices() vertices}, and {@link #getVertices2d() vertices2d}
	 * lists will always have the same initial vertices in the same order.
	 */
	void reset();

	/**
	 * Executes every {@link OrigamiGen1#FoldingOperation FoldingOperation} stored in
	 * this origami's {@link #getHistory() history}. Does not call {@link #reset()
	 * reset}.
	 */
	void execute();

	/**
	 * Executes the specified number of steps stored in the {@link #getHistory()
	 * history} of this origami starting from the specified index.
	 *
	 * @param index
	 *            The index of the first element in the in the {@link #getHistory()
	 *            history} to execute.
	 * @param steps
	 *            The number of {@link OrigamiGen1#FoldingOperation FoldingOperations}
	 *            to execute.
	 */
	void execute(int index, int steps);

	/**
	 * Restores the state of this origami to the one before the last
	 * {@link FoldingOperation folding operation} was executed. For this method to
	 * work, the user should never use any method with a protected signature in this
	 * class.
	 *
	 * @since 2013-09-05
	 */
	void undo();

	/**
	 * Equivalent to calling {@link #undo() undo} {@code steps} times, except it
	 * will not do anything if {@code steps > history_pointer}.
	 *
	 * @param steps
	 *            The number of steps to undo.
	 * @since 2013-09-05
	 */
	void undo(int steps);

	void redo();

	void redo(int steps);

	void redoAll();

	/**
	 * Returns the size of the smallest orthogonal square all the
	 * {@link #getCorners() corners} of this origami can fit in.
	 *
	 * @return As described above.
	 * @since 2013-10-31
	 */
	double circumscribedSquareSize();

	/**
	 * Returns the difference of the largest and the smallest first coordinate
	 * occurring within the {@link #getCorners() corners} list.
	 *
	 * @return As described above.
	 */
	double paperWidth();

	/**
	 * Returns the difference of the largest and the smallest second coordinate
	 * occuring within the {@link #getCorners() corners} list.
	 *
	 * @return As described above.
	 */
	double paperHeight();

	List<double[]> foldingLine(double[] ppoint, double[] pnormal);

	List<double[]> foldingLine2d(double[] ppoint, double[] pnormal);

	int foldType(double[] ppoint, double[] pnormal);

	int foldType(double[] ppoint, double[] pnormal, int polygonIndex);

	int complexity(int step);

	int difficulty();

	/**
	 * Returns the index of the polygon in the {@link #getPolygons() polygons} list
	 * that contains a specific point on the paper given in 2D (paper space)
	 * coordinates. Returns -1 if every polygon in this origami is
	 * {@link #isStrictlyNonDegenerate(int) degenerate}.
	 * 
	 * @param point2d
	 *            The 2D coordinates of the point.
	 * @return The index of the polygon containing the point.
	 * @since 2017-02-19
	 */
	int findPolygonContaining(double... point2d);

	/**
	 * Determines where a given point on the paper ends up in the 3D space, in the
	 * current state of this origami. For example, if an element of the
	 * {@link #getVertices2d() vertices2d} list is given, the corresponding element
	 * of the {@link #getVertices() vertices} list should be returned, unless the
	 * origami is malformed.
	 * 
	 * @param point2d
	 *            The 2D (paper space) coordinates of the point.
	 * @return The 3D (origami space) coordinates of the same point.
	 * @since 2017-02-19
	 */
	double[] find3dImageOf(double... point2d);

	Origami copy();
	
	public class FoldingAction {

		public static final int FOLD_REFLECTION = 1;
		public static final int FOLD_ROTATION = 2;
		public static final int FOLD_REFLECTION_P = 3;
		public static final int FOLD_ROTATION_P = 4;
		public static final int FOLD_CREASE = 5;
		public static final int FOLD_MUTILATION = 6;
		public static final int FOLD_MUTILATION_P = 7;

		public FoldingAction(int foldID, double[] ppoint, double[] pnormal, int polygonIndex, int phi) {

			this.foldID = foldID;
			this.ppoint = ppoint;
			this.pnormal = pnormal;
			this.polygonIndex = polygonIndex;
			this.phi = phi;
		}

		public final void execute(OrigamiGen1 origami) {

			switch (foldID) {

			case FOLD_CREASE:
				origami.internalRotationFold(ppoint, pnormal, 0);
				break;
			case FOLD_REFLECTION:
				origami.internalReflectionFold(ppoint, pnormal);
				break;
			case FOLD_REFLECTION_P:
				origami.internalReflectionFold(ppoint, pnormal, polygonIndex);
				break;
			case FOLD_ROTATION:
				origami.internalRotationFold(ppoint, pnormal, phi);
				break;
			case FOLD_ROTATION_P:
				origami.internalRotationFold(ppoint, pnormal, phi, polygonIndex);
				break;
			case FOLD_MUTILATION:
				origami.internalMutilation(ppoint, pnormal);
				break;
			case FOLD_MUTILATION_P:
				origami.internalMutilation(ppoint, pnormal, polygonIndex);
				break;
			}
		}

		public final int foldID;
		public final double[] ppoint;
		public final double[] pnormal;
		public final int polygonIndex;
		public final int phi;
	}

	/**
	 * Enumerates the preset paper types of the {@link OrigamiGen1} class.
	 */
	public enum PaperType {

		/**
		 * An Origami of this {@link #papertype() papertype} will have a single polygon
		 * with vertices {0, 0, 0}, (424.3, 0, 0} {424.3, 300, 0} and {300, 0, 0} when
		 * {@link #reset() reset}.
		 */
		A4('A'),
		/**
		 * An Origami of this {@link #papertype() papertype} will have a single polygon
		 * with vertices {0, 0, 0}, (400, 0, 0} {400, 400, 0} and {0, 400, 0} when
		 * {@link #reset() reset}.
		 */
		Square('N'),
		/**
		 * An Origami of this {@link #papertype() papertype} will have a single polygon
		 * with vertices {300, 346.41, 0}, {400, 173.205, 0}, {300, 0, 0}, {100, 0, 0},
		 * {0, 173.205, 0} and {100, 346.41, 0} when {@link #reset() reset}.
		 */
		Hexagon('H'),
		/**
		 * An Origami of this {@link #papertype() papertype} will have a single polygon
		 * with vertices {0, 0, 0}, {400, 0, 0}, {400, 170, 0} and {0, 170, 0} when
		 * {@link #reset() reset}.
		 */
		Dollar('D'),
		/**
		 * An Origami of this {@link #papertype() papertype} will have a single polygon
		 * with vertices {0, 0, 0}, {400, 0, 0}, {400, 181.82, 0} and {0, 181.82, 0}
		 * when {@link #reset() reset}.
		 */
		Forint('F'),
		/**
		 * An Origami of this {@link #papertype() papertype} will have a single polygon
		 * with the vertices obtained from the
		 * {@link Geometry#ccwWindingOrder(List) ccwWindingOrder} of its
		 * {@link #corners() corners} when {@link #reset() reset}.
		 */
		Custom('E');

		private final char ID;
		private static final Map<Character, PaperType> allid = new HashMap<>();

		static {

			for (PaperType p : PaperType.values()) {

				allid.put(p.ID, p);
			}
		}

		private PaperType(final char c) {

			ID = c;
		}

		/**
		 * @return The assigned unique {@code char} value of this PaperType.
		 */
		public char toChar() {

			return ID;
		}

		/**
		 * Returns the PaperType the specified {@code char} value is assigned to.
		 *
		 * @param c
		 *            The {@code char} value of the desired PaperType.
		 * @return The desired PaperType.
		 */
		public static PaperType forChar(char c) {

			return allid.get(c);
		}

		@Override
		public String toString() throws NullPointerException {

			if (super.equals(A4)) {

				return "A4";
			}
			else if (super.equals(Square)) {

				return "Square";
			}
			else if (super.equals(Hexagon)) {

				return "Regular hexagon";
			}
			else if (super.equals(Dollar)) {

				return "Dollar bill";
			}
			else if (super.equals(Forint)) {

				return "Forint bill";
			}
			else if (super.equals(Custom)) {

				return "Custom";
			}
			else {

				throw new NullPointerException();
			}
		}
	}
}