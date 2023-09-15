package org.pdfparse;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFscanner {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PDFscanner::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame inputFrame = new JFrame("PDF Scanner - Введите путь к файлу или директории");
        inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inputFrame.setSize(700, 100);

        // Панель для ввода пути
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        // Текстовое поле для ввода пути
        JTextField textField = new JTextField();
        inputPanel.add(textField, BorderLayout.CENTER);

        // Кнопка для подтверждения ввода
        JButton scanButton = new JButton("Сканировать");
        inputPanel.add(scanButton, BorderLayout.SOUTH);

        inputFrame.add(inputPanel);
        inputFrame.setVisible(true);

        // Создаем окно для вывода результата
        JFrame resultFrame = new JFrame("Результат");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(700, 200);

        // Создаем кнопку "Скопировать"
        JButton copyButton = new JButton("Скопировать");
        resultFrame.add(copyButton, BorderLayout.SOUTH);



        // Панель для вывода результата
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());

        // Текстовая область для вывода результата
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        resultFrame.add(resultPanel);
        resultFrame.setVisible(false);
        // Добавляем слушатель событий для кнопки
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("~~~~~ " + inputFrame.getX() + " " + inputPanel.getY());

                resultFrame.setLocation(inputFrame.getX(), inputPanel.getY() + 70);

                textArea.setText("");

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
                        JOptionPane.showMessageDialog(inputFrame, "В указанной директории нет pdf файлов!",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                } else {
                    if (!file.exists() || !file.isFile()) {
                        JOptionPane.showMessageDialog(inputFrame, "Указанный файл не существует или не является файлом.",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        JOptionPane.showMessageDialog(inputFrame, "Указанный файл не является PDF-файлом.",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    fileList.add(file);
                }
                List<String> resultList = new ArrayList<>();
                AtomicInteger totalPdfPages = new AtomicInteger();
                fileList.forEach(f -> {
                    int pageCount = countPagesInPDF(f);
                    totalPdfPages.addAndGet(pageCount);
                    resultList.add(f.getName() + ". Количество страниц в PDF-файле: " + pageCount);
                });
                resultList.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                resultList.add("Итого pdf-страниц: " + totalPdfPages.intValue());

                // Обновляем текстовую область с результатами
                StringBuilder resultText = new StringBuilder();
                resultList.forEach(result -> {
                    resultText.append(result).append("\n");
                    textArea.append(result + "\n");
                });

                resultFrame.setVisible(true);
            }
        });
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String resultText = textArea.getText();
                StringSelection selection = new StringSelection(resultText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(resultFrame, "Результат скопирован в буфер обмена", "Скопировано", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        // Добавляем панель на окно и делаем окно видимым
        resultFrame.add(resultPanel);
        resultPanel.setVisible(true);


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
