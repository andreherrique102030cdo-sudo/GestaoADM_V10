package br.com.gestao.adm

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PeopleRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // A coleção usada pelo aplicativo é "pessoas".
    // A leitura aceita também o campo "nome" criado manualmente no primeiro teste.
    suspend fun list(): List<Person> {
        val snapshot = db.collection("pessoas").get().await()
        return snapshot.documents.map { doc ->
            Person(
                id = doc.id,
                name = doc.getString("name") ?: doc.getString("nome") ?: "",
                role = doc.getString("role") ?: "Membro",
                phone = doc.getString("phone") ?: doc.getString("telefone") ?: "",
                email = doc.getString("email") ?: "",
                birthDate = doc.getString("birthDate") ?: doc.getString("dataNascimento") ?: "",
                maritalStatus = doc.getString("maritalStatus") ?: doc.getString("estadoCivil") ?: "",
                active = doc.getBoolean("active") ?: true
            )
        }.filter { it.name.isNotBlank() }.sortedBy { it.name.lowercase() }
    }

    suspend fun create(person: Person): String {
        val ref = db.collection("pessoas").document()
        ref.set(person.copy(id = "")).await()
        return ref.id
    }
}

data class Person(
    val id: String = "",
    val name: String = "",
    val role: String = "Membro",
    val phone: String = "",
    val email: String = "",
    val birthDate: String = "",
    val maritalStatus: String = "",
    val active: Boolean = true
)
