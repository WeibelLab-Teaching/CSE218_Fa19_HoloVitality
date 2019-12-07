using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class GetIP : MonoBehaviour
{
    public Transform textMeshObject;
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
                    PlayerPrefs.SetString("SERVER_IP", result);
                    SceneManager.LoadScene("menuScene");
                },
                false);
            },
            TimeSpan.FromSeconds(60));
#else
        string SERVER_IP = "100.80.225.222";
        PlayerPrefs.SetString("SERVER_IP", SERVER_IP);
        SceneManager.LoadScene("menuScene");
#endif
    }
    
}
