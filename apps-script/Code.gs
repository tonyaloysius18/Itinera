const SUPPORT_EMAIL = "ynotlabs.dev@gmail.com";
const FIREBASE_LOOKUP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=";
const ALLOWED_CATEGORIES = new Set(["problem", "suggestion", "general"]);
const ALLOWED_IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);
const MAX_ATTACHMENTS = 3;
const MAX_ATTACHMENT_BYTES = 5 * 1024 * 1024;
const MAX_REQUEST_BYTES = 8 * 1024 * 1024;

function doGet() {
  return json_({ok: true, service: "Itinera feedback"});
}

function doPost(event) {
  try {
    if (!event || !event.postData || !event.postData.contents) {
      throw publicError_("empty_request");
    }
    if (event.postData.length > MAX_REQUEST_BYTES) {
      throw publicError_("request_too_large");
    }

    const request = JSON.parse(event.postData.contents);
    const identity = verifyFirebaseUser_(request.idToken);
    enforceRateLimit_(identity.localId);
    const feedback = validateFeedback_(request);

    MailApp.sendEmail({
      to: SUPPORT_EMAIL,
      name: "Itinera Feedback",
      replyTo: identity.email || SUPPORT_EMAIL,
      subject: `[Itinera] ${titleCase_(feedback.category)} feedback`,
      body: buildMessage_(feedback, identity),
      attachments: feedback.attachments,
    });

    console.log(JSON.stringify({
      event: "feedback_delivered",
      uid: identity.localId,
      category: feedback.category,
      attachmentCount: feedback.attachments.length,
    }));
    return json_({ok: true});
  } catch (error) {
    const code = error && error.publicCode ? error.publicCode : "delivery_failed";
    console.error(JSON.stringify({event: "feedback_failed", code: code}));
    return json_({ok: false, error: code});
  }
}

function verifyFirebaseUser_(idToken) {
  if (typeof idToken !== "string" || idToken.length < 100 || idToken.length > 4096) {
    throw publicError_("authentication_required");
  }

  const apiKey = PropertiesService.getScriptProperties().getProperty("FIREBASE_WEB_API_KEY");
  if (!apiKey) throw publicError_("service_not_configured");

  const response = UrlFetchApp.fetch(FIREBASE_LOOKUP_URL + encodeURIComponent(apiKey), {
    method: "post",
    contentType: "application/json",
    payload: JSON.stringify({idToken: idToken}),
    muteHttpExceptions: true,
  });
  if (response.getResponseCode() !== 200) {
    throw publicError_("authentication_failed");
  }

  const result = JSON.parse(response.getContentText());
  const user = result.users && result.users[0];
  if (!user || !user.localId) throw publicError_("authentication_failed");
  return user;
}

function enforceRateLimit_(uid) {
  const cache = CacheService.getScriptCache();
  const key = "feedback-rate-" + uid;
  const attempts = Number(cache.get(key) || "0");
  if (attempts >= 3) throw publicError_("too_many_requests");
  cache.put(key, String(attempts + 1), 300);
}

function validateFeedback_(request) {
  if (!request || typeof request !== "object") throw publicError_("invalid_feedback");

  const category = safeText_(request.category, 20).toLowerCase();
  const message = safeText_(request.message, 1000);
  const attachments = Array.isArray(request.attachments) ? request.attachments : [];
  if (!ALLOWED_CATEGORIES.has(category) || message.length < 10) {
    throw publicError_("invalid_feedback");
  }
  if (attachments.length > MAX_ATTACHMENTS) throw publicError_("too_many_attachments");

  let totalBytes = 0;
  const blobs = attachments.map(function (attachment, index) {
    const mimeType = safeText_(attachment && attachment.mimeType, 80).toLowerCase();
    const base64 = attachment && typeof attachment.contentBase64 === "string"
      ? attachment.contentBase64
      : "";
    if (!ALLOWED_IMAGE_TYPES.has(mimeType) || !base64) {
      throw publicError_("invalid_attachment");
    }

    let bytes;
    try {
      bytes = Utilities.base64Decode(base64);
    } catch (_) {
      throw publicError_("invalid_attachment");
    }
    totalBytes += bytes.length;
    if (!bytes.length || totalBytes > MAX_ATTACHMENT_BYTES) {
      throw publicError_("attachments_too_large");
    }

    const fallback = `itinera-feedback-${index + 1}.jpg`;
    const fileName = safeFileName_(attachment.fileName, fallback);
    return Utilities.newBlob(bytes, mimeType, fileName);
  });

  return {
    category: category,
    message: message,
    includeAppDetails: request.includeAppDetails === true,
    appVersion: safeText_(request.appVersion, 40),
    platform: safeText_(request.platform, 120),
    attachments: blobs,
  };
}

function buildMessage_(feedback, identity) {
  const lines = [
    feedback.message,
    "",
    "---",
    `Category: ${titleCase_(feedback.category)}`,
    `User: ${identity.email || "No email"}`,
    `Firebase UID: ${identity.localId}`,
  ];
  if (feedback.includeAppDetails) {
    lines.push(`App version: ${feedback.appVersion || "Unknown"}`);
    lines.push(`Platform: ${feedback.platform || "Unknown"}`);
  }
  return lines.join("\n");
}

function safeText_(value, maxLength) {
  return typeof value === "string" ? value.trim().slice(0, maxLength) : "";
}

function safeFileName_(value, fallback) {
  const cleaned = safeText_(value, 100).replace(/[^A-Za-z0-9._-]/g, "-");
  return cleaned || fallback;
}

function titleCase_(value) {
  return value.charAt(0).toUpperCase() + value.slice(1);
}

function publicError_(code) {
  const error = new Error(code);
  error.publicCode = code;
  return error;
}

function json_(value) {
  return ContentService
    .createTextOutput(JSON.stringify(value))
    .setMimeType(ContentService.MimeType.JSON);
}
