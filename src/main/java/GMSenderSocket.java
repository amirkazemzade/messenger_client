import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class GMSenderSocket {

    byte[] serverAddress = {127, 0, 0, 1};
    int serverPort = 50000;

    DataInputStream in;
    DataOutputStream out;

    public void joinGroup(String groupId, String sid) throws MyServerException {
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("Group -Option <SID:%s> -Option <gname:%s> ", sid, groupId));
            out.flush();
            String message = in.readUTF();
            handleJoinGroup(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private void handleJoinGroup(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("JoinedGroup")) return;
        if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    public void leaveGroup(String groupId, String sid) throws MyServerException{
        try {
            Socket serverSocket = new Socket(InetAddress.getByAddress(serverAddress), serverPort);
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format("End -Option <SID:%s> -Option <gname:%s> ", sid, groupId));
            out.flush();
            String message = in.readUTF();
            handleLeaveGroup(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private void handleLeaveGroup(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("LeftGroup")) return;
        if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    public void sendGM(String groupId, String sid, String message) throws MyServerException {
        try {
            Socket serverSocket = getServerSocket();
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format(
                    "GM -Option <to:%s> -Option <message_len:%d> -Option <message_body:%s> -Option <SID:%s>",
                    groupId, message.getBytes().length, message, sid)
            );
            out.flush();
            String response = in.readUTF();
            handleSendGMResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }
    }

    private void handleSendGMResponse(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("SENT PM")) return;
        if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
    }

    public void createGroup(String groupId, String sid) throws MyServerException {
        try {
            Socket serverSocket = getServerSocket();
            in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.writeUTF(String.format(
                            "CreateGroup -Option <gname:%s> -Option -Option <SID:%s>",
                            groupId, sid
                    )
            );
            out.flush();
            String response = in.readUTF();
            handleCreateGroupResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyServerException("Something went wrong");
        }

    }

    private void handleCreateGroupResponse(String message) throws MyServerException {
        String[] messageArray = messageToArray(message);
        if (messageArray[0].matches("GroupCreated")) return;
        if (messageArray[0].matches("ERROR")) {
            String reason = extractReason(messageArray);
            throw new MyServerException(reason);
        } else throw new MyServerException("Something went wrong");
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
