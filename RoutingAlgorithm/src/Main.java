public class Main {
    public static void main(String[] args) {
        int vexnum = 6;
        int edgenum = 8;
        Graph g = new Graph(vexnum, edgenum);
        g.createGraph();
        g.printGraph();

        int startId = 1;
        int endId = 5;

        // 调用 Dijkstra 算法
        utils.Dijkstra(g, startId);
        g.print_path_Dijkstra(endId);

        // 调用 Floyd 算法
        utils.Floyd(g);
        g.print_path_Floyd(startId, endId);
    }
}

// 拓扑图信息
// 1 6 100
// 1 5 30
// 1 3 10
// 2 3 5
// 3 4 50
// 4 6 10
// 5 4 20
// 5 6 60