/*
Heart Rate Estimation

Ke Sun
09.10.19
*/

package com.nju.ke.heartbeat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.lang.Math;

import java.net.Socket;
import java.io.OutputStream;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;


    private boolean logenabled = true;
    private String sysname = "Hearbeat";
    private TextView AppInfo;//,authenticationInfo,authenticationResult;
    private Button socketButton;
    private EditText editTextIp,editTextName,editTextAge,editTextAllergy,editTextPort;

    private static int fineLen=37;


    private String IP_address= "172.168.1.103";
    private Socket datasocket;
    private OutputStream datastream;
    byte[] networkbuf = new byte[3*100];
    private long lasttime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        editTextName = (EditText) findViewById(R.id.Name);
        editTextAge = (EditText) findViewById(R.id.Age);
        editTextAllergy = (EditText) findViewById(R.id.Allergy);
        editTextIp = (EditText)findViewById(R.id.ServerIP);
        editTextPort = (EditText)findViewById(R.id.ServerPort);


        //AppInfo = (TextView) findViewById(R.id.AppInfo);
//        authenticationInfo = (TextView)findViewById(R.id.authenticationInfo);
//        authenticationResult = (TextView)findViewById(R.id.authenticationResult);
//        progressbar = (NumberProgressBar)findViewById(R.id.progressbar);
//        authenticationInfo.setText(String.format("Authentication Result:"));
//        authenticationResult.setTextSize(30);
//        progressbar.setReachedBarColor(Color.BLACK);
//        progressbar.setProgressTextColor(Color.BLACK);
//        progressbar.setProgress(0);

        //AppInfo.setTextColor(Color.RED);
        socketButton = (Button)findViewById(R.id.buttonSocket);

        socketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //socketButton.setEnabled(false);
                //socketButton.setEnabled(true);
                new ThreadSocket().start();


            }

        });

    }

    class ThreadSocket extends Thread
    {
        public void run()
        {
            mylog("test press");
            try{
                IP_address = editTextIp.getText().toString();
                int Port = Integer.parseInt(editTextPort.getText().toString());

                datasocket = new Socket(IP_address,Port);//"172.28.53.174"
                mylog("socket connected"+datasocket);
                datastream = datasocket.getOutputStream();
                mylog("socketsream:"+datastream);
                //datastream.write("test".getBytes());

            } catch(Exception e) {
                // TODO: handle this
                mylog("socket error"+e);
            }
            mylog("test");
            char[] Name_socket = new char[50];
            String Name_string = editTextName.getText().toString();
            for (int i =0;i<50;i++)   Name_socket[i] = ' ';
            for (int i =0;i<Name_string.length();i++)   Name_socket[i] = Name_string.charAt(i);

            char[] Age_socket = new char[50];
            String Age_string = editTextAge.getText().toString();
            for (int i =0;i<50;i++)   Age_socket[i] = ' ';
            for (int i =0;i<Age_string.length();i++)   Age_socket[i] = Age_string.charAt(i);

            char[] Allergy_socket = new char[50];
            String Allergy_string = editTextAllergy.getText().toString();
            for (int i =0;i<50;i++)   Allergy_socket[i] = ' ';
            for (int i =0;i<Allergy_string.length();i++)   Allergy_socket[i] = Allergy_string.charAt(i);

//            int j = 0;
//            for (int i =0;i<50;i++) {
//                networkbuf[j++] = (byte) (Name_socket[i] & 0xFF);
//                networkbuf[j++] = (byte) ((Name_socket[i] >> 8) & 0xFF);
//            }
//
//            for (int i =0;i<50;i++) {
//                networkbuf[j++] = (byte) (Age_socket[i] & 0xFF);
//                networkbuf[j++] = (byte) ((Age_socket[i] >> 8) & 0xFF);
//            }
//
//            for (int i =0;i<50;i++) {
//                networkbuf[j++] = (byte) (Allergy_socket[i] & 0xFF);
//                networkbuf[j++] = (byte) ((Allergy_socket[i] >> 8) & 0xFF);
//            }

            String aa =String.valueOf(Name_socket) + String.valueOf(Age_socket) + String.valueOf(Allergy_socket);
            networkbuf = aa.getBytes(Charset.forName("ASCII"));
            mylog("aaa"+networkbuf.length);


//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (datastream != null) {
//                            if (datastream != null) {
//                                try {
//                                    datastream.write(networkbuf, 0, 300);
//                                    mylog("socket write" + 300);
//                                } catch (Exception e) {
//                                    mylog("socket error" + e);
//                                }
//                            }
//                        }
//                    }
//                }).start();

            mylog("cnm");
            if (datastream != null) {
                if (datastream != null) {


                    try {
                        datastream.write(networkbuf, 0, 150);
                        mylog("socket write" + 150);
                    } catch (Exception e) {
                        mylog("socket error" + e);
                    }
                }
            }
            String IP = editTextIp.getText().toString();
            String Port = editTextPort.getText().toString();
            String Server[] = new String[2];
            Server[0] = IP;
            Server[1] = Port;
            mylog("Server_send:"+Server[0]+":"+Server[1]);
            Intent intent = new Intent(MainActivity.this,Heartbeat.class);
            intent.putExtra("Server",Server);
            startActivity(intent);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void mylog(String information)
    {
        if(logenabled)
        {

            Log.i(sysname,information);
        }
    }
}
