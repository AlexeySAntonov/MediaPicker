package com.aleksejantonov.mediapicker.picker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.SimpleItemAnimator
import com.aleksejantonov.mediapicker.BuildConfig
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.SL
import com.aleksejantonov.mediapicker.base.BaseExpandableBottomSheet
import com.aleksejantonov.mediapicker.base.DiffCalculator
import com.aleksejantonov.mediapicker.base.DiffListItem
import com.aleksejantonov.mediapicker.base.animateVisibility
import com.aleksejantonov.mediapicker.base.createImageFile
import com.aleksejantonov.mediapicker.base.getPxFromDp
import com.aleksejantonov.mediapicker.base.withArguments
import com.aleksejantonov.mediapicker.picker.delegate.CameraCaptureDelegate
import com.aleksejantonov.mediapicker.picker.delegate.MediaItemDelegate
import com.aleksejantonov.mediapicker.picker.delegate.items.MediaItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.dialog_media_picker.*
import timber.log.Timber
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.math.max

class MediaPickerDialogFragment : BaseExpandableBottomSheet(), MediaPickerView {

    override val layoutRes = R.layout.dialog_media_picker
    override val slideOffsetListener: (Float) -> Unit = ::offsetChanges

    private val singleImage: Boolean
        get() = arguments?.getBoolean(SINGLE_IMAGE, true) ?: true

    private val observerId: String
        get() = arguments?.getString(OBSERVER_ID) ?: ""

    private val adapter by lazy { ImagesAdapter() }

    private var lastPhotoUri: Uri? = null
    private var lastPhotoPath: String? = null

    @InjectPresenter
    lateinit var presenter: MediaPickerPresenter

    @ProvidePresenter
    fun providePresenter(): MediaPickerPresenter =
        MediaPickerPresenter(SL.mediaProvider, SL.bottomSheetRouter)
            .apply { putInfo(singleImage, observerId) }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initList()
        done.setOnClickListener { presenter.performDoneAction() }
        closeIcon.setOnClickListener { presenter.onCloseClick() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    if (lastPhotoUri != null && lastPhotoPath != null) {
                        presenter.handlePhotoCapture(lastPhotoUri!!, lastPhotoPath!!)
                    }
                }
            }
        }
    }

    override fun showItems(items: List<DiffListItem>, selectedCount: Int) {
        adapter.items = items
        label.text = if (selectedCount > 0) {
            if (singleImage) {
                resources.getQuantityString(R.plurals.media_picker_select_images_title_plural, selectedCount, selectedCount)
            } else {
                val selectedPhotosCount = items.filter { it is MediaItem && it.type == MediaItem.Type.IMAGE && it.selected }.size
                when {
                    selectedPhotosCount == selectedCount -> resources.getQuantityString(
                        R.plurals.media_picker_select_images_title_plural,
                        selectedCount,
                        selectedCount
                    )
                    selectedPhotosCount != 0             -> resources.getQuantityString(
                        R.plurals.media_picker_select_media_title_plural,
                        selectedCount,
                        selectedCount
                    )
                    else                                 -> resources.getQuantityString(
                        R.plurals.media_picker_select_video_title_plural,
                        selectedCount,
                        selectedCount
                    )
                }
            }
        } else {
            if (singleImage) {
                getText(R.string.media_picker_select_images_title)
            } else {
                getText(R.string.media_picker_select_media_title)
            }
        }
        done.animateVisibility(selectedCount > 0)
        count.animateVisibility(selectedCount > 0)
        count.text = selectedCount.toString()
    }

    override fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            context?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    val photoFile = try {
                        createImageFile(requireContext())
                    } catch (e: IOException) {
                        Timber.e("Image file could not be created: $e")
                        null
                    } catch (e: IllegalStateException) {
                        Timber.e("Context require error: $e")
                        null
                    }

                    photoFile?.also { file ->
                        try {
                            lastPhotoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${BuildConfig.APPLICATION_ID}.fileprovider",
                                file
                            )
                            lastPhotoPath = file.absolutePath
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoUri)
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        } catch (e: IllegalArgumentException) {
                            Timber.e("The path not supported by the provider: $e")
                        }
                    }
                }
            }
        }
    }

    private fun initList() {
        with(recyclerView) {
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter = this@MediaPickerDialogFragment.adapter
        }
    }

    private fun offsetChanges(offset: Float) {
        substrate.alpha = max(offset, 0.8f)
        closeIcon.alpha = max(0f, offset)
        divider.alpha = max(0f, offset)
        label.translationX = context?.getPxFromDp((max(offset * 40, 0f)).toInt())?.toFloat() ?: 0f
        if (offset == 1f) {
            substrate.setBackgroundResource(R.drawable.background_square_white)
        } else {
            substrate.setBackgroundResource(R.drawable.background_top_rounded_white_8dp)
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 3581
        private const val SINGLE_IMAGE = "SINGLE_IMAGE"
        private const val OBSERVER_ID = "OBSERVER_ID"

        fun newInstance(singleImage: Boolean, observerId: String) = MediaPickerDialogFragment().withArguments {
            putBoolean(SINGLE_IMAGE, singleImage)
            putString(OBSERVER_ID, observerId)
        }
    }

    private inner class ImagesAdapter : ListDelegationAdapter<List<DiffListItem>>() {
        init {
            delegatesManager.apply {
                addDelegate(MediaItemDelegate(presenter::onMediaClick))
                addDelegate(CameraCaptureDelegate(presenter::onCameraClick))
            }
        }

        override fun setItems(newItems: List<DiffListItem>) {
            if (items == null) {
                items = newItems
                notifyDataSetChanged()
            } else {
                val diffResult = DiffUtil.calculateDiff(DiffCalculator(items, newItems))
                diffResult.dispatchUpdatesTo(this)
                items = newItems
            }
        }
    }
}