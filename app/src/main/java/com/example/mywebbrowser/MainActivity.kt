package com.example.mywebbrowser

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
            HomeScreen(viewModel = viewModel)

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val focusManager = LocalFocusManager.current

    val (inputValue, setInputValue) = rememberSaveable {
        mutableStateOf("https://www.google.com")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "나만의 웹 브라우저") },
                actions = {
                    IconButton(onClick = {
                        viewModel.undo()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                    IconButton(onClick = { viewModel.redo() }) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = setInputValue,
                label = { Text(text = "https://") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.url.value = inputValue
                    focusManager.clearFocus()
                })
            )
            Spacer(modifier = Modifier.height(16.dp))
            MyWebView(viewModel)
        }
    }
}

@Composable
fun MyWebView(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            val webView = WebView(it)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()

            coroutineScope.launch {
                viewModel.undoSharedFlow.collectLatest {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                }
            }

            coroutineScope.launch {
                viewModel.redoSharedFlow.collectLatest {
                    if (webView.canGoForward()) {
                        webView.goForward()
                    }
                }
            }

            webView
        },
        update = { webView ->
            webView.loadUrl(viewModel.url.value)
        }
    )
}



