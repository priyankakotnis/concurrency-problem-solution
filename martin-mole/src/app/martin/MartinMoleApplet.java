package app.martin;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class MartinMoleApplet extends Applet {

    private Martin martin;
    private Gate gate;
    private ExitingSensor sensor1;
    private EnteringSensor sensor2;
    private EnteringSensor sensor3;
    private ExitingSensor sensor4;
    private GateController gateController;
    private List<Enemy> enemies;
    private WarningIndicator warningIndicator;
    private MartinMoleCanvas martinMoleCanvas;
    private Thread enemyThreads[];
    private Thread martinThread;

    // State 
    private Button startMartinButton;
    private Checkbox frozenCheckbox;
    private Checkbox safeCheckbox;
    private int nEnemies = 1;

    public void init() {
        setBackground(Color.lightGray);
        setLayout(new BorderLayout());
        setSize(907, 500);
        addControlPanel();
        initialize();
    }

    private void initialize() throws HeadlessException {
        startMartinButton.setEnabled(true);
        frozenCheckbox.setState(false);
        // Canvas
        if (martinMoleCanvas != null) {
            remove(martinMoleCanvas);
        }
        martinMoleCanvas = new MartinMoleCanvas(this, nEnemies);
        add("North", martinMoleCanvas);
        repaint();
        //Create required objects
        buildModel();
    }

    private void addControlPanel() throws HeadlessException {
        // Controls
        frozenCheckbox = new Checkbox("Frozen", null, false);
        frozenCheckbox.setBackground(Color.lightGray);
        frozenCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (getFrozenCheckbox().getState()) {
                    martinMoleCanvas.freeze();
                } else {
                    martinMoleCanvas.thaw();
                }
            }
        });

        startMartinButton = new Button("Start Martin");
        startMartinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startMartin();
                startMartinButton.setEnabled(false);
            }
        });
        Button oneEnemyButton = new Button("One Enemy");
        oneEnemyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop();
                nEnemies = 1;
                initialize();
                start();
            }
        });
        Button twoEnemyButton = new Button("Two Enemies");
        twoEnemyButton.addActionListener((ActionEvent e) -> {
            stop();
            nEnemies = 2;
            initialize();
            start();
        });
        Button threeEnemyButton = new Button("Three Enemies");
        threeEnemyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop();
                nEnemies = 3;
                initialize();
                start();
            }
        });
        safeCheckbox = new Checkbox("Safe", null, true);
        safeCheckbox.setBackground(Color.lightGray);
        safeCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                stop();
                //Value of checkbox is passed to gate for safe/unsafe toggling.                
                initialize();
                startMartinButton.setEnabled(true);
                start();
            }
        });
        Panel controlPanel = new Panel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(frozenCheckbox);
        controlPanel.add(startMartinButton);
        controlPanel.add(oneEnemyButton);
        controlPanel.add(twoEnemyButton);
        controlPanel.add(threeEnemyButton);
        controlPanel.add(safeCheckbox);
        add("Center", controlPanel);
    }

    @Override
    public void start() {
        // Create and start threads
        enemyThreads = new Thread[nEnemies];
        for (int i = 0; i < nEnemies; i++) {
            enemyThreads[i] = new Thread(enemies.get(i));
        }
        for (int i = 0; i < nEnemies; i++) {
            enemyThreads[i].start();
        }
    }

    private void startMartin() {
        martinThread = new Thread(martin);
        martinThread.start();
    }

    @Override
    public void stop() {
        for (int i = 0; i < nEnemies; i++) {
            enemyThreads[i].interrupt();
        }
        if (martinThread != null) {
            martinThread.interrupt();
        }
    }

    /**
     * This method is used to create the objects of required classes. 
     */
    private void buildModel() {
        sensor1 = new ExitingSensor(1, martinMoleCanvas);
        sensor2 = new EnteringSensor(2, martinMoleCanvas);
        sensor3 = new EnteringSensor(3, martinMoleCanvas);
        sensor4 = new ExitingSensor(4, martinMoleCanvas);
        warningIndicator = new WarningIndicator(martinMoleCanvas);
        martin = new Martin(warningIndicator, martinMoleCanvas);
        gate = new Gate(martinMoleCanvas, safeCheckbox.getState());
        gateController = new GateController(gate, martinMoleCanvas);
        enemyThreads = new Thread[nEnemies];
        enemies = new ArrayList<>();
        for (int i = 0; i < nEnemies; i++) {
            Enemy enemy = new Enemy(i, gate, martinMoleCanvas);
            enemies.add(enemy);
        }
        // FSP renaming
        setUpActionSync();
    }

    /* 
     * Similar to Java's ActionListener, using Java 8 lambda function here.
     * The function below are similar to using say : 
     * martin.setLeaveAlias(
     *		new ActionAlias() {
     *			public void execute()  {
     *				sensor1.exit();			
     *			}
     *		}	
	 *    );
	 *    The idea behind this is to have the function calls made from Martin/Enemy's 
	 *    enter/exits in one place. 
	 *    As you can see, 
	 *    For Martin enter/exit triggering sensor. Sensor triggering gateController.
	 *    For Enemy enter/exit triggering sensors and warning indicator. Just as done in FSP.
     */
    private void setUpActionSync() {
       
        martin.setLeaveAlias(() -> {
            sensor1.exit();
        });
        sensor1.setExitAlias(() -> {
            gateController.leave();
        });
        martin.setEnterAlias(() -> {
            sensor2.enter();
        });
        sensor2.setEnterAlias(() -> {
            gateController.enter();
        });
        martin.setExitAlias(() -> {
            sensor4.exit();
        });
        sensor4.setExitAlias(() -> {
            gateController.exit();
        });
        for (Enemy enemy : enemies) {
            enemy.addEnterAlias(() -> {
                sensor3.enter();
            });
            enemy.addEnterAlias(() -> {
                warningIndicator.up();
            });
            enemy.addExitAlias(() -> {
                sensor4.exit();
            });
            enemy.addExitAlias(() -> {
                warningIndicator.down();
            });
        }
    }

    /**
     * @return the frozenCheckbox
     */
    public Checkbox getFrozenCheckbox() {
        return frozenCheckbox;
    }

}
