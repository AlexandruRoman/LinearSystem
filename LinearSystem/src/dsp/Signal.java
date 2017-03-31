
package dsp;

import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class Signal {
    public Vector<Double> x;
    public Signal() {
        x = new Vector<>();
    }

    public Signal(double[] vals) {
        this();
        for(int i=0; i<vals.length; i++)
            x.add(vals[i]);
    }
    public Signal(String fileName)
    {
        this();
        try { //try-catch block for file reader
            File file = new File(fileName);
            Scanner scnr = new Scanner(file);
            while (scnr.hasNextDouble()) {
                double nr = scnr.nextDouble();
                x.add(nr);
            }
            scnr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    void copy(Signal signal)
    {
        //copy the signal element by element
        x.removeAllElements();
        for(double a : signal.x)
        {
            x.add(a);
        }
    }
    public Signal add(Signal signal)
    {
        //in result se salveaza suma elementelor din semnale pana la lungimea celui mai scurt
        //apoi se adauga la result elementele ramase din semnalul mai lung
        Signal result = new Signal();
        if(x.size() < signal.x.size())
        {
            for(int i=0; i<x.size(); i++)
                result.x.add(x.get(i) + signal.x.get(i));
            for(int i=x.size(); i<signal.x.size(); i++)
                result.x.add(signal.x.get(i));
        }
        else
        {
            for(int i=0; i<signal.x.size(); i++)
                result.x.add(x.get(i) + signal.x.get(i));
            for(int i=signal.x.size(); i<x.size(); i++)
                result.x.add(x.get(i));
        }
        return result;
    }
    public Signal scale(double factor)
    {
        Signal result = new Signal();
        for(double a : x)
            result.x.add(a*factor);
        return result;
    }
    public Signal delay(int delay)
    {
        Signal result = new Signal();
        for (int i=0; i<delay; i++)
            result.x.add(0.0);
        for(int i=0;i<x.size()-delay;i++)
            result.x.add(x.get(i));
        return result;
    }
    public Signal convolve(Signal signal)
    {
        //fac discutie dupa lungimile celor 2 semnale
        //aux este o copie a semnalului mai scurt la care se adauga 0-uri pana se egaleaza lungimea semnalului mai lung
        Signal result = new Signal();
        if(x.size() < signal.x.size())
        {
            Vector<Double> aux = new Vector<>(x);
            for(int i=0;i<signal.x.size()-x.size(); i++)
                aux.add(0.0);
            int len = signal.x.size();
            for(int i=0;i<len; i++) {
                double a = 0;
                for(int j=0;j<=i; j++)
                {
                    a += aux.get(j) * signal.x.get(i-j);
                }
                result.x.add(a);
            }
        }
        else
        {
            Vector<Double> aux = new Vector<>(signal.x);
            for(int i=0;i<x.size()-signal.x.size(); i++)
                aux.add(0.0);
            int len = x.size();
            for(int i=0;i<len; i++) {
                double a = 0;
                for(int j=0;j<=i; j++)
                {
                    a += aux.get(i-j) * x.get(j);
                }
                result.x.add(a);
            }
        }
        return result;
    }

    public String toString()
    {
        String s = "";
        for(int i=0; i<x.size(); i++)
            s += x.get(i) + "    ";
        return s;
    }

    void save(String fileName)
    {
        try {
            File statText = new File(fileName);
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(toString());
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
