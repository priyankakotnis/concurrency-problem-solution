package app.martin;


class Gate {

    private MartinMoleCanvas view;
    private boolean isSafe;
    private boolean isUp = true;

    /**
     * Used for logging
     *
     */
    enum State {
        DOWN, UP
    }

    Gate(MartinMoleCanvas view, boolean isSafe) {
        this.view = view;
        this.isSafe = isSafe;
    }
    private State state = State.UP;

    protected State getState() {
        return state;
    }

    synchronized void lower() {
        if (isSafe) {
            view.log("Gate: action=gate.lower");
            setState(State.DOWN);
            setIsUp(false);
            view.gateDown();
        }
    }

    synchronized boolean pass() {
        view.log("Gate: Enemy checking gate: state=" + state);
        return getIsUp();

    }

    synchronized void raise() {
        setState(State.UP);
        setIsUp(true);
        view.gateUp();
        notifyAll(); // enemies want to know about this.
        view.log("Gate: notifyAll");
    }
    
    

    private boolean getIsUp() {
		return isUp;
	}

	private void setIsUp(boolean isUp) {
		this.isUp = isUp;
	}

	private void setState(State state) {
        this.state = state;
        view.log("Gate: transition to state=" + state);
    }
}
