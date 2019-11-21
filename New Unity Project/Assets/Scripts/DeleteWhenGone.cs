using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class DeleteWhenGone : MonoBehaviour
{
    // Start is called before the first frame update
    [Tooltip("how far down can it go before getting destroyed")]
    public float maxDistance = 100f;

    // Update is called once per frame
    void Update()
    {
        if(this.transform.position.y<= -maxDistance){
            Destroy(this.gameObject);
        }
    }
}
