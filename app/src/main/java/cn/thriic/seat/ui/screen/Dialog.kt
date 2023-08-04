package cn.thriic.seat.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun Dialog(title: String,confirmText:String = "确定",onConfirm:()->Unit,onCancel:()->Unit,content:@Composable ()->Unit){
    AlertDialog(
        onDismissRequest = {
            onCancel()
        },
        title = {
            Text(text = title)
        },
        text = {
            content()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCancel()
                }
            ) {
                Text("取消")
            }
        }
    )
}