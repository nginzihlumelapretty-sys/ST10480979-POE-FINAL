package com.mycompany.main;

import java.util.Scanner;

public class Main {

    static Scanner input = new Scanner(System.in);

    // Registered details (saved after a successful registration)
    static String registeredUsername;
    static String registeredPassword;
    static String registeredCellphone;

    // ---------------- PART 1: registration and login ----------------

    // Username must be exactly 5 characters and contain an underscore
    public static boolean checkUserName(String username) {
        return username != null && username.length() == 5 && username.contains("_");
    }

    // Password: at least 8 characters, upper, lower, number and special character
    public static boolean checkPasswordComplexity(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    // Cellphone must start with +27 followed by exactly 9 digits
    public static boolean checkCellphoneNumber(String number) {
        return number != null && number.matches("^\\+27[0-9]{9}$");
    }

    public static void registerUser() {
        while (true) {
            System.out.print("Enter your username: ");
            String username = input.nextLine();
            System.out.print("Enter your password: ");
            String password = input.nextLine();
            System.out.print("Enter your cellphone number (+27...): ");
            String cellphone = input.nextLine();

            boolean usernameCorrect = checkUserName(username);
            boolean passwordCorrect = checkPasswordComplexity(password);
            boolean phoneCorrect = checkCellphoneNumber(cellphone);

            if (usernameCorrect && passwordCorrect && phoneCorrect) {
                registeredUsername = username;
                registeredPassword = password;
                registeredCellphone = cellphone;
                System.out.println("User registered successfully.");
                return;
            }
            if (!usernameCorrect) {
                System.out.println("Username is not correctly formatted; it must be 5 characters and contain an underscore.");
            }
            if (!passwordCorrect) {
                System.out.println("Password is not correctly formatted; it needs 8+ characters, a capital letter, a number and a special character.");
            }
            if (!phoneCorrect) {
                System.out.println("Cellphone number is incorrectly formatted or does not contain an international code.");
            }
            System.out.println("Registration failed. Please try again.\n");
        }
    }

    public static boolean loginUser(String username, String password) {
        return username != null && password != null
                && username.equals(registeredUsername)
                && password.equals(registeredPassword);
    }

    public static String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome, it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    // ---------------- PART 2: QuickChat ----------------

    // Welcome message, numeric menu and the main while loop
    private static void quickChat() {
        System.out.println("\nWelcome to QuickChat.");
        int max = readInt("How many messages do you wish to enter? ", 1, Integer.MAX_VALUE);
        boolean messagesEntered = false;
        boolean running = true;

        // Runs until the user selects 7 (Quit)
        while (running) {
            System.out.println("\n1) Send Messages"
                    + "\n2) Display sent messages report"
                    + "\n3) Display longest sent message"
                    + "\n4) Search messages by recipient"
                    + "\n5) Delete a message by message hash"
                    + "\n6) Display stored messages (JSON file)"
                    + "\n7) Quit");
            int option = readInt("Choose an option: ", 1, 7);

            switch (option) {
                case 1:
                    if (messagesEntered) {
                        System.out.println("You have already entered all " + max + " messages.");
                        break;
                    }
                    // For loop: the user enters only the assigned number of messages.
                    // The loop counter (i) is the message number used in the message hash.
                    for (int i = 0; i < max; i++) {
                        System.out.println("\n--- Message " + (i + 1) + " of " + max + " ---");
                        // Repeat the same message until valid details are entered
                        while (!captureMessage(i)) {
                            System.out.println("Please try again.");
                        }
                    }
                    messagesEntered = true;
                    break;
                case 2:
                    System.out.println(Message.displayMessageReport());
                    break;
                case 3:
                    System.out.println(Message.displayLongestMessage());
                    break;
                case 4:
                    System.out.print("Enter the recipient cell number to search for: ");
                    System.out.println(Message.searchByRecipient(input.nextLine().trim()));
                    break;
                case 5:
                    System.out.print("Enter the message hash to delete: ");
                    System.out.println(Message.deleteByHash(input.nextLine()));
                    break;
                case 6:
                    System.out.println(Message.readStoredMessages());
                    break;
                default:
                    running = false;
            }
        }
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
    }

    private static boolean captureMessage(int number) {
        System.out.print("Recipient cell number (e.g. +27718693002): ");
        String recipient = input.nextLine().trim();

        System.out.print("Message (max 250 characters): ");
        String text = input.nextLine();
        if (text.length() > 250) {
            System.out.println("Please enter a message of less than 250 characters.");
            System.out.println(Message.checkMessageLength(text));
            return false;
        }
        if (text.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            return false;
        }

        Message msg = new Message(number, recipient, text);
        String cellResult = msg.checkRecipientCell();
        System.out.println(cellResult);
        if (!cellResult.startsWith("Cell phone number successfully")) {
            return false;
        }
        if (!msg.checkMessageID()) {
            System.out.println("Message ID is invalid.");
            return false;
        }
        System.out.println("Message ID generated: " + msg.getMessageID());
        System.out.println(Message.checkMessageLength(text));

        System.out.println("\n1) Send Message\n2) Disregard Message\n3) Store Message to send later");
        int choice = readInt("Choose an option: ", 1, 3);
        System.out.println(msg.sentMessage(choice));
        if (choice == 1) {
            System.out.println("\n" + msg.getDetails());
        }
        return true;
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(input.nextLine().trim());
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignored) { }
            System.out.println("Invalid input, please try again.");
        }
    }

    // ---------------- Program start ----------------

    public static void main(String[] args) {
        System.out.println("=== Registration ===");
        registerUser();

        System.out.println("\n=== Login ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter your username: ");
            String u = input.nextLine();
            System.out.print("Enter your password: ");
            String p = input.nextLine();
            loggedIn = loginUser(u, p);
            System.out.println(returnLoginStatus(loggedIn));
        }

        quickChat();
    }
}
