import java.io.*;
import java.net.*;
import java.util.*;

//
public class Server {
    private int num_server;
    private int num_neighbors;
    private String[][] ip_addr;
    private int[][] conn;
    private int routing_update_interval;
    private String[][] matrix;

    public Server(int num_server, int num_neighbors, String[][] ip_addr, int[][] conn, int routing_update_interval) {
        this.num_server = num_server;
        this.num_neighbors = num_neighbors;
        this.ip_addr = ip_addr;
        this.conn = conn;
        this.routing_update_interval = routing_update_interval;

        this.matrix = new String[this.num_server][this.num_server];

        for (int i = 0; i < this.num_server; i++) {
            Arrays.fill(matrix[i], "inf");
            for (int j = 0; j < this.num_server; j++)
                if (i == j)
                    this.matrix[i][j] = "0";
        }

        for (int i = 0; i < this.conn.length; i++)
            this.matrix[this.conn[i][0] - 1][this.conn[i][1] - 1] = "" + this.conn[i][2];
    }

    public static void printRow(int a, String[] row) {
        String ul = "_";
        if (a == 1) {
            for (int b = 0; b <= row.length; b++) {
                String pr = (b == 0) ? "  " + ul.repeat(6) : "" + b + ul.repeat(7);
                System.out.print(pr);
            }
            System.out.println();
        }

        System.out.print(a + "|\t");
        for (String i : row) {
            System.out.print(i);
            System.out.print("\t");
        }
        System.out.println();
    }

    public void update_link(int i, int j, String cost) {
        this.matrix[i-1][j-1] = "" + cost;
    }

    public void print_matrix() throws Exception {
        int i = 0;
        for (String[] row : matrix) {
            i++;
            printRow(i, row);
        }
    }

    public static void main(String args[]) throws Exception {
        switch (args.length) {
            case 0:
                System.out.println("Please enter a directory");
                return;
            case 4:
                break;
            default:
                System.out.println("Input arguements are wrong. Try again.");
                return;
        }

        String PATH = args[1];
        File directory = new File(PATH);

        if (!directory.exists()) {
            System.out.println("File doesn't exist!");
            return;
        }
        int routing_update_interval = Integer.parseInt(args[3]);

        Server server = null;
        try (BufferedReader br = new BufferedReader(new FileReader(directory))) {
            int num_server = Integer.parseInt(br.readLine());
            int num_neighbors = Integer.parseInt(br.readLine());
            String[][] ip_addr = new String[num_server][3];
            int[][] connections = new int[num_neighbors][3];

            for (int i = 0; i < num_server; i++)
                ip_addr[i] = br.readLine().split(" ");

            for (int i = 0; i < num_neighbors; i++)
                connections[i] = Arrays.stream(br.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();

            server = new Server(num_server, num_neighbors, ip_addr, connections, routing_update_interval);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //https://www.geeksforgeeks.org/bellman-ford-algorithm-dp-23/
        Scanner scanner = new Scanner(System.in);
        boolean status = true;
        while (status) {
            try {
                String[] input = scanner.nextLine().split(" ");
                int tem = input.length;
                switch (tem) {
                    case 1:
                        if (input[0].equals("step"))
                            server.step();

                        else if (input[0].equals("packets"))
                            server.get_packets();

                        else if (input[0].equals("display"))
                            server.print_matrix();

                        else if (input[0].equals("crash"))
                            server.crash();
                        else
                            throw new Exception();
                        break;
                    case 2:
                        server.disable(input[1]);
                    case 4:
                        server.update_link(Integer.parseInt(input[1]), Integer.parseInt(input[2]), input[3]);

                        break;
                    default:
                        throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Input wrong. Try again");
                status = true;
            }

        }
    }

    private void disable(String string) {

    }

    private void crash() {
    }

    private void get_packets() {
    }

    private void step() {
        //
    }

}
//