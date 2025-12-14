package com.example.sheshield
import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.example.sheshield.SOS.SosButton
import com.example.sheshield.SOS.SosCountDown
import com.example.sheshield.TimedCheckIn


@Composable
fun HomeScreen(){
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeContent(
            onCardTwoClick = { currentScreen = "timedCheckIn" },
            onCardFiveClick = { currentScreen = "responders" }
            )
        "timedCheckIn" -> TimedCheckIn(onNavigate = { currentScreen = it },
            onBack = { currentScreen = "home" }
        )
        "responders" -> RespondersNearMeScreen()
    }
}

@Composable
fun HomeContent(onCardTwoClick: () -> Unit,onCardFiveClick:()->Unit){

    val scrollState = rememberScrollState()
    var showCountdown by remember { mutableStateOf(false) }


    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(scrollState).padding(bottom = 35.dp)) {

        top_bar()


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {

            if (!showCountdown) {
                // NORMAL SOS BUTTON
                SosButton {
                    showCountdown = true
                }
            } else {
                // COUNTDOWN REPLACES BUTTON
                SosCountDown(
                    onFinish = {
                        showCountdown = false
                        //  CALL FIREBASE SOS HERE
                    },
                    onCancel = {
                        showCountdown = false
                    }
                )
            }
        }

        Column(modifier = Modifier.padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {


            Text("Quick Action",fontSize = 18.sp)
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)) {


                Column(verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.weight(1f)) {
                    cardOne()
                    cardThree()
                }
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)
                ,   modifier = Modifier.weight(1f)) {
                    cardTwo(onClick = { onCardTwoClick() })
                    cardFour()
                }

            }
            Box(
           modifier = Modifier.padding(horizontal = 75.dp).height(170.dp)

            ){
            cardFive(onClick={onCardFiveClick()})
            }

            safe_box()

            Text("Recent Activity",fontSize = 18.sp)

        }


    }
}


@Composable
fun safe_box(){
    Column(modifier = Modifier.fillMaxWidth().border(width = 1.dp,color=Color(0xFFFFBF00), shape= RoundedCornerShape(15.dp))
        .background(color = Color(0xFFFFFDE7), shape = RoundedCornerShape(15.dp)).padding(12.dp)) {
        Row(){
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Safety",
                modifier = Modifier.padding(4.dp),
                tint = Color(0xFFFFBF00)
            )
            Column( verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Safety Alert")
                Text("Caution advised in Downtown area (8-10 PM). 2 incidents reported this week.",
                    fontSize = 12.sp, color = Color.Gray)

            }

        }
    }
}

@Composable
fun top_bar(){
    val image= painterResource(R.drawable.shield2)
    Surface(
        color= Color(0xFF6000E9),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 15.dp, bottomEnd = 15.dp),
        modifier = Modifier.padding(top= 30.dp).fillMaxWidth(1f)
    ) {
        Column(Modifier.padding(all = 10.dp)) {
            Row() {

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "SheShield",
                        Modifier.padding(bottom = 5.dp),
                        color = Color.White,
                        fontSize = 25.sp
                    )
                    Text(text = "You're protected", color = Color.White, fontSize = 15.sp)
                }

                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.padding(20.dp),
                    tint = Color.White
                )
            }
            Surface (
                color= Color(0xFF7A4BFA),
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart =10.dp, bottomEnd = 10.dp),
                modifier = Modifier.padding(5.dp).fillMaxWidth(1f),


                ){
                Row(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = image,
                        contentDescription = "Shield Picture",
                        modifier = Modifier.width(65.dp).height(65.dp)
                    )
                    Column() {
                        Text("Safety Status: Active", color = Color.White)
                        Text("3 trusted contacts .Gps enabled",color=Color.LightGray)
                    }
                }

            }

        }

    }
}

@Composable
fun cardOne() {
    val image = painterResource(R.drawable.loc)
    Column(
        modifier = Modifier.border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp).fillMaxWidth().height(150.dp)
    ) {

        Image(
            painter = image,
            contentDescription = "location",
            modifier = Modifier.width(90.dp).height(90.dp)

        )
        Column() {
            Text("Track My Route")
            Text("Share live location", color = Color.Gray,fontSize = 14.sp)
        }
    }
}

    @Composable
    fun cardTwo(onClick: () -> Unit) {
        val image = painterResource(R.drawable.clock)
        Column(
            modifier = Modifier.border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
                .padding(10.dp).fillMaxWidth().height(150.dp).clickable{onClick()}
        ) {

            Image(
                painter = image,
                contentDescription = "Check in",
                modifier = Modifier.width(100.dp).height(100.dp)

            )
            Column() {
                Text("Timed Check-In")
                Text("Set safety timer", color = Color.Gray,fontSize = 14.sp)
            }
        }
    }
    @Composable
    fun cardThree() {
            val image = painterResource(R.drawable.electric)
            Column(
                modifier = Modifier.border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
                    .padding(10.dp).fillMaxWidth().height(150.dp)
            ) {

                Image(
                    painter = image,
                    contentDescription = "sos",
                    modifier = Modifier.width(90.dp).height(90.dp)

                )
                Column() {
                    Text("SOS Trigger")
                    Text("Configure alerts", color = Color.Gray, fontSize = 14.sp)
                }
            }
    }

@Composable
fun cardFour() {
    val image = painterResource(R.drawable.signal)
    Column(
        modifier = Modifier.border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp).fillMaxWidth().height(150.dp)
    ) {

        Image(
            painter = image,
            contentDescription = "map",
            modifier = Modifier.width(90.dp).height(90.dp)

        )
        Column() {
            Text("Safety Map")
            Text("View risk areas", color = Color.Gray,fontSize = 14.sp)
        }
    }
}

@Composable
fun cardFive(onClick:()->Unit) {
    val image = painterResource(R.drawable.people)
    Column(
        modifier = Modifier.border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp).fillMaxWidth().height(200.dp).clickable{onClick()}
    ) {

        Image(
            painter = image,
            contentDescription = "Responders",
            modifier = Modifier.width(90.dp).height(90.dp)

        )
        Column() {
            Text("Responders near me")
            Text("Find verified helpers", color = Color.Gray,fontSize = 14.sp)
        }
    }
}











