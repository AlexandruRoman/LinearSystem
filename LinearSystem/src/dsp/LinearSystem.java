package dsp;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LinearSystem {

    //HashMap si ArrayList sunt ca niste vectori obisnuiti numai ca accesarea se face nu cu "[i]" si cu ".get(i)"
    //diferenta e la HashMap unde accesarea nu e pe baza de nr si pe baza de cuvant de ex: nu v.get(i) si v.get("ceva")
    HashMap<String, Signal> signals;
    ArrayList<Element> elements;
    ArrayList<ArrayList<Signal>> arguments; //aici salvez argumentele tuturor elementelor ca sa ma folosesc de ele pe parcurs
    Signal output, tempOutput;
    String inToken, outToken;
    boolean eFisier = false;

    public LinearSystem(){
        signals = new HashMap<>();
        elements = new ArrayList<>();
        arguments = new ArrayList<>();
    }
    public LinearSystem(String fileName)
    {
        this();
        eFisier = true;
        try { //try-catch block for file reader
            File file = new File(fileName);
            Scanner scnr = new Scanner(file);
            while(scnr.hasNextLine())
            {
                //aici interpretez linie cu linie fisierul de input
                //tokens[i] inseamna cuvantul/litera i de pe randul respectiv
                String line = scnr.nextLine();
                String[] tokens = line.trim().replaceAll(" +", " ").split(" ");
                if(tokens[0].contains("INPUT"))
                {
                    inToken = tokens[1];
                    if(!signals.containsKey(inToken)) //mereu verific daca am mai intalnit inainte semnalul cu litera asta
                    {
                        signals.put(inToken, new Signal());// adaug in HashMap-ul de semnale
                    }
                }
                if(tokens[0].contains("FILTER"))
                {
                    Signal out, in;
                    ArrayList<Signal> args = new ArrayList<>();
                    double[] samples = new double[tokens.length - 3];

                    //verific daca trebuie adaugat un semnal cu eticheta tokens[1] ( daca nu e deja adaugat)
                    if(!signals.containsKey(tokens[1]))
                    {
                        out = new Signal();
                        signals.put(tokens[1], out);
                        tempOutput = out;
                    }
                    else
                        out = signals.get(tokens[1]);

                    //verific daca trebuie adaugat un semnal cu eticheta tokens[2] ( daca nu e deja adaugat)
                    if(!signals.containsKey(tokens[2]))
                    {
                        in = new Signal();
                        signals.put(tokens[2], in);
                    }
                    else
                        in = signals.get(tokens[2]);

                    for(int i=3; i<tokens.length; i++)
                        samples[i-3] = Double.parseDouble(tokens[i]);

                    elements.add(new Filter(out, in, samples));
                    args.add(in);
                    arguments.add(args);
                }
                if(tokens[0].contains("GAIN"))
                {
                    Signal out, in;
                    ArrayList<Signal> args = new ArrayList<>();
                    if(!signals.containsKey(tokens[1]))
                    {
                        out = new Signal();
                        signals.put(tokens[1], out);
                        tempOutput = out;
                    }
                    else
                        out = signals.get(tokens[1]);

                    if(!signals.containsKey(tokens[2]))
                    {
                        in = new Signal();
                        signals.put(tokens[2], in);
                    }
                    else
                        in = signals.get(tokens[2]);

                    elements.add(new Gain(out, in, Double.parseDouble(tokens[3])));
                    args.add(in);
                    arguments.add(args);
                }
                if(tokens[0].contains("DELAY"))
                {
                    Signal out, in;
                    ArrayList<Signal> args = new ArrayList<>();
                    if(!signals.containsKey(tokens[1]))
                    {
                        out = new Signal();
                        signals.put(tokens[1], out);
                        tempOutput = out;
                    }
                    else
                        out = signals.get(tokens[1]);

                    if(!signals.containsKey(tokens[2]))
                    {
                        in = new Signal();
                        signals.put(tokens[2], in);
                    }
                    else
                        in = signals.get(tokens[2]);

                    elements.add(new Delay(out, in, Integer.parseInt(tokens[3])));
                    args.add(in);
                    arguments.add(args);
                }
                if(tokens[0].contains("ADDER"))
                {
                    Signal out;
                    ArrayList<Signal> args = new ArrayList<>();
                    Signal[] in = new Signal[tokens.length - 2];
                    if(!signals.containsKey(tokens[1]))
                    {
                        out = new Signal();
                        signals.put(tokens[1], out);
                        tempOutput = out;
                    }
                    else
                        out = signals.get(tokens[1]);

                    for(int i=2; i<tokens.length; i++)
                    {
                        if(!signals.containsKey(tokens[i]))
                        {
                            in[i-2] = new Signal();
                            signals.put(tokens[i], in[i-2]);
                        }
                        else
                            in[i-2] = signals.get(tokens[i]);
                        args.add(in[i-2]);
                    }
                    elements.add(new Adder(out, in));
                    arguments.add(args);
                }
                if(tokens[0].contains("OUTPUT"))
                {
                    outToken = tokens[1];
                    if(!signals.containsKey(outToken)) {
                        tempOutput = new Signal();
                        signals.put(outToken, tempOutput);
                    }
                }
            }
            scnr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(Element element)
    {
        ArrayList<Signal> args = new ArrayList<>();
        if(element instanceof Adder) // e operatie de tip Adder
        {
            Adder x = (Adder)element;
            for(int i=0; i<x.inputs.length; i++)
                args.add(x.inputs[i]);
        }
        if(element instanceof Filter)
            args.add(((Filter)element).input);
        if(element instanceof Gain)
            args.add(((Gain)element).input);
        if(element instanceof Delay)
            args.add(((Delay)element).input);

        arguments.add(args);
        elements.add(element);
    }

    public void setInput(Signal signal)
    {
        if(!eFisier) {
            inToken = "in";
            signals.put(inToken, signal);
        }
        else {
            signals.get(inToken).x = signal.x;
        }
    }

    public Signal getInput()
    {
        return signals.get(inToken);
    }

    public void setOutput(Signal signal)
    {
        output = signal;
        if(!eFisier) {
            for(Element e : elements)
            {
                if(e.output == output)
                {
                    tempOutput = new Signal();
                    e.output = tempOutput;
                    outToken = "out";
                    signals.put(outToken, e.output);
                }
            }
        }
    }

    public Signal getOutput()
    {
        return signals.get(outToken);
    }

    boolean check(int index) // verific daca operatia i are toate semnalele de input calculate
    {
        ArrayList<Signal> args = arguments.get(index);
        for(int i=0; i<args.size(); i++)
        {
            if(args.get(i).x.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    //la compute e naspa chestia ca nu poti sa iei toate operatiile "Element" la rand si sa le calculezi pt ca
    // s-ar putea ca o operatie sa aiba nevoie de un semnal care inca nu a fost calculat si care e calculat
    // intr-o operatie care se afla in vectorul de operatii dupa operatia curenta.

    //de aceea, in compute am while-ul ala are practic imi parcurge toate operatiile de cate ori e nevoie ca sa imi calculeze tot
    public void compute()
    {
        ArrayList<ArrayList<Signal>> args = new ArrayList<>();
        ArrayList<Element> elem = new ArrayList<>();

        args.addAll(arguments);
        elem.addAll(elements);

        while(elem.size()!=0)
        {
            for(int i=0; i<elem.size(); i++) // pt fiecare operatie
            {
                if(check(i)) {
                    elem.get(i).compute();
                    elem.remove(i);
                    args.remove(i);
                    i--;
                }
            }
        }

        output.copy(signals.get(outToken));// salvez in output rezultatul final
    }

    //save iti creaza un fisier de genul celui pe care il primesti in constructorul LinearSystem(String)
    public void save(String fileName)
    {
        try {
            File statText = new File(fileName);
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);

            w.write("INPUT in\nOUTPUT out\n");
            HashMap<String, Signal> sigs = new HashMap<>();
            sigs.put("in", signals.get("in"));
            sigs.put("out", tempOutput);
            int index = 0;

            for(int i=0; i<elements.size(); i++) //pt fiecare operatie
            {
                if(elements.get(i) instanceof Adder)
                {
                    w.write("ADDER ");

                    // portiunea de cod care se ocupa de scrierea output-ului semnalului
                    if(sigs.containsValue(elements.get(i).output))
                    {
                        for(String sigName : sigs.keySet())
                        {
                            if(sigs.get(sigName) == elements.get(i).output)
                                w.write(sigName + " ");
                        }
                    }
                    else
                    {
                        String key = "sig_" + index + " ";
                        w.write(key);
                        sigs.put(key, elements.get(i).output);
                        index++;
                    }
                    for(Signal aux : arguments.get(i))
                    {
                        if(sigs.containsValue(aux))
                        {
                            for(String sigName : sigs.keySet())
                            {
                                if(sigs.get(sigName) == aux)
                                    w.write(sigName + " ");
                            }
                        }
                        else
                        {
                            String key = "sig_" + index + " ";
                            w.write(key);
                            sigs.put(key, aux);
                            index++;
                        }
                    }

                }
                if(elements.get(i) instanceof Filter)
                {
                    w.write("FILTER ");
                    if(sigs.containsValue(elements.get(i).output))
                    {
                        for(String sigName : sigs.keySet())
                        {
                            if(sigs.get(sigName) == elements.get(i).output)
                                w.write(sigName + " ");
                        }
                    }
                    else
                    {
                        String key = "sig_" + index + " ";
                        w.write(key);
                        sigs.put(key, elements.get(i).output);
                        index++;
                    }

                    for(Signal aux : arguments.get(i))
                    {
                        if(sigs.containsValue(aux))
                        {
                            for(String sigName : sigs.keySet())
                            {
                                if(sigs.get(sigName) == aux)
                                    w.write(sigName + " ");
                            }
                        }
                        else
                        {
                            String key = "sig_" + index + " ";
                            w.write(key);
                            sigs.put(key, aux);
                            index++;
                        }
                    }

                    for(int j=0; j<((Filter)elements.get(i)).filter.length; j++)
                    {
                        w.write("" + ((Filter)elements.get(i)).filter[j] + " ");
                    }
                }
                if(elements.get(i) instanceof Gain)
                {
                    w.write("GAIN ");
                    if(sigs.containsValue(elements.get(i).output))
                    {
                        for(String sigName : sigs.keySet())
                        {
                            if(sigs.get(sigName) == elements.get(i).output)
                                w.write(sigName + " ");
                        }
                    }
                    else
                    {
                        String key = "sig_" + index + " ";
                        w.write(key);
                        sigs.put(key, elements.get(i).output);
                        index++;
                    }

                    for(Signal aux : arguments.get(i))
                    {
                        if(sigs.containsValue(aux))
                        {
                            for(String sigName : sigs.keySet())
                            {
                                if(sigs.get(sigName) == aux)
                                    w.write(sigName + " ");
                            }
                        }
                        else
                        {
                            String key = "sig_" + index + " ";
                            w.write(key);
                            sigs.put(key, aux);
                            index++;
                        }
                    }
                    w.write("" + ((Gain)elements.get(i)).factor);
                }
                if(elements.get(i) instanceof Delay)
                {
                    w.write("DELAY ");
                    if(sigs.containsValue(elements.get(i).output))
                    {
                        for(String sigName : sigs.keySet())
                        {
                            if(sigs.get(sigName) == elements.get(i).output)
                                w.write(sigName + " ");
                        }
                    }
                    else
                    {
                        String key = "sig_" + index + " ";
                        w.write(key);
                        sigs.put(key, elements.get(i).output);
                        index++;
                    }

                    for(Signal aux : arguments.get(i))
                    {
                        if(sigs.containsValue(aux))
                        {
                            for(String sigName : sigs.keySet())
                            {
                                if(sigs.get(sigName) == aux)
                                    w.write(sigName + " ");
                            }
                        }
                        else
                        {
                            String key = "sig_" + index + " ";
                            w.write(key);
                            sigs.put(key, aux);
                            index++;
                        }
                    }
                    w.write("" + ((Delay)elements.get(i)).delay);
                }
                w.write("\n");
            }

            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
