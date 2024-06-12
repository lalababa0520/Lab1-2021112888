import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


final class TextGraphGui extends JFrame {
  private final TextGraph textGraph;
  private final JTextField filePathField;
  private final GraphPanel graphPanel;
  private final JTextField word1Field;
  private final JTextField word2Field;
  private final JTextArea resultArea;
  private final JTextField inputTextField;
  private final JTextArea outputArea;
  private final JTextField spWord1Field;
  private final JTextField spWord2Field;
  private final JTextArea spResultArea;
  private final JTextArea randomWalkArea;
  private final JTextField spFromField;
  private final JTextArea spFromResultArea;
  private volatile boolean stopRandomWalk;

  public TextGraphGui() {
    textGraph = new TextGraph();

    setTitle("Text Graph GUI");
    setSize(1000, 1000);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    JPanel inputPanel = new JPanel();
    filePathField = new JTextField(40);
    JButton loadButton = new JButton("Load File");
    JButton saveButton = new JButton("Save Image");
    inputPanel.add(new JLabel("File Path:"));
    inputPanel.add(filePathField);
    inputPanel.add(loadButton);
    inputPanel.add(saveButton);
    add(inputPanel, BorderLayout.NORTH);

    graphPanel = new GraphPanel();
    add(graphPanel, BorderLayout.CENTER);

    loadButton.addActionListener(e -> loadFile());
    saveButton.addActionListener(e -> saveGraphImage());

    JTabbedPane tabbedPane;
    tabbedPane = new JTabbedPane();

    JPanel queryPanel;
    queryPanel = new JPanel();
    word1Field = new JTextField(10);
    word2Field = new JTextField(10);
    JButton queryButton;
    queryButton = new JButton("Query Bridge Words");
    resultArea = new JTextArea(6, 40);
    resultArea.setLineWrap(true);
    resultArea.setWrapStyleWord(true);
    resultArea.setEditable(false);
    queryPanel.setLayout(new BorderLayout());
    JPanel queryInputPanel = new JPanel();
    queryInputPanel.add(new JLabel("Word1:"));
    queryInputPanel.add(word1Field);
    queryInputPanel.add(new JLabel("Word2:"));
    queryInputPanel.add(word2Field);
    queryInputPanel.add(queryButton);
    queryPanel.add(queryInputPanel, BorderLayout.NORTH);
    queryPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
    tabbedPane.addTab("Query Bridge Words", queryPanel);

    queryButton.addActionListener(e -> queryBridgeWords());

    JPanel generatePanel;
    generatePanel = new JPanel();
    inputTextField = new JTextField(30);
    JButton generateButton;
    generateButton = new JButton("Generate New Text");
    outputArea = new JTextArea(6, 40);
    outputArea.setLineWrap(true);
    outputArea.setWrapStyleWord(true);
    outputArea.setEditable(false);
    generatePanel.setLayout(new BorderLayout());
    JPanel generateInputPanel = new JPanel();
    generateInputPanel.add(new JLabel("Input Text:"));
    generateInputPanel.add(inputTextField);
    generateInputPanel.add(generateButton);
    generatePanel.add(generateInputPanel, BorderLayout.NORTH);
    generatePanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
    tabbedPane.addTab("Generate New Text", generatePanel);

    generateButton.addActionListener(e -> generateNewText());

    JPanel shortestPathPanel;
    shortestPathPanel = new JPanel();
    spWord1Field = new JTextField(10);
    spWord2Field = new JTextField(10);
    JButton shortestPathButton;
    shortestPathButton = new JButton("Calculate Shortest Path");
    spResultArea = new JTextArea(6, 40);
    spResultArea.setLineWrap(true);
    spResultArea.setWrapStyleWord(true);
    spResultArea.setEditable(false);
    shortestPathPanel.setLayout(new BorderLayout());
    JPanel shortestPathInputPanel = new JPanel();
    shortestPathInputPanel.add(new JLabel("Word1:"));
    shortestPathInputPanel.add(spWord1Field);
    shortestPathInputPanel.add(new JLabel("Word2:"));
    shortestPathInputPanel.add(spWord2Field);
    shortestPathInputPanel.add(shortestPathButton);
    shortestPathPanel.add(shortestPathInputPanel, BorderLayout.NORTH);
    shortestPathPanel.add(new JScrollPane(spResultArea), BorderLayout.CENTER);
    tabbedPane.addTab("Shortest Path", shortestPathPanel);

    shortestPathButton.addActionListener(e -> calculateShortestPath());

    JPanel randomWalkPanel;
    randomWalkPanel = new JPanel();
    JButton randomWalkButton;
    randomWalkButton = new JButton("Random Walk");
    JButton stopRandomWalkButton;
    stopRandomWalkButton = new JButton("Stop Random Walk");
    randomWalkArea = new JTextArea(6, 40);
    randomWalkArea.setLineWrap(true);
    randomWalkArea.setWrapStyleWord(true);
    randomWalkArea.setEditable(false);
    randomWalkPanel.setLayout(new BorderLayout());
    JPanel randomWalkInputPanel = new JPanel();
    randomWalkInputPanel.add(randomWalkButton);
    randomWalkInputPanel.add(stopRandomWalkButton);
    randomWalkPanel.add(randomWalkInputPanel, BorderLayout.NORTH);
    randomWalkPanel.add(new JScrollPane(randomWalkArea), BorderLayout.CENTER);
    tabbedPane.addTab("Random Walk", randomWalkPanel);

    randomWalkButton.addActionListener(e -> performRandomWalk());
    stopRandomWalkButton.addActionListener(e -> stopRandomWalk = true);

    JPanel shortestPathsFromPanel;
    shortestPathsFromPanel = new JPanel();
    spFromField = new JTextField(10);
    JButton shortestPathsFromButton;
    shortestPathsFromButton = new JButton("Calculate All Shortest Paths");
    spFromResultArea = new JTextArea(6, 40);
    spFromResultArea.setLineWrap(true);
    spFromResultArea.setWrapStyleWord(true);
    spFromResultArea.setEditable(false);
    shortestPathsFromPanel.setLayout(new BorderLayout());
    JPanel shortestPathsFromInputPanel = new JPanel();
    shortestPathsFromInputPanel.add(new JLabel("From:"));
    shortestPathsFromInputPanel.add(spFromField);
    shortestPathsFromInputPanel.add(shortestPathsFromButton);
    shortestPathsFromPanel.add(shortestPathsFromInputPanel, BorderLayout.NORTH);
    shortestPathsFromPanel.add(new JScrollPane(spFromResultArea), BorderLayout.CENTER);
    tabbedPane.addTab("All Shortest Paths", shortestPathsFromPanel);

    shortestPathsFromButton.addActionListener(e -> calculateAllShortestPaths());

    add(tabbedPane, BorderLayout.SOUTH);
  }

  private void loadFile() {
    String filePath = filePathField.getText();
    try {
      graphPanel.clearGraph(); // 清除旧的图形数据
      textGraph.readTextFile(filePath);
      textGraph.printGraph();
      graphPanel.setGraph(textGraph.getGraph(), textGraph.getFirstWord());
      graphPanel.repaint();
      JOptionPane.showMessageDialog(this, "File loaded successfully!");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
    }
  }

  private void saveGraphImage() {
    String currentDirectory = System.getProperty("user.dir");
    File fileToSave = new File(currentDirectory, "graph.png");
    try {
      graphPanel.saveGraphAsImage(fileToSave);
      JOptionPane.showMessageDialog(
              this, "Graph image saved successfully to " + fileToSave.getAbsolutePath());
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(
              this, "Error saving graph image: " + ex.getMessage());
    }
  }

  private void queryBridgeWords() {
    String word1 = word1Field.getText().trim().toLowerCase();
    String word2 = word2Field.getText().trim().toLowerCase();
    String result = textGraph.queryBridgeWords(word1, word2);
    resultArea.setText(result);
  }

  private void generateNewText() {
    String inputText = inputTextField.getText().trim();
    String newText = textGraph.generateNewText(inputText);
    outputArea.setText(newText);
  }

  private void calculateShortestPath() {
    String word1 = spWord1Field.getText().trim().toLowerCase();
    String word2 = spWord2Field.getText().trim().toLowerCase();

    if (word2.isEmpty()) {
      Map<String, List<String>> allPaths = textGraph.calcShortestPathsFrom(word1);
      if (allPaths.isEmpty()) {
        spResultArea.setText("No paths from \"" + word1 + "\" in the graph!");
      } else {
        StringBuilder result = new StringBuilder("Shortest paths from \"" + word1 + "\":\n");
        for (Map.Entry<String, List<String>> entry : allPaths.entrySet()) {
          result.append(String.join(" -> ", entry.getValue()));
          result.append(" (Total weight: ").append(entry.getValue().size() - 1).append(")\n");
        }
        spResultArea.setText(result.toString());
      }
    } else {
      String result = textGraph.calcShortestPath(word1, word2);
      spResultArea.setText(result);
      List<String> path = textGraph.getShortestPath(word1, word2);
      graphPanel.setHighlightedPath(path);
      graphPanel.repaint();
    }
  }

  private void calculateAllShortestPaths() {
    String word = spFromField.getText().trim().toLowerCase();
    Map<String, List<String>> allPaths = textGraph.calcShortestPathsFrom(word);
    if (allPaths.isEmpty()) {
      spFromResultArea.setText("No paths from \"" + word + "\" in the graph!");
    } else {
      StringBuilder result = new StringBuilder("Shortest paths from \"" + word + "\":\n");
      for (Map.Entry<String, List<String>> entry : allPaths.entrySet()) {
        result.append(String.join(" -> ", entry.getValue()));
        result.append(" (Total weight: ").append(entry.getValue().size() - 1).append(")\n");
      }
      spFromResultArea.setText(result.toString());
    }
  }

  private void performRandomWalk() {
    stopRandomWalk = false;
    randomWalkArea.setText("");
    new Thread(() -> {
      String result = textGraph.randomWalk(
          () -> stopRandomWalk,
          step -> SwingUtilities.invokeLater(() -> randomWalkArea.append(step + "\n"))
        );
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
              this, "Random walk result saved to random_walk.txt\n" + result));
    }).start();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      TextGraphGui frame = new TextGraphGui();
      frame.setVisible(true);
    });
  }
}
