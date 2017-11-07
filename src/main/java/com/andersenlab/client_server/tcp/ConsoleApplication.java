package com.andersenlab.client_server.tcp;

import java.util.Scanner;

public class ConsoleApplication {
    private static final String WELLCOME_TEXT = "Waiting for the next command...";
    private static boolean exitApplication = false;
    private static Server server;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (!exitApplication) {
            System.out.println(WELLCOME_TEXT);
            String line = scanner.nextLine();
            try {
                switch (line) {
                    case "start":
                        server = new Server();
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
    }
}
