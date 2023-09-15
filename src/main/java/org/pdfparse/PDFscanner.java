package org.pdfparse;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFscanner {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PDFscanner::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("PDF Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 400);
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(PDFscanner.class.getResource("/icon.png")));
        frame.setIconImage(icon.getImage());


        // Создаем панель
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Создаем текстовое поле для ввода пути
        JTextField textField = new JTextField();
        panel.add(textField, BorderLayout.NORTH);

        // Создаем панель для кнопок "Сканировать" и "Скопировать результат"
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));

        // Создаем кнопку "Сканировать"
        JButton scanButton = new JButton("Сканировать");
        buttonPanel.add(scanButton);

        // Создаем кнопку "Скопировать результат"
        JButton copyButton = new JButton("Скопировать результат");
        buttonPanel.add(copyButton);

        panel.add(buttonPanel, BorderLayout.WEST);

        // Создаем текстовую область для вывода результата
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.LIGHT_GRAY);
        textArea.setForeground(Color.BLACK);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);

        // Добавляем слушатель событий для кнопки "Сканировать"
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performScan(textField, textArea, frame);
            }
        });

        // Добавляем слушатель событий для кнопки "Скопировать результат"
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String resultText = textArea.getText();
                StringSelection selection = new StringSelection(resultText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(frame, "Результат скопирован в буфер обмена", "Скопировано", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Добавляем слушатель для обработки нажатия клавиши Enter
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performScan(textField, textArea, frame);
                }
            }
        });
    }

    private static void performScan(JTextField textField, JTextArea textArea, Frame frame) {
        textArea.setText(""); // Очищаем текстовую область

        String filePath = textField.getText().trim();
        if (filePath.equalsIgnoreCase("exit")) {
            System.exit(0); // Выход из программы при вводе "exit"
        }

        if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
            filePath = filePath.substring(1, filePath.length() - 1);
        }

        Path path = Paths.get(filePath);

        File file = path.toFile();

        List<File> fileList = new ArrayList<>();
        if (file.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> {
                if (f.getName().toLowerCase().endsWith(".pdf")) {
                    fileList.add(f);
                }
            });
            if (fileList.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "В указанной директории нет pdf файлов!", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

        } else {
            if (!file.exists() || !file.isFile()) {
                JOptionPane.showMessageDialog(frame, "Указанный файл не существует или не является файлом.", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                JOptionPane.showMessageDialog(frame, "Указанный файл не является PDF-файлом.", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            fileList.add(file);
        }
        List<String> resultList = new ArrayList<>();

        int totalPdfPages = 0;

        for (File f : fileList) {
            int pageCount = countPagesInPDF(f);
            totalPdfPages += pageCount;
            resultList.add(f.getName() + ". Количество страниц в PDF-файле: " + pageCount);
        }
        resultList.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        resultList.add("ИТОГО:\nВсего файлов: " + fileList.size());
        resultList.add("\n\nИтого pdf-страниц: " + totalPdfPages);

        // Обновляем текстовую область с результатами
        resultList.forEach(result -> {
            textArea.append(result + "\n");
        });
    }

    private static int countPagesInPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
