package jp.techacademy.ayaka.kitano.qa_app

/*
title	Firebaseから取得したタイトル
body	Firebaseから取得した質問本文
name	Firebaseから取得した質問者の名前
uid	Firebaseから取得した質問者のUID
questionUid	Firebaseから取得した質問のUID
genre	質問のジャンル
imageBytes	Firebaseから取得した画像をbyte型の配列にしたもの
answers	Firebaseから取得した質問のモデルクラスであるAnswerのArrayList
 */

import java.io.Serializable
import java.util.ArrayList

class Question(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}