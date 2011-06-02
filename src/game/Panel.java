package game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;

public class Panel extends JPanel implements MouseMotionListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7712717603765541381L;

	private static final String IMS_INFO = "imsInfo.txt";

	private static final int PWIDTH = 900; // size of panel
	private static final int PHEIGHT = 700;

	private static final int NO_DELAYS_PER_YIELD = 16;

	private static final int MAX_FRAME_SKIPS = 5;
	private int topX, topY;
	public int mouseX, mouseY;
	private Robot robot;

	private long period; // the amount of time between animate. In nanosec

	// private GameRunner gameTop; //Don't total know what this is for.
	//
	private volatile boolean gameOver = false;
	private volatile boolean isPaused = false;

	private Thread animator;

	private volatile boolean running = false; // used to stop the animation
												// thread

	private long gameStartTime; // when the game started

	private Graphics dbg;

	private Pathfinder pathfinder;
	private MapRender map;
	private Input input;
	private Image dbImage = null;
	private Grid grid;

	private Dwarfs dwarfs;

	public Panel(GameRunner gr, long period) {
		this.period = period;

		setDoubleBuffered(false);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		setBackground(Color.BLACK);
		setFocusable(true);
		requestFocus(); // the JPanel now has focus, so receives key events

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				processKey(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				processMouse(e);
			}

		});

		addMouseMotionListener(this);
		// Basically every thing uses grid
		ImagesLoader imsLoader = new ImagesLoader(IMS_INFO);
		grid = new Grid();
		map = new MapRender(imsLoader, grid);
		dwarfs = new Dwarfs(imsLoader, grid);
		Dwarfs.Dwarf dwarf = dwarfs.new Dwarf(460, 46);
		dwarfs.saveDwarf(dwarf);
		dwarf = dwarfs.new Dwarf(460, 69);
		map.changeElement(460, 69, 64);
		map.changeElement(460, 46, 64);
		dwarfs.saveDwarf(dwarf);
		pathfinder = new Pathfinder(map, grid);
		input = new Input(map, dwarfs, pathfinder, grid);

	}

	public void mouseMoved(MouseEvent e) {
		int x = e.getX() - 92;
		int y = e.getY() - 70;
	
		if (!isPaused){
			mouseX += x;
			mouseY += y;
			
			System.out.println(mouseX);
			System.out.println(mouseY);
			try {
				robot = new Robot();
				robot.mouseMove(topX + 100, topY + 100);
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	private void processMouse(MouseEvent e)
	// If some one clicks the mouse
	{
		if (!isPaused){

			if (e.getButton() == 1) {
				input.leftClick(mouseX, mouseY);
			}
			if (e.getButton() == 3) {
				input.rightClick(mouseX, mouseY);

			}
		} else {
			if (e.getButton() == 1) {
				mouseX = e.getX();
				mouseY = e.getY();
				
				try {
					robot = new Robot();
					robot.mouseMove(topX + 100, topY + 100);
				} catch (AWTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				isPaused = false;
			}
		}

	}

	public void mouseDragged(MouseEvent e) {

	}

	// All the keyboard input is handle here
	private void processKey(KeyEvent e)
	// handles termination and game-play keys
	{
		int keyCode = e.getKeyCode();

		// termination keys
		// listen for esc, q, end, ctrl-c on the canvas to
		// allow a convenient exit from the full screen configuration
		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q)
				|| (keyCode == KeyEvent.VK_END)
				|| ((keyCode == KeyEvent.VK_C) && e.isControlDown())) {
			running = false;
		}else if(keyCode == KeyEvent.VK_P){
			if (isPaused == false){
				isPaused = true;
			}else{
				isPaused = false;
			}
			
		}
		if (keyCode == KeyEvent.VK_1) {
			input.changeElement(map.stone());
		}
		if (keyCode == KeyEvent.VK_2) {
			input.changeElement(map.floor());
		}
		if (keyCode == 37) {
			input.arrowLeft();
		}
		if (keyCode == 38) {
			input.arrowUp();
		}
		if (keyCode == 39) {
			input.arrowRight();
		}
		if (keyCode == 40) {
			input.arrowDown();
		}
		// game-play keys
		// if (!isPaused && !gameOver) {
		// if (keyCode == KeyEvent.VK_LEFT)
		// else if (keyCode == KeyEvent.VK_RIGHT)
		// else if (keyCode == KeyEvent.VK_DOWN)
		// }
	} // end of processKey()

	@Override
	public void addNotify()
	// wait for the JPanel to be added to the JFrame before starting
	{
		super.addNotify(); // creates the peer
		startGame(); // start the thread
	}

	private void startGame()
	// initialise and start the thread
	{
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	} // end of startGame()

	public void stopGame() {
		running = false;
	}

	// Basically if you you want things to move and stuff you call this...
	private void gameUpdate() {
		if (!isPaused && !gameOver) {
			input.changeBoxLocation(mouseX, mouseY);
			dwarfs.move();
		}
	} // end of gameUpdate()

	public void run() {

		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		gameStartTime = System.nanoTime();

		beforeTime = gameStartTime;

		running = true;
		while (running) {
			// The three most important things in the game
			gameUpdate();
			gameRender();
			paintScreen();

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;

			if (sleepTime > 0) { // some time left in this cycle
				try {
					Thread.sleep(sleepTime / 1000000L); // nano -> ms
				} catch (InterruptedException ex) {
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			} else { // sleepTime <= 0; the frame took longer than the period
				excess -= sleepTime; // store excess time value
				overSleepTime = 0L;

				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield(); // give another thread a chance to run
					noDelays = 0;
				}
			}

			beforeTime = System.nanoTime();

			/*
			 * If frame animation is taking too long, update the game state
			 * without rendering it, to get the updates/sec nearer to the
			 * required FPS.
			 */
			int skips = 0;
			while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= period;
				gameUpdate(); // update state but don't render
				skips++;
			}
		}
		map.save();
		System.exit(0);
	}

	public void resizeCanves(int width, int height) {
		createDBImage(width, height);
		map.resize(width, height);
	}

	private void createDBImage(int width, int height)
	// When you create a db Image it will update all or Make all you varables
	// for the game to run.
	{
		dbImage = createImage(width, height);
		if (dbImage == null) {
			System.out.println("dbImage is null");
			return;
		} else {
			dbg = dbImage.getGraphics();
			dbg.fillRect(0, 0, width, height);

		}
	}

	private void gameRender() {

		if (dbImage == null) {
			createDBImage(PWIDTH, PHEIGHT);
		}
		map.draw(dbg);
		dwarfs.draw(dbg);
		input.draw(dbg);

	}

	private void paintScreen()
	// use active rendering to put the buffered image on-screen
	{
		Graphics g;
		try {
			g = this.getGraphics();
			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage, 0, 0, null);

			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)
			Toolkit.getDefaultToolkit().sync();

			g.dispose();
		} catch (Exception e) {
			System.out.println("Graphics context error: " + e);
		}
	} // end of paintScreen()

	public void updateLoction(Point location) {
		topX = location.x;
		topY = location.y;
	}

}