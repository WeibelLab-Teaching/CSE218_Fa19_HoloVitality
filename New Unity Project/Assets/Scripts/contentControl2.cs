using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;

public class contentControl2 : MonoBehaviour
{
    // Start is called before the first frame update
    private TextMeshPro textM;
    private int RespiratoryRate;
    private int criticalRate=25;
    private int nextUpdate=1;
    // Start is called before the first frame update
    void Start()
    {
        RespiratoryRate = 12;
        textM = GetComponent<TextMeshPro>();
        textM.color = Color.cyan;
        textM.text = "RR: -- /min";
    }

    // Update is called once per frame
    void Update()
    {
        if(Time.time>=nextUpdate){
            RespiratoryRate = 12+3*nextUpdate%20;
            if (RespiratoryRate>=criticalRate){
                textM.color = Color.yellow;
            }else{
                textM.color = Color.cyan;
            }
            textM.text = "RR: "+RespiratoryRate.ToString()+"/min";

            nextUpdate+=1;
        }
        
    }
}
