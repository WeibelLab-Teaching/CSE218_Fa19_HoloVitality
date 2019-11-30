using System.Collections;
using System.Collections.Generic;

using UnityEngine;
using TMPro;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System;

public class user2contentHR : MonoBehaviour
{
    private TextMeshPro textM;
    private int heartRate;
    private int variance;
    private long timeStamp;
    private int bytesRead;
    private int criticalRate=121;
    private int nextUpdate=1;

    private TcpClient client;
    private NetworkStream nwStream;

    const int PORT_NO = 5000;
    const string SERVER_IP = "127.0.0.1";
    const int BUFFERSIZE = 24;
    // Start is called before the first frame update
    void Start()
    {
        heartRate = 90;
        textM = GetComponent<TextMeshPro>();
        textM.text = "User2\nHR:  --- /min";

        //client = new TcpClient(SERVER_IP, PORT_NO);
        //nwStream = client.GetStream();
    }

    // Update is called once per frame
    void Update()
    {
        if(Time.time>=nextUpdate){
            heartRate = 90+4*nextUpdate%40;

            if (heartRate >= criticalRate)
            {
                textM.color = new Color32(255, 0, 0, 255);
            }
            else
            {
                textM.color = new Color32(255, 255, 255, 255);
            }
            textM.text = "User2\nHR: " + heartRate.ToString() + "/min";


            //byte[] bytesToRead = new byte[BUFFERSIZE];
            //int numRead = nwStream.Read(bytesToRead, 0, 24);

            //if (numRead == BUFFERSIZE)
            //{
            //    heartRate = (int)BitConverter.ToDouble(bytesToRead, 0);

            //    variance = (int)BitConverter.ToDouble(bytesToRead, 8);

            //    timeStamp = BitConverter.ToInt64(bytesToRead, 16);

            //    if (heartRate >= criticalRate)
            //    {
            //        textM.color = new Color32(255, 0, 0, 255);
            //    }
            //    else
            //    {
            //        textM.color = new Color32(255, 255, 255, 255);
            //    }
            //    textM.text = "HR: " + heartRate.ToString() + "/min";
            //}

            nextUpdate+=1;
        }
        
    }

    //private void OnDestroy()
    //{
    //    client.Close();
    //}
}
