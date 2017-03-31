
package dsp;

public class Filter extends Element{
    Signal input;
    double[] filter;

    public Filter(Signal output, Signal input, double[] filter) {
        super.output = output;
        this.input = input;
        this.filter = filter;
    }

    @Override
    public void compute() {
        output.copy(input.convolve(new Signal(filter)));
    }
}
