package com.aleksejantonov.mediapicker.picker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.aleksejantonov.mediapicker.BuildConfig
import com.aleksejantonov.mediapicker.R
import com.aleksejantonov.mediapicker.SL
import com.aleksejantonov.mediapicker.base.ui.BaseExpandableBottomSheet
import com.aleksejantonov.mediapicker.base.ui.DiffListItem
import com.aleksejantonov.mediapicker.base.animateVisibility
import com.aleksejantonov.mediapicker.base.createImageFile
import com.aleksejantonov.mediapicker.base.getPxFromDp
import com.aleksejantonov.mediapicker.base.withArguments
import com.aleksejantonov.mediapicker.picker.adapter.delegate.items.GalleryMediaItem
import kotlinx.android.synthetic.main.dialog_media_picker.*
import timber.log.Timber
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.math.max

class MediaPickerDialogFragment : BaseExpandableBottomSheet() {

  override val layoutRes = R.layout.dialog_media_picker
  override val slideOffsetListener: (Float) -> Unit = ::offsetChanges

  private val singleImage: Boolean
    get() = arguments?.getBoolean(SINGLE_IMAGE, true) ?: true

  private val limit: Int
    get() = arguments?.getInt(LIMIT) ?: 1

//  private val adapter by lazy { ImagesAdapter() }

  private var lastPhotoUri: Uri? = null
  private var lastPhotoPath: String? = null

  /**
   * Just a quick solution, use respective [ViewModelProvider.Factory] provided by the Dagger or somehow else.
   */
  private val viewModel by viewModels<MediaPickerViewModel> {
    object : ViewModelProvider.Factory {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MediaPickerViewModel(
          mediaProvider = SL.mediaProvider,
          bottomSheetRouter = SL.bottomSheetRouter,
          singleImage = singleImage,
          limit = limit
        ) as T
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initList()
    done.setOnClickListener { viewModel.performDoneAction() }
    closeIcon.setOnClickListener { viewModel.onCloseClick() }
//    viewModel.content.observe(this, {
//      if (adapter.items.isNullOrEmpty()) {
//        /**
//         * Avoid bottom sheet glitches.
//         * TODO: Use custom view or animate regular fragment instead of using the AppCompatDialogFragment with default BottomSheetBehavior
//         */
//        recyclerView.postDelayed({ showItems(it) }, 120L)
//      } else {
//        showItems(it)
//      }
//    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      when (requestCode) {
        REQUEST_IMAGE_CAPTURE -> {
          if (lastPhotoUri != null && lastPhotoPath != null) {
            viewModel.handlePhotoCapture(lastPhotoUri!!, lastPhotoPath!!)
          }
        }
      }
    }
  }

  private fun showItems(items: List<DiffListItem>) {
//    adapter.items = items
    val selectedCount = items.count { it is GalleryMediaItem && it.selected }
    label.text = if (selectedCount > 0) {
      if (singleImage) {
        resources.getQuantityString(
          R.plurals.media_picker_select_images_title_plural,
          selectedCount,
          selectedCount
        )
      } else {
        val selectedPhotosCount = items.filter { it is GalleryMediaItem && !it.isVideo && it.selected }.size
        when {
          selectedPhotosCount == selectedCount -> resources.getQuantityString(
            R.plurals.media_picker_select_images_title_plural,
            selectedCount,
            selectedCount
          )
          selectedPhotosCount != 0 -> resources.getQuantityString(
            R.plurals.media_picker_select_media_title_plural,
            selectedCount,
            selectedCount
          )
          else -> resources.getQuantityString(
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
  }

  fun dispatchTakePictureIntent() {
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
//      adapter = this@MediaPickerDialogFragment.adapter
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

//  private inner class ImagesAdapter : ListDelegationAdapter<List<DiffListItem>>() {
//    init {
//      delegatesManager.apply {
//        addDelegate(MediaItemDelegate(viewModel::onMediaClick))
//        addDelegate(CameraCaptureDelegate(::dispatchTakePictureIntent))
//      }
//    }
//
//    override fun setItems(newItems: List<DiffListItem>) {
//      if (items == null) {
//        items = newItems
//        notifyDataSetChanged()
//      } else {
//        val diffResult = DiffUtil.calculateDiff(DiffCalculator(items, newItems))
//        diffResult.dispatchUpdatesTo(this)
//        items = newItems
//      }
//    }
//  }

  companion object {
    const val REQUEST_IMAGE_CAPTURE = 3581
    private const val SINGLE_IMAGE = "SINGLE_IMAGE"
    private const val LIMIT = "LIMIT"

    fun newInstance(singleImage: Boolean, limit: Int) =
      MediaPickerDialogFragment().withArguments {
        putBoolean(SINGLE_IMAGE, singleImage)
        putInt(LIMIT, limit)
      }
  }

}