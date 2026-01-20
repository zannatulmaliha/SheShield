const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Configure email transporter (using Gmail as example)
// IMPORTANT: Use Gmail App Password, not regular password
// Get it from: https://myaccount.google.com/apppasswords
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "your-sheshield-email@gmail.com", // Your SheShield email
    pass: "xxxx xxxx xxxx xxxx", // 16-character App Password
  },
});

/**
 * Send SOS Email Alerts to all contacts
 */
exports.sendSOSEmails = functions.https.onCall(async (data, context) => {
  const {userName, location, timestamp, contacts} = data;

  // Validate data
  if (!userName || !contacts || contacts.length === 0) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "Missing required fields"
    );
  }

  const emailPromises = contacts.map(async (contact) => {
    if (!contact.email || !contact.email.includes("@")) {
      console.log(`Skipping invalid email for ${contact.name}`);
      return null;
    }

    const mailOptions = {
      from: "SheShield Safety <your-sheshield-email@gmail.com>",
      to: contact.email,
      subject: "🚨 EMERGENCY ALERT - Immediate Assistance Required",
      html: `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <style>
            body {
              font-family: Arial, sans-serif;
              line-height: 1.6;
              margin: 0;
              padding: 0;
              background-color: #f4f4f4;
            }
            .container {
              max-width: 600px;
              margin: 20px auto;
              background: white;
              border-radius: 10px;
              overflow: hidden;
              box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            .alert-box {
              background: #ff4444;
              color: white;
              padding: 30px 20px;
              text-align: center;
            }
            .alert-box h1 {
              margin: 0 0 10px 0;
              font-size: 32px;
            }
            .alert-box h2 {
              margin: 0;
              font-size: 18px;
              font-weight: normal;
            }
            .content {
              padding: 30px 20px;
            }
            .info-box {
              background: #f9f9f9;
              padding: 20px;
              border-radius: 8px;
              margin: 20px 0;
            }
            .info-box h3 {
              margin-top: 0;
              color: #333;
            }
            .info-box ul {
              list-style: none;
              padding: 0;
            }
            .info-box li {
              padding: 8px 0;
              border-bottom: 1px solid #e0e0e0;
            }
            .info-box li:last-child {
              border-bottom: none;
            }
            .button {
              display: inline-block;
              background: #6000E9;
              color: white !important;
              padding: 15px 40px;
              text-decoration: none;
              border-radius: 25px;
              margin: 20px 0;
              font-weight: bold;
              text-align: center;
            }
            .actions {
              background: #fff3cd;
              padding: 20px;
              border-radius: 8px;
              margin: 20px 0;
              border-left: 4px solid #ffc107;
            }
            .actions h3 {
              margin-top: 0;
              color: #856404;
            }
            .footer {
              text-align: center;
              color: #666;
              padding: 20px;
              background: #f4f4f4;
              font-size: 12px;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="alert-box">
              <h1>🚨 EMERGENCY ALERT 🚨</h1>
              <h2>Immediate Assistance Required</h2>
            </div>

            <div class="content">
              <p><strong>Dear ${contact.name},</strong></p>

              <p style="font-size: 16px; color: #d32f2f;">
                <strong>${userName}</strong> has activated their emergency SOS alert
                through the SheShield Safety App and needs your help <strong>immediately</strong>.
              </p>

              <div class="info-box">
                <h3>📍 Emergency Details:</h3>
                <ul>
                  <li><strong>⏰ Time:</strong> ${timestamp}</li>
                  <li><strong>📍 Location:</strong> <a href="${location}" style="color: #6000E9;">View on Google Maps</a></li>
                  <li><strong>👤 Person:</strong> ${userName}</li>
                </ul>
              </div>

              <div style="text-align: center;">
                <a href="${location}" class="button">
                  📍 Open Location in Maps
                </a>
              </div>

              <div class="actions">
                <h3>⚠️ Immediate Actions Required:</h3>
                <ol style="margin: 10px 0; padding-left: 20px;">
                  <li><strong>Try to contact ${userName}</strong> immediately via phone</li>
                  <li><strong>Check their location</strong> using the map link above</li>
                  <li><strong>If unable to reach them,</strong> contact local emergency services</li>
                  <li><strong>Stay alert</strong> for updates from ${userName}</li>
                </ol>
              </div>

              <p style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; color: #666;">
                <small>
                  This is an automated emergency alert from the SheShield Safety App.
                  ${userName} has designated you as a trusted emergency contact.
                </small>
              </p>
            </div>

            <div class="footer">
              <p><strong>SheShield</strong> - Personal Safety Application</p>
              <p>This is an automated emergency notification. Please do not reply to this email.</p>
            </div>
          </div>
        </body>
        </html>
      `,
    };

    try {
      await transporter.sendMail(mailOptions);
      console.log(`Email sent successfully to ${contact.email}`);
      return {success: true, email: contact.email};
    } catch (error) {
      console.error(`Failed to send email to ${contact.email}:`, error);
      return {success: false, email: contact.email, error: error.message};
    }
  });

  const results = await Promise.all(emailPromises);
  const successCount = results.filter((r) => r?.success).length;

  return {
    success: true,
    message: `Sent ${successCount} out of ${contacts.length} emails`,
    results: results,
  };
});

/**
 * Send Push Notification to a specific FCM token
 */
exports.sendSOSNotification = functions.https.onCall(async (data, context) => {
  const {token, title, body, senderName, location} = data;

  if (!token) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "FCM token is required"
    );
  }

  const message = {
    token: token,
    notification: {
      title: title || "🚨 EMERGENCY ALERT",
      body: body || "Someone needs your help!",
    },
    data: {
      senderName: senderName || "Unknown",
      location: location || "",
      type: "SOS_ALERT",
      click_action: "FLUTTER_NOTIFICATION_CLICK",
    },
    android: {
      priority: "high",
      notification: {
        channelId: "SOS_ALERTS",
        priority: "max",
        sound: "default",
        defaultVibrateTimings: true,
      },
    },
    apns: {
      payload: {
        aps: {
          sound: "default",
          badge: 1,
        },
      },
    },
  };

  try {
    const response = await admin.messaging().send(message);
    console.log("Successfully sent notification:", response);
    return {success: true, messageId: response};
  } catch (error) {
    console.error("Error sending notification:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});

/**
 * Update user's FCM token when they log in
 */
exports.updateFCMToken = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be authenticated"
    );
  }

  const {fcmToken} = data;
  const userId = context.auth.uid;

  try {
    await admin.firestore()
        .collection("users")
        .doc(userId)
        .update({fcmToken: fcmToken});

    return {success: true, message: "FCM token updated"};
  } catch (error) {
    throw new functions.https.HttpsError("internal", error.message);
  }
});