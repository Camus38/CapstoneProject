package com.hansung2022.capstoneday1zipkok.BoardSearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.Board.BoardReadAdapter
import com.hansung2022.capstoneday1zipkok.Board.BoardReadModel
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ActivityBoardSearchBinding

class BoardSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardSearchBinding

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    //각종 게시글 데이터
    var boardData: ArrayList<BoardReadModel> = arrayListOf()

    //게시판 고유 아이디 리스트 형태로 받아오기 -> 누르면 해당 게시판 열리고 게시판 아이디에 맞게 정보 받아오
    var boardList: ArrayList<String> = arrayListOf()
    private lateinit var boardSearchAdapter: BoardReadAdapter

    //댓글 갯수
    var commentCount: Int = 0

    //검색어
    var searchVocabulary: String = ""

    //스피너 아이템
    var selectedSpinnerItem: String = "전체"

    //스피너에 의해 선택되어 readDataBy에 들어갈 인자
    var selectedStringBySpinner: String = "All"




    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBoardSearchBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        val myUid = auth.currentUser?.uid.toString()

        //게시글 아답터
        boardSearchAdapter = BoardReadAdapter(this, boardData, boardList, myUid)
        //boardReadAdapter.notifyDataSetChanged()


        binding.rvSearch.adapter = boardSearchAdapter
        binding.rvSearch.layoutManager = LinearLayoutManager(this)

        //검색 누르면
        binding.btSearch.setOnClickListener {
            if(binding.etSearch.text.toString().isEmpty()){
                Toast.makeText(this,"검색어를 입력해 주세요",Toast.LENGTH_SHORT).show()
            }else {
                boardList.clear()
                boardData.clear()
                boardSearchAdapter.notifyDataSetChanged()
                readDataBy(selectedStringBySpinner)
            }
        }

        setupSpinner()
        setupSpinnerHandler()


    }

    //스피너
    private fun setupSpinner() {
        val category = resources.getStringArray(R.array.spinner_category)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, category)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

    }

    private fun setupSpinnerHandler() {
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedSpinnerItem = binding.spinner.getItemAtPosition(p2).toString()
                when (selectedSpinnerItem) {
                    "전체" -> {
                        selectedStringBySpinner = "All"
                    }
                    "제목" -> {
                        selectedStringBySpinner = "title"
                    }
                    "내용" -> {
                        selectedStringBySpinner = "msg"
                    }
                    "닉네임" -> {
                        selectedStringBySpinner = "nickname"
                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }


    private fun readDataBy(category: String) {
        searchVocabulary = binding.etSearch.text.toString()


        var countFound: Int = 0

        //전체검색인 경우
        if (selectedStringBySpinner.equals("All")) {
            db.collection("Board").whereGreaterThanOrEqualTo("title", searchVocabulary)
            db.collection("Board").orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {


                        if (document["title"].toString()
                                .contains(searchVocabulary) || document["msg"].toString()
                                .contains(searchVocabulary) || document["nickname"].toString()
                                .contains(searchVocabulary)
                        ) {
                            countFound++

                            boardList.add(document.id)
                            boardData.add(BoardReadModel(
                                document["uid"] as String,
                                document["nickname"] as String,
                                document["title"] as String,
                                document["msg"] as String,
                                document["createdAt"] as Long
                            ))
                            boardSearchAdapter.notifyDataSetChanged()
                        }

                    }
                    if (countFound == 0) {
                        Toast.makeText(this, "'$searchVocabulary' 검색 결과가 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

        } else {

            db.collection("Board").whereGreaterThanOrEqualTo("title", searchVocabulary)
            db.collection("Board").orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {


                        if (document[category].toString().contains(searchVocabulary)) {
                            countFound++

                            boardList.add(document.id)
                            boardData.add(BoardReadModel(
                                document["uid"] as String,
                                document["nickname"] as String,
                                document["title"] as String,
                                document["msg"] as String,
                                document["createdAt"] as Long
                            ))
                            boardSearchAdapter.notifyDataSetChanged()
                        }

                    }
                    if (countFound == 0) {
                        Toast.makeText(this, "'$searchVocabulary'검색 결과가 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

        }
    }
}