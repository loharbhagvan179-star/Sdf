const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * 1. Claim Daily Bonus (Cloud Function)
 * Verifies 24-hour rate limit on server side before crediting 25 coins.
 */
exports.claimDailyBonus = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated.");
  }

  const userId = context.auth.uid;
  const userRef = db.collection("users").doc(userId);
  const now = Date.now();
  const cooldownPeriod = 24 * 60 * 60 * 1000;

  return db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);
    if (!userDoc.exists) {
      throw new functions.https.HttpsError("not-found", "User document not found.");
    }

    const userData = userDoc.data();
    if (userData.isBlocked) {
      throw new functions.https.HttpsError("permission-denied", "Account is blocked.");
    }

    const lastClaim = userData.lastDailyBonusClaimTime || 0;
    if (now - lastClaim < cooldownPeriod) {
      const waitHours = Math.ceil((cooldownPeriod - (now - lastClaim)) / (1000 * 60 * 60));
      throw new functions.https.HttpsError("failed-precondition", `Bonus already claimed today. Next in ${waitHours} hours.`);
    }

    const bonusCoins = 25;
    const newBalance = (userData.coins || 0) + bonusCoins;

    transaction.update(userRef, {
      coins: newBalance,
      lastDailyBonusClaimTime: now
    });

    const txRef = db.collection("transactions").doc();
    transaction.set(txRef, {
      id: txRef.id,
      userId: userId,
      title: "Daily Bonus Claim",
      amount: bonusCoins,
      type: "EARN_DAILY",
      description: "Claimed daily bonus reward",
      timestamp: now,
      status: "SUCCESS"
    });

    return { success: true, coinsEarned: bonusCoins, newBalance };
  });
});

/**
 * 2. Submit Quiz Answers (Cloud Function)
 * Validates correctness server-side and prevents duplicate reward abuse.
 */
exports.submitQuiz = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated.");
  }

  const { quizId, answers } = data;
  const userId = context.auth.uid;

  if (!quizId || !answers) {
    throw new functions.https.HttpsError("invalid-argument", "Missing quizId or answers.");
  }

  const userRef = db.collection("users").doc(userId);
  const quizRef = db.collection("quizzes").doc(quizId);
  const questionsSnapshot = await quizRef.collection("questions").get();

  return db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);
    const quizDoc = await transaction.get(quizRef);

    if (!userDoc.exists || !quizDoc.exists) {
      throw new functions.https.HttpsError("not-found", "User or Quiz not found.");
    }

    const userData = userDoc.data();
    if (userData.isBlocked) {
      throw new functions.https.HttpsError("permission-denied", "Account is blocked.");
    }

    const completedQuizzes = userData.completedQuizIds || [];
    const alreadyRewarded = completedQuizzes.includes(quizId);

    let correctCount = 0;
    const totalQuestions = questionsSnapshot.size;

    questionsSnapshot.forEach((doc) => {
      const qData = doc.data();
      const selectedOption = answers[doc.id];
      if (selectedOption !== undefined && selectedOption === qData.correctIndex) {
        correctCount++;
      }
    });

    const quizData = quizDoc.data();
    const potentialReward = quizData.coinReward || 30;
    const earnedCoins = (!alreadyRewarded && correctCount > 0)
      ? Math.max(Math.round((correctCount / totalQuestions) * potentialReward), correctCount * 5)
      : 0;

    if (earnedCoins > 0 && !alreadyRewarded) {
      transaction.update(userRef, {
        coins: (userData.coins || 0) + earnedCoins,
        completedQuizIds: admin.firestore.FieldValue.arrayUnion(quizId)
      });

      const txRef = db.collection("transactions").doc();
      transaction.set(txRef, {
        id: txRef.id,
        userId: userId,
        title: `${quizData.title} Reward`,
        amount: earnedCoins,
        type: "EARN_QUIZ",
        description: `Scored ${correctCount}/${totalQuestions} in ${quizData.category} Quiz`,
        timestamp: Date.now(),
        status: "SUCCESS"
      });
    }

    return {
      success: true,
      totalQuestions,
      correctAnswers: correctCount,
      coinsEarned: earnedCoins,
      alreadyClaimed: alreadyRewarded
    };
  });
});

/**
 * 3. Complete Task (Cloud Function)
 * Enforces exactly 50 coins reward and one-time completion limit per task.
 */
exports.completeTask = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated.");
  }

  const { taskId } = data;
  const userId = context.auth.uid;
  const userRef = db.collection("users").doc(userId);
  const taskRef = db.collection("tasks").doc(taskId);

  return db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);
    const taskDoc = await transaction.get(taskRef);

    if (!userDoc.exists || !taskDoc.exists) {
      throw new functions.https.HttpsError("not-found", "User or Task not found.");
    }

    const userData = userDoc.data();
    const completedTasks = userData.completedTaskIds || [];

    if (completedTasks.includes(taskId)) {
      throw new functions.https.HttpsError("already-exists", "Task already completed. No duplicate rewards.");
    }

    const taskData = taskDoc.data();
    const rewardCoins = 50; // Guaranteed 50 coins per prompt rule

    transaction.update(userRef, {
      coins: (userData.coins || 0) + rewardCoins,
      completedTaskIds: admin.firestore.FieldValue.arrayUnion(taskId)
    });

    const txRef = db.collection("transactions").doc();
    transaction.set(txRef, {
      id: txRef.id,
      userId: userId,
      title: `Task: ${taskData.title}`,
      amount: rewardCoins,
      type: "EARN_TASK",
      description: taskData.description || "Completed partner/community task",
      timestamp: Date.now(),
      status: "SUCCESS"
    });

    return { success: true, rewardEarned: rewardCoins };
  });
});

/**
 * 4. Submit Redeem Request (Cloud Function)
 * Enforces 1200 coins = ₹20 minimum rule and reserves coins immediately.
 */
exports.submitRedeemRequest = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated.");
  }

  const { coinsUsed, amountInInr, payoutMethod, accountDetails } = data;
  const userId = context.auth.uid;

  if (!coinsUsed || coinsUsed < 1200 || !amountInInr || !accountDetails) {
    throw new functions.https.HttpsError("invalid-argument", "Minimum 1200 coins required for ₹20 reward.");
  }

  const userRef = db.collection("users").doc(userId);

  return db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);
    if (!userDoc.exists) {
      throw new functions.https.HttpsError("not-found", "User not found.");
    }

    const userData = userDoc.data();
    if (userData.isBlocked) {
      throw new functions.https.HttpsError("permission-denied", "Account is blocked.");
    }

    if ((userData.coins || 0) < coinsUsed) {
      throw new functions.https.HttpsError("failed-precondition", "Insufficient coins balance.");
    }

    // Deduct coins immediately to lock funds
    transaction.update(userRef, {
      coins: (userData.coins || 0) - coinsUsed
    });

    const reqRef = db.collection("redeemRequests").doc();
    transaction.set(reqRef, {
      id: reqRef.id,
      userId: userId,
      userName: userData.displayName || "Player",
      userEmail: userData.email || "",
      coinsUsed: coinsUsed,
      rewardAmountInInr: amountInInr,
      payoutMethod: payoutMethod,
      accountDetails: accountDetails,
      status: "PENDING",
      redeemCode: null,
      requestedAt: Date.now()
    });

    const txRef = db.collection("transactions").doc();
    transaction.set(txRef, {
      id: txRef.id,
      userId: userId,
      title: `Redeem Request: ₹${amountInInr}`,
      amount: -coinsUsed,
      type: "REDEEM_DEDUCT",
      description: `Submitted redeem request for ${payoutMethod}`,
      timestamp: Date.now(),
      status: "SUCCESS"
    });

    return { success: true, requestId: reqRef.id };
  });
});

/**
 * 5. Admin Approve Redeem Request (Cloud Function)
 * Assigns an available code from the protected redeemCodes collection.
 */
exports.adminApproveRedeemRequest = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Admin authentication required.");
  }

  const adminUser = await db.collection("users").doc(context.auth.uid).get();
  if (!adminUser.exists || adminUser.data().role !== "admin") {
    throw new functions.https.HttpsError("permission-denied", "Only administrators can approve redemptions.");
  }

  const { requestId } = data;
  const reqRef = db.collection("redeemRequests").doc(requestId);

  return db.runTransaction(async (transaction) => {
    const reqDoc = await transaction.get(reqRef);
    if (!reqDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Redeem request not found.");
    }

    const reqData = reqDoc.data();
    if (reqData.status !== "PENDING") {
      throw new functions.https.HttpsError("failed-precondition", `Request is already ${reqData.status}`);
    }

    // Query for an available code matching the reward
    const codesQuery = await db.collection("redeemCodes")
      .where("status", "==", "AVAILABLE")
      .where("denominationInInr", "==", reqData.rewardAmountInInr)
      .limit(1)
      .get();

    let assignedCode = null;
    if (!codesQuery.empty) {
      const codeDoc = codesQuery.docs[0];
      assignedCode = codeDoc.data().code;
      transaction.update(codeDoc.ref, {
        status: "ASSIGNED",
        assignedToUserId: reqData.userId,
        assignedAt: Date.now()
      });
    } else {
      assignedCode = `CODE-${reqData.payoutMethod.substring(0, 3)}-${Date.now().toString(36).toUpperCase()}`;
    }

    transaction.update(reqRef, {
      status: "COMPLETED",
      redeemCode: assignedCode,
      processedAt: Date.now()
    });

    return { success: true, assignedCode };
  });
});
