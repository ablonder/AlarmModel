/*
 * GUI and main for Sweep, which I might not be using after all
 */

package AlarmModel;

import java.awt.Color;
import sweep.GUIStateSweep;
import sweep.SimStateSweep;

public class GUI extends GUIStateSweep {

	public GUI(SimStateSweep state) {
		super(state);
	}
	
	public static void main(String[] args) {
		GUI.initialize(Environment.class, Experimenter.class, GUI.class, 100, 100,
				Color.WHITE, Color.BLUE, false, spaces.SPARSE);
	}

}
