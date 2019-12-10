using System.Collections;
using System.Collections.Generic;

using UnityEngine;
using TMPro;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System;


public class VRControl : MonoBehaviour
{
    // Start is called before the first frame update
    private TextMeshPro textM;
    private int variance;
    private int criticalRate=10;
    private int nextUpdate=1;

    private TcpClient client;
    private NetworkStream nwStream;

    public int PORT_NO;
    private string SERVER_IP;
    const int BUFFERSIZE = 8;
    // Start is called before the first frame update
    void Start()
    {
        variance = 0;
        textM = GetComponent<TextMeshPro>();
        textM.color = Color.cyan;
        textM.text = "VR: -- /min";

        SERVER_IP = PlayerPrefs.GetString("SERVER_IP");
        Debug.Log("VR: " + PORT_NO);
#if !UNITY_EDITOR
        try
        {
            client = new TcpClient(SERVER_IP, PORT_NO);
            nwStream = client.GetStream();
        }
        catch
        {
            Debug.Log("socket error");
        }
#endif
    }

    // Update is called once per frame
    void Update()
    {
        if(Time.time>=nextUpdate){
#if UNITY_EDITOR
            variance = 12 + 3 * nextUpdate % 20;
            if (variance >= criticalRate)
            {
                textM.color = Color.yellow;
            }
            else
            {
                textM.color = Color.cyan;
            }
            textM.text = "VR: " + variance.ToString() + "/min";

#else
            byte[] bytesToRead = new byte[BUFFERSIZE];
            if (nwStream.DataAvailable)
            {
                int numRead = nwStream.Read(bytesToRead, 0, 8);
                if (numRead == BUFFERSIZE)
                {
                    //heartRate = (int)BitConverter.ToDouble(bytesToRead, 0);

                    variance = (int)BitConverter.ToDouble(bytesToRead, 0);

                    //timeStamp = BitConverter.ToInt64(bytesToRead, 16);

                    if (variance >= criticalRate)
                    {
                        textM.color = Color.yellow;
                    }
                    else
                    {
                        textM.color = Color.cyan;
                    }
                    textM.text = "VR: " + variance.ToString() + "/min";
                }
            }
#endif
            nextUpdate += 1;
        }
        
    }

    //private void OnDestroy()
    //{
    //    nwStream.Close();
    //    client.Close();
    //}
}
