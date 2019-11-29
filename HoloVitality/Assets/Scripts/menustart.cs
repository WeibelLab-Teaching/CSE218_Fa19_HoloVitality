using UnityEngine;
using UnityEngine.SceneManagement;



public class menustart : MonoBehaviour
{

    public void gotoHeartBeat(){
        Debug.Log("change scene to HearBeat");
        SceneManager.LoadScene("HeartBeat");
    }

    public void gotoMedicalRecord()
    {
        Debug.Log("change scene to MedicalRecord");
        SceneManager.LoadScene("MedicalRecord");
    }

    public void gotoStressLevel()
    {
        Debug.Log("change scene to StressLevel");
        SceneManager.LoadScene("StressLevel");
    }

    public void goBack()
    {
        Debug.Log("change scene to menuScene");
        SceneManager.LoadScene("menuScene");
    }

}
