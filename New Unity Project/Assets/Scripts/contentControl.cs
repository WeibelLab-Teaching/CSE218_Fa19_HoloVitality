using System.Collections;
using System.Collections.Generic;

using UnityEngine;
using TMPro;

public class contentControl : MonoBehaviour
{
    private TextMeshPro textM;
    private int heartRate;
    private int criticalRate=121;
    private int nextUpdate=1;
    // Start is called before the first frame update
    void Start()
    {
        heartRate = 90;
        textM = GetComponent<TextMeshPro>();
        textM.text = "HR: --- /min";
    }

    // Update is called once per frame
    void Update()
    {
        if(Time.time>=nextUpdate){
            heartRate = 90+4*nextUpdate%40;
            if (heartRate>=criticalRate){
                textM.color = new Color32(255,0,0,255);
            }else{
                textM.color = new Color32(255,255,255,255);
            }
            textM.text = "HR: "+heartRate.ToString()+"/min";

            nextUpdate+=1;
        }
        
    }
}
