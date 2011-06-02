package game;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.swing.JFrame;

public class GameRunner extends JFrame implements WindowListener,
		ComponentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1754800786979400078L;

	private Panel p;

	private static int DEFAULT_FPS = 60;

	public GameRunner(long period) {
		// This is the set up information for the game. You want to change the core it is here
		// This is all the directories that are going to be need to play the game
		createDirs();
		createPanel(period);
		replaceMouse("images/dwarfico.png");
		setIcon("images/dwarfico.png");

		
	}
	
	private void createPanel(long period){
		// Setting information for the size title and Listeners that need in order to interact with the game
		setTitle("Dwarf");

		Container c = getContentPane(); // default BorderLayout used
		this.addComponentListener(this);
		p = new Panel(this, period);
		p.updateLoction(getLocation());
		c.add(p, "Center");
		setLocation(100, 100);
		p.addComponentListener(this);
		addWindowListener(this);
		pack();
		setResizable(true);
		setVisible(true);
	}

	private void createDirs() {
		// This should create the directories that are not there if they are there do nothing.
		// Currently there is not valid content checks in here. There should be. 
		File f = new File("./saves");
		try {
			if (f.mkdir())
				System.out.println("Directory Created " + f);
			else
				System.out.println("Directory is not created " + f);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
	}
	
	public void replaceMouse(String imageName){
		try {
			Robot robot = new Robot();
			robot.mouseMove(getLocation().x + 100, getLocation().y + 100);
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			    cursorImg, new Point(0, 0), "blank cursor");
		
		setCursor(blankCursor);
	}

	public void setIcon(String imageName) {
		URL url = this.getClass().getClassLoader().getResource(imageName);
		Image im = Toolkit.getDefaultToolkit().getImage(url);
		setIconImage(im);

	}

	public void windowOpened(WindowEvent e) {

	}

	public void windowClosing(WindowEvent e) {
		p.stopGame();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public static void main(String args[]) {
		// Do that conversations calculation for FPS. Time per sec in nanosecs
		long period = (long) 1000.0 / DEFAULT_FPS;
		new GameRunner(period * 1000000L); // ms --> nanosecs
	}

	public void componentResized(ComponentEvent e) {
		p.resizeCanves(getWidth(), getHeight());
	}

	public void componentMoved(ComponentEvent e) {
		System.out.println("moved");
		p.updateLoction(getLocation());
		
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

}
