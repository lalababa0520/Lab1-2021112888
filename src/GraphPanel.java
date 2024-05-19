import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {
    private Map<String, Map<String, Integer>> graph; // 存储图的结构，节点及其邻接节点和边的权重
    private Map<String, Point> positions; // 存储每个节点在面板上的位置
    private String firstWord; // 图中第一个单词
    private List<String> highlightedPath; // 要高亮显示的路径

    // 色系，用于节点的颜色
    private final Color[] colors = {
            new Color(0x74AED4),
            new Color(0xECA8A9),
            new Color(0xD3E2B7),
            new Color(0xCFAFD4),
            new Color(0xF7C97E),
            new Color(0xeca680),
            new Color(0x71DBC8),
            new Color(0xfb63a9),
            new Color(0x8ccbea),
            new Color(0x76925c),
    };
    // 构造方法，初始化图和节点的位置
    public GraphPanel() {
        graph = new HashMap<>();
        positions = new HashMap<>();
        highlightedPath = new ArrayList<>();
    }

    // 设置图结构和第一个单词，并生成节点位置
    public void setGraph(Map<String, Map<String, Integer>> graph, String firstWord) {
        this.graph = graph;
        this.firstWord = firstWord;
        generatePositions();
    }

    // 设置高亮路径
    public void setHighlightedPath(List<String> path) {
        highlightedPath = path;
    }

    // 清除高亮路径
    public void clearHighlightedPath(){
        highlightedPath = new ArrayList<>();
    }

    public void clearGraph() {
        graph = new HashMap<>();
        positions = new HashMap<>();
        highlightedPath = new ArrayList<>();
        firstWord = null;
        repaint();
    }

    // 生成节点的位置
    private void generatePositions() {
        int width = 1000; // 面板宽度
        int height =1400; // 面板高度
        int margin = 10; // 边距
        int levelHeight = (height - 2 * margin) / (graph.size() + 1); // 计算每层的高度

        Map<String, Integer> levels = new HashMap<>(); // 存储每个节点的层次
        Set<String> visited = new HashSet<>(); // 存储已访问的节点
        int maxLevel = 0; // 记录最大层次

        if (firstWord != null && graph.containsKey(firstWord)) {
            maxLevel = assignLevels(firstWord, 0, levels, visited); // 分配层次
        }

        Map<Integer, List<String>> levelNodes = new HashMap<>(); // 存储每层的节点
        for (String node : levels.keySet()) {
            int level = levels.get(node);
            levelNodes.putIfAbsent(level, new ArrayList<>());
            levelNodes.get(level).add(node);
        }

        // 根据层次和节点数量计算节点位置
        for (int level = 0; level <= maxLevel; level++) {
            List<String> nodes = levelNodes.get(level);
            if (nodes != null) {
                int levelWidth = (width - 2 * margin) / (nodes.size() + 1);
                for (int i = 0; i < nodes.size(); i++) {
                    String node = nodes.get(i);
                    int x = margin + (i + 1) * levelWidth;
                    int y = margin + level * levelHeight;
                    positions.put(node, new Point(x, y));
                }
            }
        }
    }

    // 分配节点层次
    private int assignLevels(String node, int level, Map<String, Integer> levels, Set<String> visited) {
        visited.add(node);
        levels.put(node, level);
        int maxLevel = level;

        if (graph.containsKey(node)) {
            for (String neighbor : graph.get(node).keySet()) {
                if (!visited.contains(neighbor)) {
                    maxLevel = Math.max(maxLevel, assignLevels(neighbor, level + 1, levels, visited));
                }
            }
        }

        return maxLevel;
    }

    // 重写 paintComponent 方法，绘制图形组件
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int sum = 0;
        // 绘制边
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String from = entry.getKey();
            Point p1 = positions.get(from);
            sum++;
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String to = edge.getKey();
                int weight = edge.getValue();
                Point p2 = positions.get(to);

                if (p2 == null) {
                    continue;
                }

                boolean isHighlighted = false;
                for (int i = 0; i < highlightedPath.size() - 1; i++) {
                    if (highlightedPath.get(i).equals(from) && highlightedPath.get(i + 1).equals(to)) {
                        isHighlighted = true;
                        break;
                    }
                }

                if (isHighlighted) {
                    g2d.setColor(Color.RED); // 突出显示的路径使用红色
                    g2d.setStroke(new BasicStroke(4)); // 较粗的线条
                } else {
                    g2d.setColor(colors[sum % colors.length]); // 其他路径使用灰色
                    g2d.setStroke(new BasicStroke(2)); // 较细的线条
                }
                drawCurvedArrowLine(g2d, p1.x, p1.y, p2.x, p2.y, isHighlighted); // 画箭头线
                g2d.setColor(colors[sum % colors.length]);
                g2d.drawString(String.valueOf(weight), (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            }
        }

        // 绘制节点
        sum = 0;
        for (Map.Entry<String, Point> entry : positions.entrySet()) {
            String node = entry.getKey();
            Point p = entry.getValue();
            int adjSize = graph.getOrDefault(node, new HashMap<>()).size();
            sum++;
            Color nodeColor = colors[sum % colors.length];
            g2d.setColor(nodeColor);

            // 计算椭圆大小
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(node);
            int stringHeight = fm.getAscent();
            int ellipseWidth = stringWidth + 10;
            int ellipseHeight = stringHeight + 5;

            // 绘制椭圆
            g2d.fillOval(p.x - ellipseWidth / 2, p.y - ellipseHeight / 2, ellipseWidth, ellipseHeight);
            g2d.setColor(nodeColor);
            g2d.drawOval(p.x - ellipseWidth / 2, p.y - ellipseHeight / 2, ellipseWidth, ellipseHeight);

            // 绘制节点文本
            g2d.setColor(Color.BLACK);
            g2d.drawString(node, p.x - stringWidth / 2, p.y + stringHeight / 4);
        }
    }

    private void drawCurvedArrowLine(Graphics2D g2d, int x1, int y1, int x2, int y2, boolean isHighlighted) {
        int d = 8; // 箭头的长度
        int h = 6;  // 箭头的宽度
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm, xn, ym, yn;
        double sin = dy / D, cos = dx / D;

        // 计算缩短后的线条终点
        int shorteningDistance = 5; // 调整此值以进一步缩短终点
        int newX2 = x1 + (int) ((D - shorteningDistance) * cos);
        int newY2 = y1 + (int) ((D - shorteningDistance) * sin);

        // 计算曲线的控制点
        int ctrlX = (x1 + x2) / 2 + dy / 4;
        int ctrlY = (y1 + y2) / 2 - dx / 4;

        // 计算终点的切线方向
        double t = 1; // 在曲线上的位置，取值范围在0到1之间
        double ctrlDx = 2 * (1 - t) * (ctrlX - x1) + 2 * t * (x2 - ctrlX);
        double ctrlDy = 2 * (1 - t) * (ctrlY - y1) + 2 * t * (y2 - ctrlY);
        double ctrlD = Math.sqrt(ctrlDx * ctrlDx + ctrlDy * ctrlDy);
        double ctrlSin = ctrlDy / ctrlD;
        double ctrlCos = ctrlDx / ctrlD;

        // 基于新的终点和切线方向计算箭头的点
        xm = newX2 - d * ctrlCos + h * ctrlSin;
        ym = newY2 - d * ctrlSin - h * ctrlCos;
        xn = newX2 - d * ctrlCos - h * ctrlSin;
        yn = newY2 - d * ctrlSin + h * ctrlCos;

        int[] xpoints = {newX2, (int) xm, (int) xn};
        int[] ypoints = {newY2, (int) ym, (int) yn};

        // 画缩短的曲线
        QuadCurve2D.Double curve = new QuadCurve2D.Double(x1, y1, ctrlX, ctrlY, newX2, newY2);
        g2d.draw(curve);

        // 画实心箭头
        g2d.fillPolygon(xpoints, ypoints, 3);
    }

    // 添加保存图形文件的方法
    public void saveGraphAsImage(File file) throws IOException {
        int width = this.getWidth();
        int height = this.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        this.paint(g2d);
        g2d.dispose();
        ImageIO.write(image, "PNG", file);
    }
}
