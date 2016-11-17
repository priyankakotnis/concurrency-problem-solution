package app.martin;

class EnteringSensor extends Sensor {

    private ActionAlias enterAlias;

    void setEnterAlias(ActionAlias actionAlias) {
        this.enterAlias = actionAlias;
    }

    EnteringSensor(int number, MartinMoleCanvas view) {
        super(number, view);
    }

    /**
     * Martin and Enemy have separate instances of this class. As we have used lambda functions in the Applet class,
     * Depending on if it is Martin's entering sensor or enemies, correct actions will be triggered when we execute the alias.
     */
    synchronized void enter() {
        view.log("Sensor" + getNumber() + ": action=enter");
        getView().flashSensor(getNumber());
        if (enterAlias != null) {
            enterAlias.execute();
        }
    }
}
