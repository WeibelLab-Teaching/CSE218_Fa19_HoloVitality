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
    private string data;
    private string nameStr, ageStr, allergyStr;
    private int bytesRead;
    private bool callOnce = true;

    private TcpClient client;
    private NetworkStream nwStream;

    public int PORT_NO;
    private string SERVER_IP;
    const int BUFFERSIZE = 300;
    // Start is called before the first frame update
    void Start()
    {

        textM = GetComponent<TextMeshPro>();
        textM.text = "Name: John Doe\nAge: 50\nAllergy: Cats\n";

        SERVER_IP = PlayerPrefs.GetString("SERVER_IP");
        Debug.Log("MR: " + PORT_NO);
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

    void Update()
    {
#if !UNITY_EDITOR
        if (callOnce)
        {
            byte[] bytesToRead = new byte[BUFFERSIZE];
            if (nwStream.DataAvailable)
            {
                int numRead = nwStream.Read(bytesToRead, 0, BUFFERSIZE);
                if (numRead == BUFFERSIZE)
                {
                    data = System.Text.Encoding.ASCII.GetString(bytesToRead);
                    nameStr = data.Substring(0, 50).TrimEnd();
                    ageStr = data.Substring(50, 50).TrimEnd();
                    allergyStr = data.Substring(100, 50).TrimEnd();

                    PlayerPrefs.SetString("user" + PORT_NO, nameStr);

                    textM.text = "Name: " + nameStr + "\nAge: " + ageStr + "\nAllergy: " + allergyStr;
                    callOnce = false;
                }
            }

        }
#endif
    }
}