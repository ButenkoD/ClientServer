package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.ServerInterface;

import java.util.Scanner;

public class ConsoleApplication {
    private static final String WELLCOME_TEXT = "Waiting for the next command...";
    private static boolean exitApplication = false;
    private static ServerInterface server;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (!exitApplication) {
            System.out.println(WELLCOME_TEXT);
            String line = scanner.nextLine();
            try {
                switch (line) {
                    case "start":
//                        server = new Server();
                        server = new AsynchronousServer();
                        new Thread(() -> server.listen()).start();
                        break;
                    case "exit":
                        if (server != null) {
                            server.stop();
                        }
                        exitApplication = true;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        scanner.close();
    }
}
