package common;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

public class InitializationMsg extends BaseMessage implements Serializable  {

    private static final long serialVersionUID = 6541462957486262100L;
    
    public ArrayList<Rectangle2D> locationAreas;
    
}
