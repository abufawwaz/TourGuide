package tourguide.tourguide;

import android.graphics.Color;
import android.view.Gravity;

/**
 * Created by tanjunrong on 6/20/15.
 */
public class Pointer {
    public final int gravity;
    public final int color;

    public Pointer() {
        this(Gravity.CENTER, Color.WHITE);
    }

    public Pointer(int gravity, int color) {
        this.gravity = gravity;
        this.color = color;
    }

}
