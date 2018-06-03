import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        List<Integer> list = new ArrayList<>();

        list.addAll(Arrays.asList(1,23,4,5,6,-1));

        Collections.sort (list);
        System.out.println(list);

        List<Integer> list2 = new ArrayList<>();
        list2.addAll(Arrays.asList(5,6));
        list.removeAll(list2);
        System.out.println(list);
        StringBuffer sb = new StringBuffer();
        StringBuilder sb2 = new StringBuilder();
    }
}
