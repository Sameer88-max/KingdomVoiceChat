const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");

initializeApp();

const db = getFirestore();
const FIRST_NUMERIC_ID = 1000001;

exports.syncPublicProfile = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Login required.");
  }

  const uid = request.auth.uid;
  const name = String(request.data?.name || "").trim();
  const country = String(request.data?.country || "").trim();
  const gender = String(request.data?.gender || "Not specified").trim();
  const age = request.data?.age;

  if (!name || name.length > 40) {
    throw new HttpsError("invalid-argument", "Name must be 1–40 characters.");
  }

  if (country.length > 60) {
    throw new HttpsError("invalid-argument", "Country is too long.");
  }

  if (age !== undefined && (!Number.isInteger(age) || age < 1 || age > 120)) {
    throw new HttpsError("invalid-argument", "Invalid age.");
  }

  const userRef = db.collection("users").doc(uid);
  const publicRef = db.collection("publicProfiles").doc(uid);
  const counterRef = db.collection("appCounters").doc("userNumericId");

  const numericId = await db.runTransaction(async (tx) => {
    const userSnap = await tx.get(userRef);
    const publicSnap = await tx.get(publicRef);
    const counterSnap = await tx.get(counterRef);

    const existingId =
      userSnap.get("numericId") || publicSnap.get("numericId");

    let assignedId = existingId;

    const lastId = Number(counterSnap.get("lastId") || FIRST_NUMERIC_ID - 1);

    if (!assignedId) {
      assignedId = Math.max(lastId + 1, FIRST_NUMERIC_ID);
    }

    if (!Number.isSafeInteger(Number(assignedId)) ||
        Number(assignedId) < FIRST_NUMERIC_ID) {
      throw new HttpsError("failed-precondition", "Invalid existing numeric ID.");
    }

    tx.set(counterRef, {
      lastId: Math.max(lastId, Number(assignedId)),
      updatedAt: FieldValue.serverTimestamp()
    }, { merge: true });

    const profileData = {
      numericId: Number(assignedId),
      name,
      country,
      gender,
      updatedAt: FieldValue.serverTimestamp()
    };

    if (age !== undefined) {
      profileData.age = age;
    }

    tx.set(userRef, profileData, { merge: true });
    tx.set(publicRef, profileData, { merge: true });

    return Number(assignedId);
  });

  return {
    success: true,
    numericId
  };
});
