package com.morteza.shuttercalculator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morteza.shuttercalculator.utils.PrefsHelper

data class BasePrices(
    val blades: List<String>,
    val motors: List<String>,
    val shafts: List<String>,
    val boxes: List<String>,
    val installBase: Float,
    val weldingBase: Float,
    val transportBase: Float,
    val extras: Map<String, Float>
)

class MainViewModel : ViewModel() {

    private val _basePrices = MutableLiveData<BasePrices>()
    val basePrices: LiveData<BasePrices> = _basePrices

    fun reloadFromPrefs(context: Context) {
        val blades = PrefsHelper.getSortedOptionList(context, "تیغه")
        val motors = PrefsHelper.getSortedOptionList(context, "موتور")
        val shafts = PrefsHelper.getSortedOptionList(context, "شفت")
        val boxes = PrefsHelper.getSortedOptionList(context, "قوطی")
        val installBase = PrefsHelper.getFloat(context, "install_base")
        val weldingBase = PrefsHelper.getFloat(context, "welding_base")
        val transportBase = PrefsHelper.getFloat(context, "transport_base")
        val extras = PrefsHelper.getAllExtraOptions(context) // Map<String, Float>

        _basePrices.postValue(
            BasePrices(
                blades = blades,
                motors = motors,
                shafts = shafts,
                boxes = boxes,
                installBase = installBase,
                weldingBase = weldingBase,
                transportBase = transportBase,
                extras = extras
            )
        )
    }
}
