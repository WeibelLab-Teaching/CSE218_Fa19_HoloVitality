package com.nju.ke.heartbeat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

public class Heartbeat extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TestSensorListener mSensorListener;


    private boolean logenabled = true;
    private String sysname = "Hearbeat";
    private LineChartView linerChart;
    private LineChartView linerChartHeartBeat;
    private TextView yInfo,heartbeatInfo, heartbeatVar;//,authenticationInfo,authenticationResult;
    private NumberProgressBar progressbar;
    private Button startButton, endButton, socketButton;
    private TextView textViewConnect;
    private String svmResult = "No result...";
    //private EditText editTextIp;

    private Queue<Double> chartDataQueue = new ArrayDeque<>(600);
    private double procDataBuffer[] = new double [100];
    private double nowBuffer[] = new double[600];
    private LinkedList<Integer> peakIndex = new LinkedList<>();
    private int updateFreq = 0;
    private Queue<Float> heartRatequeue = new ArrayDeque<>(10);
    private List<PointValue> aoPointsValues = new ArrayList<>(600);
    private List<PointValue> aoFineValues = new ArrayList<>(600);


    private svm_model svmModel;
    private static int fineLen=37;
    private double fineTemplate[] = new double [fineLen];
    private double convBuffer[] = new double[600+fineLen-1];
    private double heartnode[] = new double[128];
    private svm_node heart_svm[] = new svm_node[128];
    private int heartNum= 0;
    private Line rateline1 = new Line();
    private double dec_values[] = new double[2];

    boolean Test_mode = true;


    private String IP_address= "172.168.1.103";
    private int IP_Port = 12345;
    private Socket datasocket;
    private OutputStream datastream;
    byte[] networkbuf = new byte[8*3];
    private long lasttime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat);

        String[] Server = getIntent().getStringArrayExtra("Server");
        IP_address = Server[0];
        IP_Port = Integer.parseInt(Server[1])+1;
        mylog("Server_receive:"+Server[0]+":"+Server[1]);
        //textViewConnect = (TextView)findViewById(R.id.connectInfo);

        mSensorListener = new TestSensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linerChart = linerChartInit();
        linerChartHeartBeat = linerChartInitHeartBeat();
        yInfo = (TextView)findViewById(R.id.yInfo);
        heartbeatInfo = (TextView)findViewById(R.id.heartbeatInfo);
        heartbeatVar = (TextView)findViewById(R.id.heartbeatVar);
//        authenticationInfo = (TextView)findViewById(R.id.authenticationInfo);
//        authenticationResult = (TextView)findViewById(R.id.authenticationResult);
//        progressbar = (NumberProgressBar)findViewById(R.id.progressbar);
        yInfo.setText(String.format("SCG Signal:"));
        heartbeatInfo.setText(String.format("Heart Beat Estimation:"));
//        authenticationInfo.setText(String.format("Authentication Result:"));
//        authenticationResult.setTextSize(30);
//        progressbar.setReachedBarColor(Color.BLACK);
//        progressbar.setProgressTextColor(Color.BLACK);
//        progressbar.setProgress(0);

        socketButton = (Button)findViewById(R.id.buttonSocket);
        //editTextIp = (EditText)findViewById(R.id.edit_ip);
//        startButton = (Button)findViewById(R.id.button1);
//        startButton.setEnabled(true);
//        endButton = (Button)findViewById(R.id.button2);
//        endButton.setEnabled(false);
//        for (int i=0;i<128;i++) heart_svm[i] = new svm_node();

//        startButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                startButton.setEnabled(false);
//                endButton.setEnabled(true);
//                int ii=0;
//                for (Double index: chartDataQueue) {
//                    nowBuffer[ii] = (double) index.doubleValue();
//                    ii++;
//                }
//                new ThreadInstantAuth().start();
//            }
//        });

        socketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                socketButton.setEnabled(false);
                socketButton.setEnabled(true);
                socketButton.setTextColor(Color.GREEN);
                new ThreadSocket().start();
            }

        });
//        endButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                startButton.setEnabled(true);
//                endButton.setEnabled(false);
//            }
//        });


//        loadSVMModel();

        loadFineTemplate();
    }

    class ThreadSocket extends Thread
    {
        public void run()
        {
            try{
                datasocket = new Socket(IP_address,IP_Port);//"172.28.53.174"
                mylog("socket connected"+datasocket);
                datastream = datasocket.getOutputStream();
                mylog("socketsream:"+datastream);
                //datastream.write("test".getBytes());

            } catch(Exception e) {
                // TODO: handle this
                mylog("socket error"+e);
            }
        }
    }

    class ThreadInstantAuth extends Thread
    {
        @Override
        public void run()
        {
            // read buffer
            int len=600;
            int delay=fineLen+2;
            // nowbuffer-mean(nowbuffer)
            double sum=0;
            for (int i=0;i<len;i++) {
                sum+=nowBuffer[i];
            }
            sum=sum/len;
            for (int i=0;i<len;i++) {
                nowBuffer[i]=nowBuffer[i]-sum;
            }
            long starttime = System.nanoTime();
            convBuffer=conv(nowBuffer,fineTemplate,len,fineLen);
            peakIndex = Peaks.findPeaks(convBuffer,60,0.02);
            aoFineValues.clear();
            mylog("hahaha:"+peakIndex.size());
            for (int i=0;i<peakIndex.size();i++) {
                //aoFineValues.add(new PointValue(peakIndex.get(i), (float) convBuffer[peakIndex.get(i)]));
                if (peakIndex.get(i)-delay<0) continue;
                if (peakIndex.get(i)-delay<600)
                    aoFineValues.add(new PointValue(peakIndex.get(i)-delay, (float) nowBuffer[peakIndex.get(i)-delay]));
            }
            long endtime = System.nanoTime();
            mylog("finetime: "+(endtime-starttime));
            starttime = System.nanoTime();
            for (int i=1;i<2;i++) {
                if (peakIndex.size()<2) continue;
                if (peakIndex.get(i+1)-peakIndex.get(i)>100 || peakIndex.get(i+1)-peakIndex.get(i)<60) continue;
                for (int j=0;j<128;j++) heartnode[j]=0;
                if (peakIndex.get(i)-delay<0) continue;
                for (int j=peakIndex.get(i)-delay;j<peakIndex.get(i+1)-delay;j++) {
                    heartnode[j-(peakIndex.get(i)-delay)]=nowBuffer[j];
                }
                heartnode=heartNormoralize(heartnode,peakIndex.get(i+1)-peakIndex.get(i));
                for (int j=0;j<128;j++) {
                    heart_svm[j].index=j+1;
                    heart_svm[j].value=heartnode[j];
                }
                double result = svm.svm_predict_probability(svmModel,heart_svm,dec_values);
                //mylog("svmresult:"+result+":"+dec_values[1]);
                //svmResult = result+":"+(int)(dec_values[1]*100)+"%";
//                heartNum++;
//                String FileName = "/storage/emulated/0/heartbeat_svm/heartnode.txt";
//                File file = new File(FileName);
//                BufferedWriter writer = null;
//                FileWriter out =null;
//                try {
//                    out = new FileWriter(file, true);
//                    writer = new BufferedWriter(out);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                for (int j=0;j<128;j++) {
//                    try {
//                        writer.write(Double.toString(heart_svm[j].value)+"\t");
//                        writer.newLine();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    writer.close();
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            endtime = System.nanoTime();
            mylog("svmtime: "+(endtime-starttime));
        }
    }

    private double[] heartNormoralize(double[] heartnode,int poi) {
        double maxnow=0.0;
        double totnow=0.0;
        for (int i=0;i<poi;i++) {
            totnow+=heartnode[i];
        }
        totnow=totnow/poi;
        for (int i=0;i<poi;i++) {
            heartnode[i]=heartnode[i]-totnow;
        }
        for (int i=0;i<poi;i++) {
            if (heartnode[i]>maxnow) maxnow=heartnode[i];
        }
        for (int i=0;i<poi;i++) {
            heartnode[i]=heartnode[i]/maxnow;
        }
        return heartnode;
    }

    private double[] conv(double[] nowBuffer, double[] fineTemplate,int len1,int len2) {
        double convBuffer[] = new double[len1+len2-1];
        double newBuffer[] = new double [len1+len2+len2-2];
        int ii=0;
        for (int i=0;i<len2-1;i++) {
            newBuffer[ii]=0;
            ii++;
        }
        for (int i=0;i<len1;i++) {
            newBuffer[ii]=nowBuffer[i];
            ii++;
        }
        for (int i=0;i<len2-1;i++) {
            newBuffer[ii]=0;
            ii++;
        }
        for (int i=0;i<len1+len2-1;i++) {
            convBuffer[i]=0;
            for (int j=0;j<len2;j++) {
                convBuffer[i]+=newBuffer[i+j]*fineTemplate[j];
            }
        }

        return convBuffer;
    }

    private void loadSVMModel() {
        svmModel = new svm_model();
        try {
            //svmModel = svm.svm_load_model(Environment.getExternalStorageDirectory().getAbsolutePath() + "/heartbeat_svm/svm_model.txt");
            svmModel = svm.svm_load_model("/storage/emulated/0" + "/heartbeat_svm/svm_model.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFineTemplate() {
        Log.i(Environment.getExternalStorageDirectory().getAbsolutePath(), "hahaha");
        Log.i(Environment.getExternalStorageDirectory().getAbsolutePath(), "hahaha");
        String FileName = "/storage/emulated/0" + "/heartbeat_svm/fine_template.txt";
        //String FileName = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/heartbeat_svm/fine_template.txt";
        File file = new File(FileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < fineLen; i++) {
            try {
                fineTemplate[i] = Double.parseDouble(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mAccelerometer, 10000);//设置刷新率100Hz
        //mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }

    private class TestSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (chartDataQueue.size() == 600) chartDataQueue.remove();
            chartDataQueue.add(new Double(event.values[2]));
            //yInfo.setText(String.format("Y:%.2f m/s²",event.values[1]));
            updateFreq++;
            if (updateFreq % 10 ==0) {
                long starttime = System.nanoTime();
                peaksFinding();
                long endtime = System.nanoTime();
                mylog("peaksFinding "+(endtime-starttime));
                chartViewUpdate();
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

    private LineChartView linerChartInitHeartBeat() {
        LineChartView linerChart = (LineChartView) findViewById(R.id.linerChartheartbeat);
        Viewport v = new Viewport(linerChart.getMaximumViewport());
        linerChart.setInteractive(false);
        v.bottom = 0;
        v.top = 15;
        linerChart.setCurrentViewport(v);
        linerChart.setMaximumViewport(v);
        linerChart.setInteractive(true);
        linerChart.setZoomType(ZoomType.HORIZONTAL);
        linerChart.setMaxZoom((float) 2);//最大方法比例
        linerChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        linerChart.setVisibility(View.VISIBLE);
        //linerChart.setZoomEnabled(false);
        return linerChart;
    }

    private LineChartView linerChartInit() {
        LineChartView linerChart = (LineChartView) findViewById(R.id.linerChart);
        Viewport v = new Viewport(linerChart.getMaximumViewport());
        linerChart.setInteractive(false);
        v.bottom = 0;
        v.top = 15;
        linerChart.setCurrentViewport(v);
        linerChart.setMaximumViewport(v);
        linerChart.setInteractive(true);
        linerChart.setZoomType(ZoomType.HORIZONTAL);
        linerChart.setMaxZoom((float) 2);//最大方法比例
        linerChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        linerChart.setVisibility(View.VISIBLE);
        //linerChart.setZoomEnabled(false);
        return linerChart;
    }

    private void peaksFinding() {
        int i=0;
        double sum=0.0;
        for (Double index: chartDataQueue) {
            if (i>=500) {
                sum+=index.doubleValue();
                procDataBuffer[i-500] = (double) index.doubleValue();
            }
            i++;
        }
        peakIndex = Peaks.findPeaks(procDataBuffer,15,sum/100+0.05);
        if (peakIndex.size()==2) {
            //aoPointsValues.clear();
            if (peakIndex.get(1)-peakIndex.get(0)>50) {
                if (aoPointsValues.size()<1) aoPointsValues.add(new PointValue((float)(peakIndex.get(1)+500), (float)(procDataBuffer[peakIndex.get(1)])));
                else if (peakIndex.get(1)+500-aoPointsValues.get(aoPointsValues.size()-1).getX()>50) {
                    if (peakIndex.get(1)+500-aoPointsValues.get(aoPointsValues.size()-1).getX()<100) {
                        if (heartRatequeue.size()>=10) heartRatequeue.poll();
                        heartRatequeue.add((float)(peakIndex.get(1)+500-aoPointsValues.get(aoPointsValues.size()-1).getX()));
                    }
                    aoPointsValues.add(new PointValue((float) (peakIndex.get(1) + 500), (float) (procDataBuffer[peakIndex.get(1)])));
                }
            }
            else {
                if (aoPointsValues.size()<1) aoPointsValues.add(new PointValue((float)(peakIndex.get(0)+500), (float)(procDataBuffer[peakIndex.get(0)])));
                else if (peakIndex.get(0)+500-aoPointsValues.get(aoPointsValues.size()-1).getX()>50) {
                    if (peakIndex.get(0)+500-aoPointsValues.get(aoPointsValues.size()-1).getX()<100) {
                        if (heartRatequeue.size()>=10) heartRatequeue.poll();
                        heartRatequeue.add((float)(peakIndex.get(0)+500-aoPointsValues.get(aoPointsValues.size()-1).getX()));
                    }
                    aoPointsValues.add(new PointValue((float) (peakIndex.get(0) + 500), (float) (procDataBuffer[peakIndex.get(0)])));
                }
            }
        }
        for (i=0;i<aoPointsValues.size();i++)
            if (aoPointsValues.get(i).getX()-10>0)
                aoPointsValues.set(i, new PointValue(aoPointsValues.get(i).getX() - 10, aoPointsValues.get(i).getY()));
            else aoPointsValues.remove(i);
    }
    private void chartViewUpdate() {
        List<PointValue> yPointsValues = new ArrayList<>(600);
        int i = 0;
        //if (startButton.isEnabled()==true) {
        if (Test_mode == true) {
            for (Double index : chartDataQueue) {
                yPointsValues.add(new PointValue(i, (float) index.doubleValue()));
                i++;
            }
        }
        else {
//              for (i=0;i<128;i++) {
//                  yPointsValues.add(new PointValue(i, (float) heart_svm[i].value));
//              }
            for (i=0;i<600;i++) {
                yPointsValues.add(new PointValue(i, (float) nowBuffer[i]));
            }
//            for (i=0;i<600;i++) {
//                yPointsValues.add(new PointValue(i, (float) convBuffer[i]));
//            }
        }
        Line yline = new Line(yPointsValues).setColor(Color.argb(0xff,0xff,0x88,0x00)).setCubic(true);
        Line aoline = new Line(aoPointsValues).setColor(Color.argb(0xff,0x00,0x99,0xCC)).setCubic(true);
        List<Line> lines = new ArrayList<>();
        yline.setHasPoints(false);
        yline.setStrokeWidth(2);
        aoline.setHasPoints(true);
        aoline.setStrokeWidth(0);
        lines.add(yline);
        //if (startButton.isEnabled()==true) {
        if (Test_mode == true) {
            yInfo.setText(String.format("SCG Signal:"));
            lines.add(aoline);
//            progressbar.setReachedBarColor(Color.BLACK);
//            progressbar.setProgressTextColor(Color.BLACK);
//            progressbar.setProgress(0);
            svmResult = "No result...";
//            authenticationResult.setTextColor(Color.BLACK);
//            authenticationResult.setText(svmResult);
        }
        else {
            yInfo.setText(String.format("SCG Signal Fine Segmentation:"));
            Line aoline_fine = new Line(aoFineValues).setColor(Color.argb(0xff,0x00,0x99,0xCC)).setCubic(true);
            aoline_fine.setHasPoints(true);
            aoline_fine.setStrokeWidth(0);
            lines.add(aoline_fine);
//            authenticationResult.setText(svmResult);
            int ans=(int)(dec_values[1]*100);
            if (ans<80)    {
                svmResult = "No !";
//                authenticationResult.setTextColor(Color.RED);
//                progressbar.setReachedBarColor(Color.RED);
//                progressbar.setProgressTextColor(Color.RED);
            }
            else {
                svmResult = "Yes !";
//                authenticationResult.setTextColor(Color.argb(0xff,0x66,0x99,0x00));
//                progressbar.setReachedBarColor(Color.argb(0xff,0x66,0x99,0x00));
//                progressbar.setProgressTextColor(Color.argb(0xff,0x66,0x99,0x00));
            }
//            authenticationResult.setText(svmResult);
//            progressbar.setProgress(ans);
        }
        LineChartData data = new LineChartData();
        Axis xaxis = new Axis();
        Axis yaxis = new Axis();
        yaxis.setMaxLabelChars(5);

        xaxis.setHasLines(true);
        yaxis.setHasLines(true);


        data.setAxisXBottom(xaxis);
        data.setAxisYLeft(yaxis);
        data.setLines(lines);

        linerChart.setLineChartData(data);

        List<PointValue> heartRateEstimation = new ArrayList<>(10);

        i=0;
        double meanHeartRate = 0;
        for (Float index: heartRatequeue) {
            meanHeartRate+= (float)(6000.0/index.floatValue());
            heartRateEstimation.add(new PointValue((float)i, (float)(6000.0/index.floatValue())));
            i++;
        }

        double avgHeartRate = meanHeartRate/(double)heartRatequeue.size();
        double stdHeartRate = 0;
        for (Float index: heartRatequeue) {
            stdHeartRate += ((float)(6000.0/index.floatValue()) - avgHeartRate)*((float)(6000.0/index.floatValue()) - avgHeartRate);
        }

        Line rateline = new Line(heartRateEstimation).setColor(Color.BLACK).setCubic(true);

        List<Line> linesRate = new ArrayList<>();
        rateline.setHasPoints(true);
        rateline.setStrokeWidth(2);
        LineChartData dataRate = new LineChartData();
        //if (startButton.isEnabled()==true) {
        if (Test_mode == true) {
            linesRate.add(rateline);
            rateline1=rateline;
            avgHeartRate = meanHeartRate/(double)heartRatequeue.size();
            stdHeartRate = Math.sqrt(stdHeartRate/(float)(heartRatequeue.size()-1));
            heartbeatInfo.setText(String.format("Heart Rate Estimation:%f",avgHeartRate));
            heartbeatVar.setText(String.format("Heart Rate Variability:%f",stdHeartRate));

            long a;
            int j = 0;
            a = Double.doubleToRawLongBits(avgHeartRate);
            networkbuf[j++] = (byte) (a & 0xFF);
            networkbuf[j++] = (byte) ((a >> 8) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 16) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 24) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 32) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 40) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 48) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 56) & 0xFF);
            a = Double.doubleToRawLongBits(stdHeartRate);
            networkbuf[j++] = (byte) (a & 0xFF);
            networkbuf[j++] = (byte) ((a >> 8) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 16) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 24) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 32) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 40) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 48) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 56) & 0xFF);
            a = System.currentTimeMillis();
            networkbuf[j++] = (byte) (a & 0xFF);
            networkbuf[j++] = (byte) ((a >> 8) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 16) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 24) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 32) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 40) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 48) & 0xFF);
            networkbuf[j++] = (byte) ((a >> 56) & 0xFF);
            if (System.currentTimeMillis() - lasttime>1000) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (datastream != null) {
                            if (datastream != null) {
                                try {
                                    datastream.write(networkbuf, 0, 24);
                                    mylog("socket write" + 24);
                                } catch (Exception e) {
                                    mylog("socket error" + e);
                                }
                            }
                        }
                    }
                }).start();
                lasttime = System.currentTimeMillis();
            }

        }
        else {
            linesRate.add(rateline1);
        }
        Axis xaxisRate = new Axis();
        Axis yaxisRate = new Axis();
        xaxisRate.setHasLines(true);
        yaxisRate.setHasLines(true);
        yaxisRate.setMaxLabelChars(5);


        dataRate.setAxisXBottom(xaxis);
        dataRate.setAxisYLeft(yaxis);
        dataRate.setLines(linesRate);
        linerChartHeartBeat.setLineChartData(dataRate);
    }

    private void mylog(String information)
    {
        if(logenabled)
        {

            Log.i(sysname,information);
        }
    }
}
