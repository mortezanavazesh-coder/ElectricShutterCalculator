package com.morteza.shuttercalculator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morteza.shuttercalculator.utils.PrefsHelper

// مدل داده قیمت‌های پایه
data class BasePrices(
    val blades: List<String> = emptyList(),
    val motors: List<String> = emptyList(),
    val shafts: List<String> = emptyList(),
    val boxes: List<String> = emptyList(),
    val installBase: Float = 0f,
    val weldingBase: Float = 0f,
    val transportBase: Float = 0f,
    val extras: Map<String, Float> = emptyMap()
)

class MainViewModel : ViewModel() {

    private val _basePrices = MutableLiveData<BasePrices>()
    val basePrices: LiveData<BasePrices> get() = _basePrices

    // بارگذاری داده‌ها از PrefsHelper
    fun reloadFromPrefs(context: Context) {
        val blades = PrefsHelper.getSortedOptionList(context, "تیغه")
        val motors = PrefsHelper.getSortedOptionList(context, "موتور")
        val shafts = PrefsHelper.getSortedOptionList(context, "شفت")
        val boxes = PrefsHelper.getSortedOptionList(context, "قوطی")

        val installBase = PrefsHelper.getFloat(context, "install_base")
        val weldingBase = PrefsHelper.getFloat(context, "welding_base")
        val transportBase = PrefsHelper.getFloat(context, "transport_base")

        val extras = PrefsHelper.getAllExtraOptions(context)

        _basePrices.value = BasePrices(
            blades = blades,
            motors = motors,
            shafts = shafts,
            boxes = boxes,
            installBase = installBase,
            weldingBase = weldingBase,
            transportBase = transportBase,
            extras = extras
        )
    }
}
