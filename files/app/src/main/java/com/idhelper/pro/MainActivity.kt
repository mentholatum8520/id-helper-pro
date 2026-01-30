package com.idhelper.pro

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import okhttp3.*
import java.io.InputStream

class MainActivity : ComponentActivity() {
    // 建立一個 OKHttp 客戶端，用來維持 Cookie (Session)
    private val client = OkHttpClient.Builder().cookieJar(object : CookieJar {
        private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) { cookieStore[url] = cookies }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url] ?: listOf()
    }).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        
        var idNum by remember { mutableStateOf("") }
        var year by remember { mutableStateOf("") }
        var month by remember { mutableStateOf("") }
        var day by remember { mutableStateOf("") }
        var captchaImg by remember { mutableStateOf<Bitmap?>(null) }
        var captchaInput by remember { mutableStateOf("") }
        var webResponse by remember { mutableStateOf("尚未連線至官網") }

        // 1. 圖片選取器
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val image = InputImage.fromFilePath(context, it)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image).addOnSuccessListener { result ->
                    // 簡單辨識邏輯：抓取符合格式的字串
                    idNum = "[A-Z][12][0-9]{8}".toRegex().find(result.text)?.value ?: ""
                    webResponse = "辨識完成，正在獲取官網驗證碼..."
                    // 辨識成功後，自動去官網拿驗證碼
                    scope.launch { captchaImg = fetchCaptcha() }
                }
            }
        }

        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("查發證小助手 Pro", style = MaterialTheme.typography.headlineLarge)
            
            Button(onClick = { launcher.launch("image/*") }, Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("📷 讀取證件照片 / 拍照")
            }

            // 辨識出的文字呈現
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("辨識結果 (可手動修正)", style = MaterialTheme.typography.titleSmall)
                    TextField(value = idNum, onValueChange = {idNum = it}, label = {Text("身分證字號")})
                    Row {
                        TextField(value = year, onValueChange = {year = it}, label = {Text("年")}, modifier = Modifier.weight(1f))
                        TextField(value = month, onValueChange = {month = it}, label = {Text("月")}, modifier = Modifier.weight(1f))
                        TextField(value = day, onValueChange = {day = it}, label = {Text("日")}, modifier = Modifier.weight(1f))
                    }
                }
            }

            // 驗證碼呈現
            if (captchaImg != null) {
                Text("驗證碼 (從內政部官網獲取):")
                Image(bitmap = captchaImg!!.asImageBitmap(), contentDescription = "Captcha", Modifier.height(60.dp))
                TextField(value = captchaInput, onValueChange = {captchaInput = it}, label = {Text("輸入上方驗證碼")}, modifier = Modifier.fillMaxWidth())
                
                Button(onClick = {
                    scope.launch {
                        webResponse = "查詢中..."
                        webResponse = submitToGov(idNum, year, month, day, captchaInput)
                    }
                }, Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("送出查詢並獲取結果")
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("官網回傳資料：", style = MaterialTheme.typography.titleMedium)
            Text(webResponse, color = MaterialTheme.colorScheme.primary)
        }
    }

    // 核心邏輯：抓取官網驗證碼
    private suspend fun fetchCaptcha(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 內政部驗證碼 URL (需要維持同一個 Session)
            val request = Request.Builder().url("https://www.ris.gov.tw/app/portal/captcha/image?time=${System.currentTimeMillis()}").build()
            val response = client.newCall(request).execute()
            val inputStream: InputStream = response.body?.byteStream() ?: return@withContext null
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) { null }
    }

    // 核心邏輯：送出查詢
    private suspend fun submitToGov(id: String, y: String, m: String, d: String, code: String): String = withContext(Dispatchers.IO) {
        try {
            // 這裡模擬 POST 請求到 ris.gov.tw (實際欄位名稱依官網為準)
            val formBody = FormBody.Builder()
                .add("id
