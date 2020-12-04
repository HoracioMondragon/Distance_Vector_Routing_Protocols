import java.io.*;
import java.net.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

//
public class Server {
    private int num_server;
    private int localServerID;
    private int num_neighbors;
    private String[][] ip_addr;
    private int[][] conn;
    private int routing_update_interval;
    private String[][] matrix;
    private Socket localSocket;
    private DataOutputStream dOut;
    private DataInputStream dIn;
    public Server(int num_server, int num_neighbors, String[][] ip_addr, int[][] conn, int routing_update_interval) {
        this.num_server = num_server;
        this.num_neighbors = num_neighbors;
        this.ip_addr = ip_addr;
        this.conn = conn;
        this.routing_update_interval = routing_update_interval;
        //creating socket
     
        System.out.println();
        try {//needs to get socket from file
            localSocket = new Socket(ip_addr[0][1], Integer.parseInt(ip_addr[0][2]));
            // InetAddress myIP=InetAddress.getLocalHost();
            //this line is for debugging
            // localSocket = new Socket();
            
            //creating in/out streams to send and recieve messages
            dOut = new DataOutputStream(localSocket.getOutputStream());
            dIn = new DataInputStream(localSocket.getInputStream());
            localSocket.connect(localSocket.getRemoteSocketAddress());
            
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println(e+" error here 1");
        }
        //////////creating timer for period table updates//////
        class TableUpdate extends TimerTask {
            @Override
            public void run() {
                //for testing timer
                System.out.println("update sent");
                try{
                    dOut.writeUTF("message sent");
                    dOut.flush();
                }
                catch (Exception e) {
            //TODO: handle exception
            System.out.println(e+" error here 2");
        }
            }
        }
        new Timer().schedule(new TableUpdate(),0,routing_update_interval);
        

        ///////////////////////////////////////////////////////

        ///////creating recieving function for table updates//////////////////////
        Thread serverConnect = new Thread(new Runnable() {
            public void run() {
                try{
                    System.out.println(dIn.readUTF());
                    }
                    catch (Exception e) {
                        //TODO: handle exception
                        System.out.println(e+" error here 3");
                    }
            }
        });
        serverConnect.run();


        ///////////////////////////////////////////////////////////////////////////
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
    public void crashed(){
        for(int i=0;i<this.num_server;i++){
            for(int j=0;j<this.num_server;j++){
                if(i!=j){
                matrix[i][j]="inf";
                }
            }
        }
    }
    public void update_link(int i, int j, String cost) {
        this.matrix[i-1][j-1] = "" + cost;
        this.matrix[j-1][i-1]= ""+cost;
    }

    public void print_matrix() throws Exception {
        //may need to be reformatted after changes to include unique ID's for server
        //gets list of costs of neighbor id's in-order smallest to largest
        for(int i=0;i<this.num_server;i++){
            for(int j=1;j<=this.num_server;j++){//source server id
                System.out.print(("Source Server ID:"+(i+1)+"  Next Hop Server ID:"));//source server id
                System.out.print(j+"  Cost of Path:");//neighbor server id
                System.out.print(matrix[i][j-1]);//cost of hop
                System.out.println();
            }
        }
        // int i = 0;
        // for (String[] row : matrix) {
        //     i++;
        //     printRow(i, row);
        // }
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
        //do not close connection to server ID
        //change topology/matrix to show it as closed/unavilable
        update_link(localServerID, Integer.parseInt(string), "inf");
    }

    private void crash() {
        //close all active connections

        //update matrix to show all connections closed
        crashed();
    }

    private void get_packets() {
        //count number of recieved table updates from other servers
        //reset to 0 whenever get_packets() is called
        //reset counter at the end here
    }

    private void step() {
        //
    }

}
//