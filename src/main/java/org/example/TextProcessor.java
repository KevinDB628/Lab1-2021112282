package org.example;

import org.graphstream.graph.Edge; // 边
import org.graphstream.graph.Graph; // 图
import org.graphstream.graph.Node; // 点
import org.graphstream.graph.implementations.SingleGraph; // 添加节点、边，以及设置图形的属性
import org.graphstream.ui.layout.springbox.implementations.SpringBox; // SpringBox布局算法
import org.graphstream.ui.view.Viewer; // 渲染图形

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;    //  以上用来读写文件和处理IO异常
import java.util.*; // 包含多种工具和接口

public class TextProcessor {

    public static void main(String[] args) {
        String filePath = "..//double//text.txt";  // Replace with your file path

        try {
            String processedText = processTextFile(filePath); // 传入文件路径,返回处理后的文本
            System.out.println("Processed Text:");
            System.out.println(processedText); // 输出处理过的文件内容

            // 将processedText处理后的文本转化成一个有向图的数据结构。
            Map<String, Map<String, Integer>> directedGraphData = buildDirectedGraph(processedText);
            // 创建一个GraphStream图对象
            Graph graph = createGraph(directedGraphData);

            // Console menu
            Scanner scanner = new Scanner(System.in); // 读取用户在控制台的输入
            boolean exit = false; // 设置一个布尔变量exit用于控制菜单循环
            while (!exit) {
                System.out.println("\nMenu:");
                System.out.println("1. 查看有向图");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 根据桥接词生成新文本");
                System.out.println("4. 计算最短路径");
                System.out.println("5. 随机游走");
                System.out.println("6. 退出");
                System.out.print("请选择操作（1-6）: ");
                int choice = scanner.nextInt(); // 获取用户的输入

                switch (choice) {
                    case 1:
                        showDirectedGraph(graph);
                        break;
                    case 2:
                        queryBridgeWords(scanner, graph);
                        break;
                    case 3:
                        generateNewText(scanner, graph);
                        break;
                    case 4:
                        calcShortestPath(scanner, graph);
                        break;
                    case 5:
                        randomWalk(graph);
                        break;
                    case 6: // 结束菜单循环，程序将退出
                        exit = true;
                        break;
                    default:
                        System.out.println("无效的选项，请重新选择。");
                }
            }

        } catch (IOException e) { // 如果发生IOException，则使用e.printStackTrace()方法打印异常信息，帮助定位问题所在。
            e.printStackTrace();
        }
    }

    // 读取并处理
    public static String processTextFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder(); // 创建了一个StringBuilder对象sb，用于动态构建和修改返回的字符串。
        BufferedReader reader = new BufferedReader(new FileReader(filePath)); // 支持缓冲以及逐行读取文本
        String line; // 存储从文件中读取的每一行文本

        while ((line = reader.readLine()) != null) { // 每次读取文件的一行。如果读取到的行不为null（即文件未读完），则继续循环
            sb.append(processLine(line)).append(" "); // 每处理完一行文本后，加一个空格作为分隔
        }
        reader.close();
        return sb.toString().trim(); // 将StringBuilder对象sb转换为字符串,使用trim()方法去掉最后追加的额外空格
    }

    public static String processLine(String line) {
        // replaceAll("[\\p{Punct}]", " ")将输入字符串line中的所有标点符号（根据正则表达式[\\p{Punct}]匹配到的）替换为空格
        // replaceAll("[^a-zA-Z\\s]", "")移除所有非英文字母和非空格字符
        // toLowerCase()将所有字符转换成小写
        return line.replaceAll("[\\p{Punct}]", " ").replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
    }
    // 有向图
    public static Map<String, Map<String, Integer>> buildDirectedGraph(String text) {
        String[] words = text.split("\\s+"); // 通过正则表达式"\\s+"分割，得到所有单词的数组words,\\s+的意思是一个或多个空格
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        for (int i = 0; i < words.length - 1; i++) { // 遍历所有单词
            String wordA = words[i];
            String wordB = words[i + 1];

            if (!graph.containsKey(wordA)) { // 是否已经包含了当前单词wordA作为一个节点
                graph.put(wordA, new HashMap<>()); // 添加wordA作为新节点
            }
            // 添加A到B的边
            Map<String, Integer> edges = graph.get(wordA);
            edges.put(wordB, Integer.valueOf(edges.getOrDefault(wordB, Integer.valueOf(0)) + 1));
        }

        return graph;
    }
    // 创建图
    public static Graph createGraph(Map<String, Map<String, Integer>> graphData) {
        Graph graph = new SingleGraph("Text Graph");

        for (String from : graphData.keySet()) { // 循环遍历graphData
            if (graph.getNode(from) == null) { // 创建起始节点
                Node node = graph.addNode(from);
                node.addAttribute("ui.label", from);
            }
            for (String to : graphData.get(from).keySet()) { // 遍历目标节点
                if (graph.getNode(to) == null) {
                    Node node = graph.addNode(to);
                    node.addAttribute("ui.label", to);
                }
                String edgeId = from + "->" + to; // 创建边,格式为“from->to”
                if (graph.getEdge(edgeId) == null) {
                    Edge edge = graph.addEdge(edgeId, from, to, true);
                    edge.setAttribute("weight", graphData.get(from).get(to));
                    edge.addAttribute("ui.label", graphData.get(from).get(to));
                }
            }
        }
        return graph;
    }
    // 可视化图
    public static void showDirectedGraph(Graph graph) {
        // 图的属性
        graph.addAttribute("ui.stylesheet", "node { fill-color: red; size: 20px; text-alignment: center; text-size: 14px; } edge { text-size: 14px; }");
        graph.addAttribute("ui.quality"); // 形质量优先
        graph.addAttribute("ui.antialias"); // 抗锯齿

        Viewer viewer = graph.display(); // 打开一个新窗口展示该图形
        viewer.enableAutoLayout(new SpringBox()); // 自动布局功能 更美观
    }

    // 查询桥接词
    public static List<String> findBridgeWords(Graph graph, String word1, String word2) {
        Node node1 = graph.getNode(word1);
        Node node2 = graph.getNode(word2);

        if (node1 == null || node2 == null) {
            return Collections.emptyList();  // 有单词不在图中，返回空列表
        }
        // 寻找桥接词
        List<String> bridgeWords = new ArrayList<>();

        for (Edge edge1 : node1.getLeavingEdgeSet()) {
            Node intermediate = edge1.getTargetNode();
            for (Edge edge2 : intermediate.getLeavingEdgeSet()) {
                if (edge2.getTargetNode().equals(node2)) {
                    bridgeWords.add(intermediate.getId());
                }
            }
        }

        return bridgeWords;
    }
    // 查询桥接词
    public static void queryBridgeWords(Scanner scanner, Graph graph) {
        System.out.print("请输入第一个单词：");
        String word1 = scanner.next().toLowerCase();
        System.out.print("请输入第二个单词： ");
        String word2 = scanner.next().toLowerCase();
        // 获取表示这两个单词的节点
        Node startNode = graph.getNode(word1);
        Node targetNode = graph.getNode(word2);
        // 检查节点是否存在
        if (startNode == null || targetNode == null) {
            System.out.println("起始单词或目标单词不在图中。");
            return;
        }
        // 查找桥接词：
        List<String> bridgeWords = findBridgeWords(graph, word1, word2);
        if (bridgeWords.isEmpty()) {
            System.out.println("没有找到桥接词。");
        } else {
            System.out.println("桥接词： " + String.join(", ", bridgeWords));
        }
    }
    // 产生新文本
    public static void generateNewText(Scanner scanner, Graph graph) {
        System.out.print("请输入新文本：");
        scanner.nextLine(); // 清理输入缓冲区
        String newText = scanner.nextLine().toLowerCase(); // 读取用户输入的一整行文本，并将其转换成小写

        String result = generateNewText(graph, newText);
        System.out.println("生成的新文本：");
        System.out.println(result);
    }
    // 找出两个单词之间的最短路径
    public static void calcShortestPath(Scanner scanner, Graph graph) {
        System.out.print("请输入起始单词：");
        String startWord = scanner.next().toLowerCase(); // 获取并转换为小写形式
        System.out.print("请输入目标单词：");
        String targetWord = scanner.next().toLowerCase();
        // 获取节点
        Node startNode = graph.getNode(startWord);
        Node targetNode = graph.getNode(targetWord);

        if (startNode == null || targetNode == null) {
            System.out.println("起始单词或目标单词不在图中。");
            return;
        }

        List<Node> shortestPath = dijkstraShortestPath(graph, startNode, targetNode);
        if (shortestPath.isEmpty()) {
            System.out.println("起始单词到目标单词之间无最短路径。");
            return;
        }
        // 输出最短路径
        System.out.println("最短路径：");
        for (Node node : shortestPath) {
            System.out.print(node.getId() + " ");
        }
        System.out.println();

        // 高亮最短路径
        highlightPath(graph, shortestPath);
    }


    public static String generateNewText(Graph graph, String newText) {
        String[] words = newText.split("\\s+"); // 使用空白字符分割输入的文本
        StringBuilder result = new StringBuilder();
        // 循环遍历每对相邻的单词
        for (int i = 0; i < words.length - 1; i++) {
            result.append(words[i]).append(" ");

            String wordA = words[i];
            String wordB = words[i + 1];

            List<String> bridgeWords = findBridgeWords(graph, wordA, wordB);
            if (!bridgeWords.isEmpty()) {
                Random random = new Random();
                String bridgeWord = bridgeWords.get(random.nextInt(bridgeWords.size()));
                result.append(bridgeWord).append(" ");
            }
        }
        result.append(words[words.length - 1]);

        return result.toString();
    }
    // 最短路径算法 迪杰斯特拉
    public static List<Node> dijkstraShortestPath(Graph graph, Node startNode, Node targetNode) {
        Map<Node, Integer> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (Node node : graph) {
            distances.put(node, Integer.valueOf(Integer.MAX_VALUE));
            previousNodes.put(node, null);
        }
        distances.put(startNode, Integer.valueOf(0));
        priorityQueue.add(startNode);

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();

            if (currentNode.equals(targetNode)) {
                break;
            }

            for (Edge edge : currentNode.getLeavingEdgeSet()) {
                Node neighbor = edge.getTargetNode();
                Integer edgeWeight = edge.getAttribute("weight");
                if (edgeWeight == null) {
                    continue;  // Skip if edge weight is null
                }

                int newDist = distances.get(currentNode) + edgeWeight;
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, Integer.valueOf(newDist));
                    previousNodes.put(neighbor, currentNode);
                    priorityQueue.add(neighbor);
                }
            }
        }

        List<Node> path = new ArrayList<>();
        for (Node at = targetNode; at != null; at = previousNodes.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        if (path.get(0).equals(startNode)) {
            return path;
        } else {
            return Collections.emptyList();
        }
    }
    // 高亮
    public static void highlightPath(Graph graph, List<Node> path) {
        for (Edge edge : graph.getEdgeSet()) {
            edge.removeAttribute("ui.style");
        }

        for (int i = 0; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            Edge edge = graph.getEdge(from.getId() + "->" + to.getId());
            if (edge != null) {
                edge.addAttribute("ui.style", "fill-color: green;");
            }
        }
    }
    // 随机游走
    public static void randomWalk(Graph graph) {
        List<Node> walkPath = new ArrayList<>();
        Set<Edge> visitedEdges = new HashSet<>();
        Random random = new Random();

        List<Node> nodes = new ArrayList<>();
        for (Node node : graph) {
            nodes.add(node);
        }

        Node currentNode = nodes.get(random.nextInt(nodes.size()));
        walkPath.add(currentNode);
        System.out.println("开始: " + currentNode.getId());
        while (true) {
            System.out.print("按下Enter 继续, 或输入'stop'停止: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("stop")) {
                break;
            }

            List<Edge> leavingEdges = new ArrayList<>(currentNode.getLeavingEdgeSet());
            if (leavingEdges.isEmpty()) {
                System.out.println("No further nodes to walk to from " + currentNode.getId());
                break;
            }

            Edge randomEdge = leavingEdges.get(random.nextInt(leavingEdges.size()));
            if (visitedEdges.contains(randomEdge)) {
                System.out.println("Encountered a previously visited edge. Stopping walk.");
                break;
            }

            visitedEdges.add(randomEdge);
            currentNode = randomEdge.getTargetNode();
            walkPath.add(currentNode);


            System.out.println("走到: " + currentNode.getId());
        }

        System.out.println("随机游走路径:");
        for (Node node : walkPath) {
            System.out.print(node.getId() + " ");
        }
        System.out.println();

        try (FileWriter writer = new FileWriter("random_walk.txt")) {
            for (Node node : walkPath) {
                writer.write(node.getId() + " ");
            }
            System.out.println("路径保存到random_walk.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}