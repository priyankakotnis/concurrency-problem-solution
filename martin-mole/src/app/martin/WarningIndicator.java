package app.martin;

class WarningIndicator {

    private int enemyCount = 0;
    private final MartinMoleCanvas view;

    WarningIndicator(MartinMoleCanvas view) {
        this.view = view;
    }

    synchronized boolean off() {
        return enemyCount == 0;
    }

    synchronized void up() {
        view.log("WarningIndicator: action=up");
        ++enemyCount;
        if (enemyCount == 1) {
            view.warningIndicatorOn();
        }
        if (enemyCount > 0) {
            view.log("WarningIndicator: Indicator is ON");
        }
        view.log("WarningIndicator: Enemy count UP to " + enemyCount);
    }

    synchronized void down() {
        view.log("WarningIndicator: action=down");
         --enemyCount;
            view.log("WarningIndicator: Enemy count DOWN to " + enemyCount);
        if (off()) {
            view.log("WarningIndicator: Indicator is OFF");            
            view.warningIndicatorOff();
            notify();  // Martin needs to know about this.
            view.log("WarningIndicator: NotifyAll called.");
        }
    }
}
