using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class testButton : MonoBehaviour
{
    public void LoadHeartbeat()
    {
        Debug.Log("change heartbeat");
        Application.LoadLevel("HeartBeat");
    }
}
