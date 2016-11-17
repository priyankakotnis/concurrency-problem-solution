package app.martin;

class Sensor {

    private final int number;  // 1 to 4
    protected MartinMoleCanvas view;

    Sensor(int number, MartinMoleCanvas view) {
        this.number = number;
        this.view = view;;
    }

    int getNumber() {
        return number;
    }

    /**
     * @return the view
     */
    protected MartinMoleCanvas getView() {
        return view;
    }
}
