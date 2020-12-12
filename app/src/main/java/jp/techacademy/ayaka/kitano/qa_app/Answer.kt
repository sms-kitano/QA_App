package jp.techacademy.ayaka.kitano.qa_app

/*
body	Firebaseから取得した回答本文
name	Firebaseから取得した回答者の名前
uid	Firebaseから取得した回答者のUID
answerUid	Firebaseから取得した回答のUID
 */

import java.io.Serializable

class Answer (val body: String, val name: String, val uid: String, val answerUid: String) : Serializable
