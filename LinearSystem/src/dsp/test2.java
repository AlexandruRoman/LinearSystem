package dsp;

public class test2 {

    public static void main(String args[])
    {
        LinearSystem ls = new LinearSystem("in.txt");
        double[] samples = {2, 3, 4, 3, 2, 0, -2, -3, -4, -3, -2, 0, 0, 0, 0};
        Signal a = new Signal(samples);
        Signal d = new Signal();
        ls.setInput(a);
        System.out.println("in\t" + a);
        ls.setOutput(d);
        ls.compute();
        System.out.println("out\t" + d);
    }
}
