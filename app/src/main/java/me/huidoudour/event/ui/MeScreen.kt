package me.huidoudour.event.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import me.huidoudour.event.R

/**
 * 关于页面 Compose 组件：对齐原 XML 布局 activity_me.xml
 *
 * 布局结构（无 Toolbar）：
 *   - 3 个 TextView 垂直居中排列（packed chain）
 *   - Button 定位到底部（marginBottom=80dp）
 */
@Composable
fun MeScreenContent() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // 中间文本区域
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // textView1 - hello_world（XML无textAppearance，默认大小）
            Text(
                text = stringResource(R.string.hello_world),
                style = MaterialTheme.typography.bodyLarge
            )
            // textView3 - about_description
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // textView2 - about_developer
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_developer),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // 底部 GitHub 按钮 - 对齐 XML: light_pink背景, black文字, 135dp宽, marginBottom=80dp
        Button(
            onClick = {
                val url = "https://github.com/huidoudour"
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            },
            modifier = Modifier
                .width(135.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB6C1), // light_pink
                contentColor = Color(0xFF000000)     // black
            )
        ) {
            Text(stringResource(R.string.visit_github))
        }
    }
}
