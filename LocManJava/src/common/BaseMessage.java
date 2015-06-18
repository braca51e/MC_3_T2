package common;

import java.io.Serializable;

public class BaseMessage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6199632973361628795L;

    public long time = 0;
    public int hopcount = 0;
}
