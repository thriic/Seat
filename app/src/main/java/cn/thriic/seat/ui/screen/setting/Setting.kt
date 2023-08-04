package cn.thriic.seat.ui.screen.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import cn.thriic.seat.share
import cn.thriic.seat.ui.screen.Dialog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingView(viewModel: SettingViewModel, navController: NavHostController) {
    val uiState by viewModel.uiState.collectAsState()
    var checked by remember { mutableStateOf(true) }
    var openDialog by remember { mutableStateOf(false) }
    var shareDialog by remember { mutableStateOf(false) }
    var importDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
            ) {
                item {
                    SettingSubTitle("基础")
                    SettingItem(
                        modifier = Modifier.clickable { openDialog = true },
                        title = "座位表规格",
                        description = "行x列"
                    ) {
                        Text(
                            modifier = it,
                            text = "${uiState.aspect.first}x${uiState.aspect.second}"
                        )
                    }
                    Divider(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )
                }

                item {
                    val context = LocalContext.current
                    SettingSubTitle("导出/导入")
                    SettingItem(
                        modifier = Modifier.clickable {
                            context.share(
                                content = Json.encodeToString(
                                    uiState.aspect
                                ) + "\n" + Json.encodeToString(uiState.seats)
                            )
                        },
                        title = "导出"
                    ) {

                    }
                    SettingItem(
                        modifier = Modifier.clickable { importDialog = true },
                        title = "导入",
                        description = "选取.seat或.txt文件 选择“导入座位表”打开"
                    ) {

                    }
                }
            }
        }
    )

    if (openDialog) {
        var length by rememberSaveable { mutableStateOf(uiState.aspect.first.toString()) }
        var width by rememberSaveable { mutableStateOf(uiState.aspect.second.toString()) }
        Dialog(
            title = "规格",
            onConfirm = {
                if (length.isNotEmpty() && width.isNotEmpty())
                    viewModel.send(SettingIntent.UpdateAspect(Pair(length.toInt(), width.toInt())))
                openDialog = false
            },
            onCancel = { openDialog = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(150.dp)
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = length,
                    singleLine = true,
                    onValueChange = { if (it.isNotEmpty() && it.isDigitsOnly()) length = it },
                    label = { Text("行数") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
                OutlinedTextField(
                    value = width,
                    singleLine = true,
                    onValueChange = { if (it.isDigitsOnly()) width = it },
                    label = { Text("列数") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
            }
        }
    }
    if (importDialog) {
        Dialog(
            title = "导入",
            onConfirm = {
                importDialog = false
            },
            onCancel = { importDialog = false }
        ) {
            Text(text = "请通过文件浏览器、qq等软件,\n打开后缀为'.txt'或'.seat'的座位表文件\n选择'导入座位表'完成导入")
        }
    }
}

@Composable
fun SettingItem(
    modifier: Modifier,
    title: String,
    description: String = "",
    content: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            if (description.isNotEmpty()) Text(
                text = description,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp
            )
        }
        content(Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun SettingSubTitle(title: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = title, color = MaterialTheme.colorScheme.surfaceTint, fontSize = 14.sp)
    }
}

