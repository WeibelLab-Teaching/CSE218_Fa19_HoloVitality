using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

public class displayIP : MonoBehaviour
{
    // Start is called before the first frame update
    public Transform textMeshObject;
    TextMesh textMesh;

    void Start()
    {
        this.textMesh = this.textMeshObject.GetComponent<TextMesh>();
        this.textMesh.text = "Server IP: "+ PlayerPrefs.GetString("SERVER_IP");
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
