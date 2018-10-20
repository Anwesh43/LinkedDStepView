package ui.anwesome.com.dstepview

/**
 * Created by anweshmishra on 20/10/18.
 */

import android.view.MotionEvent
import android.view.View
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.content.Context

val nodes : Int = 5

fun Canvas.drawDStepView(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val r : Float = gap / 3
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#673AB7")
    save()
    translate(i * gap + gap, h/2)
    for (j in 0..1) {
        val sc : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f * j)) * 2
        val sf : Float = 1f - 2 * j
        save()
        scale(1f, sf)
        drawLine(0f, r - r * sc, 0f, r, paint)
        drawArc(RectF(-r, -r, r, r), 90f - 90f * sc, 90f * sc, false, paint)
        restore()
    }
    restore()
}