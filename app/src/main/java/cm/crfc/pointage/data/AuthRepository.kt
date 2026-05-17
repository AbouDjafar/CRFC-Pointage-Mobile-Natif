package cm.crfc.pointage.data

import cm.crfc.pointage.data.local.UserDao
import cm.crfc.pointage.model.OperationResult
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.util.genId
import cm.crfc.pointage.util.nowIso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val userDao: UserDao,
    private val sessionStorage: SessionStorage
) {
    fun observeUsers(): Flow<List<User>> = userDao.observeAll().map { items -> items.map { it.toDomain() } }

    fun observeCurrentUser(): Flow<User?> =
        combine(sessionStorage.currentUserId, observeUsers()) { currentId, users ->
            users.firstOrNull { it.id == currentId && it.isActive }
        }

    suspend fun getUserById(id: String): User? = userDao.getById(id)?.toDomain()

    suspend fun login(loginId: String, password: String): Boolean {
        val users = userDao.getAll().map { it.toDomain() }
        val normalized = loginId.trim().lowercase()
        val found = users.firstOrNull { user ->
            val shortLogin = user.email.substringBefore("@").lowercase()
            (user.email.lowercase() == normalized || shortLogin == normalized) &&
                user.password == password &&
                user.isActive
        }
        return if (found != null) {
            sessionStorage.saveCurrentUser(found.id)
            true
        } else {
            false
        }
    }

    suspend fun logout() {
        sessionStorage.clear()
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        jobTitle: String,
        password: String
    ): OperationResult {
        val normalizedEmail = email.trim().lowercase()
        if (!normalizedEmail.contains("@") || normalizedEmail.length < 5) {
            return OperationResult(false, "Email invalide.")
        }
        if (password.length < 6) {
            return OperationResult(false, "Mot de passe trop court (6 caracteres min).")
        }
        if (firstName.isBlank() || lastName.isBlank() || jobTitle.isBlank()) {
            return OperationResult(false, "Tous les champs sont obligatoires.")
        }
        if (userDao.getByEmail(normalizedEmail) != null) {
            return OperationResult(false, "Cet email est deja utilise.")
        }
        userDao.insert(
            User(
                id = genId(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = normalizedEmail,
                jobTitle = jobTitle.trim(),
                password = password,
                role = UserRole.AGENT,
                isActive = true,
                createdAt = nowIso()
            ).toEntity()
        )
        return OperationResult(true)
    }

    suspend fun updateProfile(
        currentUser: User,
        firstName: String,
        lastName: String,
        jobTitle: String,
        currentPassword: String? = null,
        newPassword: String? = null
    ): OperationResult {
        if (firstName.isBlank() || lastName.isBlank() || jobTitle.isBlank()) {
            return OperationResult(false, "Tous les champs sont obligatoires.")
        }
        if (!newPassword.isNullOrBlank()) {
            if (currentPassword != currentUser.password) {
                return OperationResult(false, "Mot de passe actuel incorrect.")
            }
            if (newPassword.length < 6) {
                return OperationResult(false, "Nouveau mot de passe trop court.")
            }
        }
        userDao.insert(
            currentUser.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                jobTitle = jobTitle.trim(),
                password = newPassword ?: currentUser.password
            ).toEntity()
        )
        return OperationResult(true)
    }

    suspend fun createUser(
        currentUser: User,
        firstName: String,
        lastName: String,
        email: String,
        jobTitle: String,
        password: String,
        role: UserRole
    ): OperationResult {
        if (currentUser.role != UserRole.ADMIN) {
            return OperationResult(false, "Acces refuse.")
        }
        val normalizedEmail = email.trim().lowercase()
        if (!normalizedEmail.contains("@") || normalizedEmail.length < 5) {
            return OperationResult(false, "Email invalide.")
        }
        if (password.length < 6) {
            return OperationResult(false, "Mot de passe trop court.")
        }
        if (firstName.isBlank() || lastName.isBlank() || jobTitle.isBlank()) {
            return OperationResult(false, "Tous les champs sont obligatoires.")
        }
        if (userDao.getByEmail(normalizedEmail) != null) {
            return OperationResult(false, "Cet email est deja utilise.")
        }
        userDao.insert(
            User(
                id = genId(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = normalizedEmail,
                jobTitle = jobTitle.trim(),
                password = password,
                role = role,
                isActive = true,
                createdAt = nowIso(),
                createdBy = currentUser.id
            ).toEntity()
        )
        return OperationResult(true)
    }

    suspend fun toggleUserActive(currentUser: User, targetId: String) {
        if (currentUser.role != UserRole.ADMIN || targetId == currentUser.id || targetId == "crfc-admin-001") return
        val user = userDao.getById(targetId)?.toDomain() ?: return
        userDao.insert(user.copy(isActive = !user.isActive).toEntity())
    }

    suspend fun deleteUser(currentUser: User, targetId: String) {
        if (currentUser.role != UserRole.ADMIN || targetId == currentUser.id || targetId == "crfc-admin-001") return
        userDao.deleteById(targetId)
    }
}
