package origamieditor3d.ui.panel;

import javax.swing.JPanel;

import origamieditor3d.graphics.Camera;
import origamieditor3d.origami.Origami;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public abstract class Panel extends JPanel {

    private static final long serialVersionUID = 1L;

    public enum RulerMode {
        Normal, Neusis, Planethrough, Angle_bisector
    }

    protected Origami PanelOrigami;
    protected Camera PanelCamera;
    protected RulerMode rulerMode;
    
    protected Integer tracker_x, tracker_y;
    protected boolean trackerOn;
    protected boolean ready_to_paint;
    
    public abstract void reset();

    public abstract boolean isTracked();
    public abstract void setTracker(Camera refcam, int x, int y);
    public abstract void resetTracker();

    public abstract void rulerOn(Camera refcam, int x1, int y1, int x2, int y2);
    public abstract void rulerOff();

    public abstract void tiltTriangleTo(Camera refcam, Integer... xy);
    public abstract void resetTriangle();
    public abstract void grabTriangleAt(int vertIndex);
    
    public Camera panelCamera() {
        return PanelCamera;
    }
    
    public void update(Origami origami) {

        PanelOrigami = origami;
        ready_to_paint = true;
    }
    
    public void setRulerMode(RulerMode mode) {
        rulerMode = mode;
    }
    
    public void resetZoom() {
        if (PanelOrigami.circumscribedSquareSize() > 0) {
            PanelCamera.setZoom(0.8 * Math.min(getWidth(), getHeight())
                    / PanelOrigami.circumscribedSquareSize());
        }
    }
}
