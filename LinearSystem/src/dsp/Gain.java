
package dsp;

public class Gain extends Element {
    Signal input;
    double factor;

    public Gain(Signal output, Signal input, double factor) {
        super.output = output;
        this.input = input;
        this.factor = factor;
    }

    @Override
    public void compute() {
        output.copy(input.scale(factor));
    }
}
