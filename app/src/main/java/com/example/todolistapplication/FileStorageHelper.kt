package com.example.todolistapplication



import android.content.Context

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File



object FileStorageHelper {
    private const val FILE_NAME = "todo_list.json"

    fun saveTodoList(context: Context, list: List<TodoItemData>) {
        val jsonString = Gson().toJson(list)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(jsonString)
    }

    fun loadTodoList(context: Context): MutableList<TodoItemData> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()
        val jsonString = file.readText()
        val type = object : TypeToken<MutableList<TodoItemData>>() {}.type
        return Gson().fromJson(jsonString, type) ?: mutableListOf()
    }
}
