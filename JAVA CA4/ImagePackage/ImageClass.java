package ImagePackage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import MainPackage.MainClass;

import java.awt.*;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
public class ImageClass {
    static JFrame imageFrame;
    static JPanel imagePanel;
    static Image loadedImage = null;
    static JLabel toolsBoxLabel, filtersBoxLabel, timeLabel, dateLabel; 
    static JComboBox<String> toolsBox, filtersBox;
    static JButton imageCropBtn, filtersApplyBtn, saveBtn, backBtn, exitBtn, undoBtn, redoBtn, openBtn;
    static ActionListenerClass listener;
    static Stack<Image> undoStack, redoStack;
    static boolean editFlag = false;
    static ToolsClass drawTool;
    static LocalDateTime startTime;

    public ImageClass() {
        SwingUtilities.invokeLater(() -> {
            try {
                undoStack = new Stack<>();
                redoStack = new Stack<>();
                imageFrame = new JFrame("Image Editor");
                imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                imageFrame.setLayout(new BorderLayout());
                imageFrame.setSize(1080, 600);

                imagePanel = new JPanel(new BorderLayout());
                imageFrame.add(imagePanel, BorderLayout.CENTER);

                JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                imageFrame.add(toolbarPanel, BorderLayout.NORTH);

                toolsBoxLabel = new JLabel("Tool: ");
                toolbarPanel.add(toolsBoxLabel);
                String[] tools = {"None", "Rectangle", "Circle"};
                toolsBox = new JComboBox<>(tools);
                toolsBox.setSelectedIndex(0);
                toolbarPanel.add(toolsBox);
                listener = new ActionListenerClass();
                toolsBox.addActionListener(listener);

                imageCropBtn = new JButton("Crop Image");
                toolbarPanel.add(imageCropBtn);
                imageCropBtn.addActionListener(listener);

                filtersBoxLabel = new JLabel("Filter: ");
                toolbarPanel.add(filtersBoxLabel);
                String[] filters = {"None", "Light", "Dark", "Blur", "Invert"};
                filtersBox = new JComboBox<>(filters);
                filtersBox.setSelectedIndex(0);
                toolbarPanel.add(filtersBox);

                filtersApplyBtn = new JButton("Apply Filter");
                toolbarPanel.add(filtersApplyBtn);
                filtersApplyBtn.addActionListener(listener);

                saveBtn = new JButton("Save");
                toolbarPanel.add(saveBtn);
                saveBtn.addActionListener(listener);

                undoBtn = new JButton("Undo");
                toolbarPanel.add(undoBtn);
                undoBtn.addActionListener(listener);

                redoBtn = new JButton("Redo");
                toolbarPanel.add(redoBtn);
                redoBtn.addActionListener(listener);

                backBtn = new JButton("Back");
                toolbarPanel.add(backBtn);
                backBtn.addActionListener(listener);

                exitBtn = new JButton("Exit");
                toolbarPanel.add(exitBtn);
                exitBtn.addActionListener(listener);

                openBtn = new JButton("Choose Image");
                toolbarPanel.add(openBtn);
                openBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String filePath = chooseImageFile();
                        if (filePath != null) {
                            try {
                                loadedImage = ImageIO.read(new File(filePath));
                                int desiredWidth = 800; 
                                int scaledHeight = (int) (((double) desiredWidth / loadedImage.getWidth(null)) * loadedImage.getHeight(null));
                                loadedImage = loadedImage.getScaledInstance(desiredWidth, scaledHeight, Image.SCALE_DEFAULT);
                                drawTool = new ToolsClass(loadedImage);
                                imagePanel.removeAll(); 
                                imagePanel.add(drawTool, BorderLayout.CENTER);
                                imageFrame.pack(); 
                                imageFrame.repaint(); 
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });

                
                JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                dateTimePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 
                toolbarPanel.add(dateTimePanel);

                timeLabel = new JLabel("00:00:00");
                dateTimePanel.add(timeLabel);

                dateLabel = new JLabel(""); 
                dateTimePanel.add(dateLabel);

                imageFrame.setVisible(true);

                startTime = LocalDateTime.now();
                Timer timer = new Timer(1000, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateElapsedTime();
                    }
                });
                timer.start();
            } catch (HeadlessException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void updateElapsedTime() {
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        String timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeLabel.setText(timeStr);

        
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = dateFormatter.format(currentDate);
        dateLabel.setText(formattedDate);
    }

    public static String chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp", "jpeg", "tiff");
        fileChooser.setFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    static class ActionListenerClass implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() == toolsBox) {
                try {
                    if (toolsBox.getSelectedIndex() != 0)
                        drawTool.setToolType(toolsBox.getSelectedIndex());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (actionEvent.getSource() == imageCropBtn) {
                try {
                    if (toolsBox.getSelectedIndex() != 0) {
                        undoStack.push(drawTool.getImage());
                        drawTool.setImage(drawTool.cropImage(toBufferedImage(drawTool.getImage())));
                        toolsBox.setSelectedIndex(0);
                        editFlag = true;
                    } else
                        JOptionPane.showMessageDialog(imageFrame, "Please Choose a tool to cut with");
                } catch (HeadlessException | RasterFormatException ex) {
                    ex.printStackTrace();
                }
            } else if (actionEvent.getSource() == filtersApplyBtn) {
                FiltersClass filterTool = new FiltersClass();
                try {

                    switch (filtersBox.getSelectedIndex()) {
                        case 1:
                            undoStack.push(drawTool.getImage());
                            loadedImage = filterTool.lightenImage(toBufferedImage(drawTool.getImage()));
                            drawTool.setImage(loadedImage);
                            redoStack.clear();
                            filtersBox.setSelectedIndex(0);
                            editFlag = true;
                            break;
                        case 2:
                            undoStack.push(drawTool.getImage());
                            loadedImage = filterTool.darkenImage(toBufferedImage(drawTool.getImage()));
                            drawTool.setImage(loadedImage);
                            redoStack.clear();
                            filtersBox.setSelectedIndex(0);
                            editFlag = true;
                            break;
                        case 3:
                            undoStack.push(drawTool.getImage());
                            loadedImage = filterTool.blurImage(toBufferedImage(drawTool.getImage()));
                            drawTool.setImage(loadedImage);
                            redoStack.clear();
                            filtersBox.setSelectedIndex(0);
                            editFlag = true;
                            break;
                        case 4:
                            undoStack.push(drawTool.getImage());
                            loadedImage = filterTool.invertImage(toBufferedImage(drawTool.getImage()));
                            drawTool.setImage(loadedImage);
                            redoStack.clear();
                            filtersBox.setSelectedIndex(0);
                            editFlag = true;
                            break;
                        default:
                            JOptionPane.showMessageDialog(imageFrame, "Please Choose a filter to apply");
                            break;
                    }

                } catch (HeadlessException ex) {
                    ex.printStackTrace();
                }
            } else if (actionEvent.getSource() == saveBtn) {
                try {
                    if (editFlag) {
                        String filePath = null;
                        JFileChooser fileChooser = new JFileChooser(filePath);
                        imageFrame.setVisible(false);
                        int choosenBtn = fileChooser.showSaveDialog(imageFrame);
                        if (choosenBtn == JFileChooser.APPROVE_OPTION) {
                            File tempFile = new File(fileChooser.getSelectedFile().toString() + ".png");
                            ImageIO.write(toBufferedImage(drawTool.getImage()), "png", tempFile);
                            imageFrame.setVisible(true);

                        } else {
                            imageFrame.setVisible(true);
                        }
                    } else JOptionPane.showMessageDialog(imageFrame, "You can NOT do this right now!");
                } catch (HeadlessException | IOException ex) {
                    ex.printStackTrace();
                }
            } else if (actionEvent.getSource() == undoBtn) {
                try {
                    if (!undoStack.empty() && editFlag == true) {
                        redoStack.push(drawTool.getImage());
                        loadedImage = toBufferedImage((Image) undoStack.pop());
                        drawTool.setImage(loadedImage);
                        drawTool.repaint();
                    } else
                        JOptionPane.showMessageDialog(imageFrame, "You Can NOT do this right now!");
                } catch (HeadlessException ex) {
                    ex.printStackTrace();
                }
            } else if (actionEvent.getSource() == redoBtn) {
                try {
                    if (!redoStack.empty() && editFlag == true) {
                        undoStack.push(drawTool.getImage());
                        loadedImage = toBufferedImage((Image) redoStack.pop());
                        drawTool.setImage(loadedImage);
                        drawTool.repaint();
                    } else
                        JOptionPane.showMessageDialog(imageFrame, "You Can NOT do this right now!");
                } catch (HeadlessException ex) {
                    ex.printStackTrace();
                }
            } else if (actionEvent.getSource() == backBtn) {
                try {
                    if (JOptionPane.showConfirmDialog(imageFrame, "Are you sure?", "Query", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        imageFrame.setVisible(false);
                        MainClass.main(null);
                    }
                } catch (HeadlessException ex) {
                    ex.printStackTrace();
                }
            } else if (actionEvent.getSource() == exitBtn) {
                try {
                    if (JOptionPane.showConfirmDialog(imageFrame, "Are you sure?", "Query", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                } catch (HeadlessException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageClass();
        });
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }
}
