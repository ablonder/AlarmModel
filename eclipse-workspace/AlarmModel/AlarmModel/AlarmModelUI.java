/*
 * Implements GUI for alarm model
 * 
 * @author Aviva Blonder
 */

package AlarmModel;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.*;
import sim.portrayal.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.*;

public class AlarmModelUI extends GUIState {
	
	public Display2D display;
	public JFrame frame;
	public SparseGridPortrayal2D portrayal = new SparseGridPortrayal2D();
	
	public AlarmModelUI(SimState state) {
		super(state);
	}
	
	public static String getName() {
		return("Learned Heterospecific Alarm Call Recognition");
	}
	
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}
	
	public void setupPortrayals() {
		AlarmModel model = (AlarmModel) state;
		// tell the portrayals what to portray and how
		portrayal.setField(model.grid);
		// TODO - draw the agents with shapes based on their genotype and color based on behavior
		portrayal.setPortrayalForAll(new OvalPortrayal2D()
				{public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
					if(object.getClass() == Agent.class) {
						Agent agent = (Agent) object;
						// lets see if I can scale by foraging too
						scale = 1 + agent.foragecount*.005;
						if(agent.type == 'v') paint = Color.blue;
						else if(agent.type == 'i') paint = Color.orange;
						else paint = Color.green;
					} else {
						scale = 2;
						if(object.getClass() == Predator.class) paint = Color.red;
						else paint = Color.magenta;
					}
					super.draw(object, graphics, info);
				}
				});
		// reschedule displayer
		display.reset();
		display.setBackdrop(Color.white);
		// redraw display
		display.repaint();
	}
	
	public void init(Controller c) {
		super.init(c);
		display = new Display2D(600, 600, this);
		display.setClipping(false);
		
		frame = display.createFrame();
		frame.setTitle("Alarm Display");
		c.registerFrame(frame);
		frame.setVisible(true);
		display.attach(portrayal, "Alarm Environment");
	}
	
	public void quit() {
		super.quit();
		if(frame != null) frame.dispose();
		frame = null;
		display = null;
	}
	
	public static void main(String[] args) {
		// this needs to create an alarm model, make sure gui is set to true or it'll just run normally
		AlarmModel model = new AlarmModel("testInput.txt");
		AlarmModelUI vid = new AlarmModelUI(model);
		Console c = new Console(vid);
		c.setVisible(true);
	}

}
