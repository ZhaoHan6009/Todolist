package com.example.todolistapplication



import android.content.Context

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File



object FileStorageHelper {
    private const val FILE_NAME = "todo_list.json"//当前页面
    private const val SAVED_LISTS_FILE_NAME = "saved_lists.json"//侧边栏


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

    ////////
    fun saveListsToSider(context: Context, savedLists: Map<String, List<TodoItemData>>) {
        val jsonString = Gson().toJson(savedLists)
        val file = File(context.filesDir, SAVED_LISTS_FILE_NAME)
        file.writeText(jsonString)
    }

    fun loadListsToSider(context: Context): Map<String, List<TodoItemData>> {
        val file = File(context.filesDir, SAVED_LISTS_FILE_NAME)
        if (!file.exists()) return mapOf()
        val jsonString = file.readText()
        val type = object : TypeToken<Map<String, List<TodoItemData>>>() {}.type
        return Gson().fromJson(jsonString, type) ?: mapOf()
    }
}
