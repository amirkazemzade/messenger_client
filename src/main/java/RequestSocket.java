import models.Session;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RequestSocket {

    // address and port of Receiver Socket
    private final byte[] serverAddress = {127, 0, 0, 1};
    private final int serverPort = 40000;

    public String createUser(String username, String password) throws MyServerException {
        try {
            Socket socket = connect();

            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            output.writeUTF(String.format("Make -Option <user:%s> -Option <pass:%s> \r\n", username, password));
            output.flush();
            String response = input.readUTF();
            String[] messageArray = messageToArray(response);
            socket.close();
            if (messageArray[0].matches("User Accepted")) {
                return extractId(messageArray);
            } else if (messageArray[0].matches("User Not Accepted")) {
                String reason = extractReason(messageArray);
                throw new MyServerException(reason);
            } else {
                System.out.println(response);
                throw new MyServerException("Something went wrong");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    public Session login(String username, String password) throws MyServerException {
        try {
            Socket socket = connect();

            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            output.writeUTF(String.format("Connect -Option <user:%s> -Option <pass:%s> \r\n", username, password));
            output.flush();
            String response = input.readUTF();
            String[] messageArray = messageToArray(response);
            socket.close();
            if (messageArray[0].matches("Connected")) {
                return extractSession(messageArray);
            } else if (messageArray[0].matches("ERROR")) {
                String reason = extractReason(messageArray);
                throw new MyServerException(reason);
            } else {
                System.out.println(response);
                throw new MyServerException("Something went wrong");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private Socket connect() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(InetAddress.getByAddress(serverAddress), serverPort));
        System.out.println("connected to server");
        return socket;
    }

    private String[] messageToArray(String message) {
        return message.split(" -Option ");
    }

    private String extractId(String[] messageArray) {
        String id = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("<|>|:");
            if (option[1].matches("id")) id = option[2];
        }
        return id;
    }

    private Session extractSession(String[] messageArray) {
        String id = null;
        String sid = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("<|>|:");
            if (option[1].matches("id")) id = option[2];
            else if (option[1].matches("SID")) sid = option[2];
        }
        return new Session(id, sid);
    }

    private String extractReason(String[] messageArray) {
        String reason = null;
        for (int i = 1; i < messageArray.length; i++) {
            String[] option = messageArray[i].split("<|>|:");
            if (option[1].matches("reason")) reason = option[2];
        }
        return reason;
    }
}
