package ru.avalon.javapp.devj120;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class FileViewer extends JFrame {

    private final JList fileList;
    private final JTextArea fileContent;
    private File[] files;

    public FileViewer() {
        setBounds(300,200,600,400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        fileList = new JList();
        fileList.addListSelectionListener(e -> onFiledListItemSelection());
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    enterToDir();
                }
            }
        });

        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER)
                    enterToDir();
            }
        });

        fileContent = new JTextArea();
        fileContent.setEditable(false);

        JSplitPane sp = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(fileList),
                new JScrollPane(fileContent));
        add(sp, BorderLayout.CENTER);
        sp.setDividerLocation(150);
        // путь к каталогу пользователя откуда запускается программа
        goToDir(new File(System.getProperty("user.dir")));
    }

    private void goToDir(File dir) {
        File[] ff = dir.listFiles();
        if (ff == null) {
            fileContent.setText("Error reading file list of " + dir.getAbsolutePath() + ".");
            return;
        }
        setTitle(dir.getAbsolutePath());
        files = ff;
        Arrays.sort(files, FileViewer::compareFiles);
        File parent = dir.getParentFile();
        if (parent != null) {
            File[] af = new File[files.length+1];
            af[0] = parent;
            System.arraycopy(files, 0, af,1, files.length);
            files = af;
        }

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        if (parent != null)
            names[0] = "...";
        fileList.setListData(names);
    }

    private static int compareFiles(File f1, File f2) {
        if(f1.isDirectory() && f2.isFile())
            return -1;
        if (f1.isFile() && f2.isDirectory())
            return 1;
        return f1.getName().compareTo(f2.getName());
    }

    private void onFiledListItemSelection() {
        int seldNdx = fileList.getSelectedIndex();
        if (seldNdx == -1)
            return;

        if(files[seldNdx].isDirectory()) {
            fileContent.setText("");
            return;
        }

        try (FileReader fr = new FileReader(files[seldNdx])){
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = fr.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
            fileContent.setText(sb.toString());
            fileContent.setCaretPosition(0);
        } catch (IOException e) {
            fileContent.setText("Error reading file: " + e.getMessage() + ".");
        }
    }

    private void enterToDir() {
        int seldNdx = fileList.getSelectedIndex();
        if (seldNdx == -1)
            return;

        File f = files[seldNdx];

        if (!f.isDirectory())
            return;
        goToDir(f);
    }

    public static void main(String[] args) {
        new FileViewer().setVisible(true);
    }
}
