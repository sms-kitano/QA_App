package jp.techacademy.ayaka.kitano.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

//「お気に入り」質問の一覧画面
class FavoriteListActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mFavoriteArrayList: ArrayList<Question>
    private lateinit var mAdapter: FavoriteListAdapter
    private var mFavoriteRef: DatabaseReference? = null
    private var mQuestionRef: DatabaseReference? = null

    private var mGenre = 0

    var idList = mutableListOf<String>()
    var genreList = mutableListOf<String>()


    private val mFavoriteEventListener = object : ChildEventListener {
        //質問をFirebaseから取得
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            for (postSnapshot in dataSnapshot.children) {

            if(idList.contains(postSnapshot.key)) {
                var tempKey = postSnapshot.key
                //var map = postSnapshot!!.child(tempKey.toString())!!.value as Map<String, String>
                var map = postSnapshot.value as Map<String, String>
                val title = map["title"] ?: ""
                val body = map["body"] ?: ""
                val name = map["name"] ?: ""
                val uid = map["uid"] ?: ""
                val imageString = map["image"] ?: ""
                val bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val answerArrayList = ArrayList<Answer>()
                val answerMap = map["answers"] as Map<String, String>?
                if (answerMap != null) {
                    for (key in answerMap.keys) {
                        val temp = answerMap[key] as Map<String, String>
                        val answerBody = temp["body"] ?: ""
                        val answerName = temp["name"] ?: ""
                        val answerUid = temp["uid"] ?: ""
                        val answer = Answer(answerBody, answerName, answerUid, key)
                        answerArrayList.add(answer)
                    }
                }

                val favorite = Question(
                    title, body, name, uid, dataSnapshot.key ?: "",
                    mGenre, bytes, answerArrayList
                )
                mFavoriteArrayList.add(favorite)
            }}
            mAdapter.notifyDataSetChanged()
        }


        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_list)


        // UIの初期設定
        title = "「お気に入り」一覧"

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser

        // ListViewの準備
        mListView = findViewById(R.id.favoritelistView)
        mAdapter = FavoriteListAdapter(this)
        mFavoriteArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mAdapter.setFavoriteArrayList(mFavoriteArrayList)
        mListView.adapter = mAdapter

        // リスナーを登録する
        mFavoriteRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid).child(QuestionPATH)

        //お気に入りをFirebaseから取得
        mFavoriteRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mFavoriteArrayList.clear()

                for (postSnapshot in dataSnapshot.children) {
                    val map = postSnapshot.value as Map<String, String>
                    val questionid = map["questionid"] ?: ""
                    val genre = map["genre"] ?: ""

                    idList.add(questionid)
                    genreList.add(genre)
                }

                mQuestionRef = mDatabaseReference.child(ContentsPATH)

                mQuestionRef!!.addChildEventListener(mFavoriteEventListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })




        //お気に入りの質問をタップすると質問詳細画面へ
        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mFavoriteArrayList[position])
            startActivity(intent)
        }

    }
}
