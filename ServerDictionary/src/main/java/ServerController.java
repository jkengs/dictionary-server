import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ServerController extends Thread{

    Server server;

    private final String EXIT_COMMAND = "exit";
    private final String STATUS_COMMAND = "status";
    private final String HELP_COMMAND = "help";
    private final String LOCATE_COMMAND = "locate";

    public ServerController(Server server) {
        this.server = server;
    }

    public void run() {
        Scanner inputReader = new Scanner(System.in);
        displayHelp();
        while(true) {
            if (inputReader.hasNext()) {
                String input = inputReader.next();
                switch (input.toLowerCase()) {

                    case EXIT_COMMAND:
                        server.exitProgram();
                        break;

                    case STATUS_COMMAND:
                        displayCurrentTime();
                        displayUpTime();
                        displayRequestNo();
                        break;

                    case LOCATE_COMMAND:
                        displayPath();
                        break;

                    case HELP_COMMAND:
                        displayHelp();
                        break;

                }
            }
        }
    }

    private void displayPath() {
        Path path = Paths.get(server.getDictionaryFilePath());
        Path absolutePath = path.toAbsolutePath();
        System.out.println("Dictionary FilePath: " +absolutePath);
    }

    private void displayCurrentTime() {
        Date date = new Date();
        System.out.printf("Current Date is %s\n", date);
    }

    private void displayUpTime() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        long upTime = runtime.getUptime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(upTime);
        long seconds = (TimeUnit.MILLISECONDS.toSeconds(upTime) % 60);
        System.out.printf("Uptime: %d minutes and %d seconds.\n",minutes,seconds);
    }

    private void displayRequestNo() {
        System.out.printf("Number of Requests: %d\n",server.getRequestNo());
    }

    private void displayHelp(){
        System.out.println("\nDictionary Server\n" +
                "\nCommands:\n" +
                "    help            print Help (this message) and exit\n" +
                "    status          print server status\n" +
                "    locate          print the absolute path of dictionary file\n" +
                "    exit            close and exit the server\n");
    }
}
