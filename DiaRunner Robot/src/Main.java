public class Main {
    public static void main(String[] args) {
        ListenSocket server = new ListenSocket();
        while (true)
            server.openConnection();
    }
}
