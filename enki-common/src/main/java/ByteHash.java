package Enki;
import java.util.Random;

// not to be used directly, see CyclicHash and RabinKarpHash instead
// not to be used directly, see CyclicHash and RabinKarpHash instead
import java.util.Random;

public class ByteHash {

    public static final int[] array;

    static {
        Random r = new Random(0);
        array = new int[1<<8];
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.abs(r.nextInt());
        }
    }

}
