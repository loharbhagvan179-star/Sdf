# Quiz & Earn - Firebase & Android Setup Guide

**Quiz & Earn** is a modern Android application built using Kotlin, Jetpack Compose, and Firebase.
Users earn virtual coins by completing quizzes and tasks, and can redeem them for rewards (e.g., **1200 Coins = ₹20 Reward**).
The app comes equipped with full client and server-side validation, duplicate reward prevention, anti-abuse checks, and a separate Admin Dashboard.

---

## 1. Firebase Project Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project named **Quiz & Earn**.
2. Add an Android app with:
   - **Package Name**: `com.aistudio.quizearn.xkdf` (or matching your `applicationId` in `app/build.gradle.kts`)
   - Download `google-services.json` and place it in the `app/` directory (`/app/google-services.json`).
3. Enable the following services:
   - **Authentication**: Email/Password and Anonymous/Phone sign-in providers.
   - **Cloud Firestore**: Create database in production mode.
   - **Firebase Cloud Messaging** (Optional, for reward notifications).

---

## 2. Deploy Firestore Security Rules

Install the Firebase CLI and login:
```bash
npm install -g firebase-tools
firebase login
firebase use --add <your-firebase-project-id>
```

Deploy the included `firestore.rules`:
```bash
firebase deploy --only firestore:rules
```

---

## 3. Deploy Cloud Functions

Navigate to the `functions/` folder:
```bash
cd functions
npm install
firebase deploy --only functions
```

The functions deployed include:
- `claimDailyBonus`: 24-hour streak and cooldown validator (+25 coins).
- `submitQuiz`: Server-side answer scoring and duplicate reward prevention.
- `completeTask`: Backend validation crediting exactly +50 coins per task once.
- `submitRedeemRequest`: Validates 1200 coins minimum threshold for ₹20 reward and reserves funds.
- `adminApproveRedeemRequest`: Protected function assigning unspent codes from the vault.

---

## 4. App Architecture & Dual-Engine Operation

The app includes an **Adaptive Dual-Engine Repository**:
- When `google-services.json` is attached with live credentials, it automatically connects to Firebase Authentication & Cloud Firestore.
- While testing in development or offline, the in-app secure engine provides pre-seeded questions (GK, Science, Sports, Technology), tasks, redeem codes, and admin role switching so all features work immediately out of the box without setup blocks!

---

## 5. Admin Dashboard Access

- You can switch to the Admin demo by tapping **Admin Demo** on the Profile screen or using an account with `role: "admin"`.
- As Admin, you can:
  - Approve redemption requests (which automatically pulls and assigns a code from the code vault).
  - Reject requests (which refunds coins back to the user's wallet).
  - Add fresh gift card and redeem codes.
  - Block or unblock users.
  - View real-time platform statistics.
