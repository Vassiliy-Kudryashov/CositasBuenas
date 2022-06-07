package cositas.buenas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UIProperties {

    public static void main(String[] args) {
        List<Object> uiKeys = new ArrayList<>(UIManager.getLookAndFeelDefaults().keySet());
        uiKeys.sort(Comparator.comparing(String::valueOf));
        uiKeys.stream().map(key -> key + "->" + UIManager.get(key)).forEach(System.out::println);
    }
}