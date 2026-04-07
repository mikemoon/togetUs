package sky.kr.co.newtogetusa.ui.main.search.player

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryAreaRequirementSetBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryAreaRequirementSetFragment : BaseFragment<FragmentDeliveryAreaRequirementSetBinding, DeliveryAreaRequirementSetViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_area_requirement_set
    override val viewModel: DeliveryAreaRequirementSetViewModel by viewModels()

    private var departSelection: DeliveryAreaSelection? = null
    private var destSelection: DeliveryAreaSelection? = null

    override fun init() {
        super.init()

        findNavController().currentBackStackEntry?.savedStateHandle?.get<DeliveryAreaSelection>(
            DeliveryAreaLocationSearchFragment.KEY_DEPART_RESULT
        )?.let {
            departSelection = it
            renderSelection()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.get<DeliveryAreaSelection>(
            DeliveryAreaLocationSearchFragment.KEY_DEST_RESULT
        )?.let {
            destSelection = it
            renderSelection()
        }

        dataBinding.tvDepartArea.setOnClickListener {
            findNavController().navigate(
                DeliveryAreaRequirementSetFragmentDirections
                    .actionDeliveryAreaRequirementSetFragmentToDeliveryAreaLocationSearchFragment(true)
            )
        }

        dataBinding.tvDestArea.setOnClickListener {
            findNavController().navigate(
                DeliveryAreaRequirementSetFragmentDirections
                    .actionDeliveryAreaRequirementSetFragmentToDeliveryAreaLocationSearchFragment(false)
            )
        }

        dataBinding.tvDone.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable(KEY_DEPART_SELECTION, departSelection)
                putParcelable(KEY_DEST_SELECTION, destSelection)
                putBoolean(KEY_MY_AREA, dataBinding.sw.isChecked)
            }
            parentFragmentManager.setFragmentResult(KEY_AREA_REQUIREMENT_RESULT, bundle)
            findNavController().popBackStack()
        }
    }

    override fun initObserver() {
        super.initObserver()

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<DeliveryAreaSelection>(DeliveryAreaLocationSearchFragment.KEY_DEPART_RESULT)
            ?.observe(viewLifecycleOwner, Observer { result ->
                departSelection = result
                renderSelection()
            })

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<DeliveryAreaSelection>(DeliveryAreaLocationSearchFragment.KEY_DEST_RESULT)
            ?.observe(viewLifecycleOwner, Observer { result ->
                destSelection = result
                renderSelection()
            })

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                DeliveryAreaRequirementSetViewModel.Event.Close ->{
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun renderSelection() {
        dataBinding.tvDepartArea.text = departSelection?.name ?: "지역을 선택해 주세요"
        dataBinding.tvDestArea.text = destSelection?.name ?: "지역을 선택해 주세요"
    }

    companion object {
        const val KEY_AREA_REQUIREMENT_RESULT = "delivery_area_requirement_result"
        const val KEY_DEPART_SELECTION = "depart_selection"
        const val KEY_DEST_SELECTION = "dest_selection"
        const val KEY_MY_AREA = "my_area"
    }
}