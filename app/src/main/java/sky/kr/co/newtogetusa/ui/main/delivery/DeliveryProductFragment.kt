package sky.kr.co.newtogetusa.ui.main.delivery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryProductBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.delivery.product.HorizontalSpaceItemDecoration
import sky.kr.co.newtogetusa.ui.main.delivery.product.ItemMoveCallback
import sky.kr.co.newtogetusa.ui.main.delivery.product.ProductPickImageAdapter
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber
import java.io.File
import java.util.UUID
import kotlin.getValue

@AndroidEntryPoint
class DeliveryProductFragment :
    BaseFragment<FragmentDeliveryProductBinding, DeliveryProductViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_product
    override val viewModel: DeliveryProductViewModel by viewModels()

    private val sharedViewModel: DeliveryRequestSharedViewModel by navGraphViewModels(R.id.home)

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var rvAdapter: ProductPickImageAdapter
    private val selectedUris = mutableListOf<Uri>()

    override fun init() {
        super.init()

        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data ?: return@registerForActivityResult
                    val tempPaths = mutableListOf<String>()

                    // 다중 선택
                    data.clipData?.let { clip ->
                        for (i in 0 until clip.itemCount) {
                            val uri = clip.getItemAt(i).uri
                            val path = copyUriToTempFile(requireContext(), uri)
                            tempPaths.add(path)
                        }
                    }

                    // 단일 선택
                    data.data?.let { uri ->
                        val path = copyUriToTempFile(requireContext(), uri)
                        tempPaths.add(path)
                    }

                    // ViewModel 갱신
                    viewModel.addAttachImages(tempPaths)

                    // RecyclerView 갱신
                    rvAdapter.setData(
                        viewModel.attachImagesUrl.value.map { Uri.fromFile(File(it)) }
                    )
                }
            }

        dataBinding.etTitle.doAfterTextChanged {
            viewModel.productTitle.value = it?.toString().orEmpty()
        }

        dataBinding.etDescription.doAfterTextChanged {
            viewModel.productDescription.value = it?.toString().orEmpty()
        }

        rvAdapter = ProductPickImageAdapter(requireContext(), viewModel) { removeUri, position ->
            selectedUris.remove(removeUri)
            rvAdapter.removeItem(position)
        }.apply {
            setGalleryClickListener(object : ProductPickImageAdapter.OnGalleryClickListener {
                override fun onGalleryClick() {
                    openImagePicker()
                }
            })
            setDragListener(object : ProductPickImageAdapter.OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                    itemTouchHelper.startDrag(viewHolder)
                }
            }
            )
        }
        dataBinding.rv.apply {
            adapter = rvAdapter
            addItemDecoration(HorizontalSpaceItemDecoration(8.dpToPx()))
        }
        val callback = ItemMoveCallback(rvAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(dataBinding.rv)

        val categories = mapOf(
            "서류/문서" to "type_1",
            "전자기기" to "type_2",
            "음식/식품" to "type_3",
            "생활/잡화" to "type_4",
            "귀중품" to "type_5",
            "기타/다중" to "type_6"
        )

        categories.forEach { category ->

            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = category.key
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener {
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(
                        context,
                        if (isSelected) R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4
                    )
                    setTextColor(requireContext().getColor(if (isSelected) R.color.primary_100 else R.color.black_80))
                    viewModel.productType.value = category.value
                }
            }
            dataBinding.flProductSort.addView(itemTv)

        }

        val weights = mapOf(
            "가벼움 (~3KG)" to "small",
            "보통 (3~10KG)" to "medium",
            "무거움 (10KG~)" to "big"
        )
        weights.forEach { weight ->
            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = weight.key
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener {
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(
                        context,
                        if (isSelected) R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4
                    )
                    setTextColor(requireContext().getColor(if (isSelected) R.color.primary_100 else R.color.black_80))
                    viewModel.productWeight.value = weight.value
                }
            }
            dataBinding.flProductWeight.addView(itemTv)
        }

        val sizes = mapOf(
            "작음 (작은 상자/에코백 수준)" to "small",
            "중간 (두 손으로 안을 수준)" to "medium",
            "큼 (대형 박스/차량 필요)" to "big"
        )
        sizes.forEach { size ->
            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = size.key
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener {
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(
                        context,
                        if (isSelected) R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4
                    )
                    setTextColor(requireContext().getColor(if (isSelected) R.color.primary_100 else R.color.black_80))
                    viewModel.productVolume.value = size.value
                }
            }
            dataBinding.flProductSize.addView(itemTv)
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryProductViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                DeliveryProductViewModel.Event.SelectedComplete -> {
                    sharedViewModel.updateProductInfo(
                        viewModel.productTitle.value,
                        viewModel.productDescription.value,
                        viewModel.productType.value,
                        viewModel.productWeight.value,
                        viewModel.productVolume.value
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun copyUriToTempFile(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val input = resolver.openInputStream(uri) ?: error("InputStream null")

        val tempFile = File.createTempFile(
            "delivery_${System.currentTimeMillis()}_${UUID.randomUUID()}",
            ".jpg",
            context.cacheDir
        )

        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
        input.close()

        return tempFile.absolutePath
    }
}