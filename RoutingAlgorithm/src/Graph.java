import java.util.Scanner;

public class Graph {

    private int vexnum; // 图中的顶点数
    private int edgenum; // 图中的边数
    private int[][] matrix; // 邻接矩阵
    private Dis[] disList; // Dijkstra算法所用到的记录起点到每个点的距离数组

    private int[][] distance; // Floyd算法所用到的记录各个顶点之间距离的矩阵
    private int[][] path; // Floyd算法所用到的记录路径的数组

    Graph(int vexnum, int edgenum) {
        this.vexnum = vexnum;
        this.edgenum = edgenum;
        this.matrix = new int[vexnum][vexnum];
        this.disList = new Dis[vexnum];
        this.path = new int[vexnum][vexnum];
        this.distance = new int[vexnum][vexnum];

        // 初始化邻接矩阵
        for (int i = 0; i < vexnum; i++) {
            for (int j = 0; j < vexnum; j++) {
                matrix[i][j] = Integer.MAX_VALUE; // 初始化距离为最大
            }
            matrix[i][i] = 0;
        }
    }

    public void createGraph() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入每条边的起点,终点,以及权重");

        int start;
        int end;
        int weight;
        int edge_count = 0;
        while (edge_count != this.edgenum) {
            start = sc.nextInt();
            end = sc.nextInt();
            weight = sc.nextInt();
            edge_count++;

            matrix[start - 1][end - 1] = weight;
            
        }
        sc.close();
    }

    public void printGraph() {
        System.out.println("拓扑图的邻接矩阵为: ");
        for (int i = 0; i < vexnum; i++) {
            for (int j = 0; j < vexnum; j++) {
                if (matrix[i][j] == Integer.MAX_VALUE) {
                    System.out.print("∞" + " ");
                } else {
                    System.out.print(matrix[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    public void print_path_Dijkstra(int id) {
        System.out.println(disList[id-1].getPath());
        System.out.println(disList[id-1].getCost());
    }

    public void print_path_Floyd(int startId, int endId) {
        System.out.print("v" + Integer.valueOf(startId));
        int temp = path[startId-1][endId-1];
        while(temp != endId-1){
            System.out.print("--->" + "v" +Integer.valueOf(temp+1));
            temp = path[temp][endId-1];
        }
        System.out.println("--->" + "v" + Integer.valueOf(endId));
        System.out.println(distance[startId-1][endId-1]);
    }

    public int getVexnum(){
        return this.vexnum;
    }

    public int getedgenum(){
        return this.edgenum;
    }

    public int[][] getMatrix(){
        return this.matrix;
    }

    public Dis[] getDis(){
        return this.disList;
    }

    public int[][] getPath(){
        return this.path;
    }

    public int[][] getDistance(){
        return this.distance;
    }
}
