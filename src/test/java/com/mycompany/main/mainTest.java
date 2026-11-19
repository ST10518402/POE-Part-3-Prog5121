package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for the Chat Application classes in Main.java.
 *
 * Place this file in:
 * src/test/java/com/mycompany/main/MainTest.java
 */
class MainTest {

    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login("Mc", "Chopper");

        // Clear shared Message data before each test so tests stay independent.
        Message.resetCounters();
        Message.setSender("+27838968976");
    }

    // ---------------------------------------------------------
    // PART 1: LOGIN AND REGISTRATION TESTS
    // ---------------------------------------------------------

    @Test
    void validUsernameShouldBeAccepted() {
        assertTrue(login.checkUserName("mc_1"));
    }

    @Test
    void usernameWithoutUnderscoreShouldBeRejected() {
        assertFalse(login.checkUserName("mc123"));
    }

    @Test
    void usernameLongerThanFiveCharactersShouldBeRejected() {
        assertFalse(login.checkUserName("mc_user"));
    }

    @Test
    void validPasswordShouldBeAccepted() {
        assertTrue(login.checkPasswordComplexity("Password1!"));
    }

    @Test
    void passwordMissingRequiredCharactersShouldBeRejected() {
        assertFalse(login.checkPasswordComplexity("password"));
        assertFalse(login.checkPasswordComplexity("PASSWORD"));
        assertFalse(login.checkPasswordComplexity("Password!"));
        assertFalse(login.checkPasswordComplexity("Password1"));
    }

    @Test
    void validSouthAfricanInternationalCellNumberShouldBeAccepted() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    void cellNumberWithoutPlusPrefixShouldBeRejected() {
        assertFalse(login.checkCellPhoneNumber("27838968976"));
    }

    @Test
    void registrationShouldSucceedWhenAllDetailsAreValid() {
        String result = login.registerUser(
                "mc_1",
                "Password1!",
                "+27838968976"
        );

        assertTrue(result.contains("Username successfully captured."));
        assertTrue(result.contains("Password successfully captured."));
        assertTrue(result.contains("Cell phone number successfully added."));
        assertEquals("mc_1", login.getRegisteredUsername());
        assertEquals("+27838968976", login.getRegisteredCellPhone());
    }

    @Test
    void loginShouldSucceedWithRegisteredCredentials() {
        login.registerUser("mc_1", "Password1!", "+27838968976");

        assertTrue(login.loginUser("mc_1", "Password1!"));
        assertEquals(
                "Welcome Mc Chopper it is great to see you.",
                login.returnLoginStatus("mc_1", "Password1!")
        );
    }

    @Test
    void loginShouldFailWithIncorrectPassword() {
        login.registerUser("mc_1", "Password1!", "+27838968976");

        assertFalse(login.loginUser("mc_1", "WrongPass1!"));
        assertEquals(
                "Username or password incorrect, please try again.",
                login.returnLoginStatus("mc_1", "WrongPass1!")
        );
    }

    // ---------------------------------------------------------
    // PART 2: MESSAGE VALIDATION AND SENDING TESTS
    // ---------------------------------------------------------

    @Test
    void generatedMessageIdShouldContainTenDigits() {
        Message message = new Message("+27838968976", "Hello there");

        assertNotNull(message.getMessageID());
        assertEquals(10, message.getMessageID().length());
        assertTrue(message.getMessageID().matches("[0-9]{10}"));
    }

    @Test
    void validRecipientNumberShouldBeAccepted() {
        assertTrue(Message.isValidRecipient("+27838968976"));
    }

    @Test
    void invalidRecipientNumberShouldBeRejected() {
        assertFalse(Message.isValidRecipient("0838968976"));
        assertFalse(Message.isValidRecipient("+27ABC8968976"));
    }

    @Test
    void messageOf250CharactersShouldBeAccepted() {
        String messageText = "a".repeat(250);

        assertEquals(
                "Message ready to send.",
                Message.validateMessageText(messageText)
        );
    }

    @Test
    void messageLongerThan250CharactersShouldBeRejected() {
        String messageText = "a".repeat(251);

        assertEquals(
                "Message exceeds 250 characters by 1; please reduce the size.",
                Message.validateMessageText(messageText)
        );
    }

    @Test
    void sendOptionShouldAddMessageToSentList() {
        Message message = new Message("0012345678", "+27838968976", "Hello there");

        assertEquals("Message successfully sent.", message.sentMessage(1));
        assertTrue(Message.sentMessageList.contains("Hello there"));
        assertEquals(1, Message.returnTotalMessagessSent());
    }

    @Test
    void disregardOptionShouldAddMessageToDisregardedList() {
        Message message = new Message("0012345678", "+27838968976", "Not sending");

        assertEquals("Press 0 to delete the message", message.sentMessage(2));
        assertTrue(Message.disregardedMessageList.contains("Not sending"));
        assertEquals(0, Message.returnTotalMessagessSent());
    }

    @Test
    void storeOptionShouldAddMessageToStoredList() {
        Message message = new Message("0012345678", "+27838968976", "Save this message");

        assertEquals("Message successfully stored.", message.sentMessage(3));
        assertTrue(Message.storedMessageList.contains("Save this message"));
    }

    @Test
    void invalidMessageOptionShouldReturnError() {
        Message message = new Message("0012345678", "+27838968976", "Hello");

        assertEquals(
                "Invalid option. Please select 1, 2, or 3.",
                message.sentMessage(9)
        );
    }

    @Test
    void messageHashShouldUseUppercaseAndMessageNumber() {
        Message message = new Message("0012345678", "+27838968976", "hello world");

        assertEquals("00:1:HELLOWORLD", message.createMessageHash());
    }

    // ---------------------------------------------------------
    // PART 3: STORED MESSAGE TESTS
    // ---------------------------------------------------------

    @Test
    void searchByMessageIdShouldReturnRecipientAndMessage() {
        Message message = new Message("1234567890", "+27838968976", "Find this message");
        message.sentMessage(3);

        assertEquals(
                "Recipient: +27838968976, Message: Find this message",
                Message.searchByMessageId("1234567890")
        );
    }

    @Test
    void searchByUnknownMessageIdShouldReturnNotFoundMessage() {
        assertEquals(
                "Message ID not found.",
                Message.searchByMessageId("9999999999")
        );
    }

    @Test
    void searchByRecipientShouldReturnOnlyStoredMessages() {
        Message first = new Message("1111111111", "+27838968976", "Stored message");
        Message second = new Message("2222222222", "+27838968976", "Sent message");

        first.sentMessage(3);
        second.sentMessage(1);

        assertEquals(
                java.util.List.of("Stored message"),
                Message.searchByRecipient("+27838968976")
        );
    }

    @Test
    void longestStoredMessageShouldBeReturned() {
        new Message("1111111111", "+27838968976", "Short").sentMessage(3);
        new Message("2222222222", "+27838968976", "This is the longest message").sentMessage(3);

        assertEquals(
                "This is the longest message",
                Message.getLongestStoredMessage()
        );
    }

    @Test
    void senderAndRecipientDisplayShouldIncludeStoredMessageDetails() {
        new Message("1111111111", "+27838968976", "Stored message").sentMessage(3);

        String result = Message.displaySenderAndRecipient();

        assertTrue(result.contains("Sender: +27838968976"));
        assertTrue(result.contains("Recipient: +27838968976"));
    }

    @Test
    void deleteByHashShouldRemoveStoredMessageFromLists() {
        Message message = new Message("1234567890", "+27838968976", "Delete this message");
        message.sentMessage(3);
        String hash = message.getMessageHash();

        String result = Message.deleteByHash(hash);

        assertEquals(
                "Message: \"Delete this message\" successfully deleted.",
                result
        );
        assertTrue(Message.storedMessageList.isEmpty());
        assertTrue(Message.messageIdList.isEmpty());
        assertTrue(Message.messageHashList.isEmpty());
    }

    @Test
    void storedReportShouldContainStoredMessageInformation() {
        Message message = new Message("1234567890", "+27838968976", "Report this message");
        message.sentMessage(3);

        String report = Message.getStoredReport();

        assertTrue(report.contains("=== Stored Messages Report ==="));
        assertTrue(report.contains(message.getMessageHash()));
        assertTrue(report.contains("1234567890"));
        assertTrue(report.contains("Report this message"));
    }
}

