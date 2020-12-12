import java.io.*;

public class Msg implements Serializable {
    public int round;
    public int value;

    Msg( int round, int value){
        this.round = round;
        this.value = value;
    }

    public void print(String spacer){
        System.out.printf("%sround: %d\n", spacer, round);
        System.out.printf("%svalue: %d\n", spacer, value);
    }
}
