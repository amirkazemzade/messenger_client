import models.GMResponse;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

// a class for receiving group messages from server
public class GMReceiverSocket {

    byte[] serverAddress = {127, 0, 0, 1};
    int serverPort = 50000;

    DataInputStream in;
    DataOutputStream out;

    // waits for server to send a group message to it
    public GMResponse receive(String sid) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("GM Connect -Option <SID:%s>", sid));
            out.flush();
            String message = in.readUTF();
            return handleConnectResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    // handles response of server after sending a group message receive request
    // if request succeeded returns a GMResponse and throws a MyServerException if an error occurs
    private GMResponse handleConnectResponse(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("GM")) return extractGMResponse(messageArray);
        else if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    // gets list of all group messages of user from server
    public ArrayList<GMResponse> getAllMessages(String sid) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("GM GetAll -Option <SID:%s>", sid));
            out.flush();
            String message = in.readUTF();
            return handleAllMessagesResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    // handles response of server after request of getting all group messages
    // if request succeeded returns a list of GMResponse and throws a MyServerException if an error occurs
    private ArrayList<GMResponse> handleAllMessagesResponse(String message) throws MyServerException {
        String[] messageLines = message.split("\r\n");
        String[] messageArray = messageLines[0].split(" -Option ");
        if (messageArray[0].matches("GM All")) {
            return extractAllMessages(messageLines);
        } else if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    // gets list of group messages of the group with given groupId
    public ArrayList<GMResponse> getAllMessagesFrom(String sid, String groupId) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("GM GetAllFrom -Option <SID:%s> -Option <gname:%s>", sid, groupId));
            out.flush();
            String message = in.readUTF();
            return handleAllMessagesResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    // extracts a list of messages from given string array of response lines
    private ArrayList<GMResponse> extractAllMessages(String[] messageLines) throws MyServerException {
        ArrayList<GMResponse> messages = new ArrayList<>();
        for (int i = 1; i < messageLines.length; i++) {
            String[] messageArray = messageLines[i].split(" -Option ");
            messages.add(extractGMResponse(messageArray));
        }
        return messages;
    }

    // extracts the reason of the error from response
    private String extractReason(String[] messageArray) {
        String reason = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("[<>:]");
            if (option[1].matches("reason")) reason = option[2];
        }
        return reason;
    }

    // splits given message to an array that contains request and its options
    private String[] messageToArray(String message) {
        return message.split(" -Option ");
    }

    // extract a group message from the string array of response
    private GMResponse extractGMResponse(String[] messageArray) throws MyServerException {
        String senderId = null;
        String groupId = null;
        String lengthString = null;
        String message = null;
        String sendTime = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("[<>:]");
            if (option[1].matches("from")) senderId = option[2];
            else if (option[1].matches("to")) groupId = option[2];
            else if (option[1].matches("message_len")) lengthString = option[2];
            else if (option[1].matches("message_body")) message = option[2];
            else if (option[1].matches("send_time")) sendTime = option[2];
        }
        if (sendTime == null || lengthString == null || senderId == null || groupId == null || message == null)
            throw new MyServerException("invalid response");

        int length;
        try {
            length = Integer.parseInt(lengthString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new MyServerException("invalid response");
        }

        return new GMResponse(groupId, senderId, length, message, new Timestamp(Long.parseLong(sendTime)));
    }

    // gets list of users of the group with given group id
    public ArrayList<String> getGroupUsers(String groupId) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("Users -Option <gname:%s>", groupId));
            out.flush();
            String message = in.readUTF();
            return handleGroupUsersResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    // handles response of getting group users
    private ArrayList<String> handleGroupUsersResponse(String message) throws MyServerException {
        String[] messageLines = message.split("\r\n");
        String[] messageArray = messageLines[0].split(" -Option ");
        if (messageArray[0].matches("USERS_LIST:")) {
            return extractAllUsers(messageLines);
        } else if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    // extracts users from given array of response lines
    private ArrayList<String> extractAllUsers(String[] messageLines) {
        ArrayList<String> users = new ArrayList<>();
        String[] messageArray = messageLines[1].split("[|]");
        for (String s : messageArray) {
            String user = s.replaceAll("[<>]", "");
            users.add(user);
        }
        return users;
    }

}

