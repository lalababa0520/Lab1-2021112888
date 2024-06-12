
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FilenameUtils;

final class TextGraph {
  private Map<String, Map<String, Integer>> graph;
  private String firstWord;

  public TextGraph() {
    graph = new HashMap<>();
  }

  public void readTextFile(String filePath) throws IOException {
    graph = new HashMap<>();
    // 读取指定路径的文件内容
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String text = String.join(" ", lines).toLowerCase(); // 将列表中的所有行合并成一个字符串，并转换为小写
    text = text.replaceAll("[^a-z ]", " "); // 使用正则表达式将所有非小写字母和空格的字符替换为空格
    String[] words = text.split("\\s+"); // 使用正则表达式将文本字符串按一个或多个空白字符拆分成单词数组
    if (words.length > 0) { // 如果单词数组非空，将第一个单词设置为图的起始点
      firstWord = words[0];
    }

    // 遍历单词数组，从第一个单词到倒数第二个单词，目的是构建单词之间的边
    for (int i = 0; i < words.length - 1; i++) {
      // 当前单词和下一个单词
      String word1 = words[i];
      String word2 = words[i + 1];
      graph.putIfAbsent(word1, new HashMap<>()); // 确保图中包含word1节点，如果没有，则添加一个新的空邻居列表
      // 确保图中包含word2节点，如果没有，则添加一个新的空邻居列表
      graph.putIfAbsent(word2, new HashMap<>());
      // 获取word1的邻居列表，将word2添加到该列表中
      // 使用getOrDefault方法获取从word1到word2的当前权重（默认值为0），然后将权重加1
      graph.get(word1).put(word2, graph.get(word1).getOrDefault(word2, 0) + 1);
    }
  }


  public Map<String, Map<String, Integer>> getGraph() {
    return graph;
  }

  public String getFirstWord() {
    return firstWord;
  }

  public void printGraph() {
    // Use entrySet() to iterate over map entries directly
    for (Map.Entry<String, Map<String, Integer>> outerEntry : graph.entrySet()) {
      String from = outerEntry.getKey();
      Map<String, Integer> innerMap = outerEntry.getValue();

      System.out.print(from + " -> ");

      // Continue to use entrySet() for the inner map
      for (Map.Entry<String, Integer> entry : innerMap.entrySet()) {
        System.out.print(entry.getKey() + " (" + entry.getValue() + ") ");
      }

      System.out.println();
    }
  }


  //查询桥接词
  public String queryBridgeWords(String word1, String word2) {
    if (!graph.containsKey(word1) && !graph.containsKey(word2)) {
      return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
    } else if (!graph.containsKey(word1)) {
      return "No \"" + word1 + "\" in the graph!";
    } else if (!graph.containsKey(word2)) {
      return "No \"" + word2 + "\" in the graph!";
    }

    Set<String> bridgeWords = new HashSet<>();
    Map<String, Integer> neighbors = graph.get(word1); //单词1的所有邻接词
    for (String neighbor : neighbors.keySet()) {
      if (graph.get(neighbor).containsKey(word2)) {
        bridgeWords.add(neighbor);
      }
    }

    if (bridgeWords.isEmpty()) {
      return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
    } else {
      String result;
      if (bridgeWords.size() > 1) {
        result = "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ";
      } else {
        result = "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" is: ";
      }
      result += String.join(", ", bridgeWords);
      return result;
    }
  }

  //生成桥接词
  public String generateNewText(String inputText) {
    String[] words = inputText.toLowerCase().split("\\s+");
    StringBuilder newText = new StringBuilder();
    Random rand = new SecureRandom();

    for (int i = 0; i < words.length - 1; i++) {
      newText.append(words[i]).append(" ");
      String word1 = words[i];
      String word2 = words[i + 1];
      Set<String> bridgeWords = new HashSet<>();

      if (graph.containsKey(word1)) {
        for (String neighbor : graph.get(word1).keySet()) {
          if (graph.get(neighbor).containsKey(word2)) {
            bridgeWords.add(neighbor);
          }
        }
      }

      if (!bridgeWords.isEmpty()) {
        String[] bridges = bridgeWords.toArray(new String[0]);
        String bridgeWord = bridges[rand.nextInt(bridges.length)];
        newText.append(bridgeWord).append(" ");
      }
    }
    newText.append(words[words.length - 1]);

    return newText.toString();
  }


  //计算最短路
  public String calcShortestPath(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      if (!graph.containsKey(word1) && !graph.containsKey(word2)) {
        return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
      } else if (!graph.containsKey(word1)) {
        return "No \"" + word1 + "\" in the graph!";
      } else {
        return "No \"" + word2 + "\" in the graph!";
      }
    }

    Map<String, Integer> distances = new HashMap<>();
    Map<String, String> previousNodes = new HashMap<>();
    PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    for (String node : graph.keySet()) {
      distances.put(node, Integer.MAX_VALUE);
      previousNodes.put(node, null);
    }

    distances.put(word1, 0);
    pq.add(word1);

    while (!pq.isEmpty()) {
      String current = pq.poll();
      if (current.equals(word2)) {
        break;
      }

      for (Map.Entry<String, Integer> neighbor : graph.get(current).entrySet()) {
        int newDist = distances.get(current) + neighbor.getValue();
        if (newDist < distances.get(neighbor.getKey())) {
          distances.put(neighbor.getKey(), newDist);
          previousNodes.put(neighbor.getKey(), current);
          pq.add(neighbor.getKey());
        }
      }
    }

    if (distances.get(word2) == Integer.MAX_VALUE) {
      return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
    }

    List<String> path = new ArrayList<>();
    for (String at = word2; at != null; at = previousNodes.get(at)) {
      path.add(at);
    }
    Collections.reverse(path);

    StringBuilder result;
    result = new StringBuilder("Shortest path from \"" + word1 + "\" to \"" + word2 + "\": ");
    result.append(String.join(" -> ", path));
    result.append(" (Total weight: ").append(distances.get(word2)).append(")");

    return result.toString();
  }

  //得到最短路
  public List<String> getShortestPath(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return Collections.emptyList();
    }

    Map<String, Integer> distances = new HashMap<>();
    Map<String, String> previousNodes = new HashMap<>();
    PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    for (String node : graph.keySet()) {
      distances.put(node, Integer.MAX_VALUE);
      previousNodes.put(node, null);
    }

    distances.put(word1, 0);
    pq.add(word1);

    while (!pq.isEmpty()) {
      String current = pq.poll();
      if (current.equals(word2)) {
        break;
      }

      for (Map.Entry<String, Integer> neighbor : graph.get(current).entrySet()) {
        int newDist = distances.get(current) + neighbor.getValue();
        if (newDist < distances.get(neighbor.getKey())) {
          distances.put(neighbor.getKey(), newDist);
          previousNodes.put(neighbor.getKey(), current);
          pq.add(neighbor.getKey());
        }
      }
    }

    if (distances.get(word2) == Integer.MAX_VALUE) {
      return Collections.emptyList();
    }

    List<String> path = new ArrayList<>();
    for (String at = word2; at != null; at = previousNodes.get(at)) {
      path.add(at);
    }
    Collections.reverse(path);

    return path;
  }

  public Map<String, List<String>> calcShortestPathsFrom(String word) {
    Map<String, List<String>> shortestPaths = new HashMap<>();
    if (!graph.containsKey(word)) {
      return shortestPaths;
    }

    Map<String, Integer> distances = new HashMap<>();
    Map<String, String> previousNodes = new HashMap<>();
    PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    for (String node : graph.keySet()) {
      distances.put(node, Integer.MAX_VALUE);
      previousNodes.put(node, null);
    }

    distances.put(word, 0);
    pq.add(word);

    while (!pq.isEmpty()) {
      String current = pq.poll();

      for (Map.Entry<String, Integer> neighbor : graph.get(current).entrySet()) {
        int newDist = distances.get(current) + neighbor.getValue();
        if (newDist < distances.get(neighbor.getKey())) {
          distances.put(neighbor.getKey(), newDist);
          previousNodes.put(neighbor.getKey(), current);
          pq.add(neighbor.getKey());
        }
      }
    }

    for (String target : graph.keySet()) {
      if (!target.equals(word) && distances.get(target) != Integer.MAX_VALUE) {
        List<String> path = new ArrayList<>();
        for (String at = target; at != null; at = previousNodes.get(at)) {
          path.add(at);
        }
        Collections.reverse(path);
        shortestPaths.put(target, path);
      }
    }
    return shortestPaths;
  }

  //随机游走
  public String randomWalk(Supplier<Boolean> stopSignal, Consumer<String> stepCallback) {
    if (graph.isEmpty()) {
      return "Graph is empty!";
    }

    Random rand = new SecureRandom();
    List<String> nodes = new ArrayList<>(graph.keySet());
    String currentNode = nodes.get(rand.nextInt(nodes.size())); // 随机选择起始节点
    StringBuilder walk = new StringBuilder(currentNode);
    Set<String> visitedEdges = new HashSet<>();

    stepCallback.accept("Step: " + currentNode); // 打印第一个节点

    while (graph.containsKey(currentNode) && !graph.get(currentNode).isEmpty()) {
      if (stopSignal.get()) {
        break;
      }
      Map<String, Integer> neighbors = graph.get(currentNode);
      List<String> neighborList = new ArrayList<>(neighbors.keySet());
      String nextNode = neighborList.get(rand.nextInt(neighborList.size())); // 随机选择下一个节点
      String edge = currentNode + "->" + nextNode;
      if (visitedEdges.contains(edge)) {
        break;
      }
      visitedEdges.add(edge);
      currentNode = nextNode;
      walk.append(" -> ").append(currentNode);

      stepCallback.accept("Step: " + currentNode); // 处理当前步骤回调

      try {
        Thread.sleep(1000); // 1秒延迟
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }

    String result = walk.toString();
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream("random_walk.txt"), StandardCharsets.UTF_8))) {
      writer.write(result); // 将结果写入文件，指定使用UTF-8编码
    } catch (IOException e) {
      e.printStackTrace(); // 异常处理
    }

    return result;
  }


  public void saveGraphImage(GraphPanel graphPanel, String filePath) throws IOException {
    File file;
    file = new File(FilenameUtils.getName(filePath));
    graphPanel.saveGraphAsImage(file);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      TextGraphGui frame = new TextGraphGui();
      frame.setVisible(true);
    });
  }
}
