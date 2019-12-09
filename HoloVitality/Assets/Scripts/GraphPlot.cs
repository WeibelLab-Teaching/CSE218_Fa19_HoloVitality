using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using System.Linq.Expressions;
using BarGraph.VittorCloud;

using TMPro;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System;

public class GraphPlot : MonoBehaviour
{
    public List<BarGraphDataSet> exampleDataSet; // public data set for inserting data into the bar graph
    BarGraphGenerator barGraphGenerator;

    // Start is called before the first frame update
    public Transform textMeshObject1, textMeshObject2;
    TMPro.TextMeshProUGUI textMesh1, textMesh2;


    private TcpClient client;
    private NetworkStream nwStream;
    private int bytesRead;
    public int PORT_NO;
    private string SERVER_IP;
    private bool callOnce = true;
    private bool dataReady = false;
    const int BUFFERSIZE = 160;

   

    void Start()
    {

        SERVER_IP = PlayerPrefs.GetString("SERVER_IP");
        Debug.Log("Analysis: " + PORT_NO);
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
        string user1 = PlayerPrefs.GetString("user12349");
        string user2 = PlayerPrefs.GetString("user12359");

        this.textMesh1 = this.textMeshObject1.GetComponent<TMPro.TextMeshProUGUI>();
        this.textMesh2 = this.textMeshObject2.GetComponent<TMPro.TextMeshProUGUI>();


        if (user1!="")
        {
            this.textMesh1.text = user1;
        }
        else
        {
            this.textMesh1.text = "User1";
        }
       

        if (user2!="")
        {
            this.textMesh2.text = user2;
        }
        else
        {
            this.textMesh2.text = "User2";
        }


        barGraphGenerator = GetComponent<BarGraphGenerator>();


        //if the exampleDataSet list is empty then return.
        if (exampleDataSet.Count == 0)
        {
            Debug.LogError("ExampleDataSet is Empty!");
            return;
        }
        barGraphGenerator.GeneratBarGraph(exampleDataSet);

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
                Debug.Log(numRead);
                if (numRead == BUFFERSIZE)
                {
                    Debug.Log("789789798");

                    for (int num = 0; num < 2; num++)
                    {
                        int j = 0;
                        for (; j < 10; j++)
                        {
                            exampleDataSet[num].ListOfBars[j].YValue = (int)BitConverter.ToDouble(bytesToRead, (num * 10 + j) * 8);
                            Debug.Log(exampleDataSet[num].ListOfBars[j].YValue);
                        }
                        callOnce = false;
                    }
                }
            }
        }
        if (!callOnce)
        {
            dataReady = true;
        }
#endif
    }


    //call when the graph starting animation completed,  for updating the data on run time
    public void StartUpdatingGraph()
    {
        StartCoroutine(CreateDataSet());
    }



    IEnumerator CreateDataSet()
    {
        //  yield return new WaitForSeconds(3.0f);
#if UNITY_EDITOR
        while (true)
        {
            GenerateRandomData();
            yield return new WaitForSeconds(3.0f);
        }
#else
        while(!dataReady){
             yield return new WaitForSeconds(1.0f);
        }
         
    
        for (int dataset = 0; dataset < 2; dataset++)
        {
            
            for (int y = 0; y < 10; y++)
            {
                barGraphGenerator.AddNewDataSet(dataset, y, exampleDataSet[dataset].ListOfBars[y].YValue);
                yield return new WaitForSeconds(1.0f);
            }
        }
#endif

    }



    //Generates the random data for the created bars
    void GenerateRandomData()
    {
        Debug.Log(barGraphGenerator.yMaxValue);
        int dataSetIndex = UnityEngine.Random.Range(0, exampleDataSet.Count);
        int xyValueIndex = UnityEngine.Random.Range(0, exampleDataSet[dataSetIndex].ListOfBars.Count);
        exampleDataSet[dataSetIndex].ListOfBars[xyValueIndex].YValue = UnityEngine.Random.Range(barGraphGenerator.yMinValue, barGraphGenerator.yMaxValue);
        barGraphGenerator.AddNewDataSet(dataSetIndex, xyValueIndex, exampleDataSet[dataSetIndex].ListOfBars[xyValueIndex].YValue);
    }
}



