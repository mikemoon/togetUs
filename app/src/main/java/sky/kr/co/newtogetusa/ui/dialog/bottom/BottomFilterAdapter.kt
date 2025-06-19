package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemBottomFilterBinding

class BottomFilterAdapter(
    private val list: List<String>,
    private val itemSelectCallback: ((String) -> Unit)? = null
):RecyclerView.Adapter<BottomFilterAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemBottomFilterBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(item = list[position])

    inner class VH(val binding: ItemBottomFilterBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String){
            binding.tv.text = item
            binding.root.setOnClickListener{
                itemSelectCallback?.invoke(item)
            }
        }
    }
}