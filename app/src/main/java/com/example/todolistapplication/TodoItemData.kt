package com.example.todolistapplication

data class TodoItemData(
    val title: String,
    val deadline: String,
    val description: String,
    var isDone: Boolean = false,
    val imageUriStrings: List<String> = emptyList()
)