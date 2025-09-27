package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(36, 84, 251));
        g2d.fillRect(0, 0, getWidth(), 100);
        g2d.fillRect(0, 100, 100, getHeight() - 100);
    }
}

public class Main extends JFrame implements ActionListener, ChangeListener {
    private JButton download;
    private JButton saveButton;
    private JLabel imageLabel;
    private JButton Filter;
    private JButton Encoder;
    private JButton Decryptor;
    private static final int MAX_IMAGE_SIZE = 750;
    private BufferedImage currentProcessedImage = null;
    private static final Color PRIMARY_COLOR = new Color(244, 244, 244);
    private static final Color SECONDARY_COLOR = new Color(110, 115, 117);
    private static final int FONT_SIZE = 12;
    private JTextArea textArea;
    private JScrollPane imageScrollPane;
    private JScrollPane textScrollPane;
    private int imageWidth = 300;
    private int imageHeight = 300;
    private GradientPanel contentPane;
    private RoundButton backButton;
    private boolean showingText = false;
    private final char[] charset;
    private Map<Character, Integer> charToNumberMap;
    private String originalText;
    private List<Integer> originalPixels;
    private BufferedImage originalImage;
    private Map<Integer, Character> numberToCharMap;
    private String encodedText;
    private Map<Character, Integer> map = new HashMap<>();

    public Main() {
        super("Обработчик изображений");
        setupLookAndFeel();
        setupFrame();
        setupComponents();
        addListeners();
        setFileChooserFont();

        charset = generateCharset('!');
        numberToCharMap = createNumberToCharMap();
        charToNumberMap = createCharToNumberMap();

        setVisible(true);
    }

    private Map<Integer, Character> createNumberToCharMap() {
        Map<Integer, Character> map = new HashMap<>();
        for (int i = 0; i <= 255; i++) {
            char c = charset[i % charset.length];
            map.put(i, c);
        }
        return map;
    }

    private char[] generateCharset(char startChar) {
        char[] charset = new char[256];
        int charIndex = 0;
        for (int i = (int) startChar; i < 65536; i++) {
            char c = (char) i;
            if (Character.isDefined(c) && Character.isLetterOrDigit(c)) {
                charset[charIndex++] = c;
                if (charIndex == 256) break;
            }
        }
        for (int i = charIndex; i < 256; i++) {
            charset[i] = ' ';
        }
        return charset;
    }

    private Map<Character, Integer> createCharToNumberMap() {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i <= 255; i++) {
            char c = numberToCharMap.get(i);
            map.put(c, i);

        }
        return map;
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void setupComponents() {
        contentPane = new GradientPanel();
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(PRIMARY_COLOR);

        download = createButton("Загрузить");
        saveButton = createButton("Сохранить");

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        leftPanel.setBackground(PRIMARY_COLOR);
        leftPanel.add(download);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightPanel.setBackground(PRIMARY_COLOR);
        rightPanel.add(saveButton);

        buttonPanel.add(leftPanel, BorderLayout.WEST);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        imageScrollPane = new JScrollPane(imageLabel);
        imageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        Encoder = CoderButton("Шифровать");
        Decryptor = CoderButton("Расшифровать");
        backButton = new RoundButton("←");

        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        r.setBackground(PRIMARY_COLOR);
        r.add(Encoder);

        JPanel l = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        l.setBackground(PRIMARY_COLOR);
        l.add(Decryptor);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        backPanel.setBackground(PRIMARY_COLOR);
        backPanel.add(backButton);
        backButton.setVisible(false);

        buttonPanel.add(l, BorderLayout.NORTH);
        buttonPanel.add(r, BorderLayout.CENTER);
        contentPane.add(backPanel, BorderLayout.NORTH);

        contentPane.add(imageScrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.setBackground(PRIMARY_COLOR);

        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(imageWidth, imageHeight));
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private static class RoundButton extends JButton {
        public RoundButton(String text) {
            super(text);
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setPreferredSize(new Dimension(30, 30));
            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(new Color(232, 113, 25));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, getWidth(), getHeight());
            g2d.setColor(new Color(244, 244, 244, 0));
            g2d.fill(circle);
            super.paintComponent(g2d);
            g2d.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            Shape shape = new Ellipse2D.Double(0, 0, getWidth(), getHeight());
            return shape.contains(x, y);
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        button.setBackground(new Color(244, 244, 244, 0));
        button.setForeground(new Color(232, 113, 25));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return button;
    }

    private JButton CoderButton(String text) {
        JButton encoder = new JButton(text);
        encoder.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        encoder.setBackground(new Color(244, 244, 244, 0));
        encoder.setForeground(new Color(232, 113, 25));
        encoder.setFocusPainted(false);
        encoder.setPreferredSize(new Dimension(150, 50));
        encoder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return encoder;
    }

    private void addListeners() {
        download.addActionListener(this);
        saveButton.addActionListener(this);
        Encoder.addActionListener(this);
        Decryptor.addActionListener(this);
        backButton.addActionListener(this);
    }

    private void handleDownloadAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Изображения", "jpg", "jpeg", "png", "gif", "bmp");
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt");
        fileChooser.addChoosableFileFilter(imageFilter);
        fileChooser.addChoosableFileFilter(textFilter);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileExtension = getFileExtension(selectedFile);

            try {
                if (fileExtension.equalsIgnoreCase("txt")) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        textArea.setText(stringBuilder.toString());

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(Main.this, "Ошибка при чтении текстового файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                    imageLabel.setIcon(null);
                    currentProcessedImage = null;
                    originalImage = null;
                    originalPixels = null;

                } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg") ||
                        fileExtension.equalsIgnoreCase("png") || fileExtension.equalsIgnoreCase("gif") ||
                        fileExtension.equalsIgnoreCase("bmp")) {
                    BufferedImage originalImage = ImageIO.read(selectedFile);
                    if (originalImage != null) {
                        this.originalImage = originalImage;
                        currentProcessedImage = originalImage;
                        int scaledWidth = originalImage.getWidth() / 5;
                        int scaledHeight = originalImage.getHeight() / 5;
                        ImageIcon scaledIcon = scaleImage(originalImage, scaledWidth, scaledHeight);
                        imageLabel.setIcon(scaledIcon);
                        imageWidth = scaledIcon.getIconWidth();
                        imageHeight = scaledIcon.getIconHeight();
                        imageScrollPane.setPreferredSize(new Dimension(imageWidth, imageHeight));
                        imageScrollPane.revalidate();
                        imageScrollPane.repaint();
                        textScrollPane.setPreferredSize(new Dimension(imageWidth, imageHeight));
                        originalPixels = getPixelValues(currentProcessedImage);
                    } else {
                        handleImageProcessingError(new IOException("Не удалось загрузить изображение"));
                    }
                } else {
                    JOptionPane.showMessageDialog(Main.this, "Неподдерживаемый тип файла", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                handleImageProcessingError(ex);
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }

    private void setFileChooserFont() {
        final FontUIResource font = new FontUIResource(new Font("Arial", Font.PLAIN, FONT_SIZE));
        UIManager.put("FileChooser.font", font);
        UIManager.put("FileChooser.listFont", font);
        UIManager.put("FileChooser.viewFont", font);
        UIManager.put("FileChooser.detailViewFont", font);
    }

    private void handleImageProcessingError(IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(Main.this, "Ошибка при обработке изображения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        currentProcessedImage = null;
        imageLabel.setIcon(null);
    }

    private BufferedImage invertColors(BufferedImage originalImage) {
        if (originalImage == null) return null;
        final int width = originalImage.getWidth();
        final int height = originalImage.getHeight();
        final BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final Color originalColor = new Color(originalImage.getRGB(x, y));
                processedImage.setRGB(x, y, new Color(255 - originalColor.getRed(), 255 - originalColor.getGreen(), 255 - originalColor.getBlue()).getRGB());
            }
        }
        return processedImage;
    }

    private ImageIcon scaleImage(BufferedImage originalImage, int maxLabelWidth, int maxLabelHeight) {
        if (originalImage == null) return new ImageIcon();
        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();
        if (imageWidth <= 0 || imageHeight <= 0) return new ImageIcon();
        double scale = Math.min((double) maxLabelWidth / imageWidth, (double) maxLabelHeight / imageHeight);
        final Image scaledImage = originalImage.getScaledInstance((int) (imageWidth * scale), (int) (imageHeight * scale), Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private void saveImage() {
        String textToSave = textArea.getText();

        if (textToSave == null || textToSave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет текста для сохранения", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить текстовый файл");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write(textToSave);
                JOptionPane.showMessageDialog(this, "Текст сохранен в файл: " + fileToSave.getAbsolutePath(), "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void replaceImageWithTextField() {
        if (currentProcessedImage == null) return;
        contentPane.remove(imageScrollPane);
        contentPane.add(textScrollPane, BorderLayout.CENTER);
        backButton.setVisible(true);
        showingText = true;
        contentPane.revalidate();
        contentPane.repaint();
    }

    private void showImage() {
        if (currentProcessedImage == null) return;
        contentPane.remove(textScrollPane);
        contentPane.add(imageScrollPane, BorderLayout.CENTER);
        backButton.setVisible(false);
        showingText = false;
        contentPane.revalidate();
        contentPane.repaint();
    }

    private String processImage(BufferedImage image) {
        if (image == null) return null;
        StringBuilder pixelData = new StringBuilder();
        final int width = image.getWidth();
        final int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                pixelData.append(String.format("%d %d %d ", color.getRed(), color.getGreen(), color.getBlue()));
            }
            pixelData.append("\n");
        }
        return pixelData.toString();
    }

    private String encodeText() {
        String text = textArea.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        StringBuilder encodedText = new StringBuilder();
        String[] words = text.trim().split("\\s+");
        for (String word : words) {
            try {
                int number = Integer.parseInt(word);
                if (number >= 0 && number <= 255) {
                    encodedText.append(charset[number]).append(" ");
                } else {
                    encodedText.append(word).append(" ");
                }
            } catch (NumberFormatException ex) {
                encodedText.append(word).append(" ");
            }
        }
        return encodedText.toString().trim();
    }

    private String encodePixelData(String pixelData) {
        StringBuilder encodedPixelData = new StringBuilder();
        String[] numbers = pixelData.split("\\s+");
        for (int i = 0; i < numbers.length; i++) {
            String numberStr = numbers[i];
            try {
                int number = Integer.parseInt(numberStr);
                if (numberToCharMap.containsKey(number)) {
                    encodedPixelData.append(numberToCharMap.get(number));
                } else {
                    encodedPixelData.append("?");
                }
            } catch (NumberFormatException e) {
                encodedPixelData.append(numberStr);
            }
        }
        return encodedPixelData.toString();
    }

    private String decodePixelData(String encodedText) {
        StringBuilder decodedPixelData = new StringBuilder();
        for (int i = 0; i < encodedText.length(); i++) {
            char encodedChar = encodedText.charAt(i);
            if (charToNumberMap.containsKey(encodedChar)) {
                int number = charToNumberMap.get(encodedChar);
                decodedPixelData.append(number).append(" ");
            } else {
                decodedPixelData.append("? ");
            }
        }
        return decodedPixelData.toString();
    }

    private void decodeText() {
        String encodedText = textArea.getText();
        if (encodedText == null || encodedText.isEmpty()) {
            JOptionPane.showMessageDialog(Main.this, "Нет текста для расшифровки", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String decodedText = decodePixelData(encodedText);
        if (decodedText == null) {
            JOptionPane.showMessageDialog(Main.this, "Ошибка при расшифровке текста", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        textArea.setText(decodedText);
        replaceImageWithTextField();

        // Попытка преобразовать расшифрованные данные в изображение (только если есть изображение)
        if (originalImage != null) {
            try {
                String[] rgbValues = decodedText.trim().split("\\s+");
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();

                if (rgbValues.length >= width * height * 3) {
                    BufferedImage restoredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    int index = 0;
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int red = Integer.parseInt(rgbValues[index++]);
                            int green = Integer.parseInt(rgbValues[index++]);
                            int blue = Integer.parseInt(rgbValues[index++]);

                            Color color = new Color(red, green, blue);
                            restoredImage.setRGB(x, y, color.getRGB());
                        }
                    }

                    restoredImage = invertColors(restoredImage);
                    currentProcessedImage = restoredImage;
                    ImageIcon restoredIcon = scaleImage(restoredImage, imageWidth, imageHeight);
                    imageLabel.setIcon(restoredIcon);

                } else {
                    JOptionPane.showMessageDialog(Main.this, "Недостаточно RGB значений для восстановления изображения", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(Main.this, "Ошибка при преобразовании RGB значений: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Main.this, "Произошла общая ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(Main.this, "Невозможно создать изображение, так как оно не загружено.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == download) {
            handleDownloadAction(e);
        } else if (e.getSource() == saveButton) {
            saveImage();
        } else if (e.getSource() == Encoder) {
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    if (currentProcessedImage != null) {
                        BufferedImage invertedImage = invertColors(currentProcessedImage);
                        currentProcessedImage = invertedImage;
                        String pixelData = processImage(currentProcessedImage);
                        return encodePixelData(pixelData);
                    } else {
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        encodedText = get();
                        if (encodedText != null) {
                            textArea.setText(encodedText);
                            replaceImageWithTextField();
                        } else {
                            JOptionPane.showMessageDialog(Main.this, "Ошибка при обработке изображения. Изображение не загружено.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(Main.this, "Произошла ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } else if (e.getSource() == Decryptor) {
            decodeText();
        } else if (e.getSource() == backButton) {
            showImage();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    private List<Integer> getPixelValues(BufferedImage image) {
        if (image == null) return null;
        List<Integer> pixels = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels.add(image.getRGB(x, y));
            }
        }
        return pixels;
    }
}