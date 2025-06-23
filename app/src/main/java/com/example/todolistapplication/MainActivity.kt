package com.example.todolistapplication

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.todolistapplication.ui.theme.TodolistApplicationTheme
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodolistApplicationTheme(darkTheme = true) {
                val context = LocalContext.current
                val todoList = remember { mutableStateListOf<TodoItemData>() }//主页面事项list
                var showAddDialog by remember { mutableStateOf(false) }//添加事项弹窗
                var showDeleteConfirmDialog by remember { mutableStateOf(false) }//一键删除所有事项弹窗
                var showCompleteDialog by remember { mutableStateOf(false) }//事项完成弹窗
                var completedTaskTitle by remember { mutableStateOf("") }//完成的事项标题

                //侧边栏
                val drawerState = rememberDrawerState(DrawerValue.Closed)//侧边栏状态
                val coroutineScope = rememberCoroutineScope()//协程？
                val savedLists = remember { mutableStateMapOf<String, List<TodoItemData>>() }//保存当前页面事项到侧边栏

                //侧边栏内容（待修改）
                val leftdrawerContent: @Composable () -> Unit = {
                    Column(modifier = Modifier.padding(16.dp)) {
                        savedLists.keys.forEach { name ->
                            Text(
                                text = name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        todoList.clear()
                                        todoList.addAll(savedLists[name] ?: emptyList())
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }


                // 读取本地数据
                LaunchedEffect(Unit) {
                    val storedList = FileStorageHelper.loadTodoList(context)
                    //todoList.clear()
                    todoList.addAll(storedList)
                    //todoList.addAll(sortTodoListByDeadline(storedList)) // 排序
                    todoList.sortWith(compareBy { it.deadline })

                }



                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            modifier = Modifier.statusBarsPadding(),
                            //侧边栏按钮
                            navigationIcon = {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "打开侧边栏")
                                }
                            },

                            //总标题
                            title = {
                                Text(
                                    text = "Todo List",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            actions = {

                                // 保存到侧边栏后清空当前页面
                                var showSaveDialog by remember { mutableStateOf(false) }
                                var saveListName by remember { mutableStateOf("") }

                                /*IconButton(onClick = {
                                    savedLists["todolist1"] = todoList.toList()
                                    todoList.clear()
                                     })
                                {
                                    Icon(Icons.Filled.Done, contentDescription = "保存")
                                }*/


                                //修改：通过用户输入一个名称
                                IconButton(onClick = { showSaveDialog = true }) {
                                    Icon(Icons.Filled.Done, contentDescription = "保存")
                                }
                                if (showSaveDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showSaveDialog = false },
                                        title = { Text("保存任务列表") },
                                        text = {
                                            OutlinedTextField(
                                                value = saveListName,
                                                onValueChange = { saveListName = it },
                                                label = { Text("列表名称") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                if (saveListName.isNotBlank()) {
                                                    savedLists[saveListName] = todoList.toList()
                                                    showSaveDialog = false
                                                }
                                            }) {
                                                Text("保存")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showSaveDialog = false }) {
                                                Text("取消")
                                            }
                                        }
                                    )
                                }


                                //添加一个事项
                                IconButton(onClick = { showAddDialog = true }) {
                                    Icon(Icons.Filled.Add, contentDescription = "添加任务")
                                }
                                //删除所有事项
                                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "删除全部")
                                }


                            }
                        )
                    }




                ) { innerPadding ->

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = leftdrawerContent
                    ) {

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        TodoListScreen(
                            todoList = todoList,
                            onCheckedChange = { index, checked ->
                                todoList[index] = todoList[index].copy(isDone = checked)
                                FileStorageHelper.saveTodoList(context, todoList)
                                if (checked) {
                                    completedTaskTitle = todoList[index].title
                                    showCompleteDialog = true
                                }
                            },
                            onDelete = { index ->
                                todoList.removeAt(index)
                                //todoList.clear()
                                //todoList.addAll(sortTodoListByDeadline(todoList)) // 排序
                                todoList.sortWith(compareBy { it.deadline })

                                FileStorageHelper.saveTodoList(context, todoList)


                            },
                            onSave = { index, updatedItem ->
                                todoList[index] = updatedItem

                                //todoList.clear()
                                //todoList.addAll(sortTodoListByDeadline(todoList)) // 排序
                                todoList.sortWith(compareBy { it.deadline })

                                FileStorageHelper.saveTodoList(context, todoList)
                            }
                        )

                    }
                    }

                    if (showAddDialog) {
                        AddTaskDialog(
                            onConfirm = { title, deadline,description ->
                                val newItem = TodoItemData(title, deadline,description, isDone = false)
                                todoList.add(newItem)

                                //todoList.clear()
                                todoList.addAll(sortTodoListByDeadline(todoList)) // 排序
                                todoList.sortWith(compareBy { it.deadline })

                                FileStorageHelper.saveTodoList(context, todoList)
                                showAddDialog = false
                            },
                            onDismiss = { showAddDialog = false }
                        )
                    }
                    if (showDeleteConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmDialog = false },
                            title = { Text("确认删除") },
                            text = { Text("确定要删除所有事项吗？此操作不可撤销。") },
                            confirmButton = {
                                TextButton(onClick = {
                                    todoList.clear()
                                    FileStorageHelper.saveTodoList(context, todoList)
                                    showDeleteConfirmDialog = false
                                }) {
                                    Text("确定")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                    Text("取消")
                                }
                            }
                        )
                    }

                }

                if (showCompleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showCompleteDialog = false },
                        title = {
                            Text("You did it!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                            )
                                },
                        text = {
                            Column( modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally)
                            {
                                Text("完成了：$completedTaskTitle")
                                Spacer(modifier = Modifier.height(8.dp))

                                Image(
                                    painter = painterResource(id = R.drawable.done),
                                    contentDescription = "完成",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(top = 8.dp)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCompleteDialog = false }) {
                                Text("确定")
                            }
                        }
                    )
                }

            }





            }
    }
}



@Composable
fun TodoListScreen(
    todoList: List<TodoItemData>,
    onCheckedChange: (index: Int, Boolean) -> Unit,
    onDelete: (index: Int) -> Unit,
    onSave: (index: Int, updatedItem: TodoItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(todoList.size) { index ->
            val itemindex = todoList[index]
            TodoItem(
                todoItem = itemindex,
                index = index,
                onCheckedChange = { checked -> onCheckedChange(index, checked) },
                onDeleteItem = { onDelete(index) },
                onSaveItem = { _, updated -> onSave(index, updated) }
            )
        }
    }
}



@Composable
fun TodoItem(
    todoItem: TodoItemData,
    index: Int,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onSaveItem: (Int, TodoItemData) -> Unit
) {
    var showDetail by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetail = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = todoItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (todoItem.isDone) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "截止日期：${todoItem.deadline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Checkbox(
                checked = todoItem.isDone,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }

    if (showDetail) {
        TodoItemDetailDialog(
            todoItem = todoItem,
            onDismiss = { showDetail = false },
            onDelete = {
                onDeleteItem(index)
                showDetail = false
            },
            onSave = { updatedItem ->
                onSaveItem(index, updatedItem)
                showDetail = false
            }
        )
    }
}




@Composable
fun TodoItemDetailDialog(
    todoItem: TodoItemData,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (TodoItemData) -> Unit
) {
    var title by remember { mutableStateOf(todoItem.title) }
    var deadline by remember { mutableStateOf(todoItem.deadline) }
    var description by remember { mutableStateOf(todoItem.description.ifEmpty { "" }) }


    var imageUriStrings by remember { mutableStateOf(todoItem.imageUriStrings ?: emptyList()) }
    //val imageUri = imageUriString?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }



    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            imageUriStrings = imageUriStrings + uris.map { it.toString() }
        }
    }



    val hasChanged = title != todoItem.title || deadline != todoItem.deadline || description != todoItem.description|| imageUriStrings != (todoItem.imageUriStrings ?: "")

    var showUnsavedDialog by remember { mutableStateOf(false) }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("提示") },
            text = { Text("编辑失败：未保存") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onDismiss()
                }) {
                    Text("关闭")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (hasChanged) {
                showUnsavedDialog = true
            } else {
                onDismiss()
            }
        },
        title = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                //singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors()
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    //singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 5,
                    colors = TextFieldDefaults.colors()



                )

                Spacer(modifier = Modifier.height(8.dp))

                imageUriStrings.forEach { uriStr ->
                    val painter = rememberAsyncImagePainter(model = Uri.parse(uriStr))
                    val isSelected = uriStr == selectedImageUri

                    Image(
                        painter = painter,
                        contentDescription = "已选图片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = 4.dp)
                            .clickable { selectedImageUri = if (isSelected) null else uriStr } // 切换选中状态
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Color.Red else Color.Transparent
                            )
                    )
                }





                Button(onClick = {
                    launcher.launch("image/*")
                }) {
                    Text("添加图片")
                }


                //删除图片
                Button(
                    onClick = {
                        selectedImageUri?.let {
                            imageUriStrings = imageUriStrings.filter { it != selectedImageUri }
                            selectedImageUri = null
                        }
                    },
                    enabled = selectedImageUri != null
                ) {
                    Text("删除图片")
                }


            }
        },





        confirmButton = {
            TextButton(
                onClick = {
                    if (hasChanged) {
                        onSave(todoItem.copy(
                            title = title,
                            deadline = deadline,
                            description = description,
                            imageUriStrings = imageUriStrings))
                        onDismiss()
                    }
                },
                enabled = hasChanged
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("删除", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    if (hasChanged) {
                        showUnsavedDialog = true
                    } else {
                        onDismiss()
                    }
                }) {
                    Text("关闭")
                }
            }
        }
    )
}



@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (taskName: String, deadline: String, description: String) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") } // 新增描述输入状态

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加任务") },
        text = {
            Column {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("名称") },
                    //singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("截止日期") },
                    //singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    //maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskName.isNotBlank() && deadline.isNotBlank()) {
                        onConfirm(taskName,deadline,description)
                    }
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}




@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    TodolistApplicationTheme(darkTheme = true) {
        val sampleTasks = listOf(
            TodoItemData("示例任务", "2025-06-20 12:00","", isDone = false)
        )
        TodoListScreen(
            todoList = sampleTasks,
            onCheckedChange = { index, checked -> },
            onDelete = { index -> },
            onSave = { index, updatedItem -> }
        )



    }
}

//跳转页面

//排序
fun sortTodoListByDeadline(todoList: List<TodoItemData>): List<TodoItemData> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return todoList.sortedBy { todoItem ->
        try {
            // 将字符串格式的截止日期转换为 Date 对象
            val date = dateFormat.parse(todoItem.deadline)
            date?.time ?: Long.MAX_VALUE // 如果解析失败，将该任务放在最后
        } catch (e: Exception) {
            Long.MAX_VALUE // 如果解析失败，将该任务放在最后
        }
    }
}
