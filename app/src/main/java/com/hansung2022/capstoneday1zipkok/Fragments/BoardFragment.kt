package com.hansung2022.capstoneday1zipkok.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.Board.BoardReadAdapter
import com.hansung2022.capstoneday1zipkok.Board.BoardReadModel
import com.hansung2022.capstoneday1zipkok.Board.BoardWriteActivity
import com.hansung2022.capstoneday1zipkok.BoardSearch.BoardSearchActivity
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.FragmentBoardBinding

class BoardFragment : Fragment() {
    private var binding: FragmentBoardBinding? = null
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    //각종 게시글 데이터
    var boardData: ArrayList<BoardReadModel> = arrayListOf()

    //게시판 고유 아이디 리스트 형태로 받아오기 -> 누르면 해당 게시판 열리고 게시판 아이디에 맞게 정보 받아오
    var boardList: ArrayList<String> = arrayListOf()
    private lateinit var boardReadAdapter: BoardReadAdapter

    //댓글 갯수
    var commentCount : Int =0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Log.d("lifecycletest", "onCreateView")

        binding = FragmentBoardBinding.inflate(inflater, container, false)


        //게시판 찾기
        binding!!.ivSearchBoard.setOnClickListener {
            val intent = Intent(context, BoardSearchActivity::class.java)
            startActivity(intent)
        }

        //refresh
        binding!!.ivRefresh.setOnClickListener {
            boardList.clear()
            boardData.clear()
            readData()
        }







        auth = Firebase.auth
        val myUid = auth.currentUser?.uid.toString()

        //게시글 아답터
        boardReadAdapter = BoardReadAdapter(context!!, boardData, boardList, myUid)
        //boardReadAdapter.notifyDataSetChanged()


        binding!!.boardRecyclerView.adapter = boardReadAdapter
        binding!!.boardRecyclerView.layoutManager = LinearLayoutManager(context)


        binding!!.btWriteBoard.setOnClickListener {
            val intent = Intent(context, BoardWriteActivity::class.java)
            startActivity(intent)
        }

        //내 글
        binding!!.btMyThing.setOnClickListener {
            readMyData(myUid)
        }

        binding!!.btAllThing.setOnClickListener {
            boardList.clear()
            boardData.clear()
            readData()
        }

        return binding!!.root
    }
    //데이터 받아오는 함수. 받아 올 때 게시판 고유 번호도 받아옴. 나중에 댓글 사용할 때도 사용 할 듯.
    private fun readData() {


        db.collection("Board")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    //얘도 초기화 해야 됨
                    boardList.add(document.id)

                    //얘도 초기화
                    boardData.add(BoardReadModel(
                        document["uid"] as String,
                        document["nickname"] as String,
                        document["title"] as String,
                        document["msg"] as String,
                        document["createdAt"] as Long
                    ))
                    boardReadAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun readMyData(myUid : String) {
        boardData.clear()
        boardList.clear()


        db.collection("Board")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    if(document["uid"].toString().equals(myUid)){
                        //얘도 초기화 해야 됨
                        boardList.add(document.id)

                        //얘도 초기화
                        boardData.add(BoardReadModel(
                            document["uid"] as String,
                            document["nickname"] as String,
                            document["title"] as String,
                            document["msg"] as String,
                            document["createdAt"] as Long
                        ))
                    }


                    boardReadAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->

            }
    }






    //글 작성후에 다시 프래그먼트로 돌아왔을 때 새로 그리기
    override fun onResume() {
        super.onResume()
        Log.d("lifecycletest", "onResume")

        boardList.clear()
        boardData.clear()
        readData()


    }



    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("lifecycletest", "onDestroyView")


        binding = null
    }


}