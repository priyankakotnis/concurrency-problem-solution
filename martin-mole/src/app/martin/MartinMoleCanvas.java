package app.martin;

import java.awt.*;
import java.applet.*;
import java.util.Date;

/**
 * This class is a view class used for creating/updating the view.
 * @author Priyanka
 *
 */
class MartinMoleCanvas extends Canvas {

    enum EnemyWayPointEnum {
        GATE,
        ROAD_END
    }

    enum MartinWayPointEnum {
        HOUSE,
        COURTYARD,
        ROAD_END
    }
    private final static int ENEMY_START_X = 8;
    private final static int ENEMY_INIT_X_SPACING = 88;
    private final static int ENEMY_X_SPACING = 120;
    private final static int ROAD_Y = 250;
    private final static Point ROAD_END_WAYPOINT = new Point(800, ROAD_Y);
    private final static Point HOUSE_WAYPOINT = new Point(400, 180);
    private final static Point GATE_WAYPOINT = new Point(200, ROAD_Y);
    private final static Point COURTYARD_WAYPOINT = new Point(HOUSE_WAYPOINT.x, ROAD_Y - 30);
    private final static Point WARNING_INDICATOR = new Point(HOUSE_WAYPOINT.x - 40, ROAD_Y - 30);
    private Image enemyImage;
    private Image martinImage;
    private Image backdropImage;
    private Image offScreenImage;
    private Image gateUpImage;
    private Image gateDownImage;
    private Image gateImage;
    private Image houseImage;
    private Image warningIndicatorOnImage;
    private Image warningIndicatorOffImage;
    private Image warningIndicatorImage;
    private Image[] sensorImage = new Image[4];
    private Image[] sensorSignalImage = new Image[4];
    private Image[] sensorNoSignalImage = new Image[4];
    private Dimension offscreensize;
    private Graphics offScreenGraphics;
    private AudioClip crashSound;
    private final Point[] enemies;
    private final Point martin;
    private final long startTime;
    private long[] sensorActivationTimes = new long[4];
    private Checkbox frozenCheckbox;
    private boolean frozen = false;
    private int cycleTime = 20;

    MartinMoleCanvas(MartinMoleApplet controller, int nEnemies) {
        super();
        frozenCheckbox = controller.getFrozenCheckbox();
        crashSound = controller.getAudioClip(controller.getDocumentBase(), "sound/crash.au");
        MediaTracker mediaTracker;
        mediaTracker = new MediaTracker(this);
        enemyImage = controller.getImage(controller.getDocumentBase(), "image/enemy.gif");
        mediaTracker.addImage(enemyImage, 0);
        martinImage = controller.getImage(controller.getDocumentBase(), "image/martin.gif");
        mediaTracker.addImage(martinImage, 1);
        backdropImage = controller.getImage(controller.getDocumentBase(), "image/backdrop.gif");
        mediaTracker.addImage(backdropImage, 2);
        warningIndicatorOnImage = controller.getImage(controller.getDocumentBase(), "image/warningOn.gif");
        mediaTracker.addImage(warningIndicatorOnImage, 3);
        warningIndicatorOffImage = controller.getImage(controller.getDocumentBase(), "image/warningOff.gif");
        mediaTracker.addImage(warningIndicatorOffImage, 4);
        gateUpImage = controller.getImage(controller.getDocumentBase(), "image/gateUp.gif");
        mediaTracker.addImage(gateUpImage, 5);
        gateDownImage = controller.getImage(controller.getDocumentBase(), "image/gateDown.gif");
        mediaTracker.addImage(gateDownImage, 6);
        houseImage = controller.getImage(controller.getDocumentBase(), "image/house.gif");
        mediaTracker.addImage(houseImage, 6);
        for (int sensorIndex = 0; sensorIndex <= 3; sensorIndex++) {
            Image onImage = controller.getImage(controller.getDocumentBase(), "image/sensor" + (sensorIndex + 1) + "Signal.gif");
            sensorSignalImage[sensorIndex] = onImage;
            mediaTracker.addImage(onImage, 7 + 2 * sensorIndex);
            Image offImage = controller.getImage(controller.getDocumentBase(), "image/sensor" + (sensorIndex + 1) + "NoSignal.gif");
            sensorNoSignalImage[sensorIndex] = offImage;
            sensorImage[sensorIndex] = offImage;
            mediaTracker.addImage(offImage, 8 + 2 * sensorIndex);
        }
        try {
            for (int i = 0; i < 17; i++) {
                mediaTracker.waitForID(i);
            }
        } catch (java.lang.InterruptedException e) {
            System.out.println("Couldn't load one of the images");
            e.printStackTrace();
        }
        warningIndicatorImage = warningIndicatorOffImage;
        gateImage = gateUpImage;
        setSize(backdropImage.getWidth(null), backdropImage.getHeight(null));
        startTime = new Date().getTime();
        frozen = false;
        enemies = new Point[nEnemies];
        for (int i = 0; i < nEnemies; i++) {
            enemies[i] = new Point();
            enemies[i].x = ENEMY_START_X - i * ENEMY_INIT_X_SPACING;
            enemies[i].y = ROAD_Y;
        }
        martin = new Point();
        martin.x = HOUSE_WAYPOINT.x;
        martin.y = HOUSE_WAYPOINT.y;
        repaint();
        log("View Initialized");
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }

    @Override
    public void update(Graphics g) {
        Dimension d = getSize();
        if ((offScreenImage == null) || (d.width != offscreensize.width)
            || (d.height != offscreensize.height)) {
            offScreenImage = createImage(backdropImage.getWidth(null), backdropImage.getHeight(null));
            offscreensize = d;
            offScreenGraphics = offScreenImage.getGraphics();
            offScreenGraphics.setFont(new Font("Helvetica", Font.BOLD, 36));
        }
        offScreenGraphics.setColor(Color.lightGray);
        offScreenGraphics.fillRect(0, 0, d.width, d.height);
        offScreenGraphics.drawImage(backdropImage, 0, 0, this);
        offScreenGraphics.drawImage(warningIndicatorImage, WARNING_INDICATOR.x, WARNING_INDICATOR.y, this);
        offScreenGraphics.drawImage(houseImage, HOUSE_WAYPOINT.x, HOUSE_WAYPOINT.y - 30, this);
        offScreenGraphics.drawImage(gateImage, GATE_WAYPOINT.x + 80, GATE_WAYPOINT.y - 85, this);
        offScreenGraphics.drawImage(martinImage, martin.x, martin.y, this);
        // Draw enemies
        for (int i = 0; i < enemies.length; i++) {
            offScreenGraphics.drawImage(enemyImage, enemies[i].x, enemies[i].y, this);
        }
        // Draw Sensors
        long now = new Date().getTime();
        for (int sensorIndex = 0; sensorIndex < 4; sensorIndex++) {
            long activationTime = sensorActivationTimes[sensorIndex];
            if (activationTime != 0 && now - activationTime > 300) {
                sensorActivationTimes[sensorIndex] = 0;
                sensorImage[sensorIndex] = sensorNoSignalImage[sensorIndex];
            }
            offScreenGraphics.drawImage(sensorImage[sensorIndex], 0, 0, this);
        }
        // Check for crash
        if (!frozen) {
            for (Point enemy : enemies) {
                if (martin.y == enemy.y && Math.abs(enemy.x + 80 - martin.x) < 5) {
                    log("View: Crash");
                    offScreenGraphics.setColor(Color.red);
                    offScreenGraphics.drawString("Crunch!", 200, 100);
                    frozen = true;
                    frozenCheckbox.setState(true);
                    crashSound.play();
                }
            }
        }
        g.drawImage(offScreenImage, 0, 0, null);
    }

    void warningIndicatorOn() {
        warningIndicatorImage = warningIndicatorOnImage;
        repaint();
    }

    void warningIndicatorOff() {
        warningIndicatorImage = warningIndicatorOffImage;
        repaint();
    }

    void gateUp() {
        gateImage = gateUpImage;
        repaint();
    }

    void gateDown() {
        gateImage = gateDownImage;
        repaint();
    }

    void flashSensor(int sensorNum) {
        int sensorIndex = sensorNum - 1;
        sensorImage[sensorIndex] = sensorSignalImage[sensorIndex];
        long now = new Date().getTime();
        sensorActivationTimes[sensorIndex] = now;
        // This will automatically be reset to no-signal after 500ms as the sensors work.
        repaint();
    }

    /**
     * Animate moving the enemy to the next way-point. The enemy waits until it has reached the gate
     * and then checks on the gate's status to pass.
     *
     * @param enemyIndex
     * @return
     * @throws InterruptedException
     */
    void moveEnemy(int enemyIndex, EnemyWayPointEnum requiredWayPoint) throws InterruptedException {
        log("View:Moving Enemy" + enemyIndex + " to waypoint " + requiredWayPoint);
        Point enemy = enemies[enemyIndex];
        EnemyWayPointEnum currentWayPoint;
        do {
            synchronized (this) {
                while (frozen) {
                    log("View: Frozen Enemy" + enemyIndex);
                    wait();
                }
                // We let Enemy bump/merge into the earlier enemy in view at the Gate.
                             
            	   enemy.x += Math.pow(2, enemyIndex + 1);	                   
            	    // Set the enemy back to the beginning.
                    if (enemy.x > ROAD_END_WAYPOINT.x) {
                        enemy.x = ENEMY_START_X - ENEMY_INIT_X_SPACING;
                    }
                
                repaint();
            }
            //For viewable car speed
            Thread.sleep(cycleTime);
            //Check the Enemies current position
            if (enemy.equals(GATE_WAYPOINT)) {
                currentWayPoint = EnemyWayPointEnum.GATE;
            } else if (enemy.equals(ROAD_END_WAYPOINT)) {
                currentWayPoint = EnemyWayPointEnum.ROAD_END;
            } else {
                currentWayPoint = null;
            }
        } while (!requiredWayPoint.equals(currentWayPoint));
    }

    /**
     * Returns true when Martin has reached the required position
     *
     * @param requiredWayPoint
     * @return
     * @throws InterruptedException
     */
    void moveMartin(MartinWayPointEnum requiredWayPoint) throws InterruptedException {
        log("View: Moving Martin to waypoint " + requiredWayPoint);
        MartinWayPointEnum currentWayPoint;
        do {
            synchronized (this) {
                while (frozen) {
                    log("View: Frozen Martin");
                    wait();
                }
                if (martin.y < ROAD_Y) {
                    // moving towards road
                    martin.y += 2;
                } else {
                    // moving along road             
                    martin.x += 2;
                    // When Martin moves past the end of the road he appears in the house.
                    if (martin.x > ROAD_END_WAYPOINT.x) {
                        // Return martin to house
                        martin.x = HOUSE_WAYPOINT.x;
                        martin.y = HOUSE_WAYPOINT.y;
                    }
                }
                repaint();
            }
            //For viewable car speed
            Thread.sleep(cycleTime);
            //Check Martin's current position
            if (martin.equals(HOUSE_WAYPOINT)) {
                currentWayPoint = MartinWayPointEnum.HOUSE;
            } else if (martin.equals(COURTYARD_WAYPOINT)) {
                currentWayPoint = MartinWayPointEnum.COURTYARD;
            } else if (martin.equals(ROAD_END_WAYPOINT)) {
                currentWayPoint = MartinWayPointEnum.ROAD_END;
            } else {
                // Martin is between waypoints
                currentWayPoint = null;
            }
        } while (!requiredWayPoint.equals(currentWayPoint));
    }

    synchronized void freeze() {
        frozen = true;
    }

    synchronized void thaw() {
        frozen = false;
        notifyAll();
    }

    void log(String message) {
        double time = (Math.round(new Date().getTime() - startTime + 0.0) / 100 + 0.0) / 10;
        System.out.println(time + ": " + message);
    }
}
