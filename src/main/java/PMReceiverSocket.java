import models.Message;
import models.PMResponse;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

public class PMReceiverSocket {

    byte[] serverAddress = {127, 0, 0, 1};
    int serverPort = 41000;

    DataInputStream in;
    DataOutputStream out;

    public ArrayList<Message> getAllMessages(String sid) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("PM GetAll -Option <SID:%s>", sid));
            out.flush();
            String message = in.readUTF();
            return handleAllMessagesResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    public ArrayList<Message> getAllMessagesFrom(String sid, String desId) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("PM GetAllFrom -Option <SID:%s> -Option <from:%s>", sid, desId));
            out.flush();
            String message = in.readUTF();
            return handleAllMessagesResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private ArrayList<Message> handleAllMessagesResponse(String message) throws MyServerException {
        String[] messageLines = message.split("\r\n");
        String[] messageArray = messageLines[0].split(" -Option ");
        if (messageArray[0].matches("PM All")) {
            return extractAllMessages(messageLines);
        } else if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    private ArrayList<Message> extractAllMessages(String[] messageLines) throws MyServerException {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 1; i < messageLines.length; i++) {
            String[] messageArray = messageLines[i].split(" -Option ");
            messages.add(extractPMResponse(messageArray));
        }
        return messages;
    }

    public Message receive(String sid) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("PM Connect -Option <SID:%s>", sid));
            out.flush();
            String message = in.readUTF();
            return handleConnectResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private Message handleConnectResponse(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("PM")) return extractPMResponse(messageArray);
        else if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    private String extractReason(String[] messageArray) {
        String reason = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("[<>:]");
            if (option[1].matches("reason")) reason = option[2];
        }
        return reason;
    }

    private String[] messageToArray(String message) {
        return message.split(" -Option ");
    }

    private PMResponse extractPMResponse(String[] messageArray) throws MyServerException {
        String senderId = null;
        String receiverId = null;
        String lengthString = null;
        String message = null;
        String sendTime = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("[<>:]");
            if (option[1].matches("from")) senderId = option[2];
            else if (option[1].matches("to")) receiverId = option[2];
            else if (option[1].matches("message_len")) lengthString = option[2];
            else if (option[1].matches("message_body")) message = option[2];
            else if (option[1].matches("send_time")) sendTime = option[2];
        }
        if (sendTime == null || lengthString == null || senderId == null || receiverId == null || message == null) throw new MyServerException("invalid response");

        int length;
        try {
            length = Integer.parseInt(lengthString);
        } catch (NumberFormatException e){
            e.printStackTrace();
            throw new MyServerException("invalid response");
        }

        return new PMResponse(new Timestamp(Long.parseLong(sendTime)), senderId, receiverId, length, message);
    }
}
