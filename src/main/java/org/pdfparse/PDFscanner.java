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
        // Создаем главное окно
        JFrame frame = new JFrame("PDF Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 200);

        // Создаем панель с компонентами
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Создаем текстовое поле для ввода пути
        JTextField textField = new JTextField();
        textField.setBackground(Color.ORANGE);
        panel.add(textField, BorderLayout.CENTER);

        // Создаем кнопку для подтверждения ввода
        JButton button = new JButton("Сканировать");
        button.setBackground(Color.lightGray);
        panel.add(button, BorderLayout.SOUTH);

        // Добавляем слушатель событий для кнопки
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                        JOptionPane.showMessageDialog(frame, "В указанной директории нет pdf файлов!",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                } else {
                    if (!file.exists() || !file.isFile()) {
                        JOptionPane.showMessageDialog(frame, "Указанный файл не существует или не является файлом.",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        JOptionPane.showMessageDialog(frame, "Указанный файл не является PDF-файлом.",
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

                String resultText = String.join("\n", resultList);
                JOptionPane.showMessageDialog(frame, resultText,
                        "Результат", JOptionPane.INFORMATION_MESSAGE);

                // Копируем результат в буфер обмена
                StringSelection selection = new StringSelection(resultText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            }
        });

        // Добавляем панель на окно и делаем окно видимым
        frame.add(panel);
        frame.setVisible(true);
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
