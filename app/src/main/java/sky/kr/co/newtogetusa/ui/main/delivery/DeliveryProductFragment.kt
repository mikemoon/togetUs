package sky.kr.co.newtogetusa.ui.main.delivery

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

@AndroidEntryPoint
class DeliveryProductFragment : BaseFragment<FragmentDeliveryProductBinding, DeliveryProductViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_product
    override val viewModel: DeliveryProductViewModel by viewModels()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var rvAdapter: ProductPickImageAdapter
    private val selectedUris = mutableListOf<Uri>()

    override fun init() {
        super.init()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uris = mutableListOf<Uri>()

                data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                }

                data?.data?.let { uri ->
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    uris.add(uri)
                }

                selectedUris.clear()
                selectedUris.addAll(uris)
                Timber.d("selectedUris : $selectedUris")
                (dataBinding.rv.adapter as ProductPickImageAdapter).addItems(selectedUris.toList())
            }
        }

        rvAdapter = ProductPickImageAdapter(requireContext()){ removeUri, position ->
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

        val categories = listOf("서류/문서", "전자기기", "음식/식품", "생활/잡화", "귀중품", "기타/다중")
        categories.forEach { category ->

            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = category
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener{
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(context, if(isSelected)R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4)
                    setTextColor(requireContext().getColor(if(isSelected)R.color.primary_100 else R.color.black_80))
                }
            }
            dataBinding.flProductSort.addView(itemTv)

        }

        val weights = listOf("가벼움 (~3KG)", "보통 (3~10KG)", "무거움 (10KG~)")
        weights.forEach { weight ->
            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = weight
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener{
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(context, if(isSelected)R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4)
                    setTextColor(requireContext().getColor(if(isSelected)R.color.primary_100 else R.color.black_80))
                }
            }
            dataBinding.flProductWeight.addView(itemTv)
        }

        val sizes = listOf("작음 (작은 상자/에코백 수준)", "중간 (두 손으로 안을 수준)", "큼 (대형 박스/차량 필요)")
        sizes.forEach { size ->
            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = size
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener{
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(context, if(isSelected)R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4)
                    setTextColor(requireContext().getColor(if(isSelected)R.color.primary_100 else R.color.black_80))
                }
            }
            dataBinding.flProductSize.addView(itemTv)
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                DeliveryProductViewModel.Event.Back ->{
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
}