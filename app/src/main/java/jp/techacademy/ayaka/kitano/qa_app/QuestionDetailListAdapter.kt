package jp.techacademy.ayaka.kitano.qa_app

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.list_question_detail.*

class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDataBaseReference: DatabaseReference

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val user = FirebaseAuth.getInstance().currentUser
        val questionid = mQustion.questionUid

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                    convertView =
                        mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }

            // FirebaseAuthのオブジェクトを取得する
            mAuth = FirebaseAuth.getInstance()
            mDataBaseReference = FirebaseDatabase.getInstance().reference

            val favorite_button = convertView.findViewById<View>(R.id.favorite_button)
            val favorite_button2 = convertView.findViewById<View>(R.id.favorite_button2)
            //ログインしている場合に質問詳細画面に「お気に入り」ボタンを表示
            if(user == null){
                favorite_button.visibility = View.INVISIBLE
                favorite_button2.visibility = View.INVISIBLE
            } else {
                //ログインしている場合に「お気に入り」が既にタップされているかどうかをボタンの見た目で判断できるようにする
                val favoriteRef = mDataBaseReference.child(FavoritePATH).child(user!!.uid).child(QuestionPATH).child(questionid)
                favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value != null){
                            favorite_button.visibility = View.INVISIBLE
                            favorite_button2.visibility = View.VISIBLE
                        } else {
                            favorite_button.visibility = View.VISIBLE
                            favorite_button2.visibility = View.INVISIBLE
                        }
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                })
            }

            //「お気に入り」ボタンが押されたら、お気に入りに登録
            favorite_button.setOnClickListener {
                val favoriteRef = mDataBaseReference.child(FavoritePATH).child(user!!.uid).child(QuestionPATH).child(questionid)
                favorite_button.visibility = View.INVISIBLE
                favorite_button2.visibility = View.VISIBLE

                val data = HashMap<String, String>()
                data["title"] = mQustion.title
                data["genre"] = mQustion.genre.toString()
                data["questionid"] = mQustion.questionUid
                favoriteRef.setValue(data)//pushにすると謎のハッシュ値ができてしまう
            }

            //既にお気に入りに登録済であれば、お気に入りから削除
            favorite_button2.setOnClickListener {
                val favoriteRef = mDataBaseReference.child(FavoritePATH).child(user!!.uid).child(QuestionPATH).child(questionid)
                favorite_button.visibility = View.VISIBLE
                favorite_button2.visibility = View.INVISIBLE

                favoriteRef.removeValue()
            }

            val body = mQustion.body
            val name = mQustion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQustion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQustion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }
}