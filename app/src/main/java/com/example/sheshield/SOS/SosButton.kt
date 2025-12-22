package com.example.sheshield.SOS

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sheshield.R

@Composable
fun SosButton(onSosClick:()->Unit){
    val img=painterResource(R.drawable.sos6)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = img,
            contentDescription = "sos button",
            modifier = Modifier.width(300.dp).height(300.dp).clickable { onSosClick() },

            )
    }
}