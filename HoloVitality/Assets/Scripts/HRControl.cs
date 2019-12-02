using System.Collections;
using System.Collections.Generic;

using UnityEngine;
using TMPro;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System;

public class HRControl : MonoBehaviour
{
    private TextMeshPro textM;
    private int heartRate;
    private int bytesRead;
    private int criticalRate=121;
    private int nextUpdate=10;


    private TcpClient client;
    private NetworkStream nwStream;

    public int PORT_NO;
    private string SERVER_IP;
    const int BUFFERSIZE = 8;
    // Start is called before the first frame update
    void Start()
    {
        heartRate = 90;
        textM = GetComponent<TextMeshPro>();
        textM.text = "HR: --- /min";


        SERVER_IP = PlayerPrefs.GetString("SERVER_IP");
        Debug.Log("HR: " + PORT_NO);
#if !UNITY_EDITOR
        client = new TcpClient(SERVER_IP, PORT_NO);
        nwStream = client.GetStream();
#endif
    }

    // Update is called once per frame
    void Update()
    {
        if(Time.time>=nextUpdate){
#if UNITY_EDITOR
            heartRate = 90 + 4 * nextUpdate % 40;

            if (heartRate >= criticalRate)
            {
                textM.color = new Color32(255, 0, 0, 255);
            }
            else
            {
                textM.color = new Color32(255, 255, 255, 255);
            }
            textM.text = "HR: " + heartRate.ToString() + "/min";
#else
            byte[] bytesToRead = new byte[BUFFERSIZE];
            if (nwStream.DataAvailable) {
                int numRead = nwStream.Read(bytesToRead, 0, 8);
                Debug.Log("1234");

                if (numRead == BUFFERSIZE)
                {
                    heartRate = (int)BitConverter.ToDouble(bytesToRead, 0);

                    if (heartRate >= criticalRate)
                    {
                        textM.color = new Color32(255, 0, 0, 255);
                    }
                    else
                    {
                        textM.color = new Color32(255, 255, 255, 255);
                    }
                    textM.text = "HR: " + heartRate.ToString() + "/min";
                }
            }
#endif
            nextUpdate += 1;
        }

    }

    //private void OnDestroy()
    //{
    //    client.Close();
    //}
}
