package com.hansung2022.capstoneday1zipkok.Todo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hansung2022.capstoneday1zipkok.databinding.DialogAddtodoBinding

class CustomDialog : DialogFragment() {
    private var _binding: DialogAddtodoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogAddtodoBinding.inflate(inflater, container, false)
        val view = binding.root
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 각 버튼 클릭 시 각각의 함수 호출
        binding.dialBtnCheck.setOnClickListener {
            val msg = binding.diaEtDolist.text.toString()
            buttonClickListener.onButton1Clicked(msg) //아래 인터페이스에서 만든 함수 인자 이렇게 전달
            dismiss()
        }
        binding.dialBtnCancel.setOnClickListener {
            dismiss()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 인터페이스
    interface OnButtonClickListener {
        fun onButton1Clicked(msg: String) //액티비티에 인자 전달을 위해 String 타입인자 넣어서 보내줌

    }

    // 클릭 이벤트 설정
    fun setButtonClickListener(buttonClickListener: OnButtonClickListener) {
        this.buttonClickListener = buttonClickListener
    }

    // 클릭 이벤트 실행
    private lateinit var buttonClickListener: OnButtonClickListener

}
