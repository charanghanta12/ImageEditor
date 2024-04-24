package MainPackage;

import ImagePackage.ImageClass;
import javax.swing.*;
import java.awt.event.*;

public class MainClass {

    static JFrame mainFrame;
    static JButton exitBtn, openEditorBtn;
    static ActionListenerClass listener;

    public static void main(String[] args) {
        createMainFrame();
    }

    public static void createMainFrame() {
        mainFrame = new JFrame("Image Editor");
        mainFrame.setVisible(true);
        mainFrame.setResizable(false);
        mainFrame.setSize(400, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(null);

        listener = new ActionListenerClass();

        openEditorBtn = new JButton("Open Editor");
        openEditorBtn.setBounds(50, 20, 130, 30);
        mainFrame.add(openEditorBtn);
        openEditorBtn.addActionListener(listener);

        exitBtn = new JButton("Exit");
        exitBtn.setBounds(200, 20, 90, 30);
        mainFrame.add(exitBtn);
        exitBtn.addActionListener(listener);
    }

    static class ActionListenerClass implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() == exitBtn) {
                if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure?", "Query", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            } else if (actionEvent.getSource() == openEditorBtn) {
                
                mainFrame.dispose(); 
                new ImageClass();
            }
        }
    }
}
