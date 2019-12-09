using UnityEngine;
using UnityEngine.SceneManagement;



public class menustart : MonoBehaviour
{

    public void gotoHeartBeat(){
        Debug.Log("change scene to HearBeat");
        SceneManager.LoadScene("HeartBeat");
    }


    public void gotoAnalysis()
    {
        Debug.Log("change scene to Analysis");
        SceneManager.LoadScene("Analysis");
    }

    public void goBack()
    {
        Debug.Log("change scene to menuScene");
        SceneManager.LoadScene("menuScene");
    }

}
