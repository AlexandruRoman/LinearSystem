
package dsp;

public class Delay extends Element {
    Signal input;
    int delay;

    public Delay(Signal output, Signal input, int delay) {
        super.output = output;
        this.input = input;
        this.delay = delay;
    }

    @Override
    public void compute() {
        output.copy(input.delay(delay));
    }
}
