package com.example.drivers_app.directions_helpers

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.text.SpannableStringBuilder
import android.util.Log
import com.example.drivers_app.MainActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_available_order_dialog.*
import org.json.JSONObject
import java.util.*

/**
 * Created by Vishal on 10/20/2018.
 */

internal class PointsParser(mContext: Context, private val directionMode: String) :
        AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
    private val context: Context = mContext

    private val taskCallback: TaskLoadedCallback = mContext as TaskLoadedCallback

    private var routeData: String = ""

    // Parsing the data in non-ui thread
    override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {
        val jObject: JSONObject
        var routes: List<List<HashMap<String, String>>>? = null

        try {
            jObject = JSONObject(jsonData[0])
            Log.d("myLog", jsonData[0])
            val parser = DataParser()
            Log.d("myLog", parser.toString())

            // Starts parsing data
            routes = parser.parse(jObject)
            routeData = parser.routeData
            Log.d("myLog", "Executing routes")
            Log.d("myLog", routes.toString())

        } catch (e: Exception) {
            Log.d("myLog", e.toString())
            e.printStackTrace()
        }

        return routes
    }

    // Executes in UI thread, after the parsing process
    override fun onPostExecute(result: List<List<HashMap<String, String>>>) {
        var points: ArrayList<LatLng>
        var lineOptions: PolylineOptions? = null
        // Traversing through all the routes
        for (i in result.indices) {
            points = ArrayList()
            lineOptions = PolylineOptions()
            // Fetching i-th route
            val path = result[i]
            // Fetching all the points in i-th route
            for (j in path.indices) {
                val point = path[j]
                val lat = java.lang.Double.parseDouble(Objects.requireNonNull<String>(point["lat"]))
                val lng = java.lang.Double.parseDouble(Objects.requireNonNull<String>(point["lng"]))
                val position = LatLng(lat, lng)
                points.add(position)
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points)
            if (directionMode.equals("walking", ignoreCase = true)) {
                lineOptions.width(10f)
                lineOptions.color(Color.MAGENTA)
            } else {
                lineOptions.width(20f)
                lineOptions.color(Color.BLUE)
            }
            Log.d("myLog", "onPostExecute lineOptions decoded")
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            //mMap.addPolyline(lineOptions);
            taskCallback.onTaskDone(lineOptions)
            if (routeData.isNotEmpty()) {
                val activity = context as MainActivity
                val routeDataArray = routeData.split(' ')
                val routeDistance = Math.ceil(routeDataArray[0].toDouble() / 1000)
                val routeTime = Math.ceil(routeDataArray[1].toDouble() / 60)
                activity.runOnUiThread {
                    activity.availableOrderDialog?.toOfferTime!!.text = SpannableStringBuilder(routeTime.toString() + "min")
                    activity.availableOrderDialog?.toOfferDistance!!.text = SpannableStringBuilder(routeDistance.toString() + "km")
                }
            }
        } else {
            Log.d("myLog", "without PolyLines drawn")
        }
    }
}