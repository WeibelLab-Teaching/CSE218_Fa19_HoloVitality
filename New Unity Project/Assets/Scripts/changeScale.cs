using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
public class changeScale : MonoBehaviour
{
     public float x = 0.1f;

     public float nextUpdate = 0.5f;
     private Vector3 orgPos;
     private Vector3 orgScale;
     void Start(){
        orgPos = transform.position;
        orgScale = transform.localScale;
     }
     void Update(){
         if(Time.time>=nextUpdate){
            x = (float) Math.Sin(Time.time/10);
            transform.localScale = orgScale+ new Vector3(x,0,0);
            transform.position = orgPos + new Vector3(x/2,0,0);
            nextUpdate+=0.2f;
        }
         
     }

}
