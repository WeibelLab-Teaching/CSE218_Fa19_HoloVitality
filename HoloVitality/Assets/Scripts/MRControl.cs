using System.Collections;
using System.Collections.Generic;

using UnityEngine;
using TMPro;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System;

public class MRControl : MonoBehaviour
{
    private TextMeshPro textM;
    private string nameStr, ageStr, allergyStr;
    private int bytesRead;

    private TcpClient client;
    private NetworkStream nwStream;

    public int PORT_NO;
    private string SERVER_IP;
    const int BUFFERSIZE = 8;   //change
    // Start is called before the first frame update
    void Start()
    {

        textM = GetComponent<TextMeshPro>();
        textM.text = "Name: John Doe\nAge: 50\nAllergy: Cats\n";


        SERVER_IP = PlayerPrefs.GetString("SERVER_IP");
        Debug.Log("HR: " + PORT_NO);
#if !UNITY_EDITOR
        client = new TcpClient(SERVER_IP, PORT_NO);
        nwStream = client.GetStream();

        byte[] bytesToRead = new byte[BUFFERSIZE];
        if (nwStream.DataAvailable)
        {
            int numRead = nwStream.Read(bytesToRead, 0, 8);
            Debug.Log("1234");

            if (numRead == BUFFERSIZE)
            {
                //heartRate = (int)BitConverter.ToDouble(bytesToRead, 0);


                textM.text = "Name: " + nameStr + "\nAge: " + ageStr + "\nAllergy: " + allergyStr;
            }
        }
#endif
    }
}