package com.ndejje.mycampusconnect.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ndejje.mycampusconnect.models.Club
import com.ndejje.mycampusconnect.models.ClubCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

object ClubsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val clubsCollection = db.collection("clubs")

    // Get all clubs from Firestore
    suspend fun getAllClubs(): List<Club> {
        return try {
            val snapshot = clubsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject<Club>()?.copy(clubId = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Get clubs as Flow (for real-time updates)
    fun getAllClubsFlow(): Flow<List<Club>> = flow {
        val snapshot = clubsCollection.get().await()
        val clubs = snapshot.documents.mapNotNull { doc ->
            doc.toObject<Club>()?.copy(clubId = doc.id)
        }
        emit(clubs)
    }

    // Get clubs by category
    suspend fun getClubsByCategory(category: String): List<Club> {
        return try {
            val snapshot = clubsCollection
                .whereEqualTo("category", category)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject<Club>()?.copy(clubId = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Get single club by ID
    suspend fun getClubById(clubId: String): Club? {
        return try {
            val doc = clubsCollection.document(clubId).get().await()
            doc.toObject<Club>()?.copy(clubId = doc.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Join a club
    suspend fun joinClub(clubId: String, userId: String): Boolean {
        return try {
            // Add user to club's members subcollection
            db.collection("clubs")
                .document(clubId)
                .collection("members")
                .document(userId)
                .set(mapOf("joinedAt" to System.currentTimeMillis()))
                .await()

            // Increment member count
            clubsCollection.document(clubId)
                .update("memberCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Leave a club
    suspend fun leaveClub(clubId: String, userId: String): Boolean {
        return try {
            db.collection("clubs")
                .document(clubId)
                .collection("members")
                .document(userId)
                .delete()
                .await()

            clubsCollection.document(clubId)
                .update("memberCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Check if user is a member
    suspend fun isUserMember(clubId: String, userId: String): Boolean {
        return try {
            val doc = db.collection("clubs")
                .document(clubId)
                .collection("members")
                .document(userId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}