import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class PMSenderSocket {

    byte[] serverAddress = {127, 0, 0, 1};
    int serverPort = 41000;

    DataInputStream in;
    DataOutputStream out;

    public void connectToAnotherUser(String destinationUsername, String sid) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("PM ConnectTo -Option <to:%s> -Option <SID:%s>", destinationUsername, sid));
            out.flush();
            String message = in.readUTF();
            handleConnectToAnotherUserResponse(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private void handleConnectToAnotherUserResponse(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("SENT PM")) return;
        if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    public void sendPM(String destinationUsername, String sid, String message) throws MyServerException {
        try {
            Socket serverSocket = getServerSocket();
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format(
                    "PM -Option <message_len:%d> -Option <to:%s> -Option <message_body:%s> -Option <SID:%s>",
                    message.length(), destinationUsername, message, sid)
            );
            out.flush();
            String response = in.readUTF();
            handleConnectToAnotherUserResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private Socket getServerSocket() throws IOException {
        return new Socket(InetAddress.getByAddress(serverAddress), serverPort);
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
}
