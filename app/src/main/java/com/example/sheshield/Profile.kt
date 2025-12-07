package com.example.sheshield

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ButtonDefaults



@Composable
fun ProfileScreen(){
    val scrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier.verticalScroll(scrollState).padding(bottom = 35.dp)) {
        profile_settings()
        Column(modifier = Modifier.padding(25.dp),verticalArrangement = Arrangement.spacedBy(16.dp)){
            Text("PERSONAL INFORMATION", color = Color.Gray)
            personal_info()
            Text("SAFETY SETTINGS", color = Color.Gray)
            safety_settings()
            Text("PRIVACY & SECURITY", color = Color.Gray)
            privacy_security()
            Text("SUPPORT & INFORMATION", color = Color.Gray)
            Support_info()
            verified_users()
            Button(modifier = Modifier
                .padding(vertical = 4.dp).fillMaxWidth(1f) .border(
                    width = 3.dp,
                    color = Color.Red,
                    shape = RoundedCornerShape(16.dp)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFCDD2)
                ),


                onClick ={} ) {
                Text("Log Out", color = Color.Red, modifier = Modifier.padding(5.dp), fontSize = 20.sp)
            }

        }


    }
}

@Composable
fun personal_info(){
    Column(
        modifier = Modifier.border(1.dp,Color.LightGray, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)

        ){
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.padding(10.dp),
                tint = Color.Gray
            )
            Column() {
                Text("Email")
                Text(text="sarahj@gmail.com",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){

            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone",
                modifier = Modifier.padding(10.dp),
                tint = Color.Gray
            )
            Column() {
                Text("Phone Number")
                Text(text="+ (880) 1712344568",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )

        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "address",
                modifier = Modifier.padding(10.dp),
                tint = Color.Gray
            )
            Column() {
                Text("Home Address")
                Text(text="house-23,sector-23,Road-12/C,Dhaka-1230",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
    }
}

@Composable
fun safety_settings(){
    Column(
        modifier = Modifier.border(1.dp,Color.LightGray, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)

        ){
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "SOS",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF9C27B0)

            )
            Column() {
                Text("SOS Triggers")
                Text(text="Shake,voice,power button",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Contacts",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF9C27B0)
            )
            Column() {
                Text("Trusted Contacts")
                Text(text="3 contacts addded",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )

        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "notification",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF9C27B0)
            )
            Column() {
                Text("Notifications")
                Text(text="Push,SMS,email alerts",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
    }
}

@Composable
fun privacy_security(){
    Column(
        modifier = Modifier.border(1.dp,Color.LightGray, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)

        ){
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security",
                modifier = Modifier.padding(10.dp),


            )
            Column() {
                Text("Privacy Settings")
                Text(text="Control your data",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Data",
                modifier = Modifier.padding(10.dp)
            )
            Column() {
                Text("Data & Storage")
                Text(text="Recording,report,history",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )

        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "app lock",
                modifier = Modifier.padding(10.dp),

            )
            Column() {
                Text("App Lock")
                Text(text="Require PIN or biometric",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Switch(
                checked = true,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF26C72B),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
        }
    }

}

@Composable
fun Support_info(){
    Column(
        modifier = Modifier.border(1.dp,Color.LightGray, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)

        ){
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Help",
                modifier = Modifier.padding(10.dp),


            )
            Column() {
                Text("Help Center")
                Text(text="FAQs and guides",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){

            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = "Support",
                modifier = Modifier.padding(10.dp),

            )
            Column() {
                Text("Contact Support")
                Text(text="Get help from our team",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )

        }
        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ){
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "About",
                modifier = Modifier.padding(10.dp),

            )
            Column() {
                Text("About SheShield")
                Text(text="Version 1.0.0",color=Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
            )
        }
    }
}

@Composable
fun verified_users(){
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFE1F5FE)).border(
                width = 2.dp,
                color = Color(0xFF64B5F6),
                shape = RoundedCornerShape(15.dp)
            ).padding(20.dp),


    ) {



                Row() {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier.padding(2.dp),
                        Color(0xFF2196F3),

                        )
                    Text("Verified User")
                }
                Text(text = "Your account is verified with email and phone number", color = Color.Gray,
                    fontSize = 15.sp)

                Row() {
                    Text(
                        text = "Add ID Verification ",
                        color = Color(0xFF1E88E5),

                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow",
                        tint=Color(color=0xFF1E88E5)
                    )
                }

            }






}

@Composable
fun profile_settings(){
    val image= painterResource(R.drawable.profile_pp)
    Surface(
        color= Color(0xFF7831A4),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 15.dp, bottomEnd = 15.dp),
        modifier = Modifier.padding(top = 30.dp).fillMaxWidth(1f)
    ) {
        Column(Modifier.padding(bottom = 10.dp)) {
            Text(
                "Profile & Settings",
                modifier = Modifier.padding(13.dp),
                color = Color.White,
                fontSize = 24.sp
            )
            Surface (
                color= Color(0xFF9955C5),
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart =10.dp, bottomEnd = 10.dp),
                modifier = Modifier.padding(20.dp).fillMaxWidth(1f),


                ){
                Row(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = image,
                        contentDescription = "Profile Picture"
                    )
                    Column() {
                        Text("Sarah Wilson", fontSize = 30.sp, color = Color.White)
                        Text("Member since november 2024",color=Color.LightGray)
                    }
                }

            }

        }

    }
}


