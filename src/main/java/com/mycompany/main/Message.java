package com.mycompany.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Message class for QuickChat.
 *
 * Attribution: the Gson read/write code below follows the examples in the
 * Gson User Guide (Google, https://github.com/google/gson/blob/main/UserGuide.md).
 */
public class Message {

    private static final String JSON_FILE = "storedMessages.json";
    private static final int MAX_LENGTH = 250;

    // ---------- Parallel arrays (the same index belongs to the same message) ----------
    // Sent messages
    private static final ArrayList<String> sentMessages = new ArrayList<>();
    private static final ArrayList<String> sentRecipients = new ArrayList<>();
    private static final ArrayList<String> sentIds = new ArrayList<>();
    private static final ArrayList<String> sentHashes = new ArrayList<>();

    // Stored messages (also written to the JSON file)
    private static final ArrayList<String> storedMessages = new ArrayList<>();
    private static final ArrayList<String> storedRecipients = new ArrayList<>();
    private static final ArrayList<String> storedIds = new ArrayList<>();
    private static final ArrayList<String> storedHashes = new ArrayList<>();

    // Disregarded messages
    private static final ArrayList<String> disregardedMessages = new ArrayList<>();

    // ---------- Instance fields ----------
    private final int messageNumber;
    private final String messageID;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    public Message(int messageNumber, String recipient, String messageText) {
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageID = generateMessageID();
        this.messageHash = createMessageHash();
    }

    // ---------- Validation ----------

    // Message ID is a 10 digit number
    private static String generateMessageID() {
        long number = (long) (new Random().nextDouble() * 10_000_000_000L);
        return String.format("%010d", number);
    }

    // The ID must be no more than 10 characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Recipient must start with +27 followed by 9 digits
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+27[0-9]{9}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    public static String checkMessageLength(String text) {
        if (text.length() <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds " + MAX_LENGTH + " characters by "
                + (text.length() - MAX_LENGTH) + ", please reduce size.";
    }

    // Hash: first two digits of ID : message number : FIRSTWORDLASTWORD (upper case)
    public String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");
        String first = words[0].replaceAll("[^A-Za-z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        return (messageID.substring(0, 2) + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    // ---------- Send / disregard / store ----------

    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(messageText);
                sentRecipients.add(recipient);
                sentIds.add(messageID);
                sentHashes.add(messageHash);
                return "Message successfully sent.";
            case 2:
                disregardedMessages.add(messageText);
                return "Message disregarded.";
            case 3:
                storedMessages.add(messageText);
                storedRecipients.add(recipient);
                storedIds.add(messageID);
                storedHashes.add(messageHash);
                return storeMessage();
            default:
                return "Invalid option.";
        }
    }

    // Adds this message to the JSON file
    public String storeMessage() {
        Message[] existing = loadFromJson();
        Message[] updated = new Message[existing.length + 1];
        System.arraycopy(existing, 0, updated, 0, existing.length);
        updated[existing.length] = this;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            gson.toJson(updated, writer);
        } catch (IOException e) {
            return "Message stored in memory, but the JSON file could not be written: " + e.getMessage();
        }
        return "Message successfully stored.";
    }

    // Reads the JSON file into an array (empty array if there is no file)
    private static Message[] loadFromJson() {
        File file = new File(JSON_FILE);
        if (!file.exists()) {
            return new Message[0];
        }
        try (FileReader reader = new FileReader(file)) {
            Message[] loaded = new Gson().fromJson(reader, Message[].class);
            return loaded == null ? new Message[0] : loaded;
        } catch (IOException e) {
            return new Message[0];
        }
    }

    // Read the JSON file into an array and display it to the user
    public static String readStoredMessages() {
        Message[] loaded = loadFromJson();
        if (loaded.length == 0) {
            return "There are no stored messages in the JSON file.";
        }
        StringBuilder sb = new StringBuilder("--- Stored messages (JSON file) ---\n");
        for (Message m : loaded) {
            sb.append(m.getDetails()).append("\n\n");
        }
        return sb.toString().trim();
    }

    // ---------- Reports and searching ----------

    // Display the sent messages report
    public static String displayMessageReport() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder sb = new StringBuilder("--- Sent messages report ---\n");
        for (int i = 0; i < sentMessages.size(); i++) {
            sb.append("Message ID: ").append(sentIds.get(i))
              .append("\nMessage Hash: ").append(sentHashes.get(i))
              .append("\nRecipient: ").append(sentRecipients.get(i))
              .append("\nMessage: ").append(sentMessages.get(i))
              .append("\n\n");
        }
        return sb.toString().trim();
    }

    // Search the sent array for the longest message
    public static String displayLongestMessage() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        int longest = 0;
        for (int i = 1; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).length() > sentMessages.get(longest).length()) {
                longest = i;
            }
        }
        return "Longest sent message (" + sentMessages.get(longest).length() + " characters):\n"
                + sentMessages.get(longest)
                + "\nRecipient: " + sentRecipients.get(longest)
                + "\nMessage ID: " + sentIds.get(longest);
    }

    // Search the arrays for all messages sent (or stored) for a recipient
    public static String searchByRecipient(String cell) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentRecipients.get(i).equals(cell)) {
                sb.append("[Sent] ").append(sentMessages.get(i)).append("\n");
            }
        }
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedRecipients.get(i).equals(cell)) {
                sb.append("[Stored] ").append(storedMessages.get(i)).append("\n");
            }
        }
        if (sb.length() == 0) {
            return "No messages found for " + cell + ".";
        }
        return "Messages for " + cell + ":\n" + sb.toString().trim();
    }

    // Delete a message using its message hash
    public static String deleteByHash(String hash) {
        String wanted = hash == null ? "" : hash.trim();
        for (int i = 0; i < sentHashes.size(); i++) {
            if (sentHashes.get(i).equalsIgnoreCase(wanted)) {
                String text = sentMessages.get(i);
                sentMessages.remove(i);
                sentRecipients.remove(i);
                sentIds.remove(i);
                sentHashes.remove(i);
                return "Message \"" + text + "\" successfully deleted.";
            }
        }
        for (int i = 0; i < storedHashes.size(); i++) {
            if (storedHashes.get(i).equalsIgnoreCase(wanted)) {
                String text = storedMessages.get(i);
                storedMessages.remove(i);
                storedRecipients.remove(i);
                storedIds.remove(i);
                storedHashes.remove(i);
                return "Message \"" + text + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // Clears all arrays and the JSON file (used by the unit tests)
    public static void resetAll() {
        sentMessages.clear();
        sentRecipients.clear();
        sentIds.clear();
        sentHashes.clear();
        storedMessages.clear();
        storedRecipients.clear();
        storedIds.clear();
        storedHashes.clear();
        disregardedMessages.clear();
        new File(JSON_FILE).delete();
    }

    // ---------- Getters ----------

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }
}
