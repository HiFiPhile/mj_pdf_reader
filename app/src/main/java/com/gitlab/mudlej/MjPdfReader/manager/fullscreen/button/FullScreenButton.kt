import android.app.Activity
import android.view.View
import com.google.android.material.button.MaterialButton


abstract class FullScreenButton(
    private val activity: Activity,
    private val button: MaterialButton
) {

    abstract fun getLabel(): String

    private var visible = true

    fun showLabel() {
        visible = true
        button.text = getLabel()
    }

    fun hideLabel() {
        visible = false
        button.text = ""
    }

    fun toggleLabel() {
        if (visible) hideLabel() else showLabel()
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        button.setOnClickListener(listener)
    }

}