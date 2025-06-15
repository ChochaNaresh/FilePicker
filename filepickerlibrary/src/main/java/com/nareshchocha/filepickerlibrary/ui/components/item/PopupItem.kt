package com.nareshchocha.filepickerlibrary.ui.components.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepickerlibrary.models.BaseConfig

@Composable
fun PopupItem(
    modifier: Modifier = Modifier,
    item: BaseConfig,
    itemView: @Composable ((item: BaseConfig) -> Unit)? = null,
    onItemClick: (BaseConfig) -> Unit
) {
    if (itemView != null) {
        itemView(item)
    } else {
        TextButton(
            onClick = { onItemClick(item) },
            modifier = modifier
        ) {
            Row {
                val iconRes = item.popUpIcon
                if (iconRes != null && iconRes != 0) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .height(24.dp)
                    )
                }
                Text(item.popUpText ?: "")
            }
        }
    }
}
