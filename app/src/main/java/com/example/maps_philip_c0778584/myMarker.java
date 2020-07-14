package com.example.maps_philip_c0778584;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Vector;

public class myMarker {

    public static int orientation(LatLng p1, LatLng p2,
                                  LatLng p3)
    {

        double val = (p2.longitude - p1.longitude) * (p3.latitude - p2.latitude) -
                (p2.latitude - p1.latitude) * (p3.longitude - p2.longitude);

        if (val == 0) return 0;


        return (val > 0)? 1: 2;
    }
    public static Vector<LatLng> convexHull(LatLng markers[], int n)
    {

        Vector<LatLng> hull = new Vector<LatLng>();


        if (n < 3){
            hull.addAll(Arrays.asList(markers));
            return hull;
        }




        int l = 0;
        for (int i = 1; i < n; i++)
            if (markers[i].latitude < markers[l].latitude)
                l = i;


        int p = l, q;
        do
        {

            hull.add(markers[p]);


            q = (p + 1) % n;

            for (int i = 0; i < n; i++)
            {

                if (orientation(markers[p], markers[i], markers[q])
                        == 2)
                    q = i;
            }


            p = q;

        } while (p != l);



        return hull;
    }


}
