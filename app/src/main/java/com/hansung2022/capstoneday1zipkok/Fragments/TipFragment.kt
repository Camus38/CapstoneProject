package com.hansung2022.capstoneday1zipkok.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.Tip.TipListActivity
import com.hansung2022.capstoneday1zipkok.databinding.FragmentTipBinding

class TipFragment : Fragment() {
    private var binding: FragmentTipBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTipBinding.inflate(layoutInflater,container,false)


//        binding!!.ftBtAll.setOnClickListener {
//            val intent = Intent(context,TipListActivity::class.java)
//            intent.putExtra("category","categoryAll")
//            startActivity(intent)
//        }

        binding!!.ftBtC1.setOnClickListener {
            val intent = Intent(context, TipListActivity::class.java)
            intent.putExtra("category","content1")
            startActivity(intent)
        }

        binding!!.ftBtC2.setOnClickListener {
            val intent = Intent(context,TipListActivity::class.java)
            intent.putExtra("category","content2")
            startActivity(intent)
        }

        binding!!.ftBtC3.setOnClickListener {
            val intent = Intent(context,TipListActivity::class.java)
            intent.putExtra("category","content3")
            startActivity(intent)
        }

        binding!!.ftBtC4.setOnClickListener {
            val intent = Intent(context,TipListActivity::class.java)
            intent.putExtra("category","content4")
            startActivity(intent)
        }
        binding!!.ftBtC5.setOnClickListener {
            val intent = Intent(context,TipListActivity::class.java)
            intent.putExtra("category","content5")
            startActivity(intent)
        }
        binding!!.ftBtC6.setOnClickListener {
            val intent = Intent(context,TipListActivity::class.java)
            intent.putExtra("category","content6")
            startActivity(intent)
        }


        // Inflate the layout for this fragment
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}