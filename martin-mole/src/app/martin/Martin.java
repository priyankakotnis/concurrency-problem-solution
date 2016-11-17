package app.martin;

class Martin implements Runnable {
	//Used for logging
    public enum State {
        HOUSE, COURTYARD, ROAD
    }

    private State state;
    private final WarningIndicator indicator;
    private final MartinMoleCanvas view;

    /**
     * Constructor with indicator - Martin is using the indicator.
     */
    Martin(WarningIndicator indicator, MartinMoleCanvas view) {
        super();
        this.indicator = indicator;
        this.view = view;
    }

    /*
	 * Aliasing - to match the FSP process renaming Martin process does not need
	 * to know about the sensor process.
     */
    private ActionAlias leaveAlias;
    private ActionAlias enterAlias;
    private ActionAlias exitAlias;

    void leave() {
        view.log("Martin: action=leave");
        leaveAlias.execute();  // Triggers sensor1.exit
        setState(State.COURTYARD);
    }

    void enter() throws InterruptedException {
    	 while (!indicator.off()) {
             view.log("Martin: waits");
             synchronized (indicator) {
                 indicator.wait(); // wait on indicator to notify
             }
             view.log("Martin: woken");
         }
    	view.log("Martin: action=enter");
        setState(State.ROAD);
        enterAlias.execute(); // Triggers sensor2.enter  
    }

    synchronized void exit() {
        view.log("Martin: action=exit");
        exitAlias.execute();  // Triggers sensor4.exit
        setState(State.HOUSE);
    }

    State getState() {
        return state;
    }

    @Override
    public void run() {

        try {          
            while (true) {
                leave();
                view.moveMartin(MartinMoleCanvas.MartinWayPointEnum.COURTYARD);               
                enter();
                view.moveMartin(MartinMoleCanvas.MartinWayPointEnum.ROAD_END);
                exit();
                view.moveMartin(MartinMoleCanvas.MartinWayPointEnum.HOUSE);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            view.log("Martin thread interrupted, finishing.");
        }

    }

    void setLeaveAlias(ActionAlias actionAlias) {
        this.leaveAlias = actionAlias;

    }

    void setEnterAlias(ActionAlias actionAlias) {
        this.enterAlias = actionAlias;

    }

    void setExitAlias(ActionAlias actionAlias) {
        this.exitAlias = actionAlias;

    }

    private void setState(State state) {
        this.state = state;
        view.log("Martin: transition to state=" + state);
    }
}
