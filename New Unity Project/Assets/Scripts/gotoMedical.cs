using Microsoft.MixedReality.Toolkit.Input;
using Microsoft.MixedReality.Toolkit;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public class gotoMedical : MonoBehaviour
{
    // Start is called before the first frame update

    public void OnInputClicked()
    {
        Debug.Log("change scene medical");
        Application.LoadLevel("MedicalRecord");
    }
}
