import java.io.*;

public class Msg implements Serializable{
    public String content;
    public VectorClock Vm;
    public int source_pid;

    Msg(String _content, VectorClock _Vm, int _source_pid){
        content = _content;
        Vm = _Vm;
        source_pid = _source_pid;
    }

    public void print(String spacer){
        System.out.printf("%sContent: %s\n",  spacer,content);
        System.out.printf("%sSource pid: %d\n", spacer, source_pid);
        System.out.printf("%sVector clock:", spacer);
        Vm.print();
    }
}

