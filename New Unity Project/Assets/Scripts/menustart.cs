using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class menustart : MonoBehaviour
{
    // Start is called before the first frame update
    public void changeMenuScene(string scenename){
        Application.LoadLevel(scenename);
    }
}
