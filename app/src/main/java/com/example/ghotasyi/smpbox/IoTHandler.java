package com.example.ghotasyi.smpbox;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.UnsupportedEncodingException;

public class IoTHandler {

    private Context context;
    private MqttAndroidClient client;

    private Listener listener;

    public IoTHandler(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    void connect (String serverURL, String username, String password){
        String clientid = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, serverURL, clientid);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try{
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            byte[] pesan = message.getPayload();
                            String msg = "";
                            for (byte huruf : pesan){
                                msg += String.valueOf(Character.toChars(huruf));
                            }
                            listener.onMessageArrived(topic, msg);
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });
                    //menghubungkan main act bila berhasil conect
                    listener.onConnected();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void subscribe(String[] topic, int[] qos){
        try{
            IMqttToken token = client.subscribe(topic, qos);

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            }
            );
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
//btn_kirim.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String payloadku = et_payload.getText().toString();
//            et_payload.setText("");
//            mqttHelper.mqttPublish(payloadku);
//            Toast.makeText(getActivity().getApplicationContext(), "Payload Terkirim", Toast.LENGTH_SHORT).show();
//        }
//    });
    void publish(String payload, String topic){

        byte[] encodePayload = new byte[0];

        try{
            encodePayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodePayload);
            IMqttDeliveryToken publish = client.publish(topic, message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public interface Listener {
        boolean onCreateOptionMenu(Menu menu);

        boolean onOptionItemSelected(MenuItem item);

        void onMessageArrived(String topic, String message);
        void onConnected();
    }
}
