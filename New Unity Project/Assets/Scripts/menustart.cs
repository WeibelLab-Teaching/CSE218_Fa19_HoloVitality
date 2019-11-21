using Microsoft.MixedReality.Toolkit.Input;
using Microsoft.MixedReality.Toolkit;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public class menustart : MonoBehaviour
{
    // Start is called before the first frame update


    public void gotoHeartBeat(){
        Debug.Log("change scene to HearBeat");
        Application.LoadLevel("HeartBeat");
    }

    public void gotoMedicalRecord()
    {
        Debug.Log("change scene to MedicalRecord");
        Application.LoadLevel("MedicalRecord");
    }

    public void gotoStressLevel()
    {
        Debug.Log("change scene to StressLevel");
        Application.LoadLevel("StressLevel");
    }

}
