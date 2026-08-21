package br.com.gestao.adm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun signIn(email: String, password: String): Result<UserProfile> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("Usuário não encontrado.")
        val snap = db.collection("users").document(uid).get().await()
        val roleText = snap.getString("role") ?: error("Perfil de acesso não configurado.")
        val role = Role.from(roleText)
        UserProfile(uid, snap.getString("name") ?: email, role)
    }

    fun signOut() = auth.signOut()

    suspend fun currentProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val snap = db.collection("users").document(uid).get().await()
        val role = snap.getString("role")?.let(Role::from) ?: return null
        return UserProfile(uid, snap.getString("name") ?: "", role)
    }
}

enum class Role(val label: String) {
    ADMINISTRADOR("Administrador"),
    PASTOR("Pastor"),
    SECRETARIA("Secretaria"),
    LIDER("Líder"),
    MEMBRO("Membro");

    companion object {
        fun from(value: String): Role = entries.firstOrNull {
            it.name.equals(value.trim(), ignoreCase = true)
        } ?: error("Perfil de acesso inválido: $value")
    }
}

data class UserProfile(val uid: String, val name: String, val role: Role)
