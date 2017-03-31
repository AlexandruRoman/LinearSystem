
package dsp;

public class Adder extends Element{
    Signal[] inputs;

    public Adder(Signal output, Signal[] inputs) {
        super.output = output;
        this.inputs = inputs;
    }
    public Adder(Signal output, Signal input1, Signal input2)
    {
        inputs = new Signal[2];
        super.output = output;
        inputs[0] = input1;
        inputs[1] = input2;
    }

    @Override
    public void compute() {
        output.copy(inputs[0].add(inputs[1]));
        for(int i=2; i<inputs.length; i++)
            output.copy(output.add(inputs[i]));
    }
}
