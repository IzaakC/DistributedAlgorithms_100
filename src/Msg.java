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

    public void print(){
        System.out.printf("Content: %s\n", content);
        System.out.printf("Source pid: %d\n", source_pid);
        System.out.printf("Vector clock:");
        Vm.print();
    }
}

