import validations.InputProcessor;
import validations.impl.InputProcessorImpl;

import java.util.Scanner;

public class MySearchEngine {
    public static void start() {
        Scanner scanner = new Scanner(System.in);
        String continueCommand = "Y";
        String input;
        InputProcessor inputProcessor = new InputProcessorImpl();

        do {
            System.out.println("Enter command:");
            input = scanner.nextLine();
            inputProcessor.processCommand(input);
            System.out.println("Continue? [Y/N]");
            continueCommand = scanner.nextLine();
        } while(continueCommand.equalsIgnoreCase("Y"));

        scanner.close();
    }
}