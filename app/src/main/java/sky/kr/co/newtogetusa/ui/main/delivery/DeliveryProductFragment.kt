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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryProductBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.delivery.product.HorizontalSpaceItemDecoration
import sky.kr.co.newtogetusa.ui.main.delivery.product.ItemMoveCallback
import sky.kr.co.newtogetusa.ui.main.delivery.product.ProductPickImageAdapter
import sky.kr.co.newtogetusa.utils.FileUtil.copyUriToTempFile
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

    private val sharedViewModel: DeliveryRequestSharedViewModel by navGraphViewModels(R.id.nav_graph)

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var rvAdapter: ProductPickImageAdapter
    private val selectedUris = mutableListOf<Uri>()

    private val categoryViews = mutableMapOf<String, AppCompatTextView>()
    private val weightViews = mutableMapOf<String, AppCompatTextView>()
    private val sizeViews = mutableMapOf<String, AppCompatTextView>()

    private var latestState: DeliveryRequestState? = null

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

                    val imageUrlList = viewModel.attachImagesUrl.value.map { Uri.fromFile(File(it)) }
                    // RecyclerView 갱신
                    rvAdapter.setData(
                        imageUrlList
                    )
                    sharedViewModel.attachImagesUrl.value = imageUrlList
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
            sharedViewModel.attachImagesUrl.value = sharedViewModel.attachImagesUrl.value - removeUri
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

        rvAdapter.setData(
            sharedViewModel.attachImagesUrl.value
        )
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){

                launch {
                    viewModel.productTypes.collect {
                        createSelectableItems(
                            dataBinding.flProductSort,
                            it,
                            categoryViews
                        ) { code -> viewModel.productType.value = code }
                        latestState?.productType?.let {
                            selectSingle(categoryViews, it)
                            viewModel.productType.value = it
                        }
                    }
                }

                launch {
                    viewModel.productWeights.collect {
                        createSelectableItems(
                            dataBinding.flProductWeight,
                            it,
                            weightViews
                        ) { code -> viewModel.productWeight.value = code }
                        latestState?.productWeight?.let {
                            selectSingle(weightViews, it)
                            viewModel.productWeight.value = it
                        }
                    }
                }

                launch {
                    viewModel.productVolumes.collect {
                        createSelectableItems(
                            dataBinding.flProductSize,
                            it,
                            sizeViews
                        ) { code -> viewModel.productVolume.value = code }
                        latestState?.productVolume?.let {
                            selectSingle(sizeViews, it)
                            viewModel.productVolume.value = it
                        }
                    }
                }

                launch {
                    sharedViewModel.state.collect { state ->
                        Timber.d("sharedState $state")
                        latestState = state
                        state.productTitle?.let {
                            dataBinding.etTitle.setText(it)
                            viewModel.productTitle.value = it
                        }
                        state.productDescription?.let {
                            dataBinding.etDescription.setText(it)
                            viewModel.productDescription.value = it
                        }
                        state.productType?.let {
                            selectSingle(categoryViews, it)
                            viewModel.productType.value = it
                        }

                        state.productWeight?.let {
                            selectSingle(weightViews, it)
                            viewModel.productWeight.value = it
                        }

                        state.productVolume?.let {
                            selectSingle(sizeViews, it)
                            viewModel.productVolume.value = it
                        }
                    }
                }

            }
        }
    }

    private fun selectSingle(
        views: Map<String, AppCompatTextView>,
        selectedKey: String
    ) {
        views.forEach { (key, tv) ->
            val selected = key == selectedKey
            tv.isSelected = selected
            tv.background = ContextCompat.getDrawable(
                tv.context,
                if (selected) R.drawable.background_st_p60_s_p10_r4
                else R.drawable.background_s_b5_r4
            )
            tv.setTextColor(
                ContextCompat.getColor(
                    tv.context,
                    if (selected) R.color.primary_100 else R.color.black_80
                )
            )
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

    private fun createSelectableItems(
        container: FlexboxLayout,
        items: List<BaseCommonDto>,
        viewMap: MutableMap<String, AppCompatTextView>,
        onSelect: (String) -> Unit
    ) {
        container.removeAllViews()
        viewMap.clear()

        items.forEach { dto ->
            val tv = AppCompatTextView(requireContext()).apply {
                text = dto.name
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(ContextCompat.getColor(context, R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener {
                    selectSingle(viewMap, dto.code)
                    onSelect(dto.code)
                }
            }

            viewMap[dto.code] = tv
            container.addView(tv)
        }
    }
}