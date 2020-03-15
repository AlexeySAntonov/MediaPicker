package com.aleksejantonov.mediapicker.base

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class RxPresenter<View : MvpView> : MvpPresenter<View>() {

    private val destroyedPresenterSubscriptions = CompositeDisposable()
    private val destroyedViewSubscriptions = CompositeDisposable()
    private val detachedViewSubscriptions = CompositeDisposable()
    private val reloadSubscriptions = CompositeDisposable()
    val viewActions: PublishRelay<Any> = PublishRelay.create()

    fun viewAction(action: Any) {
        viewActions.accept(action)
    }

    protected fun Disposable.keepUntilPresenterDestroyed() {
        destroyedPresenterSubscriptions.add(this)
    }

    protected fun Disposable.keepUntilViewDestroyed() {
        destroyedViewSubscriptions.add(this)
    }

    protected fun Disposable.keepUntilViewDetached() {
        detachedViewSubscriptions.add(this)
    }

    protected fun Disposable.keepUntilReload() {
        reloadSubscriptions.add(this)
    }

    protected fun releaseDestroyedPresenterSubscriptions() {
        destroyedPresenterSubscriptions.clear()
    }

    protected fun releaseDestroyedViewSubscriptions() {
        destroyedViewSubscriptions.clear()
    }

    protected fun releaseDetachedViewSubscriptions() {
        detachedViewSubscriptions.clear()
    }

    protected fun releaseReloadSubscriptions() {
        reloadSubscriptions.clear()
    }

    override fun detachView(view: View) {
        detachedViewSubscriptions.clear()
        super.detachView(view)
    }

    override fun destroyView(view: View) {
        destroyedViewSubscriptions.clear()
        super.destroyView(view)
    }

    override fun onDestroy() {
        destroyedPresenterSubscriptions.clear()
        reloadSubscriptions.clear()
        super.onDestroy()
    }

}