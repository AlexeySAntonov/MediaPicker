package com.aleksejantonov.mediapicker.picker

import com.aleksejantonov.mediapicker.base.DiffListItem
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface MediaPickerView : MvpView {
    fun showItems(items: List<DiffListItem>, selectedCount: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun dispatchTakePictureIntent()
}