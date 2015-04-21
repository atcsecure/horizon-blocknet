package nhz.xbridge.chat;

import nhz.Nhz;
import nhz.xbridge.Receiver;
import nhz.xbridge.XBridgeCommand;
import nhz.xbridge.XBridgePackage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Chat {
    private static class ChatHolder {
        public static final Chat INSTANCE = new Chat();
    }

    public static Chat getInstance() {
        return ChatHolder.INSTANCE;
    }

    private Chat() { }

    public List<Message> getHistory() {
        return MessageRepository.getInstance().getMessages();
    }

    public void send(Message message) throws IOException {
        sendData(message.toXBridgePackage());
        MessageRepository.getInstance().add(message);
    }

    private void sendData(XBridgePackage xBridgePackage) throws IOException {
        String xbridgeHost = Nhz.getStringProperty("nhz.xbridge.host");
        int xbridgePort = Nhz.getIntProperty("nhz.xbridge.port");

        Socket socket = new Socket(xbridgeHost, xbridgePort);
        DataOutputStream os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        os.write(xBridgePackage.convertToByteArray());
        os.flush();
        socket.close();
    }



    public void initListener(String address) throws IOException {
        String xbridgeHost = Nhz.getStringProperty("nhz.xbridge.host");
        int xbridgePort = Nhz.getIntProperty("nhz.xbridge.port");

        XBridgePackage announcePackage = new XBridgePackage(XBridgeCommand.xbcAnnounceAddresses, address.getBytes());

        Socket socket = new Socket(xbridgeHost, xbridgePort);
        final DataInputStream is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        os.write(announcePackage.convertToByteArray());
        os.flush();

        Receiver r = new Receiver(is, address);
        Thread t = new Thread(r);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();

            is.close();
            socket.close();
        }
    }
}
