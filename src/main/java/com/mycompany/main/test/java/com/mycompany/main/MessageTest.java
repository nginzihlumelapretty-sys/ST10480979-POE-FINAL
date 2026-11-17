package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTest {

    private static final String CELL1 = "+27718693002";
    private static final String CELL2 = "+27834557896";

    @BeforeEach
    public void setUp() {
        Message.resetAll();
    }

    @Test
    public void messageLengthUnder250() {
        assertEquals("Message ready to send.", Message.checkMessageLength("Hi Mike"));
    }

    @Test
    public void messageLengthOver250() {
        String longText = "a".repeat(260);
        assertEquals("Message exceeds 250 characters by 10, please reduce size.",
                Message.checkMessageLength(longText));
    }

    @Test
    public void recipientCellCorrect() {
        Message m = new Message(0, CELL1, "Hello");
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    public void recipientCellIncorrect() {
        Message m = new Message(0, "0718693002", "Hello");
        assertTrue(m.checkRecipientCell().startsWith("Cell phone number is incorrectly formatted"));
    }

    @Test
    public void messageIdIsTenDigits() {
        Message m = new Message(0, CELL1, "Hello");
        assertTrue(m.checkMessageID());
        assertEquals(10, m.getMessageID().length());
    }

    @Test
    public void messageHashIsCorrect() {
        Message m = new Message(0, CELL1, "Hi Mike, can you join us for dinner tonight");
        String expected = m.getMessageID().substring(0, 2) + ":0:HITONIGHT";
        assertEquals(expected, m.getMessageHash());
    }

    @Test
    public void sendMessageAddsToSentArrays() {
        Message m = new Message(0, CELL1, "Did you get the cake?");
        assertEquals("Message successfully sent.", m.sentMessage(1));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void disregardedMessageIsNotSent() {
        Message m = new Message(0, CELL1, "Yohoooo, I am at your gate.");
        assertEquals("Message disregarded.", m.sentMessage(2));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void storedMessageIsWrittenToJsonAndRead() {
        Message m = new Message(0, CELL2, "Where are you? You are late!");
        assertEquals("Message successfully stored.", m.sentMessage(3));
        String output = Message.readStoredMessages();
        assertTrue(output.contains("Where are you? You are late!"));
        assertTrue(output.contains(m.getMessageID()));
    }

    @Test
    public void readStoredMessagesWhenEmpty() {
        assertEquals("There are no stored messages in the JSON file.", Message.readStoredMessages());
    }

    @Test
    public void longestMessageIsFound() {
        new Message(0, CELL1, "Short one").sentMessage(1);
        new Message(1, CELL2, "This is clearly the longest message of them all").sentMessage(1);
        new Message(2, CELL1, "Medium message").sentMessage(1);
        assertTrue(Message.displayLongestMessage()
                .contains("This is clearly the longest message of them all"));
    }

    @Test
    public void longestMessageWhenNoneSent() {
        assertEquals("No messages have been sent.", Message.displayLongestMessage());
    }

    @Test
    public void searchByRecipientFindsMessages() {
        new Message(0, CELL1, "First for one").sentMessage(1);
        new Message(1, CELL2, "For two").sentMessage(1);
        new Message(2, CELL1, "Second for one").sentMessage(1);
        String result = Message.searchByRecipient(CELL1);
        assertTrue(result.contains("First for one"));
        assertTrue(result.contains("Second for one"));
        assertFalse(result.contains("For two"));
    }

    @Test
    public void searchByRecipientNotFound() {
        assertEquals("No messages found for " + CELL1 + ".", Message.searchByRecipient(CELL1));
    }

    @Test
    public void deleteByHashRemovesMessage() {
        Message m = new Message(0, CELL1, "Delete me please");
        m.sentMessage(1);
        assertEquals("Message \"Delete me please\" successfully deleted.",
                Message.deleteByHash(m.getMessageHash()));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void deleteByHashNotFound() {
        assertEquals("Message hash not found.", Message.deleteByHash("00:0:NOPE"));
    }

    @Test
    public void reportShowsAllDetails() {
        Message m = new Message(0, CELL1, "Report test message");
        m.sentMessage(1);
        String report = Message.displayMessageReport();
        assertTrue(report.contains(m.getMessageID()));
        assertTrue(report.contains(m.getMessageHash()));
        assertTrue(report.contains(CELL1));
        assertTrue(report.contains("Report test message"));
    }

    @Test
    public void reportWhenNoMessages() {
        assertEquals("No messages have been sent.", Message.displayMessageReport());
    }
}
