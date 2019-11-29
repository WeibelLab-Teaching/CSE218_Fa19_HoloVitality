using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;


public class NameInput : MonoBehaviour
{
    // Start is called before the first frame update
    // Start is called before the first frame update
    private TextMeshPro textM;
    public string placeHolder;
    
    // Start is called before the first frame update
    void Start()
    {
        placeHolder = "Name: John Doe";
        textM = GetComponent<TextMeshPro>();
        
        textM.text = placeHolder;
    }

    // Update is called once per frame
    
}
