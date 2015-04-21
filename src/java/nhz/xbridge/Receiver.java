package nhz.xbridge;

import nhz.xbridge.chat.MessageRepository;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Receiver implements Runnable {

    private DataInputStream is;
    private String currentAddress;

    public Receiver(DataInputStream is, String currentAddress) {
        this.is = is;
        this.currentAddress = currentAddress;
    }

    @Override
    public void run() {
        int receivedBytes;
        try {
            while (-1 != (receivedBytes = is.read())) {
                byte[] buffer = new byte[receivedBytes];
                try {
                    is.read(buffer);
                    System.out.println("received from x-bridge: " + new String(buffer));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                dataWork(buffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dataWork(byte[] buffer) {
        // todo fix
        int excess = 3;

        int size = buffer.length;
        if (size > excess)
            buffer = Arrays.copyOfRange(buffer, excess, size);

        if (XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES <= size) {
            byte[] addressToBytes = Arrays.copyOfRange(buffer, 0, XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES);
            String to = new String(addressToBytes);
            if (to.equals(currentAddress)) {
                String from = null;
                byte[] dataBytes = null;

                if (2 * XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES <= size) {
                    byte[] addressFromBytes = Arrays.copyOfRange(buffer, XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES, 2 * XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES);
                    from = new String(addressFromBytes);
                    dataBytes = Arrays.copyOfRange(buffer, 2 * XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES, buffer.length);
                } else {
                    // dataBytes = Arrays.copyOfRange(buffer, XBridgeMessage.X_BRIDGE_ADDRESS_SIZE_IN_BYTES, buffer.length);
                }

                if (null != dataBytes) {
                    XBridgeMessage xBridgeMessage = new XBridgeMessage();
                    xBridgeMessage.setFrom(from);
                    xBridgeMessage.setTo(currentAddress);
                    xBridgeMessage.setContent(dataBytes);

                    MessageRepository.getInstance().add(xBridgeMessage);
                }
            }
        }
    }


}
