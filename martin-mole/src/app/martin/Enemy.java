package app.martin;

import java.util.ArrayList;
import java.util.List;

class Enemy implements Runnable {
	// Used for logging
	enum State {
		NOT_ON_ROAD, ROAD
	}

	private final int id;
	private State state;
	private final Gate gate;
	private final List<ActionAlias> enterAliases = new ArrayList<>();
	private final List<ActionAlias> exitAliases = new ArrayList<>();
	private final MartinMoleCanvas view;

	Enemy(int id, Gate gate, MartinMoleCanvas view) {
		super();
		this.id = id;
		this.gate = gate;
		this.view = view;
	}

	void enter() throws InterruptedException {
		while (!gate.pass()) {
			view.log("Enemy" + id + ": waits");
			synchronized (gate) {
				gate.wait(); // gate.raise will notify all waiting enemy threads
			}
			view.log("Enemy" + id + ": woken");
		}
		view.log("Enemy" + id + ": action=enter");
		setState(State.ROAD);
		for (ActionAlias enterAlias : enterAliases) {
			enterAlias.execute();
		}
	}

	synchronized void exit() {
		view.log("Enemy" + id + ": action=exit");
		setState(State.NOT_ON_ROAD);
		for (ActionAlias exitAlias : exitAliases) {
			exitAlias.execute();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				// Go to the GATE
				view.moveEnemy(id, MartinMoleCanvas.EnemyWayPointEnum.GATE);
				// Enter will happen if gate.pass() in enabled else Enemy will
				// wait till the Gate notifies.
				enter();
				view.moveEnemy(id, MartinMoleCanvas.EnemyWayPointEnum.ROAD_END);
				exit();

			}
		} catch (InterruptedException e) {
			view.log("Enemy " + id + " thread interrupted, finishing.");
		}
	}

	void addEnterAlias(ActionAlias actionAlias) {
		this.enterAliases.add(actionAlias);

	}

	void addExitAlias(ActionAlias actionAlias) {
		this.exitAliases.add(actionAlias);

	}

	private void setState(State state) {
		this.state = state;
		view.log("Enemy" + id + ": transitions to state=" + state);
	}

}
