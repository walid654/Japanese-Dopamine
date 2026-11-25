package com.example.util

import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
    READ,
    WRITE,
    DELETE
}

object FirestoreErrorHelper {
    fun handleFirestoreError(e: Exception, operation: OperationType): String {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Permission denied for $operation. Please ensure you are signed in."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Network connection unavailable. Please check your internet."
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    "Requested item not found."
                FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                    "Item already exists."
                else -> "Cloud error (${e.code.name}): ${e.localizedMessage ?: "Unknown error"}"
            }
            else -> "Unexpected error: ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }
}
