using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class GetIP : MonoBehaviour
{
    public Transform textMeshObject;
    private string SERVER_IP = "";
    TextMesh textMesh;

    private void Start()
    {
        this.textMesh = this.textMeshObject.GetComponent<TextMesh>();
        this.textMesh.text = "Scanning for a QRCode";

#if !UNITY_EDITOR
        MediaFrameQrProcessing.Wrappers.ZXingQrCodeScanner.ScanFirstCameraForQrCode(
            result =>
            {
              UnityEngine.WSA.Application.InvokeOnAppThread(() =>
              {
                this.textMesh.text = $"Got result {result} at {DateTime.Now}";
                SERVER_IP = this.textMesh.text;
                PlayerPrefs.SetString("SERVER_IP", SERVER_IP);
                Application.LoadLevel("menuScene");
              }, 
              false);
            },
            null);
#endif
        SERVER_IP = "192.168.1.3";
        PlayerPrefs.SetString("SERVER_IP", SERVER_IP);
        SceneManager.LoadScene("menuScene");
    }

    public void OnReset()
    {
        this.textMesh.text = "say scan or run to start";
    }
    
}
