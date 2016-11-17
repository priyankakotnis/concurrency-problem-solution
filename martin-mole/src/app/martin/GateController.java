package app.martin;

/**
 * This controller class uses sensor actions to sense that Martin wants to enter the road,
 * and hence lowers the Gate to hold the incoming Enemies so Martin can use the road safely
 * with the help on Warning Indicator.
 */
class GateController {
	

    enum State {
        LOWERED, ENTERED, EXITED
    }

    private final Gate gate;
    private State state = State.EXITED;
    private MartinMoleCanvas view;

    GateController(Gate gate, MartinMoleCanvas view) {
        this.gate = gate;
        this.view = view;
    }

    void leave() {
        view.log("GateController: action=leave");           
        lower();
    }

    private void lower() {  
        setState(state.LOWERED);
        gate.lower();
    }

    /**
     * Martin enter 
     */
    void enter() {
        view.log("GateController: action=enter");        
        setState(state.ENTERED);
    }

    synchronized void exit() {
        view.log("GateController: action=exit");
        /*Gate controller knows if Martin was on the road and implies Martin triggered the exit sensor by exiting.*/
        if (State.ENTERED.equals(state)) {
            view.log("GateController: Martin exit --> GATE Controller exit --> Raise the gate now");
            raise();
            state = State.EXITED;
        }
    }

    //sync removed
    private void raise() {
        gate.raise();
    }

    private void setState(State state) {
        this.state = state;
        view.log("GateController: transition to state=" + state);
    }
}
