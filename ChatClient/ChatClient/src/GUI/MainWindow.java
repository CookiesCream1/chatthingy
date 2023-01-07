package GUI;

import javax.swing.JFrame;

public class MainWindow extends JFrame {

    private static MainWindow instance;
    public static MainWindow getMainWindow(){
        if (instance != null) instance = new MainWindow();
        return instance;
    }
    private MainWindow()
    {

    }
}
