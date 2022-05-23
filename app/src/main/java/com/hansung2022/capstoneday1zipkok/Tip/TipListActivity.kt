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


        //myRef1.child("분식").child().setValue(TipModel("content1","분식",))
//        myRef1.child("분식").child("로제 떡볶이").setValue(TipModel("content1","분식","로제 떡볶이","blog.naver.com/peace8012/222645266975","https://postfiles.pstatic.net/MjAyMjAyMTFfMTkz/MDAxNjQ0NTc0NTUwODI4.GiQwuKSbzHPaYamAJJP1zinCNJv8uDMJdGIgWxSwOFUg.-PkCTrXTnkrPhptlWiIrR4QOKfpyaYKzUNpXnM53av0g.JPEG.peace8012/SE-5355931f-0559-4d57-851d-54358e407fb0.jpg?type=w966"))
//        myRef1.child("분식").child("오징어순대 만들기").setValue(TipModel("content1","분식","오징어순대 만들기","blog.naver.com/keum9261/222655469378","https://postfiles.pstatic.net/MjAyMjAyMjNfMTI5/MDAxNjQ1NTgzNjg5Nzkz.iuiRosN18Kd0FiHvvRdoT5rgGDX58HccdllL7XGc5w0g.GuL4ZeMMOeKANyEMiEDtyunHlVE9l81_kQmFcBpayx0g.JPEG.keum9261/SE-e8a4fe5c-e7d1-4612-a14c-16a8c1d0d3db.jpg?type=w773"))
//        myRef1.child("분식").child("매운 해물떡볶이 만들기").setValue(TipModel("content1","분식","매운 해물떡볶이 만들기","blog.naver.com/lotusms12/222681581346?isInf=true","https://postfiles.pstatic.net/MjAyMjAyMTNfMzUg/MDAxNjQ0NzYzMzM4NTQw.XvacDnrDhcRgcvqX9olGlYLbKtLqUdQplcbN8CpabVog.p2yBaQHeaioDPFqqs7XGjR8hkF6Omq8gzEG8syPKXeAg.JPEG.lotusms12/2.JPG?type=w966"))
//        myRef1.child("분식").child("만두 만들기").setValue(TipModel("content1","분식","만두 만들기","blog.naver.com/jh2y3/222683295971","https://postfiles.pstatic.net/MjAyMjAzMjZfMjAw/MDAxNjQ4MjQ3NTAyMjQ3.DfTZdoUqHlOGOYeStUyrLp0QQn0Ovcl8KDY-FNQOyesg.TeyWG6Llhn3CEuBpjyNolHgqzBB05GGFpbtu9VUPifkg.JPEG.jh2y3/%EA%B9%80%EC%B9%98%EB%A7%8C%EB%91%90_%EB%A7%8C%EB%93%A4%EA%B8%B0_(25).JPG?type=w966"))
//        myRef1.child("분식").child("쫄면 만들기").setValue(TipModel("content1","분식","쫄면 만들기","blog.naver.com/kimhy004/222692126400?isInf=true","https://postfiles.pstatic.net/MjAyMjA0MDVfMjk4/MDAxNjQ5MTI1NDI0MzE2.4HE2Fi8Fc-hUgxdPotqJlzkL0uERPRhBgZg0euZlp7kg.qgIhm-vOsJ-1mQVzmLcnQF6tGiwjRJj5SFHUZLc0Uugg.PNG.kimhy004/SE-8032f0f6-241d-4872-aaac-10d847228ffd.png?type=w966"))
//        myRef1.child("분식").child("당면순대 만들기").setValue(TipModel("content1","분식","당면순대 만들기","blog.naver.com/sweetmamang/222566149184","https://postfiles.pstatic.net/MjAyMTExMTJfMTgy/MDAxNjM2Njk3OTQ1ODc3.kPDEFVJrP64tliR9gmMnYD2KFOYwXQJFGYm6ixUrg8Ag.hOJkaQqCVzZd5xglc-S7miRktOAkIYIGlRS9I2brJ_Ig.JPEG.sweetmamang/IMG_6893.JPG?type=w773"))
//
//        myRef1.child("한식").child("깍두기 담그기").setValue(TipModel("content1","한식","깍두기 담그기","blog.naver.com/baby0817/222682520347?isInf=true","https://postfiles.pstatic.net/MjAyMjAzMjVfNzMg/MDAxNjQ4MTcyNjg0MDcz.bD0EB1K0KxH2aB2ZpmhRB0kMKkJopO958jSOxQtGnhUg.gFN70QV_TuaM19ckkiebC27Su3nKs0f0jLWa49_JXKUg.JPEG.baby0817/tugjfurjfdDSC05753135.jpg?type=w966"))
//        myRef1.child("한식").child("만두국 만들기").setValue(TipModel("content1","한식","만두국 만들기","blog.naver.com/leehumdinger/222658314175","https://postfiles.pstatic.net/MjAyMjAyMjVfMTk1/MDAxNjQ1NzY2NjMyNjg3.cXn8XGr87RHkLCkyVPCI-uQiEf2inRysPgMRMFNoT7Yg.SD65kM_z-vyWRU9IGYN3sYo61ocY60zqOIpeP5naOP0g.JPEG.leehumdinger/%EC%84%A4%EB%A0%81%ED%83%95_%EC%82%AC%EA%B3%A8%EC%9C%A1%EC%88%98%EB%A1%9C_%EB%A7%8C%EB%91%90%EA%B5%AD_%EB%A7%8C%EB%93%A4%EA%B8%B015.JPG?type=w966"))
//        myRef1.child("한식").child("잡채 만들기").setValue(TipModel("content1","한식","잡채 만들기","blog.naver.com/imafreee/222699008514","https://postfiles.pstatic.net/MjAyMjA0MTJfMjU5/MDAxNjQ5NzYzMDAwMDQ3.EDflA3xhKcthDtm0SZq-8fxvbLAkJUiAFdGHywH2Jkgg.77NnQwvrbYWV-dHtmcRxL7oS8FIDfs72h6UYLucHktUg.JPEG.imafreee/%EC%9E%A1%EC%B1%8401.jpg?type=w966"))
//        myRef1.child("한식").child("닭개장 만들기").setValue(TipModel("content1","한식","닭개장 만들기","blog.naver.com/kicmemorybox/222682825320","https://postfiles.pstatic.net/MjAyMjAzMDdfMTU2/MDAxNjQ2NjMyNTA0NDUy.B8TfSmU3xPpZU-qevJMO4Kr3nSN3uORxatHOQ6Qujw0g.BG0NUQuCmO-pnRPh8sy9gxBuB9CJUfJimWs3t5jivzEg.JPEG.kicmemorybox/01.jpg?type=w773"))
//        myRef1.child("한식").child("부대찌개 만들기").setValue(TipModel("content1","한식","부대찌개 만들기","blog.naver.com/femail317/222583615354","https://postfiles.pstatic.net/MjAyMTEyMDFfOTgg/MDAxNjM4MzE5NTUzODk1.Fp2rrlLtXxI8SCV9l_MPdTOCCG-5-6Wn8cYQ-izNi38g.t7dCjKSxCSiNvRj29oH5CwK1nEElyKGDK6lZx0tJIrgg.JPEG.femail317/SE-ae9b224d-29f8-45d4-b176-59d09bec44d3.jpg?type=w773"))
//        myRef1.child("한식").child("불고기 만들기").setValue(TipModel("content1","한식","불고기 만들기","blog.naver.com/tkmh1982/222610373822","https://postfiles.pstatic.net/MjAyMjAxMDFfMTY1/MDAxNjQxMDI2ODcxNDM0.1TAFykYkqxYG0PgG59wa25USUCTRhqUSe8S4wghss10g.VmEjib1crzpXH2iycqWDqI2wGv26PZheFoOFb9bYxLwg.JPEG.tkmh1982/DSC04726.JPG?type=w966"))
//
//        myRef1.child("중식").child("해물짬뽕 만들기").setValue(TipModel("content1","중식","해물짬뽕 만들기","blog.naver.com/peace8012/222587194982?isInf=true","https://postfiles.pstatic.net/MjAyMTEyMDRfMjcx/MDAxNjM4NjI5ODkzMzg3.hx9C7kyUQy5qw8ro-PAPK4eF20hUJER56Meo-gKlghYg.Op51L86tifASZ4K5rRNyjbq885gF8cz8cKmq8gHWyLgg.JPEG.peace8012/SE-322e6c32-b5fb-4944-822d-6685e5144a8a.jpg?type=w966"))
//        myRef1.child("중식").child("중국집 볶음밥 만들기").setValue(TipModel("content1","중식","중국집 볶음밥 만들기","blog.naver.com/jsoof/222659418993?isInf=true","https://postfiles.pstatic.net/MjAyMjAyMjdfMjk3/MDAxNjQ1OTU1NDA3Mjk4.30ZoK23sUBgexoBlqa6_cthlFHjhyOXuQlrFUph2Hl4g.BV2HIQ3aWbR6iAClyUxixnMl_pwbC9jrXpszhR-N324g.JPEG.jsoof/IMG_9059.JPG?type=w966"))
//        myRef1.child("중식").child("고추잡채 만들기").setValue(TipModel("content1","중식","고추잡채 만들기","blog.naver.com/peace8012/222664023721?isInf=true","https://postfiles.pstatic.net/MjAyMjAzMDRfODcg/MDAxNjQ2Mzg5NzY0MjA2.4FjSsM0wNp2EFqhiPfGMXq496OVim4nhxu4gPFpfhUsg.hBt0yaBeUW29iN9Tl-pRNMLEYOOinp5-hSint4ycYU0g.JPEG.peace8012/SE-de4de6ca-1b9e-44e5-b466-9b63ce388ffe.jpg?type=w966"))
//        myRef1.child("중식").child("탕수육 만들기").setValue(TipModel("content1","중식","탕수육 만들기","blog.naver.com/jin5194/222694084072?isInf=true","https://postfiles.pstatic.net/MjAyMjA0MDdfMTAz/MDAxNjQ5Mjk2MDk1NjI1.ecmlIsV8KF0mcy8z7dMnWvsICfxaBHq7svRN4CtITdQg.7tYznX3IZS8HPoWnIN6rGCFVzpuObxERasZ79AvkIDIg.JPEG.jin5194/%EB%A9%94%EC%9D%B81.JPG?type=w966"))
//        myRef1.child("중식").child("중국냉면 만들기").setValue(TipModel("content1","중식","중국냉면 만들기","blog.naver.com/pooq87/222439665899","https://postfiles.pstatic.net/MjAyMTA3MjBfMjMz/MDAxNjI2NzU0MTkwNjY1.AwE-yAH3onMmZNJjhyL6zGftsV4joX0XAvYXIZlm4Wgg.yp22QpZ5rzMGuz9Bm_RbzyKF5G1Vvrfov6Aii8KzJD8g.JPEG.pooq87/SE-57c793bb-5a05-458d-9562-42871f03fce5.jpg?type=w966"))
//        myRef1.child("중식").child("울면 만들기").setValue(TipModel("content1","중식","울면 만들기","blog.naver.com/okuimasami/222668965244","https://postfiles.pstatic.net/MjAyMjAzMDhfMTMy/MDAxNjQ2NzMwNjQ4NzE2.P86JeE_tlUVTIIQnV5Nislnj6NhQqv3E_UvGi6_mjKIg.kx7H-LDymtRJ_6shF3IfqVRmJAALd97yycpRZD5B0d0g.JPEG.okuimasami/1646730646787.jpg?type=w966"))
//
//        myRef1.child("일식").child("참치회").setValue(TipModel("content1","일식","참치회","blog.naver.com/aisama2/222653278302","https://postfiles.pstatic.net/MjAyMjAyMThfODAg/MDAxNjQ1MTU4MzQxMzM0.Rw5VqO4hX_oYORIv_gjM46GRLaqZwD7nxz9yk7Ps-BAg.0q3NB6qRv97PpWrup5Y95nhnj923DBQUAVI34-uciw4g.JPEG.aisama2/IMG_3752.JPG?type=w773"))
//        myRef1.child("일식").child("야끼소바 만들기").setValue(TipModel("content1","일식","야끼소바 만들기","blog.naver.com/jiwon_n37/222616599057","https://postfiles.pstatic.net/MjAyMjAxMDhfNzkg/MDAxNjQxNjQ5MzY2NTcy.Jf0rVnw4SoJPjYCge5kNvGUSiKponuId3bO_3mHPARgg.M2-7_j74LSKlHURT8359uXc4l_X-SYGI6Hj0ZttUSF4g.JPEG.jiwon_n37/IMG_9436.JPG?type=w966"))
//        myRef1.child("일식").child("오꼬노미야끼 만들기").setValue(TipModel("content1","일식","오꼬노미야끼 만들기","blog.naver.com/gugu9416/222707194009?isInf=true","https://postfiles.pstatic.net/MjAyMjA0MjFfMzEg/MDAxNjUwNTE3NDMyNTA4.h55Z5v5WWQc-MZ9moz1VB2lkDP9T27aNofo99Q00B8Eg.lnc0BYe0ucGpHrjTSmIvCkd7Elv9OQCAV7OX8AfhMhog.JPEG.gugu9416/SE-65074803-8901-40c7-bec0-25549ea01d6d.jpg?type=w966"))
//        myRef1.child("일식").child("규동 만들기").setValue(TipModel("content1","일식","규동 만들기","blog.naver.com/nkh9475/222676298269","https://postfiles.pstatic.net/MjAyMjAzMTdfMTE4/MDAxNjQ3NTI4MDg1ODI5.0GzS1tmh3yyxU4BfdWO0Q39NzVKP78J4xM1o9wXwCEMg.VMHena3gXV9FBh0kzuAWjZMp26QneB67zbaHpgXf2S4g.PNG.nkh9475/SE-450d260c-da0a-411c-8f97-f1a021999d75.png?type=w966"))
//        myRef1.child("일식").child("오차즈케 만들기").setValue(TipModel("content1","일식","오차즈케 만들기","blog.naver.com/limeshampoo/222481682245","https://postfiles.pstatic.net/MjAyMTA4MjNfMTUw/MDAxNjI5NzI4NjUzNDk1.m3E-Y4qbxc1lZ-yvpsU_K9PJlNTj-u9BbFyIIsekHvog.2-JjtSVDPjhn0AQJGqOibM_duFDz2hGM7jyObFpbOM8g.JPEG.limeshampoo/OP0A4232.JPG?type=w966"))
//        myRef1.child("일식").child("가츠동 만들기").setValue(TipModel("content1","일식","가츠동 만들기","blog.naver.com/peace8012/222680535868?isInf=true","https://postfiles.pstatic.net/MjAyMjAzMjNfMjA0/MDAxNjQ3OTk2MzI0MDE1.ROePFst-83uORJz7Fn8cxg7Xd7af8yeeEeT8wBP6CYEg.GnLnn_7Z9zgx0jugtWppTQk6CQE1Sft_pB5JIIxqY-0g.JPEG.peace8012/SE-74054e73-6f9b-4100-8b05-d0ea65a48175.jpg?type=w966"))
//
//        myRef1.child("양식").child("함박스테이크").setValue(TipModel("content1","양식","함박스테이크","blog.naver.com/peace8012/222705253901?isInf=true","https://postfiles.pstatic.net/MjAyMjA0MTlfMTM3/MDAxNjUwMzQ3Mjg3MzEy.L4gWSRk7tQLQeMRguUnuxVhNRQj7F_VYoUNZtDP6-GMg.OEwHTzPlhGoG-UTQP5GMlMdBnvuPoi5R14wG06Q0vBYg.JPEG.peace8012/SE-22f762c1-e6e1-4b22-a552-9497e52d2d00.jpg?type=w966"))
//        myRef1.child("양식").child("햄버거 만들기").setValue(TipModel("content1","양식","햄버거 만들기","blog.naver.com/mijinkim80/222689596134?isInf=true","https://postfiles.pstatic.net/MjAyMjAzMjhfMjY1/MDAxNjQ4NDczNzgxNjU5.wJag1Ck6Vg1_m1kYTP54wjLKl8cRQIQwsM5SmqVxcecg.Xvr99cCs4V_86fKq4CkEA3bhpSkWyiwF0zOkc3f2320g.JPEG.mijinkim80/7.JPEG?type=w773"))
//        myRef1.child("양식").child("토마토 스파게티 만들기").setValue(TipModel("content1","양식","토마토 스파게티 만들기","blog.naver.com/baby0817/222665164240?isInf=true","https://postfiles.pstatic.net/MjAyMjAzMDZfMTQg/MDAxNjQ2NTMyMzEwNzYy.zSvjAvpTNiA_oNLOXw582XqAXnb1B038Ba3q-Spgv7Ug.pJ19_XSInIXqnfM9ZEf9_TUHAeQOCV22RSWJEGCq2z0g.JPEG.baby0817/600bds09100s123.jpg?type=w966"))
//        myRef1.child("양식").child("피자 만들기").setValue(TipModel("content1","양식","피자 만들기","blog.naver.com/firehouse79/222676212609","https://postfiles.pstatic.net/MjAyMjAzMThfMTYy/MDAxNjQ3NTY1MTg5NDMy.LTrt1ivrtpNenJVHo3ObE-I3MNYERVbt7bmczfKg4mAg.PdPsGwxl1H60sYukva17TB9jRfvThn7ml8pcYJuFVcgg.JPEG.firehouse79/SE-b507d4f4-10c3-47f3-8ac9-72da8d1cfacf.jpg?type=w966"))
//        myRef1.child("양식").child("라자냐 만들기").setValue(TipModel("content1","양식","라자냐 만들기","blog.naver.com/lalacucina/222603588811","https://postfiles.pstatic.net/MjAyMTEyMjJfMjMw/MDAxNjQwMTgzNjU3Nzg5.1J83zuolJTdUv1At5E-bI-WcxxO3ClzHSePr6y6LLtwg.dplxXASa1dimTVwIXzlR7bQrqdDysP9V_cxpqUXwdG4g.JPEG.lalacucina/%EB%9D%BC%EC%9E%90%EB%83%90_%EB%A7%8C%EB%93%A4%EA%B8%B06.jpg?type=w773"))
//        myRef1.child("양식").child("맥앤치즈 만들기").setValue(TipModel("content1","양식","맥앤치즈 만들기","blog.naver.com/firehouse79/222490045225","https://postfiles.pstatic.net/MjAyMTA4MzFfMTgz/MDAxNjMwMzM4ODg4Mjcx.mwQt5IILUxSLwM6CRIIlzYjmahsjdj1p75qJbYzNkOAg.VF_7WE9W1mR_mV10DZ7FuU4VuPALIQYpIdBQygIsfHgg.JPEG.firehouse79/SE-caf2b1f7-ea3b-4afc-a70d-243224358976.jpg?type=w966"))
//
//
//        myRef2.child("빨래").child("흰 옷 빨래").setValue(TipModel("content2","빨래","흰 옷 빨래","blog.naver.com/83hyemin/222704985953?isInf=true","https://postfiles.pstatic.net/MjAyMjA0MTlfMjM5/MDAxNjUwMzMyMDYzNjQ4.L7iSDwQmC5husVpwZb5cqrEslntu8eXJYaogrni5Zygg.7U8ZFTEbNdfWOsAw4cuAitWM3WdOr3_nAkoT-1-OgN4g.JPEG.83hyemin/%E3%85%8E%E3%85%87%E3%85%87%E3%84%B9%E3%85%88%E3%84%B1_1.JPG?type=w966"))
//        myRef2.child("빨래").child("섬유 유연제").setValue(TipModel("content2","빨래","섬유 유연제","post.naver.com/viewer/postView.naver?volumeNo=28179823&memberNo=15460571&vType=VERTICAL","https://post-phinf.pstatic.net/MjAyMDA1MDRfNDQg/MDAxNTg4NTU1OTM0NTg3.aTfdYg6-jmd_Ioc2rqVhVxbVmOQb0cyFw1nypzHcBk4g.YfKbWn2bsfgSFO-qpOeIEBV_N6Xg0ufX_iXAB-E_dOMg.JPEG/1.jpg?type=w1200"))
//        myRef2.child("빨래").child("옷 땀냄새 제거").setValue(TipModel("content2","빨래","옷 땀냄새 제거","blog.naver.com/gsgold80/222474533879","https://postfiles.pstatic.net/MjAyMTA4MTdfMjE1/MDAxNjI5MTc3ODEyMzc0.CmP9bb1kXgcUtpTLB-6_MljDHeUs19ohjuNq1XCrBbAg.wfTA5_GCsMM5iplnu5OnwgdShfj_K20NNapXkHbq98cg.JPEG.gsgold80/%EC%98%B7%EB%95%80%EB%83%84%EC%83%88%EC%A0%9C%EA%B1%B01.JPG?type=w773"))
//        myRef2.child("빨래").child("다리미질").setValue(TipModel("content2","빨래","다리미질","blog.naver.com/ohj7137/90172680217","https://postfiles.pstatic.net/20130507_11/ohj7137_13679327846277Yys6_JPEG/imagesCA8FBBFP.jpg?type=w2"))
//        myRef2.child("빨래").child("이불빨래").setValue(TipModel("content2","빨래","이불빨래","blog.naver.com/gorgeous0312/222681956628","https://postfiles.pstatic.net/MjAyMjAzMjRfMTYy/MDAxNjQ4MDUwNDYzODk4.tyWJKiOlSFZlFq4irJXgk_1u5nhLhy8fNobCPrLeOjcg.CVwsXRLWHUGhRwpda6tZghyMPUKXdZRcdum_IM4Asswg.JPEG.gorgeous0312/%EC%9D%B4%EB%B6%88%EC%A2%85%EB%A5%98.jpg?type=w966"))
//        myRef2.child("빨래").child("신발 세탁").setValue(TipModel("content2","빨래","신발 세탁","post.naver.com/viewer/postView.nhn?volumeNo=23773652&memberNo=33030565&vType=VERTICAL","https://post-phinf.pstatic.net/MjAxOTA4MjBfMTgw/MDAxNTY2Mjk2NTgyOTM0.xMdMQ40uuPNqIEgkhXc0bZsnyxXL_gz-gHHOobwS_wsg.kTipzg4iqWLFONTdFu_TkGxs7E7YVjObQQoSN_IMFPQg.PNG/Group_3.png?type=w1200"))
//
//        myRef2.child("청소").child("욕실 곰팡이 제거").setValue(TipModel("content2","청소","욕실 곰팡이 제거","post.naver.com/viewer/postView.naver?volumeNo=32237085&memberNo=41255538&vType=VERTICAL","https://post-phinf.pstatic.net/MjAyMTA4MjVfMjM1/MDAxNjI5ODU5NDM4MzAx.03GD0QmFuSV58qpL10aPsF1UeoG4W4ltx8_mfYVyaEUg.ogk4lgeZfq6HlK0zHlW57Rg-E6Jnfs9FvflDfNo596Ug.JPEG/image_3145656881629857776536.jpg?type=w1200"))
//        myRef2.child("청소").child("변기 청소").setValue(TipModel("content2","청소","변기 청소","blog.naver.com/emblem00/222593016462","https://postfiles.pstatic.net/MjAyMTEyMTFfMjIz/MDAxNjM5MjI3MDE5MjY0.rnS8SaI3xw6Ln6fvaZld_Ca2snrMozuptMuLSXprQ4sg.AOyeqphTE4MA3psJ6PqM0LL5iTrHDEVJimpM-CHKSRUg.JPEG.emblem00/SE-8d5b1761-c31c-4b5c-941b-ecd61c01b1a3.jpg?type=w966"))
//        myRef2.child("청소").child("유리 닦기").setValue(TipModel("content2","청소","유리 닦기","blog.naver.com/skfroj/222393223609","https://postfiles.pstatic.net/MjAyMTA2MTBfNzcg/MDAxNjIzMzMxNzUwMDgx.oWJnOHMi5oR7YvDVm97mmuwjWqE3CaYZxuIjOPo0ijsg.MFD8uN7YkDoL9BqCSWytiGfMPxvhQJ1kwKI7zybkrBYg.JPEG.skfroj/photo-1469981283837-561b3779462f.jpg?type=w773"))
//        myRef2.child("청소").child("싱크대 청소").setValue(TipModel("content2","청소","싱크대 청소","blog.naver.com/yellow_digo/222625334359","https://postfiles.pstatic.net/MjAyMjAxMThfODIg/MDAxNjQyNDMxNzg1Mzcx.4fSynaivARwG5lVIAgQDVTT8V1C2yH3w4qmUXH7v-8Mg.D3VtBybdOiFomIVmWYV62HJwOrWdMD8mXVtsqmXme84g.JPEG.yellow_digo/SE-e00fd56c-3483-4157-b27a-10ef3f78dbdc.jpg?type=w966"))
//        myRef2.child("청소").child("세면대 머리카락 제거").setValue(TipModel("content2","청소","세면대 머리카락 제거","blog.naver.com/tlstjsdk1319/222656399410","https://postfiles.pstatic.net/MjAyMjAyMjRfMjA1/MDAxNjQ1NjY0MTAyNzc0.AMljWzYD0MtV9BzJL2T_hrdf2_tLD7FtWZ6ANTmjiO0g.N-rth0eMySsXrIzGdqUEom5meI9kr-JrlPeY0htStrQg.JPEG.tlstjsdk1319/IMG_0395.jpg?type=w773"))
//
//        myRef2.child("가전").child("세탁기 청소").setValue(TipModel("content2","가전","세탁기 청소","blog.naver.com/p0o9/222711951414?isInf=true","https://postfiles.pstatic.net/MjAyMjA0MjZfMjAz/MDAxNjUwOTQ1MDQxODc5.cAAwx0y6ZH8rsymp5EQLjBVGxKY3dA4NkHfvcA6BVLYg.IwzZWiDcqZ4I6RV4XeGGkNDEAW-9-MBWFXftRYWG5ikg.JPEG.p0o9/SE-0f7dae73-293c-4a2d-943b-1eb6f6b94f02.jpg?type=w966"))
//        myRef2.child("가전").child("tv 사이즈 결정").setValue(TipModel("content2","가전","tv 사이즈 결정","blog.naver.com/jsajsf99/222668373901","https://post-phinf.pstatic.net/MjAxODExMjZfMTIx/MDAxNTQzMjE3MjU2MDM5.0wWBYbEsbwpVpAhvwGRRbUlWPvDxUBfv38oxmtBPfEEg.t0DW_RBJwa-wryuhBWTbftj9nRiQ67NHaSBiRcnzWfQg.JPEG/32%EC%9D%B8%EC%B9%981.jpg?type=w1200"))
//        myRef2.child("가전").child("로봇 청소기").setValue(TipModel("content2","가전","로봇 청소기","m.post.naver.com/viewer/postView.naver?volumeNo=33137771&memberNo=51643186","https://postfiles.pstatic.net/MjAyMjA0MjdfMjI1/MDAxNjUxMDI4NTU0MDQz.n7Vu1mvCNQIODbagOkBhbqri8mf__6305r1JO0v5bWwg.bcUYfTGQx_CXmBugj916r_c3oGGtLZV6GunojpKSK3og.JPEG.shsh2699/2%EB%8C%80%ED%91%9C%EC%82%AC%EC%A7%84.jpg?type=w773"))
//
//
//        myRef3.child("강아지").child("강아지 사료").setValue(TipModel("content3","강아지","강아지 사료","m.post.naver.com/viewer/postView.naver?volumeNo=33511661&memberNo=42920381","https://post-phinf.pstatic.net/MjAyMjAzMjVfMTM2/MDAxNjQ4MTY2MzQ3MTkz.NjZxpVgDexPDeKQAD6JpWOT0_fcNVMEKCeSKb5qN5PAg.WFn4tj4fqDzciFV-fWwQrSi2ZfNRnaR3KpiKbMZ_sckg.JPEG/6.jpg?type=w1200"))
//        myRef3.child("강아지").child("강아지 예방접종").setValue(TipModel("content3","강아지","강아지 예방접종","blog.naver.com/sooamc/222693108503","https://postfiles.pstatic.net/MjAyMjA0MDVfMjEz/MDAxNjQ5MTQ3MTI0Nzkw.KeO4Hq3iYqz8Pw080FcHNrQzJQuTynTyqFR-Br3LrjAg.Zdo-DCTwersa7unWI8uhirsgrUCWHPvXB-_ufVZDKaUg.PNG.sooamc/1_(5).png?type=w966"))
//        myRef3.child("강아지").child("강아지 훈련").setValue(TipModel("content3","강아지","강아지 훈련","post.naver.com/viewer/postView.naver?volumeNo=33666615&memberNo=38399662&vType=VERTICAL","https://post-phinf.pstatic.net/MjAyMjA0MjFfMjcg/MDAxNjUwNTIxMjcwNDE1.DtYhBgXvtmqsO3b5poDaJEQGc0JorNcLO5KX_Aosj7Yg.0II2aFY-MRQhn-lMvRkYJeflWpjUO5Fn7E-anxOHUg0g.JPEG/nomao-saeki-DX4XRS7w6Cw-unsplash-edited-scaled.jpg?type=w1200"))
//        myRef3.child("강아지").child("강아지 배변훈련").setValue(TipModel("content3","강아지","강아지 배변훈련","blog.naver.com/eehqql/222682047099","https://postfiles.pstatic.net/MjAyMjAzMjRfMjg0/MDAxNjQ4MTE2Nzc4MTA2.utbtIfrygXnwzPFxf2Qc4UTS48hYCPb8IprntHMXte4g.aiwfh-fl3z7Lj9eKwtESDeFlSDUgss_tydsBoQcIGUwg.JPEG.eehqql/KakaoTalk_20220324_154628096_03.jpg?type=w966"))
//        myRef3.child("강아지").child("강아지 간식").setValue(TipModel("content3","강아지","강아지 간식","blog.naver.com/hs0910h/222693759112","https://media.istockphoto.com/photos/dog-with-delicious-pet-treat-bone-at-garden-lawn-picture-id874857174?b=1&k=20&m=874857174&s=170667a&w=0&h=Ggu_WZysAQCY_H3SO0OAb4V0JTsJ7Ar5XHq9k08yqEE="))
//
//        myRef3.child("고양이").child("고양이 사료").setValue(TipModel("content3","고양이","고양이 사료","blog.naver.com/ismaind/222482822820","https://postfiles.pstatic.net/MjAyMjA0MDdfNjMg/MDAxNjQ5Mjg5MTk4OTcy.QB_2E0zHKfmIjpAPJLWteprPgZXfIfeClilE6-N2xT0g.4ug9mYQ_SRMdpZXePYWyE9LUSPwlNfJr4HMAigH_2ckg.JPEG.carib5/pexels-photo-7725607.jpeg?type=w773"))
//        myRef3.child("고양이").child("고양이 질병").setValue(TipModel("content3","고양이","고양이 질병","blog.naver.com/jekyll13/222571715413","https://postfiles.pstatic.net/MjAyMTExMThfMTEg/MDAxNjM3MTk4ODIyODI4.AL8M8X2szQKIYU8DZT6WPVR1wH_NuGtiDs0p-fT4cY0g.9lgjIziQjr0NoOZ5ezGRlvQORxjeXK5npbLLdeCZJUog.JPEG.jekyll13/8e7a22eeff8381aec1f603cdfdf4c23b_1636839529_4014.jpeg?type=w773"))
//        myRef3.child("고양이").child("고양이 목욕").setValue(TipModel("content3","고양이","고양이 목욕","blog.naver.com/sooamc/222528446023","https://postfiles.pstatic.net/MjAyMTEwMDZfMTMx/MDAxNjMzNDkyMjAwMTk2.MYAJs6WtQXraU9XMJdnjWo_lsDwD5VRhDOyv-2yFdw4g.zzKnHRvB7mo_d3nYHR97C4OhWkpk5enUXUNhd7F3rgMg.PNG.sooamc/1_(4).png?type=w966"))
//        myRef3.child("고양이").child("고양이 간식").setValue(TipModel("content3","고양이","고양이 간식","blog.naver.com/2eeee211/222702299858","https://postfiles.pstatic.net/MjAyMjA0MTZfODEg/MDAxNjUwMDcxMDQwNzg5.V9WNhHFwr3uFQKFWn4He7zPXuVFaDupoiO2SeDGbOksg.A4FoalWhkjxtGzWQ4IpB2wpHkwCvEBOJldQcb-CeOEkg.JPEG.2eeee211/2.jpg?type=w580"))
//        myRef3.child("고양이").child("고양이 장난감").setValue(TipModel("content3","고양이","고양이 장난감","blog.naver.com/2013dalja/222029501319","https://postfiles.pstatic.net/MjAyMDA3MTNfOTcg/MDAxNTk0NjE1NzA3ODYz.U6upObWYEZfeM-E4kxEZqDSrmPRb3YbH3W4bc1YofI8g.Sbt4jEZMqsUMmAJJERE9UPEXsvw6GUPSVeFHFL4G8-wg.JPEG.2013dalja/8.jpg?type=w966"))
//
//        myRef3.child("소동물").child("햄스터").setValue(TipModel("content3","소동물","햄스터","blog.naver.com/yongjin22/222470041758","https://postfiles.pstatic.net/MjAyMTA4MTRfMTE5/MDAxNjI4OTMxOTgyODUy.spyEmzqwdGYMal26lEXfPA2ECu08UlENYO3FDcv_yVYg.-dx_sctB2JealtvjbhnQ2X_ycOwL8You18GXQVUY5SIg.JPEG.yongjin22/7.jpg?type=w966"))
//        myRef3.child("소동물").child("고슴도치").setValue(TipModel("content3","소동물","고슴도치","blog.naver.com/memories_album/221944177876","https://postfiles.pstatic.net/MjAyMDA1MDNfMTIz/MDAxNTg4NTEzOTEzMTU4.s3Bl8U9aj5lQPC_x-smRBOBTfYtwSAqtMj0g9ZwTnoYg.cY3xalwIoftFiPxT_WyQIRfTCuYDn2fAkAfZSrw2Q3sg.JPEG.memories_album/muaz-aj-SMoGda6duIY-unsplash.jpg?type=w773"))
//        myRef3.child("소동물").child("금붕어").setValue(TipModel("content3","소동물","금붕어","blog.naver.com/memories_album/222112918327","https://postfiles.pstatic.net/MjAyMDEwMTFfMjkx/MDAxNjAyNDI0Njg5MTg4.FKx8wv9cVEFurwbqdMFx8ujwCf4-FRy1Y2RKrLXIq7sg.C4XT5TD9zJShtg03kGaczgXh9q4HOYIfVeexRDHthncg.JPEG.memories_album/23395662826_3a4092e457_c.jpg?type=w773"))
//        myRef3.child("소동물").child("구피").setValue(TipModel("content3","소동물","구피","blog.naver.com/iwillbes27/221979492878","https://postfiles.pstatic.net/MjAyMDA1MjZfMTAx/MDAxNTkwNDY3OTQzNjI3.JwBKtf4ZuSANpWqVRJV4di_SYfNhbKMpG9NZ99_uPGMg.0ddzGpEY7IB9RQSCMSdBv652Y-eek1UqYFsR823_ARUg.JPEG.iwillbes27/DSC09100.JPG?type=w966"))
//        myRef3.child("소동물").child("다람쥐").setValue(TipModel("content3","소동물","다람쥐","blog.naver.com/tjrgh4567/222063499505","https://postfiles.pstatic.net/MjAyMDA4MTdfMzgg/MDAxNTk3NjY2NTE1NDAx.dZ5dmTensVJUajlmd9M94z6fOHYLJKh3tS2tvsncPlcg.5QJax8jFTfxHEJrG-OX8-37pXMHivfRK-3YBSE_LUVMg.JPEG.tjrgh4567/IMG_0048.JPG?type=w966"))
//
//        myRef4.child("주식").child("주식용어1").setValue(TipModel("content4","주식","주식용어 모음","blog.naver.com/vipro/222690065218","https://postfiles.pstatic.net/MjAyMjA0MDJfMjg0/MDAxNjQ4OTA4MDEzMzQy.snNBIzkg7fb1a-zGhOhuzBMEMpZn_vqU3oAaydc3nyIg.mMS5lAsWnBTuqCcH04TBsizR3wYZ4ulJMWOyBtDuFHQg.PNG.vipro/SE-bd976dba-4d41-4b53-b990-8e5caeea53c4.png?type=w966"))
//        myRef4.child("주식").child("주식용어2").setValue(TipModel("content4","주식","주식용어2","post.naver.com/viewer/postView.naver?volumeNo=33629708&memberNo=2966116&vType=VERTICAL","https://post-phinf.pstatic.net/MjAyMjA0MTRfMjMy/MDAxNjQ5OTI0ODY1NjYz.-DXiAwrI-VSi5FmAnFMmGdxA74Oj-uDdE6_cqWltIVog.lE4QIFWzrf4T2fXtKrLqEOnA74-HQg3xP4dVKk29_fAg.PNG/1.png?type=w1200"))
//        myRef4.child("주식").child("주식차트 보는 법").setValue(TipModel("content4","주식","주식차트 보는 법","blog.naver.com/rec1820/222318400729","https://postfiles.pstatic.net/MjAyMTA0MjFfMjMz/MDAxNjE5MDExMzc4MDkw.wQ2o_TB4KcuPwp90d7DbHGdW9okK84MFA-N00LtJ9gog.xEjtvnik3q_XbeAi6O_5vMDwYVQTfGCUsm5Te7VqCeog.JPEG.rec1820/CKXNGFDRE004.JPG?type=w966"))
//        myRef4.child("주식").child("주식 팁").setValue(TipModel("content4","주식","주식 팁","blog.naver.com/dfin1004/222608645299","https://postfiles.pstatic.net/MjAyMTEyMzBfNjMg/MDAxNjQwODUyNjQ3MDkx.A4O6XFqsemlJDgojywPBNKzxnsAp9IWK9rIMplhJH2kg.6Rx3jOvKjv1J4bQFrcE43HAKsAFW1d2SD_5bE3INZFwg.JPEG.dfin1004/CK_psxtg0617738_l.jpg?type=w966"))
//
//        myRef4.child("가상화폐").child("가상화폐란?").setValue(TipModel("content4","가상화폐","가상화폐란?","blog.naver.com/bjun119/222546959857","https://postfiles.pstatic.net/MjAyMTEwMjJfMjQx/MDAxNjM0ODg3MDk4OTUz.Qz00xChZHTa4ccQz1qm9VHXBG8ncx-mORGgh6smUJQMg.vgcKGA9we8SaW2ds9FTm7YfmyD_5ghQaszOGn4LS_lwg.JPEG.bjun119/dollars-g5e9b77574_1920.jpg?type=w966"))
//        myRef4.child("가상화폐").child("메타버스와 NFT").setValue(TipModel("content4","가상화폐","메타버스와 NFT","post.naver.com/viewer/postView.naver?volumeNo=33422649&memberNo=5184&vType=VERTICAL","https://post-phinf.pstatic.net/MjAyMjAzMDdfMjU3/MDAxNjQ2NjE5MzU4MTUw.2lDX0gTOrCoQ_s7BcC4XzJaJqmu8olumxV758yoJ4l4g.SPWh4MfRbj0uyZKAXrLwSzsxCoPvg5llD3hrB9HrlPEg.JPEG/shutterstock_1931510018.jpg?type=w1200"))
//        myRef4.child("가상화폐").child("암호화폐 투자법").setValue(TipModel("content4","가상화폐","암호화폐 투자법","blog.naver.com/ruhilim/222651236586","https://postfiles.pstatic.net/MjAyMjAyMThfMTA5/MDAxNjQ1MTY5NTg4ODM0.TBC5qG6K50Ga0jaHTlYkiI5nVZObn0ybnqGTBd6DFj8g.kABowBktmzKuVnVNLIPaJFDeZYDbr2FXrsInFSjyXcwg.PNG.ruhilim/image.png?type=w966"))
//        myRef4.child("가상화폐").child("암호화폐 전망").setValue(TipModel("content4","가상화폐","암호화폐 전망","blog.naver.com/kondo5813/222628769673","https://postfiles.pstatic.net/MjAyMjAxMjJfMjQg/MDAxNjQyODU4NzA1NDYx.sJjKeLLUraEJpRDDdeCAo_NZMCQLWJ5mzDhRvFpP7pAg.2u3QeGjMpIAk0gqU-utk8czuQX-o2DTZAIv0LUn_mV8g.JPEG.kondo5813/00.JPG?type=w773"))
//
//
//        myRef5.child("계약").child("계약파기").setValue(TipModel("content5","계약","계약파기","blog.naver.com/lady3322/222710907025","https://postfiles.pstatic.net/MjAyMjA0MjVfMjk1/MDAxNjUwODY2OTYzOTc3.yvtDIrR4i7kPTtmZ5WtxPufz_mgPIYbeUuQb5jkPbRog.2b7TtwbEGmZz3PEFjf3mioJ52ATFei83ODZx9-tjPvsg.JPEG.lady3322/SE-2e03e265-230a-4324-b0f6-52a1136ce670.jpg?type=w773"))
//        myRef5.child("계약").child("계약기간 전 이사").setValue(TipModel("content5","계약","계약기간 전 이사","blog.naver.com/chldltmfzz/222496525885","https://postfiles.pstatic.net/MjAyMTA5MDZfMTEx/MDAxNjMwODkxMjgyNzcw.LiZiXS7GldR7T_AVxCdkMcMiMZDL3h06VnGgPbXLc3cg._DVFOb2EzQ2rjoDUaIEAqQ3buCgxd-4L22tGgV7aJrsg.JPEG.chldltmfzz/10.JPG?type=w580"))
//        myRef5.child("계약").child("월세 세액 공제").setValue(TipModel("content5","계약","월세 세액 공제","blog.naver.com/pullip0821/222688931429","https://postfiles.pstatic.net/MjAyMjA0MDFfMjEz/MDAxNjQ4NzkxODUxMzAz.gbor2oJ0JLMrjbJAGbTK-IDz_Vf8XXzPOn3biL2P4LMg.lmKXou7G8HTKVoj_Qmnb5ScTx5Vakefu7qFyVqhvjDYg.JPEG.pullip0821/KakaoTalk_20220401_115236182.jpg?type=w773"))
//        myRef5.child("계약").child("월세 청년지원").setValue(TipModel("content5","계약","월세 청년지원","cafe.naver.com/dokchi/11680057?art=ZXh0ZXJuYWwtc2VydmljZS1uYXZlci1zZWFyY2gtY2FmZS1wcg.eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjYWZlVHlwZSI6IkNBRkVfVVJMIiwiY2FmZVVybCI6ImRva2NoaSIsImFydGljbGVJZCI6MTE2ODAwNTcsImlzc3VlZEF0IjoxNjUyMTkyNzUwMjYwfQ.gGPFKZqzByw0tdP4ue4A0C9RkfHosXZjOiqE9ASJihg","https://cafeptthumb-phinf.pstatic.net/MjAyMjA0MjlfMjk2/MDAxNjUxMjMyODYxMTE1.fejSv4poLEdJvUT7xHPbB4xtEg5fHdQoRD2oVfiHgiEg.m7fTbkEi5ckgCSMwysYX-GcO5Pm-ExxB2f13WBZL8UEg.PNG/image.png?type=w1600"))
//
//        myRef5.child("하자보수").child("하자보수 책임").setValue(TipModel("content5","하자보수","하자보수 책임","blog.naver.com/maldok/222044265485","https://postfiles.pstatic.net/MjAyMDA3MjhfMTM4/MDAxNTk1OTIwMjk1OTcw.KVYJVAL2AN0YTuKfO7q6TvMgIOzivGgN_nxWfSX0nuQg.U8aPIMS9V_7ndIrFyUkcM-sW4nafs5KmGWouqsfXrD4g.PNG.maldok/1.png?type=w773"))
//        myRef5.child("하자보수").child("만기 시 하자보수").setValue(TipModel("content5","하자보수","만기 시 하자보수","blog.naver.com/l2y0j/222643782164","https://postfiles.pstatic.net/MjAyMjAyMTBfMTU5/MDAxNjQ0NDU5NzY3Mzkx.WHDE49CpaaadVpWn_V5b6RTkoMOvz8A0MaFVWtuLA5Ag.3V0eVqviP0FhN-elfgsNtNLY4k7-IW4jLTgWR-eMvYIg.PNG.l2y0j/image.png?type=w773"))


//        myRef6.child("독서").child("독서 자세").setValue(TipModel("content6","독서","독서 자세","post.naver.com/viewer/postView.nhn?volumeNo=26120423&memberNo=36122335&vType=VERTICAL","https://post-phinf.pstatic.net/MjAxOTEwMDRfMjU1/MDAxNTcwMTUzNDg5Njc2.5BCpHpaGzq8liKpOL7I9XZcjT9gKwttFMPgEW4RGXsgg.uiyxxOJmkCpHlIyug9NaTC8_yEuUMTB3gjBUmSqRqzQg.JPEG/%EB%8F%85%EC%84%9C-%EC%98%AC%EB%B0%94%EB%A5%B8-%EC%9E%90%EC%84%B805.jpg?type=w1200"))
//        myRef6.child("독서").child("독서 명언").setValue(TipModel("content6","독서","독서 명언","blog.naver.com/book_seeker/222168725921","https://postfiles.pstatic.net/MjAyMDEyMTBfMTQz/MDAxNjA3NTMwMzk0Njk2.czhMIPY0nuAuzWAPq3_sU2wQ7Yq4vsSBmMeGV2NUJG8g.7CWHFEKswWgqTiuiDqpVdPFPVzYeZOSrkwa2pl6VGg4g.PNG.book_seeker/%EB%8F%85%EC%84%9C_%EB%AA%85%EC%96%B8.png?type=w966"))
//        myRef6.child("독서").child("독서법").setValue(TipModel("content6","독서","독서법","post.naver.com/viewer/postView.naver?volumeNo=33137938&memberNo=29566044&vType=VERTICAL","https://post-phinf.pstatic.net/MjAyMjAxMTdfOSAg/MDAxNjQyMzk3ODI5MTAx.Z1DqKal_IIakXIx9AGYAZ_2ZFWvTUNyJW_2AEsJbo10g._5Oiyz6TiWvYcIHcw_JtGrqkqZePQce661y0LXiQBdMg.PNG/10.png?type=w1200"))
//

//        myRef6.child("운동").child("스쿼트 자세").setValue(TipModel("content6","운동","스쿼트 자세","blog.naver.com/wkdjjs/222630111963","https://postfiles.pstatic.net/MjAyMjAxMjRfMTk2/MDAxNjQzMDA5NDM5NTIx.wj2sV54ZqcrF3WyX4KxSEd_a7yrtD50dGLN95H-RF9Yg.vmhZ1MF7J6XPOiCEKa2EBG-fGvBRj1Ai3uXZGZqonTsg.JPEG.wkdjjs/output_462055273.jpg?type=w773"))
//        myRef6.child("운동").child("데드리프트 자세").setValue(TipModel("content6","운동","데드리프트 자세","blog.naver.com/sjw2558/222722699562","https://postfiles.pstatic.net/MjAyMjA1MDZfMTEy/MDAxNjUxODA2ODUzMzY1.FtHfchUHXeUQcb4LQt5zD5VK-V1e6Tko4E0SolX_6J0g.rA196A6GQtpvQ8PXLX061sGLufKPC7ttsnxEoc79GN8g.JPEG.sjw2558/20220204%EF%BC%85EF%EF%BC%85BC%EF%BC%85BF221127.jpg?type=w966"))
//        myRef6.child("운동").child("밴치프레스 자세").setValue(TipModel("content6","운동","밴치프레스 자세","blog.naver.com/woojin390000/222698607732","https://postfiles.pstatic.net/MjAyMjA0MTJfMTUz/MDAxNjQ5NzQwMjM2NjA3.YT8vxzZtIRXAr4tjXzsV4U-2aS7aVisKof4ALFjigskg.BplBxNkfzlQRDqkyZ9AScZhbrJv68rW8ylrJ27NQ5TUg.PNG.woojin390000/20220412_140915.png?type=w773"))
//        myRef6.child("운동").child("운동 세트 수").setValue(TipModel("content6","운동","운동 세트 수","blog.naver.com/musclebear11/222719787187","https://postfiles.pstatic.net/MjAyMjA1MDNfMTE1/MDAxNjUxNTYyNzQ1MzY3.jMC9ICgbZ9ORiKdop3lL5TvWdjr7nrxAUv9ynqwLncsg.GR-W9DVNfQwl2dV11kzlKNyR6uSi_WcWbeknHDTjchgg.JPEG.musclebear11/fitness-gf40b3090b_1920.jpg?type=w773"))
//
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

