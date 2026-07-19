package sky.kr.co.newtogetusa.ui.main.my.profilePage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemProfileDeliveryReqTopBinding
import sky.kr.co.newtogetusa.ui.main.my.ProfileManagementViewModel

class DeliveryReqFilterAdapter(
    private val viewModel: ProfileManagementViewModel
) : RecyclerView.Adapter<DeliveryReqFilterAdapter.FilterVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterVH {
        return FilterVH(
            ItemProfileDeliveryReqTopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilterVH, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    inner class FilterVH(
        private val binding: ItemProfileDeliveryReqTopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvTopAll.isSelected =
                viewModel.topMenuLiveData.value is ProfileManagementViewModel.TopMenu.All
            binding.tvTopDoing.isSelected =
                viewModel.topMenuLiveData.value is ProfileManagementViewModel.TopMenu.Doing
            binding.tvTopEnd.isSelected =
                viewModel.topMenuLiveData.value is ProfileManagementViewModel.TopMenu.End

            binding.tvTopAll.setOnClickListener {
                viewModel.onTopMenuSelect(viewModel.menuAll)
            }
            binding.tvTopDoing.setOnClickListener {
                viewModel.onTopMenuSelect(viewModel.menuDoing)
            }
            binding.tvTopEnd.setOnClickListener {
                viewModel.onTopMenuSelect(viewModel.menuEnd)
            }
        }
    }
}
