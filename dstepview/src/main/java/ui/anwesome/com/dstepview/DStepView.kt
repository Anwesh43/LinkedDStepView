package ui.anwesome.com.dstepview

/**
 * Created by anweshmishra on 20/10/18.
 */

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.content.Context

val nodes : Int = 5

fun Canvas.drawDStep(i : Int, scale : Float, paint : Paint) {
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

class DStepView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator (var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class DStepNode(var i : Int, val state : State = State()) {
        private var next : DStepNode? = null
        private var prev : DStepNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = DStepNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawDStep(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : DStepNode {
            var curr : DStepNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class DStep(var i : Int) {
        private var curr : DStepNode = DStepNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : DStepView) {

        private val dstep : DStep = DStep(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            dstep.draw(canvas, paint)
            animator.animate {
                dstep.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            dstep.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : DStepView {
            val view : DStepView = DStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}