import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice targetDevice;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private EditText messageEditText;
    private TextView chatTextView;
    private ImageView imageView;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MESSAGE_RECEIVED = 2;
    private static final int IMAGE_RECEIVED = 3;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVED:
                    String receivedMessage = (String) msg.obj;
                    chatTextView.append("Received: " + receivedMessage + "\n");
                    break;
                case IMAGE_RECEIVED:
                    Bitmap receivedImage = (Bitmap) msg.obj;
                    imageView.setImageBitmap(receivedImage);
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        messageEditText = findViewById(R.id.messageEditText);
        chatTextView = findViewById(R.id.chatTextView);
        imageView = findViewById(R.id.imageView);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void enableBluetooth(View view) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void connectToDevice(View view) {
        // Implement Bluetooth device discovery and connection logic here
    }

    public void sendMessage(View view) {
        String message = messageEditText.getText().toString();
        if (socket != null && outputStream != null) {
            try {
                outputStream.write(message.getBytes());
                chatTextView.append("You: " + message + "\n");
                messageEditText.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendImage(View view) {
        // Implement image sending logic here
    }

    private class MessageReceiverThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String receivedMessage = new String(buffer, 0, bytes);
                    handler.obtainMessage(MESSAGE_RECEIVED, receivedMessage).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close sockets and streams here
    }
}
