package cn.thriic.seat.ui.screen.seatview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import cn.thriic.seat.R
import cn.thriic.seat.data.Person
import cn.thriic.seat.data.Seat
import cn.thriic.seat.data.SeatType
import cn.thriic.seat.toMatrix
import cn.thriic.seat.ui.screen.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatView(viewModel: SeatViewModel, navController: NavHostController) {
    viewModel.send(SeatIntent.UpdateSettings)
    val state by viewModel.uiState.collectAsState()

    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            if (!expanded) TopAppBar(
                title = {
                    Text(
                        "Seat",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (scale != 1f || offset != Offset.Zero) IconButton(onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    }) {
                        Icon(painterResource(R.drawable.restart_alt), "回到初始位置")
                    }
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "搜索")
                    }

                    IconButton(onClick = { navController.navigate("setting") }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "设置")
                    }

                }
            )
            else
                SearchBar(
                    placeHolderText = "输入姓名/学号",
                    defaultText = searchText,
                    onSearch = { searchText = it },
                    onBack = { expanded = false }
                )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!expanded) {

                val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale *= zoomChange
                    offset += offsetChange * scale
                }
                SeatCanvas(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformState),
                    viewModel = viewModel
                )
                if (state.select != null) {
                    BottomSheet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.background),
                        viewModel = viewModel
                    )
                }
            } else {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = innerPadding.calculateTopPadding())
                )
                SearchList(
                    innerPadding = innerPadding,
                    viewModel = viewModel,
                    searchText = searchText,
                    onClickItem = {
                        viewModel.send(SeatIntent.SearchClick(it))
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    defaultText: String = "",
    placeHolderText: String,
    onSearch: (text: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }
    var textValue by remember { mutableStateOf(defaultText) }
    val rotateClearIconDegree by animateFloatAsState(
        targetValue = if (textValue.isNotEmpty()) 90f else 0f,
        animationSpec = tween(durationMillis = 400)
    )
    Box(
        modifier = modifier
            .padding(horizontal = 5.dp)
            .height(64.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = textValue,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(textValue)
                }),
                onValueChange = {
                    textValue = it
                    onSearch(textValue)
                },
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "search",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 0.dp)
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (textValue.isEmpty()) {
                                Text(
                                    text = placeHolderText,
                                )
                            }
                            innerTextField()
                        }

                        AnimatedVisibility(visible = textValue.isNotEmpty()) {
                            IconButton(
                                onClick = { textValue = "" },
                                modifier = Modifier.rotate(rotateClearIconDegree)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "clear"
                                )
                            }
                        }

                    }
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxHeight()
                    .weight(1f),
                textStyle = TextStyle(fontSize = 18.sp)
            )
        }
    }
}

@Composable
fun SearchList(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    viewModel: SeatViewModel,
    searchText: String,
    onClickItem: (Int) -> Unit
) {
    val seatState by viewModel.seatState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = modifier,
        contentPadding = innerPadding
    ) {
        item {
            seatState.seats.forEachIndexed { index, seat ->
                if (seat.type == SeatType.Occupied && (seat.person?.name?.contains(searchText) == true || seat.person?.id?.contains(
                        searchText
                    ) == true)
                )
                    ListItem(
                        title = seat.person.name,
                        subscription = seat.person.id,
                        pos = with(index.toMatrix(seatState.size.second)) { "第${first}行 第${second}列" },
                        onClick = { onClickItem(index) }
                    )
            }
        }
    }
}

@Composable
fun ListItem(title: String, subscription: String, pos: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = subscription, fontWeight = FontWeight.Bold)
            Text(text = pos, fontWeight = FontWeight.Light, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheet(modifier: Modifier = Modifier, viewModel: SeatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val seatState by viewModel.seatState.collectAsState()
    val seat = seatState.seats[uiState.select!!]

    var openDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Divider(modifier = Modifier.fillMaxWidth())
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            val pos = uiState.select!!.toMatrix(seatState.size.second)
            Text(
                text = when (seat.type) {
                    SeatType.Unoccupied -> "无人"
                    SeatType.Occupied -> seat.person!!.name
                    SeatType.Disabled -> "未启用"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (seat.type == SeatType.Occupied) Text(text = "学号:" + seat.person!!.id)
            Text(text = "第${pos.first}行 第${pos.second}列")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                when (seat.type) {
                    SeatType.Unoccupied -> {
                        OutlinedButton(onClick = { openDialog = true }) {
                            Text("添加")
                        }
                        OutlinedButton(onClick = { viewModel.send(SeatIntent.SetAvailable(false)) }) {
                            Text("禁用")
                        }
                    }

                    SeatType.Occupied -> {
                        OutlinedButton(onClick = { openDialog = true }) {
                            Text("修改")
                        }
                        OutlinedButton(onClick = { viewModel.send(SeatIntent.SetPerson(null)) }) {
                            Text("删除")
                        }
                    }

                    SeatType.Disabled -> {
                        OutlinedButton(onClick = { viewModel.send(SeatIntent.SetAvailable(true)) }) {
                            Text("启用")
                        }
                    }
                }

            }
        }

    }

    if (openDialog) {
        var name by rememberSaveable { mutableStateOf(if (seat.person != null) seat.person.name else "") }
        var id by rememberSaveable { mutableStateOf(if (seat.person != null) seat.person.id else "") }
        Dialog(
            title = "修改",
            onConfirm = {
                viewModel.send(SeatIntent.SetPerson(Person(name, id)))
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("学号") },
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
}


@Composable
fun SeatCanvas(modifier: Modifier = Modifier, viewModel: SeatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val seatState by viewModel.seatState.collectAsState()

    val textMeasure = rememberTextMeasurer()
    val seatColor = arrayOf(
        MaterialTheme.colorScheme.surfaceTint,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.outlineVariant
    )
    val textColor = MaterialTheme.colorScheme.onSurface


    Canvas(
        modifier = modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        viewModel.send(SeatIntent.CanvasClick(tapOffset))
                    }
                )
            }
    ) {
        viewModel.send(SeatIntent.Init(size))
        seatState.seats.forEachIndexed { index, seats ->
            drawSeat(seats, seatColor[0], seatColor[1], seatColor[2], index == uiState.select)
//            if (seats.type == SeatType.Occupied) {
//                val fontPx = (seats.pos!!.endX - seats.pos.startX) / 4f //字体大小自适应?尽量
//                val text = buildAnnotatedString {
//                    withStyle(
//                        style = SpanStyle(
//                            color = textColor,
//                            fontSize = fontPx.toSp(),
//                            fontWeight = FontWeight.Bold,
//                        ),
//                    ) {
//                        append(seats.person!!.name)
//                    }
//                }
//                drawText(
//                    textMeasurer = textMeasure,
//                    text = text.substring(0, if (text.length > 3) 3 else text.length),
//                    topLeft = with(seats.pos) {
//                        Offset(
//                            (startX + endX) / 2 - fontPx * (if (seats.person!!.name.length > 3) 3 else seats.person.name.length) / 2,
//                            (startY + endY) / 2 - fontPx * 0.6f
//                        )
//                    },
//                )
//            }
        }


    }

}


private fun DrawScope.drawSeat(
    seat: Seat,
    selectColor: Color,
    occupiedColor: Color,
    unOccupiedColor: Color,
    select: Boolean = false
) {
    if (seat.type == SeatType.Disabled && !select) return
    val pos = seat.pos ?: return
    val width = (pos.endX - pos.startX) * 0.8f
    val padding = ((pos.endX - pos.startX) - width) / 2
    val boundStart = Offset(pos.startX, pos.startY) + Offset(padding, padding)
    val boundEnd = Offset(pos.endX, pos.endY) - Offset(padding, padding)
    val radius = width * 0.2f
    val strokeWidth = if (select) 6f else 4f

    val path = Path().apply {
        moveTo(boundStart.x, boundStart.y + radius)
        addArc(
            Rect(Offset(boundStart.x + radius, boundStart.y + radius), radius),
            180f,
            90f,
        )
        lineTo(boundEnd.x - radius, boundStart.y)
        arcTo(
            Rect(Offset(boundEnd.x - radius, boundStart.y + radius), radius),
            270f,
            90f,
            false
        )
        lineTo(boundEnd.x, boundEnd.y - radius)
        arcTo(Rect(Offset(boundEnd.x - radius, boundEnd.y - radius), radius), 0f, 90f, false)
        lineTo(boundStart.x + radius, boundEnd.y)
        arcTo(Rect(Offset(boundStart.x + radius, boundEnd.y - radius), radius), 90f, 90f, false)
        close()
    }
    drawPath(
        path,
        if (select) selectColor else if (seat.type == SeatType.Occupied) occupiedColor else unOccupiedColor,
        style = Stroke(strokeWidth)
    )

}


private fun DrawScope.drawSeatT(seat: Seat) {
    if (seat.type == SeatType.Disabled) return
    val pos = seat.pos ?: return
    val width = (pos.endX - pos.startX) * 0.8f
    val padding = ((pos.endX - pos.startX) - width) / 2
    val boundStart = Offset(pos.startX, pos.startY) + Offset(padding, padding)
    val boundEnd = Offset(pos.endX, pos.endY) - Offset(padding, padding)
    val radius = width * 0.2f
    val strokeWidth = 3f

    val path = Path().apply {
//        moveTo(70f, 840f)
//        quadraticBezierTo(57.25f, 840f, 48.63f, 831.33f)
//        quadraticBezierTo(40f, 822.65f, 40f, 809.83f)
//        quadraticBezierTo(40f, 797f, 48.63f, 788.5f)
//        quadraticBezierTo(57.25f, 780f, 70f, 780f)
//        lineTo(890f, 780f)
//        quadraticBezierTo(902.75f, 780f, 911.38f, 788.67f)
//        quadraticBezierTo(920f, 797.35f, 920f, 810.17f)
//        quadraticBezierTo(920f, 823f, 911.38f, 831.5f)
//        quadraticBezierTo(902.75f, 840f, 890f, 840f)
//        lineTo(70f, 840f)
//        close()
//        moveTo(140f, 720f)
//        quadraticBezierTo(116f, 720f, 98f, 702f)
//        quadraticBezierTo(80f, 684f, 80f, 660f)
//        lineTo(80f, 180f)
//        quadraticBezierTo(80f, 156f, 98f, 138f)
//        quadraticBezierTo(116f, 120f, 140f, 120f)
//        lineTo(820f, 120f)
//        quadraticBezierTo(844f, 120f, 862f, 138f)
//        quadraticBezierTo(880f, 156f, 880f, 180f)
//        lineTo(880f, 660f)
//        quadraticBezierTo(880f, 684f, 862f, 702f)
//        quadraticBezierTo(844f, 720f, 820f, 720f)
//        lineTo(140f, 720f)
//        close()
        moveTo(boundStart.x, boundStart.y + radius)
        arcTo(
            Rect(Offset(boundStart.x + radius, boundStart.y + radius), radius),
            180f,
            90f,
            false
        )
        lineTo(boundEnd.x - radius, boundStart.y)
        arcTo(
            Rect(Offset(boundEnd.x - radius, boundStart.y + radius), radius),
            270f,
            90f,
            false
        )
        lineTo(boundEnd.x, boundEnd.y * 0.75f - radius)
        arcTo(
            Rect(Offset(boundEnd.x - radius, boundEnd.y * 0.75f - radius), radius),
            0f,
            90f,
            false
        )
        lineTo(boundStart.x + radius, boundEnd.y * 0.75f)
        arcTo(
            Rect(Offset(boundStart.x + radius, boundEnd.y * 0.75f - radius), radius),
            90f,
            90f,
            false
        )
        close()
    }
    drawPath(
        path,
        Color.Black,
        style = Stroke(strokeWidth)
    )
}