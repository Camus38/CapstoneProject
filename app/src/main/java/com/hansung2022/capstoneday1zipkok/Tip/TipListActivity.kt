package com.hansung2022.capstoneday1zipkok.Tip

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ActivityTipListBinding

class TipListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipListBinding
    private val database = Firebase.database
    private val items = ArrayList<TipModel>()
    private lateinit var rvAdapter: TipRvAdapter
    private lateinit var dialog: AlertDialog //중분류 다이얼로그 닫는 목적
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTipListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        val uid = auth.currentUser!!.uid


        rvAdapter = TipRvAdapter(baseContext, items, uid)
        binding.tipRecyclerView.adapter = rvAdapter
        binding.tipRecyclerView.layoutManager = LinearLayoutManager(this)


        val myRef1 = database.getReference("contentContainer").child("content1")
        val myRef2 = database.getReference("contentContainer").child("content2")
        val myRef3 = database.getReference("contentContainer").child("content3")
        val myRef4 = database.getReference("contentContainer").child("content4")
        val myRef5 = database.getReference("contentContainer").child("content5")
        val myRef6 = database.getReference("contentContainer").child("content6")


        //tipFragment로부터 받아온 category분류값
        val category = intent.getStringExtra("category")



        //중분류 리스트
        var middleList = arrayOf("")
        val foodMiddleList = arrayOf("전체", "한식", "중식", "일식", "양식", "분식")
        val homeMiddleList = arrayOf("전체", "빨래", "청소", "가전")
        val petMiddleList = arrayOf("전체", "강아지", "고양이", "소동물")
        val investmentMiddleList = arrayOf("전체", "주식", "가상화폐")
        val contractMiddleList = arrayOf("전체", "계약", "하자보수")
        val lifeMiddleList = arrayOf("전체", "독서", "운동")


        when (category) {
            //받은 카테고리 따라서
            "content1" -> {
                middleList = foodMiddleList

            }
            "content2" -> {
                middleList = homeMiddleList

            }
            "content3" -> {
                middleList = petMiddleList

            }
            "content4" -> {
                middleList = investmentMiddleList

            }
            "content5" -> {
                middleList = contractMiddleList

            }
            "content6" -> {
                middleList = lifeMiddleList

            }
        }


        //다이얼로그 선택 이후 위치저장, 처음 입장시에는 아무것도 선택 안 되어 있게 -1
        var exCheckedItemPosition = -1
        var exCheckedItemString = ""

        //다이얼로그로 분류 해주는 함수
        fun showSelectAlertDialog() {
            AlertDialog.Builder(this).run {
                setIcon(R.drawable.mycategory)
                setTitle("선택하세요")
                setSingleChoiceItems(middleList,
                    exCheckedItemPosition,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            //전체 검색 일 경우
                            if (middleList[which].equals("전체")) {
                                items.clear()
                                for (item in middleList) {
                                    tipReadFromFireBase(database.getReference("contentContainer")
                                        .child(category.toString()).child(item))
                                    Log.d("itemshow", "$item clicked all")
                                }
                                exCheckedItemPosition = which
                                exCheckedItemString = middleList[which]

                                dialog?.dismiss()
                            }
                            //전체 검색이 아닐 경우
                            else {
                                items.clear()
                                tipReadFromFireBase(database.getReference("contentContainer")
                                    .child(category.toString()).child(middleList[which]))
                                exCheckedItemPosition = which
                                exCheckedItemString = middleList[which]

                                dialog?.dismiss()
                            }
                        }
                    })
                //setPositiveButton("닫기", null)
                show()

            }
        }

        //일단 입장하면 보여주기
        showSelectAlertDialog()

        //중분류
        binding.btMiddleCategory.setOnClickListener {
            showSelectAlertDialog()
        }
        //카테고리 글씨 눌러도 나오게
        binding.tvCategory.setOnClickListener {
            showSelectAlertDialog()
        }

        //아이템 클릭 ->TipShowActivity로 이동
        rvAdapter.itemClick = object : TipRvAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {

                val intent = Intent(this@TipListActivity, TipShowActivity::class.java)
                intent.putExtra("url", items[position].webUrl)
                startActivity(intent)

            }
        }

        rvAdapter.likeClick = object : TipRvAdapter.LikeClick{
            override fun onClick(view: View, position: Int) {

                var existLikeUid: Boolean = false
                val ivTipLike = findViewById<ImageView>(R.id.iv_tipLike)
                var tvTipLike = findViewById<TextView>(R.id.tv_tipLike).text.toString().toInt()
                var tvTipLikeAfter = findViewById<TextView>(R.id.tv_tipLike)

                database.getReference("contentLike").child(items[position].content).child(items[position].middleCategory)
                    .child(items[position].title).child(uid).get().addOnSuccessListener {

                        if(it.value.toString().equals("null")){
                            existLikeUid = false
                        }
                        else{
                            existLikeUid = true
                        }

                        //좋아요가 안 눌려 있다면
                        if (existLikeUid == false){

                            AlertDialog.Builder(this@TipListActivity)
                                .setTitle("좋아요를 누르시겠습니까?")
                                .setNegativeButton("네"){ _, _ ->
                                    database.getReference("contentLike").child(items[position].content).child(items[position].middleCategory)
                                        .child(items[position].title).child(uid).setValue(uid)

                                    items.clear()

                                    if (exCheckedItemString.equals("전체")) {

                                        for (item in middleList) {
                                            tipReadFromFireBase(database.getReference("contentContainer")
                                                .child(category.toString()).child(item))

                                        }
                                    }else{
                                        tipReadFromFireBase(database.getReference("contentContainer").child(category!!).child(exCheckedItemString))
                                        Toast.makeText(baseContext,"좋아요를 눌렀습니다", Toast.LENGTH_SHORT).show()
                                    }



                                }
                                .setPositiveButton("아니오"){ _, _ ->}
                                .create()
                                .show()




                        }
                        //좋아요가 눌려 있으면
                        else{
                            AlertDialog.Builder(this@TipListActivity)
                                .setTitle("좋아요를 취소 하시겠습니까?")

                                .setNegativeButton("네"){ _, _ ->
                                    database.getReference("contentLike").child(items[position].content).child(items[position].middleCategory)
                                        .child(items[position].title).child(uid).removeValue()
                                    items.clear()

                                    if (exCheckedItemString.equals("전체")) {

                                        for (item in middleList) {
                                            tipReadFromFireBase(database.getReference("contentContainer")
                                                .child(category.toString()).child(item))

                                        }
                                    }else{
                                        tipReadFromFireBase(database.getReference("contentContainer").child(category!!).child(exCheckedItemString))
                                        Toast.makeText(baseContext,"좋아요를 취소했습니다", Toast.LENGTH_SHORT).show()
                                    }

                                }
                                .setPositiveButton("아니오"){ _, _ ->}
                                .create()
                                .show()

                        }
                    }

                database.getReference("contentLike").child(category.toString()).child(items[position].middleCategory).child(items[position].title).child(uid).setValue(uid)

            }

        }

    }

    private fun tipReadFromFireBase(root: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    Log.d("content", dataModel.toString())
                    println(dataModel.toString())
                    ////// 위에서 받은 dataModel(데이터 덩어리 -> TipModel 형태로 받음 -> items에 추가
                    val item = dataModel.getValue(TipModel::class.java)
                    items.add(item!!)
                }
                rvAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        root.addValueEventListener(postListener)
    }
}

