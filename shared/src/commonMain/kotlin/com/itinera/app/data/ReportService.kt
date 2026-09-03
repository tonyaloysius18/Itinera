package com.itinera.app.data

import com.itinera.app.model.Report
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/**
 * Writes user abuse reports to the top-level `reports/{reportId}` collection so they
 * can be reviewed out-of-band (Firebase console / admin tooling). Reports are
 * write-only from the client — see the Firestore rule:
 *
 *   match /reports/{reportId} {
 *     allow create: if request.auth != null;
 *     allow read, update, delete: if false;
 *   }
 */
class ReportService {

    private val db = Firebase.firestore

    private fun reportsRef() = db.collection("reports")

    /** File a report. Throws on failure; callers wrap in try/catch. */
    suspend fun submitReport(report: Report) {
        reportsRef().document(report.id).set(report)
    }
}
